package com.simple

import com.simple.json.JSON

fun main() {
    val timing = TimingItem(
        timestamp = 12,
        isKiai = true
    )
    println(JSON.stringify(timing))
}

data class TimingItem(
    val timestamp: Long,
    val isKiai: Boolean
)
