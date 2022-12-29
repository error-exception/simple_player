package com.simple.player.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.R
import com.simple.player.activity.BaseActivity2
import com.simple.player.model.Song

class KgListScreen(private val activity: BaseActivity2) {

    var onRefresh: (() -> Unit)? = null
    var playPosition = mutableStateOf(-1)
    val list = mutableStateListOf<KgListItem>()
    var onItemClick: ((position: Int, song: Song) -> Unit)? = null

    @Composable
    fun ComposeContent() {
        Scaffold(
            topBar = { TopBar() },
            content = {
                Content()
            }
        )
    }

    @Composable
    private fun TopBar() {
        TopAppBar(
            title = {
                Text(text = "播放列表")
            },
            navigationIcon = {
                IconButton(onClick = { activity.finish() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = "back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { onRefresh?.invoke() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_refresh_24),
                        contentDescription = "refresh",
                        tint = Color.White
                    )
                }
            }
        )
    }

    @Composable
    private fun Content() {
        LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(list) { index, item ->
                ListItem(position = index, item = item)
            }
        }
    }

    @Composable
    private fun ListItem(position: Int, item: KgListItem) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable {
                    onItemClick?.invoke(position, item.song)
                },
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = item.song.title,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                color = if (position == playPosition.value) MaterialTheme.colors.primary else Color.Black
            )
            Text(
                text = item.tag.first,
                fontSize = 12.sp,
                color = item.tag.second,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),

            )
        }

    }

    data class KgListItem(
        val song: Song,
        val tag: Pair<String, Color>
    )
}