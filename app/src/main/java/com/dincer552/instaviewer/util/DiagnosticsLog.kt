package com.dincer552.instaviewer.util

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiagnosticsLog {
    private const val PREFS = "linkviewer_diagnostics"
    private const val KEY = "entries"
    private const val MAX_CHARS = 60000

    @Synchronized
    fun add(context: Context, stage: String, message: String, error: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val detail = buildString {
            append(timestamp).append(" | ").append(stage).append(" | ").append(message)
            if (error != null) {
                append(" | ").append(error.javaClass.simpleName).append(": ").append(error.message ?: "no message")
                error.stackTrace.take(8).forEach { append("\n    at ").append(it) }
            }
        }
        Log.d("LinkViewerDiag", detail)
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY, (prefs.getString(KEY, "").orEmpty() + detail + "\n").takeLast(MAX_CHARS)).apply()
    }

    fun read(context: Context): String =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "").orEmpty().ifBlank { "Henüz tanılama kaydı yok." }

    fun clear(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }

    fun safeUrl(url: String): String = runCatching {
        val uri = java.net.URI(url)
        "${uri.scheme}://${uri.host}${uri.rawPath.orEmpty()}"
    }.getOrDefault("URL çözümlenemedi")
}