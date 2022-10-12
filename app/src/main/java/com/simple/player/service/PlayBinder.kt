package com.simple.player.service

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.bumptech.glide.Glide
import com.simple.player.*
import com.simple.player.activity.HomeActivity
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.model.MutablePair
import com.simple.player.model.Song
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.util.AppConfigure
import java.lang.Exception

class PlayBinder(private val service: SimpleService):
    Binder(),
    MusicEvent.OnMusicPauseListener,
    MusicEvent.OnMusicPlayListener,
    MusicEvent.OnSongChangedListener
{
    private lateinit var mNotification: Notification
    init {
        MusicEventHandler.register(this)
    }

    private lateinit var notificationIntent: PendingIntent

    fun initNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = service.getString(R.string.app_name)
            val description = service.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("simple_player", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = service.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val (smallLayout, bigLayout) = updateNotificationView()

//        val intent = Intent(service, HomeActivity::class.java).apply {
//            addCategory(Intent.CATEGORY_LAUNCHER)
//            //component = ComponentName(service.packageName, service::class.java.name)
//
//        }
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val intent = Intent(SimpleService.ACTION_NOTIFY_MOVE_TO_FRONT)
        notificationIntent = PendingIntent.getBroadcast(service, 0, intent, 0)
        mNotification = NotificationCompat.Builder(service, "simple_player").run {
            setSmallIcon(R.drawable.ic_launcher)
            setContentIntent(notificationIntent)
            setCustomContentView(smallLayout)
            setCustomBigContentView(bigLayout)
            priority = NotificationCompat.PRIORITY_MAX
            build()
        }
        val managerCompat = NotificationManagerCompat.from(service)
        managerCompat.notify(1, mNotification)
        service.startForeground(1, mNotification)
    }

    fun updateNotification() {
        val (smallLayout, bigLayout) = updateNotificationView()
        mNotification = NotificationCompat.Builder(service, "simple_player").run {
            setSmallIcon(R.drawable.ic_launcher)
            setContentIntent(notificationIntent)
            setCustomContentView(smallLayout)
            setCustomBigContentView(bigLayout)
            priority = NotificationCompat.PRIORITY_MAX
            build()
        }
        val managerCompat = NotificationManagerCompat.from(service)
        managerCompat.notify(1, mNotification)
        service.startForeground(1, mNotification)

    }

    private val previousIntent = PendingIntent.getBroadcast(service, 0, Intent(SimpleService.ACTION_NOTIFY_PREVIOUS), 0)
    private val playIntent = PendingIntent.getBroadcast(service, 0, Intent(SimpleService.ACTION_NOTIFY_PLAY), 0)
    private val nextIntent = PendingIntent.getBroadcast(service, 0, Intent(SimpleService.ACTION_NOTIFY_NEXT), 0)

    private val smallLayout = RemoteViews(service.packageName, R.layout.notification_small_music)
    private val bigLayout = RemoteViews(service.packageName, R.layout.notification_big_music)
    private fun updateNotificationView(): Pair<RemoteViews, RemoteViews> {

        smallLayout.setOnClickPendingIntent(R.id.notification_play, playIntent)
        smallLayout.setOnClickPendingIntent(R.id.notification_next, nextIntent)
        smallLayout.setImageViewResource(
            R.id.notification_play,
            if (SimplePlayer.isPlaying)
                R.drawable.ic_baseline_pause_24
            else
                R.drawable.ic_play_dark
        )
        smallLayout.setTextViewText(R.id.notification_title, SimplePlayer.currentSong.title)
        smallLayout.setTextViewText(R.id.notification_artist, SimplePlayer.currentSong.artist)

        bigLayout.setOnClickPendingIntent(R.id.notification_previous, previousIntent)
        bigLayout.setOnClickPendingIntent(R.id.notification_play, playIntent)
        bigLayout.setOnClickPendingIntent(R.id.notification_next, nextIntent)
        bigLayout.setImageViewResource(
            R.id.notification_play,
            if (SimplePlayer.isPlaying)
                R.drawable.ic_baseline_pause_24
            else
                R.drawable.ic_play_dark
        )
        bigLayout.setTextViewText(R.id.notification_title, SimplePlayer.currentSong.title)
        bigLayout.setTextViewText(R.id.notification_artist, SimplePlayer.currentSong.artist)

        return Pair(smallLayout, bigLayout)
    }

    fun close() {
        AppConfigure.Player.songId = SimplePlayer.currentSong.id
        val i = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, SimplePlayer.playerId)
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, service.packageName)
        service.sendBroadcast(i)
        SimplePlayer.close()
        service.mAudioManager.unregisterMediaButtonEventReceiver(service.mComponentName)
        service.unregisterReceiver(service.mReceiver)
        service.mHandler.removeCallbacksAndMessages(null)
        SQLiteDatabaseHelper.close()
        Util.release()
        MusicEventHandler.unregister(this)
    }

    override fun onMusicPause() {
        updateNotification()
    }

    override fun onMusicPlay() {
        updateNotification()
    }

    override fun onSongChanged(newSongId: Long) {
        updateNotification()
    }

    companion object {
        const val ACTION_NOTIFY_BUTTON = "com.simple.player.intent.action.BUTTON_CLICK"
        const val TAG = "PlayBinder"
    }

}