package com.dincer552.instaviewer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaContentDao {
    @Query("SELECT * FROM media_content ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<MediaContent>>

    @Query("SELECT * FROM media_content WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<MediaContent>>

    @Query("SELECT * FROM media_content WHERE isDownloaded = 1 ORDER BY timestamp DESC")
    fun getDownloaded(): Flow<List<MediaContent>>

    @Query("SELECT * FROM media_content WHERE platform LIKE :query OR title LIKE :query OR description LIKE :query ORDER BY timestamp DESC")
    fun searchMedia(query: String): Flow<List<MediaContent>>

    @Query("SELECT * FROM media_content WHERE platform = :platform ORDER BY timestamp DESC")
    fun getByPlatform(platform: String): Flow<List<MediaContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaContent): Long

    @Update
    suspend fun update(media: MediaContent)

    @Delete
    suspend fun delete(media: MediaContent)

    @Query("DELETE FROM media_content WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM media_content")
    suspend fun deleteAll()

    @Query("UPDATE media_content SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE media_content SET isDownloaded = :isDownloaded, localPath = :path WHERE id = :id")
    suspend fun setDownloaded(id: Long, isDownloaded: Boolean, path: String? = null)
}