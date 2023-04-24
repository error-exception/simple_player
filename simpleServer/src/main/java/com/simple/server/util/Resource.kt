package com.simple.server.util

import com.simple.server.header.MimeType
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception

class Resource {

    private var inputStream: InputStream? = null
    private var isInputStreamSet: Boolean = false
    private var inputStreamLength: Long = 0

    var resourceFile: File? = null
        private set
    var resourceData: ByteArray? = null
        private set
    lateinit var mimeType: MimeType
        private set


    fun setResource(resourceFile: File, mimeType: MimeType) {
        this.resourceFile = resourceFile
        this.mimeType = mimeType
    }

    fun setResource(resourceData: ByteArray, mimeType: MimeType) {
        this.resourceData = resourceData
        this.mimeType = mimeType
    }

    fun setResource(inputStream: InputStream, mimeType: MimeType, length: Long) {
        this.inputStream = inputStream
        this.mimeType = mimeType
        inputStreamLength = length
        isInputStreamSet = true
    }

    fun openInputStream(): InputStream {
        if (isInputStreamSet) {
            return inputStream!!
        }
        try {
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (resourceFile != null) {
            inputStream = FileInputStream(resourceFile!!)
            return inputStream!!
        }
        if (resourceData != null && resourceData!!.isNotEmpty()) {
            inputStream = ByteArrayInputStream(resourceData)
            return inputStream!!
        }
        throw FileNotFoundException("no resource found!!")
    }

    fun getLength(): Long {
        if (resourceFile != null) {
            return resourceFile!!.length()
        }
        if (resourceData != null) {
            return resourceData!!.size.toLong()
        }
        if (isInputStreamSet) {
            return inputStreamLength
        }
        throw FileNotFoundException("no resource found!!")
    }

    fun close() {
        try {
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}