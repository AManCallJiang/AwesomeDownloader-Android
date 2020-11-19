package com.jiang.awesomedownloader.http

import com.jiang.awesomedownloader.database.TaskInfo
import com.jiang.awesomedownloader.core.AwesomeDownloaderOption
import com.jiang.awesomedownloader.core.listener.IDownloadListener
import com.jiang.awesomedownloader.core.controller.DownloadController
import okhttp3.OkHttpClient
import okhttp3.Request
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
        downloadListener: IDownloadListener,
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
}