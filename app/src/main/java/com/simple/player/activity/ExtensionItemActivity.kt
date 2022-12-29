package com.simple.player.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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

class ExtensionItemActivity: BaseActivity2() {

    private val TAG = javaClass.simpleName

    private val inputText = mutableStateOf("")
    private val extensionList = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessExtensions = AppConfigure.Settings.accessExtension
        for (s in accessExtensions) {
            extensionList.add(s)
        }

        setContent {
            ComposeTestTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(text = "添加拓展名") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                            }
                        }
                    )
                    TextFieldWithButton(
                        buttonText = "OK",
                        value = inputText.value,
                        label = "输入拓展名",
                        onValueChange = {
                            inputText.value = it
                        },
                        onClick = {
                            if (inputText.value.isEmpty() || inputText.value.isBlank()) {
                                return@TextFieldWithButton
                            }
                            val regex = Regex("[a-zA-Z0-4!]+")
                            if (inputText.value.matches(regex)) {
                                extensionList.add(inputText.value)
                                inputText.value = ""
                                val set = extensionList.toMutableSet()
                                AppConfigure.Settings.accessExtension= set
                            } else {
                                toast("输入有误")
                            }
                        }
                    )
                    DeletableItemList(
                        list = { extensionList} ,
                        onDelete = { index, item ->
                            extensionList.removeAt(index)
                            val set = extensionList.toMutableSet()
                            AppConfigure.Settings.accessExtension= set
                        },
                        onEdit = { index, item ->
                            inputText.value = item
                            extensionList.removeAt(index)
                        }
                    )
                }
            }
        }
    }

}