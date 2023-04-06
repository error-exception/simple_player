package com.simple.player.util

import android.animation.ValueAnimator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

object LegacyAnimation {

    fun floatAnimation(
        initialValue: Float,
        targetValue: Float,
        duration: Long,
        interpolator: Interpolator = LinearInterpolator(),
        onUpdate: ((Float, ValueAnimator) -> Unit)? = null
    ): ValueAnimator {
        val animator = ValueAnimator.ofFloat(initialValue, targetValue)
        animator.duration = duration
        animator.interpolator = interpolator
        animator.addUpdateListener {
            onUpdate?.invoke(it.animatedValue as Float, it)
        }
        return animator
    }

    fun ValueAnimator.close() {
        removeAllUpdateListeners()
        removeAllListeners()
        cancel()
    }

}