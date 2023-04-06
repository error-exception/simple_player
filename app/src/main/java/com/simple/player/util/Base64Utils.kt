package com.simple.player.util

import android.util.Base64
import java.nio.charset.StandardCharsets

object Base64Utils {

    fun decodeToString(bytes: ByteArray): String {
        val b = Base64.decode(bytes, Base64.DEFAULT)
        return String(b, 0, b.size, StandardCharsets.UTF_8)
    }

    fun decode(s: String): ByteArray {
        return Base64.decode(s, Base64.DEFAULT)
    }


}