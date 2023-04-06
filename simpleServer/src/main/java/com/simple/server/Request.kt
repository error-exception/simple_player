package com.simple.server

import com.simple.server.header.HttpHeader
import com.simple.server.header.MimeType
import com.simple.server.util.logger
import com.simple.server.util.tokenizer
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.Socket
import java.nio.charset.StandardCharsets

class Request(socket: Socket) {

    private val input = socket.getInputStream()
    private val attr = HashMap<String, Any>()
    private var header: HttpHeader? = null
    private val log = logger("Request")

    var isHttpRequest = true
    var method: String? = null
    var requestUrl: RequestUrl? = null
    var httpVersion: String? = null
    var requestBody: RequestBody? = null

    init {
        val byte = readSingleByte()
        if (byte != 'G'.code && byte != 'P'.code) {
            isHttpRequest = false
        } else {
            var s = byte.toChar() + readLineAsString()
            val list = s.split(' ')
            method = list[0]
            requestUrl = RequestUrl(list[1])
            httpVersion = list[2]
            log(list.toString())
            while (true) {
                s = readLineAsString()
                if (s.isEmpty()) {
                    break
                }
                processRequestLine(s)
            }
            requestBody = parseRequestBody()
        }
    }

    private fun parseRequestBody(): RequestBody? {
        val len = getHttpHeader().getContentLength()
        if (len == 0L) {
            return null
        }
        return RequestBody(input, this).apply {
            this.length = len
        }
    }

    private fun processRequestLine(requestLine: String) {
        val tokens = requestLine.tokenizer(":")
        val name = tokens[0]
        val value = tokens[1]
        val httpHeader = getHttpHeader()
        httpHeader.set(name, value)
    }

    private fun readLineAsString(): String {
        return readLineBytes(true).toString(SimpleHttpServerConfig.charset)
    }

    private fun readLineBytes(skipCRLF: Boolean = true): ByteArray {
        val data = ByteArrayOutputStream()
        var b = readSingleByte()
        val cr = cr.toInt()
        while (b != cr) {
            data.write(b)
            b = readSingleByte()
        }
        if (skipCRLF) {
            readSingleByte()
        } else {
            data.write(b)
            data.write(readSingleByte())
        }
        return data.toByteArray()
    }

    private fun readSingleByte(): Int = input.read()

    fun getHttpHeader(): HttpHeader {
        if (header == null) {
            header = HttpHeader(this)
        }
        return header as HttpHeader
    }

    fun setHttpHeader(header: HttpHeader) {
        this.header = header
    }

    fun getAttribute(key: String): Any? {
        return attr[key]
    }

    fun setAttribute(key: String, value: Any) {
        attr[key] = value
    }

    fun isAjaxRequest(): Boolean {
        return getHttpHeader().getXRequestedWith() == "XMLHttpRequest"
    }

    class RequestUrl(requestUrl: String) {
        val url: String
        val parameter: HashMap<String, String> = HashMap()
        init {
            val i = requestUrl.indexOf('?')
            url = if (i < 0) {
                requestUrl
            } else {
                val u = requestUrl.substring(0, i)
                val parameterString = requestUrl.substring(i + 1)
                val map = parseQueryString(parameterString)
                parameter.putAll(map)
                u
            }
        }

        override fun toString(): String {
            return "url:$url param:${parameter}"
        }
    }

