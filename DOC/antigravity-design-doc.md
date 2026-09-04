# System Design Document: AntiGravity
## Bi-Fuel Vehicle Telemetry & Mileage Tracking System

---

## 1. Overview & Problem Statement

Modern bi-fuel vehicles (Petrol + Compressed Natural Gas / CNG) switch dynamically between two separate fuel reserves:
- **Petrol** is consumed during initial cold starts, high-load conditions, and periodic injector lubricity intervals.
- **CNG** is consumed during steady cruising and normal driving conditions.
- Commercial dashboards display disjointed gauges: an analog cylinder pressure dial or auxiliary LED bar for CNG alongside the OEM digital petrol gauge.

Because distance traveled accumulates on a single odometer while fuel is drawn intermittently from two distinct reservoirs measured in mismatched units (liters vs. kilograms), standard distance-over-volume formulas fail. Drivers cannot isolate true CNG fuel economy, measure residual petrol drain, or accurately compute their real running cost per kilometer.

**AntiGravity** is an offline-first, client-side application paired with an Android Auto projection companion. It ingests manual logs and automated vehicle telemetry via the Android for Cars App Library (`androidx.car.app.hardware`) to compute:
1. Blended running costs (₹/km).
2. Pure CNG tank-to-tank efficiency (km/kg).
3. Subtractive residual petrol consumption (km/L).

---

## 2. Goals & Non-Goals

### Goals
- **Blended Running Cost (₹/km):** Real-time rolling and per-trip financial efficiency calculations.
- **Pure CNG Isolation (km/kg):** Automated or 1-tap capture of full-to-empty CNG drive cycles.
- **Subtractive Residual Petrol Analysis (km/L):** Long-range attribution of hidden petrol consumption during cold starts and auto-switchovers.
- **Hardware Telemetry Integration:** Zero-friction telemetry ingestion (odometer sync, fuel-low alerts, stationary fuel-jump detection) via Android Auto `CarHardwareManager`.
- **Zero Backend / Complete Privacy:** Client-only data persistence with exportable snapshots and local transactional integrity.

### Non-Goals
- Proprietary CAN bus reverse-engineering for aftermarket CNG ECUs (no physical wire-splicing required).
- Cloud fleet management, multi-user accounts, or mandatory remote server synchronization.
- Turn-by-turn navigation or route planning.

---

## 3. High-Level Architecture

The system operates across two distinct domains: the **Android Auto / Android Native Host Layer** (telemetry ingestion and in-car quick actions) and the **Client Application Layer** (local database, state machine, and analytical engine).

