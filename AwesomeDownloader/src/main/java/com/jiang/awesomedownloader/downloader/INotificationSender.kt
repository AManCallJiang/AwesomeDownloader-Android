package com.jiang.awesomedownloader.downloader

interface INotificationSender {
    fun createNotificationChannel()

    open fun showDownloadProgressNotification(
        progress: Int,
        fileName: String,
        isDownloading: Boolean
    )

    open fun cancelDownloadProgressNotification()

    open fun showDownloadStopNotification(fileName: String)

    open fun showDownloadDoneNotification(fileName: String, filePath: String)
}