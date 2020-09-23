package com.jiang.awesomedownloader.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      RoomHelper
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/21 22:03
 */
const val DATABASE_NAME = "AwesomeDownloader_DB"

class DownloadTaskManager(private val appContext: Context) {

    private val database by lazy {
        Room.databaseBuilder(appContext, DownloaderRoomDatabase::class.java, DATABASE_NAME).build()
    }

    private val dao by lazy { database.getTaskInfoDao() }

    suspend fun getAllTaskInfo(): MutableList<TaskInfo> = dao.queryAll()
    suspend fun getUnfinishedTaskInfo(): MutableList<TaskInfo> = dao.queryUnfinished()

    fun getAllTaskInfoLiveData(): LiveData<List<TaskInfo>> = dao.queryAllAndReturnLiveData()
    fun getUnfinishedTaskInfoLiveData(): LiveData<MutableList<TaskInfo>> =
        dao.queryUnfinishedLiveData()

    suspend fun getFinishedTaskInfo(): MutableList<TaskInfo> = dao.queryFinished()
    fun getFinishedTaskInfoLiveData(): LiveData<MutableList<TaskInfo>> =
        dao.queryFinishedLiveData()

    suspend fun insertTaskInfo(taskInfo: TaskInfo) {
        dao.insert(taskInfo)
    }

    suspend fun deleteTaskInfo(taskInfo: TaskInfo) {
        dao.delete()
    }

    suspend fun deleteTaskInfoArray(taskInfoArray: Array<TaskInfo>) {
        dao.deleteArray(taskInfoArray)
    }

    suspend fun updateTaskInfo(taskInfo: TaskInfo) {
        dao.update(taskInfo)
    }
}