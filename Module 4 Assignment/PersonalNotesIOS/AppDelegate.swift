// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 — Mobile App Development
// Module:      4 — Personal Notes (iOS)
// File:        AppDelegate.swift
// Description: Standard application delegate.  Scene lifecycle
//              is managed by SceneDelegate; this file only
//              handles app-level events (launch, termination).
// ============================================================

import UIKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        return true
    }

    // MARK: — UISceneSession Lifecycle

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        return UISceneConfiguration(
            name: "Default Configuration",
            sessionRole: connectingSceneSession.role
        )
    }
}
