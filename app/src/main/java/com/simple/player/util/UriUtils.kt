package com.simple.player.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

object UriUtils {

    fun getRealFilePath(context: Context, uri: Uri): String?  {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri);
                val split = docId.split(":");
                val type = split[0];
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().absolutePath + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri);
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong());
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri);
                val split = docId.split(":");
                val type = split[0];
                var contentUri: Uri? = null;
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                }
                val selection = MediaStore.Images.Media._ID + "=?";
                val selectionArgs = arrayOf(split[1]);
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.lastPathSegment;
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path;
        }
        return null;
    }

    /**
     * 从本地设备数据库查询数据.
     *
     * @param context       上下文
     * @param uri           内容提供者的标识
     * @param selection     设置条件，相当于SQL语句中的where
     * @param selectionArgs 条件值
     * @return 查询结果
     */
    fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        uri ?: return null
        var cursor: Cursor? = null;
        val column = MediaStore.Audio.Media.DATA;
        val projection = arrayOf(column);  //告诉Provider要返回的内容（列Column）
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            cursor?.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority;
    }


}