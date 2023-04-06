package com.simple.player.util

import java.lang.StringBuilder

object StringUtils {

    fun stringToCode(str: String): String {
        val sb = StringBuilder()
        var i = 0
        for (element in str) {
            sb.append(Integer.toHexString(element.code))
            if (i++ != str.length - 1)
                sb.append('-')
        }
        return sb.toString()
    }

    fun codeToString(code: String): String {
        val sb = StringBuilder()
        val arr = code.split("-").toTypedArray()
        for (str in arr) {
            sb.append(Integer.parseInt(str, 16).toChar())
        }
        return sb.toString()
    }

    fun toInteger(s: String?, i: Int = -1): Int {
        try {
            s ?: return i
            return s.toInt()
        } catch (e: Exception) {
            return i
        }
    }

    fun toLong(s: String?, l: Long = -1L): Long {
        try {
            s ?: return l
            return s.toLong()
        } catch (e: Exception) {
            return l
        }
    }

    fun toFloat(s: String?, i: Float = 0f): Float {
        try {
            s ?: return i
            return s.toFloat()
        } catch (e: Exception) {
            return i
        }
    }

    fun toString(any: Any?, s: String = ""): String {
        any ?: return s
        return any.toString()
    }
}