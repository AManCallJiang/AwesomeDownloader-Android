package com.jiang.awesomedownloader.core.downloader

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.jiang.awesomedownloader.core.AwesomeDownloader
import com.jiang.awesomedownloader.core.controller.DownloadController
import com.jiang.awesomedownloader.core.listener.IDownloadListener
import com.jiang.awesomedownloader.database.DownloadTaskManager
import com.jiang.awesomedownloader.database.TaskInfo
import com.jiang.awesomedownloader.http.OkHttpManager
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

const val NOTIFICATION_FOREGROUND_SERVICE_ID = 2888
class ForegroundServiceDownloader() : LifecycleService(), IDownloader {
    val tag = "ForegroundService"
    override lateinit var appContext: Context
    override val scope: CoroutineScope = lifecycleScope
    override val downloadController: DownloadController by lazy { DownloadController() }
    override val downloadQueue: Queue<TaskInfo> by lazy { ConcurrentLinkedQueue<TaskInfo>() }
    override val taskManager: DownloadTaskManager by lazy { DownloadTaskManager(appContext) }
    override var downloadingTask: TaskInfo? = null
    override val okHttpClient: OkHttpClient by lazy {
        OkHttpManager.getClient(
            AwesomeDownloader.option,
            downloadListener,
            downloadController
        )
    }
    override val downloadListener: IDownloadListener by lazy { createListener() }

    override fun close() {
        stopAll()
        downloadingTask = null
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        startForeground(
            NOTIFICATION_FOREGROUND_SERVICE_ID,
            AwesomeDownloader.notificationSender.buildForegroundServiceNotification()
        )
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return DownloadServiceBinder()
    }

    inner class DownloadServiceBinder : Binder() {
        fun getService() = this@ForegroundServiceDownloader
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(tag, "onStartCommand: ")
        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(tag, "onUnbind: ")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy: ")
    }
}
