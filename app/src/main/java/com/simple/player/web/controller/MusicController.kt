package com.simple.player.web.controller

import android.net.Uri
import android.util.Log
import com.simple.json.JSON
import com.simple.player.model.Song
import com.simple.player.model.TimingInfo
import com.simple.player.playlist.PlaylistManager
import com.simple.player.util.FileUtil
import com.simple.player.web.ResponseUtils
import com.simple.player.web.service.MusicService
import com.simple.server.GetMapping
import com.simple.server.Param
import com.simple.server.PostMapping
import com.simple.server.Request
import com.simple.server.RequestController
import com.simple.server.Response
import com.simple.server.constant.AttributeConstant
import com.simple.server.constant.MimeTypes
import com.simple.server.constant.ResponseState
import com.simple.server.header.MimeType
import com.simple.server.toString
import com.simple.server.util.Resource
import java.nio.charset.StandardCharsets
import kotlin.math.log

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

    @GetMapping("/music")
    @Param(["id", ":response", ":request"])
    fun getMusic(id: String, response: Response, request: Request) {
        val song = PlaylistManager.localPlaylist[id.toLong()]
        println(song)
        if (song == null) {
            response.responseWithEmptyBody(ResponseState.NOT_FOUND)
            return
        }
        val resource = Resource()
        val uri = Uri.parse(song.uri)
        val inputStream = FileUtil.openInputStream(uri = uri)
        if (inputStream == null) {
            Log.e("MusicController", "getMusic: open inputStream failed")
            response.responseWithEmptyBody(ResponseState.INTERNAL_SERVER_ERROR)
            return
        }
        val mimeType = MimeType(MimeTypes.MT_APPLICATION_OCTET_STREAM)
        resource.setResource(
            inputStream = inputStream,
            mimeType = mimeType,
            length = FileUtil.getLength(uri = uri)
        )
        request.setAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE, resource)
        response.handleRequest(request = request, server = this.server)
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
        println("uploadTiming: Content-Length: ${request.getHttpHeader().getContentLength()}")
        val requestBody = request.requestBody
            ?: return ResponseUtils.responseEmpty(
                code = CODE_EMPTY_TIMING,
                message = "empty timing"
            )
        if (requestBody.length == 0L) {
            return ResponseUtils.responseEmpty(
                code = CODE_EMPTY_TIMING,
                message = "empty timing"
            )
        }
        val bodyData = requestBody.getData()
            ?: return ResponseUtils.responseEmpty(
                code = CODE_REQUEST_DATA_ERROR,
                message = "request data error"
            )
        if (bodyData.isEmpty()) {
            return ResponseUtils.responseEmpty(
                code = CODE_EMPTY_TIMING,
                message = "empty timing"
            )
        }
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