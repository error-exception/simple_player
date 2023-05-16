package com.simple.player.web.service

import com.simple.player.util.AppConfigure
import com.simple.player.util.FileUtil
import java.io.File

object ImageService {

    fun availableBackground(): ArrayList<String> {
        val backgroundDir = AppConfigure.Web.backgroundPath
        if (backgroundDir.isNotEmpty()) {
            val dir = File(backgroundDir)
            return fileList(dir)
        }
        val resourceRoot = AppConfigure.Web.resourceRoot
        if (resourceRoot.isNotEmpty()) {
            val dir = File("$resourceRoot/background")
            return fileList(dir)
        }
        val internalBackground = File(FileUtil.mWebRoot.absolutePath + "/background")
        if (internalBackground.exists() && internalBackground.isDirectory) {
            return fileList(internalBackground)
        }
        return ArrayList()
    }

    private fun fileList(file: File): ArrayList<String> {
        val list = file.list() ?: return ArrayList()
        val arrayList = ArrayList<String>()
        for (s in list) {
            arrayList += s
        }
        return arrayList
    }

    fun getBackgroundFile(name: String): File? {
        val backgroundDir = AppConfigure.Web.backgroundPath
        if (backgroundDir.isNotEmpty()) {
            val file = File("$backgroundDir/$name")
            return if (file.exists() && file.isFile) file else null
        }
        val resourceRoot = AppConfigure.Web.resourceRoot
        if (resourceRoot.isNotEmpty()) {
            val file = File("$resourceRoot/background/$name")
            return if (file.exists() && file.isFile) file else null
        }
        val internalBackground = File(FileUtil.mWebRoot.absolutePath + "/background/$name")
        if (internalBackground.exists() && internalBackground.isFile) {
            return internalBackground
        }
        return null
    }

}