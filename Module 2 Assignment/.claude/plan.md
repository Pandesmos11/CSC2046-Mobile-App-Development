# Tip Calculator — Module 3 Implementation Plan

## Project location
`/Users/shanepotts/Desktop/FRCC/Mobile App Development/Module 3 Assignment/TipCalculator/`

## Stack
- Native Android, Kotlin, Views-based XML (NOT Compose — gestures/SeekBar are far more natural in Views)
- Versions match Module 1 exactly: AGP 9.0.0 · Kotlin 2.0.21 · Gradle 9.1.0 · SDK 36 · minSdk 24

## File tree
```
TipCalculator/
├── gradlew / gradlew.bat          ← copied from Module 1 (binary-safe)
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar    ← copied from Module 1 (binary)
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml        ← Views deps only (no Compose)
├── build.gradle.kts              ← project-level
├── settings.gradle.kts
├── app/
│   ├── build.gradle.kts          ← app-level
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/frcc/tipcalculator/
│       │   └── MainActivity.kt
│       └── res/
│           ├── layout/activity_main.xml
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               └── themes.xml
└── ../generate_report.js         ← Node.js docx report (Module 3 Assignment folder)
```

## UI (activity_main.xml)
- ScrollView → vertical LinearLayout
- **Input card** (MaterialCardView):
  - Bill Amount — TextInputLayout with `$` prefix, `numberDecimal` keyboard
  - Tip % row — SeekBar (0–30%, default 15%) + live label ("15%")
  - Quick-tip row — 5 tonal buttons: 10 / 15 / 18 / 20 / 25%
  - Split row — `−` button · count label · `+` button
- **Results card** (MaterialCardView):
  - Tip Amount / Per Person / Total (label + value pairs)
  - "← Swipe to clear →" hint at bottom

## MainActivity.kt
| Feature | Mechanism |
|---|---|
| Live calculation | `TextWatcher` on bill EditText + `SeekBarChangeListener` |
| Quick-tip buttons | `setOnClickListener` → set `seekBar.progress` → recalculate |
| Split counter | `−`/`+` buttons, floor at 1 |
| Input validation | `toDoubleOrNull()` + negative check; shows `—` or error on bad input |
| Swipe to clear | `GestureDetectorCompat` → `onFling()`, horizontal threshold 100px / 100px·s⁻¹ |

## Documentation (.docx)
- `generate_report.js` in Module 3 Assignment folder, using `docx` npm package
- Covers: architecture choice, UI decisions, validation approach, gesture implementation

## Steps
1. Create Module 3 Assignment folder structure
2. Copy binary gradle files from Module 1
3. Write all source files (Kotlin + XML + gradle)
4. Open in Android Studio → sync → build
5. Write and run generate_report.js → produce .docx
