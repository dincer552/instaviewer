package com.dincer552.instaviewer.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MediaContent::class, DownloadItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaContentDao(): MediaContentDao
    abstract fun downloadDao(): DownloadDao
}