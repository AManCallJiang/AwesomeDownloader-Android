package com.jiang.awesomedownloader.downloader

import android.content.Context

import android.util.Log
import com.jiang.awesomedownloader.database.*
import com.jiang.awesomedownloader.http.OkHttpManager
import com.jiang.awesomedownloader.http.RetrofitManager
import com.jiang.awesomedownloader.tool.MediaStoreHelper
import com.jiang.awesomedownloader.tool.writeFileInDisk
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.Exception

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      AwesomeDownloader
 * @Description:    核心类（单例）
 * @Author:         江
 * @CreateDate:     2020/8/18 20:04
 */
const val WRITE_BUFFER_SIZE = 1024 * 4
const val TAG = "AwesomeDownloader"

object AwesomeDownloader {
    //设置
    val option by lazy { AwesomeDownloaderOption() }
    private var isInitialized = false

    // private val downloadListener by lazy { DownloadListener() }
    private val downloadController by lazy { DownloadController() }

    //    private val api by lazy {
//        RetrofitManager.createRetrofit(
//            downloadListener,
//            downloadController,
//            option
//        )
//    }
    private val downloadQueue by lazy { ConcurrentLinkedQueue<TaskInfo>() }

    private lateinit var taskManager: DownloadTaskManager
    private var downloadingTask: TaskInfo? = null
    private var appContext: Context? = null
    private lateinit var notificationSender: NotificationSender

    private var onDownloadError: (Exception) -> Unit = {}
    private var onDownloadProgressChange: (Long) -> Unit = {}
    private var onDownloadStop: (Long, Long) -> Unit = { _: Long, _: Long -> }
    private var onDownloadFinished: (String, String) -> Unit = { _: String, _: String -> }

    private val okHttpClient by lazy {
        OkHttpManager.getClient(
            option,
            downloadListener,
            downloadController
        )
    }

