package com.mrousavy.camera.core.utils

import android.util.Log
import androidx.exifinterface.media.ExifInterface

object ExifUtils {
  private const val TAG = "ExifUtils"

  private val FALLBACK_TAGS = arrayOf(
    ExifInterface.TAG_DATETIME,
    ExifInterface.TAG_DATETIME_ORIGINAL,
    ExifInterface.TAG_DATETIME_DIGITIZED,
    ExifInterface.TAG_SUBSEC_TIME,
    ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
    ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
    ExifInterface.TAG_MAKE,
    ExifInterface.TAG_MODEL,
    ExifInterface.TAG_SOFTWARE,
    ExifInterface.TAG_EXPOSURE_TIME,
    ExifInterface.TAG_EXPOSURE_PROGRAM,
    ExifInterface.TAG_EXPOSURE_MODE,
    ExifInterface.TAG_F_NUMBER,
    ExifInterface.TAG_APERTURE_VALUE,
    ExifInterface.TAG_FLASH,
    ExifInterface.TAG_FOCAL_LENGTH,
    ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
    ExifInterface.TAG_WHITE_BALANCE,
    ExifInterface.TAG_COLOR_SPACE,
    ExifInterface.TAG_SCENE_TYPE,
    ExifInterface.TAG_IMAGE_DESCRIPTION,
    ExifInterface.TAG_COPYRIGHT,
    ExifInterface.TAG_USER_COMMENT,
    ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
    ExifInterface.TAG_ISO_SPEED_RATINGS,
    ExifInterface.TAG_METERING_MODE,
    ExifInterface.TAG_GPS_LATITUDE,
    ExifInterface.TAG_GPS_LATITUDE_REF,
    ExifInterface.TAG_GPS_LONGITUDE,
    ExifInterface.TAG_GPS_LONGITUDE_REF,
    ExifInterface.TAG_GPS_ALTITUDE,
    ExifInterface.TAG_GPS_ALTITUDE_REF,
    ExifInterface.TAG_GPS_TIMESTAMP,
    ExifInterface.TAG_GPS_DATESTAMP
  )

  fun copyAttributes(from: ExifInterface, to: ExifInterface) {
    try {
      val method = ExifInterface::class.java.getMethod("copyTo", ExifInterface::class.java)
      method.isAccessible = true
      method.invoke(from, to)
      return
    } catch (ignored: Throwable) {
      Log.w(TAG, "ExifInterface.copyTo() not available, copying fallback tags...")
    }

    FALLBACK_TAGS.forEach { tag ->
      val value = from.getAttribute(tag)
      if (value != null) {
        to.setAttribute(tag, value)
      }
    }
  }
}
