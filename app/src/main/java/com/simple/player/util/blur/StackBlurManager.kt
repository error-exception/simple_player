/**
 * StackBlur v1.0 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez></eenriquelopez>@gmail.com>
 * http://www.lopez-manas.com
 *
 * Author of the original algorithm: Mario Klingemann <mario.quasimondo.com>
 *
 * This is a compromise between Gaussian Blur and Box blur
 * It creates much better looking blurs than Box Blur, but is
 * 7x faster than my Gaussian Blur implementation.
 *
 * I called it Stack Blur because this describes best how this
 * filter works internally: it creates a kind of moving stack
 * of colors whilst scanning through the image. Thereby it
 * just has to add one new block of color to the right side
 * of the stack and remove the leftmost color. The remaining
 * colors on the topmost layer of the stack are either added on
 * or reduced by one, depending on if they are on the right or
 * on the left side of the stack.
 *
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
</mario.quasimondo.com> */
//package com.enrique.stackblur;
package com.simple.player.util.blur

import android.graphics.Bitmap
import java.io.FileOutputStream
import java.lang.Exception
import java.util.concurrent.Executors

class StackBlurManager(val image: Bitmap) {

    private lateinit var _result: Bitmap
    private val _blurProcess: BlurProcess

    fun process(radius: Int): Bitmap {
        _result = _blurProcess.blur(image, radius.toFloat())!!
        return _result
    }

    fun returnBlurredImage(): Bitmap {
        return _result
    }

    fun saveIntoFile(path: String?) {
        try {
            val out = FileOutputStream(path)
            _result.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors()
        val EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS)
    }

    init {
        _blurProcess = JavaBlurProcess()
    }
}