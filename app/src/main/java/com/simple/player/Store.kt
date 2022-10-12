package com.simple.player

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import com.simple.player.model.Song


object Store {
    lateinit var applicationContext: Context
    lateinit var iconTypeface: Typeface
    var taskId = 0
}