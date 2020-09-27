package com.jiang.awesomedownloader.tool

import android.content.Context
import android.os.Environment


/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      PathSelector
 * @Description:    路径选择工具
 * @Author:         江
 * @CreateDate:     2020/8/29 14:48
 */
class PathSelector(private val context: Context) {

    /**
     * 返回手机内部储存的应用缓存路径 (/data/user/0/{packageName}/cache)
     * @return String
     */
    fun getCacheDirPath(): String = context.cacheDir.absolutePath

    /**
     * 返回手机外部储存的应用缓存路径 (/storage/emulated/0/Android/data/{packageName}/cache)
     * @return String
     */
    fun getExternalCacheDirPath(): String = context.externalCacheDir?.absolutePath!!


    /**
     * 返回手机外部储存的应用图片路径 (/storage/emulated/0/Android/data/{packageName}/files/Pictures)
     * @return String
     */
    fun getPicturesDirPath(): String =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath!!

    /**
     * 返回手机外部储存的应用影片路径 (/storage/emulated/0/Android/data/{packageName}/files/Movies)
     * @return String
     */
    fun getVideosDirPath(): String =
        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath!!

    /**
     * 返回手机外部储存的应用音乐路径 (/storage/emulated/0/Android/data/{packageName}/files/Music)
     * @return String
     */
    fun getMusicDirPath(): String =
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath!!

    /**
     * 返回手机外部储存的应用下载文件路径 (/storage/emulated/0/Android/data/{packageName}/files/Download)
     * @return String
     */
    fun getDownloadsDirPath(): String =
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath!!

    //    /storage/emulated/0
    /**
     * 返回手机外部储存的根目录 (/storage/emulated/0)
     * @return String
     */
    fun getExternalRootDir(): String = Environment.getExternalStorageDirectory().absolutePath

}