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

enum class CostTimeframe(val label: String, val months: Int) {
    ONE_MONTH("1M", 1),
    THREE_MONTHS("3M", 3),
    SIX_MONTHS("6M", 6),
    TWELVE_MONTHS("12M", 12)
}

enum class CngMileageCondition(val label: String, val description: String) {
    FILL_TO_EMPTY("Fill ➔ Empty", "Tank ran completely empty"),
    REFILL_BEFORE_EMPTY("Full ➔ Full", "Refilled to full before empty"),
    AWAITING_DATA("Awaiting Data", "Not enough completed cycles")
}

enum class PetrolMileageCondition(val label: String, val description: String) {
    FILL_TO_RESERVE("Fill ➔ Reserve", "Driven down to reserve indicator"),
    REFILL_BEFORE_RESERVE("Full ➔ Full", "Refilled to full before reserve"),
    AWAITING_DATA("Awaiting Data", "Not enough completed cycles")
}
