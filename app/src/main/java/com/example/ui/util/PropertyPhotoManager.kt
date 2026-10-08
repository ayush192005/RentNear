package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PropertyPhotoManager {

    private const val PHOTO_DIR_NAME = "property_photos"
    private const val CACHE_DIR_NAME = "camera_photos"

    /**
     * Copies an image chosen from Gallery (via Content Uri) to permanent internal app storage.
     * This avoids permission expiry when reading temporary gallery content Uris.
     */
    fun saveGalleryUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, PHOTO_DIR_NAME).apply { mkdirs() }
            val destFile = File(dir, "gallery_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to original uri string if copy fails
            sourceUri.toString()
        }
    }

    /**
     * Saves a captured camera Bitmap directly into internal storage.
     */
    fun saveCameraBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val dir = File(context.filesDir, PHOTO_DIR_NAME).apply { mkdirs() }
            val file = File(dir, "cam_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates a temporary file and FileProvider Uri for high-resolution Camera capture.
     */
    fun createTempCameraUri(context: Context): Pair<Uri, File>? {
        return try {
            val cacheDir = File(context.cacheDir, CACHE_DIR_NAME).apply { mkdirs() }
            val tempFile = File(cacheDir, "temp_capture_${System.currentTimeMillis()}.jpg")
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, tempFile)
            Pair(uri, tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Promotes a captured temporary camera file into permanent internal storage.
     */
    fun promoteTempCameraFile(context: Context, tempFile: File): String? {
        return try {
            if (!tempFile.exists() || tempFile.length() == 0L) return null
            val dir = File(context.filesDir, PHOTO_DIR_NAME).apply { mkdirs() }
            val destFile = File(dir, "camera_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
            tempFile.copyTo(destFile, overwrite = true)
            tempFile.delete()
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