    /**
     * 下载当前任务并写入硬盘，完成后切换到下一个任务
     */
    private suspend fun download() {
        withContext(Dispatchers.IO) {
            if (downloadingTask == null) return@withContext
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

    /**
     *
     * @param appContext Context ：Application 的 Context
     * @return AwesomeDownloader
     */
    fun init(appContext: Context): AwesomeDownloader {
        if (isInitialized) return this

        AwesomeDownloader.appContext = appContext
        taskManager = DownloadTaskManager(appContext)
        notificationSender =
            DefaultNotificationSender(appContext)
        notificationSender.createNotificationChannel()
        isInitialized = true

        return this
    }

    /**
     * 加入下载任务队列
     * @param url String 下载地址
     * @param filePath String  文件路径（不含文件名）
     * @param fileName String 文件名称（包括格式后缀）
     * @return AwesomeDownloader
     */
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
                taskManager.insertTaskInfo(taskInfo)
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

    /**
     *切换到下一个任务
     */
    private suspend fun switching2NextTask() {
        downloadingTask = downloadQueue.poll()
        download()
    }

    /**
     * 获取当前下载队列所有任务信息的数组
     * @return Array<(TaskInfo?)>
     */
    fun getDownloadQueueArray() = downloadQueue.toTypedArray()

    /**
     * 查询所有任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryAllTaskInfo(): MutableList<TaskInfo> = taskManager.getAllTaskInfo()

    /**
     * 查询未完成的任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryUnfinishedTaskInfo(): MutableList<TaskInfo> =
        taskManager.getUnfinishedTaskInfo()

    /**
     * 返回包含所有任务信息的LiveData
     * @return LiveData<List<TaskInfo>>
     */
    fun getAllTaskInfoLiveData() = taskManager.getAllTaskInfoLiveData()

    /**
     * 返回包含未完成的任务信息的LiveData
     * @return LiveData<MutableList<TaskInfo>>
     */
    fun getUnfinishedTaskInfoLiveData() = taskManager.getUnfinishedTaskInfoLiveData()

    /**
     *查询已完成的任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryFinishedTaskInfo() = taskManager.getFinishedTaskInfo()

    /**
     * 返回包含已完成的任务信息的LiveData
     * @return LiveData<MutableList<TaskInfo>>
     */
    fun getFinishedTaskInfoLiveData() = taskManager.getFinishedTaskInfoLiveData()

    /**
     *停止所有下载
     */
    fun stopAll() {
        downloadController.pause()
        downloadQueue.clear()
        //downloadingTask = null
    }

    /**
     *恢复所有下载
     */
    fun resumeAndStart() {
        GlobalScope.launch(Dispatchers.IO) {
            queryUnfinishedTaskInfo()
                .let {
                    if (it.isNotEmpty()) {
                        downloadQueue.clear()
                        downloadQueue.addAll(it)
                        downloadController.start()
                        switching2NextTask()
                    }
                }
        }
    }

    /**
     *取消全部任务
     */
    fun cancelAll() {
        stopAll()
        GlobalScope.launch(Dispatchers.IO) {
            taskManager.deleteTaskInfoArray(downloadQueue.toTypedArray())
            downloadQueue.clear()
            downloadingTask = null
        }
        notificationSender.cancelDownloadProgressNotification()
    }

    /**
     * 取消当前任务
     */
    fun cancel() {
        stopAll()
        if (downloadingTask != null) {
            GlobalScope.launch(Dispatchers.IO) {
                downloadQueue.poll()
                taskManager.deleteTaskInfo(downloadingTask!!)
                downloadingTask = null
                notificationSender.cancelDownloadProgressNotification()
                delay(2000)
                resumeAndStart()
            }
        }
    }

    /**
     *关闭AwesomeDownloader
     */
    fun close() {
        stopAll()
        downloadQueue.clear()
        downloadingTask = null
        appContext = null
        isInitialized = false
    }

    /**
     *
     * @param taskInfo TaskInfo
     */
    private fun notifyMediaStore(taskInfo: TaskInfo) {
        try {
            MediaStoreHelper.notifyMediaStore(
                taskInfo,
                appContext!!
            )
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "notifyMediaStore: ${e.message}", e)
            onDownloadError(e)
        }
    }

    /**
     *
     * @param onError Function1<Exception, Unit>（param1->异常）
     * @return AwesomeDownloader
     */
    fun setOnError(onError: (Exception) -> Unit): AwesomeDownloader {
        onDownloadError = onError
        return this
    }

    /**
     *
     * @param onProgressChange Function1<Long, Unit> （param1：进度 0-100）
     * @return AwesomeDownloader
     */
    fun setOnProgressChange(onProgressChange: (Long) -> Unit): AwesomeDownloader {
        onDownloadProgressChange = onProgressChange
        return this
    }

    /**
     *
     * @param onStop Function2<Long, Long, Unit> （param1：已下载的字节数, param2：总字节数）
     * @return AwesomeDownloader
     */
    fun setOnStop(onStop: (Long, Long) -> Unit): AwesomeDownloader {
        onDownloadStop = onStop
        return this
    }

    /**
     *
     * @param onFinished Function2<String, String, Unit>（param1：文件路径, param2：文件名称）
     * @return AwesomeDownloader
     */
    fun setOnFinished(onFinished: (String, String) -> Unit): AwesomeDownloader {
        onDownloadFinished = onFinished
        return this
    }

    /**
     *
     * @param sender NotificationSender
     * @return AwesomeDownloader
     */
    fun setNotificationSender(sender: NotificationSender): AwesomeDownloader {
        notificationSender = sender
        return this
    }

    private val downloadListener by lazy {
        object : BaseDownloadListener {
            override fun onProgressChange(progress: Long) {
                Log.d(TAG, "$progress %")
                if (option.showNotification) {
                    notificationSender.showDownloadProgressNotification(
                        progress.toInt(), downloadingTask?.fileName ?: "null"
                    )
                }
                GlobalScope.launch(Dispatchers.Main) {
                    onDownloadProgressChange(progress)
                }
            }

            override fun onStop(downloadBytes: Long, totalBytes: Long) {
                Log.d(TAG, "$downloadBytes b")
                val task =
                    downloadingTask
                task?.let {
                    it.downloadedBytes += downloadBytes
                    if (it.status == TASK_STATUS_UNINITIALIZED) {
                        it.totalBytes = totalBytes
                        it.status = TASK_STATUS_UNFINISHED
                    }
                    GlobalScope.launch(Dispatchers.IO) { taskManager.updateTaskInfo(it) }
                    notificationSender.showDownloadStopNotification(task.fileName)
                }
                GlobalScope.launch(Dispatchers.Main) {
                    onDownloadStop(downloadBytes, totalBytes)
                }
            }

            override fun onFinish(downloadBytes: Long, totalBytes: Long) {
                val task = downloadingTask
                task?.let {
                    if (it.status == TASK_STATUS_UNINITIALIZED) it.totalBytes = totalBytes
                    it.downloadedBytes += downloadBytes
                    it.status = TASK_STATUS_FINISH
                    GlobalScope.launch(Dispatchers.IO) { taskManager.insertTaskInfo(it) }
                    notifyMediaStore(it)
                }
                if (option.showNotification) {

                    notificationSender.showDownloadDoneNotification(
                        downloadingTask?.fileName ?: "null",
                        downloadingTask?.filePath ?: "null"
                    )
                    notificationSender.cancelDownloadProgressNotification()
                }
                GlobalScope.launch(Dispatchers.Main) {
                    onDownloadFinished(task?.filePath ?: "null", task?.fileName ?: "null")
                }
            }
        }
    }

}






