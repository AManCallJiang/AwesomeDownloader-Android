package com.jiang.awesomedownloader.core.sender

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jiang.awesomedownloader.R
import com.jiang.awesomedownloader.receiver.*
import com.jiang.awesomedownloader.tool.TAG


/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      DefaultNotificationSender
 * @Description:    默认的通知发送者
 * @Author:         江
 * @CreateDate:     2020/9/17 15:28
 */


class DefaultNotificationSender(context: Context) : NotificationSender(context) {

    private val stopIntent = createIntent(StopReceiver::class.java, "ACTION_STOP")
    private val stopPendingIntent =
        createPendingIntent(context, stopIntent)

    private val cancelIntent = createIntent(CancelReceiver::class.java, "ACTION_CANCEL")
    private val cancelPendingIntent =
        createPendingIntent(context, cancelIntent)

    private val cancelAllIntent = createIntent(CancelAllReceiver::class.java, "ACTION_CANCEL_ALL")
    private val cancelAllPendingIntent = createPendingIntent(context, cancelAllIntent)

    private val resumeIntent = createIntent(ResumeReceiver::class.java, "ACTION_RESUME")
    private val resumePendingIntent = createPendingIntent(context, resumeIntent)

    private fun createIntent(receiverClass: Class<out BroadcastReceiver>, tag: String): Intent {
        return Intent(context, receiverClass).apply { action = tag }
    }

    private fun createPendingIntent(context: Context, intent: Intent): PendingIntent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }


    override fun buildDownloadProgressNotification(progress: Int, fileName: String): Notification {
        return NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_download)
            .addAction(
                R.drawable.ic_baseline_pause,
                context.getString(R.string.stop),
                stopPendingIntent
            )
            .addAction(
                R.drawable.ic_baseline_cancel_24,
                context.getString(R.string.cancel),
                cancelPendingIntent
            )
            .addAction(
                R.drawable.ic_baseline_delete_forever,
                context.getString(R.string.cancel_all),
                cancelAllPendingIntent
            )
            .setContentTitle("$fileName ${context.getString(R.string.downloading)}")
            .setContentText("$progress%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .setProgress(NOTIFICATION_PROGRESS_MAX, progress, false)
            .build()
    }

    override fun buildDownloadStopNotification(fileName: String): Notification {
        return NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_download)
            .addAction(
                R.drawable.ic_baseline_play_arrow,
                context.getString(R.string.resume),
                resumePendingIntent
            )
            .setContentTitle("$fileName ${context.getString(R.string.stoped)}")
            .setContentText(context.getString(R.string.notification_content_stop))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .build()
    }

    override fun buildDownloadDoneNotification(filePath: String, fileName: String): Notification {
        val openFileIntent = Intent(context, OpenFileReceiver::class.java).apply {
            action = "ACTION_OPEN"
            putExtra("ACTION_OPEN", 0)
            putExtra(INTENT_EXTRA_PATH, "$filePath/$fileName")
            Log.d(TAG, "showDownloadDoneNotification: $filePath/$fileName")
        }
        val openPendingIntent =
            createPendingIntent(context, openFileIntent)
        return NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("$fileName ${context.getString(R.string.done)}")
            .setContentText(fileName)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .build()
    }

}