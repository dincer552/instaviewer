package com.dincer552.instaviewer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE status = 'downloading' OR status = 'pending' ORDER BY timestamp DESC")
    fun getActiveDownloads(): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE status = 'completed' ORDER BY timestamp DESC")
    fun getCompletedDownloads(): Flow<List<DownloadItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadItem): Long

    @Update
    suspend fun update(download: DownloadItem)

    @Delete
    suspend fun delete(download: DownloadItem)

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()

    @Query("UPDATE downloads SET progress = :progress, downloadedBytes = :downloaded, speed = :speed, status = :status WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int, downloaded: Long, speed: Long, status: String)

    @Query("UPDATE downloads SET status = 'completed', filePath = :path WHERE id = :id")
    suspend fun markCompleted(id: Long, path: String?)
}