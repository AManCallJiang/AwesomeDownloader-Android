package com.jiang.awesomedownloader.core


import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.jiang.awesomedownloader.core.sender.DefaultNotificationSender
import com.jiang.awesomedownloader.core.downloader.DefaultDownloader
import com.jiang.awesomedownloader.core.downloader.ForegroundServiceDownloader
import com.jiang.awesomedownloader.core.downloader.IDownloader
import com.jiang.awesomedownloader.core.sender.NotificationSender
import com.jiang.awesomedownloader.database.TaskInfo
import com.jiang.awesomedownloader.tool.TAG
import java.util.*



/**
 *
 * @ProjectName:    AwesomeDownloaderDemo
 * @ClassName:      AwesomeDownloader
 * @Description:    门面类
 * @Author:         江
 * @CreateDate:     2020/8/18 20:04
 */
object AwesomeDownloader {
    //设置
    val option by lazy { AwesomeDownloaderOption() }

    private lateinit var realDownloader: IDownloader

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: ")
            val binder = service as ForegroundServiceDownloader.DownloadServiceBinder
            realDownloader = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected: ")
        }
    }

    var onDownloadError: LinkedList<(Exception) -> Unit> = LinkedList()
    var onDownloadProgressChange: LinkedList<(Long) -> Unit> = LinkedList()
    var onDownloadStop: LinkedList<(Long, Long) -> Unit> = LinkedList()
    var onDownloadFinished: LinkedList<(String, String) -> Unit> = LinkedList()


    lateinit var notificationSender: NotificationSender

    /**
     * 前台服务模式启动
     * @param contextWrapper ContextWrapper
     * @return AwesomeDownloader
     */
    fun initWithServiceMode(contextWrapper: ContextWrapper): AwesomeDownloader {
        initSender(contextWrapper.applicationContext)
        val serviceIntent = Intent(contextWrapper, ForegroundServiceDownloader::class.java)
        contextWrapper.apply {
            startService(serviceIntent)
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        return this
    }

    private fun initSender(appContext: Context) {
//        if (!this::notificationSender.isInitialized) {
        notificationSender = DefaultNotificationSender(appContext)
        notificationSender.createNotificationChannel()
        //}
    }

    /**
     * 默认模式启动（与页面绑定，页面销毁时，下载器也会结束生命）
     * @param activity FragmentActivity
     * @return AwesomeDownloader
     */
    fun initWithDefaultMode(activity: FragmentActivity): AwesomeDownloader {
        initSender(activity.applicationContext)
        realDownloader = ViewModelProvider(activity).get(DefaultDownloader::class.java)
        return this
    }

    fun initWithDefaultMode(fragment: Fragment): AwesomeDownloader {
        initSender(fragment.activity?.applicationContext ?: fragment.requireContext())
        realDownloader = ViewModelProvider(fragment).get(DefaultDownloader::class.java)
        return this
    }

    fun close(contextWrapper: ContextWrapper) {
        val serviceIntent = Intent(contextWrapper, ForegroundServiceDownloader::class.java)
        realDownloader.close()
        contextWrapper.apply {
            unbindService(serviceConnection)
            stopService(serviceIntent)
        }

    }

    /**
     * 任务入队
     * @param url String http下载地址
     * @param filePath String 文件绝对路径（不包括文件名），一般用路径选择器
     * @see com.jiang.awesomedownloader.tool.PathSelector 选择返回的路径
     *
     * @param fileName String 文件名（包含拓展名，否则无法判断文件类型）
     * @return AwesomeDownloader
     */
    fun enqueue(url: String, filePath: String, fileName: String): AwesomeDownloader {
        realDownloader.enqueue(url, filePath, fileName)
        return this
    }

    fun stopAll() {
        realDownloader.stopAll()
    }

    fun resume() {
        realDownloader.resumeAndStart()
    }

    fun cancelAll() {
        realDownloader.cancelAll()
    }

    fun cancel() {
        realDownloader.cancel()
    }

    fun clearCache(taskInfo: TaskInfo) {
        realDownloader.clearCache(taskInfo)
    }

    /**
     * 添加错误监听
     * @param onError Function1<Exception, Unit> 传入方法的Exception类型参数为捕获的异常
     * @return AwesomeDownloader
     */
    fun addOnErrorListener(onError: (Exception) -> Unit): AwesomeDownloader {
        onDownloadError.addLast(onError)
        return this
    }

    /**
     * 添加任务进度更改监听
     * @param onProgressChange Function1<Long, Unit> 传入方法的Long类型参数为下载进度（0-100）
     * @return AwesomeDownloader
     */
    fun addOnProgressChangeListener(onProgressChange: (Long) -> Unit): AwesomeDownloader {
        onDownloadProgressChange.addLast(onProgressChange)
        return this
    }

    /**
     * 添加任务停止监听
     * @param onStop Function2<Long, Long, Unit> 传入方法的第一个Long类型参数为下载已下载的字节数,
     * 第二个Long类型参数为文件总共要下载的字节数
     * @return AwesomeDownloader
     */
    fun addOnStopListener(onStop: (Long, Long) -> Unit): AwesomeDownloader {
        onDownloadStop.addLast(onStop)
        return this
    }

    /**
     * 添加任务完成监听
     * @param onFinished Function2<String, String, Unit> 传入方法的第一个String类型参数为文件绝对
     * 路径（不包括文件名）,第二个String类型参数为文件名
     * @return AwesomeDownloader
     */
    fun addOnFinishedListener(onFinished: (String, String) -> Unit): AwesomeDownloader {
        onDownloadFinished.addLast(onFinished)
        return this
    }

    /**
     * 移除所有监听
     */
    fun removeAllOnErrorListener() = onDownloadError.clear()

    fun removeAllOnProgressChangeListener() = onDownloadProgressChange.clear()

    fun removeAllOnStopListener() = onDownloadStop.clear()

    fun removeAllOnFinishedListener() = onDownloadFinished.clear()


    /**
     * 获取当前下载队列所有任务信息的数组
     * @return Array<(TaskInfo?)>
     */
    fun getDownloadQueueArray() = realDownloader.getDownloadQueueArray()

    /**
     * 获取当前下载任务
     * @return TaskInfo?
     */
    fun getDownloadingTask() = realDownloader.downloadingTask

    /**
     * 查询所有任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryAllTaskInfo(): MutableList<TaskInfo> = realDownloader.queryAllTaskInfo()

    /**
     * 查询未完成的任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryUnfinishedTaskInfo(): MutableList<TaskInfo> =
        realDownloader.queryUnfinishedTaskInfo()

    /**
     * 返回包含所有任务信息的LiveData
     * @return LiveData<List<TaskInfo>>
     */
    fun getAllTaskInfoLiveData() = realDownloader.getAllTaskInfoLiveData()

    /**
     * 返回包含未完成的任务信息的LiveData
     * @return LiveData<MutableList<TaskInfo>>
     */
    fun getUnfinishedTaskInfoLiveData() = realDownloader.getUnfinishedTaskInfoLiveData()

    /**
     *查询已完成的任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryFinishedTaskInfo() = realDownloader.queryFinishedTaskInfo()

    /**
     * 返回包含已完成的任务信息的LiveData
     * @return LiveData<MutableList<TaskInfo>>
     */
    fun getFinishedTaskInfoLiveData() = realDownloader.getFinishedTaskInfoLiveData()

    suspend fun deleteTaskInfo(taskInfo: TaskInfo) = realDownloader.deleteTaskInfo(taskInfo)

    suspend fun deleteTaskInfoArray(array: Array<TaskInfo>) =
        realDownloader.deleteTaskInfoArray(array)

    suspend fun deleteById(id: Long) = realDownloader.deleteTaskInfoByID(id)
}