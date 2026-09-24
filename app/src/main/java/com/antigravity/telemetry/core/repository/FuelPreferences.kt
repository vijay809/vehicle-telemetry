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

    fun isPetrolColdStartIncluded(): Boolean {
        return prefs.getBoolean("petrol_include_cold_start", true)
    }

    fun setPetrolColdStartIncluded(included: Boolean) {
        prefs.edit().putBoolean("petrol_include_cold_start", included).apply()
    }

    fun getSimulationMode(): Boolean {
        return prefs.getBoolean("is_simulation_mode", false)
    }

    fun setSimulationMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_simulation_mode", enabled).apply()
    }

    fun getCalibratedOdometer(): Double {
        return prefs.getFloat("calibrated_odometer", 9284.0f).toDouble()
    }

    fun setCalibratedOdometer(odometerKm: Double) {
        prefs.edit().putFloat("calibrated_odometer", odometerKm.toFloat()).apply()
    }

    fun getCalibratedFuelPercent(): Double {
        return prefs.getFloat("calibrated_fuel_percent", 25.0f).toDouble()
    }

    fun setCalibratedFuelPercent(fuelPercent: Double) {
        prefs.edit().putFloat("calibrated_fuel_percent", fuelPercent.toFloat()).apply()
    }

    // Android Auto Connection Tracker & Counter
    fun getAutoConnectionCount(): Int {
        return prefs.getInt("auto_connection_count", 0)
    }

    fun incrementAutoConnectionCount(): Int {
        val newCount = getAutoConnectionCount() + 1
        prefs.edit()
            .putInt("auto_connection_count", newCount)
            .putLong("last_auto_connected_timestamp", System.currentTimeMillis())
            .apply()
        return newCount
    }

    fun resetAutoConnectionCount() {
        prefs.edit()
            .putInt("auto_connection_count", 0)
            .putLong("total_auto_connected_minutes", 0L)
            .apply()
    }

    fun getLastAutoConnectedTimestamp(): Long {
        return prefs.getLong("last_auto_connected_timestamp", 0L)
    }

    fun setLastAutoConnectedTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_auto_connected_timestamp", timestamp).apply()
    }

    fun getTotalAutoConnectedDurationMinutes(): Long {
        return prefs.getLong("total_auto_connected_minutes", 0L)
    }

    fun addAutoConnectedDurationMinutes(minutes: Long) {
        val total = getTotalAutoConnectedDurationMinutes() + minutes
        prefs.edit().putLong("total_auto_connected_minutes", total).apply()
    }

    // Vehicle Disconnect Tracking (for Cold vs Warm Start Detection)
    fun getLastAutoDisconnectedTimestamp(): Long {
        return prefs.getLong("last_auto_disconnected_timestamp", 0L)
    }

    fun setLastAutoDisconnectedTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_auto_disconnected_timestamp", timestamp).apply()
    }

    // Active CNG Cycle Auto-Detected Cold Starts
    fun getActiveCycleColdStarts(): Int {
        return prefs.getInt("active_cycle_cold_starts", 0)
    }

    fun incrementActiveCycleColdStarts(): Int {
        val newCount = getActiveCycleColdStarts() + 1
        prefs.edit().putInt("active_cycle_cold_starts", newCount).apply()
        return newCount
    }

    fun resetActiveCycleColdStarts() {
        prefs.edit().putInt("active_cycle_cold_starts", 0).apply()
    }

    // Cold Start Cooldown Threshold in Hours (Default: 3.5 hours)
    fun getColdStartThresholdHours(): Float {
        return prefs.getFloat("cold_start_threshold_hours", 3.5f)
    }

    fun setColdStartThresholdHours(hours: Float) {
        prefs.edit().putFloat("cold_start_threshold_hours", hours).apply()
    }
}
