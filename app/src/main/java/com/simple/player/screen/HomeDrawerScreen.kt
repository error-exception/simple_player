package com.simple.player.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.simple.player.R
import com.simple.player.Store
import com.simple.player.model.Song
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.NRed
import com.simple.player.ui.theme.RoundIcon
import com.simple.player.ui.theme.RowSpace
import com.simple.player.util.ArtworkProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class HomeDrawerScreen {

    var onDrawerMenuItemClick: ((item: Item, index: Int) -> Unit)? = null

    private val drawerList = listOf(
        Item(R.drawable.ic_baseline_search_24, "歌曲扫描"),
        Item(R.drawable.ic_baseline_delete_24, "清除专辑封面缓存"),
        Item(R.drawable.ic_baseline_headset_24, "佩戴检查"),
        Item(R.drawable.ic_baseline_developer_mode_24, "开发者"),
        Item(R.drawable.ic_baseline_bug_report_24, "实验性功能"),
        Item(R.drawable.ic_baseline_settings_24, "设置"),
        Item(R.drawable.ic_baseline_web_24, "网页播放器"),
        Item(R.drawable.ic_baseline_exit_to_app_24, "退出")
    )

    private val artwork = mutableListOf<Any?>(null)

    data class Item(
        @DrawableRes val icon: Int,
        val text: String
    )

    @Composable
    fun ComposeContent() {
        HomeDrawerSongInfo()
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for ((i, item) in drawerList.withIndex()) {
                HomeDrawerListItem(item = item, i)
            }
        }

    }



    @Composable
    private fun HomeDrawerSongInfo() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colors.primary),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(artwork)
//                    .crossfade(false)
//                    .allowHardware(true)
//                    .allowRgb565(true)
//                    .error(R.drawable.nc_player_content_default_work_art)
//                    .build(),
//                contentDescription = "",
//                modifier = Modifier
//                    .size(80.dp)
//                    .clip(CircleShape)
//                    .border(2.dp, Color.White, CircleShape),
//            )
            Text(
                text = Store.state.songTitle.value,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = Store.state.songArtist.value,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
        }
    }

    @Composable
    private fun HomeDrawerListItem(item: Item, index: Int) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onDrawerMenuItemClick?.invoke(item, index)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(painter = painterResource(id = item.icon), contentDescription = "icon", tint = MaterialTheme.colors.primary)
                Text(
                    text = item.text,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}