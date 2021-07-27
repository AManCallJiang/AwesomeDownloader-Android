package com.jiang.awesomedownloader.core.downloader

import android.content.Context
import android.util.Log
import com.jiang.awesomedownloader.core.AwesomeDownloader
import com.jiang.awesomedownloader.core.controller.DownloadController
import com.jiang.awesomedownloader.database.*
import com.jiang.awesomedownloader.core.listener.IDownloadListener
import com.jiang.awesomedownloader.http.OkHttpManager
import com.jiang.awesomedownloader.tool.MediaStoreHelper
import com.jiang.awesomedownloader.tool.TAG
import com.jiang.awesomedownloader.tool.writeFileInDisk
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.File
import java.util.Queue

/**
 *
 * @ProjectName:    AwesomeDownloaderDemo
 * @ClassName:      IDownloader
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/11/10 20:45
 */
interface IDownloader {
    var appContext: Context

    val scope: CoroutineScope
    val downloadController: DownloadController
    val downloadQueue: Queue<TaskInfo>
    val taskManager: DownloadTaskManager
    var downloadingTask: TaskInfo?
    val okHttpClient: OkHttpClient
    val downloadListener: IDownloadListener
    fun enqueue(url: String, filePath: String, fileName: String) {
        scope.launch(Dispatchers.IO) {
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
                taskManager.insertTaskInfo(taskInfo)
                downloadQueue.offer(taskInfo)
                if (downloadingTask == null) switching2NextTask()

            } catch (e: Exception) {
                Log.e("download", e.localizedMessage, e)
                withContext(Dispatchers.Main) {
                    AwesomeDownloader.onDownloadError.forEach { it.invoke(e) }
                }
            }
        }
    }

    private suspend fun download() {
        withContext(Dispatchers.IO) {
            if (downloadingTask == null) {
                return@withContext
            }
            val task = downloadingTask!!
            val request = OkHttpManager.createRequest(task)
            val response = okHttpClient.newCall(request).execute()
            writeFileInDisk(
                response.body()!!,
                File(task.filePath, task.fileName),
                task.status == TASK_STATUS_UNFINISHED
            )
            Log.d(TAG, "download: switching2NextTask()")
            switching2NextTask()
        }
    }

    private suspend fun switching2NextTask() {
        downloadingTask = downloadQueue.poll()
        downloadController.start()
        download()
    }

    fun stopAll() {
        downloadController.pause()
        downloadQueue.clear()
    }

    fun resumeAndStart() {
        scope.launch(Dispatchers.IO) {
            queryUnfinishedTaskInfo().let {
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
        downloadController.pause()
        scope.launch(Dispatchers.IO) {
            Log.d(TAG, "cancelAll deleteTaskInfoArray: ${getDownloadQueueArray()}")
            taskManager.deleteAllUnfinishedTaskInfo()
            downloadQueue.forEach {
                clearCache(it)
            }
            downloadQueue.clear()
            downloadingTask = null
            //downloadController.start()
        }
        AwesomeDownloader.notificationSender.cancelDownloadProgressNotification()

    }

    fun cancel() {
        downloadController.pause()
        if (downloadingTask != null) {
            scope.launch(Dispatchers.IO) {
                Log.d(TAG, "delete: $downloadingTask")
                clearCache(downloadingTask!!)
                taskManager.deleteTaskInfoByID(downloadingTask!!.id)
                downloadQueue.poll()
                downloadingTask = null
                AwesomeDownloader.notificationSender.cancelDownloadProgressNotification()
                delay(2000)
                //downloadController.start()
                switching2NextTask()
            }
        }
    }

    private fun notifyMediaStore(taskInfo: TaskInfo) {
        try {
            MediaStoreHelper.notifyMediaStore(taskInfo, appContext)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "notifyMediaStore: ${e.message}", e)
            AwesomeDownloader.onDownloadError.forEach { it.invoke(e) }
        }
    }

    fun clearCache(taskInfo: TaskInfo) {
        scope.launch(Dispatchers.IO) {
            val file = File(taskInfo.getAbsolutePath())
            if (file.exists()) file.delete()
        }
    }

    fun getDownloadQueueArray() = downloadQueue.toTypedArray()


    suspend fun queryAllTaskInfo(): MutableList<TaskInfo> = taskManager.getAllTaskInfo()

    suspend fun queryUnfinishedTaskInfo(): MutableList<TaskInfo> =
        taskManager.getUnfinishedTaskInfo()


    fun getAllTaskInfoLiveData() = taskManager.getAllTaskInfoLiveData()


    fun getUnfinishedTaskInfoLiveData() = taskManager.getUnfinishedTaskInfoLiveData()


    suspend fun queryFinishedTaskInfo() = taskManager.getFinishedTaskInfo()


    fun getFinishedTaskInfoLiveData() = taskManager.getFinishedTaskInfoLiveData()

    suspend fun deleteTaskInfo(taskInfo: TaskInfo) = taskManager.deleteTaskInfo(taskInfo)
    suspend fun deleteTaskInfoArray(array: Array<TaskInfo>) = taskManager.deleteTaskInfoArray(array)
    suspend fun deleteTaskInfoByID(id: Long) = taskManager.deleteTaskInfoByID(id)

//    fun setOnError(onError: (Exception) -> Unit) {
//        AwesomeDownloader.onDownloadError = onError
//    }
//
//
//    fun setOnProgressChange(onProgressChange: (Long) -> Unit) {
//        AwesomeDownloader.onDownloadProgressChange = onProgressChange
//    }
//
//    fun setOnStop(onStop: (Long, Long) -> Unit) {
//        AwesomeDownloader.onDownloadStop = onStop
//    }
//
//    fun setOnFinished(onFinished: (String, String) -> Unit) {
//        AwesomeDownloader.onDownloadFinished = onFinished
//    }
//
//    fun setNotificationSender(sender: NotificationSender) {
//        AwesomeDownloader.notificationSender = sender
//    }

    fun createListener(): IDownloadListener = object : IDownloadListener {
        var progress = 0L
        override fun onProgressChange(downloadBytes: Long, totalBytes: Long) {
            val newProgress =
                (downloadingTask!!.downloadedBytes + downloadBytes) * 100 / if (downloadingTask!!.status == TASK_STATUS_UNINITIALIZED) totalBytes else downloadingTask!!.totalBytes
            if (progress != newProgress && newProgress < 100L) {
                progress = newProgress
                Log.d(TAG, "$progress %")
                if (AwesomeDownloader.option.showNotification) {
                    AwesomeDownloader.notificationSender.showDownloadProgressNotification(
                        progress.toInt(), downloadingTask?.fileName ?: "null"
                    )
                }
                scope.launch(Dispatchers.Main) {
                    AwesomeDownloader.onDownloadProgressChange.forEach { it.invoke(progress) }
                }
            } else if (progress != newProgress && newProgress == 100L) {
                onFinish(downloadBytes, totalBytes)
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
                scope.launch(Dispatchers.IO) { taskManager.updateTaskInfo(it) }
                AwesomeDownloader.notificationSender.showDownloadStopNotification(task.fileName)
            }
            scope.launch(Dispatchers.Main) {
                AwesomeDownloader.onDownloadStop.forEach { it.invoke(downloadBytes, totalBytes) }
            }
        }

        override fun onFinish(downloadBytes: Long, totalBytes: Long) {
            Log.d(TAG, "onFinish: ")
            val task = downloadingTask
            task?.let {
                if (it.status == TASK_STATUS_UNINITIALIZED) it.totalBytes = totalBytes
                it.downloadedBytes += downloadBytes
                it.status = TASK_STATUS_FINISH
                scope.launch(Dispatchers.IO) {
                    taskManager.insertTaskInfo(it)
                    if (AwesomeDownloader.option.notifyMediaStoreWhenItDone) {
                        notifyMediaStore(it)
                    }
                }

            }
            if (AwesomeDownloader.option.showNotification) {
                AwesomeDownloader.notificationSender.showDownloadDoneNotification(
                    downloadingTask?.fileName ?: "null",
                    downloadingTask?.filePath ?: "null"
                )
                AwesomeDownloader.notificationSender.cancelDownloadProgressNotification()
            }
            scope.launch(Dispatchers.Main) {
                AwesomeDownloader.onDownloadFinished.forEach {
                    it.invoke(task?.filePath ?: "null", task?.fileName ?: "null")
                }
            }
        }
    }

    fun close()

}