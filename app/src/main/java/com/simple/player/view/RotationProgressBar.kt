package com.simple.player.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.simple.player.R
import com.simple.player.Util

class RotationProgressBar : View {

    private lateinit var paint : Paint
    private lateinit var f : RectF
    private var strokeWidth : Float = 8f
    private var mBarColor: Int = -0x23d3e1
    private lateinit var animator : ObjectAnimator

    constructor(context: Context):super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        var ta = context.obtainStyledAttributes(attributeSet, R.styleable.RotationProgressBar)
        mBarColor = ta.getColor(R.styleable.RotationProgressBar_bar_color, -0x23d3e1)
        strokeWidth = ta.getDimension(R.styleable.RotationProgressBar_bar_width, Util.dpToPx(4f).toFloat())
        ta.recycle()
        init()
    }

    constructor(context : Context, attr : AttributeSet, p : Int) : super(context, attr, p) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint.setColor(Color.BLUE)
        paint.isAntiAlias = true
        paint.isDither = true
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE

        f = RectF()

        animator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f)
        animator.duration = 2000
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ObjectAnimator.INFINITE

    }

    override fun onDraw(canvas: Canvas?) {
        var height = this.height
        var width = this.width
        paint.color = mBarColor
        f.set(strokeWidth, strokeWidth, height - strokeWidth, width - strokeWidth)
        canvas?.drawArc(f, 0f, 90f, false, paint)
        canvas?.drawArc(f, 180f, 90f, false, paint)
        super.onDraw(canvas)
    }

    fun start() {
        if (!animator.isStarted) {
            animator.start()
        }
    }

    fun stop() {
        if (animator.isStarted || animator.isRunning) {
            animator.end()
            animator.cancel()
        }
    }

}