package com.mattdangelo.flashpad

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class FlashlightManager private constructor(application: Application) {
    enum class FlashlightState {
        ON,
        OFF
    }

    val currentFlashlightState = FlashlightState.OFF;

    // TODO: Move into constructor and raise error if this isn't available
//    fun isFlashlightAvailable(): Boolean {
//        return application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
//    }

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

    fun setFlashlightState(brightness: Int) {
        if (cameraManager != null && cameraId != null) {
            try {
                if (brightness == 0) {
                    cameraManager!!.setTorchMode(cameraId!!, false)
                }
                else {
                    cameraManager!!.turnOnTorchWithStrengthLevel(cameraId!!, brightness);
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        TODO("Not yet implemented")
    }

    fun initialize() {
        TODO("Not yet implemented")
    }
}
