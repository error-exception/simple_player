package com.simple.player.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.NRed
import com.simple.player.ui.theme.windowBackground
import com.simple.player.ui.theme.windowBackgroundAlpha

class CheckHeadset : BaseActivity2() {

    private var primaryColor = NRed
    private var leftColor = mutableStateOf(NRed)
    private var rightColor = mutableStateOf(NRed)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTestTheme {
                primaryColor = MaterialTheme.colors.primary
                leftColor.value = primaryColor
                rightColor.value = primaryColor
                Column (modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Text("佩戴检查")
                        },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "返回")
                            }
                        }
                    )
                    CheckHeadsetCompose()
                }
            }
        }

    }

    @Composable
    fun CheckHeadsetCompose() {
        val leftC by remember {
            leftColor
        }
        val rightC by remember {
            rightColor
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface (
                shape = RoundedCornerShape(12.dp),
                color = windowBackgroundAlpha
            ) {
                Row (
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface (shape = CircleShape) {
                        IconButton(
                            modifier = Modifier
                                .size(96.dp)
                                .background(leftC),
                            onClick = {
                                playMusic()
                                leftColor.value = primaryColor
                                rightColor.value = windowBackground
                                SimplePlayer.setVolume(1F, 0f)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "左",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    IconButton(
                        modifier = Modifier
                            .size(0.dp, 96.dp)
                            .weight(1F),
                        onClick = {
                            playMusic()
                            leftColor.value = primaryColor
                            rightColor.value = primaryColor
                            SimplePlayer.setVolume(1F, 1F)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_headset_24),
                            contentDescription = "中",
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Surface(shape = CircleShape) {
                        IconButton(
                            modifier = Modifier
                                .size(96.dp)
                                .background(rightC),
                            onClick = {
                                playMusic()
                                leftColor.value = windowBackground
                                rightColor.value = primaryColor
                                SimplePlayer.setVolume(0f, 1F)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "右",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                }
            }
        }

    }

    private fun playMusic() {
        val player = SimplePlayer
        if (!player.isPlaying) {
            player.start(false)
        }
    }

    override fun onDestroy() {
        SimplePlayer.setVolume(1F, 1F)
        super.onDestroy()
    }

}

