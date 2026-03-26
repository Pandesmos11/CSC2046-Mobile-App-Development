// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      4 - Personal Notes
// File:        NotesRepository.kt
// Description: Handles all persistence using SharedPreferences.
//              Notes are serialized as a JSON array and stored
//              under a single key.  Using org.json (built-in on
//              Android) avoids adding any external dependencies.
//
//              Persistence method:  SharedPreferences + JSON
//              Storage location:    /data/data/com.frcc.personalnotes/
//                                   shared_prefs/notes_prefs.xml
//              Scope:               Private to this app only
//              Trade-off:           Simple to implement; suitable for
//                                   small-to-medium note collections.
//                                   A production app with hundreds of
//                                   notes would benefit from Room
//                                   (SQLite ORM) for indexed queries.
// ============================================================

package com.frcc.personalnotes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class NotesRepository(context: Context) {

    // Obtain a private SharedPreferences file for this app.
    // MODE_PRIVATE ensures no other app can read or write this file.
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "notes_prefs"
        private const val KEY_NOTES  = "notes_json"
    }

    // ==========================================================
    // loadNotes - deserialize JSON array → list, sorted by most
    //   recently updated first so new changes float to the top
    // ==========================================================
    fun loadNotes(): MutableList<Note> {
        val json  = prefs.getString(KEY_NOTES, "[]") ?: "[]"
        val array = JSONArray(json)
        val list  = mutableListOf<Note>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Note(
                    id        = obj.getString("id"),
                    title     = obj.getString("title"),
                    body      = obj.getString("body"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt")
                )
            )
        }

        // Most recently modified note appears first in the list
        return list.sortedByDescending { it.updatedAt }.toMutableList()
    }

    // ==========================================================
    // saveNotes - serialize list → JSON array and commit to prefs
    //   apply() writes asynchronously; commit() blocks the calling
    //   thread.  apply() is preferred here because a tiny delay
    //   before the write completes is acceptable for a notes app.
    // ==========================================================
    fun saveNotes(notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(
                JSONObject().apply {
                    put("id",        note.id)
                    put("title",     note.title)
                    put("body",      note.body)
                    put("createdAt", note.createdAt)
                    put("updatedAt", note.updatedAt)
                }
            )
        }
        prefs.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    // ==========================================================
    // addNote - prepend new note, persist, return refreshed list
    // ==========================================================
    fun addNote(note: Note): MutableList<Note> {
        val notes = loadNotes()
        notes.add(0, note)
        saveNotes(notes)
        return loadNotes()
    }

    // ==========================================================
    // updateNote - find by id, replace in list, persist
    //   Stamps updatedAt with current time so the sort order
    //   reflects when the note was last changed.
    // ==========================================================
    fun updateNote(updated: Note): MutableList<Note> {
        val notes = loadNotes()
        val idx   = notes.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            updated.updatedAt = System.currentTimeMillis()
            notes[idx]        = updated
        }
        saveNotes(notes)
        return loadNotes()
    }

    // ==========================================================
    // deleteNote - remove by id, persist, return refreshed list
    // ==========================================================
    fun deleteNote(id: String): MutableList<Note> {
        val notes = loadNotes().filter { it.id != id }.toMutableList()
        saveNotes(notes)
        return notes
    }
}
