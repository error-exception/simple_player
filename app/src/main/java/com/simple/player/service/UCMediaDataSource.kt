package com.simple.player.service

import android.media.MediaDataSource
import com.simple.player.decode.UCDecoder
import java.io.File
import java.io.RandomAccessFile

class UCMediaDataSource(private val ucFile: File): MediaDataSource() {

    private val TAG = "UCMediaDataSource"
    private val randomAccess = RandomAccessFile(ucFile, "r")

    override fun close() {
        randomAccess.close()
    }

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
        buffer ?: return -1
        randomAccess.seek(position)
        val length = randomAccess.read(buffer, offset, size)
        var i = offset
        while (i < size) {
            buffer[i] = UCDecoder.decryptByte(buffer[i]).toByte()
            i++
        }
        return length
    }

    override fun getSize(): Long {
        return ucFile.length()
    }
}