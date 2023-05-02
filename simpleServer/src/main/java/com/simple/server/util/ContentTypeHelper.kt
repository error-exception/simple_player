package com.simple.server.util

import com.simple.server.constant.MimeTypes

object ContentTypeHelper {


    fun getContentTypeByExtensionName(extension: String): String {
        val type =  when (extension) {
            "html" -> MimeTypes.MT_TEXT_HTML
            "css" -> MimeTypes.MT_TEXT_CSS
            "js" -> MimeTypes.MT_TEXT_JAVASCRIPT
            "ico" -> MimeTypes.MT_IMAGE_X_ICON
            "png" -> MimeTypes.MT_IMAGE_PNG
            "jpg", "jpeg" -> MimeTypes.MT_IMAGE_JPEG
            "gif" -> MimeTypes.MT_IMAGE_GIF
            "svg" -> MimeTypes.MT_IMAGE_SVG_XML
            "webp" -> MimeTypes.MT_IMAGE_WEBP
            "mp3" -> MimeTypes.MT_AUDIO_MP3
            "ogg" -> MimeTypes.MT_AUDIO_OGG
            "json" -> MimeTypes.MT_APPLICATION_JSON
            else -> MimeTypes.MT_APPLICATION_OCTET_STREAM
        }
        return type
    }

    fun getContentTypeByURL(url: String): String {
        return getContentTypeByExtensionName(getExtensionName(url))
    }

    fun isTextType(type: String): Boolean {
        return type.startsWith("text/") || type.startsWith(MimeTypes.MT_APPLICATION_JSON)
    }

    private fun getExtensionName(url: String): String {
        return url.substring(url.lastIndexOf('.') + 1).lowercase()
    }

}