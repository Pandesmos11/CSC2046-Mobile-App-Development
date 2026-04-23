// ============================================================
// Name:        Shane Potts
// Date:        04/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      6 - Photo Journal
// File:        JournalRepository.kt
// Description: Persistence layer using SharedPreferences + JSON.
//              Entries are serialised to a JSON array and stored
//              under the key "entries".  Photo files live in
//              app-specific external storage and are deleted here
//              when an entry is removed.
// ============================================================

package com.frcc.photojournal

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class JournalRepository(context: Context) {

    private val prefs = context.getSharedPreferences("photo_journal", Context.MODE_PRIVATE)

    // ----------------------------------------------------------
    // loadEntries — deserialise all entries, newest first
    // ----------------------------------------------------------
    fun loadEntries(): List<JournalEntry> {
        val json  = prefs.getString("entries", "[]") ?: "[]"
        val array = JSONArray(json)
        val list  = mutableListOf<JournalEntry>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                JournalEntry(
                    id         = obj.getString("id"),
                    photoPath  = obj.getString("photoPath"),
                    annotation = obj.optString("annotation", ""),
                    createdAt  = obj.getLong("createdAt")
                )
            )
        }
        return list.sortedByDescending { it.createdAt }
    }

    // ----------------------------------------------------------
    // saveEntries — serialise and persist the full list
    // ----------------------------------------------------------
    private fun saveEntries(entries: List<JournalEntry>) {
        val array = JSONArray()
        entries.forEach { e ->
            array.put(JSONObject().apply {
                put("id",         e.id)
                put("photoPath",  e.photoPath)
                put("annotation", e.annotation)
                put("createdAt",  e.createdAt)
            })
        }
        prefs.edit().putString("entries", array.toString()).apply()
    }

    fun addEntry(entry: JournalEntry) {
        val entries = loadEntries().toMutableList()
        entries.add(entry)
        saveEntries(entries)
    }

    fun updateEntry(entry: JournalEntry) {
        val entries = loadEntries().toMutableList()
        val idx     = entries.indexOfFirst { it.id == entry.id }
        if (idx >= 0) {
            entries[idx] = entry
            saveEntries(entries)
        }
    }

    // deleteEntry removes the record AND its photo file from disk
    fun deleteEntry(entry: JournalEntry) {
        val entries = loadEntries().toMutableList()
        entries.removeAll { it.id == entry.id }
        saveEntries(entries)
        File(entry.photoPath).takeIf { it.exists() }?.delete()
    }
}
