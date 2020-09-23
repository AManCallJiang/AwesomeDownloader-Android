package com.jiang.awesomedownloader.http

import com.jiang.awesomedownloader.database.TASK_STATUS_UNFINISHED
import com.jiang.awesomedownloader.database.TaskInfo
import com.jiang.awesomedownloader.downloader.AwesomeDownloader
import com.jiang.awesomedownloader.downloader.AwesomeDownloaderOption
import com.jiang.awesomedownloader.downloader.BaseDownloadListener
import com.jiang.awesomedownloader.downloader.DownloadController
import com.jiang.awesomedownloader.tool.writeFileInDisk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 *
 * @ProjectName:    AwesomeDownloaderDemo
 * @ClassName:      OkHttpManager
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/9/21 14:47
 */
object OkHttpManager {

    fun getClient(
        option: AwesomeDownloaderOption,
        downloadListener: BaseDownloadListener,
        downloadController: DownloadController
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(ProgressInterceptor(downloadListener, downloadController))
            .connectTimeout(option.timeout, TimeUnit.SECONDS)
            .build()


    fun createRequest(taskInfo: TaskInfo): Request =
        Request.Builder().url(taskInfo.url)
            .addHeader("Range", "bytes=${taskInfo.downloadedBytes}-")
            .build()
//    suspend fun download(
//        taskInfo: TaskInfo,
//        option: AwesomeDownloaderOption,
//        downloadListener: BaseDownloadListener,
//        downloadController: DownloadController
//    ) {
//        val request = Request.Builder().url(taskInfo.url)
//            .addHeader("Range", "bytes=${taskInfo.downloadedBytes}-")
//            .build()
//
//        val response =
//            getClient(option, downloadListener, downloadController).newCall(request).execute()
//        writeFileInDisk(
//            response.body()!!,
//            File(taskInfo.filePath, taskInfo.fileName),
//            taskInfo.status == TASK_STATUS_UNFINISHED
//        )
//    }
}