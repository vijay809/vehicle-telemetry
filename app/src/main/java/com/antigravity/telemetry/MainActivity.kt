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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.telemetry.core.designsystem.AntiGravityTheme
import com.antigravity.telemetry.core.designsystem.CanvasLavender
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AntiGravityApp
        val repository = app.repository
        val telemetryManager = app.telemetryManager

        setContent {
            AntiGravityTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                var showRefillSheet by remember { mutableStateOf(false) }
                var showSimulatorSheet by remember { mutableStateOf(false) }
                var refillPrompt by remember { mutableStateOf<StationaryRefillPrompt?>(null) }

                val refillSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { it != androidx.compose.material3.SheetValue.Hidden }
                )
                val simulatorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CanvasLavender
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "dashboard") {
                            composable("dashboard") {
                                val dashboardVm = remember { DashboardViewModel(repository) }
                                DashboardScreen(
                                    viewModel = dashboardVm,
                                    onNavigateToRefill = { showRefillSheet = true },
                                    onNavigateToCngEmpty = { navController.navigate("cng_empty") },
                                    onNavigateToLedger = { navController.navigate("ledger") },
                                    onOpenSimulator = { showSimulatorSheet = true }
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
                                    onBack = { navController.popBackStack() }
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
                                    val currentOdo = repository.getLastRefillEvent(com.antigravity.telemetry.core.model.FuelType.CNG)?.odometerKm ?: 42850.0
                                    repository.updateOdometer(currentOdo + delta)
                                }
                            },
                            onResetActualData = {
                                scope.launch {
                                    repository.resetActualData()
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
            }
        }
    }
}
