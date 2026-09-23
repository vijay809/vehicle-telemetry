package com.antigravity.telemetry.core.database

import androidx.room.TypeConverter
import com.antigravity.telemetry.core.model.EventSource
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelType

class Converters {
    @TypeConverter
    fun fromFuelType(value: FuelType?): String? = value?.name

    @TypeConverter
    fun toFuelType(value: String?): FuelType? = value?.let { enumValueOf<FuelType>(it) }

    @TypeConverter
    fun fromEventSource(value: EventSource): String = value.name

    @TypeConverter
    fun toEventSource(value: String): EventSource = enumValueOf(value)

    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = try {
        enumValueOf<EventType>(value)
    } catch (e: Exception) {
        when (value) {
            "REFILL" -> EventType.CNG_FILL
            "FUEL_LOW" -> EventType.PETROL_RESERVE
            else -> EventType.ODOMETER_UPDATE
        }
    }
}
