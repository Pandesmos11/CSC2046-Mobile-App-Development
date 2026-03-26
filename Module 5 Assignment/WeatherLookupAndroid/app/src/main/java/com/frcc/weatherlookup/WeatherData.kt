// ============================================================
// Name:        Shane Potts
// Date:        03/25/2026
// Course:      CSC2046 - Mobile App Development
// Module:      5 - Weather Lookup
// File:        WeatherData.kt
// Description: Display-ready data models produced by
//              WeatherApiService after parsing the raw JSON
//              from OpenWeatherMap.  These classes are intentionally
//              separate from the raw API response format so the UI
//              never has to touch org.json objects directly.
// ============================================================

package com.frcc.weatherlookup

// Current conditions for the searched city
data class CurrentWeather(
    val cityName:    String,
    val country:     String,
    val tempF:       Double,
    val feelsLikeF:  Double,
    val humidity:    Int,
    val windMph:     Double,
    val description: String,
    val iconCode:    String       // OWM icon code, e.g. "01d", "10n"
)

// One day in the 5-day forecast (built from OWM's 3-hourly list)
data class ForecastDay(
    val dayName:     String,      // "Mon", "Tue", …
    val dateStr:     String,      // "Mar 25"
    val highF:       Double,
    val lowF:        Double,
    val description: String,
    val iconCode:    String
)

// Maps an OWM icon code to a representative emoji so the app
// avoids downloading images and adding an image-loading library.
fun iconToEmoji(code: String): String {
    val isNight = code.endsWith("n")
    return when (code.dropLast(1)) {
        "01" -> if (isNight) "🌙" else "☀️"
        "02" -> if (isNight) "🌙" else "⛅"
        "03" -> "🌥"
        "04" -> "☁️"
        "09" -> "🌧"
        "10" -> "🌦"
        "11" -> "⛈"
        "13" -> "🌨"
        "50" -> "🌫"
        else -> "🌡"
    }
}
