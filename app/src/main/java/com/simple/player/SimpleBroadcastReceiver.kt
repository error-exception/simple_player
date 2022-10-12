package com.simple.player

import android.content.*

class SimpleBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(p1: Context, p2: Intent) {
        val act = p2.action
        if (act == Intent.ACTION_MEDIA_BUTTON) {
            val intent = Intent(ACTION_MEDIA_BUTTON)
            intent.putExtras(p2.extras!!)
            p1.sendBroadcast(intent)
        }
    }

    companion object {
        const val ACTION_MEDIA_BUTTON = "com.simple.player.action.MEDIA_BUTTON"
    }
}