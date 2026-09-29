package com.antigravity.telemetry.core.telemetry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.car.app.connection.CarConnection
import com.antigravity.telemetry.AntiGravityApp

class CarConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val app = context.applicationContext as? AntiGravityApp ?: return
        AutoTelemetryLogger.log("CAR_RECEIVER", "Broadcast received: $action")

        when (action) {
            "android.app.action.ENTER_CAR_MODE" -> {
                AutoTelemetryLogger.log("CAR_RECEIVER", "ENTER_CAR_MODE triggered")
                app.telemetryManager.onConnectionStateChanged(true)
            }
            "android.app.action.EXIT_CAR_MODE" -> {
                AutoTelemetryLogger.log("CAR_RECEIVER", "EXIT_CAR_MODE triggered")
                app.telemetryManager.onConnectionStateChanged(false)
            }
            "androidx.car.app.connection.action.CAR_CONNECTION_UPDATED" -> {
                try {
                    val carConnection = CarConnection(context)
                    val type = carConnection.type.value ?: CarConnection.CONNECTION_TYPE_NOT_CONNECTED
                    val isConnected = (type == CarConnection.CONNECTION_TYPE_PROJECTION ||
                                       type == CarConnection.CONNECTION_TYPE_NATIVE)
                    AutoTelemetryLogger.log("CAR_RECEIVER", "CAR_CONNECTION_UPDATED: type=$type isConnected=$isConnected")
                    app.telemetryManager.onConnectionStateChanged(isConnected)
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("CAR_RECEIVER", "Error reading CarConnection: ${e.message}")
                }
            }
            "android.bluetooth.device.action.ACL_CONNECTED",
            "android.intent.action.ACTION_POWER_CONNECTED" -> {
                AutoTelemetryLogger.log("CAR_RECEIVER", "$action -> Checking vehicle connection")
                try {
                    val carConnection = CarConnection(context)
                    val type = carConnection.type.value ?: CarConnection.CONNECTION_TYPE_NOT_CONNECTED
                    val isConnected = (type == CarConnection.CONNECTION_TYPE_PROJECTION ||
                                       type == CarConnection.CONNECTION_TYPE_NATIVE)
                    if (isConnected) {
                        AutoTelemetryLogger.log("CAR_RECEIVER", "Android Auto confirmed active upon $action")
                        app.telemetryManager.onConnectionStateChanged(true)
                    }
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("CAR_RECEIVER", "Error reading CarConnection on $action: ${e.message}")
                }
            }
            "android.bluetooth.device.action.ACL_DISCONNECTED",
            "android.intent.action.ACTION_POWER_DISCONNECTED" -> {
                AutoTelemetryLogger.log("CAR_RECEIVER", "$action -> Checking vehicle disconnect")
                try {
                    val carConnection = CarConnection(context)
                    val type = carConnection.type.value ?: CarConnection.CONNECTION_TYPE_NOT_CONNECTED
                    val isConnected = (type == CarConnection.CONNECTION_TYPE_PROJECTION ||
                                       type == CarConnection.CONNECTION_TYPE_NATIVE)
                    if (!isConnected && app.preferences.isVehicleConnected()) {
                        AutoTelemetryLogger.log("CAR_RECEIVER", "Vehicle disconnected upon $action")
                        app.telemetryManager.onConnectionStateChanged(false)
                    }
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("CAR_RECEIVER", "Error on disconnect check: ${e.message}")
                }
            }
        }
    }
}
