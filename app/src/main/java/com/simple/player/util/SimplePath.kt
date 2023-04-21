package com.simple.player.util

/**
 * 一个简单处理路径的类, 路径分隔符必须为 “/”
 */
class SimplePath(path: String = "") {

    private var pathList: ArrayList<String> = ArrayList()

    init {
        if (path.isNotEmpty()) {
            if (path.contains("/")) {
                val list = path.split("/")
                for (s in list) {
                    pathList += s
                }
            } else {
                pathList += path
            }
        }
    }

    fun toParent() {
        pathList.removeLast()
    }

    fun getParent(): SimplePath {
        val path = SimplePath()
        for (index in pathList.indices) {
            if (index < pathList.size - 1) {
                path.pathList += pathList[index]
            }
        }
        return path
    }

    fun pathString(): String {
        val sb = StringBuilder()
        for (s in pathList) {
            sb.append(s).append('/')
        }
        if (sb.isEmpty()) {
            return ""
        }
        return sb.deleteCharAt(sb.length - 1).toString()
    }

    fun append(pathString: String): SimplePath {
        pathList += pathString
        return this
    }

    fun getFilename(): String {
        return pathList.last()
    }

    fun getExtension(): String {
        val last = pathList.last()
        val index = last.lastIndexOf('.')
        return if (index == -1) {
            ""
        } else {
            last.substring(index + 1)
        }
    }

    fun replaceLast(pathString: String) {
        pathList[pathList.size - 1] = pathString
    }

    fun hasExtension(): Boolean {
        return pathList.last().lastIndexOf('.') != -1
    }

    override fun toString(): String {
        return pathString()
    }

}