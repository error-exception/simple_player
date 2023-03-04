package com.simple.player.decode

import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import com.simple.player.Util
import com.simple.player.Util.toHexString
import com.simple.player.json.JSON
import com.simple.player.model.KgmBaseInfo
import com.simple.player.model.Song
import com.simple.player.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class KgmConverter(private val song: Song) {

    var state: KgmConvertState = KgmConvertState.Success
    var decryptedData = byteArrayOf()

    suspend fun fetchKgmInfo(hash: String): String? {
        return withContext(Dispatchers.IO) {
            val okHttpClient = OkHttpClient()
            val request = Request.Builder().run {
                url("http://m.kugou.com/app/i/getSongInfo.php?cmd=playInfo&hash=$hash")
                build()
            }
            val response = okHttpClient.newCall(request = request).execute()
            val body = response.body
            body ?: return@withContext null
            return@withContext body.string()
        }

    }

    suspend fun parseInfo(json: String?): KgmBaseInfo {
        return withContext(Dispatchers.Default) {
            val kgmBaseInfo = KgmBaseInfo()
            if (json == null) {
                kgmBaseInfo.valid = false
                return@withContext kgmBaseInfo
            }
            val jsonObject = JSON.parseJSONObject(jsonString = json)
            kgmBaseInfo.let {
                it.title = jsonObject.getString("songName")
                it.artist = jsonObject.getString("singerName")
                it.hash128 = jsonObject.getString("extra.128hash")
                it.hash320 = jsonObject.getString("extra.320hash")
                it.hashHigh = jsonObject.getString("extra.highhash")
                it.hashSQ = jsonObject.getString("extra.sqhash")
                it.albumId = jsonObject.getLong("albumid") ?: -1
                it.albumUrl = jsonObject.getString("album_img")?.replace("{size}", "480")
            }
            kgmBaseInfo.valid = true
            kgmBaseInfo
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun decryptToMemory() {
        return withContext(Dispatchers.Default) {
            val input = FileUtil.openInputStream(uri = Uri.parse(song.path))
            input ?: return@withContext
            val data = KgmDecoder.decode(input)
            data ?: return@withContext
            decryptedData = data
        }
    }

    suspend fun checkForCompleteness(kgmBaseInfo: KgmBaseInfo): Boolean {
        return withContext(Dispatchers.Default) {
            val hexString = Util.md5Sum(decryptedData).toHexString()
            return@withContext hexString == kgmBaseInfo.hash128
                    || hexString == kgmBaseInfo.hash320
                    || hexString == kgmBaseInfo.hashHigh
                    || hexString == kgmBaseInfo.hashSQ
        }
    }

    suspend fun writeToDisk(kgmBaseInfo: KgmBaseInfo) {
        withContext(Dispatchers.IO) {
            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + "/${kgmBaseInfo.artist} - ${kgmBaseInfo.title}.mp3"
            FileUtil.writeBytes(File(outPath), decryptedData)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun convert() {
        val info = fetchKgmInfo(song.path)
        if (info == null) {
            state = KgmConvertState.FetchFailed
        }
        val kgmBaseInfo = parseInfo(info)
        if (kgmBaseInfo.valid) {
            decryptToMemory()
            if (decryptedData.isEmpty()) {
                state = KgmConvertState.DecryptFailed
            }
            val isComplete = checkForCompleteness(kgmBaseInfo = kgmBaseInfo)
            if (!isComplete) {
                state = KgmConvertState.AudioIncomplete
                return
            }
            writeToDisk(kgmBaseInfo = kgmBaseInfo)
        }

    }

}

sealed class KgmConvertState {
    object AudioIncomplete: KgmConvertState()
    object FetchFailed: KgmConvertState()
    object DecryptFailed: KgmConvertState()
    object Success: KgmConvertState()
    object Complete: KgmConvertState()
}