package com.simple.player.model

import com.simple.json.annota.JSONIgnore

class TimingInfo {
    var version: String = ""
    var bpm: Float = 0f
    var offset: Long = 0
    val timingList: ArrayList<TimingItem> = ArrayList()
    /**
     * song id
     */
    var id: Long = -1

    @JSONIgnore
    var isModified = false
}

data class TimingItem(
    val timestamp: Long,
    val isKiai: Boolean
)