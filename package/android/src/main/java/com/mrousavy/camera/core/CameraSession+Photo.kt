package com.mrousavy.camera.core

import android.media.AudioManager
import android.util.Log
import com.mrousavy.camera.core.extensions.takePicture
import com.mrousavy.camera.core.types.Flash
import com.mrousavy.camera.core.types.Orientation
import com.mrousavy.camera.core.types.TakePhotoOptions
import com.mrousavy.camera.core.utils.FileUtils
import com.mrousavy.camera.core.utils.PortraitProcessor
import com.mrousavy.camera.core.utils.computeFieldOfViewDegrees

suspend fun CameraSession.takePhoto(options: TakePhotoOptions): Photo {
  val camera = camera ?: throw CameraNotReadyError()
  val configuration = configuration ?: throw CameraNotReadyError()
  val photoConfig = configuration.photo as? CameraConfiguration.Output.Enabled<CameraConfiguration.Photo> ?: throw PhotoNotEnabledError()
  val photoOutput = photoOutput ?: throw PhotoNotEnabledError()

  // Flash
  if (options.flash != Flash.OFF && !camera.cameraInfo.hasFlashUnit()) {
    throw FlashUnavailableError()
  }
  photoOutput.flashMode = options.flash.toFlashMode()
  // Shutter sound
  val enableShutterSound = options.enableShutterSound && !audioManager.isSilent
  // isMirrored (EXIF)
  val isMirrored = photoConfig.config.isMirrored

  // Shoot photo!
  val photoFile = photoOutput.takePicture(
    options.file.file,
    isMirrored,
    enableShutterSound,
    metadataProvider,
    callback,
    CameraQueues.cameraExecutor
  )

  val path = photoFile.uri.path ?: throw InvalidPathError("photo-path-null")
  val portraitResult = PortraitProcessor.enforcePortrait(path)
  val width = portraitResult.width
  val height = portraitResult.height
  val rotation = photoOutput.targetRotation
  val orientation = Orientation.fromSurfaceRotation(rotation)
  val fieldOfView = computeFieldOfViewDegrees()

  Log.i(CameraSession.TAG, "Photo ready at $path -> ${width}x$height, FOV h=${fieldOfView.horizontal}°, v=${fieldOfView.vertical}°")

  return Photo(path, width, height, orientation, isMirrored, fieldOfView.horizontal, fieldOfView.vertical)
}

private val AudioManager.isSilent: Boolean
  get() = ringerMode != AudioManager.RINGER_MODE_NORMAL
