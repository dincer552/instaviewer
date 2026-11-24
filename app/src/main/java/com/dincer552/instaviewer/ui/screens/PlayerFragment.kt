package com.dincer552.instaviewer.ui.screens

import android.app.DownloadManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.dincer552.instaviewer.R
import com.dincer552.instaviewer.util.LinkValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class PlayerFragment : Fragment() {
    private var exoPlayer: ExoPlayer? = null
    private var mediaWebView: WebView? = null
    private var currentUrl: String? = null
    private var currentPlatform: String = "Unknown"
    private var playerView: PlayerView? = null
    private var pipButton: ImageButton? = null
    private var statusText: TextView? = null
    private var optionsButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_player, container, false)
        playerView = root.findViewById(R.id.player_view)
        pipButton = root.findViewById(R.id.pip_button)
        statusText = root.findViewById(R.id.player_status)
        optionsButton = root.findViewById(R.id.options_button)
        currentUrl = LinkValidator.normalizeUrl(arguments?.getString("url").orEmpty())
        val url = currentUrl
        if (url == null) { showStatus("Geçerli bir bağlantı bulunamadı."); return root }
        currentPlatform = LinkValidator.detectPlatform(url)
        optionsButton?.setOnClickListener { showDownloadOptions() }
        when (currentPlatform) {
            "Direct Media" -> startDirectMedia(url)
            "Instagram" -> showInstagram(url)
            "YouTube" -> showYouTube(url)
            "X / Twitter" -> showX(url)
            else -> showPlatformPage(url)
        }
        return root
    }

    private fun startDirectMedia(url: String) {
        playerView?.visibility = View.VISIBLE
        pipButton?.visibility = View.VISIBLE
        optionsButton?.text = "Videoyu indir"
        val exo = ExoPlayer.Builder(requireContext()).build()
        exo.setMediaItem(MediaItem.fromUri(url))
        exo.prepare()
        exo.playWhenReady = true
        exo.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                showStatus("Video oynatılamadı. Bağlantı doğrudan ve erişilebilir bir medya dosyası olmalı.")
            }
        })
        exoPlayer = exo
        playerView?.player = exo
        statusText?.visibility = View.GONE
        pipButton?.setOnClickListener { activity?.enterPictureInPictureMode(PictureInPictureParams.Builder().build()) }
    }

    private fun showInstagram(url: String) {
        val web = createWebView()
        optionsButton?.text = "İndirme seçenekleri"
        showStatus("Instagram'ın resmi gömme oynatıcısı yükleniyor…")
        viewLifecycleOwner.lifecycleScope.launch {
            val responseHtml = withContext(Dispatchers.IO) { fetchInstagramOEmbed(url) }
            if (!isAdded || mediaWebView !== web) return@launch
            val embed = responseHtml ?: instagramFallbackEmbed(url)
            web.loadDataWithBaseURL("https://www.instagram.com/", htmlDocument(embed, "Instagram içeriği"), "text/html", "UTF-8", null)
            if (responseHtml == null) showStatus("oEmbed yanıt vermedi; Instagram'ın yerleşik embed yöntemi deneniyor.")
        }
    }

    private fun showYouTube(url: String) {
        val web = createWebView()
        optionsButton?.text = "İndirme seçenekleri"
        val videoId = LinkValidator.youtubeVideoId(url)
        if (videoId == null) {
            showStatus("YouTube video kimliği çözülemedi. YouTube sayfası açılıyor.")
            web.loadUrl(url)
            return
        }
        showStatus("YouTube'un resmi oynatıcısı yükleniyor…")
        val embed = """
            <div class="video-frame">
              <iframe src="https://www.youtube.com/embed/$videoId?playsinline=1&rel=0"
                title="YouTube video player" frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
            </div>
        """.trimIndent()
        web.loadDataWithBaseURL("https://www.youtube.com/", htmlDocument(embed, "YouTube video"), "text/html", "UTF-8", null)
    }

    private fun showX(url: String) {
        val web = createWebView()
        optionsButton?.text = "İndirme seçenekleri"
        showStatus("X gönderisinin resmi embed oynatıcısı yükleniyor…")
        val safeUrl = htmlEscape(url)
        val embed = """
            <blockquote class="twitter-tweet"><a href="$safeUrl">Gönderiyi X üzerinde görüntüle</a></blockquote>
            <script async src="https://platform.x.com/widgets.js" charset="utf-8"></script>
        """.trimIndent()
        web.loadDataWithBaseURL("https://x.com/", htmlDocument(embed, "X gönderisi"), "text/html", "UTF-8", null)
    }

    private fun showPlatformPage(url: String) {
        val web = createWebView()
        optionsButton?.text = "İndirme seçenekleri"
        showStatus("$currentPlatform içeriği yükleniyor…")
        web.loadUrl(url)
    }

    private fun createWebView(): WebView {
        playerView?.visibility = View.GONE
        pipButton?.visibility = View.GONE
        mediaWebView?.let { old ->
            (old.parent as? ViewGroup)?.removeView(old)
            old.destroy()
        }
        val web = WebView(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) { statusText?.visibility = View.GONE }
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    if (request?.isForMainFrame == true) showStatus("İçerik yüklenemedi. Gönderi kaldırılmış, özel veya platform tarafından sınırlandırılmış olabilir.")
                }
            }
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        (view as? ViewGroup)?.addView(web, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        mediaWebView = web
        return web
    }

    private fun fetchInstagramOEmbed(url: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val encoded = URLEncoder.encode(url, "UTF-8")
            val endpoint = URL("https://graph.facebook.com/v27.0/instagram_oembed?url=$encoded&maxwidth=658")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 18_000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "LinkViewer-Android")
            }
            if (connection.responseCode !in 200..299) return null
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                .optString("html").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun instagramFallbackEmbed(url: String): String {
        val safe = htmlEscape(url)
        return """
            <blockquote class="instagram-media" data-instgrm-permalink="$safe" data-instgrm-version="14"
              style="background:#fff;border:0;margin:1px auto;max-width:658px;width:calc(100% - 2px);">
              <a href="$safe">Bu gönderiyi Instagram'da görüntüle</a>
            </blockquote>
            <script async src="https://www.instagram.com/embed.js"></script>
        """.trimIndent()
    }

    private fun htmlDocument(body: String, title: String): String {
        val safeTitle = htmlEscape(title)
        return """
            <!doctype html><html><head>
            <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
            <meta charset="utf-8"><title>$safeTitle</title>
            <style>
              html,body{margin:0;padding:0;min-height:100%;background:#101116;color:#fff}
              body{display:flex;justify-content:center;align-items:flex-start;font-family:Arial,sans-serif}
              .video-frame{position:relative;width:100%;padding-bottom:56.25%;background:#000}
              .video-frame iframe{position:absolute;inset:0;width:100%;height:100%;border:0}
              .instagram-media{max-width:658px!important;min-width:0!important;width:calc(100% - 2px)!important;margin:1px auto!important}
              .twitter-tweet{margin:8px auto!important}
            </style></head><body><main style="width:100%;max-width:760px;">$body</main></body></html>
        """.trimIndent()
    }

    private fun htmlEscape(value: String): String = value.replace("&", "&amp;").replace("\"", "&quot;")
        .replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;")

    private fun showDownloadOptions() {
        val url = currentUrl ?: return
        when (currentPlatform) {
            "Direct Media" -> downloadDirectMedia(url)
            "Instagram" -> AlertDialog.Builder(requireContext())
                .setTitle("Instagram indirme seçenekleri")
                .setMessage("Instagram'ın resmi oEmbed servisi herkese açık gönderileri uygulama içinde gösterir; video dosyasının indirme adresini vermez. Kendi Instagram içeriklerini Hesaplar Merkezi üzerinden indirebilirsin. Başkasına ait içerikleri yalnızca indirme iznin varsa kaydet.")
                .setPositiveButton("Instagram'da aç") { _, _ -> openInAppOrBrowser(url, "com.instagram.android") }
                .setNeutralButton("Kendi içeriklerimi indir") { _, _ -> openInBrowser("https://accountscenter.instagram.com/info_and_permissions/") }
                .setNegativeButton("Kapat", null).show()
            "YouTube" -> AlertDialog.Builder(requireContext())
                .setTitle("YouTube indirme seçenekleri")
                .setMessage("YouTube'un geliştirici politikaları, YouTube videolarının üçüncü taraf uygulamalarda dosya olarak indirilmesini veya Premium dışı çevrimdışı oynatılmasını yasaklıyor. Premium üyeler desteklenen videoları YouTube uygulamasında çevrimdışı kaydedebilir. Kendi videolarını YouTube Studio'dan indirebilirsin.")
                .setPositiveButton("YouTube'da aç") { _, _ -> openInAppOrBrowser(url, "com.google.android.youtube") }
                .setNeutralButton("YouTube Studio") { _, _ -> openInAppOrBrowser("https://studio.youtube.com/", "com.google.android.apps.youtube.creator") }
                .setNegativeButton("Kapat", null).show()
            "X / Twitter" -> AlertDialog.Builder(requireContext())
                .setTitle("X video indirme seçenekleri")
                .setMessage("X, uygun ücretli abonelere yalnızca gönderi sahibi indirmeye izin verdiyse belirli videolar için indirme seçeneği sunar. X uygulamasında gönderinin üç nokta menüsünden 'Videoyu indir' seçeneğini kontrol et. İzin kapalıysa LinkViewer indirmeyi zorlamaz.")
                .setPositiveButton("X'te aç") { _, _ -> openInAppOrBrowser(url, "com.twitter.android") }
                .setNeutralButton("Resmi indirme yardımı") { _, _ -> openInBrowser("https://help.x.com/en/using-x/x-videos") }
                .setNegativeButton("Kapat", null).show()
            else -> AlertDialog.Builder(requireContext()).setTitle("İndirme kullanılamıyor")
                .setMessage("Bu platform bağlantısından doğrudan bir video dosyası alınamadı. Platformun resmi indirme seçeneklerini kullan.")
                .setPositiveButton("Bağlantıyı aç") { _, _ -> openInBrowser(url) }
                .setNegativeButton("Kapat", null).show()
        }
    }

    private fun downloadDirectMedia(url: String) {
        if (!LinkValidator.isDirectMediaUrl(url)) {
            Toast.makeText(requireContext(), "Bu bağlantı doğrudan video dosyası değil.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val uri = Uri.parse(url)
            val rawName = uri.lastPathSegment?.substringBefore('?').orEmpty()
            val fileName = rawName.replace(Regex("""[^A-Za-z0-9._-]"""), "_")
                .takeIf { it.isNotBlank() } ?: ("LinkViewer_" + System.currentTimeMillis() + ".mp4")
            val request = DownloadManager.Request(uri)
                .setTitle(fileName)
                .setDescription("LinkViewer – yalnızca indirme hakkınız olan içerikler")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true).setAllowedOverRoaming(false)
            when {
                fileName.endsWith(".mp4", true) || fileName.endsWith(".m4v", true) -> request.setMimeType("video/mp4")
                fileName.endsWith(".webm", true) -> request.setMimeType("video/webm")
                fileName.endsWith(".mov", true) -> request.setMimeType("video/quicktime")
            }
            val manager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(requireContext(), "İndirme başlatıldı. Bildirimlerden takip edebilirsin.", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "İndirme başlatılamadı. Bağlantıyı kontrol et.", Toast.LENGTH_LONG).show()
        }
    }

    private fun openInAppOrBrowser(url: String, packageName: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(packageName)) }
        catch (_: Exception) { openInBrowser(url) }
    }

    private fun openInBrowser(url: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.android.chrome")) }
        catch (_: Exception) { Toast.makeText(requireContext(), "Bağlantıyı açmak için ilgili uygulamayı veya Chrome'u yükle.", Toast.LENGTH_LONG).show() }
    }

    private fun showStatus(message: String) {
        statusText?.text = message
        statusText?.visibility = View.VISIBLE
    }

    override fun onPause() {
        mediaWebView?.onPause()
        exoPlayer?.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mediaWebView?.onResume()
        exoPlayer?.play()
    }

    override fun onDestroyView() {
        exoPlayer?.release()
        exoPlayer = null
        playerView?.player = null
        mediaWebView?.apply { stopLoading(); loadUrl("about:blank"); removeAllViews(); destroy() }
        mediaWebView = null
        playerView = null
        pipButton = null
        statusText = null
        optionsButton = null
        super.onDestroyView()
    }
}