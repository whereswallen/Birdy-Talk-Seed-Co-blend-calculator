# Seed Co. Production Calculator

A premium, production-ready Android app for **Birdy Talk Seed Co.** that scales bird-seed blend
recipes on the production floor using exact `BigDecimal` math — no floating-point drift on real
weights.

Built with **100% Kotlin**, **Jetpack Compose + Material 3**, and a **Clean Architecture / MVVM**
structure with state produced through Kotlin `StateFlow`s.

---

## Features

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
:core-domain   Pure Kotlin/JVM — models, RecipeRepository, BlendCalculator (BigDecimal),
               UnitConverter. Fully unit-tested, no Android dependency.
:app           Android application — Compose UI, Material 3 theme, CalculatorViewModel,
               DataStore unit persistence.
```

```
core-domain/
  model/       Ingredient, Blend (validates percentages total 100)
  data/        RecipeRepository (the six immutable blends)
  calc/        BlendCalculator, IngredientWeight, BatchResult, AnchorResult
  units/       UnitSystem, UnitConverter, FormattedWeight
app/ .../ui/
  theme/       Color, Type (bundled variable fonts), Theme (SeedCoTheme)
  components/  UnitToggleHeader, LabeledDropdown, NumericField, IngredientResultCard,
               HeadlineTotalCard, CompositionChart (donut/ratio bar/legend), WarningBanner,
               AnimatedWeightText
  batch/       BatchScreen (Tab 1)
  anchor/      AnchorScreen (Tab 2)
  CalculatorViewModel — single source of truth (StateFlow<CalculatorUiState>)
  AppRoot — persistent header, unit toggle, tab navigation, adaptive layout
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
The `:core-domain` math (`BlendCalculator`, `UnitConverter`, `RecipeRepository`) is covered by a
JUnit suite that runs on the plain JVM — 17 tests, all green — validating the anchor/batch
scaling examples, the three unit conversions (including the ounce carry-over), and that every
recipe totals exactly 100%. The Compose `:app` module compiles against the standard AndroidX
toolchain in Android Studio / CI.

---

## Fonts & licensing
Playfair Display and JetBrains Mono are bundled under the SIL Open Font License 1.1. Their full
license texts are included at the repository root:
- `LICENSES-fonts-PlayfairDisplay.txt`
- `LICENSES-fonts-JetBrainsMono.txt`
