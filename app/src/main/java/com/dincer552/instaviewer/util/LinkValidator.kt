package com.dincer552.instaviewer.util

import java.net.URL
import java.util.Locale

object LinkValidator {

    private val ALLOWED_HOSTS = listOf(
        "instagram.com", "www.instagram.com",
        "youtube.com", "www.youtube.com", "youtu.be",
        "x.com", "www.x.com",
        "twitter.com", "www.twitter.com", "t.co",
        "tiktok.com", "www.tiktok.com", "vm.tiktok.com",
        "reddit.com", "www.reddit.com",
        "facebook.com", "www.facebook.com", "m.facebook.com",
        "threads.net", "www.threads.net",
        "vimeo.com", "www.vimeo.com",
        "bsky.app", "www.bsky.app"
    )

    private val VIDEO_EXTENSIONS = listOf(".mp4", ".webm", ".m4v", ".mov")

    fun isValidLink(text: String?): Boolean {
        val url = extractUrl(text ?: "")
        return url != null
    }

    fun extractUrl(text: String): String? {
        val candidate = """https?://[^\s<>"']+""".toRegex(RegexOption.IGNORE_CASE)
            .find(text)?.value
            ?.trimEnd('.', ',', ')', ']', '!')
            ?: return null

        val uri = java.net.URI.create(candidate).toURL()
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null

        val allowed = ALLOWED_HOSTS.any { h ->
            host == h || host.endsWith(".$h")
        } || isDirectVideo(candidate)

        return candidate.takeIf { allowed && uri.protocol.equals("https", ignoreCase = true) }
    }

    fun detectPlatform(url: String): String {
        val host = try {
            java.net.URI.create(url).host ?: ""
        } catch (_: Exception) {
            ""
        }.lowercase(Locale.ROOT)

        return when {
            host.contains("instagram.com") -> "Instagram"
            host.contains("youtube.com") || host.contains("youtu.be") -> "YouTube"
            host.contains("tiktok.com") -> "TikTok"
            host.contains("reddit.com") -> "Reddit"
            host.contains("facebook.com") -> "Facebook"
            host.contains("threads.net") -> "Threads"
            host.contains("vimeo.com") -> "Vimeo"
            host.contains("bsky.app") -> "Bluesky"
            host.contains("twitter.com") -> "X / Twitter"
            host == "x.com" || host.endsWith(".x.com") -> "X / Twitter"
            host == "t.co" -> "X / Twitter"
            isDirectVideo(url) -> "Direct Media"
            else -> "Unknown"
        }
    }

    private fun isDirectVideo(url: String): Boolean {
        val path = try {
            java.net.URI.create(url).path.orEmpty().lowercase(Locale.ROOT)
        } catch (_: Exception) {
            ""
        }
        return VIDEO_EXTENSIONS.any { path.endsWith(it) }
    }
}