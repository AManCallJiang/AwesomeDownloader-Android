package com.jiang.awesomedownloader.http

import com.jiang.awesomedownloader.downloader.AwesomeDownloader
import com.jiang.awesomedownloader.downloader.AwesomeDownloaderOption
import com.jiang.awesomedownloader.downloader.DownloadController
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      RetrofitManager
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/31 16:23
 */
object RetrofitManager {
    private const val READ_TIME_OUT: Long = 5

    fun createRetrofit(
        downloadListener: AwesomeDownloader.DownloadListener,
        downloadController: DownloadController,
        option: AwesomeDownloaderOption
    ): RetrofitAPI {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1")
            .client(getClient(ProgressInterceptor(downloadListener, downloadController), option))
            .build()
            .create(RetrofitAPI::class.java)
    }

    private fun getClient(interceptor: Interceptor, option: AwesomeDownloaderOption): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.apply {
            addInterceptor(interceptor)
            connectTimeout(option.timeout, TimeUnit.SECONDS)
            readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            writeTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
        }
        return builder.build()
    }
}