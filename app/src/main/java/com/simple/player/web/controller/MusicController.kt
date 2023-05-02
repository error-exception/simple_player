package com.simple.player.web.controller

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.simple.player.decode.KgmInputStream
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.KgmMediaDataSource
import com.simple.player.service.NCMMediaDataSource
import com.simple.player.service.UCMediaDataSource
import com.simple.player.util.FileUtil
import com.simple.player.web.ResponseUtils
import com.simple.player.web.service.MusicService
import com.simple.server.GetMapping
import com.simple.server.Param
import com.simple.server.PostMapping
import com.simple.server.request.Request
import com.simple.server.RequestController
import com.simple.server.Response
import com.simple.server.constant.AttributeConstant
import com.simple.server.constant.MimeTypes
import com.simple.server.constant.ResponseState
import com.simple.server.header.MimeType
import com.simple.server.util.Resource

const val CODE_NO_TIMING_FOUND = 10001
const val CODE_EMPTY_TIMING = 10002
const val CODE_REQUEST_DATA_ERROR = 10003
const val CODE_TIMING_UPLOAD_FAILED = 10004
class MusicController: RequestController() {

    @GetMapping("/musicList")
    fun musicList(): HashMap<String, Any?> {
        val musicList = PlaylistManager.localPlaylist.list
        return ResponseUtils.ok(musicList)
    }

    // TODO: 对于加密歌曲，缓存加密的信息，便于再次请求相同 Id 的歌曲时加快解密时间
    @GetMapping("/music")
    @Param(["id", ":response", ":request"])
    fun getMusic(id: String, response: Response, request: Request) {
        val song = PlaylistManager.localPlaylist[id.toLong()]
        println(song)
        if (song == null) {
            response.responseWithEmptyBody(ResponseState.NOT_FOUND)
            return
        }
        val uri = Uri.parse(song.uri)
        var inputStream = FileUtil.openInputStream(uri = uri)
        if (inputStream == null) {
            Log.e("MusicController", "getMusic: open inputStream failed")
            response.responseWithEmptyBody(ResponseState.INTERNAL_SERVER_ERROR)
            return
        }
        val mimeType = MimeType(MimeTypes.MT_APPLICATION_OCTET_STREAM)
        inputStream = when (song.type) {
            "kge", "kgm" -> KgmInputStream(inputStream)
//            "uc", "uc!" -> UCMediaDataSource(uri.toFile())
//            "ncm" -> NCMMediaDataSource(uri.toFile())
            else -> inputStream
        }
        val resource = Resource(
            inputStream = inputStream,
            mimeType = mimeType,
            length = FileUtil.getLength(uri = uri)
        )
        request.setAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE, resource)
        response.handleRequest(request = request)
    }

    @GetMapping("/timing")
    @Param(["id"])
    fun getTiming(id: String): HashMap<String, Any?> {
        val timingInfo = MusicService.getTimingInfo(id.toLong())
            ?: return ResponseUtils.responseEmpty(
                code = CODE_NO_TIMING_FOUND,
                message = "no timing found"
            )
        return ResponseUtils.ok(timingInfo)
    }

    @PostMapping("/uploadTiming")
    @Param([":request"])
    fun saveTiming(request: Request): HashMap<String, Any?> {
//        println("uploadTiming: Content-Length: ${request.getHttpHeader().getContentLength()}")
        val requestBody = request.requestBody
        if (requestBody.isEmpty()) {
            return ResponseUtils.responseEmpty(
                code = CODE_EMPTY_TIMING,
                message = "empty timing"
            )
        }
        val bodyData = requestBody.byteData
        val result = MusicService.saveTimingInfo(bodyData)
        if (!result) {
            return ResponseUtils.responseEmpty(
                code = CODE_TIMING_UPLOAD_FAILED,
                message = "timing upload failed"
            )
        }
        return ResponseUtils.ok()
    }

}