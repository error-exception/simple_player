package com.simple.player.json.generator

import com.simple.player.json.annota.JSONAlias
import com.simple.player.json.annota.JSONIgnore
import java.lang.reflect.Modifier

internal class JSONGenerator {

    private fun generateFromMap(map: Map<*, *>): String {
        if (map.isEmpty()) {
            return "{}"
        }
        val builder = StringBuilder("{")
        for (entry in map.entries) {
            builder.append(toString(entry.key))
                .append(':')
                .append(toString(entry.value))
                .append(',')
        }
        builder.deleteAt(builder.length - 1).append('}')
        return builder.toString()
    }

    private fun generateFromList(list: List<*>): String {
        if (list.isEmpty()) {
            return "[]"
        }
        val s = StringBuilder("[")
        for (i in list.indices) {
            val element = list[i]
            s.append(toString(element))
                .append(',')
        }
        return s.deleteAt(s.length - 1).append(']').toString()
    }

    private fun generateFromObject(obj: Any): String {
        val s = StringBuilder("{")
        val clazz = obj::class.java
        val declaredFields = clazz.declaredFields
        for (declaredField in declaredFields) {
            val jsonIgnore = declaredField.getAnnotation(JSONIgnore::class.java)
            if (jsonIgnore != null) {
                continue
            }
            val jsonAlias = declaredField.getAnnotation(JSONAlias::class.java)
            val fieldName = declaredField.name
            val jsonField = jsonAlias?.alias ?: fieldName
            val getMethodName = StringBuilder("get").append(fieldName)
            getMethodName.setCharAt("get".length, getMethodName["get".length].uppercaseChar())
            try {
                val getter = clazz.getDeclaredMethod(getMethodName.toString())
                if (!Modifier.isPublic(getter.modifiers)) {
                    continue
                }
                val jsonValue = getter.invoke(obj)
                s.append('"').append(jsonField).append('"').append(':')
                    .append(toString(jsonValue)).append(',')

            } catch (e: NoSuchMethodException) {
                continue
            }
        }
        s.deleteAt(s.length - 1).append('}')
        return s.toString()
    }

    private fun isPrimitiveType(e: Any): Boolean {
        return (e is Int) || (e is Short) || (e is Boolean)
                || (e is Byte) || (e is Long) || (e is Float) || (e is Double)
    }

    fun toString(value: Any?): String {
        return when {
            value == null -> {
                "null"
            }
            isPrimitiveType(value) -> {
                value.toString()
            }
            value is List<*> -> {
                generateFromList(value)
            }
            value is Map<*, *> -> {
                generateFromMap(value)
            }
            value is CharSequence -> {
                """"$value""""
            }
            else -> {
                generateFromObject(value)
            }
        }
    }

}