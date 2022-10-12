package com.simple.player.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.simple.player.*
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.database.SongDao
import com.simple.player.model.Song
import com.simple.player.playlist.PlaylistManager
import com.simple.player.adapter.ResultAdapter
import com.simple.player.util.ArtworkProvider
import com.simple.player.util.ProgressHandler
import java.lang.ref.WeakReference
import java.util.ArrayList

class ScanMusicResult : BaseActivity(), AdapterView.OnItemClickListener {

    private var addImmediately = false
    lateinit var selected: ArrayList<Song>
    private lateinit var mAdapter: ResultAdapter
    private lateinit var mHandler: MyHandler
    private var isActivityStart = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addImmediately = intent.getBooleanExtra(EXTRA_ADD_IMMEDIATELY, false)
        setContentView(R.layout.scan_music_result)
        actionTitle = "搜索结果"
        optionIcon = R.drawable.ic_baseline_check_24
        val list: ListView = findViewById(R.id.scan_music_result_list)

        mAdapter = ResultAdapter(this, mSongs!!)
        mHandler = MyHandler(Looper.getMainLooper(), this)
        list.adapter = mAdapter
        list.onItemClickListener = this
        val info: TextView = findViewById<View>(R.id.scan_music_result_info) as TextView
        info.text = "共 ${mSongs!!.size} 首，请选择所需添加的歌曲"
    }

    override fun onStart() {
        super.onStart()
        if (!isActivityStart && addImmediately) {
            onOptionPressed()
            isActivityStart = true
        }
    }

    override fun onOptionPressed() {
        mHandler.sendEmptyMessage(MSG_INIT_LIST)
    }

    override fun onItemClick(p1: AdapterView<*>?, p2: View, p3: Int, p4: Long) {
        mAdapter.select(p3)
    }

    val adapter: ResultAdapter
        get() = mAdapter

    internal class MyHandler(looper: Looper, activity: ScanMusicResult) : Handler(looper) {
        private val parent: WeakReference<ScanMusicResult> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            val a = parent.get() ?: return
            if (msg.what == MSG_INIT_LIST) {
                ProgressHandler.handle(before = {
                    Util.showProgressDialog(a, 10, "正在初始化列表......")
                    a.selected = a.adapter.selected!!
                }, handle = {
                    val database = SQLiteDatabaseHelper.database
                    database.beginTransaction()
                    database.delete("song_in_list", "list_id = ?", arrayOf(PlaylistManager.localPlaylist.id.toString()))
                    database.delete("song", null, null)
                    val contentValues = ContentValues()
                    val localPlaylist = PlaylistManager.localPlaylist
                    localPlaylist.clear()
                    for (song in a.selected) {
                        contentValues.put(SongDao.ID, song.id)
                        contentValues.put(SongDao.TITLE, song.title)
                        contentValues.put(SongDao.ARTIST, song.artist)
                        contentValues.put(SongDao.TYPE, song.type)
                        contentValues.put(SongDao.PATH, song.path)
                        contentValues.put(SongDao.BITRATE, song.bitrate)
                        database.insertOrThrow("song", null, contentValues)
                        contentValues.clear()
                        contentValues.put("list_id", localPlaylist.id)
                        contentValues.put("song_id", song.id)
                        database.insertOrThrow("song_in_list", null, contentValues)
                        contentValues.clear()
                        localPlaylist += song
                    }
                    database.setTransactionSuccessful()
                    database.endTransaction()
                    ArtworkProvider.clearArtworkCache(a)
                }, after = {
                    Util.closeProgressDialog(10)
                    Util.toast("初始化完成")
                    a.finish()
                })
            }
            super.handleMessage(msg)
        }

    }

    override fun onDestroy() {
        mAdapter.clearSelected()
        mSongs = null
        mHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ADD_IMMEDIATELY = "add_immediately"
        const val MSG_INIT_LIST = 330
        private var mSongs: List<Song>? = null
        fun setResult(result: List<Song>?) {
            mSongs = result
            for (song in mSongs!!) {
                song.isChecked = true
            }
        }
    }
}