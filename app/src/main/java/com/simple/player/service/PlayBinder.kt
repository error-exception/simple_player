package com.simple.player.service

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.media.audiofx.AudioEffect
import android.os.Binder
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import com.simple.player.*
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.util.AppConfigure
import com.simple.player.util.ArtworkProvider

class PlayBinder(private val service: SimpleService): Binder(), MusicEventListener {

    private lateinit var mNotification: Notification
    private lateinit var notificationIntent: PendingIntent

    init {
        MusicEvent2.register(this)
    }

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

//        val intent = Intent(service, HomeActivity::class.java).apply {
//            addCategory(Intent.CATEGORY_LAUNCHER)
//            //component = ComponentName(service.packageName, service::class.java.name)
//
//        }
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val intent = Intent(SimpleService.ACTION_NOTIFY_MOVE_TO_FRONT)
        notificationIntent = PendingIntent.getBroadcast(service, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        updateNotification(updateImage = true)
    }

    private fun pushNotification(notificationViews: Pair<RemoteViews, RemoteViews>) {
        mNotification = NotificationCompat.Builder(service, "simple_player").run {
            setSmallIcon(R.drawable.ic_launcher)
            setContentIntent(notificationIntent)
            setCustomContentView(notificationViews.first)
            setCustomBigContentView(notificationViews.second)
            priority = NotificationCompat.PRIORITY_MAX
            build()
        }
        val managerCompat = NotificationManagerCompat.from(service)
        managerCompat.notify(1, mNotification)
        service.startForeground(1, mNotification)
    }

    private val previousIntent = PendingIntent.getBroadcast(service, 0, Intent(SimpleService.ACTION_NOTIFY_PREVIOUS), PendingIntent.FLAG_IMMUTABLE)
    private val playIntent = PendingIntent.getBroadcast(service, 0, Intent(SimpleService.ACTION_NOTIFY_PLAY), PendingIntent.FLAG_IMMUTABLE)
    private val nextIntent = PendingIntent.getBroadcast(service, 0, Intent(SimpleService.ACTION_NOTIFY_NEXT), PendingIntent.FLAG_IMMUTABLE)

    private val smallLayout = RemoteViews(service.packageName, R.layout.notification_small_music)
    private val bigLayout = RemoteViews(service.packageName, R.layout.notification_big_music)
    private fun updateRemoteViews(bitmap: Bitmap? = null): Pair<RemoteViews, RemoteViews> {

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

        if (bitmap != null) {
            smallLayout.setImageViewBitmap(R.id.notification_artwork, bitmap)
            bigLayout.setImageViewBitmap(R.id.notification_artwork, bitmap)
        } else {
            smallLayout.setImageViewResource(R.id.notification_artwork, R.drawable.ic_launcher)
            bigLayout.setImageViewResource(R.id.notification_artwork, R.drawable.ic_launcher)
        }

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
        MusicEvent2.unregister(this)
    }

    override fun onMusicPause() {
        updateNotification()
    }

    override fun onMusicPlay() {
        updateNotification()
    }

    override fun onSongChanged(newSongId: Long) {
        updateNotification(updateImage = true)
    }

    fun updateNotification(updateImage: Boolean = false) {
        if (!updateImage) {
            val notificationViews = updateRemoteViews()
            pushNotification(notificationViews)
            return
        }
        val notificationViews = updateRemoteViews(null)
        pushNotification(notificationViews)
//        val request = ImageRequest.Builder(service.applicationContext)
//            .data(ArtworkProvider.getArtworkDataForCoil(SimplePlayer.currentSong))
//            .size(256)
//            .allowRgb565(true)
//            .allowHardware(true)
//            .error(R.drawable.default_artwork)
//            .target(
//                onSuccess = {
//                    val bitmap = it.toBitmap()
//                    val notificationViews = updateRemoteViews(bitmap)
//                    pushNotification(notificationViews)
//                },
//                onError = {
//                    val notificationViews = updateRemoteViews(null)
//                    pushNotification(notificationViews)
//                }
//            )
//            .build()
//
//        Coil.imageLoader(service.applicationContext).enqueue(request)
    }

    companion object {
        const val ACTION_NOTIFY_BUTTON = "com.simple.player.intent.action.BUTTON_CLICK"
        const val TAG = "PlayBinder"
    }

}