package com.simple.player.activity

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import com.simple.player.R
import com.simple.player.model.Song
import com.simple.player.Util
import com.simple.player.Util.dps
import com.simple.player.drawable.RoundBitmapDrawable2
import com.simple.player.playlist.PlaylistManager
import com.simple.player.util.*
import java.io.File
import java.lang.Exception

class MusicInfo : BaseActivity(), View.OnClickListener {
    private lateinit var album: ImageView
    private var isAlbumExist = false// 判断专辑图片是否存在，若存在，则可以导出
    private var isLoaded = false
    private val src: String? = null
    private var songId: Long = 0
    private var drawable: RoundBitmapDrawable2? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songId = intent.getLongExtra(EXTRA_MUSIC_ID, -1)
        setContentView(R.layout.music_info)
        actionTitle = ("详细信息")
        album = findViewById<View>(R.id.music_info_artwork) as ImageView
        val export = findViewById<View>(R.id.music_info_export_btn) as Button
        val edit = findViewById<View>(R.id.music_info_edit_btn) as Button
        isAlbumExist = true
        export.setOnClickListener(this)
        edit.setOnClickListener(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!isLoaded) {
            init()
            isLoaded = true
        }
    }

    private fun init() {
        //load artwork
        var bitmap: Bitmap? = null
        ProgressHandler.handle (handle = {
            bitmap = try {
                null
//                Glide.with(album)
//                    .asBitmap()
//                    .load(ArtworkProvider.getArtworkUri(PlaylistManager.localPlaylist[songId]!!))
//                    .placeholder(R.drawable.default_artwork)
//                    .skipMemoryCache(true)
//                    .submit(album.width, album.height)
//                    .get()
            } catch (e: Exception) {
                null
            }
        }, after = {
            if (bitmap != null) {
                album.setImageDrawable(RoundBitmapDrawable2(bitmap!!, 8.dps.toFloat()))
            } else {
                isAlbumExist = false
                album.setImageResource(R.drawable.default_artwork)
            }
        })
        //load title and artist
        val song: Song? = PlaylistManager.localPlaylist[songId]
        val title: TextView = findViewById(R.id.music_info_title)
        val artist: TextView = findViewById(R.id.music_info_artist)
        title.text = song?.title
        artist.text = song?.artist
        //load detail information
        val table: TableLayout = findViewById(R.id.music_info_detail)
        val info = arrayOf(
            arrayOf("时长: ", "03:24"),
            arrayOf("比特率: ", "${song?.bitrate}kps"),
            arrayOf("格式: ", song?.type)
        )
        for (g in info.indices) {
            val row = createTableRow()
            val name: TextView = createTextView(info[g][0].toString())
            val `val`: TextView = createTextView(info[g][1].toString())
            row.addView(name)
            row.addView(`val`)
            table.addView(row)
        }
    }

    override fun onClick(p1: View) {
        if (p1.id == R.id.music_info_export_btn) {
            if (!isAlbumExist) {
                Toast.makeText(this, "当前歌曲的专辑图片不存在", Toast.LENGTH_LONG).show()
                return
            }
            var outFile: File? = null
            var bit: Bitmap? = null
            ProgressHandler.handle(before = {
                Util.showProgressDialog(this@MusicInfo, 69, "正在导出……")
            }, after = {
                Util.closeProgressDialog(69)
                if (outFile!!.exists()) AlertDialog.Builder(this@MusicInfo)
                    .setTitle("保存成功")
                    .setMessage("当前歌曲的专辑图片已保存在" + outFile.absolutePath + "中")
                    .setPositiveButton("确定", null)
                    .show()
                if (bit != null && !bit.isRecycled) bit.recycle()
            })
        }
    }

    private fun createTextView(content: String): TextView {
        /* unit sp */
        return TextView(this).apply {
            layoutParams = TableRow.LayoutParams(-2, -2)
            text = content
        }
    }

    private fun createTableRow(): TableRow {
        return TableRow(this).apply {
            layoutParams = TableLayout.LayoutParams(-2, -2)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        drawable ?: return
        val bit = drawable!!.bitmap
        if (!bit.isRecycled) {
            bit.recycle()
        }
    }

    companion object {
        const val EXTRA_MUSIC_ID = "music_id"
    }
}