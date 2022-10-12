package com.simple.player.service

import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Parcelable
import android.view.KeyEvent
import com.simple.player.SimpleBroadcastReceiver
import com.simple.player.Store
import com.simple.player.activity.LockscreenActivity
import com.simple.player.util.AppConfigure

internal class PlayActionReceiver(private val service: SimpleService): BroadcastReceiver() {

    private var isScreenOn = true
    internal var isNeverPlayed = true
    private var lastVolume: Int

    init {
        val audioManager = service.getSystemService(Service.AUDIO_SERVICE) as AudioManager
        lastVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    private fun getCurrentVolume(): Int {
        val audioManager = service.getSystemService(Service.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    override fun onReceive(context: Context, p2: Intent) {
        when (p2.action) {
            Intent.ACTION_SCREEN_OFF -> handleScreenOff()
            AudioManager.ACTION_HEADSET_PLUG -> handleHeadsetPlug(p2)
            SimpleService.VOLUME_CHANGED_ACTION -> handleVolumeChange(p2)
            Intent.ACTION_SCREEN_ON -> isScreenOn = true
            AudioManager.ACTION_AUDIO_BECOMING_NOISY -> handleAudioBecomingNoisy()
            SimpleBroadcastReceiver.ACTION_MEDIA_BUTTON -> handleMediaButton(p2)
            PlayBinder.ACTION_NOTIFY_BUTTON -> handleNotifyButton(p2)
            SimpleService.ACTION_NOTIFY_PREVIOUS -> {
                SimplePlayer.playPrevious()
            }
            SimpleService.ACTION_NOTIFY_PLAY -> {
                SimplePlayer.startOrPause()
            }
            SimpleService.ACTION_NOTIFY_NEXT -> {
                SimplePlayer.playNext()
            }
            SimpleService.ACTION_NOTIFY_MOVE_TO_FRONT -> {
                val service1 = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                service1.moveTaskToFront(Store.taskId, ActivityManager.MOVE_TASK_WITH_HOME)
            }
        }
    }

    private fun handleScreenOff() {
        val intent = Intent(service.applicationContext, LockscreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        service.applicationContext.startActivity(intent)
        isScreenOn = false
    }

    private fun handleAudioBecomingNoisy() {
        SimplePlayer.pause(isNoFade = true)
    }

    private fun handleMediaButton(p2: Intent) {
        val event = p2.getParcelableExtra<Parcelable>(Intent.EXTRA_KEY_EVENT) as KeyEvent?
        if (event!!.action != KeyEvent.ACTION_UP) return
        service.mHandler.sendEmptyMessage(SimpleService.MSG_EAR_PHONE_MSG)
        //abortBroadcast()
        return
    }

    private fun handleNotifyButton(p2: Intent) {
        val extra = p2.getStringExtra("notify")
        if (extra == "play") {
            SimplePlayer.startOrPause(false)
        } else if (extra == "next") {
            SimplePlayer.playNext()
        }
    }

    private fun handleVolumeChange(p2: Intent) {
        val isOn = AppConfigure.Settings.volumeShuffle
        val volume = getCurrentVolume()
        if (!isOn) {
            return
        }
        if (isScreenOn) {
            lastVolume = volume
            return
        }
        if (volume == lastVolume) {
            return
        }
        if (lastVolume > volume) {
            if (!isScreenOn) {
                SimplePlayer.playNext()
                val audioManager = service.getSystemService(Service.AUDIO_SERVICE) as AudioManager
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE)
            }
        }
        if (lastVolume < volume) {
            if (!isScreenOn) {
                SimplePlayer.playPrevious()
                val audioManager = service.getSystemService(Service.AUDIO_SERVICE) as AudioManager
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE)
            }
        }
    }

    private fun handleHeadsetPlug(p2: Intent) {
        if (!SimplePlayer.hasBeenStarted) {
            return
        }
        val state = p2.getIntExtra("state", 0)
        val isOn = AppConfigure.Settings.headsetAutoPlay

        SimplePlayer.volume = 1F
        if (state == 1 && isOn) {
            if (!SimplePlayer.isPlaying) {
                SimplePlayer.start(false)
            }
        }
    }
}
