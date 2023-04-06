package com.simple.player.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import com.simple.player.drawable.RoundDrawable
import com.simple.player.drawable.RoundBitmapDrawable2
import com.simple.player.playlist.PlaylistManager
import java.util.*

@Deprecated(level = DeprecationLevel.WARNING, message = "内存占用大")
object ArtworkBuilder {

    private var blurRadius: Int = 0
    private var round: Int = 0
    private var size: Int = 0
    private var isRoundSet = false
    private var id = 0L
    private var map = LinkedList<Pair<Long, Bitmap>>()

    fun id(songId: Long): ArtworkBuilder {
        id = songId
        return this
    }

    fun size(size: Int): ArtworkBuilder {
        if (size > this.size) {
            this.size = size
        }
        return this
    }

    fun blur(radius: Int): ArtworkBuilder {
        blurRadius = radius
        return this
    }

    fun round(radius: Int = 1): ArtworkBuilder {
        round = radius
        isRoundSet = true
        return this
    }

    fun createBitmap(context: Context): Bitmap? {
        for (pair in map) {
            if (pair.first == id && !pair.second.isRecycled) {
                return pair.second
            }
        }
        val uri = ArtworkProvider.getArtworkUri(PlaylistManager.localPlaylist[id]!!)
        uri ?: return null
        var bitmap = if (size == 0) {
            getBitmapQuick(context, uri, size, size)
        } else {
            getBitmapQuick(context, uri, -1, -1)
        }
        if (blurRadius > 1 && bitmap != null) {
            bitmap = BlurUtils.blur(bitmap, blurRadius);
        }
        addBitmap(id, bitmap)
        return bitmap
    }


    fun createDrawable(context: Context): Drawable? {
        val bitmap = createBitmap(context) ?: return null
        if (isRoundSet && round == 1) {
            return RoundDrawable(bitmap)
        }
        if (round > 1) {
            return RoundBitmapDrawable2(bitmap, round.toFloat())
        }
        return null
    }

    private fun addBitmap(id: Long, bitmap: Bitmap?) {
        bitmap ?: return
        if (map.size >= 2) {
            val pair = map.last
            if (!pair.second.isRecycled) {
                pair.second.recycle()
            }
            map.removeLast()
        }
        map.addFirst(Pair(id, bitmap))
    }

    private val sBitmapOptionsCache = BitmapFactory.Options()
    private fun getBitmapQuick(context: Context, uri: Uri, width: Int, height: Int): Bitmap? {
        val resolver = context.contentResolver
        val parcelFileDescriptor = resolver.openFileDescriptor(uri, "r")
        var sampleSize = 1
        if (width > 0 && height > 0) {
            sBitmapOptionsCache.inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor, null,
                sBitmapOptionsCache
            )
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
        var bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor, null,
            sBitmapOptionsCache
        )
        if (bitmap != null ) {
            bitmap = BitmapUtils.cropBitmapAspect(bitmap, 1F, 1F)
            if (width > 0 && height > 0) {
                if (sBitmapOptionsCache.outWidth != width || sBitmapOptionsCache.outHeight != height) {
                    val tmp = Bitmap.createScaledBitmap(bitmap, width, height, true)
                    if (tmp != bitmap) bitmap.recycle()
                    bitmap = tmp
                }
            }

        }
        return bitmap
    }

    init {
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565
        sBitmapOptionsCache.inDither = false
    }

}