package com.simple.player.json.annota

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class JSONAlias(
    val alias: String
)
