// ============================================================
// Name:        Shane Potts
// Date:        04/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      6 - Photo Journal
// File:        MainActivity.kt
// Description: Gallery screen — displays all journal entries in
//              a 2-column grid.  Tapping an entry opens it for
//              editing; the FAB starts a new entry.  An empty-
//              state label is shown when no entries exist yet.
// ============================================================

package com.frcc.photojournal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import android.widget.PopupMenu

class MainActivity : AppCompatActivity() {

    private lateinit var repository: JournalRepository
    private lateinit var adapter:    EntryAdapter
    private lateinit var emptyView:  TextView

    private val openEditor = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { loadEntries() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        repository = JournalRepository(this)
        emptyView  = findViewById(R.id.txt_empty)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_gallery)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = EntryAdapter(emptyList()) { entry ->
            val intent = Intent(this, EntryEditorActivity::class.java).apply {
                putExtra("mode",    "edit")
                putExtra("entryId", entry.id)
            }
            openEditor.launch(intent)
        }
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            val intent = Intent(this, EntryEditorActivity::class.java).apply {
                putExtra("mode", "new")
            }
            openEditor.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadEntries()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu) {
            showPopupMenu(findViewById(R.id.action_menu))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showPopupMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            inflate(R.menu.menu_options)
            setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.option_about) showAbout()
                true
            }
            show()
        }
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage(
                "Developer:  Shane Potts\n" +
                "Course:     CSC2046 Mobile App Development\n" +
                "Module 6:   Photo Journal"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadEntries() {
        val entries = repository.loadEntries()
        adapter.updateList(entries)
        emptyView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
    }
}
