package com.simple.player.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.simple.player.R

class CustomListItemModel(private val parent: View) {

    private val nameView: TextView = parent.findViewById(R.id.home_playerlist_item_name)
    private val countView: TextView = parent.findViewById(R.id.home_playerlist_item_song_count)
    private val coverView: ImageView = parent.findViewById(R.id.home_playerlist_item_cover)

    var name: String = ""
        set(value) {
            nameView.text = value
            field = value
        }

    var count: Int = 0
        set(value) {
            countView.text = "共 $value 首"
            field = value
        }

    var removed: Boolean = false

    fun setCover(bitmap: Bitmap?) {
        coverView.setImageBitmap(bitmap)
    }

    fun setCover(drawable: Drawable?) {
        coverView.setImageDrawable(drawable)
    }

    fun setCover(resId: Int) {
        coverView.setImageResource(resId)
    }
}
