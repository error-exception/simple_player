package com.simple.player.lyrics

import android.animation.TimeAnimator
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.simple.player.service.SimplePlayer

class LyricsBooster(private val activity: ComponentActivity): DefaultLifecycleObserver {

    private val timeAnimator = TimeAnimator().apply {
        setTimeListener { animation, totalTime, deltaTime ->
            lrc?.let {
                val list = it.lrcLineList
                var i = list.size - 1
                val current = com.simple.player.service.SimplePlayer.current.toLong()
                while (i >= 0) {
                    val lyricsWord = list[i]
                    if (lyricsWord.startTime < current) {
                        activeLine?.value = i
                        break
                    }
                    i--
                }
            }
        }
    }

    private var lrc: Lrc? = null

    private var activeLine: MutableState<Int>? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    fun setLyrics(lrc: Lrc?): LyricsBooster {
        this.lrc = lrc
        return this
    }

    fun setActiveLineTarget(activeLine: MutableState<Int>) {
        this.activeLine = activeLine
    }

    fun start() {
        if (!timeAnimator.isStarted) {
            timeAnimator.start()
        } else if (timeAnimator.isPaused) {
            timeAnimator.resume()
        }
    }

    fun pause() {
        if (!timeAnimator.isStarted) {
            return
        }
        timeAnimator.pause()
    }

    fun cancel() {
        timeAnimator.removeAllListeners()
        timeAnimator.cancel()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        activity.lifecycle.removeObserver(this)
        timeAnimator.cancel()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        timeAnimator.pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        timeAnimator.resume()
    }


}