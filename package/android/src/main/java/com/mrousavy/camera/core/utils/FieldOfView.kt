package com.mrousavy.camera.core.utils

import android.hardware.camera2.CameraCharacteristics
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
    val camera2Info = Camera2CameraInfo.from(cameraInfo)
    val characteristics = camera2Info.cameraCharacteristics
    val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) as? SizeF
      ?: return FieldOfView(0.0, 0.0)
    val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
      ?: return FieldOfView(0.0, 0.0)
    if (sensorSize.width == 0f || sensorSize.height == 0f || focalLengths.isEmpty()) {
      return FieldOfView(0.0, 0.0)
    }
    val baseFocalLength = focalLengths.minOrNull()?.toDouble() ?: return FieldOfView(0.0, 0.0)
    val zoomRatio = cameraInfo.zoomState.value?.zoomRatio?.toDouble() ?: 1.0
    val effectiveFocalLength = baseFocalLength * zoomRatio
    val horizontal = 2.0 * Math.toDegrees(atan(sensorSize.width.toDouble() / (2.0 * effectiveFocalLength)))
    val vertical = 2.0 * Math.toDegrees(atan(sensorSize.height.toDouble() / (2.0 * effectiveFocalLength)))
    Log.i(CameraSession.TAG, "Computed Field Of View (zoom=$zoomRatio): h=$horizontal°, v=$vertical°")
    FieldOfView(horizontal, vertical)
  } catch (error: Throwable) {
    Log.w(CameraSession.TAG, "Failed to compute Field Of View! ${error.message}", error)
    FieldOfView(0.0, 0.0)
  }
}
