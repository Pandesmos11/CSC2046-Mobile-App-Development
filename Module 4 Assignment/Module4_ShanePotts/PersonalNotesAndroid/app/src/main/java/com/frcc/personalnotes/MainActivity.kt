// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      4 - Personal Notes
// File:        MainActivity.kt
// Description: Notes list screen.  Displays all saved notes in
//              a RecyclerView sorted by most-recently-updated.
//              The floating action button (FAB) opens a blank
//              editor; tapping a card opens that note for editing.
//              Swiping a card left or right deletes it with an
//              undo Snackbar.  The list refreshes in onResume so
//              any change made in NoteEditorActivity is reflected
//              without extra inter-activity communication.
// Inputs:      User taps (FAB, card, swipe)
// Outputs:     Updated RecyclerView, SharedPreferences changes
// ============================================================

package com.frcc.personalnotes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object {
        // Key used to pass a note's id to NoteEditorActivity
        const val EXTRA_NOTE_ID = "note_id"
    }

    // ── Views ───────────────────────────────────────────────────
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recycler: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var tvEmpty: TextView

    // ── Data layer ──────────────────────────────────────────────
    private lateinit var repo:    NotesRepository
    private lateinit var adapter: NoteAdapter

    // ==========================================================
    // onCreate - wire views, adapter, and gestures
    // ==========================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repo     = NotesRepository(this)
        toolbar  = findViewById(R.id.toolbar)
        recycler = findViewById(R.id.recyclerNotes)
        fab      = findViewById(R.id.fab)
        tvEmpty  = findViewById(R.id.tvEmpty)

        setSupportActionBar(toolbar)

        // Create adapter with an empty list; onResume loads real data
        adapter = NoteAdapter(mutableListOf()) { note -> openEditor(note.id) }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter       = adapter

        setupSwipeToDelete()

        fab.setOnClickListener { openEditor(null) }
    }

    // ==========================================================
    // onResume - reload list every time the screen becomes active
    //   so edits made in NoteEditorActivity are always visible
    // ==========================================================
    override fun onResume() {
        super.onResume()
        refreshList()
    }

    // ==========================================================
    // onCreateOptionsMenu - inflate the three-line icon into the toolbar
    // ==========================================================
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // ==========================================================
    // onOptionsItemSelected - tapping the three-line icon opens a
    //   PopupMenu anchored to the toolbar, matching Module 3's pattern
    // ==========================================================
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> {
                val popup = PopupMenu(this, toolbar)
                popup.menuInflater.inflate(R.menu.menu_options, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.option_about -> { showAbout(); true }
                        else              -> false
                    }
                }
                popup.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ==========================================================
    // refreshList - pull latest notes from SharedPrefs and update UI
    // ==========================================================
    private fun refreshList() {
        val notes = repo.loadNotes()
        adapter.updateList(notes)
        tvEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
    }

    // ==========================================================
    // openEditor - start NoteEditorActivity
    //   noteId = null  → create new note
    //   noteId = <id>  → edit existing note
    // ==========================================================
    private fun openEditor(noteId: String?) {
        val intent = Intent(this, NoteEditorActivity::class.java)
        if (noteId != null) intent.putExtra(EXTRA_NOTE_ID, noteId)
        startActivity(intent)
    }

    // ==========================================================
    // setupSwipeToDelete - attach ItemTouchHelper so left/right
    //   swipe on any card deletes that note, with an UNDO action
    // ==========================================================
    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            // We only handle swipe, not drag-to-reorder
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.adapterPosition
                if (position == RecyclerView.NO_POSITION) return

                // 1. Remove from adapter immediately for instant UI feedback
                val deleted = adapter.removeAt(position)

                // 2. Persist the deletion to SharedPreferences
                repo.deleteNote(deleted.id)

                // 3. Update empty-state banner
                tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE

                // 4. Offer an undo action via Snackbar
                Snackbar.make(recycler, "Note deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        repo.addNote(deleted)
                        refreshList()
                    }
                    .show()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recycler)
    }

    // ==========================================================
    // showAbout - displays developer / course info
    // ==========================================================
    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage(
                "Developer:  Shane Potts\n" +
                "Course:     CSC2046 Mobile App Development\n" +
                "Module 4:   Personal Notes"
            )
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
