package com.antigravity.telemetry.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.antigravity.telemetry.core.model.FuelType
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    fun getVehicle(id: String): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleSync(id: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET activeOdometerKm = :odometerKm WHERE id = :id")
    suspend fun updateOdometer(id: String, odometerKm: Double)
}

@Dao
interface FuelEventDao {
    @Query("SELECT * FROM fuel_events WHERE isSimulation = :isSimulation ORDER BY odometerKm DESC, timestamp DESC")
    fun getEventsFlow(isSimulation: Boolean = false): Flow<List<FuelEventEntity>>

    @Query("SELECT * FROM fuel_events WHERE isSimulation = :isSimulation ORDER BY odometerKm ASC, timestamp ASC")
    suspend fun getEventsAscSync(isSimulation: Boolean = false): List<FuelEventEntity>

    @Query("SELECT * FROM fuel_events WHERE vehicleId = :vehicleId AND isSimulation = :isSimulation ORDER BY odometerKm DESC, timestamp DESC")
    fun getEventsByVehicle(vehicleId: String, isSimulation: Boolean = false): Flow<List<FuelEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: FuelEventEntity)

    @Update
    suspend fun updateEvent(event: FuelEventEntity)

    @Query("DELETE FROM fuel_events WHERE id = :id")
    suspend fun deleteEvent(id: String)

    @Query("DELETE FROM fuel_events WHERE isSimulation = :isSimulation")
    suspend fun deleteAllEvents(isSimulation: Boolean)

    @Query("SELECT * FROM fuel_events WHERE vehicleId = :vehicleId AND type = 'REFILL' AND fuelType = :fuelType AND isSimulation = :isSimulation ORDER BY odometerKm DESC LIMIT 1")
    suspend fun getLastRefillEvent(vehicleId: String, fuelType: FuelType, isSimulation: Boolean = false): FuelEventEntity?

    @Query("SELECT * FROM fuel_events WHERE vehicleId = :vehicleId AND type = 'CNG_EMPTY' AND isSimulation = :isSimulation ORDER BY odometerKm DESC LIMIT 1")
    suspend fun getLastCngEmptyEvent(vehicleId: String, isSimulation: Boolean = false): FuelEventEntity?
}

@Dao
interface DriveSegmentDao {
    @Query("SELECT * FROM drive_segments WHERE isSimulation = :isSimulation ORDER BY startOdometerKm DESC")
    fun getAllSegmentsFlow(isSimulation: Boolean = false): Flow<List<DriveSegmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: DriveSegmentEntity)

    @Query("DELETE FROM drive_segments WHERE isSimulation = :isSimulation")
    suspend fun deleteAllSegments(isSimulation: Boolean)
}
