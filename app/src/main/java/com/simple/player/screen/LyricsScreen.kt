package com.simple.player.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun TestLyrics() {
    val width by animateDpAsState(targetValue = 100.dp)
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Hello",
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            text = "Hello",
            color = Color.Red,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.width(20.dp)
        )
    }
}





























