package com.antigravity.telemetry.core.model

enum class FuelType {
    CNG,
    PETROL
}

enum class EventSource {
    MANUAL,
    ANDROID_AUTO,
    OBD2
}

enum class EventType {
    CNG_FILL,
    CNG_EMPTY,
    PETROL_FILL,
    PETROL_RESERVE,
    ODOMETER_UPDATE,
    MANUAL_FUEL_SWITCH,
    COLD_START,
    WARM_START,

    // Legacy types retained for database backward-compatibility
    REFILL,
    FUEL_LOW;

    val isCng: Boolean
        get() = this == CNG_FILL || this == CNG_EMPTY

    val isPetrol: Boolean
        get() = this == PETROL_FILL || this == PETROL_RESERVE || this == FUEL_LOW

    val isFill: Boolean
        get() = this == CNG_FILL || this == PETROL_FILL || this == REFILL
}
