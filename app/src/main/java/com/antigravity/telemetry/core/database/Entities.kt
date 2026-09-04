package com.antigravity.telemetry.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.antigravity.telemetry.core.model.EventSource
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelType

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cngTankCapacityKg: Double,
    val petrolTankCapacityL: Double,
    val estimatedWarmupDistanceKmPerColdStart: Double,
    val activeOdometerKm: Double
)

@Entity(tableName = "fuel_events")
data class FuelEventEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val timestamp: Long,
    val odometerKm: Double,
    val source: EventSource,
    val type: EventType,
    val fuelType: FuelType?,
    val quantity: Double?,
    val pricePerUnit: Double?,
    val totalCost: Double?,
    val isFullTank: Boolean,
    val fuelLevelPercent: Double?,
    val coldStartsSinceLastRefill: Int,
    val confirmedByUser: Boolean,
    val stationName: String?,
    val isSimulation: Boolean = false
)

@Entity(tableName = "drive_segments")
data class DriveSegmentEntity(
    @PrimaryKey val id: String,
    val startOdometerKm: Double,
    val endOdometerKm: Double,
    val distanceKm: Double,
    val fuelType: FuelType,
    val associatedRefillEventId: String?,
    val isSimulation: Boolean = false
)
