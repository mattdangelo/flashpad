package com.mattdangelo.flashpad

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class FlashlightManager(private val context: Context) {

    private val cameraManager: CameraManager? by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    private var cameraId: String? = null

    init {
        cameraId = getCameraId()
    }

    private fun getCameraId(): String? {
        val cameraIds = cameraManager?.cameraIdList
        for (id in cameraIds.orEmpty()) {
            val characteristics = cameraManager?.getCameraCharacteristics(id)
            val flashAvailable = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            val lensFacing = characteristics?.get(CameraCharacteristics.LENS_FACING)

            if (flashAvailable && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id
            }
        }
        return null
    }

    fun toggleFlashlight(isOn: Boolean) {
        if (cameraManager != null && cameraId != null) {
            try {
                cameraManager!!.setTorchMode(cameraId!!, isOn)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    fun isFlashlightAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    fun release() {
        TODO("Not yet implemented")
    }

    fun initialize() {
        TODO("Not yet implemented")
    }
}
