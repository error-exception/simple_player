package com.simple.server.util

import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException
import kotlin.jvm.Throws

object StreamUtils {

    private const val BUFFER_SIZE = 4096

    @Throws(FileNotFoundException::class)
    fun copy(resource: Resource, outputStream: OutputStream) {
        val inputStream = resource.getInputStream()
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                outputStream.write(buffer, 0, length)
            }
        } catch (e: FileNotFoundException) {
            throw e
        } finally {
            inputStream.close()
            outputStream.flush()
        }
    }

    @Throws(FileNotFoundException::class, IllegalArgumentException::class)
    fun copyToRange(resource: Resource, outputStream: OutputStream, start: Long, count: Long) {
        val inputStream = resource.getInputStream()
        try {
            var buffer = ByteArray(BUFFER_SIZE)
            var length: Int
            val resLength = resource.getLength()
            if (start >= resLength) {
                throw IllegalArgumentException("start must < resource size")
            }
            if (start + count > resLength) {
                throw IllegalArgumentException("start + count must < resource size")
            }
            var readCount = count
            inputStream.skip(start)
            while (inputStream.read(buffer).also { length = it } != -1 && BUFFER_SIZE <= count) {
                outputStream.write(buffer, 0, length)
                readCount -= length
            }
            if (readCount > 0) {
                buffer = ByteArray(readCount.toInt())
                length = inputStream.read(buffer)
                outputStream.write(buffer, 0, length)
            }
        } catch (e : Exception) {
            println("Error")
            throw e
        } finally {
            inputStream.close()
            outputStream.flush()
        }
    }

    fun copy(inputStream: InputStream, outputStream: OutputStream) {
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                outputStream.write(buffer, 0, length)
            }
        } finally {
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        }
    }

    fun copyToRange(inputStream: InputStream, outputStream: OutputStream, start: Long, count: Long) {
        try {
            var buffer = ByteArray(BUFFER_SIZE)
            var length: Int
            var readCount = count
            inputStream.skip(start)
            while (inputStream.read(buffer).also { length = it } != -1 && BUFFER_SIZE <= count) {
                outputStream.write(buffer, 0, length)
                readCount -= length
            }
            if (length < 0) {
                readCount = 0
            }
            if (readCount > 0) {
                buffer = ByteArray(readCount.toInt())
                length = inputStream.read(buffer)
                outputStream.write(buffer, 0, length)
            }
        } catch (_ : Exception) {
            println("Error")
        } finally {
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        }
    }

}