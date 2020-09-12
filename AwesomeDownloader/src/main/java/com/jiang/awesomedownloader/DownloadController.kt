package com.jiang.awesomedownloader

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      DownloadController
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/21 16:11
 */
class DownloadController {
    private var workState = WorkState.RUNNING
    fun pause() {
        workState = WorkState.STOP
    }

    fun start() {
        workState = WorkState.RUNNING
    }

    fun isPause() = workState == WorkState.STOP
}

enum class WorkState {
    RUNNING, STOP
}