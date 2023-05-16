package com.simple.player.web.controller

import com.simple.player.web.ResponseUtils
import com.simple.player.web.service.ImageService
import com.simple.server.GetMapping
import com.simple.server.Param
import com.simple.server.RequestController
import com.simple.server.Response
import com.simple.server.constant.AttributeConstant
import com.simple.server.constant.MimeTypes
import com.simple.server.constant.ResponseState
import com.simple.server.header.MimeType
import com.simple.server.request.Request
import com.simple.server.util.Resource
import com.simple.server.util.logger

class ImageController: RequestController() {

    /**
     * 获取可用的背景图片资源
     */
    @GetMapping("/backgroundList")
    fun backgroundList(): HashMap<String, Any?> {
        val backgrounds = ImageService.availableBackground()
        logger("ImageController")(backgrounds.toString())
        return ResponseUtils.ok(backgrounds)
    }

    /**
     * 通过背景图片的文件名获取图片
     * @param name 背景图片文件名
     */
    @GetMapping("/background")
    @Param(["name", ":request", ":response"])
    fun getBackground(name: String, request: Request, response: Response) {
        val backgroundFile = ImageService.getBackgroundFile(name = name)
        if (backgroundFile == null) {
            response.responseWithEmptyBody(ResponseState.NOT_FOUND)
            return
        }
        val resource = Resource.fromFile(file = backgroundFile, MimeType(MimeTypes.MT_IMAGE_JPEG))
        request.setAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE, resource)
        response.handleRequest(request = request)
    }
}