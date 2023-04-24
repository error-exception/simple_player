package com.simple.server

interface Interceptor {

    fun afterController(result: Any?): Any?

}