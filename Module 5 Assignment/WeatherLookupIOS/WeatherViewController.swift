// ============================================================
// Name:        Shane Potts
// Date:        03/25/2026
// Course:      CSC2046 - Mobile App Development
// Module:      5 - Weather Lookup (iOS)
// File:        WeatherViewController.swift
// Description: Single-screen weather app built programmatically
//              with UIKit.  Mirrors the Android MainActivity's
//              three-state model:
//                LOADING  — spinner visible, content hidden
//                SUCCESS  — current card + forecast stack visible
//                ERROR    — error label + Retry button visible
//              Network calls use Swift async/await bridged from
//              UIKit via Task { }.  UI updates run on MainActor.
// ============================================================

import UIKit

class WeatherViewController: UIViewController {

    // ── Search bar ───────────────────────────────────────────────
    private let cityField  = UITextField()
    private let searchBtn  = UIButton(type: .system)

    // ── Loading ──────────────────────────────────────────────────
    private let spinner    = UIActivityIndicatorView(style: .large)

    // ── Error state ──────────────────────────────────────────────
    private let errorStack = UIStackView()
    private let errorLabel = UILabel()
    private let retryBtn   = UIButton(type: .system)

    // ── Current conditions ───────────────────────────────────────
    private let currentCard    = UIView()
    private let emojiLabel     = UILabel()
    private let cityLabel      = UILabel()
    private let tempLabel      = UILabel()
    private let descLabel      = UILabel()
    private let detailStack    = UIStackView()
    private let feelsLabel     = UILabel()
    private let humidLabel     = UILabel()
    private let windLabel      = UILabel()

    // ── Forecast ─────────────────────────────────────────────────
    private let forecastHeader = UILabel()
    private let forecastStack  = UIStackView()

    // ── Scroll container ─────────────────────────────────────────
    private let scrollView     = UIScrollView()
    private let contentView    = UIView()

    // ── State ─────────────────────────────────────────────────────
    private var lastCity = ""

