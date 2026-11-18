package com.dincer552.instaviewer

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var input: EditText
    private lateinit var web: WebView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(18, 18, 24)
        window.navigationBarColor = Color.BLACK

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 10)
            setBackgroundColor(Color.rgb(18, 18, 24))
        }
        val heading = TextView(this).apply {
            text = "InstaViewer"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(2, 8, 2, 16)
        }
        input = EditText(this).apply {
            hint = "Instagram bağlantısını buraya yapıştır"
            textSize = 15f
            singleLine = true
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.rgb(42, 42, 52))
        }
        val open = Button(this).apply {
            text = "Bağlantıyı aç"
            setOnClickListener { openLink(input.text.toString()) }
        }
        val paste = Button(this).apply {
            text = "Panodan yapıştır"
            setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    input.setText(clip.getItemAt(0).coerceToText(this@MainActivity))
                } else Toast.makeText(this@MainActivity, "Pano boş", Toast.LENGTH_SHORT).show()
            }
        }
        status = TextView(this).apply {
            text = "Herkese açık Instagram bağlantısını yapıştır veya Paylaş menüsünden gönder."
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(2, 8, 2, 8)
        }
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
        }
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    progress.visibility = View.VISIBLE
                    status.text = "Sayfa yükleniyor…"
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    progress.visibility = View.GONE
                    status.text = "Sayfa açıldı. Instagram bazı içerikler için giriş isteyebilir."
                }
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return false
                    return if (url.startsWith("https://") || url.startsWith("http://")) {
                        false
                    } else {
                        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
                        true
                    }
                }
            }
            webChromeClient = WebChromeClient()
            setBackgroundColor(Color.BLACK)
        }
        root.addView(heading)
        root.addView(input, LinearLayout.LayoutParams(-1, -2))
        root.addView(open, LinearLayout.LayoutParams(-1, -2))
        root.addView(paste, LinearLayout.LayoutParams(-1, -2))
        root.addView(status)
        root.addView(progress, LinearLayout.LayoutParams(-1, 5))
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val shared = when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_VIEW -> intent.dataString
            else -> null
        }
        if (!shared.isNullOrBlank()) {
            val url = extractInstagramUrl(shared)
            input.setText(url ?: shared)
            if (url != null) openLink(url)
        }
    }

    private fun extractInstagramUrl(text: String): String? {
        val match = Regex("""https?://(?:www\.)?instagram\.com/[^\s]+""", RegexOption.IGNORE_CASE).find(text)
        return match?.value?.trimEnd('.', ',', ')', ']', '!')
    }

    private fun openLink(raw: String) {
        val url = extractInstagramUrl(raw.trim()) ?: run {
            Toast.makeText(this, "Geçerli bir Instagram bağlantısı gir.", Toast.LENGTH_LONG).show()
            return
        }
        input.setText(url)
        web.loadUrl(url)
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
