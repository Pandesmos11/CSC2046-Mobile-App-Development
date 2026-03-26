// ============================================================
// Name:        Shane Potts
// Date:        03/25/2026
// Course:      CSC2046 - Mobile App Development
// Module:      5 - Weather Lookup (iOS)
// File:        WeatherModels.swift
// Description: Two layers of models:
//              1. OWM response structs (Codable) — mirror the
//                 JSON returned by OpenWeatherMap exactly.
//              2. Display structs — clean, UI-ready data that
//                 WeatherService produces after decoding.
//              Keeping them separate means the UI never has to
//              understand raw API field names like "feels_like".
// ============================================================

import Foundation

// ── OWM raw response structs ─────────────────────────────────

struct OWMCurrentResponse: Codable {
    let name: String
    let main: OWMMain
    let weather: [OWMWeather]
    let wind: OWMWind
    let sys: OWMSys

    struct OWMMain: Codable {
        let temp: Double
        let feelsLike: Double
        let humidity: Int
        enum CodingKeys: String, CodingKey {
            case temp, humidity
            case feelsLike = "feels_like"
        }
    }
    struct OWMWeather: Codable {
        let description: String
        let icon: String
    }
    struct OWMWind: Codable {
        let speed: Double
    }
    struct OWMSys: Codable {
        let country: String
    }
}

struct OWMForecastResponse: Codable {
    let list: [OWMForecastItem]

    struct OWMForecastItem: Codable {
        let dt: TimeInterval
        let main: OWMItemMain
        let weather: [OWMCurrentResponse.OWMWeather]
        let dtTxt: String
        enum CodingKeys: String, CodingKey {
            case dt, main, weather
            case dtTxt = "dt_txt"
        }
        struct OWMItemMain: Codable {
            let temp: Double
            let tempMin: Double
            let tempMax: Double
            enum CodingKeys: String, CodingKey {
                case temp
                case tempMin = "temp_min"
                case tempMax = "temp_max"
            }
        }
    }
}

// ── Display models ───────────────────────────────────────────

struct CurrentWeather {
    let cityName:    String
    let country:     String
    let tempF:       Double
    let feelsLikeF:  Double
    let humidity:    Int
    let windMph:     Double
    let description: String
    let iconCode:    String
}

struct ForecastDay {
    let dayName:     String   // "Mon"
    let dateStr:     String   // "Mar 25"
    let highF:       Double
    let lowF:        Double
    let description: String
    let iconCode:    String
}

// ── Errors ───────────────────────────────────────────────────

enum WeatherError: LocalizedError {
    case cityNotFound
    case badResponse(Int)
    case decodingFailed

    var errorDescription: String? {
        switch self {
        case .cityNotFound:       return "City not found. Check spelling and try again."
        case .badResponse(let c): return "Server error (HTTP \(c)). Try again later."
        case .decodingFailed:     return "Could not read weather data. Try again later."
        }
    }
}

// ── Icon helper ──────────────────────────────────────────────

func iconToEmoji(_ code: String) -> String {
    let isNight = code.hasSuffix("n")
    switch String(code.dropLast()) {
    case "01": return isNight ? "🌙" : "☀️"
    case "02": return isNight ? "🌙" : "⛅"
    case "03": return "🌥"
    case "04": return "☁️"
    case "09": return "🌧"
    case "10": return "🌦"
    case "11": return "⛈"
    case "13": return "🌨"
    case "50": return "🌫"
    default:   return "🌡"
    }
}
