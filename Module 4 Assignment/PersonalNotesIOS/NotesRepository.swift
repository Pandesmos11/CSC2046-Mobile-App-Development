// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 — Mobile App Development
// Module:      4 — Personal Notes (iOS)
// File:        NotesRepository.swift
// Description: Handles all persistence using UserDefaults.
//              Notes are encoded as a JSON blob (Data) with
//              JSONEncoder, then stored under a single key.
//              JSONDecoder reconstructs the Swift structs on load.
//
//              Persistence method:  UserDefaults + Codable/JSON
//              Storage location:    ~/Library/Preferences/
//                                   <bundle-id>.plist
//              Scope:               Private to this app only
//              Trade-off:           Identical conceptual role as
//                                   SharedPreferences on Android.
//                                   Simple and dependency-free;
//                                   Core Data would be preferred
//                                   for large datasets or complex
//                                   queries in production.
// ============================================================

import Foundation

final class NotesRepository {

    // Singleton so every view controller shares one instance
    static let shared = NotesRepository()
    private init() {}

    private let key     = "personal_notes_v1"
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    // ==========================================================
    // loadNotes — read JSON blob from UserDefaults → [Note]
    //   Returns an empty array if no data exists yet.
    //   Results are sorted most-recently-updated first.
    // ==========================================================
    func loadNotes() -> [Note] {
        guard
            let data  = UserDefaults.standard.data(forKey: key),
            let notes = try? decoder.decode([Note].self, from: data)
        else {
            return []
        }
        return notes.sorted { $0.updatedAt > $1.updatedAt }
    }

    // ==========================================================
    // saveNotes — encode [Note] → JSON blob → UserDefaults
    //   Full replace; any note not in the array is discarded.
    // ==========================================================
    func saveNotes(_ notes: [Note]) {
        if let data = try? encoder.encode(notes) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }

    // ==========================================================
    // addNote — insert a new note and persist
    // ==========================================================
    @discardableResult
    func addNote(_ note: Note) -> [Note] {
        var notes = loadNotes()
        notes.insert(note, at: 0)
        saveNotes(notes)
        return loadNotes()
    }

    // ==========================================================
    // updateNote — find by id, replace, stamp updatedAt, persist
    // ==========================================================
    @discardableResult
    func updateNote(_ updated: Note) -> [Note] {
        var notes = loadNotes()
        if let idx = notes.firstIndex(where: { $0.id == updated.id }) {
            var note       = updated
            note.updatedAt = Date()
            notes[idx]     = note
        }
        saveNotes(notes)
        return loadNotes()
    }

    // ==========================================================
    // deleteNote — remove by id and persist
    // ==========================================================
    @discardableResult
    func deleteNote(id: String) -> [Note] {
        let notes = loadNotes().filter { $0.id != id }
        saveNotes(notes)
        return notes
    }
}