    // ==========================================================
    // viewDidLoad
    // ==========================================================
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(named: "SurfaceVariant") ?? .systemGroupedBackground
        setupNavBar()
        setupScrollView()
        setupSearchRow()
        setupSpinner()
        setupErrorViews()
        setupCurrentCard()
        setupForecastSection()
        showIdle()
    }

    // ==========================================================
    // Navigation bar
    // ==========================================================
    private func setupNavBar() {
        title = "Weather Lookup"
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor         = UIColor(red: 0.01, green: 0.533, blue: 0.820, alpha: 1) // #0288D1
        appearance.titleTextAttributes     = [.foregroundColor: UIColor.white]
        navigationController?.navigationBar.standardAppearance   = appearance
        navigationController?.navigationBar.scrollEdgeAppearance = appearance
        navigationController?.navigationBar.tintColor            = .white

        navigationItem.rightBarButtonItem = UIBarButtonItem(
            image:  UIImage(systemName: "line.3.horizontal"),
            style:  .plain,
            target: self,
            action: #selector(menuTapped)
        )
    }

    @objc private func menuTapped() {
        let alert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        alert.addAction(UIAlertAction(title: "About", style: .default) { [weak self] _ in
            self?.showAbout()
        })
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        present(alert, animated: true)
    }

    private func showAbout() {
        let alert = UIAlertController(
            title:          "About",
            message:        "Developer:  Shane Potts\nCourse:     CSC2046 Mobile App Development\nModule 5:   Weather Lookup",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }

    // ==========================================================
    // Layout helpers
    // ==========================================================
    private func setupScrollView() {
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        contentView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)
        scrollView.addSubview(contentView)
        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            contentView.topAnchor.constraint(equalTo: scrollView.topAnchor),
            contentView.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
            contentView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
            contentView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor),
            contentView.widthAnchor.constraint(equalTo: scrollView.widthAnchor)
        ])
    }

    private func setupSearchRow() {
        cityField.placeholder   = "City name"
        cityField.borderStyle   = .roundedRect
        cityField.returnKeyType = .search
        cityField.delegate      = self
        cityField.translatesAutoresizingMaskIntoConstraints = false

        var config = UIButton.Configuration.filled()
        config.title            = "Search"
        config.baseBackgroundColor = UIColor(red: 0.01, green: 0.533, blue: 0.820, alpha: 1)
        config.baseForegroundColor = .white
        config.cornerStyle      = .medium
        searchBtn.configuration = config
        searchBtn.translatesAutoresizingMaskIntoConstraints = false
        searchBtn.addTarget(self, action: #selector(searchTapped), for: .touchUpInside)

        let row = UIStackView(arrangedSubviews: [cityField, searchBtn])
        row.axis    = .horizontal
        row.spacing = 8
        row.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(row)

        NSLayoutConstraint.activate([
            searchBtn.widthAnchor.constraint(equalToConstant: 80),
            row.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 16),
            row.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 16),
            row.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -16)
        ])
    }

    private func setupSpinner() {
        spinner.color = UIColor(red: 0.01, green: 0.533, blue: 0.820, alpha: 1)
        spinner.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(spinner)
        NSLayoutConstraint.activate([
            spinner.centerXAnchor.constraint(equalTo: contentView.centerXAnchor),
            spinner.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 180)
        ])
    }

    private func setupErrorViews() {
        let warningLabel = UILabel(); warningLabel.text = "⚠️"; warningLabel.font = .systemFont(ofSize: 40)
        errorLabel.textAlignment = .center
        errorLabel.numberOfLines = 0
        errorLabel.textColor     = UIColor(red: 0.78, green: 0.16, blue: 0.16, alpha: 1)

        var rConfig = UIButton.Configuration.bordered()
        rConfig.title = "Retry"
        retryBtn.configuration = rConfig
        retryBtn.addTarget(self, action: #selector(retryTapped), for: .touchUpInside)

        errorStack.axis      = .vertical
        errorStack.spacing   = 12
        errorStack.alignment = .center
        [warningLabel, errorLabel, retryBtn].forEach { errorStack.addArrangedSubview($0) }
        errorStack.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(errorStack)
        NSLayoutConstraint.activate([
            errorStack.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 120),
            errorStack.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 24),
            errorStack.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -24)
        ])
    }

    private func setupCurrentCard() {
        currentCard.backgroundColor    = .systemBackground
        currentCard.layer.cornerRadius = 16
        currentCard.layer.shadowColor  = UIColor.black.cgColor
        currentCard.layer.shadowOpacity = 0.1
        currentCard.layer.shadowRadius  = 6
        currentCard.layer.shadowOffset  = CGSize(width: 0, height: 2)
        currentCard.translatesAutoresizingMaskIntoConstraints = false

        emojiLabel.font      = .systemFont(ofSize: 72)
        emojiLabel.textAlignment = .center

        cityLabel.font       = .systemFont(ofSize: 18)
        cityLabel.textColor  = .secondaryLabel
        cityLabel.textAlignment = .center

        tempLabel.font       = UIFont.systemFont(ofSize: 72, weight: .thin)
        tempLabel.textColor  = UIColor(red: 0.96, green: 0.50, blue: 0.09, alpha: 1) // amber
        tempLabel.textAlignment = .center

        descLabel.font       = .systemFont(ofSize: 18)
        descLabel.textColor  = .secondaryLabel
        descLabel.textAlignment = .center

        let divider = UIView(); divider.backgroundColor = UIColor(red: 0.70, green: 0.90, blue: 0.99, alpha: 1)

        [feelsLabel, humidLabel, windLabel].forEach {
            $0.font = .systemFont(ofSize: 13)
            $0.textAlignment = .center
            $0.numberOfLines = 2
        }
        detailStack.axis         = .horizontal
        detailStack.distribution = .fillEqually
        detailStack.spacing      = 4
        [feelsLabel, humidLabel, windLabel].forEach { detailStack.addArrangedSubview($0) }

        let cardStack = UIStackView(arrangedSubviews: [emojiLabel, cityLabel, tempLabel, descLabel, divider, detailStack])
        cardStack.axis    = .vertical
        cardStack.spacing = 4
        cardStack.translatesAutoresizingMaskIntoConstraints = false
        cardStack.setCustomSpacing(16, after: descLabel)
        currentCard.addSubview(cardStack)
        contentView.addSubview(currentCard)

        NSLayoutConstraint.activate([
            divider.heightAnchor.constraint(equalToConstant: 1),
            cardStack.topAnchor.constraint(equalTo: currentCard.topAnchor, constant: 24),
            cardStack.leadingAnchor.constraint(equalTo: currentCard.leadingAnchor, constant: 16),
            cardStack.trailingAnchor.constraint(equalTo: currentCard.trailingAnchor, constant: -16),
            cardStack.bottomAnchor.constraint(equalTo: currentCard.bottomAnchor, constant: -24),
            currentCard.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 88),
            currentCard.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 16),
            currentCard.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -16)
        ])
    }

    private func setupForecastSection() {
        forecastHeader.text      = "5-Day Forecast"
        forecastHeader.font      = UIFont.systemFont(ofSize: 16, weight: .medium)
        forecastHeader.textColor = UIColor(red: 0.01, green: 0.533, blue: 0.820, alpha: 1)
        forecastHeader.translatesAutoresizingMaskIntoConstraints = false

        forecastStack.axis    = .vertical
        forecastStack.spacing = 8
        forecastStack.translatesAutoresizingMaskIntoConstraints = false

        contentView.addSubview(forecastHeader)
        contentView.addSubview(forecastStack)

        NSLayoutConstraint.activate([
            forecastHeader.topAnchor.constraint(equalTo: currentCard.bottomAnchor, constant: 24),
            forecastHeader.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 16),
            forecastStack.topAnchor.constraint(equalTo: forecastHeader.bottomAnchor, constant: 8),
            forecastStack.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 16),
            forecastStack.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -16),
            forecastStack.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -24)
        ])
    }

    // ==========================================================
    // State management
    // ==========================================================
    private func showIdle() {
        spinner.stopAnimating()
        errorStack.isHidden    = true
        currentCard.isHidden   = true
        forecastHeader.isHidden = true
        forecastStack.isHidden = true
    }

    private func showLoading() {
        spinner.startAnimating()
        errorStack.isHidden    = true
        currentCard.isHidden   = true
        forecastHeader.isHidden = true
        forecastStack.isHidden = true
    }

    private func showSuccess(current: CurrentWeather, forecast: [ForecastDay]) {
        spinner.stopAnimating()
        errorStack.isHidden = true

        emojiLabel.text  = iconToEmoji(current.iconCode)
        cityLabel.text   = "\(current.cityName), \(current.country)"
        tempLabel.text   = "\(Int(current.tempF))°F"
        descLabel.text   = current.description
        feelsLabel.text  = "Feels like\n\(Int(current.feelsLikeF))°F"
        humidLabel.text  = "Humidity\n\(current.humidity)%"
        windLabel.text   = "Wind\n\(Int(current.windMph)) mph"

        currentCard.isHidden    = false
        forecastHeader.isHidden = false
        forecastStack.isHidden  = false

        // Rebuild forecast rows
        forecastStack.arrangedSubviews.forEach { $0.removeFromSuperview() }
        forecast.forEach { day in forecastStack.addArrangedSubview(makeForecastRow(day)) }
    }

    private func showError(_ message: String) {
        spinner.stopAnimating()
        errorLabel.text        = message
        errorStack.isHidden    = false
        currentCard.isHidden   = true
        forecastHeader.isHidden = true
        forecastStack.isHidden = true
    }

    // ==========================================================
    // makeForecastRow — builds one day card for the forecast stack
    // ==========================================================
    private func makeForecastRow(_ day: ForecastDay) -> UIView {
        let card = UIView()
        card.backgroundColor    = .systemBackground
        card.layer.cornerRadius = 12
        card.layer.shadowColor  = UIColor.black.cgColor
        card.layer.shadowOpacity = 0.07
        card.layer.shadowRadius  = 4
        card.layer.shadowOffset  = CGSize(width: 0, height: 1)

        let dayLbl  = UILabel(); dayLbl.text  = day.dayName;  dayLbl.font = UIFont.boldSystemFont(ofSize: 15)
        let dateLbl = UILabel(); dateLbl.text = day.dateStr;  dateLbl.font = UIFont.systemFont(ofSize: 12); dateLbl.textColor = .secondaryLabel
        let emoji   = UILabel(); emoji.text   = iconToEmoji(day.iconCode); emoji.font = UIFont.systemFont(ofSize: 28)
        let desc    = UILabel(); desc.text    = day.description; desc.font = UIFont.systemFont(ofSize: 13); desc.textColor = .secondaryLabel; desc.numberOfLines = 1
        let high    = UILabel(); high.text    = "H: \(Int(day.highF))°";  high.font = UIFont.boldSystemFont(ofSize: 14); high.textColor = UIColor(red: 0.96, green: 0.50, blue: 0.09, alpha: 1)
        let low     = UILabel(); low.text     = "L: \(Int(day.lowF))°";   low.font = UIFont.systemFont(ofSize: 13); low.textColor = .secondaryLabel

        let dayStack = UIStackView(arrangedSubviews: [dayLbl, dateLbl])
        dayStack.axis = .vertical; dayStack.spacing = 2

        let tempStack = UIStackView(arrangedSubviews: [high, low])
        tempStack.axis = .vertical; tempStack.alignment = .trailing; tempStack.spacing = 2

        let row = UIStackView(arrangedSubviews: [dayStack, emoji, desc, tempStack])
        row.axis = .horizontal; row.spacing = 10; row.alignment = .center
        row.translatesAutoresizingMaskIntoConstraints = false

        dayStack.widthAnchor.constraint(equalToConstant: 52).isActive = true
        emoji.widthAnchor.constraint(equalToConstant: 36).isActive   = true
        tempStack.widthAnchor.constraint(equalToConstant: 52).isActive = true
        desc.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)

        card.addSubview(row)
        NSLayoutConstraint.activate([
            row.topAnchor.constraint(equalTo: card.topAnchor, constant: 12),
            row.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 12),
            row.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -12),
            row.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -12)
        ])
        return card
    }

    // ==========================================================
    // Actions
    // ==========================================================
    @objc private func searchTapped() {
        cityField.resignFirstResponder()
        let city = cityField.text?.trimmingCharacters(in: .whitespaces) ?? ""
        guard !city.isEmpty else {
            cityField.placeholder = "Enter a city name!"
            return
        }
        fetchWeather(city: city)
    }

    @objc private func retryTapped() {
        if !lastCity.isEmpty { fetchWeather(city: lastCity) }
    }

    private func fetchWeather(city: String) {
        lastCity = city
        showLoading()

        Task {
            do {
                async let current  = WeatherService.fetchCurrentWeather(city: city)
                async let forecast = WeatherService.fetchForecast(city: city)
                let (c, f) = try await (current, forecast)
                await MainActor.run { showSuccess(current: c, forecast: f) }
            } catch {
                await MainActor.run { showError(error.localizedDescription) }
            }
        }
    }
}

// ── UITextFieldDelegate — Search key triggers fetch ──────────
extension WeatherViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        searchTapped()
        return false
    }
}
