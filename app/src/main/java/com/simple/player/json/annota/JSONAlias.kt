package com.simple.player.json.annota

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JSONAlias(
    val alias: String
)
