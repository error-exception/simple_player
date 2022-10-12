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

}