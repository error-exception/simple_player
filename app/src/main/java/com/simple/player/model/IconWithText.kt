package com.simple.player.model

class IconWithText(val icon: String, val text: String) {

    override operator fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is IconWithText) {
            return other.icon == icon && other.text == text
        }
        return false
    }

    override fun hashCode(): Int {
        var result = icon.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}