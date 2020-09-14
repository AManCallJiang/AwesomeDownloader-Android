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

    val dao by lazy { database.getTaskInfoDao() }

    fun getAllTaskInfoLiveData(): LiveData<List<TaskInfo>> = dao.queryAllAndReturnLiveData()

    fun getUnfinishedTaskInfoLiveData(): LiveData<MutableList<TaskInfo>> = dao.queryUnfinishedLiveData()
}