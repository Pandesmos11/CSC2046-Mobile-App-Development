// ============================================================
// Name:        Shane Potts
// Date:        04/11/2026
// Course:      CSC2046 - Mobile App Development
// Module:      6 - Photo Journal
// File:        EntryEditorActivity.kt
// Description: Create or edit a journal entry.
//
//              NEW mode:
//                - Immediately requests CAMERA permission
//                - Launches camera via FileProvider URI
//                - If user cancels before taking any photo → finish
//                - Auto-saves on back navigation if a photo exists
//
//              EDIT mode:
//                - Loads existing entry (photo + annotation)
//                - Tapping the photo relaunches the camera to retake
//                - Old photo file is deleted when a new one is saved
//                - Toolbar Delete button with confirmation dialog
//
//              Photo storage: getExternalFilesDir(DIRECTORY_PICTURES)
//              No READ/WRITE_EXTERNAL_STORAGE needed on Android 10+.
//              EXIF rotation corrected before display.
// ============================================================

package com.frcc.photojournal

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class EntryEditorActivity : AppCompatActivity() {

    private lateinit var repository:    JournalRepository
    private lateinit var photoContainer: View
    private lateinit var photoImageView: ImageView
    private lateinit var photoHint:      TextView
    private lateinit var annotationEdit: TextInputEditText

    private var mode         = "new"
    private var currentEntry: JournalEntry? = null
    private var photoUri:     Uri?   = null
    private var photoPath:    String = ""
    private var hasPhoto              = false

    // ----------------------------------------------------------
    // ActivityResultLaunchers — registered before onCreate
    // ----------------------------------------------------------
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            hasPhoto = true
            displayPhoto()
        } else if (mode == "new" && !hasPhoto) {
            // Cancelled before taking any photo in new-entry flow
            finish()
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else         onCameraPermissionDenied()
    }

    // ----------------------------------------------------------
    // onCreate
    // ----------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_editor)

        repository = JournalRepository(this)
        mode       = intent.getStringExtra("mode") ?: "new"

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (mode == "new") "New Entry" else "Edit Entry"
        }
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        photoContainer  = findViewById(R.id.photo_container)
        photoImageView  = findViewById(R.id.img_photo)
        photoHint       = findViewById(R.id.txt_photo_hint)
        annotationEdit  = findViewById(R.id.edit_annotation)

        photoContainer.setOnClickListener { checkCameraPermission() }

        if (mode == "edit") {
            loadExistingEntry()
        } else {
            checkCameraPermission()
        }

        // Auto-save on back if a photo was taken in new mode
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mode == "new" && hasPhoto) {
                    performSave()   // performSave calls finish()
                } else {
                    finish()
                }
            }
        })
    }

    // ----------------------------------------------------------
    // Toolbar menu
    // ----------------------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        menu.findItem(R.id.action_delete)?.isVisible = (mode == "edit")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_save   -> { performSave();  true }
        R.id.action_delete -> { confirmDelete(); true }
        else               -> super.onOptionsItemSelected(item)
    }

    // ----------------------------------------------------------
    // Load existing entry (edit mode)
    // ----------------------------------------------------------
    private fun loadExistingEntry() {
        val entryId = intent.getStringExtra("entryId")
        currentEntry = repository.loadEntries().find { it.id == entryId }
        currentEntry?.let { entry ->
            photoPath = entry.photoPath
            hasPhoto  = true
            annotationEdit.setText(entry.annotation)
            displayPhoto()
        }
    }

    // ----------------------------------------------------------
    // Camera permission + launch
    // ----------------------------------------------------------
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!,
            "photo_${System.currentTimeMillis()}.jpg"
        )
        photoPath = photoFile.absolutePath
        photoUri  = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        takePicture.launch(photoUri!!)
    }

    private fun onCameraPermissionDenied() {
        if (mode == "new" && !hasPhoto) {
            Toast.makeText(this, "Camera permission is required to add photos.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------------------------------
    // Display captured / loaded photo (background thread)
    // ----------------------------------------------------------
    private fun displayPhoto() {
        if (photoPath.isEmpty()) return
        photoHint.visibility = View.GONE
        Thread {
            val bmp = decodeSampledBitmap(photoPath, 1080, 1080)
            runOnUiThread { photoImageView.setImageBitmap(bmp) }
        }.start()
    }

    // ----------------------------------------------------------
    // Save / delete
    // ----------------------------------------------------------
    private fun performSave() {
        if (!hasPhoto) return
        val annotation = annotationEdit.text.toString().trim()
        if (mode == "new") {
            repository.addEntry(JournalEntry(photoPath = photoPath, annotation = annotation))
        } else {
            currentEntry?.let { entry ->
                val oldPath = entry.photoPath
                entry.annotation = annotation
                entry.photoPath  = photoPath
                repository.updateEntry(entry)
                // Delete old photo file if user retook the photo
                if (photoPath != oldPath) File(oldPath).takeIf { it.exists() }?.delete()
            }
        }
        setResult(RESULT_OK)
        finish()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Delete this journal entry and its photo?")
            .setPositiveButton("Delete") { _, _ ->
                currentEntry?.let { repository.deleteEntry(it) }
                setResult(RESULT_OK)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ----------------------------------------------------------
    // decodeSampledBitmap — power-of-two downsample + EXIF fix
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
