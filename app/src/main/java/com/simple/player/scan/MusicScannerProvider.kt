package com.simple.player.scan

import android.os.Build
import com.simple.player.util.AppConfigure

object MusicScannerProvider {

    fun getScanner(): MusicScanner {
        if (AppConfigure.Settings.musicSource == "MediaStore") {
            return MediaStoreMusicScanner()
        }
        return FileMusicScanner()
    }

}