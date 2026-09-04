package com.antigravity.telemetry.core.repository

import android.content.Context
import android.content.SharedPreferences
import com.antigravity.telemetry.core.model.FuelType

class FuelPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("antigravity_fuel_preferences", Context.MODE_PRIVATE)

    fun getUnitRate(fuelType: FuelType): Double {
        return when (fuelType) {
            FuelType.CNG -> prefs.getFloat("cng_unit_rate", 89.0f).toDouble()
            FuelType.PETROL -> prefs.getFloat("petrol_unit_rate", 96.5f).toDouble()
        }
    }

    fun setUnitRate(fuelType: FuelType, rate: Double) {
        if (rate <= 0.0) return
        val key = if (fuelType == FuelType.CNG) "cng_unit_rate" else "petrol_unit_rate"
        prefs.edit().putFloat(key, rate.toFloat()).apply()
    }

    fun getAutoCut(fuelType: FuelType): Boolean {
        return when (fuelType) {
            FuelType.CNG -> prefs.getBoolean("cng_auto_cut", true)
            FuelType.PETROL -> prefs.getBoolean("petrol_auto_cut", true)
        }
    }

    fun setAutoCut(fuelType: FuelType, enabled: Boolean) {
        val key = if (fuelType == FuelType.CNG) "cng_auto_cut" else "petrol_auto_cut"
        prefs.edit().putBoolean(key, enabled).apply()
    }
}
