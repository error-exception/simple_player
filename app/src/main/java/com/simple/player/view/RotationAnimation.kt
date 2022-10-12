package com.simple.player.view

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator

class RotationAnimation(target: View?, count: Int, duration: Long) {
    private var anim: ObjectAnimator = ObjectAnimator.ofFloat(target, "rotation", 0f, 360f)
    fun start() {
        if (anim.isStarted) {
            anim.resume()
        } else {
            anim.start()
        }
    }

    fun restart() {
        if (anim.isRunning || anim.isPaused || anim.isStarted) {
            anim.end()
        }
        anim.start()
    }

    fun pause() {
        if ((anim.isRunning || anim.isStarted) && !anim.isPaused) {
            anim.pause()
        }
    }

    fun stop() {
        if (anim.isRunning || anim.isStarted) {
            anim.end()
        }
    }

    //一旦取消无法再次使用
    fun cancel() {
        stop()
        anim.cancel()
    }

    init {
        anim.duration = duration
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = count
    }
}