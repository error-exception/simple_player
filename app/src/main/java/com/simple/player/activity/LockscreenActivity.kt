package com.simple.player.activity

import android.app.KeyguardManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager.widget.ViewPager
import com.simple.player.R
import com.simple.player.adapter.LockScreenViewPagerAdapter
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.model.LockscreenModel
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.SimplePlayer
import com.simple.player.util.AppConfigure

class LockscreenActivity : AppCompatActivity(),
    ViewPager.OnPageChangeListener,
        MusicEventListener
{

    private var views: ArrayList<View> = ArrayList(2);
    private lateinit var pager: ViewPager
    private lateinit var viewPagerAdapter: LockScreenViewPagerAdapter
    private lateinit var model: LockscreenModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            this.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        }

        setContentView(R.layout.lock_screen)
        this.window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        pager = findViewById(R.id.lock_screen_view_pager)
        views.add(layoutInflater.inflate(R.layout.lock_screen_item_2, pager, false))
        views.add(layoutInflater.inflate(R.layout.lock_screen_item_1, pager, false))
        viewPagerAdapter = LockScreenViewPagerAdapter(views)
        pager.adapter = viewPagerAdapter
        pager.currentItem = 2
        pager.addOnPageChangeListener(this)

        MusicEvent2.register(this)

        contentView(views[1])
    }

    private fun contentView(view: View) {
        model = LockscreenModel(view)
        model.title = SimplePlayer.currentSong.title
        model.artist = SimplePlayer.currentSong.artist
    }

    override fun onSongChanged(newSongId: Long) {
        val playlist = PlaylistManager.getSongList(AppConfigure.Player.songListId) ?: return
        val song = playlist.getSong(newSongId) ?: return
        model.title = song.title
        model.artist = song.artist
    }

    override fun onBackPressed() {}

    override fun onResume() {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardLocked) {
            finish()
        }
        super.onResume()
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE && position == 0) {
            finish()
        }
    }

    private var position = 1

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        this.position = position
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicEvent2.unregister(this)
        pager.removeOnPageChangeListener(this)
    }
}

