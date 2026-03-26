// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 — Mobile App Development
// Module:      4 — Personal Notes (iOS)
// File:        Note.swift
// Description: Data model for a single note.
//              Conforming to Codable lets Swift automatically
//              serialize/deserialize the struct to JSON using
//              JSONEncoder and JSONDecoder — no manual parsing
//              required (unlike the org.json approach on Android).
// ============================================================

import Foundation

struct Note: Codable {
    var id:        String
    var title:     String
    var body:      String
    let createdAt: Date
    var updatedAt: Date

    // Convenience initializer for creating a brand-new note.
    // id and timestamps are generated automatically.
    init(title: String = "", body: String = "") {
        self.id        = UUID().uuidString
        self.title     = title
        self.body      = body
        self.createdAt = Date()
        self.updatedAt = Date()
    }
}
