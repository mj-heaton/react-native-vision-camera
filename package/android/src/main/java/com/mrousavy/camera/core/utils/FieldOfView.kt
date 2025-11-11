package com.mrousavy.camera.core.utils

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.SizeF
import androidx.camera.camera2.interop.Camera2CameraInfo
import com.mrousavy.camera.core.CameraSession
import kotlin.math.atan

data class FieldOfView(val horizontal: Double, val vertical: Double)

internal fun CameraSession.computeFieldOfViewDegrees(): FieldOfView {
  val camera = camera ?: return FieldOfView(0.0, 0.0)
  return try {
    val cameraInfo = camera.cameraInfo
    val camera2Info = try {
      Camera2CameraInfo.from(cameraInfo)
    } catch (error: Throwable) {
      Log.w(CameraSession.TAG, "Failed to obtain Camera2 info: ${error.message}")
      return FieldOfView(0.0, 0.0)
    }
    val cameraId = camera2Info.cameraId
    val cameraManager = context.getSystemService(CameraManager::class.java)
      ?: return FieldOfView(0.0, 0.0)
    val characteristics = try {
      cameraManager.getCameraCharacteristics(cameraId)
    } catch (error: CameraAccessException) {
      Log.w(CameraSession.TAG, "Failed to read camera characteristics: ${error.message}")
      return FieldOfView(0.0, 0.0)
    }
    val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
      ?: return FieldOfView(0.0, 0.0)
    val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
      ?: return FieldOfView(0.0, 0.0)
    if (sensorSize.width == 0f || sensorSize.height == 0f || focalLengths.isEmpty()) {
      return FieldOfView(0.0, 0.0)
    }
    val baseFocalLength = focalLengths.minOrNull()?.toDouble() ?: return FieldOfView(0.0, 0.0)
    val zoomRatio = cameraInfo.zoomState.value?.zoomRatio?.toDouble()?.takeIf { it > 0 } ?: 1.0
    val effectiveFocalLength = baseFocalLength * zoomRatio
    val horizontal = 2.0 * Math.toDegrees(atan(sensorSize.width.toDouble() / (2.0 * effectiveFocalLength)))
    val vertical = 2.0 * Math.toDegrees(atan(sensorSize.height.toDouble() / (2.0 * effectiveFocalLength)))
    Log.i(CameraSession.TAG, "Computed Field Of View (zoom=$zoomRatio): h=$horizontal°, v=$vertical°")
    FieldOfView(horizontal, vertical)
  } catch (error: Throwable) {
    Log.w(CameraSession.TAG, "Failed to compute Field Of View! ${error.message}", error)
    val format = configuration?.format
    if (format != null && format.fieldOfView > 0) {
      val aspectRatio = if (format.photoSize.width != 0) {
        format.photoSize.height.toDouble() / format.photoSize.width.toDouble()
      } else {
        0.75
      }
      val horizontal = format.fieldOfView.toDouble()
      val vertical = horizontal * aspectRatio
      FieldOfView(horizontal, vertical)
    } else {
      FieldOfView(0.0, 0.0)
    }
  }
}
