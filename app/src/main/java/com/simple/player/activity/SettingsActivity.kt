package com.simple.player.activity

import android.app.Service
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.simple.player.constant.PreferencesData
import com.simple.player.R
import com.simple.player.ext.toast
import com.simple.player.service.PlayBinder
import com.simple.player.service.SimplePlayer
import com.simple.player.service.SimpleService
import com.simple.player.util.AppConfigure
import com.simple.player.util.DialogUtil

class SettingsActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences
    private var oldAccessExtension = AppConfigure.Settings.accessExtension

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent()
        intent.setPackage(packageName)
        intent.action = SimpleService.ACTION_SIMPLE_SERVICE
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(PreferencesData.CONFIG_SETTINGS, MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        val isOn = preferences!!.getBoolean(PreferencesData.SETTINGS_ENABLE_SECOND_VOLUME, false)
        if (key == PreferencesData.SETTINGS_ENABLE_SECOND_VOLUME) {
            SimplePlayer.volume = 1F
        }
        if (isOn && key == PreferencesData.SETTINGS_SECOND_VOLUME) {
            SimplePlayer.volume = 1F
        }
        if (key == PreferencesData.SETTINGS_MUSIC_SOURCE) {
            startActivity(Intent(this, ScanMusicActivity::class.java).apply {
                putExtra(ScanMusicActivity.EXTRA_SCAN_IMMEDIATELY, true)
                putExtra(ScanMusicActivity.EXTRA_AUTO_ADD, true)
            })
        }
        if (key == PreferencesData.SETTINGS_ACCESS_EXTENSION) {
            val accessExtension = AppConfigure.Settings.accessExtension
            val list = accessExtension.split(",")
            val regex = Regex("[.][a-zA-Z0-3]+");
            for (s in list) {
                val e = s.trim()
                if (!e.matches(regex)) {
                    toast("输入有误！")
                    // 临时注销监听，防止重复执行此段代码
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
                    AppConfigure.Settings.accessExtension = oldAccessExtension
                    sharedPreferences.registerOnSharedPreferenceChangeListener(this)
                    break
                }
            }
        }
        if (key == PreferencesData.SETTINGS_EXCLUDE_PATH) {
            val excludePath = AppConfigure.Settings.excludePath
            val list = excludePath.split("\n")
            val builder = StringBuilder()
            for (s in list) {
                val e = s.trim()
                if (s.isNotEmpty() || s.isNotBlank()) {
                    builder.append(s).append('\n')
                }
            }
            builder.deleteCharAt(builder.length - 1)
            AppConfigure.Settings.excludePath = builder.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}