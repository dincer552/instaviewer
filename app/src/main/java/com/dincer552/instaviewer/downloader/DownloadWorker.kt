package com.dincer552.instaviewer.downloader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DownloadWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: "download.mp4"

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("LinkViewer download")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)

        val manager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        return Result.success()
    }

    companion object {
        const val KEY_URL = "url"
        const val KEY_FILE_NAME = "file_name"
    }
}