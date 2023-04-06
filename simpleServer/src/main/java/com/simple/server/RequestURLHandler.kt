package com.simple.server

import com.simple.json.JSON
import com.simple.server.constant.AttributeConstant
import com.simple.server.constant.MimeTypes
import com.simple.server.constant.ResponseState
import com.simple.server.header.MimeType
import com.simple.server.util.Resource
import java.lang.reflect.Method

class RequestURLHandler {

    private val mappingParamList = ArrayList<MappingParam>()

    var responseContentType: String? = null

    var method: Method? = null
        set(value) {
            field = value
            value ?: return
            val parameters = value.parameters
            for (i in parameters.indices) {
                mappingParamList += MappingParam().apply {
                    paramName = parameters[i].name
                    paramType = parameters[i].type
                    val requestParam1 = parameters[i].getAnnotation(RequestParam::class.java)
                    requestParam = requestParam1
                }
            }
        }

    var requestController: RequestController? = null

    fun call(request: Request, response: Response) {
        for (i in mappingParamList.indices) {
            val param = mappingParamList[i]
            if (param.paramType.isInstance(request)) {
                param.paramValue = request
                continue
            }
            if (param.paramType.isInstance(response)) {
                param.paramValue = response
                continue
            }
            if (param.requestParam != null) {
                val requestUrl = request.requestUrl
                if (requestUrl != null) {
                    param.paramValue = requestUrl.parameter[param.requestParam!!.name]
                }
            }
        }
        // 构造函数的实参数组，用于传参
        val arr = arrayOfNulls<Any>(mappingParamList.size)
        for (index in arr.indices) {
            arr[index] = mappingParamList[index].paramValue
        }
        val returnValue = method?.invoke(requestController, *arr)

        if (!response.hasResponded) {
            if (returnValue == null) {
                response.responseWithEmptyBody(ResponseState.OK)
                return
            }
            val resource = Resource()
            when (returnValue) {
                is CharSequence -> {
                    val mimeType = MimeType("text/html; charset=${SimpleHttpServerConfig.charset}")
                    resource.setResource(returnValue.toString().toByteArray(SimpleHttpServerConfig.charset), mimeType)
                }

                is Map<*, *> -> {
                    val mimeType = MimeType("${MimeTypes.MT_APPLICATION_JSON}; charset=${SimpleHttpServerConfig.charset}")
                    resource.setResource(JSON.stringify(returnValue).toByteArray(SimpleHttpServerConfig.charset), mimeType)
                }

                is List<*> -> {
                    val mimeType = MimeType("${MimeTypes.MT_APPLICATION_JSON}; charset=${SimpleHttpServerConfig.charset}")
                    resource.setResource(JSON.stringify(returnValue).toByteArray(SimpleHttpServerConfig.charset), mimeType)
                }
                else -> {
                    if (responseContentType != null && returnValue is ByteArray) {
                        val mimeType = MimeType("$responseContentType; charset=${SimpleHttpServerConfig.charset}")
                        resource.setResource(returnValue, mimeType)
                    }
                }
            }
            request.setAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE, resource)
            response.handleRequest(request)
        }
    }

    /**
     * 存储函数单个参数的信息
     */
    class MappingParam {

        lateinit var paramName: String

        lateinit var paramType: Class<*>

        var paramValue: Any? = null

        var requestParam: RequestParam? = null

        override fun toString(): String {
            return "MappingParam(paramName='$paramName', paramType=$paramType, paramValue=$paramValue, requestParam=$requestParam)"
        }

    }

}