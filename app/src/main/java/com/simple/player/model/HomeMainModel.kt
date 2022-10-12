package com.simple.player.model

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.runtime.mutableStateOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.simple.player.R
import com.simple.player.service.SimplePlayer

class HomeMainModel {

    val playState = mutableStateOf(SimplePlayer.isPlaying)

    val playMode = mutableStateOf(SimplePlayer.playMode)

    val likeState = mutableStateOf(SimplePlayer.isCurrentSongLike)

    val headImageState = mutableStateOf(0)

    val currentSong = mutableStateOf(SimplePlayer.currentSong)

}