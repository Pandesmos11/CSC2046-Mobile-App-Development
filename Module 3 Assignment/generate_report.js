// generate_report.js
// Generates the Module 3 Tip Calculator documentation .docx
// Run: node generate_report.js
// Requires: npm install docx

const {
    Document, Packer, Paragraph, TextRun, HeadingLevel,
    AlignmentType, Table, TableRow, TableCell, WidthType,
    BorderStyle, ShadingType, UnderlineType
} = require("docx");
const fs = require("fs");

// ── Helpers ────────────────────────────────────────────────────────────────

/** Bold heading paragraph */
function heading(text, level = HeadingLevel.HEADING_1) {
    return new Paragraph({ text, heading: level, spacing: { before: 300, after: 100 } });
}

/** Normal body paragraph */
function body(text, options = {}) {
    return new Paragraph({
        children: [new TextRun({ text, size: 24, ...options })],
        spacing: { after: 160 }
    });
}

/** Bullet point */
function bullet(text) {
    return new Paragraph({
        children: [new TextRun({ text, size: 24 })],
        bullet: { level: 0 },
        spacing: { after: 80 }
    });
}

/** Code-style paragraph (monospace, shaded) */
function code(text) {
    return new Paragraph({
        children: [new TextRun({ text, font: "Courier New", size: 20 })],
        shading: { type: ShadingType.SOLID, color: "F0F0F0" },
        spacing: { after: 80 }
    });
}

/** Two-column table row */
function tableRow(label, value, isHeader = false) {
    const shading = isHeader
        ? { type: ShadingType.SOLID, color: "009688" }
        : {};
    const color   = isHeader ? "FFFFFF" : "000000";
    return new TableRow({
        children: [
            new TableCell({
                children: [new Paragraph({
                    children: [new TextRun({ text: label, bold: isHeader, color, size: 22 })]
                })],
                shading,
                width: { size: 35, type: WidthType.PERCENTAGE }
            }),
            new TableCell({
                children: [new Paragraph({
                    children: [new TextRun({ text: value, bold: false, color, size: 22 })]
                })],
                shading,
                width: { size: 65, type: WidthType.PERCENTAGE }
            })
        ]
    });
}

// ── Document ───────────────────────────────────────────────────────────────

