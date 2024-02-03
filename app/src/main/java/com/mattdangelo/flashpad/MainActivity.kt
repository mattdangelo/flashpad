package com.mattdangelo.flashpad

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mattdangelo.flashpad.ui.theme.FlashpadTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the status bar colour
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

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // Initialize flashlight manager here
        FlashlightManager.getInstance(application)

        setContent {
            FlashpadTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FlashPad()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        FlashlightManager.getInstance(application).setFlashlightState(0);
    }

    override fun onStop() {
        super.onStop()
        FlashlightManager.getInstance(application).setFlashlightState(0);
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FlashPad() {
    val flashlightManager = FlashlightManager.getInstance(LocalContext.current.applicationContext as Application)

    var boxColour by remember {
        mutableStateOf(if(flashlightManager.currentFlashlightState == FlashlightManager.FlashlightState.ON)
                           Color.LightGray
                       else
                           Color.DarkGray)
    }

    Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Spacer(modifier = Modifier.statusBarsPadding().fillMaxWidth().height(0.dp))
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(boxColour)
            .pointerInteropFilter { event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        boxColour = Color.LightGray;
                        flashlightManager.setFlashlightState(1)
                        true
                    }
                    android.view.MotionEvent.ACTION_UP -> {
                        boxColour = Color.DarkGray;
                        flashlightManager.setFlashlightState(0)
                        true
                    }
                    else -> false
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FlashpadTheme {
        FlashPad()
    }
}