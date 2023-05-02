package com.simple.server

import com.simple.json.JSON
import com.simple.server.constant.AttributeConstant
import com.simple.server.constant.MimeTypes
import com.simple.server.constant.ResponseState
import com.simple.server.header.MimeType
import com.simple.server.util.Resource
import com.simple.server.request.Request
import java.lang.Exception
import java.lang.reflect.Method

class RequestURLHandler {

    private val mappingParamList = ArrayList<MappingParam>()

    var responseContentType: String? = null

    var method: Method? = null
        set(value) {
            field = value
            value ?: return
            val params = value.getAnnotation(Param::class.java)
            if (params != null) {
                val parameterTypes = value.parameterTypes
                for ((index, parameterType) in parameterTypes.withIndex()) {
                    mappingParamList += MappingParam().apply {
                        paramName = ""
                        paramType = parameterType
                        val name = params.names[index]
                        requestParam = name
                    }
                }
            }
        }

    var requestController: RequestController? = null

    fun call(request: Request, response: Response, server: Server) {
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
                val requestUrl = request.getHttpUrl()
                val value = requestUrl.queryMap[param.requestParam!!]
                println(param.paramType.name)
                try {
                    when (param.paramType.name) {
                        "long"   -> param.paramValue = value?.toLong()
                        "int"    -> param.paramValue = value?.toInt()
                        "short"  -> param.paramValue = value?.toShort()
                        "float"  -> param.paramValue = value?.toFloat()
                        "double" -> param.paramValue = value?.toDouble()
                        "byte"   -> param.paramValue = value?.toByte()
                        "boolean"   -> param.paramValue = value?.toBoolean()
                        "char"   -> param.paramValue = value?.get(0)
                        else     -> param.paramValue = value
                    }
                } catch (e: Exception) {
                    param.paramValue = value
                }
            }
        }
        // 构造函数的实参数组，用于传参
        val arr = Array(mappingParamList.size) {
            mappingParamList[it].paramValue
        }
        var returnValue = method?.invoke(requestController, *arr)

        val interceptor = server.interceptor
        if (interceptor != null) {
            returnValue = interceptor.afterController(returnValue)
        }

        if (!response.hasResponded) {
            if (returnValue == null) {
                response.responseWithEmptyBody(ResponseState.OK)
                return
            }
            var resource: Resource? = null
            var mimeType: MimeType? = null
            when (returnValue) {
                is CharSequence -> {
                    mimeType = MimeType(MimeTypes.MT_TEXT_HTML, ServerConfig.charset)
                    resource = Resource.fromString(returnValue.toString(), mimeType)
                }

                is Map<*, *>, is List<*> -> {
                    mimeType = MimeType(MimeTypes.MT_APPLICATION_JSON, ServerConfig.charset)
                    resource = Resource.fromString(JSON.stringify(returnValue), mimeType)
                }
                else -> {
                    if (responseContentType != null && returnValue is ByteArray) {
                        mimeType = MimeType(responseContentType!!, ServerConfig.charset)
                        resource = Resource.fromBytes(returnValue, mimeType)
                    }
                }
            }
            if (resource != null) {
                request.setAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE, resource)
            }
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

        var requestParam: String? = null

        override fun toString(): String {
            return "MappingParam(paramName='$paramName', paramType=$paramType, paramValue=$paramValue, requestParam=$requestParam)"
        }

    }

}