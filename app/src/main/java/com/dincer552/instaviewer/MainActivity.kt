package com.dincer552.instaviewer

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainActivity : Activity() {

    private lateinit var input: EditText
    private lateinit var web: WebView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var openBtn: Button
    private lateinit var pasteBtn: Button

    private var hasLoadedSuccessfully = false

    // WebResourceError error codes (from Android SDK)
    private companion object {
        const val ERROR_CONNECT = -6
        const val ERROR_TIMEOUT = -7
        const val ERROR_HOST_LOOKUP = -2
        const val ERROR_REDIRECT_LOOP = -9
        const val ERROR_SSL_CERTIFICATE = -11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bgColor = ContextCompat.getColor(this, R.color.app_background)
        val surfaceColor = ContextCompat.getColor(this, R.color.surface_dark)
        val textPrimary = ContextCompat.getColor(this, R.color.text_primary)
        val textSecondary = ContextCompat.getColor(this, R.color.text_secondary)
        val errorColor = ContextCompat.getColor(this, R.color.error_color)

        window.statusBarColor = bgColor
        window.navigationBarColor = Color.BLACK

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 10)
            setBackgroundColor(bgColor)
        }

        val heading = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 25f
            setTextColor(textPrimary)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(2, 8, 2, 16)
        }

        input = EditText(this).apply {
            hint = getString(R.string.hint_instagram_url)
            textSize = 15f
            setSingleLine(true)
            setTextColor(textPrimary)
            setHintTextColor(textSecondary)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(surfaceColor)
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    openLink(input.text.toString())
                    true
                } else false
            }
        }

        openBtn = Button(this).apply {
            text = getString(R.string.btn_open_link)
            setOnClickListener { openLink(input.text.toString()) }
        }

        pasteBtn = Button(this).apply {
            text = getString(R.string.btn_paste)
            setOnClickListener { pasteFromClipboard() }
        }

        status = TextView(this).apply {
            text = getString(R.string.status_initial)
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(2, 8, 2, 8)
        }

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            setVisibility(View.GONE)
            max = 100
            progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.progress_color))
        }

        web = WebView(this).apply {
            val settings: WebSettings = settings
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.mixedContentMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                WebSettings.MIXED_CONTENT_NEVER_ALLOW
            } else {
                WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
        }

        // Create WebViewClient and WebChromeClient after web is initialized
        // to ensure proper capture of 'progress' and other properties
        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progress.setVisibility(View.VISIBLE)
                status.text = getString(R.string.status_loading)
                hasLoadedSuccessfully = false
                openBtn.isEnabled = false
                pasteBtn.isEnabled = false
                input.isEnabled = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progress.setVisibility(View.GONE)
                if (!hasLoadedSuccessfully) {
                    hasLoadedSuccessfully = true
                    status.text = getString(R.string.status_loaded)
                    status.setTextColor(textSecondary)
                }
                openBtn.isEnabled = true
                pasteBtn.isEnabled = true
                input.isEnabled = true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    progress.setVisibility(View.GONE)
                    hasLoadedSuccessfully = false
                    val errorMsg = when (error?.errorCode) {
                        ERROR_CONNECT -> getString(R.string.error_no_internet)
                        ERROR_TIMEOUT -> getString(R.string.error_timeout)
                        ERROR_HOST_LOOKUP -> getString(R.string.error_host_lookup)
                        ERROR_REDIRECT_LOOP -> getString(R.string.error_redirect_loop)
                        ERROR_SSL_CERTIFICATE -> getString(R.string.error_ssl)
                        else -> getString(R.string.error_generic)
                    }
                    status.text = errorMsg
                    status.setTextColor(errorColor)
                    showErrorToast(errorMsg)
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: android.webkit.SslErrorHandler,
                error: android.net.http.SslError
            ) {
                // SSL errors on main frame - don't proceed, show error
                if (view?.url == view?.originalUrl || view?.url?.startsWith("https://") == true) {
                    handler.cancel()
                    progress.setVisibility(View.GONE)
                    hasLoadedSuccessfully = false
                    val errorMsg = "${getString(R.string.error_ssl)}: ${error.primaryError}"
                    status.text = errorMsg
                    status.setTextColor(errorColor)
                    showErrorToast(errorMsg)
                } else {
                    handler.cancel()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return shouldOverrideUrlLoading(view, url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                return if (url.startsWith("https://") || url.startsWith("http://")) {
                    // Keep Instagram and HTTP/HTTPS links in WebView
                    false
                } else {
                    // External schemes (intent:, market:, etc.) - open externally
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {
                        Log.w("InstaViewer", "Failed to open external URL: $url")
                    }
                    true
                }
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress.setProgress(newProgress)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                if (BuildConfig.DEBUG) {
                    Log.d("InstaViewer-WebView", "[${consoleMessage.messageLevel()}] ${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})")
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }

        web.setBackgroundColor(Color.BLACK)
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        root.addView(heading)
        root.addView(input, LinearLayout.LayoutParams(-1, -2))
        root.addView(openBtn, LinearLayout.LayoutParams(-1, -2))
        root.addView(pasteBtn, LinearLayout.LayoutParams(-1, -2))
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
            Toast.makeText(this, getString(R.string.error_invalid_url), Toast.LENGTH_LONG).show()
            return
        }
        input.setText(url)
        web.loadUrl(url)
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val item = clip.getItemAt(0)
            val text: CharSequence = item.coerceToText(this)
            input.setText(text)
            val url = extractInstagramUrl(text.toString())
            if (url != null) openLink(url)
        } else {
            Toast.makeText(this, getString(R.string.error_clipboard_empty), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && ::web.isInitialized && web.canGoBack()) {
            web.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (::web.isInitialized) {
            web.loadUrl("about:blank")
            web.stopLoading()
            web.webViewClient = object : WebViewClient() {}
            web.webChromeClient = object : WebChromeClient() {}
            web.destroy()
        }
        super.onDestroy()
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}