package com.simple.player.json

class JSONArray internal constructor(val list: List<*>) {

    operator fun get(index: Int): Any? {
        return list[index]
    }

    val size: Int
        get() = list.size

    override fun toString(): String {
        return list.toString()
    }

}