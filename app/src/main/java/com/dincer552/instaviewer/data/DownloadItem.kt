package com.dincer552.instaviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val fileName: String,
    val filePath: String? = null,
    val progress: Int = 0,
    val status: String = "pending",
    val fileSize: Long = 0,
    val downloadedBytes: Long = 0,
    val speed: Long = 0,
    val timestamp: Date = Date(),
    val mimeType: String? = null
)