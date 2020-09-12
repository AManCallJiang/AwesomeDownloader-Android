package com.jiang.awesomedownloader.database

import androidx.room.*
import com.jiang.awesomedownloader.database.TASK_STATUS_FINISH
import com.jiang.awesomedownloader.database.TaskInfo

/**
 *
 * @ProjectName:    AwesomeDownloader
 * @ClassName:      TaskInfoDao
 * @Description:     java类作用描述
 * @Author:         江
 * @CreateDate:     2020/8/21 16:46
 */
@Dao
interface TaskInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg taskInfo: TaskInfo)

    @Delete
    suspend fun delete(vararg taskInfo: TaskInfo)

    @Delete
    suspend fun deleteArray(taskInfos: Array<TaskInfo>)

    @Update
    suspend fun update(vararg taskInfo: TaskInfo)

    @Query("select * from TaskInfo")
    suspend fun queryAll(): MutableList<TaskInfo>

    @Query("select * from TaskInfo where status < $TASK_STATUS_FINISH")
    suspend fun queryUnfinish(): MutableList<TaskInfo>
}