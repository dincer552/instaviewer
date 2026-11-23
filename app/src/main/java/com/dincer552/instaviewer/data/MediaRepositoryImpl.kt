package com.dincer552.instaviewer.data

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MediaRepositoryImpl(
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MediaRepository {

    override fun getAllMedia(): Flow<List<MediaItem>> =
        database.mediaContentDao().getAllMedia().map { toMediaItems(it) }

    override fun getFavorites(): Flow<List<MediaItem>> =
        database.mediaContentDao().getFavorites().map { toMediaItems(it) }

    override fun getDownloaded(): Flow<List<MediaItem>> =
        database.mediaContentDao().getDownloaded().map { toMediaItems(it) }

    override fun searchMedia(query: String): Flow<List<MediaItem>> =
        database.mediaContentDao().searchMedia("%$query%").map { toMediaItems(it) }

    override fun getByPlatform(platform: String): Flow<List<MediaItem>> =
        database.mediaContentDao().getByPlatform(platform).map { toMediaItems(it) }

    override fun insertMedia(item: MediaItem): Long =
        runCatching {
            database.mediaContentDao().insert(toEntity(item))
        }.getOrDefault(-1L)

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        withContext(ioDispatcher) {
            database.mediaContentDao().setFavorite(id, isFavorite)
        }

    override suspend fun toggleDownloaded(id: Long, isDownloaded: Boolean, path: String? = null) =
        withContext(ioDispatcher) {
            database.mediaContentDao().setDownloaded(id, isDownloaded, path)
        }

    override suspend fun deleteMedia(id: Long) =
        withContext(ioDispatcher) {
            val media = database.mediaContentDao().getAllMedia().firstOrNull()
            if (media != null) {
                val target = media.firstOrNull { it.id == id }
                target?.let { database.mediaContentDao().delete(it) }
            }
        }

    override suspend fun clearAllMedia() =
        withContext(ioDispatcher) {
            database.mediaContentDao().deleteAll()
        }

    override fun insertDownload(url: String, fileName: String): Long =
        runCatching {
            database.downloadDao().insert(DownloadItem(url = url, fileName = fileName))
        }.getOrDefault(-1L)

    override fun getDownloads(): Flow<List<DownloadItem>> =
        database.downloadDao().getAllDownloads()

    override suspend fun updateDownloadProgress(id: Long, progress: Int, downloaded: Long, speed: Long, status: String) =
        withContext(ioDispatcher) {
            database.downloadDao().updateProgress(id, progress, downloaded, speed, status)
        }

    override suspend fun markDownloadCompleted(id: Long, path: String?) =
        withContext(ioDispatcher) {
            database.downloadDao().markCompleted(id, path)
        }

    override suspend fun clearAllDownloads() =
        withContext(ioDispatcher) {
            database.downloadDao().deleteAll()
        }

    private fun toMediaItems(entities: List<MediaContent>): List<MediaItem> =
        entities.map { content ->
            MediaItem(
                id = content.id,
                url = content.url,
                platform = content.platform,
                title = content.title,
                thumbnailUrl = content.thumbnailUrl,
                duration = content.duration,
                author = content.author,
                timestamp = content.timestamp.time,
                isFavorite = content.isFavorite,
                isDownloaded = content.isDownloaded
            )
        }

    private fun toEntity(item: MediaItem): MediaContent =
        MediaContent(
            id = item.id,
            url = item.url,
            platform = item.platform,
            title = item.title,
            thumbnailUrl = item.thumbnailUrl,
            duration = item.duration,
            author = item.author,
            timestamp = java.util.Date(item.timestamp),
            isFavorite = item.isFavorite,
            isDownloaded = item.isDownloaded
        )
}

fun MediaRepository.create(context: Context): MediaRepository =
    MediaRepositoryImpl(DatabaseProvider.getDatabase(context))