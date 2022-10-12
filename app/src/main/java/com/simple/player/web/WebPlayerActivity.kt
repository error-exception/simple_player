package com.simple.player.web

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.simple.player.R
import com.simple.player.Util
import com.simple.player.activity.BaseActivity
import com.simple.player.view.IconButton
import com.simple.player.web.MainSocket

/**
 * 当音乐传输启用时，禁用该功能
 */

class WebPlayerActivity : BaseActivity(), View.OnClickListener {
    private val PORT = 8080
    private lateinit var ip: TextView
    private lateinit var state: TextView
    private lateinit var webSwitch: IconButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builderv ().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());

        setContentView(R.layout.web_player)
        actionTitle = "网页播放器"
        ip = findViewById(R.id.web_player_ip)
        state = findViewById(R.id.web_player_state)
        webSwitch = findViewById(R.id.web_player_switch)
        webSwitch.setOnClickListener(this)
        if (MainSocket.isStart) {
            state.text = "ON"
            ip.text = "http://${Util.getInetAddress()}:$PORT"
        } else {
            state.text = "OFF"
            ip.text = ""
        }
        permission()
    }

    private fun permission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 100)
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.web_player_switch) {
            if (!MainSocket.isStart) {
                MainSocket.start()
                state.text = "ON"
                ip.text = "http://${Util.getInetAddress()}:$PORT"
            } else {
                MainSocket.close()
                state.text = "OFF"
                ip.text = ""
            }
        }
    }

}