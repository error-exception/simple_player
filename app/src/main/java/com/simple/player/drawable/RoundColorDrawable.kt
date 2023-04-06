package com.simple.player.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

class RoundColorDrawable(val colour: Int, val radius: Float = 1f): Drawable() {

    private val mPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        color = colour
    }
    private val mRectF = RectF()

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(mRectF, radius, radius, mPaint)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mRectF.set(bounds)
    }

    override fun setAlpha(alpha: Int) {
        if (mPaint.alpha != alpha) {
            mPaint.alpha = alpha
            invalidateSelf()
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }


}