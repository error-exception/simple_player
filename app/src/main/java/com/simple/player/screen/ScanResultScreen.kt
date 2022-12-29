package com.simple.player.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.R
import com.simple.player.model.Song
import com.simple.player.ui.theme.RowSpace

class ScanResultScreen(private val activity: ComponentActivity) {

    var onItemClick: ((position: Int) -> Unit)? = null
    var onApplySelected: ((selectedList: ArrayList<Song>) -> Unit)? = null

    private val unselectedMap = HashMap<Int, MutableState<Boolean>>()
    private val resultList = mutableStateListOf<Song>()

    @Composable
    fun ComposeContent() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "扫描结果") },
                    navigationIcon = {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            onApplySelected ?: return@IconButton
                            val list = ArrayList<Song>()
                            for (index in resultList.indices) {
                                val value = unselectedMap[index]
                                if (value != null && value.value) {
                                    continue
                                }
                                list.add(resultList[index])
                            }
                            onApplySelected?.invoke(list)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_check_24),
                                contentDescription = "back",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            content = {
                Content()
            }
        )
    }

    @Composable
    private fun Content() {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "共扫描到 ${resultList.size} 首，请选择要添加的歌曲",
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(resultList) { index, item ->
                    ListItem(position = index, item = item)
                }
            }
        }
    }

    @Composable
    private fun ListItem(position: Int, item: Song) {
        val checked = remember {
            val c = unselectedMap[position]
            if (c == null) {
                unselectedMap[position] = mutableStateOf(false)
            }
            unselectedMap[position]!!
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable {
                checked.value = !checked.value
                onItemClick?.invoke(position)
            }
        ) {
            Box(modifier = Modifier.size(width = 56.dp, height = 64.dp), contentAlignment = Alignment.Center) {
                Checkbox(checked = !checked.value, onCheckedChange = { checked.value = !checked.value })
            }
            RowSpace(width = 8.dp)
            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                SongTitle(title = item.title)
                SongArtist(artist = item.artist)
            }
        }
    }


    @Composable
    private fun SongTitle(title: String) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = Color.Black,
            maxLines = 1,
        )
    }

    @Composable
    fun SongArtist(artist: String) {
        Text(
            text = artist,
            color = Color(0xFF808080),
            maxLines = 1,
            fontSize = 12.sp
        )
    }

    fun setResultSource(list: List<Song>) {
        resultList.addAll(list)
    }

}