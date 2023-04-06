package com.simple.player.util

import android.graphics.Bitmap
import com.simple.player.util.blur.StackBlurManager

object BlurUtils {

    fun blur(src: Bitmap, radius: Int, recycleOrigin: Boolean = true): Bitmap {
        val mBlurManager = StackBlurManager(src)
        mBlurManager.process(radius)
        val t: Bitmap = mBlurManager.returnBlurredImage()
        if (recycleOrigin)
            mBlurManager.image.recycle()
        return t
    }

}