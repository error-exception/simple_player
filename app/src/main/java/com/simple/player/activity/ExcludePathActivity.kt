package com.simple.player.activity

import android.os.Bundle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ExcludePathActivity: BaseActivity2() {

    private val inputText = mutableStateOf("")
    private val pathList = mutableStateListOf<ScanConfigItem>()

    private var isRename = false
    private var oldPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LaunchedEffect(key1 = true) {
                val excludePath = ScanConfigDao.queryValuesByType(ScanConfigDao.TYPE_EXCLUDE_PATH)
                for (s in excludePath) {
                    pathList.add(s)
                }
            }
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
                                val item = ScanConfigItem(
                                    id = -1,
                                    value = mutableStateOf(inputText.value),
                                    type = ScanConfigDao.TYPE_EXCLUDE_PATH,
                                    isValid = mutableStateOf(true)
                                )
                                pathList.add(item)
                                inputText.value = ""
                                if (isRename) {
                                    isRename = false
                                    ScanConfigDao.updateItemValue(
                                        oldValue = oldPath,
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
                                toast("该路径不存在")
                            }
                        }
                    )
                    DeletableItemList(
                        list = { pathList } ,
                        onDelete = { index, item ->
                            pathList.removeAt(index)
                            ScanConfigDao.deleteItem(
                                value = item.value.value,
                                type = item.type
                            )
                        },
                        onEdit = { index, item ->
                            inputText.value = item.value.value
                            pathList.removeAt(index)
                            isRename = true
                            oldPath = item.value.value
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