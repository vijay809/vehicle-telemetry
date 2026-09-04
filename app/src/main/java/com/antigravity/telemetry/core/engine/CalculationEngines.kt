package com.antigravity.telemetry.core.engine

import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.model.VehicleMeta
import kotlin.math.max
import kotlin.math.min

data class BlendedCostResult(
    val blendedCostPerKm: Double,
    val totalCost: Double,
    val totalDistanceKm: Double,
    val cngSpend: Double,
    val petrolSpend: Double,
    val cngSharePercent: Double,
    val petrolSharePercent: Double,
    val cngCostPerKm: Double,
    val petrolCostPerKm: Double,
    val monthlySavingsVsPetrol: Double
)

data class CngEfficiencyResult(
    val latestMileageKmPerKg: Double,
    val rawDistanceKm: Double,
    val coldStartDeductionKm: Double,
    val netCngDistanceKm: Double,
    val currentTripKm: Double,
    val isCngExhausted: Boolean,
    val exhaustedAtOdometerKm: Double?
)

data class PetrolEfficiencyResult(
    val latestMileageKmPerL: Double,
    val mileageWithColdStartKmPerL: Double,
    val mileageWithoutColdStartKmPerL: Double,
    val residualDistanceKm: Double,
    val residualDistanceWithoutColdStartKm: Double = residualDistanceKm,
    val estimatedRangeKm: Double,
    val estimatedRangeWithColdStartKm: Double = estimatedRangeKm,
    val estimatedRangeWithoutColdStartKm: Double = estimatedRangeKm,
    val totalColdStartKm: Double = 0.0,
    val totalColdStartsCount: Int = 0
)

data class CngIntervalAnalysis(
    val rawCngDistance: Double,
    val netCngDistance: Double,
    val coldStartKm: Double,
    val coldStartsCount: Int
)

object CalculationEngines {

    /**
     * Model A: Blended Running Cost (Financial Reality)
     * Calculates rolling financial efficiency across an odometer interval:
     * Blended Cost (₹/km) = sum(totalCost) / (Odo_latest - Odo_base)
     */
    fun calculateBlendedCost(events: List<FuelEvent>, currentOdometer: Double): BlendedCostResult {
        val refills = events.filter { it.type == EventType.REFILL && (it.totalCost ?: 0.0) > 0 }
        if (refills.isEmpty()) {
            return BlendedCostResult(
                blendedCostPerKm = 0.0,
                totalCost = 0.0,
                totalDistanceKm = 0.0,
                cngSpend = 0.0,
                petrolSpend = 0.0,
                cngSharePercent = 0.0,
                petrolSharePercent = 0.0,
                cngCostPerKm = 0.0,
                petrolCostPerKm = 0.0,
                monthlySavingsVsPetrol = 0.0
            )
        }

        val baseOdometer = refills.minOf { it.odometerKm }
        val latestOdometer = max(currentOdometer, refills.maxOf { it.odometerKm })
        val totalDistance = max(1.0, latestOdometer - baseOdometer)

        var cngSpend = 0.0
        var petrolSpend = 0.0

        for (refill in refills) {
            val cost = refill.totalCost ?: 0.0
            if (refill.fuelType == FuelType.CNG) {
                cngSpend += cost
            } else if (refill.fuelType == FuelType.PETROL) {
                petrolSpend += cost
            }
        }

        val totalSpend = cngSpend + petrolSpend
        val blendedCost = totalSpend / totalDistance

        val cngSharePercent = if (totalSpend > 0) (cngSpend / totalSpend) * 100 else 0.0
        val petrolSharePercent = if (totalSpend > 0) (petrolSpend / totalSpend) * 100 else 0.0

        val cngCostPerKm = cngSpend / totalDistance
        val petrolCostPerKm = petrolSpend / totalDistance

        // Benchmark comparison: A pure petrol car runs ~ 15 km/L at approx ₹96/L -> ₹6.40/km
        val purePetrolBenchmarkCostPerKm = 6.40
        val savingsPerKm = max(0.0, purePetrolBenchmarkCostPerKm - blendedCost)
        val monthlySavings = savingsPerKm * minOf(totalDistance, 1200.0)

        return BlendedCostResult(
            blendedCostPerKm = blendedCost,
            totalCost = totalSpend,
            totalDistanceKm = totalDistance,
            cngSpend = cngSpend,
            petrolSpend = petrolSpend,
            cngSharePercent = cngSharePercent,
            petrolSharePercent = petrolSharePercent,
            cngCostPerKm = cngCostPerKm,
            petrolCostPerKm = petrolCostPerKm,
            monthlySavingsVsPetrol = monthlySavings
        )
    }

