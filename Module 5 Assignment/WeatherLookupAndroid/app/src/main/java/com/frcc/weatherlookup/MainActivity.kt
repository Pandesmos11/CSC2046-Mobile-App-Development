// ============================================================
// Name:        Shane Potts
// Date:        03/25/2026
// Course:      CSC2046 - Mobile App Development
// Module:      5 - Weather Lookup
// File:        MainActivity.kt
// Description: Single-screen weather app.  The user types a city
//              name and taps Search (or presses the keyboard's
//              Search key).  The app fetches current conditions
//              and a 5-day forecast from OpenWeatherMap and
//              displays them using three distinct UI states:
//
//              LOADING  — ProgressBar visible, content hidden
//              SUCCESS  — weather card + forecast list visible
//              ERROR    — error card visible with a Retry button
//
//              Networking runs on a background Executor thread;
//              UI updates are posted back via Handler(mainLooper)
//              so the main thread is never blocked.
// Inputs:      City name text field
// Outputs:     Current conditions card, 5-day forecast RecyclerView
// ============================================================

package com.frcc.weatherlookup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    // ── Views ────────────────────────────────────────────────────
    private lateinit var toolbar:         MaterialToolbar
    private lateinit var etCity:          TextInputEditText
    private lateinit var btnSearch:       MaterialButton
    private lateinit var progressBar:     View
    private lateinit var cardError:       MaterialCardView
    private lateinit var tvError:         TextView
    private lateinit var btnRetry:        MaterialButton
    private lateinit var cardCurrent:     MaterialCardView
    private lateinit var tvEmoji:         TextView
    private lateinit var tvCityName:      TextView
    private lateinit var tvTemp:          TextView
    private lateinit var tvDescription:   TextView
    private lateinit var tvFeelsLike:     TextView
    private lateinit var tvHumidity:      TextView
    private lateinit var tvWind:          TextView
    private lateinit var tvForecastLabel: TextView
    private lateinit var rvForecast:      RecyclerView

    // ── Data / threading ────────────────────────────────────────
    private val apiService  = WeatherApiService()
    private val ioExecutor  = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var forecastAdapter: ForecastAdapter

    // Remembers the last searched city so Retry can re-fire it
    private var lastCity = ""

    // ==========================================================
    // onCreate
    // ==========================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setSupportActionBar(toolbar)

        forecastAdapter = ForecastAdapter(emptyList())
        rvForecast.layoutManager        = LinearLayoutManager(this)
        rvForecast.adapter              = forecastAdapter
        rvForecast.isNestedScrollingEnabled = false

        btnSearch.setOnClickListener { triggerSearch() }

        // Allow keyboard "Search" / "Done" key to trigger search
        etCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { triggerSearch(); true }
            else false
        }

        btnRetry.setOnClickListener { if (lastCity.isNotEmpty()) fetchWeather(lastCity) }

        showIdle()
    }

    // ==========================================================
    // triggerSearch — validate input then kick off network fetch
    // ==========================================================
    private fun triggerSearch() {
        val city = etCity.text.toString().trim()
        if (city.isEmpty()) {
            etCity.error = "Please enter a city name"
            return
        }
        hideKeyboard()
        fetchWeather(city)
    }

    // ==========================================================
    // fetchWeather — run both API calls on a background thread,
    //   then post results or an error back to the main thread
    // ==========================================================
    private fun fetchWeather(city: String) {
        lastCity = city
        showLoading()

        ioExecutor.execute {
            try {
                val current  = apiService.fetchCurrentWeather(city)
                val forecast = apiService.fetchForecast(city)
                mainHandler.post { showSuccess(current, forecast) }
            } catch (e: Exception) {
                val msg = e.message ?: "An unexpected error occurred"
                mainHandler.post { showError(msg) }
            }
        }
    }

    // ==========================================================
    // showLoading / showSuccess / showError / showIdle
    //   Each method sets exactly the right views visible/gone.
    // ==========================================================
    private fun showIdle() {
        progressBar.visibility     = View.GONE
        cardError.visibility       = View.GONE
        cardCurrent.visibility     = View.GONE
        tvForecastLabel.visibility = View.GONE
        rvForecast.visibility      = View.GONE
    }

    private fun showLoading() {
        progressBar.visibility     = View.VISIBLE
        cardError.visibility       = View.GONE
        cardCurrent.visibility     = View.GONE
        tvForecastLabel.visibility = View.GONE
        rvForecast.visibility      = View.GONE
    }

    private fun showSuccess(current: CurrentWeather, forecast: List<ForecastDay>) {
        progressBar.visibility = View.GONE
        cardError.visibility   = View.GONE

        // Current conditions
        tvEmoji.text       = iconToEmoji(current.iconCode)
        tvCityName.text    = "${current.cityName}, ${current.country}"
        tvTemp.text        = "${current.tempF.toInt()}°F"
        tvDescription.text = current.description
        tvFeelsLike.text   = "Feels like ${current.feelsLikeF.toInt()}°F"
        tvHumidity.text    = "Humidity ${current.humidity}%"
        tvWind.text        = "Wind ${current.windMph.toInt()} mph"

        cardCurrent.visibility     = View.VISIBLE
        tvForecastLabel.visibility = View.VISIBLE
        rvForecast.visibility      = View.VISIBLE

        forecastAdapter.updateDays(forecast)
    }

    private fun showError(message: String) {
        progressBar.visibility     = View.GONE
        cardCurrent.visibility     = View.GONE
        tvForecastLabel.visibility = View.GONE
        rvForecast.visibility      = View.GONE

        tvError.text         = message
        cardError.visibility = View.VISIBLE
    }

    // ==========================================================
    // Menu — three-line icon opens PopupMenu with About
    // ==========================================================
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> {
                val popup = PopupMenu(this, toolbar)
                popup.menuInflater.inflate(R.menu.menu_options, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.option_about -> { showAbout(); true }
                        else              -> false
                    }
                }
                popup.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage(
                "Developer:  Shane Potts\n" +
                "Course:     CSC2046 Mobile App Development\n" +
                "Module 5:   Weather Lookup"
            )
            .setPositiveButton("OK") { d, _ -> d.dismiss() }
            .show()
    }

    // ==========================================================
    // bindViews — find all widgets by ID once
    // ==========================================================
    private fun bindViews() {
        toolbar         = findViewById(R.id.toolbar)
        etCity          = findViewById(R.id.etCity)
        btnSearch       = findViewById(R.id.btnSearch)
        progressBar     = findViewById(R.id.progressBar)
        cardError       = findViewById(R.id.cardError)
        tvError         = findViewById(R.id.tvError)
        btnRetry        = findViewById(R.id.btnRetry)
        cardCurrent     = findViewById(R.id.cardCurrent)
        tvEmoji         = findViewById(R.id.tvWeatherEmoji)
        tvCityName      = findViewById(R.id.tvCityName)
        tvTemp          = findViewById(R.id.tvTemperature)
        tvDescription   = findViewById(R.id.tvDescription)
        tvFeelsLike     = findViewById(R.id.tvFeelsLike)
        tvHumidity      = findViewById(R.id.tvHumidity)
        tvWind          = findViewById(R.id.tvWind)
        tvForecastLabel = findViewById(R.id.tvForecastLabel)
        rvForecast      = findViewById(R.id.rvForecast)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etCity.windowToken, 0)
    }
}
