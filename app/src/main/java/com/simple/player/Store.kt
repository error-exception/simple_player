package com.simple.player

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.MediaExtractor
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import coil.ImageLoader
import com.simple.player.model.Song
import com.simple.player.service.SimplePlayer


object Store {
    lateinit var applicationContext: Context
    lateinit var iconTypeface: Typeface
    var taskId = 0
    val state = GlobalState()
    var screenWidth: Int = 0
    var screenHeight: Int = 0
}

data class GlobalState(
    val playState: MutableState<Boolean> = mutableStateOf(false),
    val songTitle: MutableState<String> = mutableStateOf(""),
    val songArtist: MutableState<String> = mutableStateOf(""),
    val currentPlayMode: MutableState<Int> = mutableStateOf(2),
    val duration: MutableState<Int> = mutableStateOf(0),
    val songUriString: MutableState<String> = mutableStateOf(""),
    val isCurrentSongLike: MutableState<Boolean> = mutableStateOf(false),
    val currentSongId: MutableState<Long> = mutableStateOf(0),
)