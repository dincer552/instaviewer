package com.dincer552.instaviewer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class MediaItem(
    val id: Long,
    val url: String,
    val platform: String,
    val title: String?,
    val thumbnailUrl: String?,
    val duration: Long,
    val author: String?,
    val timestamp: Long,
    val isFavorite: Boolean,
    val isDownloaded: Boolean
)

sealed class RepositoryResult<out T> {
    data class Success<out T>(val data: T) : RepositoryResult<T>()
    data class Error(val exception: Throwable) : RepositoryResult<Nothing>()
}

interface MediaRepository {
    fun getAllMedia(): Flow<List<MediaItem>>
    fun getFavorites(): Flow<List<MediaItem>>
    fun getDownloaded(): Flow<List<MediaItem>>
    fun searchMedia(query: String): Flow<List<MediaItem>>
    fun getByPlatform(platform: String): Flow<List<MediaItem>>
    fun insertMedia(item: MediaItem): Long
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun toggleDownloaded(id: Long, isDownloaded: Boolean, path: String? = null)
    suspend fun deleteMedia(id: Long)
    suspend fun clearAllMedia()
    fun insertDownload(url: String, fileName: String): Long
    fun getDownloads(): Flow<List<DownloadItem>>
    suspend fun updateDownloadProgress(id: Long, progress: Int, downloaded: Long, speed: Long, status: String)
    suspend fun markDownloadCompleted(id: Long, path: String?)
    suspend fun clearAllDownloads()
}