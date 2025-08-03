package com.mattdangelo.flashpad.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mattdangelo.flashpad.FlashlightManager
import com.mattdangelo.flashpad.Utils
import kotlinx.coroutines.delay
import androidx.compose.ui.ExperimentalComposeUiApi

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FlashPad(redrawState: Int) {
    val flashlightManager = FlashlightManager.getInstance(LocalContext.current.applicationContext as Application)

    var boxColour by remember { mutableStateOf(Color.DarkGray) }
    var boxSize: Int by remember { mutableIntStateOf(0) }

    LaunchedEffect(redrawState) {
        delay(50)
        boxColour = Color.DarkGray
    }

    fun handlePadAction(event: android.view.MotionEvent): Boolean {
        return when (event.action) {
            android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_MOVE -> {
                val brightness = (boxSize - event.y) / boxSize
                val clampedBrightness = brightness.coerceIn(0f, 1f)

                boxColour = Utils.blendColors(Color.DarkGray, Color.LightGray, clampedBrightness)

                flashlightManager.setFlashlightBrightness(clampedBrightness)
                true
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                boxColour = Color.DarkGray
                flashlightManager.setFlashlightBrightness(0F)
                true
            }
            else -> false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding().fillMaxWidth().height(0.dp))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(boxColour)
                .onSizeChanged { size -> boxSize = size.height }
                .pointerInteropFilter { event -> handlePadAction(event) }
        )
    }
}