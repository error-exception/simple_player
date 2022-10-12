package com.simple.player.util

import android.content.ContentUris
import android.content.Context
import android.graphics.*
import android.icu.text.DateTimePatternGenerator
import android.net.Uri
import android.widget.ImageView
import com.simple.player.Util
import kotlin.math.roundToInt

/**
 * 包含了一些对 Bitmap 进行简单处理的方法
 */
object BitmapUtil {

    /**
     * 此方法可以裁剪图片，裁剪所得的图片源自于图片的中央
     * @param src 原位图
     * @param outWidth 裁剪所得图片的宽
     * @param outHeight 裁剪所得图片的高
     * @return 裁剪所得的图片
     */
    fun cropBitmap(src: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val h = src.height
        val w = src.width
        val offsetX = (w - outWidth) shr  1
        val offsetY = (h - outHeight) shr 1
        val a = Bitmap.createBitmap(src, offsetX, offsetY, outWidth, outHeight)
        if (a != src) src.recycle()
        return a
    }

    /**
     * 以长宽之比裁剪图片，裁剪所得图片源自于图片中央，且所得的宽和高至少有一个与原图片相同，注意，在使用时，务必在比例后加f或F，以表示为float数据类型
     * @param src 源位图
     * @param aspectX 横向比例
     * @param aspectY 纵向比例
     * @return 裁剪之后的位图
     */
    fun cropBitmapAspect(src: Bitmap, aspectX: Float, aspectY: Float): Bitmap {
        val h = src.height
        val w = src.width
        var newHeight = w / aspectX * aspectY
        var newWidth = w.toFloat()
        if (newHeight > h) {
            newWidth = h / aspectY * aspectX
            newHeight = h.toFloat()
        }
        return cropBitmap(src, newWidth.roundToInt(), newHeight.roundToInt())
    }

    /**
     * 设置 ImageView 图片的亮度
     * @param img 目标 ImageView
     * @param value 0-1之间的 float 数
     */
    fun setLum(img: ImageView, value: Float) {
        val matrix = ColorMatrix()
        matrix.setScale(value, value, value, 1f)
        img.colorFilter = ColorMatrixColorFilter(matrix)
    }

    private val sBitmapOptionsCache = BitmapFactory.Options()
    fun getBitmapQuick(src: ByteArray?, w: Int, h: Int): Bitmap? {
        if (src == null) return null
        var sampleSize = 1
        sBitmapOptionsCache.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(
            src, 0, src.size, sBitmapOptionsCache)
        var nextWidth = sBitmapOptionsCache.outWidth shr 1
        var nextHeight = sBitmapOptionsCache.outHeight shr 1
        while (nextWidth > w && nextHeight > h) {
            sampleSize = sampleSize shl 1
            nextWidth = nextWidth shr 1
            nextHeight = nextHeight shr 1
        }
        sBitmapOptionsCache.inSampleSize = sampleSize
        sBitmapOptionsCache.inJustDecodeBounds = false
        var b = BitmapFactory.decodeByteArray(
            src, 0, src.size, sBitmapOptionsCache)
        if (b != null) {
            if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                val tmp = Bitmap.createScaledBitmap(b, w, h, true)
                if (tmp != b) b.recycle()
                b = tmp
            }
        }
        return b
    }

    fun getBitmapQuick(src: String?, w: Int, h: Int): Bitmap? {
        if (src == null) return null
        var sampleSize = 1
        sBitmapOptionsCache.inJustDecodeBounds = true
        BitmapFactory.decodeFile(
            src, sBitmapOptionsCache)
        var nextWidth = sBitmapOptionsCache.outWidth shr 1
        var nextHeight = sBitmapOptionsCache.outHeight shr 1
        while (nextWidth > w && nextHeight > h) {
            sampleSize = sampleSize shl 1
            nextWidth = nextWidth shr 1
            nextHeight = nextHeight shr 1
        }
        sBitmapOptionsCache.inSampleSize = sampleSize
        sBitmapOptionsCache.inJustDecodeBounds = false
        var b = BitmapFactory.decodeFile(
            src, sBitmapOptionsCache)
        if (b != null) {
            if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                val tmp = Bitmap.createScaledBitmap(b, w, h, true)
                if (tmp != b) b.recycle()
                b = tmp
            }
        }
        return b
    }

    fun getBitmapQuick(context: Context, uri: Uri, width: Int, height: Int): Bitmap? {
        val resolver = context.contentResolver
        val parcelFileDescriptor = resolver.openFileDescriptor(uri, "r")
        var sampleSize = 1
        if (width > 0 && height > 0) {
            sBitmapOptionsCache.inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor, null, sBitmapOptionsCache)
            var nextWidth = sBitmapOptionsCache.outWidth shr 1
            var nextHeight = sBitmapOptionsCache.outHeight shr 1
            while (nextWidth > width && nextHeight > height) {
                sampleSize = sampleSize shl 1
                nextWidth = nextWidth shr 1
                nextHeight = nextHeight shr 1
            }
            sBitmapOptionsCache.inSampleSize = sampleSize
            sBitmapOptionsCache.inJustDecodeBounds = false
        }
        var bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor, null, sBitmapOptionsCache)
        if (bitmap != null && width > 0 && height > 0) {
            if (sBitmapOptionsCache.outWidth != width || sBitmapOptionsCache.outHeight != height) {
                val tmp = Bitmap.createScaledBitmap(bitmap, width, height, true)
                if (tmp != bitmap) bitmap.recycle()
                bitmap = tmp
            }
        }
        return bitmap
    }

    fun getBitmapQuick(resId: Int, w: Int, h: Int): Bitmap? {
        var sampleSize = 1
        sBitmapOptionsCache.inJustDecodeBounds = true
        BitmapFactory.decodeResource(
            Util.mContext.resources, resId, sBitmapOptionsCache)
        var nextWidth = sBitmapOptionsCache.outWidth shr 1
        var nextHeight = sBitmapOptionsCache.outHeight shr 1
        while (nextWidth > w && nextHeight > h) {
            sampleSize = sampleSize shl 1
            nextWidth = nextWidth shr 1
            nextHeight = nextHeight shr 1
        }
        sBitmapOptionsCache.inSampleSize = sampleSize
        sBitmapOptionsCache.inJustDecodeBounds = false
        var b = BitmapFactory.decodeResource(
            Util.mContext.resources, resId, sBitmapOptionsCache)
        if (b != null) {
            if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                val tmp = Bitmap.createScaledBitmap(b, w, h, true)
                if (tmp != b) b.recycle()
                b = tmp
            }
        }
        return b
    }
    init {
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565
        sBitmapOptionsCache.inDither = false
    }
}