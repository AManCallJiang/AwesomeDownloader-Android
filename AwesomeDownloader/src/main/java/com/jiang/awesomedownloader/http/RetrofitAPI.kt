package com.jiang.awesomedownloader.http

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      RetrofitAPI
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/20 13:55
 */


interface RetrofitAPI {
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String,@Header("Range") byteRange:String ): ResponseBody
}