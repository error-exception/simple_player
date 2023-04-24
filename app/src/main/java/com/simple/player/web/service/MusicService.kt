package com.simple.player.web.service

import com.simple.json.JSON
import com.simple.json.JSONObject
import com.simple.player.model.TimingInfo
import com.simple.player.model.TimingItem
import com.simple.player.util.FileUtil
import com.simple.player.util.TimingFileParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.StandardCharsets

object MusicService {

    fun getTimingInfo(songId: Long): TimingInfo? {
        val targetPath = "${FileUtil.mTimingDirectory.absolutePath}/${songId}"
        val inputStream = FileUtil.openInputStream(targetPath)
        inputStream ?: return null
        return TimingFileParser.parse(inputStream = inputStream)
    }

    fun saveTimingInfo(bodyData: ByteArray): Boolean {
        val jsonString = bodyData.toString(StandardCharsets.UTF_8)
        println("jsonString=\n$jsonString")
        val jsonObject = JSON.parseJSONObject(jsonString = jsonString) ?: return false
        val timingList = jsonObject.getJsonArray("timingList")
        println(timingList)
        val list = ArrayList<TimingItem>()
        if (timingList != null && timingList.size != 0) {
            for (i in 0 until timingList.size) {
                val itemObj = timingList[i] as JSONObject
                val timestamp = itemObj.getLong("timestamp") ?: continue
                val isKiai = itemObj.getBoolean("isKiai") ?: continue
                list += TimingItem(
                    timestamp = timestamp,
                    isKiai = isKiai
                )
            }
        }
        val version = jsonObject.getString("version") ?: return false
        val bpm = jsonObject.getFloat("bpm") ?: return false
        val offset = jsonObject.getLong("offset") ?: return false
        val id = jsonObject.getLong("id") ?: return false
        val timingFileContent = StringBuilder().apply {
            append("[General]").append('\n')
                .append("version:").append(version).append('\n')
                .append("bpm:").append(bpm).append('\n')
                .append("id:").append(id).append('\n')
                .append("offset:").append(offset).append('\n')
            append("\n[Timing]").append('\n')
            for (timingItem in list) {
                append(timingItem.timestamp).append(',')
                    .append(if (timingItem.isKiai) 1 else 0)
                    .append('\n')
            }
        }
        val file = File(FileUtil.mTimingDirectory.absolutePath + "/" + id)
        FileUtil.writeTextUTF8(file = file, timingFileContent.toString())
        return true
    }

}