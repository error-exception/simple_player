package com.simple.player.service

import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.app.*
import android.content.*
import android.os.*
import android.util.Log
import com.simple.player.*
import com.simple.player.handler.SimpleHandler
import org.jetbrains.annotations.NotNull


class SimpleService : Service() {

    internal lateinit var bin: PlayBinder
    internal lateinit var mAudioManager: AudioManager
    internal lateinit var mComponentName: ComponentName
    internal lateinit var mReceiver: PlayActionReceiver
    internal val mHandler: ServiceHandler = ServiceHandler(Looper.getMainLooper(), this)

    private lateinit var mNotifyManager: NotificationManager

    override fun onCreate() {
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        mComponentName = ComponentName(this, SimpleBroadcastReceiver::class.java)
        mNotifyManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val filter = IntentFilter(SimpleBroadcastReceiver.ACTION_MEDIA_BUTTON)
        with (filter) {
            priority = 1000
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(PlayBinder.ACTION_NOTIFY_BUTTON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(VOLUME_CHANGED_ACTION)
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction(ACTION_NOTIFY_PREVIOUS)
            addAction(ACTION_NOTIFY_NEXT)
            addAction(ACTION_NOTIFY_PLAY)
            addAction(ACTION_NOTIFY_MOVE_TO_FRONT)
        }
        bin = PlayBinder(this)
        mReceiver = PlayActionReceiver(this)
        registerReceiver(mReceiver, filter)
        mAudioManager.registerMediaButtonEventReceiver(mComponentName)

        val i = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, SimplePlayer.playerId)
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        sendBroadcast(i)
        SimplePlayer.launch()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(p1: Intent): IBinder {
        bin.initNotification()
        return bin
    }

    companion object {
        const val ACTION_NOTIFY_MOVE_TO_FRONT = "com.simple.player.NOTIFY_MOVE_TO_FRONT"
        const val TAG = "SimpleService"
        const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
        const val ACTION_SIMPLE_SERVICE = "com.simple.player.SIMPLE_SERVICE"
        const val ACTION_NOTIFY_PREVIOUS = "com.simple.player.NOTIFY_PREVIOUS"
        const val ACTION_NOTIFY_PLAY = "com.simple.player.NOTIFY_PLAY"
        const val ACTION_NOTIFY_NEXT = "com.simple.player.NOTIFY_NEXT"

        const val MSG_UPDATE_NOTIFICATION = 1
        const val MSG_EAR_PHONE_MSG = 3
        const val THREAD_MSG = 2

        internal class ServiceHandler(@NotNull looper: Looper, parent: SimpleService): SimpleHandler<SimpleService>(looper, parent) {
            private var count = 0
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val bin: PlayBinder? = parent?.bin
                when (msg.what) {
                    MSG_UPDATE_NOTIFICATION -> {
                        bin?.updateNotification()
                    }
                    MSG_EAR_PHONE_MSG -> {
                        count++
                        sendEmptyMessage(THREAD_MSG)
                    }
                    THREAD_MSG -> {
                        if (msg.arg1 == count) {
                            when (count) {
                                1 -> {
                                    SimplePlayer.startOrPause()
                                }
                                2 -> {
                                    SimplePlayer.playNext()
                                }
                                3 -> {
                                    SimplePlayer.playPrevious()
                                }
                                else -> {
                                    count = 0
                                    return
                                }
                            }
                            count = 0
                        } else {
                            val message = Message.obtain()
                            message.arg1 = count
                            message.what = THREAD_MSG
                            sendMessageDelayed(message, 500)
                        }
                    }
                    else -> {
                        Log.e("a", "Audio: $msg")
                    }
                }
            }
        }
    }
}