package com.simple.player.json.generator

import android.os.Build
import androidx.annotation.RequiresApi
import com.simple.player.json.annota.JSONAlias
import com.simple.player.json.annota.JSONIgnore
import java.lang.reflect.Method

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


    @RequiresApi(Build.VERSION_CODES.N)
    private fun generateFromObject(obj: Any): String {
        val s = StringBuilder("{")
        val clazz = obj::class.java
        val declaredMethods = clazz.declaredMethods
        for (declaredMethod in declaredMethods) {
            if (declaredMethod.name.endsWith("\$annotations") || !declaredMethod.name.startsWith("get")) {
                continue
            }
            // 跳过含有 JSONIgnore 注解的属性（对于 kotlin）
            if (hasAnnotationForNormalGetter(clazz, declaredMethod, JSONIgnore::class.java)) {
                continue
            }
            val jsonName = if (hasAnnotationForNormalGetter(clazz, declaredMethod, JSONAlias::class.java)) {
                val annotationForNormalMethod =
                    getAnnotationForNormalGetter(clazz, declaredMethod, JSONAlias::class.java)!! as JSONAlias
                val alias = annotationForNormalMethod.alias
                alias
            } else {
                declaredMethod.name.substring(3).lowercase()
            }
            val value = declaredMethod.invoke(obj)
            val jsonValue = toString(value)
            s.append('"').append(jsonName).append('"').append(':')
                .append(jsonValue).append(',')
        }
        s.deleteAt(s.length - 1).append('}')
        return s.toString()
    }

    /**
     * 获取方法中是否有指定类型的注解
     */
    private fun <T: Annotation> hasAnnotationForNormalGetter(clazz: Class<*>, method: Method, annotation: Class<T>): Boolean {
        val withAnnotation = method.name + "\$annotations"
        return try {
            val declaredMethod = clazz.getDeclaredMethod(withAnnotation)
            declaredMethod.isAnnotationPresent(annotation)
        } catch (e: NoSuchMethodException) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun <T: Annotation> getAnnotationForNormalGetter(clazz: Class<*>, method: Method, annotation: Class<T>): Annotation? {
        val withAnnotation = method.name + "\$annotations"
        return try {
            val declaredMethod = clazz.getDeclaredMethod(withAnnotation)
            declaredMethod.getDeclaredAnnotation(annotation)

        } catch (e: NoSuchMethodException) {
            null
        }
    }

    private fun isBaseType(e: Any): Boolean {
        return (e is Int) || (e is Short) || (e is Boolean)
                || (e is Byte) || (e is Long) || (e is Float) || (e is Double)
    }

    fun toString(value: Any?): String {
        return when {
            value == null -> {
                "null"
            }
            isBaseType(value) -> {
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
                //generateFromObject(value)
                ""
            }
        }
    }

}