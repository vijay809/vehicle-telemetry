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
    REFILL,
    CNG_EMPTY,
    FUEL_LOW
}
