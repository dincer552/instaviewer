package com.dincer552.instaviewer.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (INSTANCE != null) return INSTANCE!!
        synchronized(AppDatabase::class.java) {
            if (INSTANCE != null) return INSTANCE!!
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "instaviewer.db"
            ).build()
            return INSTANCE!!
        }
    }
}