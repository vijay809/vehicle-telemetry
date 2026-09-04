package com.antigravity.telemetry.core.engine

import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.model.VehicleMeta
import kotlin.math.max

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
    val residualDistanceKm: Double,
    val estimatedRangeKm: Double
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
     * Model B: Pure CNG Tank-to-Tank (Full-to-Empty Cycle)
     * Isolates CNG thermodynamic efficiency by tracking boundary conditions:
     * Full Refill (E1) -> Driving Interval -> CNG_EMPTY (E2) -> Next Refill (E3)
     */
    fun calculateCngEfficiency(
        events: List<FuelEvent>,
        vehicle: VehicleMeta,
        currentOdometer: Double
    ): CngEfficiencyResult {
        val sortedEvents = events.sortedBy { it.odometerKm }
        var latestMileage = 0.0
        var lastCompletedRawDist = 0.0
        var lastCompletedDeduction = 0.0
        var lastCompletedNetDist = 0.0

        // Find consecutive CNG full-refill cycles
        val cngEvents = sortedEvents.filter {
            (it.type == EventType.REFILL && it.fuelType == FuelType.CNG) || it.type == EventType.CNG_EMPTY
        }

        for (i in 0 until cngEvents.size - 1) {
            val curr = cngEvents[i]
            val next = cngEvents[i + 1]

            // Pattern 1: Refill (Full) -> CNG_EMPTY -> Subsequent Refill
            if (curr.type == EventType.REFILL && curr.fuelType == FuelType.CNG && curr.isFullTank) {
                var emptyOdo: Double? = null
                var coldStarts = 0

                if (next.type == EventType.CNG_EMPTY) {
                    emptyOdo = next.odometerKm
                    coldStarts = next.coldStartsSinceLastRefill
                } else if (next.type == EventType.REFILL && next.fuelType == FuelType.CNG) {
                    // Heuristic Edge Case: Unregistered empty, but refill >= 90% capacity
                    val refillQty = next.quantity ?: 0.0
                    if (refillQty >= vehicle.cngTankCapacityKg * 0.90) {
                        emptyOdo = next.odometerKm - 2.0
                        coldStarts = next.coldStartsSinceLastRefill
                    }
                }

                if (emptyOdo != null && emptyOdo > curr.odometerKm) {
                    val rawDist = emptyOdo - curr.odometerKm
                    val deduction = coldStarts * vehicle.estimatedWarmupDistanceKmPerColdStart
                    val netDist = max(0.0, rawDist - deduction)

                    // If subsequent refill is available to calculate km/kg
                    val subsequentRefill = cngEvents.subList(i + 1, cngEvents.size)
                        .firstOrNull { it.type == EventType.REFILL && it.fuelType == FuelType.CNG }

                    if (subsequentRefill != null && (subsequentRefill.quantity ?: 0.0) > 0) {
                        latestMileage = netDist / (subsequentRefill.quantity ?: 1.0)
                        lastCompletedRawDist = rawDist
                        lastCompletedDeduction = deduction
                        lastCompletedNetDist = netDist
                    }
                }
            }
        }

        // Active Trip Odometer on CNG
        val lastCngRefill = sortedEvents.lastOrNull { it.type == EventType.REFILL && it.fuelType == FuelType.CNG }
        val lastCngEmpty = sortedEvents.lastOrNull { it.type == EventType.CNG_EMPTY }

        val isCngExhausted = lastCngEmpty != null && (lastCngRefill == null || lastCngEmpty.odometerKm >= lastCngRefill.odometerKm)
        val exhaustedAtOdo = if (isCngExhausted) lastCngEmpty?.odometerKm else null

        val currentTrip = if (lastCngRefill != null) {
            max(0.0, currentOdometer - lastCngRefill.odometerKm)
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
     * Model C: Subtractive Residual Petrol Mileage
     * Bounded by two Full Tank petrol events (P_A and P_B):
     * Delta D_total = O(P_B) - O(P_A)
     * D_petrol_residual = Delta D_total - sum(D_cng_total)
     * Mileage_petrol = D_petrol_residual / Q_petrol_topup
     */
    fun calculateResidualPetrolEfficiency(
        events: List<FuelEvent>,
        currentOdometer: Double,
        petrolLevelPercent: Double
    ): PetrolEfficiencyResult {
        val sortedPetrolRefills = events.filter {
            it.type == EventType.REFILL && it.fuelType == FuelType.PETROL && it.isFullTank
        }.sortedBy { it.odometerKm }

        var mileage = 0.0
        var residualDist = 0.0

        if (sortedPetrolRefills.size >= 2) {
            val pA = sortedPetrolRefills[sortedPetrolRefills.size - 2]
            val pB = sortedPetrolRefills.last()

            val grossDist = pB.odometerKm - pA.odometerKm

            // Sum CNG segment distances between pA and pB
            val cngSegmentsBetween = events.filter {
                it.odometerKm in pA.odometerKm..pB.odometerKm && it.fuelType == FuelType.CNG && it.type == EventType.REFILL
            }

            var cngTotalDist = 0.0
            for (cng in cngSegmentsBetween) {
                // approximate distance covered by CNG using average efficiency or raw intervals
                cngTotalDist += (cng.quantity ?: 0.0) * 26.0
            }

            residualDist = max(0.0, grossDist - cngTotalDist)
            val topupQty = pB.quantity ?: 1.0
            if (topupQty > 0 && residualDist > 0) {
                mileage = residualDist / topupQty
            }
        }

        // Estimated Petrol range based on current tank level
        val estimatedRange = if (mileage > 0 && petrolLevelPercent > 0) {
            (petrolLevelPercent / 100.0) * 45.0 * mileage
        } else {
            0.0
        }

        return PetrolEfficiencyResult(
            latestMileageKmPerL = mileage,
            residualDistanceKm = residualDist,
            estimatedRangeKm = estimatedRange
        )
    }
}
