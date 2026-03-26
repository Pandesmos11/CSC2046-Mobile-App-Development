// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      4 - Personal Notes
// File:        Note.kt
// Description: Data model for a single note.  All mutable fields
//              (title, body, updatedAt) are declared with 'var'
//              so the editor can update them in place.  The id and
//              createdAt fields are 'val' because they must never
//              change after the note is first created.
// ============================================================

package com.frcc.personalnotes

data class Note(
    val id:        String = java.util.UUID.randomUUID().toString(),
    var title:     String,
    var body:      String,
    val createdAt: Long   = System.currentTimeMillis(),
    var updatedAt: Long   = System.currentTimeMillis()
)
