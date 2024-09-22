package com.mattdangelo.flashpad
import android.content.pm.PackageManager
import android.app.Application
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

class FlashlightManagerTest {
    @Test
    fun `raises an exception on init when device does not have a camera flash`() {
        val application = mock(Application::class.java)
        val packageManager = mock(PackageManager::class.java)
        // Expect a device without a flash returns false for PackageManager.FEATURE_CAMERA_FLASH
        `when`(application.packageManager).thenReturn(packageManager)
        `when`(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)).thenReturn(false)
        assertThrows(UnsupportedOperationException::class.java) {
            FlashlightManager.getInstance(application)
        }
    }
}