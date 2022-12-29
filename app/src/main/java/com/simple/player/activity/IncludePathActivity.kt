package com.simple.player.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.simple.player.R
import com.simple.player.ext.toast
import com.simple.player.screen.DeletableItemList
import com.simple.player.screen.TextFieldWithButton
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.AppConfigure
import java.io.File

class IncludePathActivity: BaseActivity2() {

    private val inputText = mutableStateOf("")
    private val pathList = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val includePath = AppConfigure.Settings.includePath
        for (s in includePath) {
            pathList.add(s)
        }

        setContent {
            ComposeTestTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(text = "添加路径") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                            }
                        }
                    )
                    TextFieldWithButton(
                        buttonText = "OK",
                        value = inputText.value,
                        label = "输入路径",
                        onValueChange = {
                            inputText.value = it
                        },
                        onClick = {
                            if (inputText.value.isEmpty() || inputText.value.isBlank()) {
                                return@TextFieldWithButton
                            }
                            if (File(inputText.value).exists()) {
                                pathList.add(inputText.value)
                                inputText.value = ""
                                val set = pathList.toMutableSet()
                                AppConfigure.Settings.includePath = set
                            } else {
                                toast("该路径不存在")
                            }
                        }
                    )
                    DeletableItemList(
                        list = { pathList} ,
                        onDelete = { index, item ->
                            pathList.removeAt(index)
                            val set = pathList.toMutableSet()
                            AppConfigure.Settings.includePath = set
                        },
                        onEdit = { index, item ->
                            inputText.value = item
                            pathList.removeAt(index)
                        }
                    )
                }
            }
        }
    }

}