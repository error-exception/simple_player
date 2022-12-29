package com.simple.player.service

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.simple.player.util.AppConfigure


internal class MusicFadeAnimation(private val binder: PlayBinder): Animator.AnimatorListener {

    private var animator: ObjectAnimator? = null
    private var duration = 0L
    private var interpolator = LinearInterpolator()
    private var isPlay = false
    private lateinit var end: () -> Unit
    private lateinit var start: () -> Unit

    fun duration(duration: Long): MusicFadeAnimation {
        this.duration = duration
        return this
    }

    fun fadeIn() {
        if (animator != null && animator!!.isRunning) {
            return
        }
        isPlay = true
        animator = ObjectAnimator.ofFloat(binder, "volume", 0F, 1f)
        animator!!.duration = duration
        animator!!.interpolator = interpolator
        animator!!.addListener(this)
        animator!!.start()
    }

    fun fadeOut() {
        if (animator != null && animator!!.isRunning) {
            return
        }
        isPlay = false
        animator = ObjectAnimator.ofFloat(binder, "volume", 1f, 0F)
        animator!!.duration = duration
        animator!!.interpolator = interpolator
        animator!!.addListener(this)
        animator!!.start()
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

    override fun onAnimationCancel(animation: Animator) {}

    override fun onAnimationRepeat(animation: Animator) {}

    fun onEnd(end: () -> Unit) {
        this.end = end
    }

    fun onStart(start: () -> Unit) {
        this.start = start
    }

}