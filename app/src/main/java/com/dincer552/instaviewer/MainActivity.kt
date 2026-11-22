package com.dincer552.instaviewer

import android.app.Activity
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var linkInput: EditText
    private lateinit var browser: WebView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var downloadButton: Button
    private var activeUrl: String? = null
    private val background = Color.rgb(12, 14, 22)
    private val panel = Color.rgb(25, 29, 42)
    private val accent = Color.rgb(119, 92, 255)
    private val muted = Color.rgb(165, 172, 190)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = Color.BLACK
        window.decorView.systemUiVisibility = 0

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(8))
            setBackgroundColor(background)
        }
        val brandRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(5), 0, dp(12))
        }
        val brand = TextView(this).apply {
            text = "LINK\nVIEW"
            textSize = 21f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            letterSpacing = .08f
        }
        val subtitle = TextView(this).apply {
            text = "Sosyal videolar tek yerde"
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        brandRow.addView(brand)
        brandRow.addView(subtitle)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(10))
            setBackgroundColor(panel)
        }
        val prompt = TextView(this).apply {
            text = "VİDEO VEYA GÖNDERİ LİNKİ"
            textSize = 11f
            setTextColor(muted)
            setTypeface(null, android.graphics.Typeface.BOLD)
            letterSpacing = .06f
        }
        linkInput = EditText(this).apply {
            hint = "Instagram, YouTube veya X bağlantısı"
            textSize = 14f
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(muted)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(Color.rgb(38, 43, 59))
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val openButton = makeButton("Aç", accent) { openLink(linkInput.text.toString()) }
        val pasteButton = makeButton("Yapıştır", Color.rgb(54, 61, 80)) { pasteLink() }
        actionRow.addView(openButton, LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginEnd = dp(6) })
        actionRow.addView(pasteButton, LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginStart = dp(6) })
        card.addView(prompt)
        card.addView(linkInput, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(8) })
        card.addView(actionRow, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(9) })

        val chips = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(8))
        }
        chips.addView(makeChip("Instagram"))
        chips.addView(makeChip("YouTube"))
        chips.addView(makeChip("X / Twitter"))

        status = TextView(this).apply {
            text = "Bir bağlantı yapıştırarak başla."
            textSize = 11f
            setTextColor(muted)
            setPadding(dp(2), dp(4), dp(2), dp(7))
        }
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
            indeterminate = true
        }
        browser = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    progress.visibility = View.VISIBLE
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    progress.visibility = View.GONE
                    status.text = "İçerik açıldı. Oynatma, platformun erişim kurallarına bağlıdır."
                }
                override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                    val uri = request?.url ?: return false
                    if (uri.scheme == "http" || uri.scheme == "https") return false
                    return true
                }
            }
        }

        downloadButton = makeButton("Doğrudan video dosyasını indir", Color.rgb(27, 139, 105)) {
            downloadDirectVideo()
        }.apply { visibility = View.GONE }

        root.addView(brandRow)
        root.addView(card)
        root.addView(chips)
        root.addView(status)
        root.addView(progress, LinearLayout.LayoutParams(-1, dp(3)))
        root.addView(browser, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(6) })
        root.addView(downloadButton, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(8) })
        setContentView(root)
        handleIntent(intent)
    }

    private fun makeButton(label: String, color: Int, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            textSize = 12f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(color))
            setOnClickListener { action() }
        }

    private fun makeChip(label: String): TextView = TextView(this).apply {
        text = label
        textSize = 10f
        setTextColor(Color.WHITE)
        setPadding(dp(9), dp(6), dp(9), dp(6))
        setBackgroundColor(Color.rgb(38, 43, 59))
        val lp = LinearLayout.LayoutParams(-2, -2)
        lp.marginEnd = dp(6)
        layoutParams = lp
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun pasteLink() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            linkInput.setText(clip.getItemAt(0).coerceToText(this))
        } else toast("Pano boş.")
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
            val url = extractUrl(shared)
            if (url != null) {
                linkInput.setText(url)
                openLink(url)
            }
        }
    }

    private fun extractUrl(raw: String): String? {
        val candidate = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
            .find(raw)?.value?.trimEnd('.', ',', ')', ']', '!')
            ?: return null
        val uri = runCatching { Uri.parse(candidate) }.getOrNull() ?: return null
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null
        val allowed = host == "instagram.com" || host.endsWith(".instagram.com") ||
            host == "youtube.com" || host.endsWith(".youtube.com") ||
            host == "youtu.be" || host == "x.com" || host.endsWith(".x.com") ||
            host == "twitter.com" || host.endsWith(".twitter.com") ||
            host == "t.co" || isDirectVideo(candidate)
        return candidate.takeIf { allowed && uri.scheme == "https" }
    }

    private fun isDirectVideo(url: String): Boolean {
        val path = runCatching { Uri.parse(url).path.orEmpty().lowercase(Locale.ROOT) }.getOrDefault("")
        return listOf(".mp4", ".webm", ".m4v", ".mov").any { path.endsWith(it) }
    }

    private fun openLink(raw: String) {
        val url = extractUrl(raw.trim()) ?: run {
            toast("Geçerli bir Instagram, YouTube, X veya doğrudan video bağlantısı gir.")
            return
        }
        activeUrl = url
        linkInput.setText(url)
        val uri = Uri.parse(url)
        val host = uri.host.orEmpty().lowercase(Locale.ROOT)
        val youtubeId = when {
            host == "youtu.be" -> uri.lastPathSegment
            host.contains("youtube.com") && uri.path == "/watch" -> uri.getQueryParameter("v")
            host.contains("youtube.com") && uri.path?.startsWith("/shorts/") == true -> uri.pathSegments.getOrNull(1)
            else -> null
        }
        downloadButton.visibility = if (isDirectVideo(url)) View.VISIBLE else View.GONE
        when {
            !youtubeId.isNullOrBlank() -> {
                status.text = "YouTube videosu resmi oynatıcıda açılıyor."
                browser.loadUrl("https://www.youtube.com/embed/$youtubeId?playsinline=1&rel=0")
            }
            host == "x.com" || host.endsWith(".x.com") || host == "twitter.com" || host.endsWith(".twitter.com") -> {
                status.text = "X gönderisi açılıyor. Video görünürlüğü X erişimine bağlıdır."
                browser.loadUrl(url)
            }
            host.contains("instagram.com") -> {
                status.text = "Instagram gönderisi açılıyor."
                browser.loadUrl(url)
            }
            isDirectVideo(url) -> {
                status.text = "Doğrudan video bağlantısı hazır."
                browser.loadUrl(url)
            }
            else -> browser.loadUrl(url)
        }
    }

    private fun downloadDirectVideo() {
        val url = activeUrl ?: return
        if (!isDirectVideo(url)) {
            toast("Yalnızca doğrudan video dosyaları indirilebilir.")
            return
        }
        try {
            val name = Uri.parse(url).lastPathSegment?.substringBefore('?')?.takeIf { it.isNotBlank() } ?: "video.mp4"
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(name)
                .setDescription("LinkViewer video indirme")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, name)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            toast("İndirme başlatıldı. Yalnızca indirme hakkınız olan içerikleri kaydedin.")
        } catch (_: Exception) {
            toast("Video indirme başlatılamadı.")
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun onBackPressed() {
        if (::browser.isInitialized && browser.canGoBack()) browser.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::browser.isInitialized) {
            browser.stopLoading()
            browser.loadUrl("about:blank")
            browser.destroy()
        }
        super.onDestroy()
    }
}
