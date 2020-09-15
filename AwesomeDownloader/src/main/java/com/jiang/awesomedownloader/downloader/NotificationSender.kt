package com.jiang.awesomedownloader.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jiang.awesomedownloader.R
import com.jiang.awesomedownloader.receiver.*


/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      NotificationUtil
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/24 22:07
 */

private const val CHANNEL_NAME = "AwesomeDownloaderNotification"
private const val descriptionText = "Downloader channel to show progress"
private const val PROGRESS_MAX = 100
private const val DOWNLOAD_ID = 1001
private const val DONE_ID = 2001

class NotificationSender(private val context: Context) :
    INotificationSender {
    private val CHANNEL_ID: String = this.javaClass.name
    override fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, importance).apply {
                description =
                    descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    val stopIntent = createIntent(StopReceiver::class.java, "ACTION_STOP")

    //    Intent(context, StopReceiver::class.java).apply {
//        action = "ACTION_STOP"
//        //putExtra("ACTION_STOP", 0)
//    }
    val stopPendingIntent =
        createPendingIntent(context, stopIntent)

    val cancelIntent = createIntent(CancelReceiver::class.java, "ACTION_CANCEL")
    val cancelPendingIntent =
        createPendingIntent(context, cancelIntent)

    val cancelAllIntent = createIntent(CancelAllReceiver::class.java, "ACTION_CANCEL_ALL")
    val cancelAllPendingIntent = createPendingIntent(context, cancelAllIntent)

    val resumeIntent = createIntent(ResumeReceiver::class.java, "ACTION_RESUME")
    val resumePendingIntent = createPendingIntent(context, resumeIntent)

    private fun createIntent(receiverClass: Class<out BroadcastReceiver>, tag: String): Intent {
        return Intent(context, receiverClass).apply {
            action = tag
        }
    }

   override fun showDownloadProgressNotification(
        progress: Int,
        fileName: String,
        isDownloading: Boolean
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .addAction(
                R.drawable.ic_baseline_pause,
                context.getString(R.string.STOP),
                stopPendingIntent
            )
            .addAction(R.drawable.ic_baseline_cancel_24, "cancel", cancelPendingIntent)
            .addAction(R.drawable.ic_baseline_delete_forever, "cancel all", cancelAllPendingIntent)
            .setContentTitle("$fileName (downloading)")
            .setContentText("$progress%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .setProgress(PROGRESS_MAX, progress, false)

        NotificationManagerCompat.from(context).notify(DOWNLOAD_ID, builder.build())
    }

    override fun cancelDownloadProgressNotification() {
        NotificationManagerCompat.from(context).cancel(DOWNLOAD_ID)
    }

    override fun showDownloadStopNotification(fileName: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .addAction(
                R.drawable.ic_baseline_play_arrow,
                "resume",
                resumePendingIntent
            )
            .addAction(R.drawable.ic_baseline_cancel_24, "cancel", cancelPendingIntent)
            .addAction(R.drawable.ic_baseline_delete_forever, "cancel all", cancelAllPendingIntent)
            .setContentTitle("$fileName (stop)")
            .setContentText("0%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .setProgress(PROGRESS_MAX, 0, false)

        NotificationManagerCompat.from(context).notify(DOWNLOAD_ID, builder.build())
    }

    override fun showDownloadDoneNotification(fileName: String, filePath: String) {
        val openFileIntent = Intent(context, OpenFileReceiver::class.java).apply {
            action = "ACTION_OPEN"
            putExtra("ACTION_OPEN", 0)
            putExtra("PATH", "$filePath/$fileName")
            Log.d(TAG, "showDownloadDoneNotification: $filePath/$fileName")
        }
        val openPendingIntent =
            createPendingIntent(context, openFileIntent)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("$fileName (done)")
            .setContentText("$fileName")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(DONE_ID, builder.build())
    }

    private fun createPendingIntent(
        context: Context,
        intent: Intent
    ): PendingIntent? {
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}