    class RequestBody(
        private val input: InputStream,
        private val request2: Request
    ) {

        private var data: ByteArray? = null
        private var currentLength = 0L

        val mimeType: MimeType
        var length: Long = 0L
            set(value) {
                field = value
                currentLength = value
            }
        var multipartDataMap: HashMap<String, MultipartDataItem>? = null

        init {
            val header = request2.getHttpHeader()
            mimeType = header.getContentType()
            parse()
        }

        /**
         * 建议当 Content-Type 为 multipart/form-data，不调用此方法
         */
        fun getData(): ByteArray? {
            return data
        }

        internal fun parse() {
            if (mimeType.isSameType(MimeType.MIME_TYPE_MULTIPART_FORM_DATA)) {
                parseMultipart()
            } else if (mimeType.isSameType(MimeType.MIME_TYPE_APPLICATION_X_WWW_FORM_URLENCODED) ||
                mimeType.isSameType(MimeType.MIME_TYPE_APPLICATION_JSON)
                ) {
                // TODO: use StreamUtils.copy()
                val output = ByteArrayOutputStream()
                while (currentLength > 0) {
                    output.write(readSingleByte())
                }
                data = output.toByteArray()
                output.close()
            }
        }

        /**
         * 在解析数据部分时，每个数据部分末尾都会多 CRLF，获取时注意去掉
         */
        private fun parseMultipart() {
            multipartDataMap = HashMap()
            val unEndBoundary = "--${mimeType.boundary}"
            val endBoundary = "$unEndBoundary--"
            readLineAsString()
            do {
                val item = MultipartDataItem(this)
                item.setBoundary(unEndBoundary, endBoundary)
                item.parse()
                multipartDataMap!![item.name] = item
            } while (!item.isEndOfRequestContent)
        }

        internal fun readLineAsString(): String {
            return readLineBytes(true).toString(SimpleHttpServerConfig.charset)
        }

        internal fun readLineBytes(skipCRLF: Boolean = true): ByteArray {
            val data = ByteArrayOutputStream()
            var b = readSingleByte()
            val cr = cr.toInt()
            while (b != cr) {
                data.write(b)
                b = readSingleByte()
            }
            if (skipCRLF) {
                readSingleByte()
            } else {
                data.write(b)
                data.write(readSingleByte())
            }
            val bytes = data.toByteArray()
            data.close()
            return bytes
        }

        internal fun readSingleByte(): Int {
            currentLength--
            return input.read()
        }
    }

    class MultipartDataItem(
        private val requestContent: RequestBody
    ) {
        private lateinit var unEndBoundary: String
        private lateinit var endBoundary: String
        private val data = ArrayList<Byte>()

        var header = HttpHeader()
        var isEndOfRequestContent = false
        lateinit var name: String
        var mimeType: MimeType? = null
        var filename: String? = null

        fun setBoundary(unEndBoundary: String, endBoundary: String) {
            this.endBoundary = endBoundary
            this.unEndBoundary = unEndBoundary
        }

        fun parse() {
            var isLineEmptied = false
            while (true) {
                if (!isLineEmptied) {
                    val line = requestContent.readLineAsString()
                    if (line.isEmpty()) {
                        isLineEmptied = true
                        val contentDisposition = header.getContentDisposition()
                        mimeType = header.getContentType()
                        name = contentDisposition!!.name!!
                        filename = contentDisposition.filename
                    } else {
                        val tokens = line.tokenizer(":")
                        header.set(tokens[0], tokens[1])
                    }

                } else {
                    val bytes = requestContent.readLineBytes(false)
                    //println(bytes.contentToString())
                    if (isBoundary(bytes)) {
                        break
                    }
                    for (byte in bytes) {
                        data += byte
                    }
                }
            }
            // 去掉 CRLF
            data.removeAt(data.size - 1)
            data.removeAt(data.size - 1)
        }

        private fun isBoundary(bytes: ByteArray): Boolean {
            if (bytes.size - 2 == unEndBoundary.length) {
                val s = bytes.toString(0, bytes.size - 2, StandardCharsets.UTF_8)
                if (s == unEndBoundary) return true
            }
            if (bytes.size - 2 == endBoundary.length) {
                val s = bytes.toString(0, bytes.size - 2, StandardCharsets.UTF_8)
                if (s == endBoundary) {
                    isEndOfRequestContent = true
                    return true
                }
            }
            return false
        }

        fun getData(): ByteArray = data.toByteArray()

    }
}