package com.dincer552.instaviewer.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MediaContent::class, DownloadItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters([Converters::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaContentDao(): MediaContentDao
    abstract fun downloadDao(): DownloadDao
}