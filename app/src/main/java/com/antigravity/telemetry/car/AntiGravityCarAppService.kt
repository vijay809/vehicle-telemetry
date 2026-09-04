package com.antigravity.telemetry.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.validation.HostValidator
import com.antigravity.telemetry.AntiGravityApp
import com.antigravity.telemetry.core.model.EventSource
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AntiGravityCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return AntiGravityCarSession()
    }
}

class AntiGravityCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return TelemetryCarScreen(carContext)
    }
}

class TelemetryCarScreen(carContext: CarContext) : Screen(carContext) {

    private val repository = (carContext.applicationContext as AntiGravityApp).repository
    private val scope = CoroutineScope(Dispatchers.IO)
    private var currentOdo = 42850.0

    init {
        scope.launch {
            repository.telemetryState.collect { snapshot ->
                currentOdo = snapshot.odometerKm
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val cngEmptyAction = Action.Builder()
            .setTitle("CNG Ran Out")
            .setBackgroundColor(CarColor.createCustom(0xFFD97706.toInt(), 0xFFD97706.toInt()))
            .setOnClickListener {
                scope.launch {
                    repository.addEvent(
                        FuelEvent(
                            odometerKm = currentOdo,
                            source = EventSource.ANDROID_AUTO,
                            type = EventType.CNG_EMPTY,
                            confirmedByUser = true
                        )
                    )
                }
            }
            .build()

        val actionStrip = ActionStrip.Builder()
            .addAction(cngEmptyAction)
            .build()

        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Blended Running Cost")
                    .addText("₹3.42 / km • 82% CNG / 18% Petrol")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Current Odometer: ${String.format("%.0f", currentOdo)} km")
                    .addText("CNG Pressure: 32 bar • Petrol Level: 70%")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("1-Tap Action Ready")
                    .addText("Tap 'CNG Ran Out' when vehicle auto-switches to petrol.")
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setTitle("Vehicle Telemetry Auto")
            .setActionStrip(actionStrip)
            .build()
    }
}
