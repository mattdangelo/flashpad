package com.mattdangelo.flashpad

import android.os.Bundle
import android.view.RoundedCorner
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.mattdangelo.flashpad.ui.theme.FlashpadTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Morse code SOS timings in milliseconds, alternating flash on / flash off
private const val SOS_UNIT = 200L
private val SOS_PATTERN = listOf(
    // S
    1, 1, 1, 1, 1, 3,
    // O
    3, 1, 3, 1, 3, 3,
    // S, followed by the gap before the pattern repeats
    1, 1, 1, 1, 1, 7
).map { it * SOS_UNIT }

class MainActivity : ComponentActivity() {
    private val flashlightManager by lazy { FlashlightManager.getInstance(application) }

    // Brightness requested by the pad, 0 while it isn't being pressed
    private var padBrightness by mutableFloatStateOf(0F)
    private var sosActive by mutableStateOf(false)
    private var sosFlashOn by mutableStateOf(false)
    private var sosJob: Job? = null

    // SOS flashes take precedence over the pad
    private val flashlightBrightness: Float
        get() = if (sosFlashOn) flashlightManager.defaultFlashlightBrightness else padBrightness

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            FlashpadTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FlashPad(
                        brightness = flashlightBrightness,
                        sosActive = sosActive,
                        onPadBrightnessChange = ::updatePadBrightness,
                        onSosToggle = ::toggleSos
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopSos()
        updatePadBrightness(0F)
    }

    private fun updateFlashlight() {
        flashlightManager.setFlashlightBrightness(flashlightBrightness)
    }

    private fun updatePadBrightness(brightness: Float) {
        padBrightness = brightness
        updateFlashlight()
    }

    private fun toggleSos() {
        if (sosActive) stopSos() else startSos()
    }

    private fun startSos() {
        sosActive = true
        sosJob = lifecycleScope.launch {
            while (true) {
                for ((index, duration) in SOS_PATTERN.withIndex()) {
                    sosFlashOn = index % 2 == 0
                    updateFlashlight()
                    delay(duration)
                }
            }
        }
    }

    private fun stopSos() {
        if (!sosActive) return
        sosJob?.cancel()
        sosJob = null
        sosActive = false
        sosFlashOn = false
        // Hand the flashlight back to the pad
        updateFlashlight()
    }
}

private val PAD_MARGIN = 22.dp
private val PAD_MIN_CORNER_RADIUS = 10.dp

// Rounds the pad's corners to follow the display's rounded corners, inset by the pad margin
@Composable
private fun rememberPadShape(): Shape {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    return remember(context, density, configuration) {
        val display = context.display

        fun cornerRadius(position: Int): Dp {
            val displayRadius = with(density) { (display?.getRoundedCorner(position)?.radius ?: 0).toDp() }
            return (displayRadius - PAD_MARGIN).coerceAtLeast(PAD_MIN_CORNER_RADIUS)
        }

        AbsoluteRoundedCornerShape(
            topLeft = cornerRadius(RoundedCorner.POSITION_TOP_LEFT),
            topRight = cornerRadius(RoundedCorner.POSITION_TOP_RIGHT),
            bottomRight = cornerRadius(RoundedCorner.POSITION_BOTTOM_RIGHT),
            bottomLeft = cornerRadius(RoundedCorner.POSITION_BOTTOM_LEFT)
        )
    }
}

@Composable
fun FlashPad(
    brightness: Float,
    sosActive: Boolean,
    onPadBrightnessChange: (Float) -> Unit,
    onSosToggle: () -> Unit
) {
    val currentOnPadBrightnessChange by rememberUpdatedState(onPadBrightnessChange)

    Box(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(PAD_MARGIN)
        .clip(rememberPadShape())
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(lerp(Color.DarkGray, Color.LightGray, brightness))
            .pointerInput(Unit) {
                // The higher up the pad is pressed, the brighter the flashlight
                fun brightnessAt(change: PointerInputChange) =
                    ((size.height - change.position.y) / size.height).coerceIn(0F, 1F)

                awaitEachGesture {
                    val down = awaitFirstDown()
                    try {
                        var change: PointerInputChange? = down
                        while (change != null && change.pressed) {
                            currentOnPadBrightnessChange(brightnessAt(change))
                            change.consume()
                            change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                        }
                    } finally {
                        currentOnPadBrightnessChange(0F)
                    }
                }
            }
        )
        Text(
            text = stringResource(R.string.sos),
            color = if (sosActive) Color.White else Color.LightGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSosToggle
                )
                .padding(start = 24.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        )
    }
}
