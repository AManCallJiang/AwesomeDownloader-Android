package com.jiang.awesomedownloader.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      TaskInfo
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/21 16:40
 */
@Entity(tableName = "TaskInfo")
data class TaskInfo(
    @PrimaryKey
    var id: Long,
    var fileName: String,
    var filePath: String,
    var url: String,
    var downloadedBytes: Long,
    var totalBytes: Long,
    var status: Int
) {
    fun getAbsolutePath() = "$filePath/$fileName"
}

const val TASK_STATUS_UNINITIALIZED = 0
const val TASK_STATUS_UNFINISHED = 1
const val TASK_STATUS_FINISH = 2