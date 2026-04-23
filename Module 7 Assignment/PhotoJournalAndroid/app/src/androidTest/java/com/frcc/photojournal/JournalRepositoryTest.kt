// ============================================================
// Name:        Shane Potts
// Date:        04/22/2026
// Course:      CSC2046 - Mobile App Development
// Module:      7 - Testing and Debugging
// File:        JournalRepositoryTest.kt
// Description: Instrumented tests for JournalRepository CRUD
//              operations.  Uses ApplicationProvider to obtain a
//              real Android Context so SharedPreferences behaves
//              exactly as it does at runtime.  A dedicated prefs
//              file is cleared before and after each test to
//              prevent state leaking between cases.
// ============================================================

package com.frcc.photojournal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalRepositoryTest {

    private lateinit var context:    Context
    private lateinit var repository: JournalRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear any leftover data before each test
        context.getSharedPreferences("photo_journal", Context.MODE_PRIVATE)
            .edit().clear().commit()
        repository = JournalRepository(context)
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("photo_journal", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    // ----------------------------------------------------------
    // loadEntries
    // ----------------------------------------------------------

    @Test
    fun loadEntries_returnsEmptyList_whenNothingStored() {
        assertTrue(repository.loadEntries().isEmpty())
    }

    // ----------------------------------------------------------
    // addEntry
    // ----------------------------------------------------------

    @Test
    fun addEntry_singleEntry_persistedAndRetrieved() {
        val entry = JournalEntry(photoPath = "/test/photo.jpg", annotation = "Hello")
        repository.addEntry(entry)

        val loaded = repository.loadEntries()
        assertEquals(1, loaded.size)
        assertEquals(entry.id,         loaded[0].id)
        assertEquals("/test/photo.jpg", loaded[0].photoPath)
        assertEquals("Hello",          loaded[0].annotation)
    }

    @Test
    fun addEntry_multipleEntries_allPersisted() {
        repeat(5) { i ->
            repository.addEntry(JournalEntry(photoPath = "/photo$i.jpg", annotation = "Note $i"))
        }
        assertEquals(5, repository.loadEntries().size)
    }

    @Test
    fun addEntry_preservesCreatedAt() {
        val entry = JournalEntry(photoPath = "/test.jpg", annotation = "ts check")
        repository.addEntry(entry)
        val loaded = repository.loadEntries().first()
        assertEquals(entry.createdAt, loaded.createdAt)
    }

    // ----------------------------------------------------------
    // loadEntries ordering
    // ----------------------------------------------------------

    @Test
    fun loadEntries_returnsMostRecentFirst() {
        val older = JournalEntry(photoPath = "/old.jpg")
        Thread.sleep(10)
        val newer = JournalEntry(photoPath = "/new.jpg")
        repository.addEntry(older)
        repository.addEntry(newer)

        val loaded = repository.loadEntries()
        assertEquals(
            "Newer entry should appear first",
            newer.id, loaded[0].id
        )
    }

    // ----------------------------------------------------------
    // updateEntry
    // ----------------------------------------------------------

    @Test
    fun updateEntry_changesAnnotation() {
        val entry = JournalEntry(photoPath = "/test.jpg", annotation = "Original")
        repository.addEntry(entry)

        entry.annotation = "Updated"
        repository.updateEntry(entry)

        assertEquals("Updated", repository.loadEntries().first().annotation)
    }

    @Test
    fun updateEntry_changesPhotoPath() {
        val entry = JournalEntry(photoPath = "/old.jpg")
        repository.addEntry(entry)

        entry.photoPath = "/new.jpg"
        repository.updateEntry(entry)

        assertEquals("/new.jpg", repository.loadEntries().first().photoPath)
    }

    @Test
    fun updateEntry_doesNotAddDuplicate() {
        val entry = JournalEntry(photoPath = "/test.jpg")
        repository.addEntry(entry)
        entry.annotation = "Changed"
        repository.updateEntry(entry)

        assertEquals(1, repository.loadEntries().size)
    }

    // ----------------------------------------------------------
    // deleteEntry
    // ----------------------------------------------------------

    @Test
    fun deleteEntry_removesEntry() {
        val entry = JournalEntry(photoPath = "/test.jpg")
        repository.addEntry(entry)
        repository.deleteEntry(entry)

        assertTrue(repository.loadEntries().isEmpty())
    }

    @Test
    fun deleteEntry_removesOnlyTargetEntry() {
        val keep   = JournalEntry(photoPath = "/keep.jpg")
        val remove = JournalEntry(photoPath = "/remove.jpg")
        repository.addEntry(keep)
        repository.addEntry(remove)
        repository.deleteEntry(remove)

        val loaded = repository.loadEntries()
        assertEquals(1, loaded.size)
        assertEquals(keep.id, loaded[0].id)
    }
}
