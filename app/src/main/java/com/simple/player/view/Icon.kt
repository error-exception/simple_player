package com.simple.player.view

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.simple.player.Store

class Icon : AppCompatTextView {

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attr: AttributeSet?) : super(context!!, attr) {
        init()
    }

    constructor(context: Context?, attr: AttributeSet?, p: Int) : super(context!!, attr, p) {
        init()
    }

    private fun init() {
        typeface = Store.iconTypeface
        gravity = Gravity.CENTER
    }

    var icon: String? = null
        set(value) {
            field = value
            text = value
        }

    var iconSize: Float = 0f
        set(value) {
            field = value
            textSize = value
        }

    var iconColor: Int = Color.BLACK
        set(value) {
            field = value
            setTextColor(value)
        }

}