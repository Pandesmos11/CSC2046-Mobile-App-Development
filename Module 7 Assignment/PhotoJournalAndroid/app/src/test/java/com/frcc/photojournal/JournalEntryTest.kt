// ============================================================
// Name:        Shane Potts
// Date:        04/22/2026
// Course:      CSC2046 - Mobile App Development
// Module:      7 - Testing and Debugging
// File:        JournalEntryTest.kt
// Description: Pure JUnit unit tests for the JournalEntry data
//              class.  No Android dependencies — runs on the JVM.
//              Verifies ID uniqueness, default field values,
//              mutable field updates, and timestamp validity.
// ============================================================

package com.frcc.photojournal

import org.junit.Assert.*
import org.junit.Test

class JournalEntryTest {

    // ----------------------------------------------------------
    // ID generation
    // ----------------------------------------------------------

    @Test
    fun `each entry receives a unique id by default`() {
        val e1 = JournalEntry(photoPath = "/photos/a.jpg")
        val e2 = JournalEntry(photoPath = "/photos/b.jpg")
        assertNotEquals(
            "Two entries created without explicit IDs should not share an ID",
            e1.id, e2.id
        )
    }

    @Test
    fun `explicit id is preserved`() {
        val entry = JournalEntry(id = "fixed-id-123", photoPath = "/photos/a.jpg")
        assertEquals("fixed-id-123", entry.id)
    }

    // ----------------------------------------------------------
    // Annotation field
    // ----------------------------------------------------------

    @Test
    fun `annotation defaults to empty string`() {
        val entry = JournalEntry(photoPath = "/photos/a.jpg")
        assertEquals("", entry.annotation)
    }

    @Test
    fun `annotation is stored correctly on construction`() {
        val entry = JournalEntry(photoPath = "/photos/a.jpg", annotation = "Sunset at the lake")
        assertEquals("Sunset at the lake", entry.annotation)
    }

    @Test
    fun `annotation can be updated after creation`() {
        val entry = JournalEntry(photoPath = "/photos/a.jpg", annotation = "Old text")
        entry.annotation = "New text"
        assertEquals("New text", entry.annotation)
    }

    @Test
    fun `annotation can be set to blank`() {
        val entry = JournalEntry(photoPath = "/photos/a.jpg", annotation = "Some text")
        entry.annotation = ""
        assertTrue(entry.annotation.isBlank())
    }

    // ----------------------------------------------------------
    // photoPath field
    // ----------------------------------------------------------

    @Test
    fun `photoPath is stored on construction`() {
        val entry = JournalEntry(photoPath = "/sdcard/photos/img001.jpg")
        assertEquals("/sdcard/photos/img001.jpg", entry.photoPath)
    }

    @Test
    fun `photoPath can be updated`() {
        val entry = JournalEntry(photoPath = "/old/path.jpg")
        entry.photoPath = "/new/path.jpg"
        assertEquals("/new/path.jpg", entry.photoPath)
    }

    // ----------------------------------------------------------
    // Timestamp
    // ----------------------------------------------------------

    @Test
    fun `createdAt is a positive timestamp`() {
        val entry = JournalEntry(photoPath = "/photos/a.jpg")
        assertTrue(
            "createdAt should be a positive epoch-millis timestamp",
            entry.createdAt > 0L
        )
    }

    @Test
    fun `createdAt is close to current time`() {
        val before = System.currentTimeMillis()
        val entry  = JournalEntry(photoPath = "/photos/a.jpg")
        val after  = System.currentTimeMillis()
        assertTrue(entry.createdAt in before..after)
    }

    @Test
    fun `later entry has higher or equal createdAt than earlier entry`() {
        val e1 = JournalEntry(photoPath = "/a.jpg")
        Thread.sleep(5)
        val e2 = JournalEntry(photoPath = "/b.jpg")
        assertTrue(e2.createdAt >= e1.createdAt)
    }
}
