package com.simple.player.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.ext.toast
import com.simple.player.lyrics.KRCDecoder
import com.simple.player.ui.theme.ColumnSpace
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.FileUtil

class KrcDecodeActivity: BaseActivity2() {

    companion object {
        const val TAG = "KrcDecodeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTestTheme {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TopAppBar(
                        title = { Text(text = "KRC歌词解密") },
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

    private val content = mutableStateOf("")
    private val text = mutableStateOf("")

    @Composable
    fun Content() {
        val content by remember {
            this.content
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            var text by remember {
                text
            }
            OutlinedTextField(value = text, onValueChange = {
                text = it
            })
            TextButton(onClick = { parse() }) {
                Text(text = "OK")
            }
            ColumnSpace(height = 16.dp)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(text = content)
                }
            }
        }
    }

    private fun parse() {
        val inputStream = FileUtil.openInputStream(text.value)
        inputStream ?: return
        val content = KRCDecoder.INSTANCE.decode(inputStream)
        Log.e(TAG, content)
        this.content.value = content
    }

}