package com.simple.player.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

class RoundBitmapDrawable2(val bitmap: Bitmap, val radius: Float = 1f) : Drawable() {

    private val mPaint: Paint = Paint().apply {
        isDither = true
        isAntiAlias = true
        this.shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }
    private val mRectF: RectF = RectF()
    private val mBitmapWidth: Int = bitmap.width
    private val mBitmapHeight: Int = bitmap.height

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

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    fun setAntiAlias(aa: Boolean) {
        mPaint.isAntiAlias = aa
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun setDither(dither: Boolean) {
        mPaint.isDither = dither
        invalidateSelf()
    }

}