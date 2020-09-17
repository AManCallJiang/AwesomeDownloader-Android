package com.jiang.awesomedownloader.tool

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.jiang.awesomedownloader.database.TaskInfo
import com.jiang.awesomedownloader.downloader.AwesomeDownloader
import com.jiang.awesomedownloader.downloader.TAG


/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      MediaStoreHelper
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/29 22:08
 */
object MediaStoreHelper {
    fun notifyMediaStore(taskInfo: TaskInfo, appContext: Context) {
        if (AwesomeDownloader.option.notifyMediaStoreWhenItDone) {
            val fileName = taskInfo.fileName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = appContext.contentResolver
                when {
                    isVideoFile(fileName) -> {
                        val contentUri =
                            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        val values = ContentValues().apply {
                            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                        }
                        resolver?.insert(contentUri, values)
                    }
                    isAudioFile(fileName) -> {
                        val contentUri =
                            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        val values = ContentValues().apply {
                            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                        }
                        resolver?.insert(contentUri, values)
                    }
                    isImageFile(fileName) -> {
                        val contentUri =
                            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        }
                        resolver?.insert(contentUri, values)
                    }
                    else -> {
                        Log.d(TAG, "notifyIt: 类型不匹配 $fileName")
                        return
                    }
                }
            } else {
                if (isImageFile(fileName)) {
                    MediaStore.Images.Media.insertImage(
                        appContext.contentResolver,
                        taskInfo.getAbsolutePath(),
                        fileName,
                        taskInfo.url
                    )
                }
            }
            Log.d(TAG, "notifyMediaStore: done")
            appContext?.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://${taskInfo.filePath}/${taskInfo.fileName}")
                )
            )
            Log.d(TAG, "notifyMediaStore: sendBroadcast")
        }
    }



    fun notifyMediaScanner(taskInfo: TaskInfo, appContext: Context) {
        val fileName = taskInfo.fileName
        val mimeType = when {
            isImageFile(fileName) -> {
    //            "image/jpeg"
                "image/*"
            }
            isAudioFile(fileName) -> {
    //            "audio/x-mpeg"
                "audio/*"
            }
            isVideoFile(fileName) -> {
    //            "video/mp4"
                "video/*"
            }
            else -> {
                ""
            }
        }

        if (mimeType.isNotEmpty()) {
            MediaScannerConnection.scanFile(
                appContext,
                arrayOf(taskInfo.filePath),
                arrayOf(mimeType)
            ) { path, uri ->
                Log.d(TAG, "notifyIt: $fileName $path $uri")
            }
        } else {
            Log.d(TAG, "notifyIt: 类型不匹配 $fileName")
        }

    }
}