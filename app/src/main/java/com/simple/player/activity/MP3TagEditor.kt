package com.simple.player.activity

import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.simple.player.playlist.PlaylistManager
import android.provider.MediaStore
import android.provider.DocumentsContract
import android.content.ContentUris
import android.app.AlertDialog
import android.net.Uri
import android.view.*
import android.widget.*
import com.simple.player.R
import com.simple.player.model.Song
import com.simple.player.Util
import java.io.*
import java.util.ArrayList

class MP3TagEditor : BaseActivity(),
    View.OnClickListener /*,CompoundButton.OnCheckedChangeListener*/ {
    private var idList: ArrayList<Int>? = null
    private var imageFile: File? = null
    private var currentSong: Song? = null
    private val SELECT_PICTURE = 100
    private val SELECT_ARTWORK_FROM_KG = 200
    private var index = 0
    private var isImageSelected = false
    private var isSelectKgArtwork = false
    private var isSingle = false
    private var artwork: ImageView? = null
    private var openNext: Button? = null
    private var openPrevious: Button? = null
    private var mGallery: Button? = null
    private var mKugou: Button? = null
    private var progress: TextView? = null
    private var info: TextView? = null
    private var bit: Bitmap? = null
    override fun onCreate(p1: Bundle?) {
        super.onCreate(p1)
        setContentView(R.layout.mp3_tag_editor)
        actionTitle = "修改封面"
        optionIcon = 0
        openNext = findViewById<View>(R.id.mp3_tag_editor_open_next) as Button
        openPrevious = findViewById<View>(R.id.mp3_tag_editor_open_previous) as Button
        artwork = findViewById<View>(R.id.mp3_tag_editor_artwork) as ImageView
        info = findViewById<View>(R.id.mp3_tag_editor_info) as TextView
        progress = findViewById<View>(R.id.mp3_tag_editor_progress) as TextView
        mGallery = findViewById<View>(R.id.mp3_tag_editor_gallery) as Button
        mKugou = findViewById<View>(R.id.mp3_tag_editor_kugou) as Button
        mGallery!!.setOnClickListener(this)
        mKugou!!.setOnClickListener(this)
        openNext!!.setOnClickListener(this)
        openPrevious!!.setOnClickListener(this)

        //registerForContextMenu(artwork);
        receiveDataFromIntent()
        if (isSingle) {
            openNext!!.visibility = View.INVISIBLE
            openPrevious!!.visibility = View.INVISIBLE
            progress!!.visibility = View.INVISIBLE
        } else {
            openPrevious!!.isEnabled = false
        }
        if (currentSong != null) {
            if (currentSong!!.type != "mp3") {
                alert("提示", "当前文件为" + currentSong!!.type + "文件，暂不支持")
                viewsEnabled(false)
            } else {
                updateData(currentSong)
            }
        }
    }

    /*
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.mp3_tag_editor_artwork_options_menu,menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.mp3_tag_editor_artwork_options_menu_gallery:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/ *");
                startActivityForResult(intent,SELECT_PICTURE);
                break;
            case R.id.mp3_tag_editor_artwork_options_menu_kugou:
                Intent pickArtwork = new Intent(this,KGArtworkBrowser.class);
                startActivityForResult(pickArtwork,SELECT_ARTWORK_FROM_KG);
                break;
        }
        return true;
    }
    */
    override fun onClick(p1: View) {
        val id = p1.id
        if (id == R.id.mp3_tag_editor_gallery) {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_PICTURE)
            return
        }
        if (id == R.id.mp3_tag_editor_open_next) {
            index++
            if (index == idList!!.size - 1) {
                openNext!!.isEnabled = false
            }
            if (!openPrevious!!.isEnabled) {
                openPrevious!!.isEnabled = true
            }
        }
        if (id == R.id.mp3_tag_editor_open_previous) {
            index--
            if (index == 0) {
                openPrevious!!.isEnabled = false
            }
            if (!openNext!!.isEnabled) {
                openNext!!.isEnabled = true
            }
        }
        currentSong = PlaylistManager.localPlaylist[idList!![index].toLong()]
        if (currentSong!!.type != "mp3") {
            alert("提示", "当前文件为" + currentSong!!.type + "文件，暂不支持")
            viewsEnabled(false)
        }
        progress!!.text = "第 " + (index + 1) + " 首  共 " + idList!!.size + " 首"
        recycleBitmap(bit)
        updateData(currentSong)
        isImageSelected = false
    }

    /*
    @Override
    public void onCheckedChanged(CompoundButton p1, boolean p2) {
        int id = p1.getId();
        if (id == R.id.mp3_tag_editor_auto_match) {
            if (p2) {
                String fileName = new File(currentSong.path).getName();
                if (fileName.contains(" - ")) {
                    artist.setText(fileName.substring(0,fileName.indexOf(" - ")));
                    title.setText(fileName.substring(fileName.indexOf(" - ")+3,fileName.lastIndexOf(".")));
                } else {
                    alert("提示","此文件不按套路命名,暂时无法匹配");
                    p1.setChecked(false);
                }
            } else {
                artist.setText(originalData[1]);
                title.setText(originalData[0]);
            }
        }
    }
    
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_PICTURE && data != null) {
            val uri = data.data
            var imgPath: String? = null
            if (DocumentsContract.isDocumentUri(this, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                if ("com.android.providers.media.documents" == uri!!.authority) {
                    val id = docId.split(":").toTypedArray()[1] //解析出数字格式的id
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    imgPath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
                } else if ("com.android.providers.downloads.documents" == uri.authority) {
                    val cntUri =
                        ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(docId))
                    imgPath = getImagePath(cntUri, null)
                }
            } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
                imgPath = getImagePath(uri, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                imgPath = uri.path
            }
            if (imgPath != null) {
                imageFile = File(imgPath)
                val bit = BitmapFactory.decodeFile(imgPath)
                artwork!!.setImageBitmap(bit)
            }
            isImageSelected = true
        }
        if (resultCode == SELECT_ARTWORK_FROM_KG && requestCode == SELECT_ARTWORK_FROM_KG) {
            val src = data!!.getStringExtra("artwork_src")
            imageFile = File(src)
            recycleBitmap(bit)
            bit = BitmapFactory.decodeFile(src)
            artwork!!.setImageBitmap(bit)
            isSelectKgArtwork = true
            isImageSelected = true
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getImagePath(uri: Uri?, selection: String?): String? {
        var path: String? = null
        val cur = contentResolver.query(uri!!, null, selection, null, null)
        if (cur != null) {
            if (cur.moveToFirst()) {
                path = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            }
            cur.close()
        }
        return path
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mp3_tag_editor_menu, menu)
        return true
    }

    override fun onOptionPressed() {
        if (currentSong!!.type != "mp3") {
            Util.toast("拒绝编辑此文件")
        } else {
            saveFile()
            Util.toast("功能已关闭")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.mp3_tag_editor_menu_save -> if (currentSong!!.type != "mp3") {
                Util.toast("拒绝编辑此文件")
            } else {
                saveFile()
                Util.toast("修改完成")
            }
        }
        return true
    }

    private fun saveFile() {
        /*
        MP3FileReader reader = new MP3FileReader();
        try {
            //String title = this.title.getText().toString();
            //String artist = this.artist.getText().toString();
            MP3File file = (MP3File)reader.read(new File(currentSong.path));
            ID3v24Tag tag = null;
            if (file.hasID3v2Tag()) {
                tag = file.getID3v2TagAsv24();
            } else {
                tag = new ID3v24Tag();
            }
            / *tag.setField(FieldKey.TITLE,title);
            tag.setField(FieldKey.ARTIST,artist);
            */
        /* if (isImageSelected) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame)tag.getFrame("APIC");
                FrameBodyAPIC apic = null;
                if (frame == null) {
                    frame = new ID3v24Frame("APIC");
                    apic = new FrameBodyAPIC();
                } else {
                    apic = (FrameBodyAPIC)frame.getBody();
                }
                apic.setDescription("mp3 tagger");
                apic.setImageData(imageBytes(imageFile));
                apic.setMimeType("image/ *");
                frame.setBody(apic);
                tag.setFrame(frame);
                
            }
            file.setID3v2Tag(tag);
            file.commit();
            / *
            int id = currentSong.id;
            currentSong.title = title;
            currentSong.artist = artist;
            SongInfo.update(SongInfo.TITLE,title,id);
            SongInfo.update(SongInfo.ARTIST,artist,id);
            */
        //Util.refreshArtworkCache(currentSong.id);
        /*
        }
        catch (InvalidAudioFrameException e){}
        catch (IOException e){}
        catch (TagException e){
            alert("TagException",e.toString());
        }
        catch (ReadOnlyFileException e){
            alert("错误","当前文件为只读文件,无法修改");
        }
        catch (CannotWriteException e){
            alert("错误","修改失败");
        }
*/
    }

    private fun imageBytes(imgFile: File): ByteArray {
        var a = ByteArray(1024)
        try {
            val input: InputStream = FileInputStream(imgFile)
            var length = 0
            val offset = 0
            val output = ByteArrayOutputStream()
            while (input.read(a).also { length = it } != -1) {
                output.write(a, offset, length)
            }
            a = output.toByteArray()
            output.close()
            input.close()
        } catch (e: IOException) {
        }
        return a
    }

    private fun alert(title: CharSequence, msg: CharSequence) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("确定", null)
            .show()
    }

    private fun receiveDataFromIntent() {
        val intent = intent
        idList = intent.getIntegerArrayListExtra(EXTRA_ID_LIST)
        isSingle = idList!!.size == 1
        if (!isSingle) {
            currentSong = PlaylistManager.localPlaylist[idList!![index].toLong()]
        }
    }

    private fun updateData(song: Song?) {
        val cnt = "文件路径：" //+song.getPath();
        info!!.text = cnt
        val src: ByteArray? = null //Util.getArtworkBytes(song);
        if (src != null) {
            bit = BitmapFactory.decodeByteArray(src, 0, src.size)
            artwork!!.setImageBitmap(bit)
        } else {
            artwork!!.setImageResource(R.drawable.main_surface_now_playing_album)
            Util.toast("无专辑封面")
        }
        if (!isSingle) {
            progress!!.text = "第 " + (index + 1) + " 首  共 " + idList!!.size + " 首"
        }
    }

    private fun viewsEnabled(d: Boolean) {
        mGallery!!.isEnabled = d
        mKugou!!.isEnabled = d
    }

    private fun recycleBitmap(bitmap: Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }

    override fun onDestroy() {
        //unregisterForContextMenu(artwork);
        if (idList != null) {
            idList!!.clear()
            idList = null
        }
        artwork = null
        if (bit != null && !bit!!.isRecycled) bit!!.recycle()
        info = null
        imageFile = null
        openNext = null
        openPrevious = null
        progress = null
        currentSong = null
        mGallery = null
        mKugou = null
        setContentView(R.layout.layout_empty)
        System.gc()
        super.onDestroy()
    }

    companion object {
        //public static final String EXTRA_MUSIC_ID = "id";
        //public static final String EXTRA_IS_SINGLE = "is_single";
        const val EXTRA_ID_LIST = "id_list"
    }
}