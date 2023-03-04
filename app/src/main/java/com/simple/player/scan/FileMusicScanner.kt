package com.simple.player.scan

import android.net.Uri
import android.util.Log
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.rotationMatrix
import com.simple.player.database.ScanConfigDao
import com.simple.player.util.AppConfigure
import com.simple.player.util.FileUtil
import java.io.File

class FileMusicScanner: MusicScanner() {

    private val TAG = "FileMusicScanner"

    private val rootDirectory = FileUtil.defaultPath

    private var scanDirectories = arrayOf(rootDirectory)

    private var accessExtensions: List<ScanConfigItem> =
        ScanConfigDao.queryValuesByType(ScanConfigDao.TYPE_EXTENSION_NAME).filter {
            it.isValid.value
        }

    private var excludePaths: List<ScanConfigItem> =
        ScanConfigDao.queryValuesByType(ScanConfigDao.TYPE_EXCLUDE_PATH).filter {
            it.isValid.value
        }

    private var includePaths: List<ScanConfigItem> =
        ScanConfigDao.queryValuesByType(ScanConfigDao.TYPE_INCLUDE_PATH).filter {
            it.isValid.value
        }

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
        // 该循环使用路径过滤
        for (scanDirectory in scanDirectories) {
            if (config.swallowSearch) {
                swallowSearch(scanDirectory)
            } else {
                search(scanDirectory)
            }
        }
        // 不使用路径过滤
        if (config.searchInclude) {
            Log.e(TAG, "scan: $includePaths")
            for (includePath in includePaths) {
                val path = includePath.value.value
                if (path.isNotEmpty()) {
                    if (config.swallowSearchInclude) {
                        swallowSearchInclude(File(path))
                    } else {
                        searchInclude(File(path))
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

    private fun swallowSearch(file: File) {
        if (isExcludePath(file.absolutePath)) {
            return
        }
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

    private fun access(f: File): Boolean {
        for (extension in accessExtensions) {
            if (f.name.endsWith(".${extension.value.value}")) {
                return true
            }
        }
        return false
    }

    private fun isExcludePath(path: String): Boolean {
        for (excludePath in excludePaths) {
            if (path.startsWith(excludePath.value.value)) {
                return true
            }
        }
        return false
    }

    override val resultCount: Int
        get() = this.index.toInt()

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