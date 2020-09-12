package com.jiang.awesomedownloader

import android.content.Context
import android.os.Environment


/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      PathSelector
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/29 14:48
 */
class PathSelector(private val context: Context) {
    //   /data/user/0/{packageName}/cache
    fun getCacheDirPath(): String = context.cacheDir.absolutePath

    //    /storage/emulated/0/Android/data/{packageName}/cache
    fun getExternalCacheDirPath(): String? = context.externalCacheDir?.absolutePath

    //    /storage/emulated/0/Android/data/{packageName}/files/Pictures
    fun getPicturesDirPath(): String? =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath

    //    /storage/emulated/0/Android/data/{packageName}/files/Movies
    fun getVideosDirPath(): String? =
        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath

    //    /storage/emulated/0/Android/data/{packageName}/files/Music
    fun getMusicDirPath(): String? =
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath

    //    /storage/emulated/0/Android/data/{packageName}/files/Download
    fun getDownloadsDirPath(): String? =
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath

    //    /storage/emulated/0
    fun getExternalRootDir(): String? = Environment.getExternalStorageDirectory().absolutePath

}