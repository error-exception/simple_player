package com.simple.player.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.activity.WebPlayerActivity
import com.simple.player.ui.theme.RoundIconButton2
import com.simple.player.ui.theme.windowBackgroundAlpha

class WebPlayerScreen(private val activity: WebPlayerActivity) {

    var onSwitchClick: (() -> Unit)? = null
    val serverRunningState = mutableStateOf(false)

    @Composable
    fun ComposeContent() {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(text = "网页播放器")
                },
                navigationIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val runningState = remember {
                    serverRunningState
                }
                Surface(
                    color = windowBackgroundAlpha,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        RoundIconButton2(
                            painter = painterResource(id = R.drawable.ic_baseline_web_24),
                            contentDescription = "",
                            tint = Color.White,
                            contentPadding = 16.dp,
                            color = if (runningState.value) MaterialTheme.colors.primary else Color.Gray,
                            iconSize = 56.dp,
                            onClick = {
                                onSwitchClick?.invoke()
                            }
                        )
                    }
                }

            }
        }
    }
    
}