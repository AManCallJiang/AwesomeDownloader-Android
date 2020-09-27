package com.jiang.awesomedownloader.downloader

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      BaseDownloadListener
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/27 22:29
 */
interface BaseDownloadListener {
    fun onProgressChange(downloadBytes: Long, totalBytes: Long)
    fun onFinish(downloadBytes: Long, totalBytes: Long)
    fun onStop(downloadBytes: Long, totalBytes: Long)
}