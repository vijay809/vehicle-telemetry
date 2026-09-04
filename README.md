# Vehicle Telemetry (AntiGravity) 🚗💨

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Android Auto](https://img.shields.io/badge/Companion-Android%20Auto-blue.svg)](https://developer.android.com/training/cars/apps)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.02.00-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

An offline-first Android application and Android Auto companion engineered specifically for bi-fuel (**CNG + Petrol**) vehicles. **Vehicle Telemetry** bridges the gap in stock automotive software by calculating true running costs, isolating dual fuel efficiencies, and accounting for engine warmup cold starts during CNG cycles.

Designed following the **"Serene Mobility"** soft-minimalist pastel aesthetic.

---

## 🌟 Key Features

### 1. Dual-Fuel Efficiency & Running Cost Engines
- **Model A: Blended Running Cost (₹/km)**:
  - Calculates true blended cost per kilometer across both CNG and Petrol expenditures.
  - Computes exact monthly savings compared to a pure petrol baseline and tracks the expenditure ratio.
- **Model B: Pure CNG Tank-to-Tank Efficiency (km/kg)**:
  - Full-to-empty cycle boundary segmentation with automatic 90% auto-cut refill heuristic.
  - Cold-start compensation: isolates petrol warmup distance ($1.2\text{ km/cold start}$) from raw CNG odometer mileage.
  - Automatic switchover detection and reserve tracking when CNG runs out.
- **Model C: Dual Petrol Mileage Calculation (km/L)**:
  - Calculates two distinct petrol mileage metrics:
    - **With Cold Starts (`w/ CS`)**: Credits warmup distance driven on petrol during CNG cycles ($D_{\text{petrol}} = \text{gross} - (\text{raw}_{\text{CNG}} - \text{CS}_{\text{km}})$).
    - **Without Cold Starts (`w/o`)**: Pure steady-state petrol cruising distance ($D_{\text{petrol}} = \text{gross} - \text{raw}_{\text{CNG}}$).
  - 1-tap header toggle switches between the two metrics with instant UI reactivity.
  - Subtractive residual engine subtracting validated CNG intervals between fill and low-fuel warning triggers.

### 2. In-Car Android Auto Companion
- Built using the Android for Cars App Library (`androidx.car.app`).
- Low-distraction `PaneTemplate` displaying live blended cost (₹/km), remaining CNG range, and active fuel source.
- 1-tap **"CNG Ran Out"** in-car action to mark empty points while driving.
- Heuristic stationary refill detection: automatically triggers a prompt when fuel level jumps by $\ge 8\%$ while stationary.

### 3. Log Refill Wizard
- 2x2 bento grid with 4-way interdependent inputs:
  1. **Dispensed** (KG / L input with step controls)
  2. **Total Cost** (₹ currency input with quick-add chips)
  3. **Unit Rate** (₹/kg or ₹/L with persistent memory)
  4. **Full Tank Auto-Cut** (interactive toggle with persistent preference memory)
- Interdependent auto-calculation rules:
  - Dispensed change $\rightarrow$ updates Total Cost
  - Cost change $\rightarrow$ updates Dispensed
  - Unit Rate change $\rightarrow$ updates Total Cost
  - Dispensed + Cost change $\rightarrow$ updates Unit Rate
- Quick-access two-button row (**Dismiss** & **Save**) right under the header for fast one-handed logging.
- Fluid swipe-to-close bottom sheet design.

### 4. CNG Empty Alert Sheet
- Modern modal bottom sheet format.
- Real-time OBD verified odometer snapshot.
- Interactive cold-start stepper with dynamic footnote calculation.
- Live CNG pressure and petrol reserve indicators.

### 5. ECU & Telemetry Simulator
- Built-in simulation sheet to test telemetry triggers, switchovers, and algorithms without a connected car.
- Live Drive Simulation advancing odometer at **+0.25 km/s** (900 km/h testing pace) with full 2-decimal precision.
- 1-tap stationary refill simulation jump (+25%).
- Complete data isolation: simulation logs are strictly partitioned from real vehicle data with a 1-tap "Reset Actual Data" button.

---

## 🎨 Design System: "Serene Mobility"

The interface is crafted using the **Serene Mobility** design language:
- **Color Palette**:
  - **CNG (Mint)**: `#ECFDF5` (Pastel Bg), `#A7F3D0` (Border), `#059669` (Accent)
  - **Petrol (Apricot)**: `#FFFBEB` (Pastel Bg), `#FDE68A` (Border), `#D97706` (Accent)
  - **Background (Lavender)**: `#F8FAFC`
  - **Alerts (Rose)**: `#FFF1F2` (Pastel Bg), `#FECDD3` (Border), `#E11D48` (Accent)
- **Typography**: Outfit for bold telemetry readouts and Nunito Sans for clean legibility.
- **Micro-interactions**: Smooth animated progress tracks, subtle box shadows, and pulsing live sync indicators.

---

## 🏗️ Architecture & Tech Stack

```
com.antigravity.telemetry
├── car/                  # Android Auto CarAppService & Car Screens
├── core/
│   ├── database/         # Room Database, DAOs, and Entities
│   ├── designsystem/     # Serene Mobility theme, tokens, and bento components
│   ├── engine/           # Mathematical Models A, B, and C
│   ├── model/            # Domain models and enums
│   ├── repository/       # TelemetryRepository & FuelPreferences (SharedPreferences)
│   └── telemetry/        # TelemetryManager & stationary refill heuristics
└── feature/
    ├── cngempty/         # CNG Empty Bottom Sheet & ViewModel
    ├── dashboard/        # Main Dashboard, Cards, and ViewModel
    ├── ledger/           # Fuel Ledger Timeline, Filters, and 30D summary
    ├── refill/           # Log Refill Wizard Sheet & ViewModel
    └── simulator/        # ECU Simulator Sheet
```

- **UI Framework**: 100% Jetpack Compose with Material 3.
- **In-Car Platform**: Android for Cars App Library (`androidx.car.app`).
- **Database**: AndroidX Room (offline-first SQLite).
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow` / `SharedFlow`.
- **State Management**: Clean MVVM architecture with unidirectional data flow.

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Iguana (2023.2.1) or newer.
- **Android SDK**: `minSdk = 26` (Android 8.0 Oreo), `targetSdk = 34` (Android 14).
- **JDK**: Java 17+.

### Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/vijay809/vehicle-telemetry.git
   cd vehicle-telemetry
   ```

2. **Open in Android Studio** or build from command line:
   ```bash
   # Build debug APK
   ./gradlew assembleDebug

   # Install directly onto connected physical device or emulator
   ./gradlew installDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 🧪 Testing the Algorithms

1. Open the app and tap the **Tune** icon in the top right to open the **ECU & Telemetry Simulator**.
2. Toggle **Live Drive Simulation** on to watch the odometer advance at **+0.25 km/s**.
3. Tap **"+ Refill Fuel"** on the floating dock to log a CNG or Petrol fill and observe the interdependent unit rate calculation.
4. Tap **"CNG Empty"** on the floating dock to simulate running out of gas and observe:
   - CNG card greying out into "Exhausted" state.
   - Petrol card automatically rising to the top as the active fuel in use.
   - Cold start toggle (`w/ CS` vs `w/o`) dynamically recalculating petrol efficiency.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
