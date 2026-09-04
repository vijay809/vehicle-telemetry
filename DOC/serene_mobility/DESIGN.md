---
name: Serene Mobility
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#3c4a42'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#6c7a71'
  outline-variant: '#bbcabf'
  surface-tint: '#006c49'
  primary: '#006c49'
  on-primary: '#ffffff'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#4edea3'
  secondary: '#855300'
  on-secondary: '#ffffff'
  secondary-container: '#fea619'
  on-secondary-container: '#684000'
  tertiary: '#6d3bd7'
  on-tertiary: '#ffffff'
  tertiary-container: '#b090ff'
  on-tertiary-container: '#4600a7'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#ffddb8'
  secondary-fixed-dim: '#ffb95f'
  on-secondary-fixed: '#2a1700'
  on-secondary-fixed-variant: '#653e00'
  tertiary-fixed: '#e9ddff'
  tertiary-fixed-dim: '#d0bcff'
  on-tertiary-fixed: '#23005c'
  on-tertiary-fixed-variant: '#5516be'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
  cng-pastel-bg: '#ecfdf5'
  cng-pastel-border: '#a7f3d0'
  cng-accent: '#059669'
  cng-badge: '#d1fae5'
  petrol-pastel-bg: '#fffbeb'
  petrol-pastel-border: '#fde68a'
  petrol-accent: '#d97706'
  petrol-badge: '#fef3c7'
  canvas-lavender: '#f8fafc'
  canvas-tint: '#f1f5f9'
  surface-white: '#ffffff'
  slate-soft: '#e2e8f0'
  slate-text-main: '#1e293b'
  slate-text-muted: '#64748b'
  slate-text-faint: '#94a3b8'
  alert-pastel-bg: '#fef2f2'
  alert-pastel-border: '#fecaca'
  alert-accent: '#e11d48'
typography:
  display-odometer:
    fontFamily: Outfit
    fontSize: 44px
    fontWeight: '600'
    lineHeight: 52px
    letterSpacing: -0.03em
  display-odometer-mobile:
    fontFamily: Outfit
    fontSize: 34px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Outfit
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Outfit
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Outfit
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
  telemetry-val-lg:
    fontFamily: Outfit
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  telemetry-val-md:
    fontFamily: Outfit
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 26px
  body-lg:
    fontFamily: Nunito Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Nunito Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
  body-sm:
    fontFamily: Nunito Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-caps:
    fontFamily: Outfit
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.06em
  label-tag:
    fontFamily: Nunito Sans
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  space-2xs: 0.25rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
  space-2xl: 3rem
  card-padding: 1.5rem
  card-gap: 1rem
  screen-edge-padding: 1.25rem
  dock-safe-bottom: 6rem
---

## Brand & Style

This design system reimagines vehicle telemetry and dual-fuel management through an airy, soothing, and radically decluttered lens. Shifting away from aggressive cockpit darkness, it embraces modern soft minimalism infused with approachable, human-centric wellness aesthetics. It targets eco-conscious drivers, daily commuters, and family car owners who seek calm reassurance, instant readability, and effortless tracking without visual stress or sensory overload.

The visual direction centers on **Airy Pastel Soft-Minimalism**:
- **Breathe & Decompress**: Expansive whitespace, feather-light backgrounds infused with faint slate and lavender undertones, and generous inner padding that naturally separates critical metrics.
- **Friendly Approachability**: Soft, rounded typographic forms and pillowy containers that transform complex mechanical telemetry into intuitive, friendly insights.
- **Calm Fuel Semantics**: The high-anxiety neon greens and abrasive ambers are replaced with restorative mint pastels for CNG and warm peach/apricot hues for Petrol.
- **Delicate Boundaries**: Visual distinction is achieved through ultra-delicate tinted pastel borders and smooth ambient gradients rather than harsh divide lines or dense black drop shadows.

## Colors

The palette establishes a serene, light-bathed environment grounded in gentle slate-lavender neutrals and balanced with soft, functional pastels for instant fuel categorization.

### Semantic Fuel Colors
- **CNG (Mint Pastoral)**: Dedicated to compressed natural gas telemetry, efficiency ratings, eco scores, and green mileage.
  - Surface Fill: `cng-pastel-bg` (`#ECFDF5`)
  - Delicate Border: `cng-pastel-border` (`#A7F3D0`)
  - Pill/Chip Background: `cng-badge` (`#D1FAE5`)
  - Primary Accent & Typography: `cng-accent` (`#059669`)
