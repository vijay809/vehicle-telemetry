package com.antigravity.telemetry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antigravity.telemetry.core.designsystem.AntiGravityTheme
import com.antigravity.telemetry.core.designsystem.CanvasLavender
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.components.FloatingQuickActionDock
import com.antigravity.telemetry.core.designsystem.components.SereneAppHeader
import com.antigravity.telemetry.core.telemetry.StationaryRefillPrompt
import com.antigravity.telemetry.feature.cngempty.CngEmptyTriggerScreen
import com.antigravity.telemetry.feature.cngempty.CngEmptyViewModel
import com.antigravity.telemetry.feature.dashboard.DashboardScreen
import com.antigravity.telemetry.feature.dashboard.DashboardViewModel
import com.antigravity.telemetry.feature.ledger.FuelLedgerScreen
import com.antigravity.telemetry.feature.ledger.FuelLedgerViewModel
import com.antigravity.telemetry.feature.refill.RefillViewModel
import com.antigravity.telemetry.feature.refill.RefillWizardSheet
import com.antigravity.telemetry.feature.simulator.SimulatorBottomSheet
import com.antigravity.telemetry.core.designsystem.components.OdometerUpdateSheet
import androidx.car.app.connection.CarConnection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AntiGravityApp
        val repository = app.repository
        val telemetryManager = app.telemetryManager
        val preferences = app.preferences

        // Monitor Android Auto host connection state from the mobile device
        val carConnection = CarConnection(this)
        carConnection.type.observe(this) { type ->
            val isConnected = (type == CarConnection.CONNECTION_TYPE_PROJECTION ||
                               type == CarConnection.CONNECTION_TYPE_NATIVE)
            telemetryManager.onConnectionStateChanged(isConnected)
        }

        setContent {
            AntiGravityTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route ?: "dashboard"

                var showRefillSheet by remember { mutableStateOf(false) }
                var showSimulatorSheet by remember { mutableStateOf(false) }
                var showOdometerSheet by remember { mutableStateOf(false) }
                var refillPrompt by remember { mutableStateOf<StationaryRefillPrompt?>(null) }

                // All bottom sheets allow slide/swipe down to close
                val refillSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val simulatorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val odometerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                // Listen for heuristic stationary refill prompt
                LaunchedEffect(Unit) {
                    telemetryManager.refillPromptFlow.collect { prompt ->
                        refillPrompt = prompt
                    }
                }

                // Stationary Refill Confirmation Dialog
                refillPrompt?.let { prompt ->
                    AlertDialog(
                        onDismissRequest = { refillPrompt = null },
                        title = { Text("Stationary Refill Detected!", fontWeight = FontWeight.Bold) },
                        text = {
                            Text("Fuel level increased by +${String.format("%.0f", prompt.fuelLevelDeltaPercent)}% while vehicle was stationary at ${String.format("%,.0f", prompt.detectedOdometerKm)} km. Would you like to log this refill?")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    refillPrompt = null
                                    showRefillSheet = true
                                }
                            ) {
                                Text("Log Refill", color = CngAccent, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { refillPrompt = null }) {
                                Text("Ignore", color = SlateTextMuted)
                            }
                        }
                    )
                }

                val telemetrySnapshot by repository.telemetryState.collectAsState(initial = com.antigravity.telemetry.core.model.TelemetrySnapshot())

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CanvasLavender,
                    topBar = {
                        if (currentRoute == "dashboard" || currentRoute == "ledger") {
                            SereneAppHeader(
                                isConnected = telemetrySnapshot.isConnectedToAuto,
                                vehicleName = "Victoris CNG",
                                onOpenSimulator = { showSimulatorSheet = true }
                            )
                        }
                    },
                    bottomBar = {
                        FloatingQuickActionDock(
                            currentRoute = currentRoute,
                            onDashboardClick = {
                                if (currentRoute != "dashboard") {
                                    if (!navController.popBackStack("dashboard", inclusive = false)) {
                                        navController.navigate("dashboard") {
                                            popUpTo("dashboard") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                            onRefillClick = {
                                showRefillSheet = true
                            },
                            onHistoryClick = {
                                if (currentRoute != "ledger") {
                                    navController.navigate("ledger") {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            enterTransition = {
                                if (targetState.destination.route == "dashboard") {
                                    slideInHorizontally(
                                        initialOffsetX = { -it / 3 },
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + scaleIn(
                                        initialScale = 0.94f,
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(280))
                                } else {
                                    slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + scaleIn(
                                        initialScale = 0.94f,
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(280))
                                }
                            },
                            exitTransition = {
                                if (targetState.destination.route == "dashboard") {
                                    slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + scaleOut(
                                        targetScale = 0.94f,
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + fadeOut(animationSpec = tween(280))
                                } else {
                                    slideOutHorizontally(
                                        targetOffsetX = { -it / 3 },
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + scaleOut(
                                        targetScale = 0.94f,
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + fadeOut(animationSpec = tween(280))
                                }
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it / 3 },
                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                                ) + scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(280))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                                ) + scaleOut(
                                    targetScale = 0.94f,
                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(280))
                            }
                        ) {
                            composable("dashboard") {
                                val dashboardVm = remember { DashboardViewModel(repository, preferences) }
                                DashboardScreen(
                                    viewModel = dashboardVm,
                                    onOpenSimulator = { showSimulatorSheet = true },
                                    onOpenOdometerSheet = { showOdometerSheet = true }
                                )
                            }

                            composable("cng_empty") {
                                val cngEmptyVm = remember { CngEmptyViewModel(repository) }
                                com.antigravity.telemetry.feature.cngempty.CngEmptyTriggerScreen(
                                    viewModel = cngEmptyVm,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("ledger") {
                                val ledgerVm = remember { FuelLedgerViewModel(repository) }
                                FuelLedgerScreen(
                                    viewModel = ledgerVm,
                                    onOpenSimulator = { showSimulatorSheet = true },
                                    onOpenOdometerSheet = { showOdometerSheet = true }
                                )
                            }
                        }
                    }
                }

                // Refill Wizard Bottom Sheet
                if (showRefillSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showRefillSheet = false },
                        sheetState = refillSheetState,
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ) {
                        val refillVm = remember { RefillViewModel(repository, app.preferences) }
                        RefillWizardSheet(
                            viewModel = refillVm,
                            onDismiss = {
                                showRefillSheet = false
                            }
                        )
                    }
                }

                // ECU Simulator Bottom Sheet
                if (showSimulatorSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSimulatorSheet = false },
                        sheetState = simulatorSheetState,
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ) {
                        SimulatorBottomSheet(
                            telemetryManager = telemetryManager,
                            onAdvanceOdometer = { delta ->
                                scope.launch {
                                    val currentOdo = repository.telemetryState.firstOrNull()?.odometerKm?.takeIf { it > 0.0 }
                                        ?: repository.getLastRefillEvent(com.antigravity.telemetry.core.model.FuelType.CNG)?.odometerKm
                                        ?: 9284.0
                                    repository.updateOdometer(currentOdo + delta)
                                }
                            },
                            onResetActualData = {
                                scope.launch {
                                    repository.resetActualData()
                                }
                            },
                            onSeedSampleData = {
                                scope.launch {
                                    repository.seedLifecycleSampleData()
                                }
                            },
                            onDismiss = {
                                scope.launch { simulatorSheetState.hide() }.invokeOnCompletion {
                                    showSimulatorSheet = false
                                }
                            }
                        )
                    }
                }

                // Odometer Update Bottom Sheet (Reusable)
                if (showOdometerSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showOdometerSheet = false },
                        sheetState = odometerSheetState,
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ) {
                        val currentOdo = telemetrySnapshot.odometerKm
                        OdometerUpdateSheet(
                            currentOdometerKm = currentOdo,
                            onConfirmOdometer = { newOdo ->
                                scope.launch {
                                    repository.logOdometerUpdate(newOdo)
                                    odometerSheetState.hide()
                                }.invokeOnCompletion {
                                    showOdometerSheet = false
                                }
                            },
                            onDismiss = {
                                scope.launch { odometerSheetState.hide() }.invokeOnCompletion {
                                    showOdometerSheet = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
