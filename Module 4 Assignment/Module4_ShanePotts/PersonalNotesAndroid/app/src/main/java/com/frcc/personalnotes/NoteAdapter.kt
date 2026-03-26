// ============================================================
// Name:        Shane Potts
// Date:        03/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      4 - Personal Notes
// File:        NoteAdapter.kt
// Description: RecyclerView adapter for the notes list.
//              Each card shows the note title, a short body
//              preview, and the last-modified timestamp.
//              Clicking a card opens the editor via a callback.
//              removeAt() is called by MainActivity's swipe-to-
//              delete ItemTouchHelper before the repository delete
//              so the UI responds instantly without waiting for
//              the SharedPreferences write to complete.
// ============================================================

package com.frcc.personalnotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private var notes: MutableList<Note>,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    // Format: "Mar 11, 2026  2:45 PM"
    private val dateFormat = SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault())

    // ==========================================================
    // ViewHolder - caches the three TextViews per card
    // ==========================================================
    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle:   TextView = view.findViewById(R.id.tvNoteTitle)
        val tvPreview: TextView = view.findViewById(R.id.tvNotePreview)
        val tvDate:    TextView = view.findViewById(R.id.tvNoteDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]

        // Fall back to "Untitled" so empty-title notes still look sensible
        holder.tvTitle.text   = note.title.ifBlank { "Untitled" }

        // Collapse newlines so the two-line preview reads like a sentence
        holder.tvPreview.text = note.body.take(100).replace('\n', ' ')

        holder.tvDate.text    = dateFormat.format(Date(note.updatedAt))

        holder.itemView.setOnClickListener { onNoteClick(note) }
    }

    override fun getItemCount(): Int = notes.size

    // ==========================================================
    // updateList - replace entire dataset and refresh all rows
    // ==========================================================
    fun updateList(newNotes: MutableList<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    // ==========================================================
    // removeAt - used by ItemTouchHelper for swipe-to-delete;
    //   removes from the in-memory list and animates the card out
    //   before the caller persists the deletion to SharedPrefs
    // ==========================================================
    fun removeAt(position: Int): Note {
        val removed = notes.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }
}
