package com.simple.player.util

import com.simple.player.model.TimingInfo
import com.simple.player.model.TimingItem
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.log

object TimingFileParser {

    fun parse(inputStream: InputStream): TimingInfo? {
        var inputStreamReader: InputStreamReader? = null
        var reader: BufferedReader? = null
        val timingInfo = TimingInfo()
        try {
            inputStreamReader = InputStreamReader(inputStream)
            reader = BufferedReader(inputStreamReader)
            while (true) {
                var line = reader.readLine()?.trim()
                line ?: break
                if (line == "[General]") {
                    line = handleGeneral(timingInfo = timingInfo, reader = reader)
                }
                if (line.isNullOrEmpty()) {
                    continue
                }
                if (line == "[Timing]") {
                    handleTiming(timingInfo, reader)
                }
            }
        } catch (e: Exception) {
            return null
        } finally {
            FileUtil.closeStream(reader, inputStreamReader, inputStream)
        }
        if (timingInfo.isModified) {
            return timingInfo
        }
        return null
    }

    private fun handleGeneral(timingInfo: TimingInfo, reader: BufferedReader): String? {
        var line: String? = null
        while (true) {
            line = reader.readLine()
            if (line.isNullOrEmpty()) {
                break
            }
            val pair = line.split(':')
            val (key, value) = pair
            when (key) {
                "bpm" -> timingInfo.bpm = value.toFloat()
                "offset" -> timingInfo.offset = value.toLong()
                "id" -> timingInfo.id = value.toLong()
                "version" -> timingInfo.version = value
                else -> break
            }
            timingInfo.isModified = true
        }
        return line
    }

    private fun handleTiming(timingInfo: TimingInfo, reader: BufferedReader) {
        var line: String? = null
        while (true) {
            line = reader.readLine()
            if (line.isNullOrEmpty()) {
                break
            }
            val attrs = line.split(',')
            println(attrs.toString())
            val timingItem = TimingItem(
                timestamp = attrs[0].toLong(),
                isKiai = attrs[1] == "1"
            )
            timingInfo.timingList += timingItem
            timingInfo.isModified = true
        }
    }


}