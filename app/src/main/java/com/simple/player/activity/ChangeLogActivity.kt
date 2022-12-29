package com.simple.player.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.FileUtil

class ChangeLogActivity: BaseActivity2() {

    private val changeLogContent = mutableStateOf("")

    private var isWindowFocused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TopAppBar(
                        title = { Text(text = "更新日志") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                            }
                        }
                    )
                    Content()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!isWindowFocused && hasFocus) {
            isWindowFocused = true
            val inputStream = assets.open("text/ChangeLog.txt")
            changeLogContent.value = FileUtil.readTextUTF8(inputStream)
        }
    }

    @Composable
    fun Content() {
        LazyColumn(
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(text = changeLogContent.value)
            }
        }
    }
}