package com.simple.player.scan

import android.database.Cursor
import android.net.Uri

abstract class MusicScanner {

    protected var onEachMusic: ((Long, Uri, Cursor?, String) -> Unit)? = null
    protected var onComplete: (() -> Unit)? = null

    open val resultCount: Int = 0

    abstract fun scan()

    fun onEach(listener: (Long, Uri, Cursor?, String) -> Unit) {
        this.onEachMusic = listener
    }

    fun onComplete(listener: () -> Unit) {
        onComplete = listener
    }

}