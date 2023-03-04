package com.simple.player.util

import android.graphics.Color

object ColorUtil {

    fun getColorByName(colorName: String): Int {
        return when (colorName.lowercase()) {
            "red" -> Color.RED
            "yellow" -> Color.YELLOW
            "blue" -> Color.BLUE
            "green" -> Color.GREEN
            "black" -> Color.BLACK
            "white" -> Color.WHITE
            "gray" -> Color.GRAY
            "cyan" -> Color.CYAN
            "ltgray" -> Color.LTGRAY
            "dkgray" -> Color.DKGRAY
            "transparent" -> Color.TRANSPARENT
            else -> Color.TRANSPARENT
        }
    }

    fun isColorString(colorString: String): Boolean {
        if (colorString[0] != '#') {
            return false
        }
        for (i in 1 until colorString.length) {
            val c = colorString[i]
            if (!isHexChar(c)) {
                return false
            }
        }
        return true
    }

    private fun isHexChar(c: Char): Boolean {
        return c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'
    }

    fun toComposeColor(androidColor: Int): androidx.compose.ui.graphics.Color {
        return androidx.compose.ui.graphics.Color(
            alpha = android.graphics.Color.alpha(androidColor),
            blue = android.graphics.Color.blue(androidColor),
            red = android.graphics.Color.red(androidColor),
            green = android.graphics.Color.green(androidColor),
        )
    }

    fun toAndroidColorInt(composeColor: androidx.compose.ui.graphics.Color): Int {
        return (composeColor.alpha * 255.0f + 0.5f).toInt() shl 24 or
                ((composeColor.red * 255.0f + 0.5f).toInt() shl 16) or
                ((composeColor.green * 255.0f + 0.5f).toInt() shl 8) or
                (composeColor.blue * 255.0f + 0.5f).toInt()
    }

}