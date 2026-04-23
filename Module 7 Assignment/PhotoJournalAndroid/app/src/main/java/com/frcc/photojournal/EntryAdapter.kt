// ============================================================
// Name:        Shane Potts
// Date:        04/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      7 - Testing and Debugging
// File:        EntryAdapter.kt
// Description: RecyclerView adapter for the gallery grid.
//              Thumbnails are decoded on a background thread
//              using BitmapFactory with inSampleSize to keep
//              memory use low.  EXIF rotation is applied so
//              portrait photos display correctly on all devices.
//
//  FIX 1 (Module 7): Added LruCache keyed by file path so that
//              thumbnails decoded from disk are reused on scroll
//              rather than re-decoded on every bind.  Cache size
//              is 1/8 of the app's available heap.
//  FIX 3 (Module 7): When BitmapFactory.decodeFile returns null
//              (corrupt or externally-deleted file) the thumbnail
//              now shows a placeholder drawable instead of leaving
//              the ImageView in an undefined state.
// ============================================================

package com.frcc.photojournal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class EntryAdapter(
    private var entries: List<JournalEntry>,
    private val onClick: (JournalEntry) -> Unit
) : RecyclerView.Adapter<EntryAdapter.ViewHolder>() {

    private val executor = Executors.newFixedThreadPool(2)
    private val handler  = Handler(Looper.getMainLooper())
    private val dateFmt  = SimpleDateFormat("MMM d, yyyy", Locale.US)

    // FIX 1: LruCache — cache decoded thumbnails in memory so scrolling
    // the gallery doesn't hit the disk on every view bind.
    private val bitmapCache: LruCache<String, Bitmap> = object :
        LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount / 1024
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail:  ImageView = view.findViewById(R.id.img_thumbnail)
        val annotation: TextView  = view.findViewById(R.id.txt_annotation)
        val date:       TextView  = view.findViewById(R.id.txt_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]

        // Tag the view so we can discard stale results after recycling
        holder.thumbnail.setImageResource(R.color.teal_50)
        holder.thumbnail.tag = entry.photoPath

        // FIX 1: Check cache first; only decode from disk on a cache miss.
        // FIX 3: Fall back to a placeholder when the bitmap is null.
        val cached = bitmapCache.get(entry.photoPath)
        if (cached != null) {
            holder.thumbnail.setImageBitmap(cached)
        } else {
            executor.execute {
                val bmp = decodeSampledBitmap(entry.photoPath, 400, 400)
                if (bmp != null) bitmapCache.put(entry.photoPath, bmp)
                handler.post {
                    if (holder.thumbnail.tag == entry.photoPath) {
                        if (bmp != null) {
                            holder.thumbnail.setImageBitmap(bmp)
                        } else {
                            // FIX 3: Photo file missing or corrupt — show placeholder
                            holder.thumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    }
                }
            }
        }

        holder.annotation.text = entry.annotation.ifBlank { "No annotation" }
        holder.date.text        = dateFmt.format(Date(entry.createdAt))
        holder.itemView.setOnClickListener { onClick(entry) }
    }

    override fun getItemCount(): Int = entries.size

    fun updateList(newEntries: List<JournalEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    // ----------------------------------------------------------
    // decodeSampledBitmap — scales the image to fit reqWidth ×
    // reqHeight using inSampleSize (power-of-two downsampling),
    // then corrects for EXIF orientation.
    // ----------------------------------------------------------
    private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)

        var sample = 1
        while (opts.outWidth / sample > reqWidth || opts.outHeight / sample > reqHeight) {
            sample *= 2
        }

        val bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
            inSampleSize = sample
        }) ?: return null

        return applyExifRotation(bmp, path)
    }

    private fun applyExifRotation(bmp: Bitmap, path: String): Bitmap {
        val degrees = try {
            val exif = ExifInterface(path)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: Exception) { 0f }

        if (degrees == 0f) return bmp
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }
}
