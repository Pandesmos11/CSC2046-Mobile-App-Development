// ============================================================
// Name:        Shane Potts
// Date:        03/25/2026
// Course:      CSC2046 - Mobile App Development
// Module:      5 - Weather Lookup (iOS)
// File:        SceneDelegate.swift
// Description: Programmatically sets WeatherViewController
//              as the root inside a UINavigationController.
//              In Xcode: delete "Storyboard Name" from the scene
//              configuration in Info.plist so this code runs.
// ============================================================

import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let weatherVC  = WeatherViewController()
        let navController = UINavigationController(rootViewController: weatherVC)
        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = navController
        window?.makeKeyAndVisible()
    }
}
