package com.simple.player.activity

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.UriUtils

class AudioOpenActivity: BaseActivity2() {

    private val testText = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processAudioFile()
        setContent {
            ComposeTestTheme {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopAppBar(title = { Text(text = "Open Test") })
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                            .padding(16.dp)
                    ) {
                        val text = remember {
                            testText
                        }
                        Text(text = text.value)
                    }
                }
            }

        }
    }

    private fun processAudioFile() {

        val dataString = intent.dataString ?: return
        val uri = Uri.parse(dataString)
        testText.value = """
            $dataString
            ${UriUtils.getRealFilePath(this, uri)}
        """.trimIndent()
    }



}