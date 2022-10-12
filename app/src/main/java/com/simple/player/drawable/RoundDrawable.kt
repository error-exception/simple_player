package com.simple.player.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

class RoundDrawable(val bitmap: Bitmap) : Drawable() {
    private val mPaint: Paint
    private val mRectF: RectF
    private val mBitmapWidth: Int
    private val mBitmapHeight: Int
    override fun draw(canvas: Canvas) {
        canvas.drawOval(mRectF, mPaint)
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

    override fun setDither(dither: Boolean) {
        mPaint.isDither = dither
        invalidateSelf()
    }

    init {
        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mRectF = RectF()
        mPaint = Paint()
        with (mPaint) {
            isDither = true
            isAntiAlias = true
            this.shader = shader
        }
        mBitmapWidth = bitmap.width
        mBitmapHeight = bitmap.height
    }
}