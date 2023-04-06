package com.simple.player.json

import java.lang.RuntimeException

class JSONObject internal constructor(val map: Map<*, *>) {

    fun getBoolean(propertyExpression: String): Boolean? {
        val value = getValue(propertyExpression)
        value ?: return null
        if (value is String) {
            return value.toBooleanStrictOrNull()
        }
        throw RuntimeException("value is Object or Array")
    }


    fun getString(propertyExpression: String): String? {
        val value = getValue(propertyExpression)
        value ?: return null
        if (value is String) {
            return value
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getInt(propertyExpression: String): Int? {
        val value = getValue(propertyExpression)
        value ?: return null
        if (value is String) {
            return value.toIntOrNull(10)
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getLong(propertyExpression: String): Long? {
        val value = getValue(propertyExpression)
        value ?: return null
        if (value is String) {
            return value.toLongOrNull(10)
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getFloat(propertyExpression: String): Float? {
        val value = getValue(propertyExpression)
        value ?: return null
        if (value is String) {
            return value.toFloatOrNull()
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getDouble(propertyExpression: String): Double? {
        val value = getValue(propertyExpression)
        value ?: return null
        if (value is String) {
            return value.toDoubleOrNull()
        }
        throw RuntimeException("value is Object or Array")
    }

    fun getObjectMap(propertyExpression: String = ""): JSONObject? {
        if (propertyExpression.isEmpty()) return JSONObject(map)
        val value = getValue(propertyExpression)
        value ?: return null
        if (value is Map<*, *>) {
            return JSONObject(value)
        }
        throw RuntimeException("value is not a Object")
    }

    fun getArrayList(propertyExpression: String): JSONArray? {
        val value = getValue(propertyExpression)
        value ?: return null
        if (value is List<*>) {
            return JSONArray(value)
        }
        throw RuntimeException("value is not a Array")
    }

    private fun getValue(property: String): Any? {
        val parse = ExpressionParser().parse(property)
        var value: Any? = map
        for (s in parse) {
            value ?: return null
            value = if (value is List<*>) {
                value[s.toInt()]
            } else {
                val t = value as Map<*, *>
                t[s]
            }
        }
        if (value is String && value == "null") {
            return null
        }
        return value
    }

    override fun toString(): String {
        return map.toString()
    }
}