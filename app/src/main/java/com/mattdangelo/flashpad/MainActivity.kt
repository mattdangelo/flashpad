package com.mattdangelo.flashpad

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mattdangelo.flashpad.ui.FlashPad
import com.mattdangelo.flashpad.ui.components.MenuBar
import com.mattdangelo.flashpad.ui.helpers.configureSystemUi
import com.mattdangelo.flashpad.ui.theme.FlashpadTheme

class MainActivity : ComponentActivity() {
    private var redrawState by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureSystemUi(this)

        // Initialize flashlight manager here
        FlashlightManager.getInstance(application)

        setContent {
            var isSosActive by remember { mutableStateOf(false) }

            FlashpadTheme {
                Scaffold(
                    topBar = {
                        MenuBar(
                            isSosActive = isSosActive,
                            onToggleSos = { isSosActive = !isSosActive }
                        )
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier.padding(padding).fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        FlashPad(redrawState)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        redrawState++
        FlashlightManager.getInstance(application).setFlashlightBrightness(0F)
    }

    override fun onStop() {
        super.onStop()
        redrawState++
        FlashlightManager.getInstance(application).setFlashlightBrightness(0F)
    }
}
