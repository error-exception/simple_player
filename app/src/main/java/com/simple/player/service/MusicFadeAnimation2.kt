package com.simple.player.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator

class MusicFadeAnimation2 (private val simplePlayer: SimplePlayer): AnimatorListenerAdapter() {

    private var animator: ObjectAnimator? = null
    private var linearInterpolator = LinearInterpolator()
    private var isPlay = false
    private lateinit var end: () -> Unit
    private lateinit var start: () -> Unit

    fun fadeIn(duration: Long) {
        if (animator != null && animator!!.isRunning) {
            return
        }
        isPlay = true
        animator = ObjectAnimator.ofFloat(simplePlayer, "volume", 0F, 1f).apply {
            this.duration = duration
            interpolator = linearInterpolator
            addListener(this@MusicFadeAnimation2)
            start()
        }
    }

    fun fadeOut(duration: Long) {
        if (animator != null && animator!!.isRunning) {
            return
        }
        isPlay = false
        animator = ObjectAnimator.ofFloat(simplePlayer, "volume", 1f, 0F).apply {
            this.duration = duration
            interpolator = linearInterpolator
            addListener(this@MusicFadeAnimation2)
            start()
        }
    }

    override fun onAnimationEnd(animation: Animator) {
        animator?.cancel()
        animator = null
        if (!isPlay) {
            end()
        }
    }

    override fun onAnimationStart(animation: Animator) {
        if (isPlay) {
            start()
        }
    }

    fun onEnd(end: () -> Unit) {
        this.end = end
    }

    fun onStart(start: () -> Unit) {
        this.start = start
    }

}