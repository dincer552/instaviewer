package com.dincer552.instaviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_content")
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
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val fileSize: Long = 0
)