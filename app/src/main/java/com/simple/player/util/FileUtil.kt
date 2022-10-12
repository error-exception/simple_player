package com.simple.player.util

import android.net.Uri
import android.provider.MediaStore
import com.simple.player.Util
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets

object FileUtil {

    var defaultPath: File = File("/storage/emulated/0/")
        private set

    fun writeTextUTF8(file: File, t: String) {
        try {
            var output = FileOutputStream(file)
            var writer = OutputStreamWriter(output, "UTF-8")
            writer.write(t)
            writer.flush()
            writer.close()
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun writeBytes(file: File, data: ByteArray) {
        val output = FileOutputStream(file).apply {
            write(data, 0, data.size)
        }
        closeStream(output)
    }

    fun copy(from: File, to: File) {
        try {
            val inputStream: InputStream = FileInputStream(from)
            val outputStream: OutputStream = FileOutputStream(to)
            var len = 0
            val buffer = ByteArray(1024)
            while ((inputStream.read(buffer).also { len = it }) != -1) {
                outputStream.write(buffer, 0, len)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
        }
    }

    lateinit var mArtworkDirectory: File
    lateinit var mHeaderPicture: File
    lateinit var mWebRoot: File
    lateinit var mListDirectory: File
    lateinit var mDataDirectory: File

    fun getFileType(path: String): String {
        return path.substring(path.lastIndexOf('.') + 1).lowercase()
    }

    /**
     * 从 MediaStore 获取文件的 mimeType
     */
    fun getMimeType(uri: Uri): String? {
        var mineType: String? = null
        val projection = arrayOf(
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        val ids = arrayOf(
            getContentId(uri)
        )
        val c = Util.mContext.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            "id = ?",
            ids,
            null)
        c ?: return null
        if (c.moveToNext()) {
            mineType = c.getString(0)
        }
        c.close()
        return mineType
    }

    fun readBytes(uri: Uri): ByteArray {
        var data = ByteArray(0)
        openInputStream(uri)?.apply {
            data = readBytes(this)
        }
        return data
    }

    fun readBytes(file: File): ByteArray {
        var data = ByteArray(0)
        openInputStream(file)?.apply {
            data = readBytes(this)
        }
        return data
    }

    fun readBytes(path: String): ByteArray {
        var data = ByteArray(0)
        openInputStream(path)?.apply {
            data = readBytes(this)
        }
        return data
    }

    fun readBytes(inputStream: InputStream): ByteArray {
        var data: ByteArray
        var byteOutput: ByteArrayOutputStream? = null
        try {
            byteOutput = ByteArrayOutputStream()
            val a = ByteArray(1024)
            var len: Int
            while (inputStream.read(a).also { len = it } != -1) {
                byteOutput.write(a, 0, len)
            }
            byteOutput.flush()
            data = byteOutput.toByteArray()
        } catch (e: IOException) {
            data = ByteArray(0)
        } finally {
            closeStream(byteOutput)
            closeStream(inputStream)
        }
        return data
    }

    fun getContentId(uri: Uri): String {
        val uriString = uri.toString()
        return uriString.substring(uriString.lastIndexOf("/") + 1)
    }

    fun getLength(uri: Uri): Long {
        var length: Long = -1
        val projection = arrayOf(
            MediaStore.Files.FileColumns.SIZE
        )
        val uriString = uri.toString()
        val id = uriString.substring(uriString.lastIndexOf("/") + 1)
        val ids = arrayOf(
            id
        )
        val c = Util.mContext.contentResolver.query(MediaStore.Files.getContentUri("external"),
            projection,
            "_id = ?",
            ids,
            null)
        if (c!!.moveToNext()) {
            length = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
        }
        c.close()
        return length
    }

    fun readTextUTF8(path: String): String {
        var content = ""
        openInputStream(path)?.apply {
            content = readTextUTF8(this)
        }
        return content
    }

    fun readTextUTF8(uri: Uri): String {
        var content = ""
        val inputStream = openInputStream(uri)
        if (inputStream != null) {
            content = readTextUTF8(inputStream)
        }
        return content
    }

    fun readTextUTF8(inputStream: InputStream): String {
        val s = StringBuilder()
        var reader: InputStreamReader? = null
        try {
            reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val temp = CharArray(1024)
            var len = 0
            while (reader.read(temp).also { len = it } != -1) {
                s.append(temp, 0, len)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (e: Exception) {}
        }
        return s.toString()
    }

    fun openInputStream(uri: Uri): InputStream? {
        var inputStream: InputStream? = null
        try {
            inputStream = Util.mContext.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return inputStream
    }

    fun openInputStream(file: File): InputStream? {
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return inputStream
    }

    fun openInputStream(path: String): InputStream? {
        return openInputStream(File(path))
    }

    fun getAssetInputStream(filename: String?): InputStream? {
        var inputStream: InputStream? = null
        inputStream = try {
            Util.mContext.assets.open(filename!!)
        } catch (e: FileNotFoundException) {
            return null
        } catch (e: IOException) {
            return null
        }
        return inputStream
    }

    fun openOutputStream(path: String): OutputStream? {
        var outputStream: OutputStream? = null
        try {
            val f = File(path)
            outputStream = FileOutputStream(f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputStream
    }

    private fun closeStream(vararg inputStream: InputStream?) {
        try {
            for (stream in inputStream) {
                stream?.close()
            }
        } catch (e: Exception){}
    }

    private fun closeStream(vararg outputStream: OutputStream?) {
        try {
            for (stream in outputStream) {
                stream?.close()
            }
        } catch (e: Exception){}
    }



}