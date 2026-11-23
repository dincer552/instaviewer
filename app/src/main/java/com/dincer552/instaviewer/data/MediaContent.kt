package com.dincer552.instaviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "media_content")
@TypeConverters([Converters::class])
data class MediaContent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val platform: String,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val mediaType: String = "video",
    val duration: Long = 0,
    val viewCount: Long = 0,
    val author: String? = null,
    val timestamp: Date = Date(),
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val fileSize: Long = 0
)

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}