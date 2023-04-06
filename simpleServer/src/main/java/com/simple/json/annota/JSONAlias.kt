package com.simple.json.annota

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class JSONAlias(
    val alias: String
)
