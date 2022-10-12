/*
 * Copyright (C) 2014-2016 Qiujuer <qiujuer@live.cn>
 * WebSite http://www.qiujuer.net
 * Created 05/28/2015
 * Changed 05/29/2016
 * Version 2.0.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.simple.player.util.blur

import android.graphics.Bitmap
import java.lang.NullPointerException
import java.lang.RuntimeException
import kotlin.Throws

/**
 * This is blur image class
 *
 * Blur arithmetic is StackBlur
 */
/*
object StackBlur {
    private fun buildBitmap(original: Bitmap?, canReuseInBitmap: Boolean): Bitmap {
        // First we should check the original
        if (original == null) throw NullPointerException("Blur bitmap original isn't null")
        val config = original.config
        if (config != Bitmap.Config.ARGB_8888 && config != Bitmap.Config.RGB_565) {
            throw RuntimeException("Blur bitmap only supported Bitmap.Config.ARGB_8888 and Bitmap.Config.RGB_565.")
        }

        // If can reuse in bitmap return this or copy
        val rBitmap: Bitmap
        rBitmap = if (canReuseInBitmap) {
            original
        } else {
            original.copy(config, true)
        }
        return rBitmap
    }

    /**
     * StackBlur By Java Bitmap
     *
     * @param original         Original Image
     * @param radius           Blur radius
     * @param canReuseInBitmap Can reuse In original Bitmap
     * @return Image Bitmap
     */
    fun blur(original: Bitmap?, radius: Int, canReuseInBitmap: Boolean): Bitmap? {
        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
        if (radius < 1) {
            return null
        }
        val bitmap = buildBitmap(original, canReuseInBitmap)

        // Return this none blur
        if (radius == 1) {
            return bitmap
        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        // get array
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        // run Blur
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = ShortArray(wh)
        val g = ShortArray(wh)
        val b = ShortArray(wh)
        var rSum: Int
        var gSum: Int
        var bSum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vMin = IntArray(Math.max(w, h))
        var divSum = div + 1 shr 1
        divSum *= divSum
        val dv = ShortArray(256 * divSum)
        i = 0
        while (i < 256 * divSum) {
            dv[i] = (i / divSum).toShort()
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackPointer: Int
        var stackStart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routSum: Int
        var goutSum: Int
        var boutSum: Int
        var rinSum: Int
        var ginSum: Int
        var binSum: Int
        y = 0
        while (y < h) {
            bSum = 0
            gSum = bSum
            rSum = gSum
            boutSum = rSum
            goutSum = boutSum
            routSum = goutSum
            binSum = routSum
            ginSum = binSum
            rinSum = ginSum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rSum += sir[0] * rbs
                gSum += sir[1] * rbs
                bSum += sir[2] * rbs
                if (i > 0) {
                    rinSum += sir[0]
                    ginSum += sir[1]
                    binSum += sir[2]
                } else {
                    routSum += sir[0]
                    goutSum += sir[1]
                    boutSum += sir[2]
                }
                i++
            }
            stackPointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rSum]
                g[yi] = dv[gSum]
                b[yi] = dv[bSum]
                rSum -= routSum
                gSum -= goutSum
                bSum -= boutSum
                stackStart = stackPointer - radius + div
                sir = stack[stackStart % div]
                routSum -= sir[0]
                goutSum -= sir[1]
                boutSum -= sir[2]
                if (y == 0) {
                    vMin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vMin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinSum += sir[0]
                ginSum += sir[1]
                binSum += sir[2]
                rSum += rinSum
                gSum += ginSum
                bSum += binSum
                stackPointer = (stackPointer + 1) % div
                sir = stack[stackPointer % div]
                routSum += sir[0]
                goutSum += sir[1]
                boutSum += sir[2]
                rinSum -= sir[0]
                ginSum -= sir[1]
                binSum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bSum = 0
            gSum = bSum
            rSum = gSum
            boutSum = rSum
            goutSum = boutSum
            routSum = goutSum
            binSum = routSum
            ginSum = binSum
            rinSum = ginSum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi].toInt()
                sir[1] = g[yi].toInt()
                sir[2] = b[yi].toInt()
                rbs = r1 - Math.abs(i)
                rSum += r[yi] * rbs
                gSum += g[yi] * rbs
                bSum += b[yi] * rbs
                if (i > 0) {
                    rinSum += sir[0]
                    ginSum += sir[1]
                    binSum += sir[2]
                } else {
                    routSum += sir[0]
                    goutSum += sir[1]
                    boutSum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackPointer = radius
            y = 0
            while (y < h) {

                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] =
                    -0x1000000 and pix[yi] or (dv[rSum] shl 16) or (dv[gSum] shl 8) or dv[bSum]
                        .toInt()
                rSum -= routSum
                gSum -= goutSum
                bSum -= boutSum
                stackStart = stackPointer - radius + div
                sir = stack[stackStart % div]
                routSum -= sir[0]
                goutSum -= sir[1]
                boutSum -= sir[2]
                if (x == 0) {
                    vMin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vMin[y]
                sir[0] = r[p].toInt()
                sir[1] = g[p].toInt()
                sir[2] = b[p].toInt()
                rinSum += sir[0]
                ginSum += sir[1]
                binSum += sir[2]
                rSum += rinSum
                gSum += ginSum
                bSum += binSum
                stackPointer = (stackPointer + 1) % div
                sir = stack[stackPointer]
                routSum += sir[0]
                goutSum += sir[1]
                boutSum += sir[2]
                rinSum -= sir[0]
                ginSum -= sir[1]
                binSum -= sir[2]
                yi += w
                y++
            }
            x++
        }

        // set Bitmap
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}*/