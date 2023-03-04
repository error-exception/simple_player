package com.simple.player.scan

import androidx.compose.runtime.MutableState

class ScanConfigItem(
    val id: Int,
    val value: MutableState<String>,
    val type: Int,
    val isValid: MutableState<Boolean>
) {
    override fun toString(): String {
        return value.value
    }
}