// ============================================================
// Name:        Shane Potts
// Date:        03/25/2026
// Course:      CSC2046 - Mobile App Development
// Module:      5 - Weather Lookup
// File:        WeatherApiService.kt
// Description: All OpenWeatherMap network calls live here.
//              Uses HttpURLConnection (Android built-in) and
//              org.json for parsing — no external libraries needed.
//
//              IMPORTANT — API KEY SETUP:
//              1. Create a free account at https://openweathermap.org
//              2. Navigate to API Keys in your account dashboard
//              3. Copy your default key (or generate a new one)
//              4. Replace "YOUR_API_KEY_HERE" below with that key
//              Note: New keys take up to 2 hours to activate.
//
//              Endpoints used:
//                Current: /data/2.5/weather?q={city}&units=imperial
//                Forecast: /data/2.5/forecast?q={city}&units=imperial
// ============================================================

package com.frcc.weatherlookup

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale
import kotlin.math.abs

class WeatherApiService {

    companion object {
        // ── Replace this with your free OpenWeatherMap API key ──
        const val API_KEY = "YOUR_API_KEY_HERE"

        private const val BASE_URL = "https://api.openweathermap.org/data/2.5"
        private const val UNITS    = "imperial"   // Fahrenheit / mph
        private const val TIMEOUT  = 10_000       // 10 seconds
    }

    // ==========================================================
    // fetchCurrentWeather — GET /weather for the given city name.
    //   Throws an Exception with a user-readable message on any
    //   error so MainActivity can display it without inspecting
    //   HTTP codes or exception types directly.
    // ==========================================================
    fun fetchCurrentWeather(city: String): CurrentWeather {
        val encoded = URLEncoder.encode(city.trim(), "UTF-8")
        val url     = "$BASE_URL/weather?q=$encoded&units=$UNITS&appid=$API_KEY"
        val json    = JSONObject(get(url))

        // OWM returns HTTP 200 with cod=200 on success
        val cod = json.optInt("cod", 200)
        if (cod != 200) {
            throw Exception(json.optString("message", "City not found"))
        }

        val main    = json.getJSONObject("main")
        val weather = json.getJSONArray("weather").getJSONObject(0)
        val wind    = json.getJSONObject("wind")
        val sys     = json.getJSONObject("sys")

        return CurrentWeather(
            cityName    = json.getString("name"),
            country     = sys.getString("country"),
            tempF       = main.getDouble("temp"),
            feelsLikeF  = main.getDouble("feels_like"),
            humidity    = main.getInt("humidity"),
            windMph     = wind.getDouble("speed"),
            description = weather.getString("description")
                              .replaceFirstChar { it.uppercase() },
            iconCode    = weather.getString("icon")
        )
    }

    // ==========================================================
    // fetchForecast — GET /forecast for the given city.
    //   OWM returns 40 entries in 3-hour steps (5 days × 8 slots).
    //   We skip today, group by calendar date, then pick the
    //   reading closest to noon for the icon/description and use
    //   the daily min/max temps across all slots for that date.
    // ==========================================================
    fun fetchForecast(city: String): List<ForecastDay> {
        val encoded = URLEncoder.encode(city.trim(), "UTF-8")
        val url     = "$BASE_URL/forecast?q=$encoded&units=$UNITS&appid=$API_KEY&cnt=40"
        val json    = JSONObject(get(url))

        // Forecast endpoint returns cod as a String, not Int
        val cod = json.optString("cod", "200")
        if (cod != "200") {
            throw Exception(json.optString("message", "City not found"))
        }

        val list  = json.getJSONArray("list")
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // Group 3-hourly items by date, skipping today
        val dayMap = LinkedHashMap<String, MutableList<JSONObject>>()
        for (i in 0 until list.length()) {
            val item    = list.getJSONObject(i)
            val dateKey = item.getString("dt_txt").substring(0, 10)
            if (dateKey == today) continue
            dayMap.getOrPut(dateKey) { mutableListOf() }.add(item)
        }

        val parseFmt   = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayFmt = SimpleDateFormat("MMM d",      Locale.US)
        val dowFmt     = SimpleDateFormat("EEE",        Locale.US)

        return dayMap.entries.take(5).map { (dateKey, items) ->
            val date  = parseFmt.parse(dateKey)!!
            val temps = items.map { it.getJSONObject("main").getDouble("temp") }

            // Use the slot nearest to noon for representative icon / description
            val noonItem = items.minByOrNull { item ->
                val hour = item.getString("dt_txt").substring(11, 13).toInt()
                abs(hour - 12)
            }!!
            val dayWeather = noonItem.getJSONArray("weather").getJSONObject(0)

            ForecastDay(
                dayName     = dowFmt.format(date),
                dateStr     = displayFmt.format(date),
                highF       = temps.maxOrNull()!!,
                lowF        = temps.minOrNull()!!,
                description = dayWeather.getString("description")
                                  .replaceFirstChar { it.uppercase() },
                iconCode    = dayWeather.getString("icon")
            )
        }
    }

    // ==========================================================
    // get — opens an HttpURLConnection, reads the response body,
    //   and always disconnects.  Throws IOException on failure.
    // ==========================================================
    private fun get(urlString: String): String {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.requestMethod  = "GET"
        conn.connectTimeout = TIMEOUT
        conn.readTimeout    = TIMEOUT
        return try {
            // Use errorStream on non-2xx so OWM's JSON error body
            // is readable (e.g. {"cod":"404","message":"city not found"})
            val stream = if (conn.responseCode / 100 == 2)
                conn.inputStream else conn.errorStream
            stream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
