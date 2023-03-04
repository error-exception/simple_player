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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.simple.player.R
import com.simple.player.database.ScanConfigDao
import com.simple.player.ext.toast
import com.simple.player.scan.ScanConfigItem
import com.simple.player.screen.DeletableItemList
import com.simple.player.screen.TextFieldWithButton
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.AppConfigure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ExtensionItemActivity: BaseActivity2() {

    private val TAG = javaClass.simpleName

    private val inputText = mutableStateOf("")

    private val extensionList = mutableStateListOf<ScanConfigItem>()

    private var isRename = false

    private var oldExtensionName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LaunchedEffect(key1 = true) {
                val accessExtensions = ScanConfigDao.queryValuesByType(ScanConfigDao.TYPE_EXTENSION_NAME)
                for (s in accessExtensions) {
                    extensionList.add(s)
                }
            }
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
                                val item = ScanConfigItem(
                                    id = -1,
                                    value = mutableStateOf(inputText.value),
                                    type = ScanConfigDao.TYPE_EXTENSION_NAME,
                                    isValid = mutableStateOf(true)
                                )
                                extensionList.add(item)
                                inputText.value = ""
                                if (isRename) {
                                    isRename = false
                                    ScanConfigDao.updateItemValue(
                                        oldValue = oldExtensionName,
                                        type = item.type,
                                        newValue = item.value.value
                                    )
                                } else {
                                    ScanConfigDao.insertItem(
                                        value = item.value.value,
                                        type = item.type
                                    )
                                }
                            } else {
                                toast("输入有误")
                            }
                        }
                    )
                    DeletableItemList(
                        list = { extensionList} ,
                        onDelete = { index, item ->
                            extensionList.removeAt(index)
                            ScanConfigDao.deleteItem(
                                value = item.value.value,
                                type = item.type
                            )
                        },
                        onEdit = { index, item ->
                            inputText.value = item.value.value
                            extensionList.removeAt(index)
                            isRename = true
                            oldExtensionName = item.value.value
                        },
                        onSwitch = { _, isOn, item ->
                            MainScope().launch {
                                withContext(Dispatchers.IO) {
                                    ScanConfigDao.setValid(
                                        value = item.value.value,
                                        type = item.type,
                                        valid = isOn
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

}