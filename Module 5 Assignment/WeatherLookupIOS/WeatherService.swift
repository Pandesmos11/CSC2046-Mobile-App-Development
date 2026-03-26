// ============================================================
// Name:        Shane Potts
// Date:        03/25/2026
// Course:      CSC2046 - Mobile App Development
// Module:      5 - Weather Lookup (iOS)
// File:        WeatherService.swift
// Description: All OpenWeatherMap network calls for iOS.
//              Uses URLSession with Swift async/await (iOS 15+).
//              Both fetch functions are marked async throws so
//              the caller can use structured concurrency with
//              try await and handle errors uniformly.
//
//              IMPORTANT — API KEY SETUP:
//              Replace "YOUR_API_KEY_HERE" with your free key
//              from https://openweathermap.org (account > API Keys).
//              New keys take up to 2 hours to activate.
// ============================================================

import Foundation

enum WeatherService {

    // ── Replace with your free OpenWeatherMap API key ──────────
    static let apiKey  = "YOUR_API_KEY_HERE"

    private static let baseURL = "https://api.openweathermap.org/data/2.5"
    private static let units   = "imperial"   // Fahrenheit / mph

    // ==========================================================
    // fetchCurrentWeather — GET /weather for the given city name
    // ==========================================================
    static func fetchCurrentWeather(city: String) async throws -> CurrentWeather {
        let raw = try await get(path: "weather", city: city, as: OWMCurrentResponse.self)
        return CurrentWeather(
            cityName:    raw.name,
            country:     raw.sys.country,
            tempF:       raw.main.temp,
            feelsLikeF:  raw.main.feelsLike,
            humidity:    raw.main.humidity,
            windMph:     raw.wind.speed,
            description: raw.weather.first?.description.capitalized ?? "",
            iconCode:    raw.weather.first?.icon ?? "01d"
        )
    }

    // ==========================================================
    // fetchForecast — GET /forecast, group 3-hourly items into
    //   5 daily summaries (skipping today), using nearest-to-noon
    //   slot for icon/description and daily min/max for temps.
    // ==========================================================
    static func fetchForecast(city: String) async throws -> [ForecastDay] {
        let raw = try await get(path: "forecast", city: city, as: OWMForecastResponse.self)

        let cal     = Calendar.current
        let today   = cal.startOfDay(for: Date())
        let dowFmt  = DateFormatter(); dowFmt.dateFormat  = "EEE"
        let dateFmt = DateFormatter(); dateFmt.dateFormat = "MMM d"
        let txtFmt  = DateFormatter(); txtFmt.dateFormat  = "yyyy-MM-dd HH:mm:ss"

        // Group items by calendar day, skip today
        var dayMap: [Date: [OWMForecastResponse.OWMForecastItem]] = [:]
        for item in raw.list {
            guard let date = txtFmt.date(from: item.dtTxt) else { continue }
            let dayStart = cal.startOfDay(for: date)
            if dayStart == today { continue }
            dayMap[dayStart, default: []].append(item)
        }

        return dayMap.keys.sorted().prefix(5).compactMap { dayStart in
            guard let items = dayMap[dayStart], !items.isEmpty else { return nil }
            let temps = items.map { $0.main.temp }

            // Slot nearest to 12:00 for representative icon/description
            let noonItem = items.min { a, b in
                let aHour = cal.component(.hour, from: txtFmt.date(from: a.dtTxt) ?? Date())
                let bHour = cal.component(.hour, from: txtFmt.date(from: b.dtTxt) ?? Date())
                return abs(aHour - 12) < abs(bHour - 12)
            }!

            return ForecastDay(
                dayName:     dowFmt.string(from: dayStart),
                dateStr:     dateFmt.string(from: dayStart),
                highF:       temps.max()!,
                lowF:        temps.min()!,
                description: noonItem.weather.first?.description.capitalized ?? "",
                iconCode:    noonItem.weather.first?.icon ?? "01d"
            )
        }
    }

    // ==========================================================
    // get — shared fetch + decode helper
    //   Throws WeatherError for 404 and other non-200 responses,
    //   and WeatherError.decodingFailed if JSON doesn't match T.
    // ==========================================================
    private static func get<T: Decodable>(
        path: String,
        city: String,
        as type: T.Type
    ) async throws -> T {
        guard let encoded = city
            .trimmingCharacters(in: .whitespaces)
            .addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: "\(baseURL)/\(path)?q=\(encoded)&units=\(units)&appid=\(apiKey)")
        else { throw WeatherError.cityNotFound }

        let (data, response) = try await URLSession.shared.data(from: url)
        let status = (response as? HTTPURLResponse)?.statusCode ?? 0

        if status == 404 { throw WeatherError.cityNotFound }
        if status / 100 != 2 { throw WeatherError.badResponse(status) }

        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch {
            throw WeatherError.decodingFailed
        }
    }
}