- **Petrol (Warm Peach / Apricot)**: Dedicated to petrol reserves, fuel station fills, secondary combustion ranges, and hybrid transitions.
  - Surface Fill: `petrol-pastel-bg` (`#FFFBEB`)
  - Delicate Border: `petrol-pastel-border` (`#FDE68A`)
  - Pill/Chip Background: `petrol-badge` (`#FEF3C7`)
  - Primary Accent & Typography: `petrol-accent` (`#D97706`)
- **System Alert (Soft Rose)**: Designated for low pressure drops, critical leaks, and scheduled service notices without inducing driver panic.
  - Surface Fill: `alert-pastel-bg` (`#FEF2F2`)
  - Border: `alert-pastel-border` (`#FECACA`)
  - Accent: `alert-accent` (`#E11D48`)

### Canvas & Neutral Hierarchy
- `canvas-lavender` (`#F8FAFC`): The primary backdrop, kissed with a hint of lavender-slate to reduce white glare.
- `surface-white` (`#FFFFFF`): Pure crisp cards and floating docks providing clean contrast over the tinted canvas.
- `slate-text-main` (`#1E293B`): Deep slate navy for headings and primary metrics, providing high clarity without the harshness of pure black.
- `slate-text-muted` (`#64748B`): Neutral secondary text for descriptions, units, and timestamps.
- `slate-text-faint` (`#94A3B8`): Subtle tertiary indicators, inactive tab labels, and auxiliary metadata.

## Typography

The typographic strategy unifies geometric optimism with soft warmth:

1. **Outfit (Headings, Telemetry, and Display Readouts)**: Features geometric open apertures with softened terminals. It gives telemetry numbers a fresh, legible character without the sterile, aggressive feeling of conventional digital dashboards.
2. **Nunito Sans (Body, Descriptions, and Tags)**: Naturally friendly and rounded, Nunito Sans provides smooth continuous reading across logs, trip metrics, and settings. Its rounded proportions harmonize with the curved card structures.
3. **Telemetry Numbers**: Large numeric readouts utilize medium-to-semibold weights (`600`) rather than harsh heavy black weights, retaining legibility while keeping visual weight pleasantly light.
4. **Micro-Labels**: Micro-headers use slightly tracked uppercase Outfit (`letter-spacing: 0.06em`) to preserve glanceability while maintaining an airy, uncluttered rhythm.

## Layout & Spacing

The layout is built upon an **Airy Fluid Column Model**, prioritizing breathing room and preventing sensory overload:

- **Centering & Maximum Reach**: On mobile devices, views stretch full-width with a generous `1.25rem` (`screen-edge-padding`). On desktop and tablet viewports, the application conforms to a centered `560px` canvas, preserving a focused, tranquil feed.
- **Card Breathing Room**: Standard cards use a generous `1.5rem` (`card-padding`) internal cushion. Dense clustering is replaced by deliberate spatial separation (`card-gap: 1rem`), allowing each data group to stand on its own.
- **Generous Vertical Stacking**: Sections are decoupled by large intervals (`space-xl` / `2rem`), immediately relieving visual density and guiding the eye gently down the screen.
- **Fixed Dock Clearance**: The floating navigation dock maintains an elevated, pillowy appearance with an enforced bottom page margin of `dock-safe-bottom` (`6rem`) plus device safe insets.

## Elevation & Depth

Rather than using intense drop shadows or solid dark borders, this design system establishes depth through **Whisper-Soft Ambient Glows and Tinted Translucency**:

- **Card Surfaces**: Pure white (`#FFFFFF`) or pastel-tinted cards sit gently on top of the slate-lavender backdrop. They rely on soft, multi-stop diffuse shadows:
  - Ambient Shadow: `0 8px 24px -4px rgba(100, 116, 139, 0.06), 0 2px 6px -1px rgba(100, 116, 139, 0.04)`.
- **Delicate Pastel Outlines**: Cards are framed by 1px to 1.5px hairline outlines tinted in the card’s accent family (e.g., `#A7F3D0` for CNG, `#FDE68A` for Petrol, and `#E2E8F0` for neutral cards). This anchors the shape without creating heavy visual fences.
- **Floating Island Dock**: Floating navigation elements utilize a semi-transparent white wash (`rgba(255, 255, 255, 0.85)`) coupled with a generous `16px` backdrop blur and a pillowy, upward-diffused shadow (`0 -10px 30px -5px rgba(100, 116, 139, 0.08)`).
- **Interactive Depth**: Tapping or hovering an element transitions the card smoothly with a gentle lift (`transform: translateY(-2px)`) and a subtle increase in ambient glow, avoiding harsh active states.

