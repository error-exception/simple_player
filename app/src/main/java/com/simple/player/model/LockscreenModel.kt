package com.simple.player.model

import android.view.View
import android.widget.TextView
import com.simple.player.R

class LockscreenModel(private val view: View) {

    private val timeView: TextView = view.findViewById(R.id.lock_screen_time)
    private val dateView: TextView = view.findViewById(R.id.lock_screen_date)
    private val titleView: TextView = view.findViewById(R.id.lock_screen_title)
    private val artistView: TextView = view.findViewById(R.id.lock_screen_artist)

    var title: String = ""
        set(value) {
            field = value
            titleView.text = value
        }

    var artist: String = ""
        set(value) {
            field = value
            artistView.text = value
        }

    var time: String = ""
        set(value) {
            field = value
            timeView.text = value
        }

    var date: String = ""
        set(value) {
            field = value
            dateView.text = value
        }

}