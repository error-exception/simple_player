package com.simple.player.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.simple.player.R
import com.simple.player.Util
import com.simple.player.activity.*
import com.simple.player.adapter.DialogListAdapter
import com.simple.player.constant.IconCode
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.model.CustomListItemModel
import com.simple.player.model.HomeModel
import com.simple.player.model.IconWithText
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.SimplePlayer
import com.simple.player.util.DialogUtil

class HomeFragment: Fragment(),
    View.OnClickListener,
    MusicEventListener {

    companion object {
        const val TAG = "HomeFragment"
        val MENU_CUSTOM_LIST = arrayOf(
            IconWithText(IconCode.ICON_PLAY_ARROW, "播放"),
            IconWithText(IconCode.ICON_FORMAT_COLOR_TEXT, "重命名"),
            IconWithText(IconCode.ICON_DELETE, "删除")
        )
    }

    private var playBinder = SimplePlayer
    private lateinit var model: HomeModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MusicEvent2.register(this)
        model = HomeModel(view)
        initView()

    }

    override fun onStart() {
        super.onStart()
        val parent: LinearLayout = model.holder.customListArea
        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            val model = view.tag as CustomListItemModel
            val list = PlaylistManager.getList(model.name)!!
            if (model.count != list.count) {
                model.count = list.count
            }
        }
    }

    private fun initView() {
        model.playIcon = if (!playBinder.isPlaying)
            IconCode.ICON_PLAY_ARROW
        else
            IconCode.ICON_PAUSE
        updateInfo()
        model.isSongLike = playBinder.isCurrentSongLike
        model.playMode = playBinder.playMode

        val parent = this
        with(model.holder) {
            favorite.setOnClickListener(parent)
            playlist.setOnClickListener(parent)
            player.setOnClickListener(parent)
            previous.setOnClickListener(parent)
            next.setOnClickListener(parent)
            like.setOnClickListener(parent)
            playMode.setOnClickListener(parent)
            history.setOnClickListener(parent)
            play.setOnClickListener(parent)
            option.setOnClickListener(parent)
        }
        loadCustomPlaylist()
    }

    override fun onSongChanged(newSongId: Long) {
        model.playIcon = IconCode.ICON_PAUSE
        if (!SimplePlayer.isPlaying) {
            SimplePlayer.start()
        }
        updateInfo(newSongId)
        model.isSongLike = playBinder.isCurrentSongLike
    }

    override fun onSongAddToList(songId: Long, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            if (songId == playBinder.currentSong.id) {
                model.isSongLike = true
            }
        }
    }

    override fun onSongRemovedFromList(songId: Long, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            if (songId == playBinder.currentSong.id) {
                model.isSongLike = false
            }
        }
    }

    override fun onPlayModeChanged(oldMode: Int, newMode: Int) {
        model.playMode = newMode
    }

    override fun onMusicPause() {
        model.playIcon = IconCode.ICON_PLAY_ARROW
    }

    override fun onMusicPlay() {
        model.playIcon = IconCode.ICON_PAUSE
    }

    private fun updateInfo(id: Long = -1) {
        var song = playBinder.currentSong
        if (id > 0) {
            song = PlaylistManager.localPlaylist[id]!!
        }
        model.title = song.title
        model.artist = song.artist
        model.artwork = song.id
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.home_slide_menu -> {
//                val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
//                drawer.openDrawer(GravityCompat.START)
            }
//            R.id.home_player -> startActivity(Intent(context, PlayerContentNew::class.java))
            R.id.home_playlist -> {
                val intent = Intent(context, PlaylistActivity::class.java)
                intent.putExtra(PlaylistActivity.EXTRA_LIST_NAME, PlaylistManager.LOCAL_LIST)
                startActivity(intent)
            }
            R.id.home_favorite -> {
                val intent = Intent(context, PlaylistActivity::class.java)
                intent.putExtra(
                    PlaylistActivity.EXTRA_LIST_NAME,
                    PlaylistManager.FAVORITE_LIST)
                intent.setPackage(context?.packageName)
                startActivity(intent)
            }
            R.id.home_play -> playBinder.startOrPause(false)
            R.id.home_next -> playBinder.playNext()
            R.id.home_previous -> playBinder.playPrevious()

            R.id.home_play_mode -> playBinder.nextPlayMode()
            R.id.home_like -> {
                with (PlaylistManager) {
                    val song = playBinder.currentSong
                    if (favoriteList.hasSong(song)) {
                        removeSong(FAVORITE_LIST, song)
                    } else {
                        addSong(FAVORITE_LIST, song)
                    }
                }
            }
            R.id.action_option -> showCreateListDialog()
            R.id.home_history -> startActivity(Intent(context, PlayHistoryActivity::class.java))
        }
    }


    private fun showCreateListDialog() {
        DialogUtil.input(context, R.string.create_play_list, R.string.list_name) { _, _, p3 ->
            if (p3.equals(PlaylistManager.LOCAL_LIST) || p3.equals(PlaylistManager.FAVORITE_LIST)) {
                DialogUtil.alert(context, R.string.error, R.string.wrong_name)
                return@input
            }
            if (PlaylistManager.hasList(p3)) {
                DialogUtil.alert(context, R.string.tips, R.string.the_list_existed)
                return@input
            }
            val playlist = PlaylistManager.create(p3!!)
            if (playlist == null) {
                Util.toast("列表创建失败")
                return@input
            }
            loadCustomPlaylist()
        }
    }

    private fun loadCustomPlaylist() {
        val thiz = context
        val playlists = PlaylistManager.allCustomLists()
        val parent = model.holder.customListArea
        parent.removeAllViews()
        for (playlist in playlists) {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.home_playerlist_item, parent, false)
            val model = CustomListItemModel(view)
            view.tag = model

            view.setOnClickListener {
                val name = playlist.name
                val intent = Intent(thiz, PlaylistActivity::class.java)
                intent.putExtra(PlaylistActivity.EXTRA_LIST_NAME, name)
                startActivity(intent)
            }
            view.setOnLongClickListener {
                val adapter = DialogListAdapter(thiz!!, MENU_CUSTOM_LIST)
                DialogUtil.list(thiz, adapter) { p1, p2 ->
                    handleItemEvent(it, p2, MENU_CUSTOM_LIST, playlist)
                }
                true
            }
            model.name = playlist.name
            model.count = playlist.count
            parent.addView(view)
        }
        playlists.clear()
    }


    private fun handleItemEvent(view: View, position: Int, arr: Array<IconWithText>, playlist: AbsPlaylist) {
        val item = arr[position]
        val model = view.tag as CustomListItemModel
        when (item.text) {
            "播放" -> {
                if (playlist.count == 0) {
                    Util.toast("没有歌曲可播放")
                } else {
                    playBinder.activePlaylist = playlist
                    playBinder.loadMusicOrStart(playlist[0]!!, isNoFade = false)
                }
            }
            "重命名" -> {
                DialogUtil.input(context,
                    titleId = R.string.rename,
                    hintId = R.string.list_name
                ) { _, _, p33 ->
                    p33 ?: return@input
                    if (PlaylistManager.LOCAL_LIST == p33 || PlaylistManager.FAVORITE_LIST == p33) {
                        DialogUtil.alert(context, R.string.error, R.string.wrong_name)
                        return@input
                    }
                    if (PlaylistManager.hasList(p33)) {
                        DialogUtil.alert(context, R.string.tips, R.string.the_list_existed)
                        return@input
                    }
                    PlaylistManager.rename(playlist.name, p33)
                    model.name = p33
                }
            }
            else -> {
                DialogUtil.confirm(context,
                    "删除列表",
                    "是否删除 ${playlist.name} ？",
                    null
                ) { _, _ ->
                    val name: String = playlist.name
                    PlaylistManager.delete(name)
                    model.removed = true
                    applyRemoveCustomListItem()
                }
            }
        }
    }

    private fun applyRemoveCustomListItem() {
        val parent: LinearLayout = model.holder.customListArea
        for (i in 0 until parent.childCount) {
            val view: View = parent.getChildAt(i)
            val model = view.tag as CustomListItemModel
            if (model.removed) {
                parent.removeViewAt(i)
                return
            }
        }
    }

    override fun onDestroy() {
        MusicEvent2.unregister(this)
        super.onDestroy()
    }

}