const doc = new Document({
    styles: {
        default: {
            document: {
                run:       { font: "Calibri", size: 24 },
                paragraph: { spacing: { line: 276 } }
            }
        }
    },
    sections: [{
        children: [

            // ── Title block ───────────────────────────────────────────────
            new Paragraph({
                children: [new TextRun({
                    text: "CSC2046 – Mobile App Development",
                    bold: true, size: 36, color: "00796B"
                })],
                alignment: AlignmentType.CENTER,
                spacing:   { after: 80 }
            }),
            new Paragraph({
                children: [new TextRun({
                    text: "Module 3 Assignment: Tip Calculator",
                    bold: true, size: 30
                })],
                alignment: AlignmentType.CENTER,
                spacing:   { after: 80 }
            }),
            new Paragraph({
                children: [new TextRun({ text: "Code Documentation & Design Decisions", size: 24 })],
                alignment: AlignmentType.CENTER,
                spacing:   { after: 80 }
            }),
            new Paragraph({
                children: [new TextRun({ text: "Shane Potts  ·  S02399685", size: 22 })],
                alignment: AlignmentType.CENTER,
                spacing:   { after: 80 }
            }),
            new Paragraph({
                children: [new TextRun({ text: "Instructor: Mason George", size: 22 })],
                alignment: AlignmentType.CENTER,
                spacing:   { after: 80 }
            }),
            new Paragraph({
                children: [new TextRun({ text: "March 7, 2026", size: 22 })],
                alignment: AlignmentType.CENTER,
                spacing:   { after: 400 }
            }),

            // ── 1. Introduction ───────────────────────────────────────────
            heading("1. Introduction"),
            body(
                "This document explains the design decisions made while building the Tip Calculator " +
                "Android app for Module 3 of CSC2046. The app is written in Kotlin using the " +
                "traditional Android Views system (XML layouts + AppCompatActivity). It calculates " +
                "tip amounts, per-person splits, and grand totals in real time, and supports a " +
                "swipe-to-clear gesture."
            ),

            // ── 2. Architecture Decision ──────────────────────────────────
            heading("2. Architecture Decision: Views vs. Jetpack Compose"),
            body(
                "Android development currently supports two UI toolkits: the traditional XML Views " +
                "system and the newer Jetpack Compose declarative framework. This app uses Views for " +
                "the following reasons:"
            ),
            bullet("SeekBar and GestureDetector integrate naturally into Views-based activities without additional wrappers."),
            bullet("AppCompatActivity is straightforward to understand and document at the introductory level."),
            bullet("The Module 1 Hello World project was Compose-based; using Views here demonstrates familiarity with both approaches."),
            bullet("TextWatcher, SeekBar.OnSeekBarChangeListener, and GestureDetectorCompat are all well-documented, stable APIs."),

            // ── 3. Project Structure ──────────────────────────────────────
            heading("3. Project Structure"),
            new Table({
                rows: [
                    tableRow("File / Folder", "Purpose", true),
                    tableRow("MainActivity.kt",         "Single activity containing all app logic"),
                    tableRow("activity_main.xml",        "Full screen layout: two MaterialCardViews"),
                    tableRow("themes.xml",               "Material3 DayNight theme + custom button style"),
                    tableRow("colors.xml",               "Teal palette + surface/highlight colors"),
                    tableRow("strings.xml",              "All user-visible text (localisation-ready)"),
                    tableRow("libs.versions.toml",       "Version catalog: AGP 9.0, Kotlin 2.0.21, SDK 36"),
                    tableRow("app/build.gradle.kts",     "App-level gradle: deps, compile options, namespace"),
                    tableRow("gradle-wrapper.jar",       "Binary copied from Module 1 (same Gradle 9.1.0)"),
                ],
                width: { size: 100, type: WidthType.PERCENTAGE }
            }),
            new Paragraph({ spacing: { after: 200 } }),

            // ── 4. UI Design ──────────────────────────────────────────────
            heading("4. UI Design"),
            body(
                "The layout is a ScrollView containing a vertical LinearLayout with two " +
                "MaterialCardViews. This keeps the screen usable on small devices and when the " +
                "software keyboard appears (windowSoftInputMode=\"adjustResize\" in the manifest)."
            ),
            heading("4.1 Input Card", HeadingLevel.HEADING_2),
            bullet("Bill Amount — TextInputLayout (outlined box style) with a $ prefix. The numberDecimal input type restricts the keyboard to digits and a decimal point."),
            bullet("Tip SeekBar — Range 0–30%, default 15%. A live TextView label to the right of the 'Tip' heading always shows the exact current value."),
            bullet("Quick-tip buttons — Five tonal MaterialButtons (10%, 15%, 18%, 20%, 25%) snap the SeekBar to the selected preset instantly."),
            bullet("Split counter — Outlined minus and plus buttons flank a count TextView. The counter is floored at 1 to prevent division by zero."),
            heading("4.2 Results Card", HeadingLevel.HEADING_2),
            bullet("Three output rows: Tip Amount, Per Person, Total."),
            bullet("A teal divider separates the first two rows from the highlighted Total row."),
            bullet("The Total row uses a light teal background and larger text for quick visual scanning."),
            bullet("A small 'swipe to clear' hint sits at the bottom of the card."),

            // ── 5. Input Validation ───────────────────────────────────────
            heading("5. Input Validation"),
            body(
                "Validation happens inside the calculate() function, which is called by every " +
                "listener. This approach centralises the validation logic in one place:"
            ),
            code("val bill = billText.toDoubleOrNull()"),
            body(
                "toDoubleOrNull() returns null for any non-numeric string instead of throwing an " +
                "exception. A subsequent null-or-negative check catches both cases and sets all " +
                "output fields to 'ERR'. An empty field shows '—' (dashes) rather than an error, " +
                "since an empty field is a normal starting state, not a mistake."
            ),

            // ── 6. Dynamic UI Updates ─────────────────────────────────────
            heading("6. Dynamic UI Updates"),
            body("Two mechanisms drive live recalculation:"),
            heading("6.1 TextWatcher on the Bill EditText", HeadingLevel.HEADING_2),
            body(
                "afterTextChanged() fires after each character is typed or deleted. Only this " +
                "callback is used; beforeTextChanged and onTextChanged are left empty because " +
                "the final updated string is only available in afterTextChanged."
            ),
            heading("6.2 SeekBar.OnSeekBarChangeListener", HeadingLevel.HEADING_2),
            body(
                "onProgressChanged() fires on every pixel of thumb movement. Setting " +
                "seekBar.progress from a quick-tip button click also triggers this callback, " +
                "so the quick-tip buttons do not need their own calculation calls — they only " +
                "need to set the SeekBar value."
            ),

            // ── 7. Gesture-Based Interaction ──────────────────────────────
            heading("7. Gesture-Based Interaction: Swipe to Clear"),
            body(
                "A GestureDetectorCompat is created in setupGestureDetector() using an anonymous " +
                "SimpleOnGestureListener. SimpleOnGestureListener is preferred over the full " +
                "GestureDetector.OnGestureListener interface because it provides default no-op " +
                "implementations for all callbacks — only onDown() and onFling() need to be " +
                "overridden."
            ),
            body("onDown() returns true, which is required for the framework to deliver subsequent events in the same gesture sequence."),
            body("onFling() compares the horizontal displacement and velocity against thresholds (100 px / 100 px·s⁻¹). The swipe is also required to be more horizontal than vertical so an accidental vertical scroll does not trigger a clear."),
            body("onTouchEvent() is overridden in MainActivity to route every touch through the detector."),
            code("override fun onTouchEvent(event: MotionEvent): Boolean {"),
            code("    gestureDetector.onTouchEvent(event)"),
            code("    return super.onTouchEvent(event)"),
            code("}"),
            body("A short Toast ('Cleared') confirms the swipe to the user."),

            // ── 8. Dependency Versions ────────────────────────────────────
            heading("8. Dependency Versions"),
            body(
                "All versions were matched to the Module 1 Hello World project to guarantee " +
                "compatibility with the Android SDK and build tools already installed on the " +
                "development machine."
            ),
            new Table({
                rows: [
                    tableRow("Dependency",           "Version",   true),
                    tableRow("Android Gradle Plugin", "9.0.0"),
                    tableRow("Kotlin",                "2.0.21"),
                    tableRow("Gradle Wrapper",        "9.1.0"),
                    tableRow("compileSdk / targetSdk","36"),
                    tableRow("minSdk",                "24  (Android 7.0 Nougat)"),
                    tableRow("AppCompat",             "1.7.0"),
                    tableRow("Material Components",   "1.12.0"),
                    tableRow("ConstraintLayout",      "2.2.0"),
                ],
                width: { size: 100, type: WidthType.PERCENTAGE }
            }),
            new Paragraph({ spacing: { after: 200 } }),

            // ── 9. References ─────────────────────────────────────────────
            heading("9. References"),
            bullet("Android Developers — GestureDetector: https://developer.android.com/training/gestures/detector"),
            bullet("Android Developers — TextWatcher: https://developer.android.com/reference/android/text/TextWatcher"),
            bullet("Material Design Components — Android: https://m3.material.io/develop/android"),
            bullet("Android Developers — SeekBar: https://developer.android.com/reference/android/widget/SeekBar"),

            // ── Footer ────────────────────────────────────────────────────
            new Paragraph({
                children: [new TextRun({ text: "Tip Calculator App  ·  CSC2046  ·  Module 3  ·  Spring 2026", size: 18, color: "757575" })],
                alignment: AlignmentType.CENTER,
                spacing:   { before: 600 }
            })
        ]
    }]
});

// ── Write file ─────────────────────────────────────────────────────────────
Packer.toBuffer(doc).then((buffer) => {
    fs.writeFileSync("CSC2046_Module3_TipCalculator_Report_Potts.docx", buffer);
    console.log("Report written: CSC2046_Module3_TipCalculator_Report_Potts.docx");
});
