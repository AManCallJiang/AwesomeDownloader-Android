package com.jiang.awesomedownloader.core

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      AwesomeDownloaderOption
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/24 14:18
 */
class AwesomeDownloaderOption {
    var timeout: Long = 300
    var showNotification = true
    var notifyMediaStoreWhenItDone = true

    //未实装
    var serviceModeAutoClose = false
    var autoCloseTime = 300_000
}