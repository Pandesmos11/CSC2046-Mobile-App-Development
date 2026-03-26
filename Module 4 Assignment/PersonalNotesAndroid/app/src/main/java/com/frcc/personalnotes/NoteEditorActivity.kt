// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      4 - Personal Notes
// File:        NoteEditorActivity.kt
// Description: Create / edit a single note.
//              Receives an optional note id via Intent extras:
//                null id  → new-note mode (blank editor, cursor
//                           placed in title field automatically)
//                valid id → edit mode (fields pre-filled, Delete
//                           action visible in toolbar)
//              Saving:
//                • Toolbar "Save" icon   → explicit save + close
//                • System back / toolbar "←" → auto-save if
//                  title or body is non-empty, otherwise just close
//              Deleting:
//                • Toolbar "Delete" icon → confirmation dialog,
//                  then delete + close
// Inputs:      Note id (optional), title EditText, body EditText
// Outputs:     SharedPreferences updated via NotesRepository
// ============================================================

package com.frcc.personalnotes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class NoteEditorActivity : AppCompatActivity() {

    // ── Views ───────────────────────────────────────────────────
    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTitle: TextInputEditText
    private lateinit var etBody:  EditText

    // ── State ───────────────────────────────────────────────────
    private lateinit var repo:    NotesRepository
    private var currentNote: Note? = null
    private var isNewNote         = true

    // ==========================================================
    // onCreate - load existing note (if editing) and wire up back
    // ==========================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        repo    = NotesRepository(this)
        toolbar = findViewById(R.id.toolbarEditor)
        etTitle = findViewById(R.id.etNoteTitle)
        etBody  = findViewById(R.id.etNoteBody)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val noteId = intent.getStringExtra(MainActivity.EXTRA_NOTE_ID)
        if (noteId != null) {
            isNewNote   = false
            currentNote = repo.loadNotes().firstOrNull { it.id == noteId }
            currentNote?.let { note ->
                etTitle.setText(note.title)
                etBody.setText(note.body)
                // Place cursor at end of body so editing continues naturally
                etBody.setSelection(note.body.length)
            }
            supportActionBar?.title = "Edit Note"
        } else {
            supportActionBar?.title = "New Note"
            // Open keyboard on title field immediately for new notes
            etTitle.requestFocus()
        }

        // Register a back-press callback that auto-saves before closing.
        // Using OnBackPressedCallback (API 33+ approach) instead of the
        // deprecated onBackPressed() override.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    autoSaveAndFinish()
                }
            }
        )
    }

    // ==========================================================
    // onCreateOptionsMenu - inflate Save and (conditionally) Delete
    // ==========================================================
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        // Delete only makes sense for an existing note
        menu.findItem(R.id.action_delete)?.isVisible = !isNewNote
        return true
    }

    // ==========================================================
    // onOptionsItemSelected - toolbar button routing
    // ==========================================================
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // "←" navigation icon routes through the same back dispatcher
            // so the auto-save logic runs consistently
            android.R.id.home   -> { onBackPressedDispatcher.onBackPressed(); true }
            R.id.action_save    -> { performSave(); true }
            R.id.action_delete  -> { confirmDelete(); true }
            else                -> super.onOptionsItemSelected(item)
        }
    }

    // ==========================================================
    // performSave - write title + body to SharedPreferences
    //   If both fields are empty nothing is persisted and the
    //   activity just closes (no orphan empty notes).
    // ==========================================================
    private fun performSave() {
        val title = etTitle.text.toString().trim()
        val body  = etBody.text.toString().trim()

        if (title.isEmpty() && body.isEmpty()) {
            finish()
            return
        }

        if (isNewNote) {
            repo.addNote(Note(title = title, body = body))
        } else {
            currentNote?.let { note ->
                note.title = title
                note.body  = body
                // updatedAt is stamped inside NotesRepository.updateNote()
                repo.updateNote(note)
            }
        }
        finish()
    }

    // ==========================================================
    // autoSaveAndFinish - called by back-press callback; same
    //   behavior as performSave but triggered by navigation gesture
    // ==========================================================
    private fun autoSaveAndFinish() = performSave()

    // ==========================================================
    // confirmDelete - show a destructive-action confirmation
    //   dialog before permanently removing the note
    // ==========================================================
    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Delete this note? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                currentNote?.let { repo.deleteNote(it.id) }
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
