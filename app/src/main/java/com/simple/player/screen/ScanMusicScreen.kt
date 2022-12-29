package com.simple.player.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.R
import com.simple.player.activity.ScanMusicActivity
import com.simple.player.constant.PreferencesData
import com.simple.player.ui.theme.ColumnSpace
import com.simple.player.ui.theme.RowSpace
import com.simple.player.ui.theme.windowBackgroundAlpha
import com.simple.player.util.AppConfigure

class ScanMusicScreen(private val activity2: ScanMusicActivity) {

    private val scanning = mutableStateOf(false)
    private val songCount = mutableStateOf(0)
    private val scanOver = mutableStateOf(false)
    private val scanMode = mutableStateOf(MODE_FULL_SCAN)

    var onScanSettingsClick: (() -> Unit)? = null
    var onStartClick: ((mode: Int) -> Unit)? = null

    @Composable
    fun ComposeContent() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopBar() },
            content = {
                Content()
            }
        )
    }

    @Composable
    private fun TopBar() {
        TopAppBar(
            title = { Text(text = "音乐扫描") },
            navigationIcon = {
                IconButton(onClick = { activity2.finish() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = "back"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { onScanSettingsClick?.invoke() },
                    enabled = !scanning.value
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_settings_24),
                        contentDescription = "scan settings",
                        tint = Color.White
                    )
                }
            }
        )
    }

    @Composable
    private fun Content() {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = windowBackgroundAlpha
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Surface(shape = CircleShape) {
                        SearchButton()
                    }
                    RowSpace(width = 16.dp)
                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .height(128.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SongCounter()
                        Text(
                            text = "歌曲数量",
                            modifier = Modifier.padding(top = 8.dp),
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            ColumnSpace(height = 16.dp)
            ScanMode()

        }
    }

    @Composable
    private fun ScanMode() {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                modifier = Modifier
                    .weight(1F)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { scanMode.value = MODE_FULL_SCAN },
                color = windowBackgroundAlpha
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = scanMode.value == MODE_FULL_SCAN,
                        onClick = {
                            scanMode.value = MODE_FULL_SCAN
                        }
                    )
                    Text(
                        text = "全盘扫描",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Surface(
                color = windowBackgroundAlpha,
                modifier = Modifier
                    .weight(1F)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        enabled = AppConfigure.Settings.musicSource == PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_EXTERNAL_STORAGE
                    ) {
                        scanMode.value = MODE_UPDATE_SCAN
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = scanMode.value == MODE_UPDATE_SCAN,
                        onClick = {
                            scanMode.value = MODE_UPDATE_SCAN
                        },
                        enabled = AppConfigure.Settings.musicSource == PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_EXTERNAL_STORAGE,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colors.primary,
                            unselectedColor = MaterialTheme.colors.primary,
                        )
                    )
                    Text(
                        text = "更新",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

        }
    }

    @Composable
    private fun SearchButton() {
        if (scanning.value) {
            TextButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .size(128.dp)
                    .background(MaterialTheme.colors.primary)
            ) {
                Text(
                    text = if (scanOver.value) "OK" else "扫描中",
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        } else {
            IconButton(
                onClick = {
                    scanning.value = true
                    onStartClick?.invoke(scanMode.value)
                },
                modifier = Modifier
                    .size(128.dp)
                    .background(MaterialTheme.colors.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "scan",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }
    }

    @Composable
    private fun SongCounter() {
        Text(
            text = songCount.value.toString(),
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 32.sp
        )
    }

    fun setSongCount(count: Int) {
        songCount.value = count
    }

    fun setScanOver(flag: Boolean) {
        scanOver.value = flag
    }

    fun isScanOver(): Boolean = scanOver.value

    companion object {

        const val MODE_FULL_SCAN = 1
        const val MODE_UPDATE_SCAN = 2

    }

}