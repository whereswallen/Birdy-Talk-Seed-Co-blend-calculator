# Birdy Talk Blend Manager

A premium, production-ready Android app for **Birdy Talk Seed Co.** that scales bird-seed blend
recipes on the production floor using exact `BigDecimal` math — no floating-point drift on real
weights — and lets you manage your own recipes, costs, inventory, and history.

Built with **100% Kotlin**, **Jetpack Compose + Material 3**, and a **Clean Architecture / MVVM**
structure with state produced through Kotlin `StateFlow`s.

---

## Features

### Editable recipes, costs & margins
- **Full recipe management** in Settings → Blends: edit percentages, ingredient names, and
  per-lb costs; add/remove ingredients; create, duplicate, and delete blends; reset to defaults.
- **Hard 100% rule**: a blend can only be saved when its percentages total exactly 100% —
  enforced by the editor's Save gate and again by the domain model, so nothing invalid persists.
- **Costing**: per-ingredient and total batch cost, blended cost-per-pound, and — with a retail
  price — revenue, profit, and margin %.

### Production tools
- **Inventory / stock check**: track on-hand pounds per ingredient; batches that need more than
  you have flag a shortfall.
- **Batch history**: log any calculation and revisit or re-run it later.
- **Share**: export a plain-text batch sheet (weights + costs) via the Android share sheet.
- **Backup & restore**: export/import all recipes, costs, inventory, and history as one JSON file.

### Two calculation modes

| Tab | Purpose |
| --- | --- |
| **Batch Scaling** | Pick a blend and a target batch size; get the exact weight of every ingredient, a composition donut + ratio bar, and a total. |
| **Anchor Dump** | Dumped a bin of one ingredient? Enter its weight and the app back-solves the whole batch (`factor = weight ÷ percent`), shows every other ingredient, the total batch weight, and a soft-amber warning if it overflows your configured max bin capacity. |

### Global unit toggle
A persistent header switches **all** app-wide weights between:
- **Decimal pounds** — `14.33 lb`
- **Pounds & ounces** — `14 lb 5.3 oz`
- **Kilograms** — `6.50 kg`

All conversions use exact `BigDecimal` arithmetic (exact `0.45359237` lb→kg factor). The chosen
unit is persisted across launches via DataStore, and existing input values are re-expressed into
the new unit when you switch so the physical weight stays constant.

### Premium aesthetic
- **Playfair Display** (serif) for headers and blend titles; **JetBrains Mono** for every number
  so counters never jitter as they animate. Both fonts are bundled offline (OFL licensed).
- **Earthy-luxury palette**: deep forest-green containers, warm cream surfaces, muted gold
  accents, charcoal text — in both light and dark themes.
- **Micro-interactions**: `animateContentSize()` and `AnimatedVisibility` for list/warning
  transitions, spring-physics `animateFloatAsState` weight roll-ups, and subtle haptics when the
  anchor ingredient changes or a batch crosses the bin-capacity threshold.
- **Adaptive layout**: a `WindowSizeClass`-driven two-pane layout on expanded/tablet widths,
  stacked on phones.

---

## The six pre-loaded blends

| Blend | Composition |
| --- | --- |
| **Cardinal Rule** | 46% Safflower · 30% Black Oil Sunflower · 10% Striped Sunflower · 10% Peanut Pickouts · 4% Dried Mealworms |
| **Chickadee Chow** | 50% Black Oil Sunflower · 30% Safflower · 20% Sunflower Chips (coarse) |
| **Yard Party Mix** | 54.5% Black Oil Sunflower · 16.84% White Millet · 14.33% Striped Sunflower · 14.33% Peanut Pickouts |
| **Blue Jay Buffet** | 50% Peanut Pieces · 30% Black Oil Sunflower · 20% Striped Sunflower |
| **Neat Eats** | 50% Sunflower Chips (coarse) · 50% Peanut Pieces |
| **Finch Frenzy** | 90% Nyjer Seed · 10% Sunflower Chips (medium) |

---

## Architecture

A Gradle multi-module build keeps the production-critical math independent of Android:

```
:core-domain   Pure Kotlin/JVM — models, DefaultBlends, BlendCalculator, CostCalculator,
               StockCheck, UnitConverter, serialization. Fully unit-tested, no Android dependency.
:app           Android application — Compose UI, Material 3 theme, AppViewModel, navigation,
               DataStore/JSON persistence (blends, inventory, history, unit preference).
```

```
core-domain/
  model/       Ingredient (cost), Blend (retail price; validates 100%), EditableBlend (draft)
  data/        DefaultBlends (the six seed recipes)
  calc/        BlendCalculator, CostCalculator, IngredientWeight, BatchResult, AnchorResult
  inventory/   StockCheck (shortfalls)
  units/       UnitSystem, UnitConverter, FormattedWeight
  persistence/ BatchHistoryEntry, InventoryItem, AppBackup; serialization/ BigDecimalSerializer
app/ .../
  data/        BlendRepository, InventoryRepository, HistoryRepository, BackupRepository (DataStore)
  ui/theme/    Color, Type (bundled variable fonts), Theme (SeedCoTheme)
  ui/components/ UnitToggleHeader, LabeledDropdown, NumericField, IngredientResultCard,
                 CostSummaryCard, HeadlineTotalCard, CompositionChart, WarningBanner, …
  ui/calculator/ CalculatorScreen (header + tabs); ui/batch, ui/anchor (the two modes)
  ui/settings/ SettingsScreen, BlendListScreen, BlendEditorScreen, BackupScreen
  ui/inventory/ InventoryScreen;  ui/history/ HistoryScreen
  ui/AppViewModel — single source of truth (StateFlow); ui/AppRoot — NavHost
```

The canonical internal unit everywhere is **pounds** (`BigDecimal`); conversion to the selected
display unit happens only at the UI edge.

---

## Building

Open the project in **Android Studio** (Koala or newer) and run the `app` configuration, or from
the command line:

```bash
./gradlew :app:assembleDebug        # build the debug APK
./gradlew :core-domain:test         # run the domain unit tests
```

Requirements: JDK 17, Android SDK with **compileSdk 35** (minSdk 26). The Android Gradle Plugin
and AndroidX artifacts are resolved from Google's Maven repository, so an internet connection is
needed on first build.

### Verified in this environment
The `:core-domain` layer (`BlendCalculator`, `CostCalculator`, `StockCheck`, `UnitConverter`,
serialization, and `EditableBlend` validation) is covered by a JUnit suite that runs on the plain
JVM — 33 tests, all green — validating the anchor/batch scaling examples, the three unit
conversions (including the ounce carry-over), the cost/margin math, JSON round-trips, stock
shortfalls, and the exact-100% rule. The Compose `:app` module builds via GitHub Actions
(`.github/workflows/android.yml`), which uploads the debug APK as an artifact.

---

## Fonts & licensing
Playfair Display and JetBrains Mono are bundled under the SIL Open Font License 1.1. Their full
license texts are included at the repository root:
- `LICENSES-fonts-PlayfairDisplay.txt`
- `LICENSES-fonts-JetBrainsMono.txt`
