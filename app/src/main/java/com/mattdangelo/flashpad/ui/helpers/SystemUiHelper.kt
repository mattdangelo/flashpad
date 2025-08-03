package com.mattdangelo.flashpad.ui.helpers

import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun configureSystemUi(activity: ComponentActivity) {
    val window = activity.window

    WindowCompat.setDecorFitsSystemWindows(window, false)

    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.let {
            it.isAppearanceLightStatusBars = false
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT

            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.DarkGray.toArgb()
        }
        insets
    }

    ViewCompat.requestApplyInsets(window.decorView)

    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
}