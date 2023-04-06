package com.simple.server

import java.net.URLDecoder
import java.nio.charset.Charset

fun ByteArray.toString(charset: Charset): String {
    return String(this, 0, this.size, charset)
}

fun ByteArray.toString(offset: Int, length: Int, charset: Charset): String {
    return String(this, offset, length, charset)
}

fun String.substring(start: String, offset: Int = 0, end: Int = Int.MAX_VALUE): String {
    var i = this.indexOf(start)
    if (i < 0) {
        return this
    }
    i = start.length + offset
    val endIndex = kotlin.math.min(this.length, end)
    return this.substring(i, endIndex)
}

fun String.toCharset(): Charset {
    return Charset.forName(this)
}

fun getInRequestLine(header: HashMap<String, String>, requestHeaderName: String, queryKey: String): String? {
    val value = header[requestHeaderName]
    value ?: return null

    var i = value.indexOf(queryKey)

    if (i < 0) return null
    i += queryKey.length + 1 // skip '='
    val sb = StringBuilder()
    val hasQuotation = value[i] == '"'
    if (hasQuotation) {
        i++
        while (value[i] != '"') {
            sb.append(value[i])
            i++
        }
    } else {
        while (i < value.length && value[i] != ',' && value[i] != ';' ) {
            sb.append(value[i])
            i++
        }
    }
    return sb.toString()
}

fun parseQueryString(queryString: String): HashMap<String, String> {
    val list = queryString.split('&')
    val map = HashMap<String, String>()
    for (s in list) {
        val split = s.split('=')
        map[split[0]] = split[1].trim()
    }
    return map
}

const val cr = '\r'.code.toByte()
const val lf = '\n'.code.toByte()

data class MutablePair<K, V>(
    var first: K,
    var second: V
)

var threadId = 0