package com.jiang.awesomedownloader.http


import com.jiang.awesomedownloader.core.controller.DownloadController
import com.jiang.awesomedownloader.core.listener.IDownloadListener
import okhttp3.Interceptor
import okhttp3.Response

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      ProgressInterceptor
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/21 21:57
 */

class ProgressInterceptor(
    private val listener: IDownloadListener,
    private val downloadController: DownloadController
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())
        return originalResponse.newBuilder()
            .body(
                DownloadResponseBody(
                    originalResponse.body()!!,
                    listener,
                    downloadController
                )
            )
            .build()
    }
}
