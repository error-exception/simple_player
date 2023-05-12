package com.simple.player.web.controller

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import coil.request.ImageRequest
import com.simple.player.Util
import com.simple.player.decode.KgmInputStream
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.KgmMediaDataSource
import com.simple.player.service.NCMMediaDataSource
import com.simple.player.service.UCMediaDataSource
import com.simple.player.util.ArtworkProvider
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
import java.lang.Exception

const val CODE_NO_TIMING_FOUND = 10001
const val CODE_EMPTY_TIMING = 10002
const val CODE_REQUEST_DATA_ERROR = 10003
const val CODE_TIMING_UPLOAD_FAILED = 10004
class MusicController: RequestController() {

    @GetMapping("/musicList")
    fun musicList(): HashMap<String, Any?> {
        val musicList = PlaylistManager.getLocalList().rawList()
        return ResponseUtils.ok(musicList)
    }

    // TODO: 对于加密歌曲，缓存加密的信息，便于再次请求相同 Id 的歌曲时加快解密时间
    @GetMapping("/music")
    @Param(["id", ":response", ":request"])
    fun getMusic(id: String, response: Response, request: Request) {
        val song = PlaylistManager.getLocalList().getSong(id.toLong())
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
        var length = -1L
        inputStream = when (song.type) {
            "kge", "kgm" -> {
                length = FileUtil.getLength(uri = uri) - 1024
                KgmInputStream(inputStream)
            }
            else -> inputStream
        }
        val resource = Resource(
            inputStream = inputStream,
            mimeType = mimeType,
            length = if (length > 0L) length else FileUtil.getLength(uri = uri)
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

    @GetMapping("/artwork")
    @Param(["id", ":request", ":response"])
    fun getArtwork(id: String, request: Request, response: Response) {
        val song = PlaylistManager.getLocalList().getSong(id.toLong())
        if (song == null) {
            response.responseWithEmptyBody(ResponseState.NOT_FOUND)
            return
        }
        val uri = ArtworkProvider.getArtworkUri(song)
        if (uri == null) {
            response.responseWithEmptyBody(ResponseState.NOT_FOUND)
            return
        }
        val inputStream = FileUtil.openInputStream(uri = uri)
        if (inputStream == null) {
            response.responseWithEmptyBody(ResponseState.NOT_FOUND)
            return
        }
        val resource = Resource(
            inputStream = inputStream,
            mimeType = MimeType(MimeTypes.MT_IMAGE_PNG),
            length = FileUtil.getLength(uri)
        )
        request.setAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE, resource)
        response.handleRequest(request = request)
    }

}