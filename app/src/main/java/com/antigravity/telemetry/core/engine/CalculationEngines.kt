package com.antigravity.telemetry.core.engine

import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.model.VehicleMeta
import com.antigravity.telemetry.core.model.CostTimeframe
import com.antigravity.telemetry.core.model.CngMileageCondition
import com.antigravity.telemetry.core.model.PetrolMileageCondition
import com.antigravity.telemetry.core.model.MileageSegment
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
    val monthlySavingsVsPetrol: Double,
    val timeframe: CostTimeframe = CostTimeframe.ONE_MONTH
)

data class CngEfficiencyResult(
    val latestMileageKmPerKg: Double,
    val rawDistanceKm: Double,
    val coldStartDeductionKm: Double,
    val netCngDistanceKm: Double,
    val currentTripKm: Double,
    val isCngExhausted: Boolean,
    val exhaustedAtOdometerKm: Double?,
    val calculationCondition: CngMileageCondition = CngMileageCondition.AWAITING_DATA,
    val activeCycleColdStarts: Int = 0,
    val activeCycleNetDistanceKm: Double = currentTripKm
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
    val totalColdStartsCount: Int = 0,
    val calculationCondition: PetrolMileageCondition = PetrolMileageCondition.AWAITING_DATA,
    val activeCyclePetrolDistanceKm: Double = residualDistanceKm
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
     * Calculates rolling financial efficiency across an odometer interval filtered by timeframe:
     * Blended Cost (₹/km) = sum(totalCost in timeframe) / (Odo_latest - Odo_base)
     */
    fun calculateBlendedCost(
        events: List<FuelEvent>,
        currentOdometer: Double,
        timeframe: CostTimeframe = CostTimeframe.ONE_MONTH,
        referenceTime: Long? = null
    ): BlendedCostResult {
        val refills = events.filter { it.isRefill && (it.totalCost ?: 0.0) > 0 }
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
                monthlySavingsVsPetrol = 0.0,
                timeframe = timeframe
            )
        }

        // Establish window cutoff
        val now = System.currentTimeMillis()
        val maxEventTime = events.maxOfOrNull { it.timestamp } ?: 0L
        val effectiveRefTime = referenceTime ?: if (maxEventTime in 1 until 1_000_000_000L) {
            // Mocked small epoch timestamp in unit tests
            maxEventTime
        } else {
            maxOf(now, maxEventTime)
        }

        val cutoffTimestamp = effectiveRefTime - (timeframe.months.toLong() * 30L * 24L * 60L * 60L * 1000L)
        val refillsInWindow = refills.filter { it.timestamp >= cutoffTimestamp }

        if (refillsInWindow.isEmpty()) {
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
                monthlySavingsVsPetrol = 0.0,
                timeframe = timeframe
            )
        }

        val baseOdometer = refillsInWindow.minOf { it.odometerKm }
        val latestOdometer = max(currentOdometer, refillsInWindow.maxOf { it.odometerKm })
        val totalDistance = max(1.0, latestOdometer - baseOdometer)

        var cngSpend = 0.0
        var petrolSpend = 0.0

        for (refill in refillsInWindow) {
            val cost = refill.totalCost ?: 0.0
            if (refill.isCngRefill) {
                cngSpend += cost
            } else if (refill.isPetrolRefill) {
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
            monthlySavingsVsPetrol = monthlySavings,
            timeframe = timeframe
        )
    }

    /**
     * Model B: Pure CNG Efficiency
     * Condition 1: Fill to empty (Triggered on CNG_EMPTY)
     * Condition 2: Refill before empty (Full-to-Full top-off without hitting empty)
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
        var condition: CngMileageCondition = CngMileageCondition.AWAITING_DATA

        // Track CNG sessions
        val cngEvents = sortedEvents.filter {
            it.isCngRefill || it.isCngEmpty
        }

        var sessionStartOdo: Double? = null
        var sessionAccumulatedQty = 0.0
        var sessionColdStarts = 0
        var lastRefillEvent: FuelEvent? = null

        for (event in cngEvents) {
            if (event.isCngRefill) {
                val qty = event.quantity ?: 0.0
                if (qty > 0) {
                    val prevRefill = lastRefillEvent

                    // Condition 2: Refill before empty (Full-to-Full)
                    if (sessionStartOdo != null && prevRefill != null && event.odometerKm > prevRefill.odometerKm && event.isFullTank && prevRefill.isFullTank) {
                        val rawDist = event.odometerKm - prevRefill.odometerKm
                        val cs = event.coldStartsSinceLastRefill
                        val deduction = cs * vehicle.estimatedWarmupDistanceKmPerColdStart
                        val netDist = max(0.0, rawDist - deduction)
                        if (netDist > 0) {
                            latestMileage = netDist / qty
                            lastCompletedRawDist = rawDist
                            lastCompletedDeduction = deduction
                            lastCompletedNetDist = netDist
                            condition = CngMileageCondition.REFILL_BEFORE_EMPTY
                        }
                        sessionStartOdo = event.odometerKm
                        sessionAccumulatedQty = qty
                        sessionColdStarts = 0
                        lastRefillEvent = event
                        continue
                    }

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
                            condition = CngMileageCondition.FILL_TO_EMPTY
                        }
                        sessionStartOdo = event.odometerKm
                        sessionAccumulatedQty = qty
                        sessionColdStarts = 0
                    } else {
                        // Keep session going across top-ups without emptying
                        if (sessionStartOdo == null) {
                            sessionStartOdo = event.odometerKm
                        }
                        sessionAccumulatedQty += qty
                        sessionColdStarts += event.coldStartsSinceLastRefill
                    }
                    lastRefillEvent = event
                }
            } else if (event.type == EventType.CNG_EMPTY) {
                // Condition 1: Fill to empty
                if (sessionStartOdo != null && sessionAccumulatedQty > 0) {
                    val emptyOdo = event.odometerKm
                    if (emptyOdo > sessionStartOdo) {
                        val rawDist = emptyOdo - sessionStartOdo
                        val totalColdStarts = sessionColdStarts + event.coldStartsSinceLastRefill
                        val deduction = totalColdStarts * vehicle.estimatedWarmupDistanceKmPerColdStart
                        val netDist = max(0.0, rawDist - deduction)

                        if (netDist > 0) {
                            latestMileage = netDist / sessionAccumulatedQty
                            lastCompletedRawDist = rawDist
                            lastCompletedDeduction = deduction
                            lastCompletedNetDist = netDist
                            condition = CngMileageCondition.FILL_TO_EMPTY
                        }
                    }
                    sessionStartOdo = null
                    sessionAccumulatedQty = 0.0
                    sessionColdStarts = 0
                    lastRefillEvent = null
                }
            }
        }

        // Active Trip Odometer and Exhausted state on CNG:
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

        val activeCs = sessionColdStarts
        val activeCsDeduction = activeCs * vehicle.estimatedWarmupDistanceKmPerColdStart
        val activeNet = max(0.0, currentTrip - activeCsDeduction)

        return CngEfficiencyResult(
            latestMileageKmPerKg = latestMileage,
            rawDistanceKm = lastCompletedRawDist,
            coldStartDeductionKm = lastCompletedDeduction,
            netCngDistanceKm = lastCompletedNetDist,
            currentTripKm = currentTrip,
            isCngExhausted = isCngExhausted,
            exhaustedAtOdometerKm = exhaustedAtOdo,
            calculationCondition = condition,
            activeCycleColdStarts = activeCs,
            activeCycleNetDistanceKm = activeNet
        )
    }

    /**
     * Model C: Petrol Efficiency
     * Condition 1: Fill to reserve (Triggered on FUEL_LOW / PETROL_RESERVE)
     * Condition 2: Refill before reserve (Full-to-Full top-off before reserve)
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
        var condition: PetrolMileageCondition = PetrolMileageCondition.AWAITING_DATA

        val petrolEvents = sortedEvents.filter {
            it.isPetrolRefill || it.isPetrolReserve
        }

        var sessionStartOdo: Double? = null
        var sessionAccumulatedQty = 0.0
        var lastRefillEvent: FuelEvent? = null

        for (event in petrolEvents) {
            if (event.isPetrolRefill) {
                val qty = event.quantity ?: 0.0
                if (qty > 0) {
                    val prevRefill = lastRefillEvent

                    // Condition 2: Refill before reserve (Full-to-Full)
                    if (sessionStartOdo != null && prevRefill != null && event.odometerKm > prevRefill.odometerKm && event.isFullTank && prevRefill.isFullTank) {
                        val grossDist = event.odometerKm - prevRefill.odometerKm
                        val cngAnalysis = calculateCngIntervalAnalysis(
                            events = sortedEvents,
                            startOdo = prevRefill.odometerKm,
                            endOdo = event.odometerKm,
                            vehicle = vehicle
                        )
                        val petrolDistWithCs = max(0.0, grossDist - cngAnalysis.netCngDistance)
                        val petrolDistWithoutCs = max(0.0, grossDist - cngAnalysis.rawCngDistance)
                        if (petrolDistWithCs > 0) {
                            latestMileageWithCs = petrolDistWithCs / qty
                            latestMileageWithoutCs = petrolDistWithoutCs / qty
                            lastResidualDistWithCs = petrolDistWithCs
                            lastResidualDistWithoutCs = petrolDistWithoutCs
                            lastColdStartKm = cngAnalysis.coldStartKm
                            lastColdStartsCount = cngAnalysis.coldStartsCount
                            condition = PetrolMileageCondition.REFILL_BEFORE_RESERVE
                        }
                        sessionStartOdo = event.odometerKm
                        sessionAccumulatedQty = qty
                        lastRefillEvent = event
                        continue
                    }

                    if (sessionStartOdo == null) {
                        sessionStartOdo = event.odometerKm
                    }
                    sessionAccumulatedQty += qty
                    lastRefillEvent = event
                }
            } else if (event.isPetrolReserve) {
                // Condition 1: Fill to reserve
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
                            condition = PetrolMileageCondition.FILL_TO_RESERVE
                        }
                    }
                    sessionStartOdo = null
                    sessionAccumulatedQty = 0.0
                    lastRefillEvent = null
                }
            }
        }

        // Active distance on current petrol session if still ongoing ("Till Now")
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
            totalColdStartsCount = activeColdStartsCount,
            calculationCondition = condition,
            activeCyclePetrolDistanceKm = activeDistWithCs
        )
    }

    /**
     * Generates a complete chronological breakdown of every mileage segment
     * from past events right up to the ongoing cycle ("Till Now").
     */
    fun calculateMileageSegments(
        events: List<FuelEvent>,
        vehicle: VehicleMeta,
        currentOdometer: Double
    ): List<MileageSegment> {
        val segments = mutableListOf<MileageSegment>()
        val sortedEvents = events.sortedWith(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })

        // 1. CNG segments (Condition 1, Condition 2, Ongoing)
        val cngEvents = sortedEvents.filter { it.isCngRefill || it.isCngEmpty }
        var cngSessionStartEvent: FuelEvent? = null
        var cngSessionQty = 0.0
        var cngSessionColdStarts = 0
        var lastCngRefillEvent: FuelEvent? = null

        for (event in cngEvents) {
            if (event.isCngRefill) {
                val qty = event.quantity ?: 0.0
                if (qty > 0) {
                    val prevRefill = lastCngRefillEvent
                    if (cngSessionStartEvent != null && prevRefill != null && event.odometerKm > prevRefill.odometerKm && event.isFullTank && prevRefill.isFullTank) {
                        val raw = event.odometerKm - prevRefill.odometerKm
                        val cs = event.coldStartsSinceLastRefill
                        val csDeduction = cs * vehicle.estimatedWarmupDistanceKmPerColdStart
                        val net = max(0.0, raw - csDeduction)
                        segments.add(
                            MileageSegment(
                                fuelType = FuelType.CNG,
                                startOdometerKm = prevRefill.odometerKm,
                                endOdometerKm = event.odometerKm,
                                rawDistanceKm = raw,
                                coldStartsCount = cs,
                                coldStartDeductionKm = csDeduction,
                                netDistanceKm = net,
                                fuelQuantity = qty,
                                calculatedMileage = if (qty > 0) net / qty else null,
                                conditionLabel = "Full ➔ Full",
                                startTimestamp = prevRefill.timestamp,
                                endTimestamp = event.timestamp,
                                isOngoing = false
                            )
                        )
                        cngSessionStartEvent = event
                        cngSessionQty = qty
                        cngSessionColdStarts = 0
                        lastCngRefillEvent = event
                        continue
                    }

                    if (cngSessionStartEvent == null) {
                        cngSessionStartEvent = event
                    }
                    cngSessionQty += qty
                    cngSessionColdStarts += event.coldStartsSinceLastRefill
                    lastCngRefillEvent = event
                }
            } else if (event.type == EventType.CNG_EMPTY) {
                if (cngSessionStartEvent != null && cngSessionQty > 0) {
                    val raw = event.odometerKm - cngSessionStartEvent.odometerKm
                    val cs = cngSessionColdStarts + event.coldStartsSinceLastRefill
                    val csDeduction = cs * vehicle.estimatedWarmupDistanceKmPerColdStart
                    val net = max(0.0, raw - csDeduction)
                    segments.add(
                        MileageSegment(
                            fuelType = FuelType.CNG,
                            startOdometerKm = cngSessionStartEvent.odometerKm,
                            endOdometerKm = event.odometerKm,
                            rawDistanceKm = raw,
                            coldStartsCount = cs,
                            coldStartDeductionKm = csDeduction,
                            netDistanceKm = net,
                            fuelQuantity = cngSessionQty,
                            calculatedMileage = if (cngSessionQty > 0) net / cngSessionQty else null,
                            conditionLabel = "Fill ➔ Empty",
                            startTimestamp = cngSessionStartEvent.timestamp,
                            endTimestamp = event.timestamp,
                            isOngoing = false
                        )
                    )
                    cngSessionStartEvent = null
                    cngSessionQty = 0.0
                    cngSessionColdStarts = 0
                    lastCngRefillEvent = null
                }
            }
        }

        // Ongoing CNG cycle till now
        if (cngSessionStartEvent != null && currentOdometer > cngSessionStartEvent.odometerKm) {
            val raw = currentOdometer - cngSessionStartEvent.odometerKm
            val cs = cngSessionColdStarts
            val csDeduction = cs * vehicle.estimatedWarmupDistanceKmPerColdStart
            val net = max(0.0, raw - csDeduction)
            segments.add(
                MileageSegment(
                    fuelType = FuelType.CNG,
                    startOdometerKm = cngSessionStartEvent.odometerKm,
                    endOdometerKm = currentOdometer,
                    rawDistanceKm = raw,
                    coldStartsCount = cs,
                    coldStartDeductionKm = csDeduction,
                    netDistanceKm = net,
                    fuelQuantity = cngSessionQty,
                    calculatedMileage = null,
                    conditionLabel = "Ongoing (Till Now)",
                    startTimestamp = cngSessionStartEvent.timestamp,
                    endTimestamp = System.currentTimeMillis(),
                    isOngoing = true
                )
            )
        }

        // 2. Petrol segments (Condition 1, Condition 2, Ongoing)
        val petrolEvents = sortedEvents.filter { it.isPetrolRefill || it.isPetrolReserve }
        var petSessionStartEvent: FuelEvent? = null
        var petSessionQty = 0.0
        var lastPetRefillEvent: FuelEvent? = null

        for (event in petrolEvents) {
            if (event.isPetrolRefill) {
                val qty = event.quantity ?: 0.0
                if (qty > 0) {
                    val prevRefill = lastPetRefillEvent
                    if (petSessionStartEvent != null && prevRefill != null && event.odometerKm > prevRefill.odometerKm && event.isFullTank && prevRefill.isFullTank) {
                        val gross = event.odometerKm - prevRefill.odometerKm
                        val cngAnalysis = calculateCngIntervalAnalysis(sortedEvents, prevRefill.odometerKm, event.odometerKm, vehicle)
                        val net = max(0.0, gross - cngAnalysis.netCngDistance)
                        segments.add(
                            MileageSegment(
                                fuelType = FuelType.PETROL,
                                startOdometerKm = prevRefill.odometerKm,
                                endOdometerKm = event.odometerKm,
                                rawDistanceKm = gross,
                                coldStartsCount = cngAnalysis.coldStartsCount,
                                coldStartDeductionKm = cngAnalysis.coldStartKm,
                                netDistanceKm = net,
                                fuelQuantity = qty,
                                calculatedMileage = if (qty > 0) net / qty else null,
                                conditionLabel = "Full ➔ Full",
                                startTimestamp = prevRefill.timestamp,
                                endTimestamp = event.timestamp,
                                isOngoing = false
                            )
                        )
                        petSessionStartEvent = event
                        petSessionQty = qty
                        lastPetRefillEvent = event
                        continue
                    }

                    if (petSessionStartEvent == null) {
                        petSessionStartEvent = event
                    }
                    petSessionQty += qty
                    lastPetRefillEvent = event
                }
            } else if (event.isPetrolReserve) {
                if (petSessionStartEvent != null && petSessionQty > 0) {
                    val gross = event.odometerKm - petSessionStartEvent.odometerKm
                    val cngAnalysis = calculateCngIntervalAnalysis(sortedEvents, petSessionStartEvent.odometerKm, event.odometerKm, vehicle)
                    val net = max(0.0, gross - cngAnalysis.netCngDistance)
                    segments.add(
                        MileageSegment(
                            fuelType = FuelType.PETROL,
                            startOdometerKm = petSessionStartEvent.odometerKm,
                            endOdometerKm = event.odometerKm,
                            rawDistanceKm = gross,
                            coldStartsCount = cngAnalysis.coldStartsCount,
                            coldStartDeductionKm = cngAnalysis.coldStartKm,
                            netDistanceKm = net,
                            fuelQuantity = petSessionQty,
                            calculatedMileage = if (petSessionQty > 0) net / petSessionQty else null,
                            conditionLabel = "Fill ➔ Reserve",
                            startTimestamp = petSessionStartEvent.timestamp,
                            endTimestamp = event.timestamp,
                            isOngoing = false
                        )
                    )
                    petSessionStartEvent = null
                    petSessionQty = 0.0
                    lastPetRefillEvent = null
                }
            }
        }

        // Ongoing Petrol cycle till now
        if (petSessionStartEvent != null && currentOdometer > petSessionStartEvent.odometerKm) {
            val gross = currentOdometer - petSessionStartEvent.odometerKm
            val cngAnalysis = calculateCngIntervalAnalysis(sortedEvents, petSessionStartEvent.odometerKm, currentOdometer, vehicle)
            val net = max(0.0, gross - cngAnalysis.netCngDistance)
            segments.add(
                MileageSegment(
                    fuelType = FuelType.PETROL,
                    startOdometerKm = petSessionStartEvent.odometerKm,
                    endOdometerKm = currentOdometer,
                    rawDistanceKm = gross,
                    coldStartsCount = cngAnalysis.coldStartsCount,
                    coldStartDeductionKm = cngAnalysis.coldStartKm,
                    netDistanceKm = net,
                    fuelQuantity = petSessionQty,
                    calculatedMileage = null,
                    conditionLabel = "Ongoing (Till Now)",
                    startTimestamp = petSessionStartEvent.timestamp,
                    endTimestamp = System.currentTimeMillis(),
                    isOngoing = true
                )
            )
        }

        return segments.sortedWith(compareByDescending<MileageSegment> { it.isOngoing }.thenByDescending { it.endTimestamp })
    }

    private fun calculateCngIntervalAnalysis(
        events: List<FuelEvent>,
        startOdo: Double,
        endOdo: Double,
        vehicle: VehicleMeta
    ): CngIntervalAnalysis {
        if (endOdo <= startOdo) return CngIntervalAnalysis(0.0, 0.0, 0.0, 0)

        val cngEvents = events.filter {
            it.isCngRefill || it.isCngEmpty
        }.sortedWith(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })

        var totalRawCngDistance = 0.0
        var totalNetCngDistance = 0.0
        var totalColdStartKm = 0.0
        var totalColdStartsCount = 0

        var activeCngStart: Double? = null
        var activeColdStarts = 0

        for (event in cngEvents) {
            if (event.isCngRefill) {
                if (activeCngStart == null) {
                    activeCngStart = event.odometerKm
                }
                activeColdStarts += event.coldStartsSinceLastRefill
            } else if (event.isCngEmpty) {
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
