package com.jiang.awesomedownloader.downloader

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      DownloadController
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/21 16:11
 */
class DownloadController {
    private var workState =
        WorkState.RUNNING
    @Synchronized
    fun pause() {
        workState = WorkState.STOP
    }

    @Synchronized
    fun start() {
        workState = WorkState.RUNNING
    }

    fun isPause() = workState == WorkState.STOP
}

enum class WorkState {
    RUNNING, STOP
}