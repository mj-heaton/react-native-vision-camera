package com.mrousavy.camera.core.utils

import android.util.Size
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val MIN_DIMENSION = 1

internal fun Size.clampToMegapixels(maxPixels: Long): Size {
  if (maxPixels <= 0) return this
  val currentPixels = width.toLong() * height.toLong()
  if (currentPixels <= maxPixels) return this
  val scale = sqrt(maxPixels.toDouble() / currentPixels.toDouble())
  val clampedWidth = max(MIN_DIMENSION, (width * scale).roundToInt())
  val clampedHeight = max(MIN_DIMENSION, (height * scale).roundToInt())
  return Size(clampedWidth, clampedHeight)
}
