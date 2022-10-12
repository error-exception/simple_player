package com.simple.player.model

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.simple.player.R
import com.simple.player.Util.dps
import com.simple.player.constant.IconCode
import com.simple.player.drawable.RoundBitmapDrawable2
import com.simple.player.playlist.PlaylistManager
import com.simple.player.util.ArtworkProvider
import com.simple.player.util.ProgressHandler
import com.simple.player.view.IconButton
import java.lang.Exception

class HomeModel(private val view: View){

    val holder = ViewHolder(view)
    private val modeIcon = arrayOf(
        "\ue041", "\ue043", "\ue040"
    )

    var playMode: Int = 0
        set(value) {
            field = value
            holder.playMode.icon = modeIcon[value]
        }

    var isSongLike: Boolean = false
        set(value) {
            field = value
            if (value) {
                holder.like.icon = IconCode.ICON_FAVORITE
                holder.like.setTextColor(Color.RED)
            } else {
                holder.like.icon = IconCode.ICON_FAVORITE_BORDER
                holder.like.setTextColor(Color.BLACK)
            }
        }

    var title: String = ""
        set(value) {
            holder.title.text = value
            field = value
        }
    var artist: String = ""
        set(value) {
            field = value
            holder.artist.text = value
        }

    var playIcon: String = ""
        set(value) {
            holder.play.text = value
            field = value
        }



    var artwork: Long = 0
        set(value) {
            field = value
            var bitmap: Bitmap? = null
            //var color = Color.WHITE
            ProgressHandler.handle (handle = {
                bitmap = try {
                    Glide.with(holder.artwork)
                        .asBitmap()
                        .load(ArtworkProvider.getArtworkUri(PlaylistManager.localPlaylist[value]!!))
                        .placeholder(R.drawable.default_artwork)
                        .skipMemoryCache(true)
                        .submit(holder.artwork.width, holder.artwork.height)
                        .get()
                } catch (e: Exception) {
                    null
                }
//                color = if (bitmap != null) {
//                    val palette = Palette.from(bitmap!!).generate()
//                    palette.getMutedColor(Color.WHITE)
//                } else {
//                    Color.WHITE
//                }
            }, after = {
                if (bitmap != null) {
                    holder.artwork.setImageDrawable(RoundBitmapDrawable2(bitmap!!, 8.dps.toFloat()))
                } else {
                    holder.artwork.setImageResource(R.drawable.default_artwork)
                }
                //holder.player.setBackgroundColor(color)
            })

        }

    class ViewHolder(view: View) {
        var title: TextView = view.findViewById(R.id.home_title)
        var artist: TextView = view.findViewById(R.id.home_artist)
        var artwork: ImageView = view.findViewById(R.id.home_artwork)
        var play: IconButton = view.findViewById(R.id.home_play)
        var like: IconButton = view.findViewById(R.id.home_like)
        var playMode: IconButton = view.findViewById(R.id.home_play_mode)
        var customListArea: LinearLayout = view.findViewById(R.id.home_custom_list_area)
        var playlist: LinearLayout = view.findViewById(R.id.home_playlist)
        var player: LinearLayout = view.findViewById(R.id.home_player)
        var previous: IconButton = view.findViewById(R.id.home_previous)
        var next: IconButton = view.findViewById(R.id.home_next)
        var history: LinearLayout = view.findViewById(R.id.home_history)
        var favorite: LinearLayout = view.findViewById(R.id.home_favorite)
        var option: IconButton = view.findViewById(R.id.action_option)
    }
}