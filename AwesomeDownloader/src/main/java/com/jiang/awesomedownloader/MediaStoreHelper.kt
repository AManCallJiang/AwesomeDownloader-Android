package com.jiang.awesomedownloader

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.jiang.awesomedownloader.database.TaskInfo
import com.jiang.awesomedownloader.receiver.isAudioFile
import com.jiang.awesomedownloader.receiver.isImageFile
import com.jiang.awesomedownloader.receiver.isVideoFile


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

            appContext?.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://${taskInfo.filePath}/${taskInfo.fileName}")
                )
            )
            Log.d(TAG, "notifyMediaStore: done")
        }
    }
}