package com.simple.server

import java.nio.charset.Charset

internal object ServerConfig {

    var port: Int = 8888
    var charset: Charset = Charsets.UTF_8
    var resourceDirectory: String = ""
    var enablePartial = true

}