```
┌────────────────────────────────────────────────────────────────────────┐
│                      Android In-Car Environment                        │
│                                                                        │
│   ┌──────────────────────────────┐    ┌────────────────────────────┐   │
│   │ Android Auto Screen Template │    │ OEM Vehicle Head Unit      │   │
│   │ - 1-Tap "CNG Empty" Button   │    │ - CAN Bus (Odometer, Fuel) │   │
│   │ - Refill Prompt Dialog       │    │                            │   │
│   └──────────────┬───────────────┘    └──────────────┬─────────────┘   │
└──────────────────┼───────────────────────────────────┼─────────────────┘
                   │                                   │
                   │ User Events                       │ CarHardwareManager
                   ▼                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    Host Layer (Android Service / Bridge)               │
│                                                                        │
│       VehicleTelemetryService (MileageListener, EnergyLevelListener)   │
│            ├── Transforms raw hardware ticks to domain events          │
│            └── Detects stationary tank level deltas (>8-10%)           │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ Event Stream / Native Bridge
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│               Client Application Layer (Offline-First)                 │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Calculation & Event State Machine                              │   │
│   │ - Model A: Financial Aggregation Engine                        │   │
│   │ - Model B: CNG State Segmenter                                 │   │
│   │ - Model C: Residual Petrol Attributor                          │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │ Reads / Writes                      │
│   ┌──────────────────────────────▼─────────────────────────────────┐   │
│   │ Storage Engine (IndexedDB via Dexie.js / SQLite via op-sqlite) │   │
│   │ - vehicles, fuel_events, telemetry_snapshots                   │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Telemetry & Android Auto Integration

### Permissions & Android Manifest
The host app registers hardware access under Android for Cars requirements:

```xml
<uses-permission android:name="androidx.car.app.ACCESS_SURFACE" />
<uses-permission android:name="com.google.android.gms.permission.CAR_MILEAGE" />
<uses-permission android:name="com.google.android.gms.permission.CAR_FUEL" />
<uses-permission android:name="com.google.android.gms.permission.CAR_SPEED" />
```

### Telemetry Pipeline
1. **Odometer Ingestion:** `CarInfo.addMileageListener` captures vehicle distance in meters. Distance is converted to kilometers and kept in an active memory cache to prefill all event dialogues.
2. **Petrol Refill Detection:** `CarInfo.addEnergyLevelListener` monitors `fuelPercent` and `fuelVolumeRemaining`.
   - If `fuelPercent` increases by $\ge 8\%$ while instantaneous vehicle speed is $0	ext{ km/h}$, an unconfirmed `REFILL` event (Petrol) is queued for confirmation.
3. **Petrol Low Trigger:** If `lowFuelWarningLevel` changes to `true`, a `FUEL_LOW` snapshot is persisted with the exact odometer timestamp.
4. **Android Auto UI Surface:** 
   - Uses `PaneTemplate` and `ActionStrip` adhering to driver distraction standards.
   - Provides an immediate high-contrast button: **"CNG Ran Out"**. Tapping logs a `CNG_EMPTY` event immediately, capturing the current odometer reading without requiring mobile phone handling.

---

## 5. Data Models & Local Storage Schema

Implemented using IndexedDB via Dexie.js (for PWA) or SQLite (for React Native):

```typescript
export type FuelType = 'CNG' | 'PETROL';
export type EventSource = 'MANUAL' | 'ANDROID_AUTO' | 'OBD2';
export type EventType = 'REFILL' | 'CNG_EMPTY' | 'FUEL_LOW';

export interface VehicleMeta {
  id: string;
  name: string;
  cngTankCapacityKg: number;
  petrolTankCapacityL: number;
  estimatedWarmupDistanceKmPerColdStart: number; // Defaults to ~1.2 km
  activeOdometerKm: number;
}

export interface FuelEvent {
  id: string;
  vehicleId: string;
  timestamp: number;
  odometerKm: number;
  source: EventSource;
  type: EventType;
  
  // Specific to REFILL
  fuelType?: FuelType;
  quantity?: number;         // kg for CNG, L for Petrol
  pricePerUnit?: number;     // ₹/kg or ₹/L
  totalCost?: number;        // ₹
  isFullTank?: boolean;      // Marks auto-cut tank top-off
  
  // Specific to Telemetry / Diagnostics
  fuelLevelPercent?: number; // Snapshot of petrol fuel gauge (0.0 to 100.0)
  coldStartsSinceLastRefill?: number;
  confirmedByUser: boolean;  // False if created via heuristic auto-detection
}

export interface DriveSegment {
  id: string;
  startOdometerKm: number;
  endOdometerKm: number;
  distanceKm: number;
  fuelType: FuelType;
  associatedRefillEventId?: string;
}
```

---

## 6. Calculation Engines & Algorithms

### Model A: Blended Running Cost (Financial Reality)
Calculates financial efficiency across an arbitrary temporal or distance window $[t_{\text{start}}, t_{\text{end}}]$:

$$\text{Blended Cost (₹/km)} = \frac{\sum_{i \in \text{Refills}} \text{totalCost}_i}{\text{Odometer}_{\text{latest}} - \text{Odometer}_{\text{base}}}$$

- **Edge Case:** Initial period before the first refill defaults to vehicle average or displays a pending status.
- **Partial Fills:** Blended running cost does not require full tank cut-offs; it operates as an ongoing cash-flow ledger against odometer progression.

### Model B: Pure CNG Tank-to-Tank (Full-to-Empty Cycle)
Isolates CNG thermodynamic efficiency by tracking boundary conditions:

```
[Full Refill Event (E₁)] ─────> [Driving Interval] ─────> [CNG_EMPTY Event (E₂)]
  Odo = O₁; isFull = true                                    Odo = O₂
