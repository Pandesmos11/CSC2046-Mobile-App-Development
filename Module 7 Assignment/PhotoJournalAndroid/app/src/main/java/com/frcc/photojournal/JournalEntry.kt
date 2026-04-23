// ============================================================
// Name:        Shane Potts
// Date:        04/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      6 - Photo Journal
// File:        JournalEntry.kt
// Description: Data class representing a single journal entry.
//              Each entry links a photo (stored in app-specific
//              external storage) to a user-supplied annotation.
// ============================================================

package com.frcc.photojournal

import java.util.UUID

data class JournalEntry(
    val id: String          = UUID.randomUUID().toString(),
    var photoPath: String,                          // absolute path on disk
    var annotation: String  = "",
    val createdAt: Long     = System.currentTimeMillis()
)