## Shapes

The design system embraces an ultra-soft, friendly geometry (`roundedness: 3`) to deliver a peaceful, welcoming tactile experience:

- **Primary Cards & Containers**: Defined with `rounded-2xl` (`1.25rem` / `20px`) to `rounded-3xl` (`1.75rem` / `28px`) corners. This eliminates harsh corners, creating a continuous organic flow.
- **Buttons & Interactive Controls**: Formed as smooth, comfortable pills (`9999px`) or hyper-rounded blocks (`1rem` / `16px`), making them feel soft and inviting under thumb interaction.
- **Chips, Badges, and Status Tags**: Fully pill-shaped (`rounded-full`), gently holding pastel indicators, units, and mode states.
- **Progress Track Wells**: Track bars and comparison meters feature full semi-circular ends (`rounded-full`) with recessed pastel tracks to ensure fluid transitions.

## Components

### 1. Pastel Dual-Fuel Cards
- **Structure**: Generous cards wrapped in `rounded-3xl` with a subtle 1px border.
- **CNG Card**:
  - Background: `cng-pastel-bg` (`#ECFDF5`) or clean white with a mint gradient wash.
  - Border: `cng-pastel-border` (`#A7F3D0`).
  - Readouts: Large numeric values in `slate-text-main` (`#1E293B`) paired with a pill badge in `cng-badge` (`#D1FAE5`) bearing `cng-accent` text (`#059669`).
  - Meter Bar: Smooth mint fill over a faint mint background track.
- **Petrol Card**:
  - Background: `petrol-pastel-bg` (`#FFFBEB`) or clean white with an apricot gradient wash.
  - Border: `petrol-pastel-border` (`#FDE68A`).
  - Readouts: Large numeric values in `slate-text-main` with a pill badge in `petrol-badge` (`#FEF3C7`) bearing `petrol-accent` text (`#D97706`).
  - Meter Bar: Warm amber fill over an apricot track.

### 2. Glanceable Metric Pillows
- **Layout**: Clean white surface with `rounded-2xl`, cushioned with `card-padding`.
- **Top Row**: Delicate uppercase micro-label in `slate-text-muted` paired with an icon housed in a pastel circle.
- **Core Value**: Prominent Outfit display number, followed by a light text unit (`kg`, `L`, `km/kg`).
- **Footer**: Subtle change tag (e.g. "+3.2 km/kg") displayed inside an ultra-soft pill badge.

### 3. Floating Pill Dock (Quick Actions)
- **Layout**: Pill-shaped bar (`rounded-full`) suspended above the bottom screen edge with generous side margins.
- **Backing**: White frosted glass (`rgba(255, 255, 255, 0.88)` with `backdrop-filter: blur(16px)`) and a delicate slate border (`#E2E8F0`).
- **Action Triggers**:
  - Main Log Action: Centered pill button in gentle mint or peach with white text and a soft tinted shadow.
  - Navigation Flanks: Generous icon triggers with rounded active states in matching pastels.

### 4. Soft Segmented Switches
- **Track**: Pill-shaped container filled with `canvas-tint` (`#F1F5F9`) with 4px internal padding.
- **Thumb**: Pure white pill (`#FFFFFF`) with a diffuse float shadow (`0 2px 8px rgba(100, 116, 139, 0.12)`).
- **Typography**: Active text transitions to `slate-text-main` with a bold weight; inactive text stays in `slate-text-muted`.

### 5. Pill Badges & Chips
- Fully rounded pills with `0.375rem 0.875rem` padding.
- Pale pastel tint for the background, accompanied by dark tone-matched text for contrast and accessibility (minimum 4.5:1 ratio).
- No hard borders; separation comes cleanly from the pastel contrast.

### 6. Input Fields & Steppers
- **Inputs**: Generous height (48px to 52px), `rounded-2xl`, background in pure white with a `1px` soft slate border (`#E2E8F0`). Focus state transitions the border smoothly to mint (`#A7F3D0`) with a gentle outer glow.
- **Steppers**: Soft pastel circles (+ / -) that scale down slightly on press (`scale: 0.96`), offering clean, friendly tactile response for logging fuel quantities.