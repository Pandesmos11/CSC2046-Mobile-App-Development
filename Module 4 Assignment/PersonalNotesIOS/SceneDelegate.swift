// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 — Mobile App Development
// Module:      4 — Personal Notes (iOS)
// File:        SceneDelegate.swift
// Description: Programmatically creates the UIWindow and sets
//              NotesListViewController (inside a UINavigationController)
//              as the root view controller.
//              Note: In Xcode, delete the "Main storyboard file base
//              name" entry from Info.plist and set the Scene →
//              Storyboard Name to blank so this code takes over.
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

        // Create root view controller hierarchy programmatically
        let notesListVC = NotesListViewController(style: .plain)
        let navController = UINavigationController(rootViewController: notesListVC)

        // Create and configure the window
        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = navController
        window?.makeKeyAndVisible()
    }
}
