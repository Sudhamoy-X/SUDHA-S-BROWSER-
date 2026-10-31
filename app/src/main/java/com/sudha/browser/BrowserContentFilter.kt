package com.sudha.browser

import android.net.Uri
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lightweight host-based content filter for WebView subresource requests.
 * This is a best-effort filter, not a network-wide VPN or a complete cosmetic filter.
 */
object BrowserContentFilter {
    private val blockedAds = setOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adnxs.com", "adform.net", "", "taboola.com",
        "outbrain.com", "popads.net", "popcash.net", "zedo.com",
        "adroll.com", "criteo.com", "rubiconproject.com"
    )
    private val blockedTrackers = setOf(
        "scorecardresearch.com", "quantserve.com", "hotjar.com",
        "mixpanel.com", "segment.io", "segment.com", "amplitude.com",
        "clarity.ms", "branch.io", "appsflyer.com", "adjust.com",
        "facebook.net", "connect.facebook.net", "analytics.google.com",
        "google-analytics.com", "googletagmanager.com"
    )

    val blockedRequestCount = AtomicInteger(0)

    fun isBlocked(rawUrl: String, adsEnabled: Boolean, trackingEnabled: Boolean): Boolean {
        if (!adsEnabled && !trackingEnabled) return false
        val host = runCatching { Uri.parse(rawUrl).host?.lowercase()?.trimEnd('.') }.getOrNull()
            ?: return false
        if (host.isBlank()) return false
        fun matches(domains: Set<String>) = domains.any { entry ->
            val domain = entry.trim()
            domain.isNotEmpty() && (host == domain || host.endsWith(".$domain"))
        }
        return (adsEnabled && matches(blockedAds)) ||
            (trackingEnabled && matches(blockedTrackers))
    }
}
