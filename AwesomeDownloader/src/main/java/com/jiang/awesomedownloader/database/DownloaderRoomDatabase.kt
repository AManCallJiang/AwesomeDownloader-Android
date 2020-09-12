package com.jiang.awesomedownloader.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jiang.awesomedownloader.database.TaskInfo
import com.jiang.awesomedownloader.database.TaskInfoDao

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      DownloaderRoomDatabase
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/21 16:45
 */
@Database(entities = [TaskInfo::class], version = 1, exportSchema = false)
abstract class DownloaderRoomDatabase : RoomDatabase() {
    abstract fun getTaskInfoDao(): TaskInfoDao
}