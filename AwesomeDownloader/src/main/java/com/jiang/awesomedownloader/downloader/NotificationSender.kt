package com.jiang.awesomedownloader.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

const val CHANNEL_NAME = "AwesomeDownloaderNotification"
const val descriptionText = "Downloader channel to show progress"
const val NOTIFICATION_PROGRESS_MAX = 100
const val NOTIFICATION_DOWNLOAD_ID = 2234
const val NOTIFICATION_DONE_ID = 2235

abstract class NotificationSender(protected val context: Context) {
    protected val CHANNEL_ID: String = this.javaClass.name

    abstract fun buildDownloadProgressNotification(progress: Int, fileName: String): Notification

    abstract fun buildDownloadStopNotification(fileName: String): Notification

    abstract fun buildDownloadDoneNotification(filePath: String, fileName: String): Notification

    /**
     * 创建NotificationChannel，但仅在API 26+上创建，因为NotificationChannel类是新的，并且不在支持库中.
     * 由于您必须先创建通知渠道，然后才能在 Android 8.0 及更高版本上发布任何通知，因此应在应用启动时立即执行这段代码。
     * 反复调用这段代码是安全的，因为创建现有通知渠道不会执行任何操作。
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
                .apply { description = descriptionText }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun cancelDownloadProgressNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_DOWNLOAD_ID)
    }

    fun showDownloadStopNotification(fileName: String) {
        val notification = buildDownloadStopNotification(fileName)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_DOWNLOAD_ID, notification)
    }

    fun showDownloadDoneNotification(fileName: String, filePath: String) {
        val notification = buildDownloadDoneNotification(filePath, fileName)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_DONE_ID, notification)
    }

    fun showDownloadProgressNotification(progress: Int, fileName: String) {
        val notification = buildDownloadProgressNotification(progress, fileName)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_DOWNLOAD_ID, notification)
    }
}