package com.jiang.awesomedownloader.tool

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.jiang.awesomedownloader.core.AwesomeDownloader
import com.jiang.awesomedownloader.database.TaskInfo


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
                val mimeType = getMimeType(fileName)
                Log.d(TAG, "notifyMediaStore: mimeType:$mimeType")
                when {
                    isVideoFile(fileName) -> {
                        val contentUri =
                            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        val values = ContentValues().apply {
                            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        }
                        Log.d(
                            TAG,
                            "notifyMediaStore: ${resolver?.insert(contentUri, values).toString()}"
                        )
                    }
                    isAudioFile(fileName) -> {
                        val contentUri =
                            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        val values = ContentValues().apply {
                            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        }
                        Log.d(
                            TAG,
                            "notifyMediaStore: ${resolver?.insert(contentUri, values).toString()}"
                        )
                    }
                    isImageFile(fileName) -> {
                        val contentUri =
                            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        }
                        Log.d(
                            TAG,
                            "notifyMediaStore: ${resolver?.insert(contentUri, values).toString()}"
                        )

                    }
                    else -> {
                        Log.d(TAG, "notifyMediaStore: 类型不匹配 $fileName")
                        return
                    }
                }
            } else {
                notifyScanFile(taskInfo, appContext)
            }

        }
    }

    private fun notifyScanFile(taskInfo: TaskInfo, appContext: Context) {
        val fileName = taskInfo.fileName
        val mimeType = getMimeType(fileName)

        if (mimeType.isNullOrEmpty()) {
            MediaScannerConnection.scanFile(
                appContext,
                arrayOf(taskInfo.getAbsolutePath()),
                arrayOf(mimeType)
            ) { path, uri ->
                Log.d(
                    TAG,
                    "notifyScanFile: path:$path fileName:$fileName uri:$uri mimeType:$mimeType"
                )
            }
        } else {
            Log.d(TAG, "notifyScanFile: 类型不匹配 $fileName $mimeType")
        }
    }
}