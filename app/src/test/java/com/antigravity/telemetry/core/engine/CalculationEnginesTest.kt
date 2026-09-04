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
    fun `Model B pure CNG calculates mileage from refill to empty`() {
        val events = listOf(
            // Refill 9.0 kg at 40,000 km
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
            )
        )

        // Raw distance = 40250 - 40000 = 250 km
        // Deduction = 3 cold starts * 1.2 km = 3.6 km
        // Net CNG distance = 250 - 3.6 = 246.4 km
        // Mileage = 246.4 / 9.0 = 27.377 km/kg
        val result = CalculationEngines.calculateCngEfficiency(events, testVehicle, currentOdometer = 40250.0)

        assertEquals(250.0, result.rawDistanceKm, 0.01)
        assertEquals(3.6, result.coldStartDeductionKm, 0.01)
        assertEquals(246.4, result.netCngDistanceKm, 0.01)
        assertEquals(27.38, result.latestMileageKmPerKg, 0.02)
    }

    @Test
    fun `Model B accumulates multiple refills without empty and calculates on next trigger`() {
        val events = listOf(
            // Refill 1: 5.0 kg at 40,000 km
            FuelEvent(
                odometerKm = 40000.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                quantity = 5.0
            ),
            // Refill 2: 4.0 kg at 40,100 km (tank not emptied in between)
            FuelEvent(
                odometerKm = 40100.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                quantity = 4.0
            ),
            // CNG ran out at 40,250 km with 3 cold starts
            FuelEvent(
                odometerKm = 40250.0,
                type = EventType.CNG_EMPTY,
                coldStartsSinceLastRefill = 3
            )
        )

        // Total qty = 5.0 + 4.0 = 9.0 kg
        // Raw distance = 40250 - 40000 = 250 km
        // Deduction = 3 * 1.2 = 3.6 km
        // Net CNG distance = 246.4 km
        // Mileage = 246.4 / 9.0 = 27.38 km/kg
        val result = CalculationEngines.calculateCngEfficiency(events, testVehicle, currentOdometer = 40250.0)

        assertEquals(250.0, result.rawDistanceKm, 0.01)
        assertEquals(3.6, result.coldStartDeductionKm, 0.01)
        assertEquals(246.4, result.netCngDistanceKm, 0.01)
        assertEquals(27.38, result.latestMileageKmPerKg, 0.02)
    }

    @Test
    fun `Model C triggers petrol mileage calculation from fill to low fuel`() {
        val events = listOf(
            // Petrol refill at 30,000 km
            FuelEvent(
                odometerKm = 30000.0,
                type = EventType.REFILL,
                fuelType = FuelType.PETROL,
                quantity = 20.0
            ),
            // Low fuel light triggered at 30,300 km
            FuelEvent(
                odometerKm = 30300.0,
                type = EventType.FUEL_LOW,
                fuelType = FuelType.PETROL
            )
        )

        val result = CalculationEngines.calculateResidualPetrolEfficiency(events, currentOdometer = 30300.0, petrolLevelPercent = 10.0, vehicle = testVehicle)

        // Gross distance = 300 km (no CNG used)
        // Mileage = 300 / 20.0 = 15.0 km/L
        assertEquals(300.0, result.residualDistanceKm, 0.01)
        assertEquals(15.0, result.latestMileageKmPerL, 0.01)
    }

    @Test
    fun `Model C accumulates multiple petrol refills before low fuel trigger`() {
        val events = listOf(
            // Petrol refill 1 at 30,000 km
            FuelEvent(
                odometerKm = 30000.0,
                type = EventType.REFILL,
                fuelType = FuelType.PETROL,
                quantity = 15.0
            ),
            // Petrol refill 2 at 30,100 km (no low fuel in between)
            FuelEvent(
                odometerKm = 30100.0,
                type = EventType.REFILL,
                fuelType = FuelType.PETROL,
                quantity = 10.0
            ),
            // Low fuel triggered at 30,375 km
            FuelEvent(
                odometerKm = 30375.0,
                type = EventType.FUEL_LOW,
                fuelType = FuelType.PETROL
            )
        )

        val result = CalculationEngines.calculateResidualPetrolEfficiency(events, currentOdometer = 30375.0, petrolLevelPercent = 10.0, vehicle = testVehicle)

        // Total qty = 15 + 10 = 25 L
        // Gross distance = 375 km
        // Mileage = 375 / 25 = 15.0 km/L
        assertEquals(375.0, result.residualDistanceKm, 0.01)
        assertEquals(15.0, result.latestMileageKmPerL, 0.01)
    }

    @Test
    fun `Model C subtracts CNG distance driven during petrol session`() {
        val events = listOf(
            // Petrol refill at 30,000 km, 20 L
            FuelEvent(
                odometerKm = 30000.0,
                type = EventType.REFILL,
                fuelType = FuelType.PETROL,
                quantity = 20.0
            ),
            // CNG refill at 30,100 km
            FuelEvent(
                odometerKm = 30100.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                quantity = 5.0
            ),
            // CNG ran out at 30,220 km (120 km on CNG)
            FuelEvent(
                odometerKm = 30220.0,
                type = EventType.CNG_EMPTY,
                coldStartsSinceLastRefill = 0
            ),
            // Petrol hits low fuel at 30,420 km
            FuelEvent(
                odometerKm = 30420.0,
                type = EventType.FUEL_LOW,
                fuelType = FuelType.PETROL
            )
        )

        val result = CalculationEngines.calculateResidualPetrolEfficiency(events, currentOdometer = 30420.0, petrolLevelPercent = 10.0, vehicle = testVehicle)

        // Gross distance = 420 km
        // CNG distance = 120 km
        // Petrol distance = 300 km
        // Mileage = 300 / 20.0 = 15.0 km/L
        assertEquals(300.0, result.residualDistanceKm, 0.01)
        assertEquals(15.0, result.latestMileageKmPerL, 0.01)
    }

    @Test
    fun `CNG empty sets isCngExhausted to true`() {
        val events = listOf(
            FuelEvent(
                odometerKm = 40000.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                quantity = 9.0,
                timestamp = 500L
            ),
            FuelEvent(
                odometerKm = 40250.0,
                type = EventType.CNG_EMPTY,
                timestamp = 1000L
            )
        )

        val result = CalculationEngines.calculateCngEfficiency(events, testVehicle, currentOdometer = 40250.0)
        assertEquals(true, result.isCngExhausted)
        assertEquals(40250.0, result.exhaustedAtOdometerKm ?: 0.0, 0.01)
    }

    @Test
    fun `CNG refill after CNG empty clears exhausted state even at same odometer`() {
        val events = listOf(
            FuelEvent(
                odometerKm = 40000.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                quantity = 9.0,
                timestamp = 500L
            ),
            FuelEvent(
                odometerKm = 40250.0,
                type = EventType.CNG_EMPTY,
                timestamp = 1000L
            ),
            FuelEvent(
                odometerKm = 40250.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                quantity = 8.5,
                timestamp = 2000L
            )
        )

        val result = CalculationEngines.calculateCngEfficiency(events, testVehicle, currentOdometer = 40250.0)
        assertEquals(false, result.isCngExhausted)
        assertEquals(null, result.exhaustedAtOdometerKm)
    }

    @Test
    fun `CNG refill after CNG empty clears exhausted state when refilled at higher odometer`() {
        val events = listOf(
            FuelEvent(
                odometerKm = 40000.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                quantity = 9.0,
                timestamp = 500L
            ),
            FuelEvent(
                odometerKm = 40250.0,
                type = EventType.CNG_EMPTY,
                timestamp = 1000L
            ),
            FuelEvent(
                odometerKm = 40260.0,
                type = EventType.REFILL,
                fuelType = FuelType.CNG,
                quantity = 8.5,
                timestamp = 2000L
            )
        )

        val result = CalculationEngines.calculateCngEfficiency(events, testVehicle, currentOdometer = 40265.0)
        assertEquals(false, result.isCngExhausted)
        assertEquals(null, result.exhaustedAtOdometerKm)
        assertEquals(5.0, result.currentTripKm, 0.01) // 40265 - 40260
    }
}
