package com.mattdangelo.flashpad

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import kotlin.math.ceil

class FlashlightManager private constructor(application: Application) {
    init {
        // Check if the device has a camera flash feature
        if (!application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            throw UnsupportedOperationException("This device does not have a camera flash.")
        }
    }

    companion object {
        private const val TAG = "FlashlightManager"

        @Volatile
        private var instance: FlashlightManager? = null

        fun getInstance(application: Application): FlashlightManager {
            return instance ?: synchronized(this) {
                instance ?: FlashlightManager(application).also { instance = it }
            }
        }
    }

    private val cameraManager = application.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var maxFlashlightStrength = 1
    private var defaultFlashlightStrength = 1
    private val cameraId: String? = findBackCameraWithFlash()

    // The device's default torch brightness, normalized to 0-1
    val defaultFlashlightBrightness: Float
        get() = defaultFlashlightStrength.toFloat() / maxFlashlightStrength

    private fun findBackCameraWithFlash(): String? {
        for (id in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)

            if (flashAvailable && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                // Devices without brightness control report a single strength level
                maxFlashlightStrength = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                defaultFlashlightStrength = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL) ?: 1
                return id
            }
        }
        return null
    }

    // Sets the flashlight brightness from 0 (off) to 1 (maximum strength)
    fun setFlashlightBrightness(brightness: Float) {
        val id = cameraId ?: return
        // Round up so that any brightness above 0 turns the flashlight on
        val strength = ceil(brightness.coerceIn(0F, 1F) * maxFlashlightStrength).toInt()

        try {
            if (strength == 0) {
                cameraManager.setTorchMode(id, false)
            } else {
                cameraManager.turnOnTorchWithStrengthLevel(id, strength)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Unable to set the flashlight brightness", e)
        }
    }
}
