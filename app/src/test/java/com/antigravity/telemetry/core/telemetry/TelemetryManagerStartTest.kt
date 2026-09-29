package com.antigravity.telemetry.core.telemetry

import com.antigravity.telemetry.core.model.VehicleHardwareState
import com.antigravity.telemetry.core.repository.FuelPreferences
import com.antigravity.telemetry.core.repository.TelemetryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryManagerStartTest {

    private val repository = mockk<TelemetryRepository>(relaxed = true)
    private val preferences = mockk<FuelPreferences>(relaxed = true)
    private val vehicleHardwareStateFlow = MutableStateFlow(VehicleHardwareState(isConnected = false))
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var telemetryManager: TelemetryManager

    @Before
    fun setup() {
        every { repository.preferences } returns preferences
        every { repository.vehicleHardwareState } returns vehicleHardwareStateFlow
        every { repository.getLatestActualOdometer() } returns 9284.0
        every { preferences.getColdStartThresholdHours() } returns 3.5f

        telemetryManager = TelemetryManager(
            repository = repository,
            scope = testScope
        )
    }

    @Test
    fun `initial connection with no previous disconnect logs COLD START`() = testScope.runTest {
        // Given no previous disconnect (0L) and no start logged (0L)
        every { preferences.getLastAutoDisconnectedTimestamp() } returns 0L
        every { preferences.getLastEngineStartLoggedTimestamp() } returns 0L

        // When connected
        telemetryManager.onConnectionStateChanged(true)
        advanceUntilIdle()

        // Then cold start is logged
        coVerify(exactly = 1) {
            repository.logEngineStart(
                isColdStart = true,
                odometerKm = 9284.0,
                timestamp = any()
            )
        }
        verify { preferences.setLastEngineStartLoggedTimestamp(any()) }
    }

    @Test
    fun `connection after 4 hours cooldown logs COLD START`() = testScope.runTest {
        val now = System.currentTimeMillis()
        val fourHoursAgo = now - (4 * 60 * 60 * 1000L)
        val fiveHoursAgo = now - (5 * 60 * 60 * 1000L)

        every { preferences.getLastAutoDisconnectedTimestamp() } returns fourHoursAgo
        every { preferences.getLastEngineStartLoggedTimestamp() } returns fiveHoursAgo

        telemetryManager.onConnectionStateChanged(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.logEngineStart(
                isColdStart = true,
                odometerKm = 9284.0,
                timestamp = any()
            )
        }
    }

    @Test
    fun `connection after 30 minutes cooldown logs WARM START`() = testScope.runTest {
        val now = System.currentTimeMillis()
        val thirtyMinsAgo = now - (30 * 60 * 1000L)
        val twoHoursAgo = now - (2 * 60 * 60 * 1000L)

        every { preferences.getLastAutoDisconnectedTimestamp() } returns thirtyMinsAgo
        every { preferences.getLastEngineStartLoggedTimestamp() } returns twoHoursAgo

        telemetryManager.onConnectionStateChanged(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.logEngineStart(
                isColdStart = false,
                odometerKm = 9284.0,
                timestamp = any()
            )
        }
    }

    @Test
    fun `rapid reconnect within 2 minutes is debounced and ignored`() = testScope.runTest {
        val now = System.currentTimeMillis()
        val thirtySecondsAgo = now - 30_000L

        // A start was already logged 30 seconds ago
        every { preferences.getLastEngineStartLoggedTimestamp() } returns thirtySecondsAgo
        every { preferences.getLastAutoDisconnectedTimestamp() } returns thirtySecondsAgo

        telemetryManager.onConnectionStateChanged(true)
        advanceUntilIdle()

        // Should NOT log another start
        coVerify(exactly = 0) {
            repository.logEngineStart(any(), any(), any())
        }
    }

    @Test
    fun `disconnect less than 2 minutes ago is treated as transient cable reconnect`() = testScope.runTest {
        val now = System.currentTimeMillis()
        val tenSecondsAgo = now - 10_000L
        val oneHourAgo = now - (60 * 60 * 1000L)

        // Last start was 1 hour ago, but disconnect was only 10s ago (e.g. cable bumped while driving)
        every { preferences.getLastEngineStartLoggedTimestamp() } returns oneHourAgo
        every { preferences.getLastAutoDisconnectedTimestamp() } returns tenSecondsAgo
        every { preferences.getLastConnectionHeartbeat() } returns tenSecondsAgo

        telemetryManager.onConnectionStateChanged(true)
        advanceUntilIdle()

        // Should NOT log another start
        coVerify(exactly = 0) {
            repository.logEngineStart(any(), any(), any())
        }
    }

    @Test
    fun `disconnect after 25 min trip updates start event with duration`() = testScope.runTest {
        val now = System.currentTimeMillis()
        val twentyFiveMinsAgo = now - (25 * 60 * 1000L)

        every { preferences.isVehicleConnected() } returns true
        every { preferences.getActiveStartConnectTimestamp() } returns twentyFiveMinsAgo
        every { preferences.getActiveStartEventId() } returns "test-start-event-123"

        telemetryManager.onConnectionStateChanged(false)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.updateEngineStartDuration("test-start-event-123", 25)
        }
    }

    @Test
    fun `restart after 15 minute break logs WARM START even if disconnect was missed`() = testScope.runTest {
        val now = System.currentTimeMillis()
        val fifteenMinsAgo = now - (15 * 60 * 1000L)
        val oneHourAgo = now - (60 * 60 * 1000L)

        // Disconnect was missed, but heartbeat was recorded 15 mins ago
        every { preferences.isVehicleConnected() } returns false
        every { preferences.getLastEngineStartLoggedTimestamp() } returns oneHourAgo
        every { preferences.getLastAutoDisconnectedTimestamp() } returns 0L
        every { preferences.getLastConnectionHeartbeat() } returns fifteenMinsAgo

        telemetryManager.onConnectionStateChanged(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.logEngineStart(
                isColdStart = false,
                odometerKm = 9284.0,
                timestamp = any()
            )
        }
    }
}
