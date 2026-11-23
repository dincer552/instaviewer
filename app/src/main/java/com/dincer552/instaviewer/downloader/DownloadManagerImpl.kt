package com.dincer552.instaviewer.downloader

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.UUID

class DownloadManagerImpl(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun enqueueDownload(url: String, fileName: String): UUID {
        val inputData = Data.Builder()
            .putString(DownloadWorker.KEY_URL, url)
            .putString(DownloadWorker.KEY_FILE_NAME, fileName)
            .build()

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .addTag("download_queue")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueue(request)
        return request.id
    }

    fun cancelAllDownloads() {
        workManager.cancelAllWorkByTag("download_queue")
    }

    fun cancelDownload(workId: UUID) {
        workManager.cancelWorkById(workId)
    }

    fun getDownloadState(workId: UUID): androidx.work.WorkInfo.State? {
        return workManager.getWorkInfoById(workId).get()?.state
    }

    fun isWorkFinished(state: androidx.work.WorkInfo.State?): Boolean {
        return state == androidx.work.WorkInfo.State.SUCCEEDED || state == androidx.work.WorkInfo.State.FAILED
    }
}