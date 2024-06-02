package com.mattdangelo.flashpad

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class FlashlightManager private constructor(application: Application) {
    enum class FlashlightState { ON, OFF }

    private var currentFlashlightState = FlashlightState.OFF
    private var maxFlashlightStrength = 0;
    private var cameraId: String? = null

    companion object {
        @Volatile
        private var instance: FlashlightManager? = null

        fun getInstance(application: Application): FlashlightManager {
            return instance ?: synchronized(this) {
                instance ?: FlashlightManager(application).also { instance = it }
            }
        }
    }

    private val cameraManager: CameraManager? by lazy {
        application.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    private fun getCameraId(): String? {
        val cameraIds = cameraManager?.cameraIdList
        for (id in cameraIds.orEmpty()) {
            val characteristics = cameraManager?.getCameraCharacteristics(id)
            val flashAvailable = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            val lensFacing = characteristics?.get(CameraCharacteristics.LENS_FACING)

            // Which we have the camera characteristics loaded, store the max brightness level
            maxFlashlightStrength = characteristics?.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1

            if (flashAvailable && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id
            }
        }
        return null
    }

    init {
        // Check if the device has a camera flash feature
        if (!application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            throw UnsupportedOperationException("This device does not have a camera flash.")
        }

        cameraId = getCameraId()
    }

    fun setFlashlightBrightness(brightness: Float) {
        if (brightness < 0F || brightness > 1F) {
            // TODO: See when this happens and prevent it
            // throw IllegalArgumentException("Brightness must be between 0 and 1")
        }

        if (cameraManager != null && cameraId != null) {
            try {
                val normalizedBrightness = (brightness * maxFlashlightStrength).toInt()
                if (normalizedBrightness <= 0) {
                    cameraManager!!.setTorchMode(cameraId!!, false)
                    currentFlashlightState = FlashlightState.OFF
                }
                else {
                    cameraManager!!.turnOnTorchWithStrengthLevel(cameraId!!, normalizedBrightness);
                    currentFlashlightState = FlashlightState.ON
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }
}