```

1. Raw distance: $\Delta D_{\text{raw}} = O_2 - O_1$.
2. Petrol warmup correction:
   $$\Delta D_{\text{petrol\_warmup}} = N_{\text{cold\_starts}} \times D_{\text{warmup\_coeff}}$$
   *(where $D_{\text{warmup\_coeff}}$ defaults to $1.2\text{ km}$ unless calibrated).*
3. Net CNG Distance:
   $$\Delta D_{\text{cng}} = \max(0, \Delta D_{\text{raw}} - \Delta D_{\text{petrol\_warmup}})$$
4. Fuel consumed is measured upon the subsequent refill $E_3$ (where $E_3.\text{fuelType} = \text{'CNG'}$, refilled to auto-cut):
   $$\text{Mileage}_{\text{cng}} = \frac{\Delta D_{\text{cng}}}{E_3.\text{quantity}}\quad (\text{km/kg})$$

### Model C: Subtractive Residual Petrol Mileage
Tracks hidden petrol usage across a multi-fill observation window bounded by two `Full Tank` petrol events ($P_A$ and $P_B$):

1. Compute gross odometer delta: $\Delta D_{\text{total}} = O(P_B) - O(P_A)$.
2. Collect all validated pure CNG segments within this window:
   $$D_{\text{cng\_total}} = \sum_{k} \Delta D_{\text{cng}, k}$$
3. Isolate residual distance operated on petrol:
   $$D_{\text{petrol\_residual}} = \Delta D_{\text{total}} - D_{\text{cng\_total}}$$
4. Compute actual petrol efficiency using the volume required to return to full ($Q_{\text{petrol\_topup}} = P_B.\text{quantity}$):
   $$\text{Mileage}_{\text{petrol}} = \frac{D_{\text{petrol\_residual}}}{Q_{\text{petrol\_topup}}}\quad (\text{km/L})$$

---

## 7. Edge Cases & Fault Tolerance

| Scenario | System Handling |
| :--- | :--- |
| **Partial CNG Fill** | Marks current cycle as open/invalid for Model B. The fuel volume and expense are added strictly to Model A (Blended Cost). |
| **Unregistered CNG Empty Event** | If a CNG refill occurs without a preceding `CNG_EMPTY` event, the system checks whether the refill quantity was $\ge 90\%$ of `cngTankCapacityKg`. If true, it retroactively infers that CNG was completely exhausted at current odometer minus 2 km. |
| **Android Auto Disconnect** | Telemetry listener caches the last known odometer. On reconnection, if $\Delta \text{Odometer} > 0$, the app registers an untracked interval and prompts the user to verify fuel status. |
| **Simultaneous Fills** | When filling both CNG and Petrol at a combined station, events share the exact same odometer timestamp; distance attribution occurs across their respective cycle resolvers. |

---

## 8. Rollout & Validation Plan

1. **Phase 1 (Core PWA / Local App):** Implement local Dexie.js persistence, manual event logging forms, and Model A/B/C state parsers.
2. **Phase 2 (Android Auto Companion):** Deploy `androidx.car.app` skeleton supporting `MileageListener` and the 1-tap "CNG Empty" template action.
3. **Phase 3 (Sensor Calibration):** Conduct 1,000 km test drive comparing physical fuel bills and dashboard trip meters against Model B and C outputs to calibrate the cold-start deduction coefficient.