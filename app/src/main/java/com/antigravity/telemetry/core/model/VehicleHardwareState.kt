package com.antigravity.telemetry.core.model

enum class HardwareStatus(val label: String) {
    SUCCESS("SUCCESS"),
    UNSUPPORTED_BY_CAR("UNSUPPORTED"),
    UNAVAILABLE("UNAVAILABLE"),
    AWAITING("AWAITING")
}

data class VehicleHardwareState(
    // Identity & Profile
    val manufacturer: String? = null,
    val modelName: String? = null,
    val modelYear: Int? = null,
    val modelStatus: HardwareStatus = HardwareStatus.AWAITING,
    val fuelTypes: List<String> = emptyList(),
    val evConnectorTypes: List<String> = emptyList(),
    val profileStatus: HardwareStatus = HardwareStatus.AWAITING,

    // Mileage & Speed
    val odometerKm: Double? = null,
    val odometerMetersRaw: Float? = null,
    val mileageStatus: HardwareStatus = HardwareStatus.AWAITING,
    val speedKmh: Double? = null,
    val speedMpsRaw: Float? = null,
    val speedStatus: HardwareStatus = HardwareStatus.AWAITING,

    // Fuel & Energy
    val fuelPercent: Double? = null,
    val batteryPercent: Double? = null,
    val isLowFuel: Boolean? = null,
    val rangeRemainingKm: Double? = null,
    val energyStatus: HardwareStatus = HardwareStatus.AWAITING,

    // Compass
    val compassYaw: Float? = null,
    val compassPitch: Float? = null,
    val compassRoll: Float? = null,
    val compassStatus: HardwareStatus = HardwareStatus.AWAITING,

    // Accelerometer & Gyroscope
    val accelX: Float? = null,
    val accelY: Float? = null,
    val accelZ: Float? = null,
    val accelStatus: HardwareStatus = HardwareStatus.AWAITING,
    val gyroX: Float? = null,
    val gyroY: Float? = null,
    val gyroZ: Float? = null,
    val gyroStatus: HardwareStatus = HardwareStatus.AWAITING,

    // Hardware GPS Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val bearing: Float? = null,
    val locationStatus: HardwareStatus = HardwareStatus.AWAITING,

    // Peripherals
    val tollCardState: String? = null,
    val tollStatus: HardwareStatus = HardwareStatus.AWAITING,
    val evPortOpen: Boolean? = null,
    val evPortConnected: Boolean? = null,
    val evStatus: HardwareStatus = HardwareStatus.AWAITING,

    // Meta
    val isConnected: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
