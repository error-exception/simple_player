package com.simple.player.scan

import android.net.Uri
import android.util.Log
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.rotationMatrix
import com.simple.player.util.AppConfigure
import com.simple.player.util.FileUtil
import java.io.File

class FileMusicScanner: MusicScanner() {

    private val TAG = "FileMusicScanner"

    private val rootDirectory = FileUtil.defaultPath
    private var scanDirectories = arrayOf(rootDirectory)
    private var accessExtensions: MutableSet<String> = AppConfigure.Settings.accessExtension
    private var excludePaths: MutableSet<String> = AppConfigure.Settings.excludePath
    private var includePaths: MutableSet<String> = AppConfigure.Settings.includePath

    private var musicDirectories = HashSet<String>()

    fun setDirectories(dirs: Array<File>) {
        scanDirectories = dirs
    }

    private var config = ScanConfig(
        swallowSearch = false,
        swallowSearchInclude = false,
        searchInclude = true
    )

    fun config(
        swallowSearch: Boolean = false,
        swallowSearchInclude: Boolean = false,
        searchInclude: Boolean = true
    ) {
        config = ScanConfig(
            searchInclude = searchInclude,
            swallowSearch = swallowSearch,
            swallowSearchInclude = swallowSearchInclude
        )
    }

    override fun scan() {

        for (scanDirectory in scanDirectories) {
            if (config.swallowSearch) {
                swallowSearch(scanDirectory)
            } else {
                search(scanDirectory)
            }
        }
        if (config.searchInclude) {
            for (includePath in includePaths) {
                if (includePath.isNotEmpty()) {
                    if (config.swallowSearchInclude) {
                        swallowSearchInclude(File(includePath))
                    } else {
                        searchInclude(File(includePath))
                    }
                }
            }
        }

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
            if (f.isFile && f.length() > 1024 && access(f)) {
                val parent = f.parent
                if (parent != null) {
                    musicDirectories.add(parent)
                }
                onEachMusic?.invoke(index++, Uri.fromFile(f), null, f.name)
            }
        }
    }

    private fun searchInclude(file: File) {
        val list = file.listFiles()
        list ?: return
        for (f in list) {
            if (f.isDirectory) {
                searchInclude(f)
            }
            if (f.isFile && f.length() > 1024 && access(f)) {
                val parent = f.parent
                if (parent != null) {
                    musicDirectories.add(parent)
                }
                onEachMusic?.invoke(index++, Uri.fromFile(f), null, f.name)
            }
        }
    }

    private fun swallowSearchInclude(file: File) {
        val list = file.listFiles()
        list ?: return
        for (f in list) {
            if (f.isFile && f.length() > 1024 && access(f)) {
                val parent = f.parent
                if (parent != null) {
                    musicDirectories.add(parent)
                }
                onEachMusic?.invoke(index++, Uri.fromFile(f), null, f.name)
            }
        }
    }

    private fun swallowSearch(file: File) {
        val listFiles = file.listFiles()
        listFiles ?: return
        for (f in listFiles) {
            if (f.isFile && access(f)) {
                val parent = f.parent
                if (parent != null) {
                    musicDirectories.add(parent)
                }
                onEachMusic?.invoke(index++, Uri.fromFile(f), null, f.name)
            }
        }
    }

    override val resultCount: Int
        get() = this.index.toInt()

    private fun access(f: File): Boolean {
        for (extension in accessExtensions) {
            if (f.name.endsWith(".$extension")) {
                return true
            }
        }
        return false
    }

    private fun isExcludePath(path: String): Boolean {
        for (excludePath in excludePaths) {
            if (path.startsWith(excludePath)) {
                return true
            }
        }
        return false
    }

    override fun onComplete(listener: () -> Unit) {
        onComplete = {
            AppConfigure.Player.musicDirectories = musicDirectories
            listener()
        }
    }

    private data class ScanConfig(
        val swallowSearch: Boolean,
        val swallowSearchInclude: Boolean,
        val searchInclude: Boolean
    )

}