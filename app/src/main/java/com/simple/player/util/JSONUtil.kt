package com.simple.player.util

import java.lang.Exception
import java.lang.StringBuilder
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.ArrayList

object JSONUtil {
    private fun <T> objectToJSON(o: T): String {
        val sb = StringBuilder("{")
        try {
            val clz: Class<*> = (o as Any).javaClass
            val methods = clz.declaredMethods
            val getMethodList: MutableList<Method> = ArrayList()
            for (method in methods) {
                if (Modifier.isPublic(method.modifiers) && method.name.startsWith("get")) {
                    getMethodList.add(method)
                }
            }
            for (i in getMethodList.indices) {
                val method = getMethodList[i]
                val name = method.name.substring(3).lowercase()
                sb.append('\"').append(name).append('\"').append(':').append('\"')
                val o1 = method.invoke(o)
                if (o1 != null) {
                    sb.append(o1)
                }
                sb.append('\"')
                if (i != getMethodList.size - 1) {
                    sb.append(',')
                }
            }
            sb.append('}')
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    fun <T> listToJSON(list: MutableList<T>): String {
        val sb = StringBuilder()
        sb.append('[')
        for (i in list.indices) {
            sb.append(objectToJSON(list[i]))
            if (i != list.size - 1) {
                sb.append(',')
            }
        }
        return sb.append(']').toString()
    }
}