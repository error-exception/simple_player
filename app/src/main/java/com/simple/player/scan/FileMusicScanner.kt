package com.simple.player.scan

import android.net.Uri
import com.simple.player.util.AppConfigure
import com.simple.player.util.FileUtil
import java.io.File

class FileMusicScanner: MusicScanner() {

    private val rootDirectory = FileUtil.defaultPath
    private var accessExtensions: Array<String>
    private var excludePaths: Array<String>

    init {
        val list = AppConfigure.Settings.accessExtension.split(",")
        accessExtensions = Array(list.size) {
            list[it].trim()
        }
        val paths = AppConfigure.Settings.excludePath.split("\n")
        excludePaths = Array(paths.size) {
            paths[it].trim()
        }
    }

    override fun scan() {
        search(rootDirectory)
        onComplete?.invoke()
    }

    private var index = 0L

    private fun search(file: File) {
        val list = file.listFiles()
        list ?: return
        for (f in list) {
            if (f.isDirectory) {
                if (isExcludePath(f.absolutePath)) {
                    continue
                }
                search(f)
            }
            if (f.isFile && access(f)) {
                onEachMusic?.invoke(index++, Uri.fromFile(f), null, f.name)
            }
        }
    }

    override val resultCount: Int
        get() = this.index.toInt()

    private fun access(f: File): Boolean {
        for (extension in accessExtensions) {
            if (f.name.endsWith(extension)) {
                return true
            }
        }
        return false
    }

    private fun isExcludePath(path: String): Boolean = excludePaths.contains(path)

}