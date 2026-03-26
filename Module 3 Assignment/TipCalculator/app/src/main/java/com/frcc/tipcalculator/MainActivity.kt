// ============================================================
// Name:        Shane Potts
// Date:        03/07/2026
// Course:      CSC2046 — Mobile App Development
// Module:      3 — Tip Calculator
// Description: Native Android tip calculator. The user enters a
//              bill amount, selects a tip percentage (via SeekBar
//              or quick-preset buttons), and optionally splits the
//              bill across multiple people.  All output fields
//              update in real time.  A horizontal swipe anywhere
//              on screen resets the calculator to its defaults.
// Inputs:      Bill amount (EditText), tip % (SeekBar / buttons),
//              split count (± buttons)
// Outputs:     Tip amount, per-person amount, grand total
// ============================================================

package com.frcc.tipcalculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    // ── Views ──────────────────────────────────────────────────
    private lateinit var toolbar:      MaterialToolbar
    private lateinit var etBillAmount: TextInputEditText
    private lateinit var seekBarTip:   SeekBar
    private lateinit var tvTipPercent: TextView
    private lateinit var tvSplitCount: TextView
    private lateinit var tvTipAmount:  TextView
    private lateinit var tvPerPerson:  TextView
    private lateinit var tvTotal:      TextView

    // Quick-tip preset buttons
    private lateinit var btn10: MaterialButton
    private lateinit var btn15: MaterialButton
    private lateinit var btn18: MaterialButton
    private lateinit var btn20: MaterialButton
    private lateinit var btn25: MaterialButton

    // Split counter buttons
    private lateinit var btnMinus: MaterialButton
    private lateinit var btnPlus:  MaterialButton

    // ── State ──────────────────────────────────────────────────
    // Tracked as a property so any listener can read/write
    // without touching the UI before it's needed.
    private var splitCount = 1

    // ── Gesture detector ───────────────────────────────────────
    // GestureDetectorCompat is the AndroidX-safe wrapper
    // around GestureDetector so it works on all API levels.
    private lateinit var gestureDetector: GestureDetectorCompat

    // Swipe thresholds (pixels / pixels-per-second)
    // 100 px distance and 100 px/s velocity are intentionally low
    // so a casual swipe always triggers a clear without needing to flick hard.
    // Raise these values if accidental clears occur during testing.
    private val swipeMinDistance = 100
    private val swipeMinVelocity = 100

    // ==========================================================
    // onCreate — wires everything together
    // ==========================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        // setSupportActionBar lets the toolbar handle the options menu
        // lifecycle (onCreateOptionsMenu / onOptionsItemSelected).
        setSupportActionBar(toolbar)
        setupSeekBar()
        setupQuickTipButtons()
        setupSplitButtons()
        setupBillTextWatcher()
        setupGestureDetector()

        // Show initial "—" state immediately
        calculate()
    }

    // ==========================================================
    // bindViews — find every widget by ID once and store it
    // Calling findViewById repeatedly in listeners is wasteful;
    // storing references in lateinit vars is the idiomatic approach.
    // ==========================================================
    private fun bindViews() {
        toolbar      = findViewById(R.id.toolbar)
        etBillAmount = findViewById(R.id.etBillAmount)
        seekBarTip   = findViewById(R.id.seekBarTip)
        tvTipPercent = findViewById(R.id.tvTipPercent)
        tvSplitCount = findViewById(R.id.tvSplitCount)
        tvTipAmount  = findViewById(R.id.tvTipAmount)
        tvPerPerson  = findViewById(R.id.tvPerPerson)
        tvTotal      = findViewById(R.id.tvTotal)
        btn10        = findViewById(R.id.btn10)
        btn15        = findViewById(R.id.btn15)
        btn18        = findViewById(R.id.btn18)
        btn20        = findViewById(R.id.btn20)
        btn25        = findViewById(R.id.btn25)
        btnMinus     = findViewById(R.id.btnMinus)
        btnPlus      = findViewById(R.id.btnPlus)
    }

    // ==========================================================
    // setupSeekBar — live tip-% updates as the thumb moves
    // ==========================================================
    private fun setupSeekBar() {
        seekBarTip.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                // Update the label on every tick so the user always sees
                // the current value without waiting for touch-up.
                tvTipPercent.text = "$progress%"
                calculate()
            }

            // Required by the interface but not needed here
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar)  {}
        })
    }

    // ==========================================================
    // setupQuickTipButtons — tapping a preset snaps the SeekBar
    //   and recalculates instantly
    // ==========================================================
    private fun setupQuickTipButtons() {
        // Mapping each button to its integer value lets us use a
        // single helper instead of five identical click handlers.
        val presets = mapOf(btn10 to 10, btn15 to 15, btn18 to 18,
                            btn20 to 20, btn25 to 25)

        presets.forEach { (button, percent) ->
            button.setOnClickListener {
                seekBarTip.progress = percent   // triggers onProgressChanged → calculate()
            }
        }
    }

    // ==========================================================
    // setupSplitButtons — increment / decrement the people counter
    // ==========================================================
    private fun setupSplitButtons() {

        btnMinus.setOnClickListener {
            // Floor at 1 — splitting between zero people is undefined.
            if (splitCount > 1) {
                splitCount--
                tvSplitCount.text = splitCount.toString()
                calculate()
            }
        }

        btnPlus.setOnClickListener {
            splitCount++
            tvSplitCount.text = splitCount.toString()
            calculate()
        }
    }

    // ==========================================================
    // setupBillTextWatcher — recalculate on every keystroke
    // ==========================================================
    private fun setupBillTextWatcher() {
        etBillAmount.addTextChangedListener(object : TextWatcher {

            // afterTextChanged fires once the Editable is fully updated,
            // so we always read the final value for that keystroke.
            override fun afterTextChanged(s: Editable?) { calculate() }

            // Required by the interface but not needed here
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)    {}
        })
    }

    // ==========================================================
    // setupGestureDetector — swipe left OR right anywhere → clear
    // ==========================================================
    private fun setupGestureDetector() {
        gestureDetector = GestureDetectorCompat(this,
            object : GestureDetector.SimpleOnGestureListener() {

                // onDown must return true so the framework keeps
                // delivering subsequent events in the same gesture.
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    val dx = e2.x - (e1?.x ?: 0f)
                    val dy = e2.y - (e1?.y ?: 0f)

                    // Treat it as a horizontal swipe when the horizontal
                    // displacement and velocity both exceed their thresholds
                    // AND the gesture is more horizontal than vertical.
                    val isHorizontal = abs(dx) > abs(dy)
                    val longEnough   = abs(dx) > swipeMinDistance
                    val fastEnough   = abs(velocityX) > swipeMinVelocity

                    if (isHorizontal && longEnough && fastEnough) {
                        clearAll()
                        return true
                    }
                    return false
                }
            }
        )
    }

    // ==========================================================
    // onCreateOptionsMenu — inflate the three-line icon into the toolbar
    // ==========================================================
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // ==========================================================
    // onOptionsItemSelected — handle taps on the three-line icon
    // A PopupMenu anchored to the icon gives a familiar Android
    // dropdown without needing a full navigation drawer.
    // ==========================================================
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> {
                val anchor = findViewById<MaterialToolbar>(R.id.toolbar)
                val popup  = PopupMenu(this, anchor)
                popup.menuInflater.inflate(R.menu.menu_options, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.option_reset    -> { clearAll();    true }
                        R.id.option_share    -> { shareResult(); true }
                        R.id.option_settings -> { showSettings(); true }
                        R.id.option_about    -> { showAbout();   true }
                        else                 -> false
                    }
                }
                popup.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ==========================================================
    // onTouchEvent — route every touch through the gesture detector
    // Always call super so the framework can still handle default
    // touch behavior (e.g. dismissing the soft keyboard) even when
    // our gesture detector doesn't consume the event.
    // ==========================================================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    // ==========================================================
    // calculate — validate input, then update all result fields
    // ==========================================================
    private fun calculate() {
        val billText = etBillAmount.text.toString().trim()

        // Empty input → show dashes, no error
        if (billText.isEmpty()) {
            setResults("—", "—", "—")
            return
        }

        val bill = billText.toDoubleOrNull()

        // Non-numeric or negative → show error indicator in all three fields.
        // "ERR" is short enough to fit the output TextViews and is immediately
        // recognizable as invalid input without being alarming.
        if (bill == null || bill < 0) {
            setResults("ERR", "ERR", "ERR")
            return
        }

        val tipPct    = seekBarTip.progress / 100.0
        val tipAmount = bill * tipPct
        val total     = bill + tipAmount
        val perPerson = total / splitCount

        setResults(
            "$%.2f".format(tipAmount),
            "$%.2f".format(perPerson),
            "$%.2f".format(total)
        )
    }

    // ==========================================================
    // setResults — push formatted strings into the three output
    //   TextViews in one call
    // ==========================================================
    private fun setResults(tip: String, perPerson: String, total: String) {
        tvTipAmount.text = tip
        tvPerPerson.text = perPerson
        tvTotal.text     = total
    }

    // ==========================================================
    // shareResult — sends the current calculation via Android's
    //   share sheet (email, messages, clipboard, etc.)
    // Intent.ACTION_SEND lets the OS handle every share target
    // without needing any special permissions.
    // ==========================================================
    private fun shareResult() {
        val tipText   = tvTipAmount.text.toString()
        val totalText = tvTotal.text.toString()

        // Nothing worth sharing if the fields are empty or in error
        if (tipText == "—" || tipText == "ERR") {
            Toast.makeText(this, "Enter a bill amount first", Toast.LENGTH_SHORT).show()
            return
        }

        val bill    = etBillAmount.text.toString().trim()
        val tipPct  = seekBarTip.progress
        val people  = splitCount

        val message = buildString {
            appendLine("💰 Tip Calculator Result")
            appendLine("────────────────────────")
            appendLine("Bill:        \$$bill")
            appendLine("Tip ($tipPct%):  $tipText")
            if (people > 1) {
                appendLine("Per Person:  ${tvPerPerson.text}  ($people people)")
            }
            appendLine("Total:       $totalText")
            appendLine()
            appendLine("Calculated with Tip Calculator — CSC2046")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Tip Calculation")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    // ==========================================================
    // showSettings — displays rounding preference options
    // AlertDialog.Builder is the standard Material way to present
    // a modal choice without spinning up a separate Activity.
    // ==========================================================
    private fun showSettings() {
        val options = arrayOf("Round tip to nearest dollar", "Show exact tip (default)")
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Rounding to whole dollars is a common restaurant preference.
                        val bill = etBillAmount.text.toString().trim().toDoubleOrNull()
                        if (bill != null && bill >= 0) {
                            val tipPct    = seekBarTip.progress / 100.0
                            val rawTip    = bill * tipPct
                            val roundedTip = Math.ceil(rawTip)
                            val total     = bill + roundedTip
                            val perPerson = total / splitCount
                            setResults(
                                "$%.2f".format(roundedTip),
                                "$%.2f".format(perPerson),
                                "$%.2f".format(total)
                            )
                            Toast.makeText(this, "Tip rounded up", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Enter a bill amount first", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        calculate()
                        Toast.makeText(this, "Showing exact tip", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ==========================================================
    // showAbout — displays app and developer info
    // ==========================================================
    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage(
                "Tip Calculator  v1.0\n\n" +
                "Developer:   Shane Potts\n" +
                "Course:      CSC2046 — Mobile App Development\n" +
                "School:      Front Range Community College\n" +
                "Term:        Spring 2026\n\n" +
                "Features:\n" +
                "  • Real-time tip calculation\n" +
                "  • Adjustable tip % via SeekBar\n" +
                "  • Quick-tip presets (10–25%)\n" +
                "  • Bill splitting\n" +
                "  • Swipe to clear"
            )
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // ==========================================================
    // clearAll — reset every field to its default state
    // Centralized so both the swipe handler and the menu's
    // Reset option call the same logic.
    // ==========================================================
    private fun clearAll() {
        etBillAmount.setText("")
        seekBarTip.progress  = 15       // triggers onProgressChanged → calculate()
        splitCount           = 1
        tvSplitCount.text    = "1"
        Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show()
    }
}
