package com.dincer552.instaviewer.util

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

object LinkValidator {
    private val ALLOWED_HOSTS = setOf("instagram.com", "youtube.com", "youtu.be", "x.com", "twitter.com", "t.co", "tiktok.com", "vm.tiktok.com", "reddit.com", "facebook.com", "threads.net", "vimeo.com", "bsky.app")
    private val VIDEO_EXTENSIONS = listOf(".mp4", ".webm", ".m4v", ".mov")
    private val TRACKING_QUERY_KEYS = setOf("dmpr", "igsh", "igshid", "ig_rid", "igsi", "si", "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content", "fbclid", "gclid", "ref_src", "s")
    private val INSTAGRAM_FUNCTIONAL_QUERY_KEYS = setOf("img_index", "story_media_id")
    private val URL_PATTERN = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)

    fun isValidLink(text: String?): Boolean = extractUrl(text.orEmpty()) != null

    fun extractUrl(text: String): String? {
        val candidate = URL_PATTERN.find(text)?.value?.trimEnd('.', ',', ')', ']', '!', '?') ?: text.trim()
        return normalizeUrl(candidate)
    }

    fun normalizeUrl(raw: String): String? {
        val candidate = URL_PATTERN.find(raw)?.value?.trimEnd('.', ',', ')', ']', '!', '?') ?: raw.trim()
        val uri = runCatching { URI(candidate) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        val originalHost = uri.host?.lowercase(Locale.ROOT) ?: return null
        if (!isAllowedHost(originalHost) && !isDirectMediaPath(uri.rawPath.orEmpty())) return null

        val isInstagram = originalHost == "instagram.com" || originalHost.endsWith(".instagram.com")
        val isTwitter = originalHost == "twitter.com" || originalHost.endsWith(".twitter.com")
        val host = when {
            isInstagram -> "www.instagram.com"
            isTwitter || originalHost == "www.x.com" -> "x.com"
            else -> originalHost
        }
        var path = uri.rawPath?.ifBlank { "/" } ?: "/"
        if (isInstagram && path.matches(Regex("""/(reel|p|tv)/[^/]+/?"""))) path = path.trimEnd('/') + "/"
        val query = cleanQuery(uri.rawQuery, isInstagram)
        return buildString {
            append("https://"); append(host); append(path)
            if (!query.isNullOrBlank()) { append('?'); append(query) }
        }
    }

    fun detectPlatform(url: String): String {
        val host = runCatching { URI(url).host.orEmpty() }.getOrDefault("").lowercase(Locale.ROOT)
        return when {
            host == "instagram.com" || host.endsWith(".instagram.com") -> "Instagram"
            host == "youtube.com" || host.endsWith(".youtube.com") || host == "youtu.be" -> "YouTube"
            host == "tiktok.com" || host.endsWith(".tiktok.com") -> "TikTok"
            host == "reddit.com" || host.endsWith(".reddit.com") -> "Reddit"
            host == "facebook.com" || host.endsWith(".facebook.com") -> "Facebook"
            host == "threads.net" || host.endsWith(".threads.net") -> "Threads"
            host == "vimeo.com" || host.endsWith(".vimeo.com") -> "Vimeo"
            host == "bsky.app" || host.endsWith(".bsky.app") -> "Bluesky"
            host == "twitter.com" || host.endsWith(".twitter.com") || host == "x.com" || host.endsWith(".x.com") || host == "t.co" -> "X / Twitter"
            isDirectMediaUrl(url) -> "Direct Media"
            else -> "Unknown"
        }
    }

    fun isDirectMediaUrl(url: String): Boolean {
        val path = runCatching { URI(url).rawPath.orEmpty() }.getOrDefault("")
        return isDirectMediaPath(path)
    }

    fun youtubeVideoId(url: String): String? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null
        val id = when {
            host == "youtu.be" -> uri.path.trim('/').substringBefore('/')
            host == "youtube.com" || host.endsWith(".youtube.com") -> {
                val parts = uri.path.split('/').filter { it.isNotBlank() }
                when {
                    uri.path == "/watch" -> uri.rawQuery?.split('&')?.firstOrNull { it.substringBefore('=') == "v" }?.substringAfter('=', "")
                    parts.size >= 2 && parts[0] in setOf("shorts", "embed", "live", "v") -> parts[1]
                    else -> null
                }
            }
            else -> null
        } ?: return null
        return id.takeIf { it.matches(Regex("[A-Za-z0-9_-]{11}")) }
    }

    private fun isAllowedHost(host: String): Boolean = ALLOWED_HOSTS.any { host == it || host.endsWith(".$it") }
    private fun isDirectMediaPath(path: String): Boolean = VIDEO_EXTENSIONS.any { path.lowercase(Locale.ROOT).endsWith(it) }

    private fun cleanQuery(rawQuery: String?, instagram: Boolean): String? {
        if (rawQuery.isNullOrBlank()) return null
        val retained = rawQuery.split('&').mapNotNull { part ->
            if (part.isBlank()) return@mapNotNull null
            val rawKey = part.substringBefore('=')
            val key = runCatching { URLDecoder.decode(rawKey, StandardCharsets.UTF_8.name()).lowercase(Locale.ROOT) }
                .getOrDefault(rawKey.lowercase(Locale.ROOT))
            when {
                instagram && key in INSTAGRAM_FUNCTIONAL_QUERY_KEYS -> part
                instagram -> null
                key in TRACKING_QUERY_KEYS || key.startsWith("utm_") -> null
                else -> part
            }
        }
        return retained.takeIf { it.isNotEmpty() }?.joinToString("&")
    }
}