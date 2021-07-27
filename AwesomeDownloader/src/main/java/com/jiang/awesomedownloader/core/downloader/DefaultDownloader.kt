package com.jiang.awesomedownloader.core.downloader

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import com.jiang.awesomedownloader.core.AwesomeDownloader
import com.jiang.awesomedownloader.core.controller.DownloadController
import com.jiang.awesomedownloader.core.listener.IDownloadListener
import com.jiang.awesomedownloader.database.DownloadTaskManager
import com.jiang.awesomedownloader.database.TaskInfo
import com.jiang.awesomedownloader.http.OkHttpManager
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *
 * @ProjectName:    AwesomeDownloaderDemo
 * @ClassName:      DefaultDownloader
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/11/10 21:31
 */
class DefaultDownloader(application: Application) : IDownloader, AndroidViewModel(application) {
    override var appContext: Context = application.applicationContext
    override val scope: CoroutineScope = viewModelScope
    override val downloadController: DownloadController by lazy { DownloadController() }
    override val downloadQueue: ConcurrentLinkedQueue<TaskInfo> by lazy { ConcurrentLinkedQueue<TaskInfo>() }
    override val taskManager: DownloadTaskManager = DownloadTaskManager(appContext)
    override var downloadingTask: TaskInfo? = null
    override val okHttpClient: OkHttpClient by lazy {
        OkHttpManager.getClient(AwesomeDownloader.option, downloadListener, downloadController)
    }
    override val downloadListener: IDownloadListener by lazy { createListener() }

    override fun close() {
        stopAll()
        downloadingTask = null
        AwesomeDownloader.notificationSender.cancelDownloadProgressNotification()
    }

    override fun onCleared() {
        super.onCleared()
        close()
    }


}