    /**
     * Model B: Pure CNG Tank-to-Tank (Refill to Empty)
     * Isolates CNG thermodynamic efficiency:
     * - A session begins on CNG Refill.
     * - If more CNG is refilled without emptying the tank, the session continues and quantities accumulate (Rule 3).
     * - When CNG Empty occurs, the trigger calculates:
     *   Mileage (km/kg) = (Odo_empty - Odo_session_start - cold_start_deduction) / Total_CNG_Quantity (Rule 1)
     */
    fun calculateCngEfficiency(
        events: List<FuelEvent>,
        vehicle: VehicleMeta,
        currentOdometer: Double
    ): CngEfficiencyResult {
        val sortedEvents = events.sortedWith(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })
        var latestMileage = 0.0
        var lastCompletedRawDist = 0.0
        var lastCompletedDeduction = 0.0
        var lastCompletedNetDist = 0.0

        // Track CNG sessions
        val cngEvents = sortedEvents.filter {
            (it.type == EventType.REFILL && it.fuelType == FuelType.CNG) || it.type == EventType.CNG_EMPTY
        }

        var sessionStartOdo: Double? = null
        var sessionAccumulatedQty = 0.0
        var sessionColdStarts = 0

        for (event in cngEvents) {
            if (event.type == EventType.REFILL && event.fuelType == FuelType.CNG) {
                val qty = event.quantity ?: 0.0
                if (qty > 0) {
                    // Check if previous session was never explicitly marked empty,
                    // but this refill is >= 90% capacity (inferred empty)
                    if (sessionStartOdo != null && sessionAccumulatedQty > 0 &&
                        qty >= vehicle.cngTankCapacityKg * 0.90 &&
                        (event.odometerKm - sessionStartOdo) > 50.0
                    ) {
                        val inferredEmptyOdo = event.odometerKm - 2.0
                        val rawDist = inferredEmptyOdo - sessionStartOdo
                        val deduction = (sessionColdStarts + event.coldStartsSinceLastRefill) * vehicle.estimatedWarmupDistanceKmPerColdStart
                        val netDist = max(0.0, rawDist - deduction)
                        if (netDist > 0 && sessionAccumulatedQty > 0) {
                            latestMileage = netDist / sessionAccumulatedQty
                            lastCompletedRawDist = rawDist
                            lastCompletedDeduction = deduction
                            lastCompletedNetDist = netDist
                        }
                        // Reset session with new refill
                        sessionStartOdo = event.odometerKm
                        sessionAccumulatedQty = qty
                        sessionColdStarts = 0
                    } else {
                        // Rule 3: Keep session going across top-ups without emptying
                        if (sessionStartOdo == null) {
                            sessionStartOdo = event.odometerKm
                        }
                        sessionAccumulatedQty += qty
                        sessionColdStarts += event.coldStartsSinceLastRefill
                    }
                }
            } else if (event.type == EventType.CNG_EMPTY) {
                // Rule 1: Trigger calculation on empty
                if (sessionStartOdo != null && sessionAccumulatedQty > 0) {
                    val emptyOdo = event.odometerKm
                    if (emptyOdo > sessionStartOdo) {
                        val rawDist = emptyOdo - sessionStartOdo
                        val totalColdStarts = sessionColdStarts + event.coldStartsSinceLastRefill
                        val deduction = totalColdStarts * vehicle.estimatedWarmupDistanceKmPerColdStart
                        val netDist = max(0.0, rawDist - deduction)

                        latestMileage = netDist / sessionAccumulatedQty
                        lastCompletedRawDist = rawDist
                        lastCompletedDeduction = deduction
                        lastCompletedNetDist = netDist
                    }
                    // Session complete on empty
                    sessionStartOdo = null
                    sessionAccumulatedQty = 0.0
                    sessionColdStarts = 0
                }
            }
        }

