package com.simple.json

import com.simple.json.generator.JSONGenerator
import com.simple.json.parser.JSONArrayParser
import com.simple.json.parser.JSONObjectParser

object JSON {

    fun parseJSONArray(jsonString: String): JSONArray {
        for (c in jsonString) {
            if (c == '[') {
                val list = JSONArrayParser().parse(jsonString, 0)
                return JSONArray(list)
            }
        }
        return JSONArray(ArrayList<Any>())
    }

    fun parseJSONObject(jsonString: String): JSONObject {
        for (c in jsonString) {
            if (c == '{') {
                val map = JSONObjectParser().parse(jsonString, 0)
                return JSONObject(map)
            }
        }
        return JSONObject(HashMap<Any, Any>())
    }

    fun stringify(value: Any): String {
        val generator = JSONGenerator()
        return when (value) {
            is JSONObject -> {
                generator.toString(value.map)
            }
            is JSONArray -> {
                generator.toString(value.list)
            }
            else -> {
                generator.toString(value)
            }
        }
    }

}