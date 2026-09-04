package com.antigravity.telemetry.core.engine

import com.antigravity.telemetry.core.model.EventSource
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.model.VehicleMeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculationEnginesTest {

    private val testVehicle = VehicleMeta(
        id = "test-victoris",
        name = "Victoris CNG",
        cngTankCapacityKg = 10.0,
        petrolTankCapacityL = 45.0,
        estimatedWarmupDistanceKmPerColdStart = 1.2,
        activeOdometerKm = 42850.0
    )

    @Test
    fun `Model A blended cost computes correct rupees per km`() {
        val events = listOf(
            FuelEvent(
                odometerKm = 10000.0,
                type = EventType.REFILL,
                fuelType = FuelType.PETROL,
                totalCost = 2000.0
            ),
            FuelEvent(
                odometerKm = 10500.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                totalCost = 1000.0
            )
        )

        // Distance = 11000 - 10000 = 1000 km
        // Total cost = 3000 Rs
        // Blended cost = 3000 / 1000 = 3.0 Rs/km
        val result = CalculationEngines.calculateBlendedCost(events, currentOdometer = 11000.0)

        assertEquals(3.0, result.blendedCostPerKm, 0.01)
        assertEquals(3000.0, result.totalCost, 0.01)
        assertEquals(1000.0, result.totalDistanceKm, 0.01)
        assertEquals(33.33, result.cngSharePercent, 0.5)
        assertEquals(66.66, result.petrolSharePercent, 0.5)
    }

    @Test
    fun `Model B pure CNG isolates tank-to-tank efficiency with cold-start deduction`() {
        val events = listOf(
            // Full refill at 40,000 km
            FuelEvent(
                odometerKm = 40000.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                isFullTank = true,
                quantity = 9.0
            ),
            // CNG ran out at 40,250 km with 3 cold starts
            FuelEvent(
                odometerKm = 40250.0,
                type = EventType.CNG_EMPTY,
                coldStartsSinceLastRefill = 3
            ),
            // Next refill at 40,260 km, took 8.5 kg to auto-cut
            FuelEvent(
                odometerKm = 40260.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                isFullTank = true,
                quantity = 8.5
            )
        )

        // Raw distance = 40250 - 40000 = 250 km
        // Deduction = 3 cold starts * 1.2 km = 3.6 km
        // Net CNG distance = 250 - 3.6 = 246.4 km
        // Mileage = 246.4 / 8.5 = 28.988 km/kg
        val result = CalculationEngines.calculateCngEfficiency(events, testVehicle, currentOdometer = 40260.0)

        assertEquals(250.0, result.rawDistanceKm, 0.01)
        assertEquals(3.6, result.coldStartDeductionKm, 0.01)
        assertEquals(246.4, result.netCngDistanceKm, 0.01)
        assertEquals(28.98, result.latestMileageKmPerKg, 0.02)
    }

    @Test
    fun `Model B auto-infers CNG empty when refill is 90 percent or more of tank capacity`() {
        val events = listOf(
            // Full refill at 40,000 km
            FuelEvent(
                odometerKm = 40000.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                isFullTank = true,
                quantity = 9.0
            ),
            // Refill at 40,260 km took 9.5 kg (>= 90% of 10.0 kg tank), no explicit CNG_EMPTY logged
            FuelEvent(
                odometerKm = 40260.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                isFullTank = true,
                quantity = 9.5,
                coldStartsSinceLastRefill = 2
            )
        )

        val result = CalculationEngines.calculateCngEfficiency(events, testVehicle, currentOdometer = 40260.0)

        // Inferred empty at 40260 - 2 = 40258
        // Raw dist = 40258 - 40000 = 258 km
        // Deduction = 2 * 1.2 = 2.4 km
        // Net dist = 255.6 km
        // Mileage = 255.6 / 9.5 = 26.9 km/kg
        assertEquals(258.0, result.rawDistanceKm, 0.01)
        assertEquals(2.4, result.coldStartDeductionKm, 0.01)
        assertEquals(255.6, result.netCngDistanceKm, 0.01)
        assertEquals(26.9, result.latestMileageKmPerKg, 0.1)
    }

    @Test
    fun `Model C calculates residual petrol efficiency between full tank intervals`() {
        val events = listOf(
            // Full petrol fill P_A at 30,000 km
            FuelEvent(
                odometerKm = 30000.0,
                type = EventType.REFILL,
                fuelType = FuelType.PETROL,
                isFullTank = true,
                quantity = 35.0
            ),
            // Intermediate CNG refills
            FuelEvent(
                odometerKm = 30200.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                isFullTank = true,
                quantity = 8.0 // covers approx 8 * 26 = 208 km
            ),
            // Full petrol top-up P_B at 30,300 km, takes 6.0 L to fill
            FuelEvent(
                odometerKm = 30300.0,
                type = EventType.REFILL,
                fuelType = FuelType.PETROL,
                isFullTank = true,
                quantity = 6.0
            )
        )

        val result = CalculationEngines.calculateResidualPetrolEfficiency(events, currentOdometer = 30300.0, petrolLevelPercent = 80.0)

        // Gross distance = 300 km
        // CNG approx = 208 km
        // Residual distance = 300 - 208 = 92 km
        // Mileage = 92 / 6.0 = 15.33 km/L
        assertEquals(92.0, result.residualDistanceKm, 0.01)
        assertEquals(15.33, result.latestMileageKmPerL, 0.05)
        assertTrue(result.estimatedRangeKm > 0)
    }
}
