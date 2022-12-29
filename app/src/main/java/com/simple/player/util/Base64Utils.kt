package com.simple.player.util

import java.nio.charset.StandardCharsets
import java.util.Base64

object Base64Utils {

    fun decodeToString(bytes: ByteArray): String {
        val b = Base64.getDecoder().decode(bytes)
        return String(b, 0, b.size, StandardCharsets.UTF_8)
    }

    fun decode(s: String): ByteArray {
        val decoder = Base64.getDecoder()
        return decoder.decode(s)
    }


}