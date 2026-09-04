package com.antigravity.telemetry.core.model

import java.util.UUID

data class VehicleMeta(
    val id: String = "default-vehicle-victoris",
    val name: String = "Victoris CNG",
    val cngTankCapacityKg: Double = 10.0,
    val petrolTankCapacityL: Double = 45.0,
    val estimatedWarmupDistanceKmPerColdStart: Double = 1.2,
    val activeOdometerKm: Double = 0.0
)

data class FuelEvent(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String = "default-vehicle-victoris",
    val timestamp: Long = System.currentTimeMillis(),
    val odometerKm: Double,
    val source: EventSource = EventSource.MANUAL,
    val type: EventType,
    val fuelType: FuelType? = null,
    val quantity: Double? = null,         // kg for CNG, L for Petrol
    val pricePerUnit: Double? = null,     // ₹/kg or ₹/L
    val totalCost: Double? = null,        // ₹
    val isFullTank: Boolean = false,      // Marks auto-cut tank top-off
    val fuelLevelPercent: Double? = null, // Petrol level snapshot 0.0 - 100.0 (nullable)
    val coldStartsSinceLastRefill: Int = 0,
    val confirmedByUser: Boolean = true,
    val stationName: String? = null,
    val isSimulation: Boolean = false
)

data class DriveSegment(
    val id: String = UUID.randomUUID().toString(),
    val startOdometerKm: Double,
    val endOdometerKm: Double,
    val distanceKm: Double,
    val fuelType: FuelType,
    val associatedRefillEventId: String? = null,
    val isSimulation: Boolean = false
)

data class TelemetrySnapshot(
    val odometerKm: Double = 0.0,
    val speedKmh: Double = 0.0,
    val petrolPercent: Double? = null,      // Only non-null if reported by vehicle hardware
    val cngPressureBar: Double? = null,     // Only non-null if reported by vehicle hardware
    val isAutoModeActive: Boolean = true,
    val isLowFuelWarning: Boolean = false,
    val isConnectedToAuto: Boolean = false,
    val isSimulation: Boolean = false
)
