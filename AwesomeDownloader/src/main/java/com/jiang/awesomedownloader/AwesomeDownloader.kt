package com.jiang.awesomedownloader

import android.content.Context

import android.util.Log
import com.jiang.awesomedownloader.database.*
import com.jiang.awesomedownloader.http.RetrofitManager
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.Exception

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      AwesomeDownloader
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/18 20:04
 */
const val WRITE_BUFFER_SIZE = 1024 * 4
const val TAG = "AwesomeDownloader"

object AwesomeDownloader {
    val option by lazy { AwesomeDownloaderOption() }
    private val downloadListener by lazy { DownloadListener() }
    private val downloadController by lazy { DownloadController() }
    private val api by lazy {
        RetrofitManager.createRetrofit(downloadListener, downloadController, option)
    }
    private val downloadQueue by lazy { ConcurrentLinkedQueue<TaskInfo>() }

    private lateinit var taskManager: DownloadTaskManager
    private var downloadingTask: TaskInfo? = null
    private var appContext: Context? = null
    private lateinit var notificationUtil: NotificationUtil

    private var onDownloadError: (Exception) -> Unit = {}
    private var onDownloadProgressChange: (Long) -> Unit = {}
    private var onDownloadStop: (Long, Long) -> Unit = { _: Long, _: Long -> }
    private var onDownloadFinished: (String, String) -> Unit = { _: String, _: String -> }

    private suspend fun download() {
        withContext(Dispatchers.IO) {
            if (downloadingTask == null) return@withContext
            val task = downloadingTask!!
            writeFileInDisk(
                api.downloadFile(task.url, "bytes=${task.downloadedBytes}-"),
                File(task.filePath, task.fileName),
                task.status == TASK_STATUS_UNFINISHED
            )
            Log.d(TAG, "download: switching2NextTask()")
            switching2NextTask()
        }
    }

    fun init(appContext: Context): AwesomeDownloader {
        this.appContext = appContext
        taskManager = DownloadTaskManager(appContext)
        notificationUtil = NotificationUtil(appContext)
        notificationUtil.createNotificationChannel()
        return this
    }

    fun enqueue(url: String, filePath: String, fileName: String): AwesomeDownloader {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val taskInfo = TaskInfo(
                    System.currentTimeMillis(),
                    fileName,
                    filePath,
                    url,
                    0,
                    0,
                    TASK_STATUS_UNINITIALIZED
                )
                taskManager.dao.insert(taskInfo)
                downloadQueue.offer(taskInfo)
                if (downloadingTask == null) switching2NextTask()

            } catch (e: Exception) {
                Log.e("download", e.localizedMessage, e)
                withContext(Dispatchers.Main) {
                    onDownloadError(e)
                }
            }
        }
        return this
    }

    private suspend fun switching2NextTask() {
        downloadingTask = downloadQueue.poll()
        download()
    }

    fun getDownloadQueueArray() = downloadQueue.toTypedArray()

     suspend fun queryAllTaskInfo(): MutableList<TaskInfo> {
        return withContext(Dispatchers.IO) {
            return@withContext taskManager.dao.queryAll()
        }
    }


     suspend fun queryUnfinished(): MutableList<TaskInfo> {
        return withContext(Dispatchers.IO) {
            return@withContext taskManager.dao.queryUnfinish()
        }
    }

    fun stopAll() {
        downloadController.pause()
        downloadQueue.clear()
        //downloadingTask = null
    }

    fun resumeAndStart() {
        GlobalScope.launch(Dispatchers.IO) {
            queryUnfinished().let {
                if (it.isNotEmpty()) {
                    downloadQueue.clear()
                    downloadQueue.addAll(it)
                    downloadController.start()
                    switching2NextTask()
                }
            }
        }
    }

    fun cancelAll() {
        stopAll()
        GlobalScope.launch(Dispatchers.IO) {
            taskManager.dao.deleteArray(downloadQueue.toTypedArray())
            downloadQueue.clear()
            downloadingTask = null
        }
        notificationUtil.cancelDownloadProgressNotification()
    }

    fun cancel() {
        stopAll()
        if (downloadingTask != null) {
            GlobalScope.launch(Dispatchers.IO) {
                downloadQueue.poll()
                taskManager.dao.delete(downloadingTask!!)
                downloadingTask = null
                notificationUtil.cancelDownloadProgressNotification()
                delay(2000)
                resumeAndStart()
            }

        }
    }

    fun close() {
        stopAll()
        downloadQueue.clear()
        downloadingTask = null
        appContext = null
    }

    private fun notifyMediaStore(taskInfo: TaskInfo) {
        try {
            MediaStoreHelper.notifyMediaStore(taskInfo, appContext!!)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "notifyMediaStore: ${e.message}", e)
            onDownloadError(e)
        }
    }

    fun setOnError(onError: (Exception) -> Unit): AwesomeDownloader {
        onDownloadError = onError
        return this
    }

    fun setOnProgressChange(onProgressChange: (Long) -> Unit): AwesomeDownloader {
        onDownloadProgressChange = onProgressChange
        return this
    }

    fun setOnStop(onStop: (Long, Long) -> Unit): AwesomeDownloader {
        onDownloadStop = onStop
        return this
    }

    fun setOnFinished(onFinished: (String, String) -> Unit): AwesomeDownloader {
        onDownloadFinished = onFinished
        return this
    }

    class DownloadListener : BaseDownloadListener {
        override fun onProgressChange(progress: Long) {
            Log.d(TAG, "$progress %")
            if (option.showNotification) {
                notificationUtil.showDownloadProgressNotification(
                    progress.toInt(),
                    downloadingTask?.fileName ?: "null",
                    true
                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                onDownloadProgressChange(progress)
            }
        }

        override fun onStop(downloadBytes: Long, totalBytes: Long) {
            Log.d(TAG, "$downloadBytes b")
            val task = downloadingTask
            task?.let {
                it.downloadedBytes += downloadBytes
                if (it.status == TASK_STATUS_UNINITIALIZED) {
                    it.totalBytes = totalBytes
                    it.status = TASK_STATUS_UNFINISHED
                }
                GlobalScope.launch(Dispatchers.IO) { taskManager.dao.update(it) }
                notificationUtil.showDownloadStopNotification(task.fileName)
            }
            GlobalScope.launch(Dispatchers.Main) { onDownloadStop(downloadBytes, totalBytes) }
        }

        override fun onFinish(downloadBytes: Long, totalBytes: Long) {
            val task = downloadingTask
            task?.let {
                if (it.status == TASK_STATUS_UNINITIALIZED) it.totalBytes = totalBytes
                it.downloadedBytes += downloadBytes
                it.status = TASK_STATUS_FINISH
                GlobalScope.launch(Dispatchers.IO) { taskManager.dao.insert(it) }
                notifyMediaStore(it)
            }
            if (option.showNotification) {
                notificationUtil.cancelDownloadProgressNotification()
                notificationUtil.showDownloadDoneNotification(
                    downloadingTask?.fileName ?: "null",
                    downloadingTask?.filePath ?: "null"
                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                onDownloadFinished(
                    task?.filePath ?: "null",
                    task?.fileName ?: "null"
                )
            }
        }


    }
}






