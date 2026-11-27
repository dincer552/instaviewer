package com.dincer552.instaviewer.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dincer552.instaviewer.R
import com.dincer552.instaviewer.util.DiagnosticsLog
import java.text.DecimalFormat

class DownloadsFragment : Fragment() {
    private var list: LinearLayout? = null
    private var empty: TextView? = null
    private val density get() = resources.displayMetrics.density
    private fun Int.dp(): Int = (this * density).toInt()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_downloads, container, false)
        list = root.findViewById(R.id.downloads_list)
        empty = root.findViewById(R.id.downloads_empty)
        root.findViewById<TextView>(R.id.downloads_refresh).setOnClickListener { refreshDownloads() }
        return root
    }

    override fun onResume() {
        super.onResume()
        refreshDownloads()
    }

    private data class DownloadRow(
        val id: Long, val title: String, val description: String, val status: Int,
        val reason: Int, val downloaded: Long, val total: Long, val uri: Uri?
    )

    private fun refreshDownloads() {
        val context = context ?: return
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val rows = mutableListOf<DownloadRow>()
        var cursor: Cursor? = null
        try {
            cursor = manager.query(DownloadManager.Query())
            cursor?.use { c ->
                val idCol = c.getColumnIndex(DownloadManager.COLUMN_ID)
                val titleCol = c.getColumnIndex(DownloadManager.COLUMN_TITLE)
                val descCol = c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)
                val statusCol = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val reasonCol = c.getColumnIndex(DownloadManager.COLUMN_REASON)
                val soFarCol = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalCol = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val uriCol = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                while (c.moveToNext()) {
                    val title = if (titleCol >= 0) c.getString(titleCol).orEmpty() else "Video"
                    val description = if (descCol >= 0) c.getString(descCol).orEmpty() else ""
                    // Only show downloads created by LinkViewer; do not mix other apps' downloads.
                    if (!description.contains("LinkViewer", ignoreCase = true) &&
                        !title.startsWith("Instagram_", ignoreCase = true)) continue
                    rows += DownloadRow(
                        if (idCol >= 0) c.getLong(idCol) else -1L,
                        title, description,
                        if (statusCol >= 0) c.getInt(statusCol) else DownloadManager.STATUS_FAILED,
                        if (reasonCol >= 0) c.getInt(reasonCol) else 0,
                        if (soFarCol >= 0) c.getLong(soFarCol) else 0L,
                        if (totalCol >= 0) c.getLong(totalCol) else -1L,
                        if (uriCol >= 0) c.getString(uriCol)?.let(Uri::parse) else null
                    )
                }
            }
        } catch (e: Exception) {
            DiagnosticsLog.add(context, "DOWNLOADS_QUERY_ERROR", "DownloadManager kayıtları okunamadı", e)
            Toast.makeText(context, "İndirme listesi okunamadı.", Toast.LENGTH_SHORT).show()
        }
        rows.sortByDescending { it.id }
        list?.removeAllViews()
        empty?.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        rows.forEach { row -> list?.addView(createDownloadRow(row)) }
        DiagnosticsLog.add(context, "DOWNLOADS_REFRESH", "LinkViewer indirme kaydı: ${rows.size}")
    }

    private fun createDownloadRow(row: DownloadRow): View {
        val context = requireContext()
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14.dp(), 12.dp(), 14.dp(), 12.dp())
            setBackgroundColor(Color.rgb(32, 35, 42))
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.bottomMargin = 10.dp()
            layoutParams = params
        }
        val title = TextView(context).apply {
            text = row.title
            textSize = 15f
            setTextColor(Color.WHITE)
            maxLines = 2
        }
        val state = TextView(context).apply {
            text = statusText(row)
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, 5.dp(), 0, 5.dp())
        }
        card.addView(title)
        card.addView(state)
        if (row.status == DownloadManager.STATUS_RUNNING || row.status == DownloadManager.STATUS_PENDING) {
            val progress = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = if (row.total > 0) ((row.downloaded * 100) / row.total).toInt() else 0
            }
            card.addView(progress, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5.dp()))
            if (row.total > 0) {
                card.addView(TextView(context).apply {
                    text = "${formatBytes(row.downloaded)} / ${formatBytes(row.total)}"
                    textSize = 11f
                    setTextColor(Color.LTGRAY)
                    setPadding(0, 4.dp(), 0, 0)
                })
            }
        }
        if (row.status == DownloadManager.STATUS_SUCCESSFUL) {
            card.setOnClickListener { openDownloadedFile(row) }
            card.addView(TextView(context).apply {
                text = "Açmak için dokun"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.colorSecondary))
                setPadding(0, 8.dp(), 0, 0)
            })
        }
        return card
    }

    private fun statusText(row: DownloadRow): String = when (row.status) {
        DownloadManager.STATUS_PENDING -> "Sırada bekliyor"
        DownloadManager.STATUS_RUNNING -> "İndiriliyor…"
        DownloadManager.STATUS_PAUSED -> "Duraklatıldı"
        DownloadManager.STATUS_SUCCESSFUL -> "İndirme tamamlandı · ${formatBytes(row.total)}"
        DownloadManager.STATUS_FAILED -> "İndirme başarısız · hata kodu ${row.reason}"
        else -> "Durum bilinmiyor"
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "—"
        if (bytes < 1024) return "${bytes} B"
        val kb = bytes / 1024.0
        if (kb < 1024) return DecimalFormat("0.0").format(kb) + " KB"
        return DecimalFormat("0.0").format(kb / 1024.0) + " MB"
    }

    private fun openDownloadedFile(row: DownloadRow) {
        val context = context ?: return
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        try {
            val uri = manager.getUriForDownloadedFile(row.id) ?: row.uri
            if (uri == null) {
                Toast.makeText(context, "Dosya konumu bulunamadı.", Toast.LENGTH_SHORT).show()
                return
            }
            val mime = context.contentResolver.getType(uri) ?: "video/*"
            startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        } catch (e: Exception) {
            DiagnosticsLog.add(context, "DOWNLOAD_OPEN_ERROR", "İndirilen dosya açılamadı", e)
            Toast.makeText(context, "Dosyayı açacak uygulama bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        list = null
        empty = null
        super.onDestroyView()
    }
}