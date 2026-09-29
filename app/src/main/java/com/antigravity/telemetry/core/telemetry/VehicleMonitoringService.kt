package com.antigravity.telemetry.core.telemetry

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.antigravity.telemetry.AntiGravityApp
import com.antigravity.telemetry.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VehicleMonitoringService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var heartbeatJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = applicationContext as? AntiGravityApp
        val prefs = app?.preferences

        val initialNotification = buildNotification("Connected to vehicle • Tracking trip telemetry")
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    initialNotification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    } else {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    }
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            AutoTelemetryLogger.log("SERVICE", "startForeground failed: ${e.message}", isError = true)
        }

        startHeartbeatLoop(prefs)

        return START_STICKY
    }

    private fun startHeartbeatLoop(prefs: com.antigravity.telemetry.core.repository.FuelPreferences?) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            val connectTime = prefs?.getActiveStartConnectTimestamp() ?: System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                prefs?.setLastConnectionHeartbeat(now)

                val elapsedMinutes = maxOf(0, ((now - connectTime) / 60_000L).toInt())
                val text = if (elapsedMinutes > 0) {
                    "Connected to vehicle • ${elapsedMinutes}m drive"
                } else {
                    "Connected to vehicle • Tracking trip telemetry"
                }

                val notification = buildNotification(text)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.notify(NOTIFICATION_ID, notification)

                delay(30_000L) // Update every 30 seconds
            }
        }
    }

    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("AntiGravity Telemetry")
            .setContentText(contentText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vehicle Connection Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent status indicator while connected to Android Auto"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        try {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "vehicle_telemetry_channel"

        fun start(context: Context) {
            try {
                val intent = Intent(context, VehicleMonitoringService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                AutoTelemetryLogger.log("SERVICE", "VehicleMonitoringService started.")
            } catch (e: Exception) {
                AutoTelemetryLogger.log("SERVICE", "Failed to start VehicleMonitoringService: ${e.message}", isError = true)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, VehicleMonitoringService::class.java)
                context.stopService(intent)
                AutoTelemetryLogger.log("SERVICE", "VehicleMonitoringService stopped.")
            } catch (e: Exception) {
                AutoTelemetryLogger.log("SERVICE", "Failed to stop VehicleMonitoringService: ${e.message}", isError = true)
            }
        }
    }
}
