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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mattdangelo.flashpad.ui.theme.FlashpadTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var redrawState by mutableIntStateOf(0)

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
                    FlashPad(redrawState)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        redrawState++
        FlashlightManager.getInstance(application).setFlashlightBrightness(0F);
    }

    override fun onStop() {
        super.onStop()
        redrawState++
        FlashlightManager.getInstance(application).setFlashlightBrightness(0F);
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FlashPad(redrawState: Int) {
    val flashlightManager = FlashlightManager.getInstance(LocalContext.current.applicationContext as Application)

    var boxColour by remember { mutableStateOf(Color.DarkGray) }
    var boxSize: Int by remember { mutableIntStateOf(0) }

    LaunchedEffect(redrawState) {
        delay(100)
        boxColour = Color.DarkGray
    }

    Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Spacer(modifier = Modifier.statusBarsPadding().fillMaxWidth().height(0.dp))
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(boxColour)
            .onSizeChanged { size -> boxSize = size.height }
            .pointerInteropFilter { event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_MOVE -> {
                        boxColour = Color.LightGray;
                        flashlightManager.setFlashlightBrightness((boxSize - event.y) / boxSize)
                        true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        boxColour = Color.DarkGray;
                        flashlightManager.setFlashlightBrightness(0F)
                        true
                    }
                    else -> false
                }
            }
        )
    }
}