        // Active Trip Odometer and Exhausted state on CNG:
        // A refill MUST clear the exhausted state (refill takes precedence over previous empty)
        val lastCngRefill = cngEvents.filter { it.type == EventType.REFILL && it.fuelType == FuelType.CNG }
            .maxWithOrNull(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })
        val lastCngEmpty = cngEvents.filter { it.type == EventType.CNG_EMPTY }
            .maxWithOrNull(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })

        val isCngExhausted = when {
            lastCngEmpty == null -> false
            lastCngRefill == null -> true
            else -> lastCngEmpty.timestamp > lastCngRefill.timestamp && lastCngEmpty.odometerKm >= lastCngRefill.odometerKm
        }
        val exhaustedAtOdo = if (isCngExhausted) lastCngEmpty?.odometerKm else null

        val effectiveCurrentOdo = maxOf(
            currentOdometer,
            sessionStartOdo ?: 0.0,
            lastCngRefill?.odometerKm ?: 0.0
        )

        val currentTrip = if (sessionStartOdo != null) {
            max(0.0, effectiveCurrentOdo - sessionStartOdo)
        } else if (lastCngRefill != null && !isCngExhausted) {
            max(0.0, effectiveCurrentOdo - lastCngRefill.odometerKm)
        } else {
            0.0
        }

        return CngEfficiencyResult(
            latestMileageKmPerKg = latestMileage,
            rawDistanceKm = lastCompletedRawDist,
            coldStartDeductionKm = lastCompletedDeduction,
            netCngDistanceKm = lastCompletedNetDist,
            currentTripKm = currentTrip,
            isCngExhausted = isCngExhausted,
            exhaustedAtOdometerKm = exhaustedAtOdo
        )
    }

    /**
     * Model C: Petrol Efficiency (Fill to Low Fuel)
     * Calculates Petrol efficiency:
     * - A session begins on Petrol Refill.
     * - If more Petrol is refilled without hitting Low Fuel, the session continues and quantities accumulate (Rule 3).
     * - When Low Fuel occurs (FUEL_LOW event), the trigger calculates:
     *   Gross Distance = Odo_low_fuel - Odo_session_start
     *   Net Petrol Distance = Gross Distance - sum(CNG distance in interval)
     *   Mileage (km/L) = Net Petrol Distance / Total_Petrol_Quantity (Rule 2)
     */
    fun calculateResidualPetrolEfficiency(
        events: List<FuelEvent>,
        currentOdometer: Double,
        petrolLevelPercent: Double,
        vehicle: VehicleMeta = VehicleMeta()
    ): PetrolEfficiencyResult {
        val sortedEvents = events.sortedWith(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })
        var latestMileageWithCs = 0.0
        var latestMileageWithoutCs = 0.0
        var lastResidualDistWithCs = 0.0
        var lastResidualDistWithoutCs = 0.0
        var lastColdStartKm = 0.0
        var lastColdStartsCount = 0

        val petrolEvents = sortedEvents.filter {
            (it.type == EventType.REFILL && it.fuelType == FuelType.PETROL) ||
            (it.type == EventType.FUEL_LOW && it.fuelType == FuelType.PETROL)
        }

        var sessionStartOdo: Double? = null
        var sessionAccumulatedQty = 0.0

        for (event in petrolEvents) {
            if (event.type == EventType.REFILL && event.fuelType == FuelType.PETROL) {
                val qty = event.quantity ?: 0.0
                if (qty > 0) {
                    // Rule 3: Keep session going across top-ups without reaching low fuel
                    if (sessionStartOdo == null) {
                        sessionStartOdo = event.odometerKm
                    }
                    sessionAccumulatedQty += qty
                }
            } else if (event.type == EventType.FUEL_LOW && event.fuelType == FuelType.PETROL) {
                // Rule 2: Trigger calculation on Low Fuel
                if (sessionStartOdo != null && sessionAccumulatedQty > 0) {
                    val lowFuelOdo = event.odometerKm
                    if (lowFuelOdo > sessionStartOdo) {
                        val grossDist = lowFuelOdo - sessionStartOdo
                        val cngAnalysis = calculateCngIntervalAnalysis(
                            events = sortedEvents,
                            startOdo = sessionStartOdo,
                            endOdo = lowFuelOdo,
                            vehicle = vehicle
                        )
                        val petrolDistWithCs = max(0.0, grossDist - cngAnalysis.netCngDistance)
                        val petrolDistWithoutCs = max(0.0, grossDist - cngAnalysis.rawCngDistance)

                        if (sessionAccumulatedQty > 0) {
                            latestMileageWithCs = petrolDistWithCs / sessionAccumulatedQty
                            latestMileageWithoutCs = petrolDistWithoutCs / sessionAccumulatedQty
                            lastResidualDistWithCs = petrolDistWithCs
                            lastResidualDistWithoutCs = petrolDistWithoutCs
                            lastColdStartKm = cngAnalysis.coldStartKm
                            lastColdStartsCount = cngAnalysis.coldStartsCount
                        }
                    }
                    // Session completes on low fuel
                    sessionStartOdo = null
                    sessionAccumulatedQty = 0.0
                }
            }
        }

        // Active distance on current petrol session if still ongoing
        var activeDistWithCs = lastResidualDistWithCs
        var activeDistWithoutCs = lastResidualDistWithoutCs
        var activeColdStartKm = lastColdStartKm
        var activeColdStartsCount = lastColdStartsCount

        if (sessionStartOdo != null) {
            val grossDist = max(0.0, currentOdometer - sessionStartOdo)
            val cngAnalysis = calculateCngIntervalAnalysis(
                events = sortedEvents,
                startOdo = sessionStartOdo,
                endOdo = currentOdometer,
                vehicle = vehicle
            )
            activeDistWithCs = max(0.0, grossDist - cngAnalysis.netCngDistance)
            activeDistWithoutCs = max(0.0, grossDist - cngAnalysis.rawCngDistance)
            activeColdStartKm = cngAnalysis.coldStartKm
            activeColdStartsCount = cngAnalysis.coldStartsCount
        }

        val rangeWithCs = if (latestMileageWithCs > 0 && petrolLevelPercent > 0) {
            (petrolLevelPercent / 100.0) * vehicle.petrolTankCapacityL * latestMileageWithCs
        } else {
            0.0
        }

        val rangeWithoutCs = if (latestMileageWithoutCs > 0 && petrolLevelPercent > 0) {
            (petrolLevelPercent / 100.0) * vehicle.petrolTankCapacityL * latestMileageWithoutCs
        } else {
            0.0
        }

        return PetrolEfficiencyResult(
            latestMileageKmPerL = latestMileageWithCs,
            mileageWithColdStartKmPerL = latestMileageWithCs,
            mileageWithoutColdStartKmPerL = latestMileageWithoutCs,
            residualDistanceKm = activeDistWithCs,
            residualDistanceWithoutColdStartKm = activeDistWithoutCs,
            estimatedRangeKm = rangeWithCs,
            estimatedRangeWithColdStartKm = rangeWithCs,
            estimatedRangeWithoutColdStartKm = rangeWithoutCs,
            totalColdStartKm = activeColdStartKm,
            totalColdStartsCount = activeColdStartsCount
        )
    }

    private fun calculateCngIntervalAnalysis(
        events: List<FuelEvent>,
        startOdo: Double,
        endOdo: Double,
        vehicle: VehicleMeta
    ): CngIntervalAnalysis {
        if (endOdo <= startOdo) return CngIntervalAnalysis(0.0, 0.0, 0.0, 0)

        val cngEvents = events.filter {
            (it.type == EventType.REFILL && it.fuelType == FuelType.CNG) || it.type == EventType.CNG_EMPTY
        }.sortedWith(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })

        var totalRawCngDistance = 0.0
        var totalNetCngDistance = 0.0
        var totalColdStartKm = 0.0
        var totalColdStartsCount = 0

        var activeCngStart: Double? = null
        var activeColdStarts = 0

        for (event in cngEvents) {
            if (event.type == EventType.REFILL && event.fuelType == FuelType.CNG) {
                if (activeCngStart == null) {
                    activeCngStart = event.odometerKm
                }
                activeColdStarts += event.coldStartsSinceLastRefill
            } else if (event.type == EventType.CNG_EMPTY) {
                if (activeCngStart != null) {
                    val cngStart = activeCngStart
                    val cngEnd = event.odometerKm
                    activeColdStarts += event.coldStartsSinceLastRefill

                    val overlapStart = max(cngStart, startOdo)
                    val overlapEnd = min(cngEnd, endOdo)

                    if (overlapEnd > overlapStart) {
                        val rawOverlap = overlapEnd - overlapStart
                        val totalCycleDist = max(1.0, cngEnd - cngStart)
                        val overlapRatio = rawOverlap / totalCycleDist
                        val coldStartDeduction = activeColdStarts * vehicle.estimatedWarmupDistanceKmPerColdStart * overlapRatio
                        val netOverlap = max(0.0, rawOverlap - coldStartDeduction)

                        totalRawCngDistance += rawOverlap
                        totalNetCngDistance += netOverlap
                        totalColdStartKm += coldStartDeduction
                        totalColdStartsCount += (activeColdStarts * overlapRatio).toInt()
                    }

                    activeCngStart = null
                    activeColdStarts = 0
                }
            }
        }

        // Active CNG session running up to endOdo
        if (activeCngStart != null && activeCngStart < endOdo) {
            val overlapStart = max(activeCngStart, startOdo)
            val overlapEnd = endOdo
            if (overlapEnd > overlapStart) {
                val rawOverlap = overlapEnd - overlapStart
                val coldStartDeduction = activeColdStarts * vehicle.estimatedWarmupDistanceKmPerColdStart
                val netOverlap = max(0.0, rawOverlap - coldStartDeduction)

                totalRawCngDistance += rawOverlap
                totalNetCngDistance += netOverlap
                totalColdStartKm += coldStartDeduction
                totalColdStartsCount += activeColdStarts
            }
        }

        val clampedRaw = min(totalRawCngDistance, endOdo - startOdo)
        val clampedNet = min(totalNetCngDistance, endOdo - startOdo)

        return CngIntervalAnalysis(
            rawCngDistance = clampedRaw,
            netCngDistance = clampedNet,
            coldStartKm = totalColdStartKm,
            coldStartsCount = totalColdStartsCount
        )
    }
}
