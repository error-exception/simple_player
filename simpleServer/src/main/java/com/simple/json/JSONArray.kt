package com.simple.json

import java.lang.RuntimeException

class JSONArray {

    internal val list = ArrayList<Any?>()

    operator fun get(index: Int): Any? {
        return list[index]
    }

    fun getInt(index: Int): Int? {
        val value = get(index) ?: return null
        if (value is String) {
            return value.toInt()
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getLong(index: Int): Long? {
        val value = get(index) ?: return null
        if (value is String) {
            return value.toLong()
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getShort(index: Int): Short? {
        val value = get(index) ?: return null
        if (value is String) {
            return value.toShort()
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getByte(index: Int): Byte? {
        val value = get(index) ?: return null
        if (value is String) {
            return value.toByte()
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getBoolean(index: Int): Boolean? {
        val value = get(index) ?: return null
        if (value is String) {
            return value.toBoolean()
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getChar(index: Int): Char? {
        val value = get(index) ?: return null
        if (value is String) {
            return value[0]
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getString(index: Int): String? {
        val value = get(index) ?: return null
        if (value is String) {
            return value
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getJsonObject(index: Int): JSONObject? {
        val value = get(index) ?: return null
        if (value is JSONObject) {
            return value
        }
        throw RuntimeException("value is not a json object")
    }

    fun getJsonArray(index: Int): JSONArray? {
        val value = get(index) ?: return null
        if (value is JSONArray) {
            return value
        }
        throw RuntimeException("value is not a json array")
    }

    fun add(value: Any?) {
        list += value
    }

    operator fun plusAssign(value: Any?) {
        add(value)
    }

    val size: Int
        get() = list.size

    override fun toString(): String {
        return list.toString()
    }

}