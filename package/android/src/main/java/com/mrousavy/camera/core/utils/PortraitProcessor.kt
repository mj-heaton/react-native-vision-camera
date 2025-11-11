package com.mrousavy.camera.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File

data class PortraitProcessingResult(val width: Int, val height: Int, val rotated: Boolean, val durationMs: Long)

object PortraitProcessor {
  private const val TAG = "PortraitProcessor"

  fun enforcePortrait(imagePath: String): PortraitProcessingResult {
    val start = SystemClock.elapsedRealtime()
    val file = File(imagePath)
    var rotated = false
    var size = FileUtils.getImageSize(imagePath)
    val originalExif = try {
      ExifInterface(imagePath)
    } catch (error: Throwable) {
      Log.w(TAG, "Unable to read EXIF before processing: ${error.message}")
      null
    }

    try {
      if (size.width >= size.height) {
        Log.i(TAG, "Image ${size.width}x${size.height} is not portrait - rotating 90deg...")
        val bitmapOptions = BitmapFactory.Options().apply {
          inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, bitmapOptions)
        if (bitmap != null) {
          val matrix = Matrix().apply { postRotate(90f) }
          val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
          bitmap.recycle()
          FileUtils.writeBitmapTofile(rotatedBitmap, file, 100)
          rotatedBitmap.recycle()
          rotated = true
          size = FileUtils.getImageSize(imagePath)
        } else {
          Log.w(TAG, "Failed to decode Bitmap for $imagePath. Skipping rotation.")
        }
      }

      val updatedExif = try {
        ExifInterface(imagePath)
      } catch (error: Throwable) {
        Log.w(TAG, "Unable to reopen EXIF after rotation: ${error.message}")
        null
      }

      if (originalExif != null && updatedExif != null) {
        ExifUtils.copyAttributes(originalExif, updatedExif)
        updatedExif.setAttribute(ExifInterface.TAG_ORIENTATION, null)
        updatedExif.saveAttributes()
      } else if (updatedExif != null) {
        updatedExif.setAttribute(ExifInterface.TAG_ORIENTATION, null)
        updatedExif.saveAttributes()
      }

      val duration = SystemClock.elapsedRealtime() - start
      Log.i(TAG, "Portrait normalization (rotated=$rotated) completed in ${duration}ms (${size.width}x${size.height}).")
      return PortraitProcessingResult(size.width, size.height, rotated, duration)
    } catch (error: Throwable) {
      val duration = SystemClock.elapsedRealtime() - start
      Log.e(TAG, "Failed to enforce portrait output: ${error.message}", error)
      return PortraitProcessingResult(size.width, size.height, rotated, duration)
    }
  }
}
