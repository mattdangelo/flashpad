package com.mattdangelo.flashpad;

import androidx.compose.ui.graphics.Color;

class Utils {
    companion object {
        fun blendColors(color1:Color, color2: Color, ratio: Float): Color {
            val inverseRatio = 1 - ratio
            return Color(
                red = (color1.red * inverseRatio + color2.red * ratio).coerceIn(0f, 1f),
                green = (color1.green * inverseRatio + color2.green * ratio).coerceIn(0f, 1f),
                blue = (color1.blue * inverseRatio + color2.blue * ratio).coerceIn(0f, 1f)
            )
        }
    }
}
