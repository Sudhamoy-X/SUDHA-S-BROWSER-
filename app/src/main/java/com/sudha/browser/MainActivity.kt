package com.sudha.browser

import android.annotation.SuppressLint
import android.media.AudioManager
import android.app.DownloadManager
import android.print.PrintAttributes
import android.print.PrintManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.io.ByteArrayInputStream
import android.app.Activity
import android.widget.FrameLayout
import android.view.View
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private val Midnight = Color(0xFF080B14)
private val SurfaceBlue = Color(0xFF111A2B)
private val ElectricBlue = Color(0xFF398BFF)
private val Cyan = Color(0xFF45D7F5)

private data class BrowserTab(val id: Int, var url: String = "", var title: String = "New tab", var webView: WebView? = null)
private data class SavedPage(val title: String, val url: String)
private data class DownloadEntry(val id: Long, val title: String, val status: String, val downloaded: Long, val total: Long, val reason: Int, val uri: Uri?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { SudhaBrowserApp(this) } }
}


@Composable
private fun SudhaBrandMark(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "sudha-energy-ring")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5200), repeatMode = RepeatMode.Restart),
        label = "energy-ring-rotation"
    )
    val counterRotation by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(7600), repeatMode = RepeatMode.Restart),
        label = "energy-ring-counter-rotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.48f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700), repeatMode = RepeatMode.Reverse),
        label = "energy-ring-pulse"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.055f
            val pad = stroke * 1.8f
            drawCircle(
                color = Color(0xFF174BBA).copy(alpha = 0.12f + pulse * 0.12f),
                radius = size.minDimension * 0.43f,
                style = Stroke(width = stroke * 2.4f)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(Color.Transparent, Color(0xFF16DFFF).copy(alpha = pulse), Color(0xFF376BFF), Color(0xFFB34DFF), Color.Transparent)),
                startAngle = rotation,
                sweepAngle = 238f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(pad, pad),
                size = androidx.compose.ui.geometry.Size(size.width - pad * 2, size.height - pad * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(Color.Transparent, Color(0xFF8B45FF).copy(alpha = pulse * 0.8f), Color(0xFF28CFFF), Color.Transparent)),
                startAngle = counterRotation,
                sweepAngle = 92f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(pad * 1.65f, pad * 1.65f),
                size = androidx.compose.ui.geometry.Size(size.width - pad * 3.3f, size.height - pad * 3.3f),
                style = Stroke(width = stroke * 0.55f, cap = StrokeCap.Round)
            )
        }
        Box(
            Modifier.fillMaxSize(0.63f)
                .background(Brush.radialGradient(listOf(Color(0xFF182A70).copy(alpha = 0.65f), Color(0xFF080F27).copy(alpha = 0.1f))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.sudha_exact_app_icon),
                contentDescription = "SUDHA Browser logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(0.76f).graphicsLayer {
                    shadowElevation = 18f
                    scaleX = 1.03f
                    scaleY = 1.03f
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SudhaBrowserApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("sudha_browser_data", Context.MODE_PRIVATE) }
    val updateScope = rememberCoroutineScope()
    var updateStatus by remember { mutableStateOf("You are using the latest checked version.") }
    var checkingUpdate by remember { mutableStateOf(false) }
    fun readList(key: String): List<SavedPage> = runCatching {
        val array = JSONArray(prefs.getString(key, "[]"))
        (0 until array.length()).map { i -> array.getJSONObject(i).let { SavedPage(it.optString("title"), it.optString("url")) } }
    }.getOrDefault(emptyList())
    val bookmarks = remember { mutableStateListOf<SavedPage>().also { it.addAll(readList("bookmarks")) } }
    val history = remember { mutableStateListOf<SavedPage>().also { it.addAll(readList("history")) } }
    val tabs = remember { mutableStateListOf(BrowserTab(1)) }
    val recentlyClosedTabs = remember { mutableStateListOf<SavedPage>() }
    var tabSearch by remember { mutableStateOf("") }
    var savedSearch by remember { mutableStateOf("") }
    var sortBookmarksAZ by remember { mutableStateOf(false) }
    var nextTabId by remember { mutableIntStateOf(2) }
    var activeTabId by remember { mutableIntStateOf(1) }
    var address by remember { mutableStateOf("") }
    var progress by remember { mutableIntStateOf(0) }
    var screen by remember { mutableStateOf("browser") }
    var showHomeDashboard by remember { mutableStateOf(true) }
    var adsEnabled by remember { mutableStateOf(prefs.getBoolean("block_ads", true)) }
    var trackingEnabled by remember { mutableStateOf(prefs.getBoolean("block_trackers", true)) }
    var desktopMode by remember { mutableStateOf(prefs.getBoolean("desktop_mode", false)) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var searchEngine by remember { mutableStateOf(prefs.getString("search_engine", "Google") ?: "Google") }
    var showSearchEngines by remember { mutableStateOf(false) }
    var textZoom by remember { mutableIntStateOf(prefs.getInt("text_zoom", 100).coerceIn(75, 500)) }
    var showFindBar by remember { mutableStateOf(false) }
    var readerMode by remember { mutableStateOf(false) }
    var javaScriptEnabled by remember { mutableStateOf(prefs.getBoolean("javascript_enabled", true)) }
    var imagesEnabled by remember { mutableStateOf(prefs.getBoolean("images_enabled", true)) }
    var cookiesEnabled by remember { mutableStateOf(prefs.getBoolean("cookies_enabled", true)) }
    var safeBrowsingEnabled by remember { mutableStateOf(prefs.getBoolean("safe_browsing", true)) }
    var popupsBlocked by remember { mutableStateOf(prefs.getBoolean("block_popups", true)) }
    var thirdPartyCookies by remember { mutableStateOf(prefs.getBoolean("third_party_cookies", false)) }
    var dataSaverEnabled by remember { mutableStateOf(prefs.getBoolean("data_saver", false)) }
    var showShortcuts by remember { mutableStateOf(prefs.getBoolean("show_shortcuts", true)) }
    var gestureNavigationEnabled by remember { mutableStateOf(prefs.getBoolean("gesture_navigation", true)) }
    var volumeGestureEnabled by remember { mutableStateOf(prefs.getBoolean("volume_gesture", false)) }
    var elementBlockerEnabled by remember { mutableStateOf(prefs.getBoolean("element_blocker", false)) }
    var dnsProvider by remember { mutableStateOf(prefs.getString("dns_provider", "System default") ?: "System default") }
    var fullscreenVideo by remember { mutableStateOf<View?>(null) }
    var fullscreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    val originalSystemUi = remember { mutableIntStateOf(0) }
    fun exitVideoFullscreen() {
        val activity = context as? Activity
        val decor = activity?.window?.decorView as? FrameLayout
        fullscreenVideo?.let { decor?.removeView(it) }
        fullscreenVideo = null
        fullscreenCallback?.onCustomViewHidden()
        fullscreenCallback = null
        if (activity != null) activity.window.decorView.systemUiVisibility = originalSystemUi.intValue
    }
    var findText by remember { mutableStateOf("") }
    BrowserContentFilter.adsEnabled = adsEnabled
    BrowserContentFilter.trackingEnabled = trackingEnabled
    var downloads by remember { mutableStateOf<List<DownloadEntry>>(emptyList()) }
    val downloadManager = remember { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    fun refreshDownloads() {
        val result = mutableListOf<DownloadEntry>()
        val cursor = downloadManager.query(DownloadManager.Query().setFilterByStatus(
            DownloadManager.STATUS_PENDING or DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PAUSED or DownloadManager.STATUS_SUCCESSFUL or DownloadManager.STATUS_FAILED
        ))
        cursor?.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                val title = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)) ?: "Downloaded file"
                val statusCode = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val status = when (statusCode) {
                    DownloadManager.STATUS_PENDING -> "Waiting"
                    DownloadManager.STATUS_RUNNING -> "Downloading"
                    DownloadManager.STATUS_PAUSED -> "Paused / waiting for network"
                    DownloadManager.STATUS_SUCCESSFUL -> "Completed"
                    DownloadManager.STATUS_FAILED -> "Failed"
                    else -> "Unknown"
                }
                val downloaded = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                result.add(DownloadEntry(id, title, status, downloaded, total, reason, downloadManager.getUriForDownloadedFile(id)))
            }
        }
        downloads = result
    }

    LaunchedEffect(screen) {
        if (screen == "downloads") {
            while (true) {
                refreshDownloads()
                delay(1000)
            }
        }
    }
    val activeTab = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()

    fun persist(key: String, list: List<SavedPage>) {
        val array = JSONArray()
        list.forEach { array.put(JSONObject().put("title", it.title).put("url", it.url)) }
        prefs.edit().putString(key, array.toString()).apply()
    }
    fun navigate(raw: String) {
        val value = raw.trim()
        if (value.isBlank()) return
        val target = when {
            value.startsWith("https://") || value.startsWith("http://") -> value
            value.contains(" ") || !value.contains(".") -> (when (searchEngine) { "Bing" -> "https://www.bing.com/search?q="; "DuckDuckGo" -> "https://duckduckgo.com/?q="; "Brave" -> "https://search.brave.com/search?q="; else -> "https://www.google.com/search?q=" }) + Uri.encode(value)
            else -> "https://$value"
        }
        activeTab.url = target; activeTab.title = target; address = target; screen = "browser"; showHomeDashboard = false
        activeTab.webView?.loadUrl(target)
    }
    fun addTab() { val tab = BrowserTab(nextTabId++); tabs.add(tab); activeTabId = tab.id; address = ""; screen = "browser"; showHomeDashboard = true }
    fun closeTab(id: Int) {
        val index = tabs.indexOfFirst { it.id == id }; if (index < 0) return
        val closing = tabs[index]
        if (closing.url.isNotBlank()) { recentlyClosedTabs.add(0, SavedPage(closing.title, closing.url)); while (recentlyClosedTabs.size > 10) recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex) }
        closing.webView?.apply { stopLoading(); destroy() }; tabs.removeAt(index)
        if (tabs.isEmpty()) {
            val fresh = BrowserTab(nextTabId++)
            tabs.add(fresh)
            activeTabId = fresh.id
            address = ""
            screen = "browser"
            showHomeDashboard = true
        } else if (activeTabId == id) {
            val selected = tabs[(index - 1).coerceAtLeast(0)]
            activeTabId = selected.id
            address = selected.url
            showHomeDashboard = selected.url.isBlank()
            screen = "browser"
        }
    }
    fun duplicateActiveTab() { val source = activeTab; val copy = BrowserTab(nextTabId++, source.url, source.title); tabs.add(copy); activeTabId = copy.id; address = copy.url; screen = "browser"; showHomeDashboard = copy.url.isBlank() }
    fun reopenLastClosedTab() { val last = recentlyClosedTabs.firstOrNull() ?: return; recentlyClosedTabs.removeAt(0); val tab = BrowserTab(nextTabId++, last.url, last.title); tabs.add(tab); activeTabId = tab.id; address = tab.url; screen = "browser"; showHomeDashboard = false }
    fun closeOtherTabs() { val keep = activeTabId; tabs.filter { it.id != keep }.forEach { tab -> if (tab.url.isNotBlank()) recentlyClosedTabs.add(0, SavedPage(tab.title, tab.url)); tab.webView?.apply { stopLoading(); destroy() } }; while (recentlyClosedTabs.size > 10) recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex); tabs.removeAll { it.id != keep } }
    fun closeAllTabs() { tabs.forEach { tab -> if (tab.url.isNotBlank()) recentlyClosedTabs.add(0, SavedPage(tab.title, tab.url)); tab.webView?.apply { stopLoading(); destroy() } }; while (recentlyClosedTabs.size > 10) recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex); tabs.clear(); val fresh = BrowserTab(nextTabId++); tabs.add(fresh); activeTabId = fresh.id; address = ""; screen = "browser"; showHomeDashboard = true }

    fun saveBookmark() {
        if (activeTab.url.isBlank()) return
        val item = SavedPage(activeTab.title, activeTab.url)
        if (bookmarks.none { it.url == item.url }) { bookmarks.add(0, item); persist("bookmarks", bookmarks) }
    }
    fun openSaved(item: SavedPage) { activeTab.url = item.url; activeTab.title = item.title; address = item.url; screen = "browser"; showHomeDashboard = false; activeTab.webView?.loadUrl(item.url) }

    BackHandler(enabled = fullscreenVideo != null || screen != "browser" || !showHomeDashboard || activeTab.webView?.canGoBack() == true) {
        if (fullscreenVideo != null) {
            exitVideoFullscreen()
        } else if (screen != "browser") {
            screen = "browser"
            showHomeDashboard = activeTab.url.isBlank()
            address = activeTab.url
        } else if (activeTab.webView?.canGoBack() == true) {
            activeTab.webView?.goBack()
        } else {
            showHomeDashboard = true
            address = ""
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = ElectricBlue, secondary = Cyan, background = Midnight, surface = SurfaceBlue)) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF050A18), Color(0xFF08152C), Color(0xFF050914)))).statusBarsPadding()) {
            if (screen != "browser") Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SudhaBrandMark(Modifier.size(54.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("SUDHA BROWSER", color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1, softWrap = false)
                        Text("Your Web. Your Way.", color = Cyan, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { saveBookmark() }) { Icon(Icons.Default.Bookmark, "Bookmark page", tint = Color.White) }
                    IconButton(onClick = { showFindBar = !showFindBar; findText = "" }) { Icon(Icons.Default.FindInPage, "Find in page", tint = Color.White) }
                    IconButton(onClick = { val url = activeTab.url; if (url.isNotBlank()) runCatching { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url), "Share page")) } }) { Icon(Icons.Default.Share, "Share page", tint = Color.White) }
                    IconButton(onClick = { activeTab.webView?.reload() }) { Icon(Icons.Default.Refresh, "Reload", tint = Color.White) }
                    IconButton(onClick = { screen = "settings" }) { Icon(Icons.Default.Settings, "Settings", tint = Color.White) }
                    IconButton(onClick = { screen = "tabs" }) { BadgedBox(badge = { Badge { Text(tabs.size.toString()) } }) { Icon(Icons.Default.Tab, "Tabs", tint = Color.White) } }
                }
            }
            if (screen == "browser") {
                if (!showHomeDashboard) Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).background(SurfaceBlue, CircleShape).padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = Cyan); Spacer(Modifier.width(8.dp))
                    TextField(value = address, onValueChange = { address = it }, modifier = Modifier.weight(1f), placeholder = { Text("Search or enter address", color = Color.LightGray) }, singleLine = true,
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Cyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go), keyboardActions = KeyboardActions(onGo = { navigate(address) }))
                    IconButton(onClick = { navigate(address) }) { Icon(Icons.Default.ArrowForward, "Go", tint = Cyan) }
                }
                if (!showHomeDashboard) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { activeTab.webView?.goBack() }, enabled = activeTab.webView?.canGoBack() == true) {
                            Icon(Icons.Default.ArrowBack, "Go back", tint = if (activeTab.webView?.canGoBack() == true) Color.White else Color.Gray)
                        }
                        IconButton(onClick = { activeTab.webView?.goForward() }, enabled = activeTab.webView?.canGoForward() == true) {
                            Icon(Icons.Default.ArrowForward, "Go forward", tint = if (activeTab.webView?.canGoForward() == true) Color.White else Color.Gray)
                        }
                        IconButton(onClick = { if (progress in 1..99) activeTab.webView?.stopLoading() else activeTab.webView?.reload() }) {
                            Icon(if (progress in 1..99) Icons.Default.Close else Icons.Default.Refresh, if (progress in 1..99) "Stop loading" else "Reload page", tint = Cyan)
                        }
                        IconButton(onClick = { saveBookmark() }) {
                            Icon(Icons.Default.Bookmark, "Save bookmark", tint = Cyan)
                        }
                        IconButton(onClick = {
                            val url = activeTab.url
                            if (url.isNotBlank()) runCatching {
                                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url), "Share page"))
                            }
                        }) { Icon(Icons.Default.Share, "Share page", tint = Color.White) }
                        IconButton(onClick = { showFindBar = !showFindBar; findText = "" }) {
                            Icon(Icons.Default.FindInPage, "Find in page", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val url = activeTab.url
                            if (url.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Page URL", url))
                            }
                        }) { Icon(Icons.Default.ContentCopy, "Copy link", tint = Color.White) }
                        IconButton(onClick = { addTab() }) { Icon(Icons.Default.Add, "New tab", tint = Color.White) }
                        IconButton(onClick = { showHomeDashboard = true; address = "" }) {
                            Icon(Icons.Default.Home, "Home page", tint = Color.White)
                        }
                    }
                }
                if (showHomeDashboard && prefs.getBoolean("show_shortcuts", true)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        listOf("Google" to "https://www.google.com", "YouTube" to "https://m.youtube.com", "Gmail" to "https://mail.google.com", "Maps" to "https://maps.google.com").forEachIndexed { index, item ->
                            val tint = listOf(Color(0xFF55B7FF), Color(0xFFFF5577), Color(0xFFFF657D), Color(0xFF55D6B0))[index]
                            Column(Modifier.weight(1f).background(Brush.verticalGradient(listOf(Color(0xFF142541), Color(0xFF0B1426))), RoundedCornerShape(18.dp)).border(1.dp, Color(0xFF294B7A), RoundedCornerShape(18.dp)).clickable { navigate(item.second) }.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(34.dp).background(tint.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) {
                                    Text(item.first.take(1), color = tint, style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(item.first, color = Color(0xFFD7E8FF), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
                if (!showHomeDashboard && showFindBar) Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = findText, onValueChange = { findText = it; activeTab.webView?.findAllAsync(it) }, modifier = Modifier.weight(1f), placeholder = { Text("Find on page") }, singleLine = true)
                    TextButton(onClick = { activeTab.webView?.findAllAsync(findText); activeTab.webView?.findNext(true) }) { Text("Find") }
                    TextButton(onClick = { activeTab.webView?.findNext(true) }) { Text("Next") }
                    TextButton(onClick = { activeTab.webView?.clearMatches(); showFindBar = false; findText = "" }) { Text("Close") }
                }
                if (!showHomeDashboard && progress in 1..99) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(2.dp), color = ElectricBlue, trackColor = Midnight)
                if (showHomeDashboard) {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(18.dp))
                        SudhaBrandMark(Modifier.size(112.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("SUDHA", color = Color.White, style = MaterialTheme.typography.headlineLarge, letterSpacing = 8.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text("B R O W S E R", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 5.sp)
                        Spacer(Modifier.height(7.dp))
                        Text("FASTER  •  SAFER  •  TOGETHER", color = Color(0xFF9AB9E4), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(24.dp))
                        Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF142747), Color(0xFF101A33))), RoundedCornerShape(22.dp)).border(1.dp, Color(0xFF28518A), RoundedCornerShape(22.dp)).padding(start = 14.dp, end = 6.dp, top = 5.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null, tint = Cyan, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(8.dp))
                            TextField(value = address, onValueChange = { address = it }, modifier = Modifier.weight(1f), placeholder = { Text("Search or type URL", color = Color(0xFF9BAFCB)) }, singleLine = true, colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Cyan), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go), keyboardActions = KeyboardActions(onGo = { navigate(address) }))
                            IconButton(onClick = { navigate(address) }) { Icon(Icons.Default.ArrowForward, "Search", tint = Cyan) }
                        }
                        if (showShortcuts) {
                            Spacer(Modifier.height(18.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                listOf("Google" to "https://www.google.com", "YouTube" to "https://m.youtube.com", "Gmail" to "https://mail.google.com", "Maps" to "https://maps.google.com").forEachIndexed { index, item ->
                                    val tint = listOf(Color(0xFF55B7FF), Color(0xFFFF5577), Color(0xFFFF657D), Color(0xFF55D6B0))[index]
                                    Column(Modifier.weight(1f).background(Brush.verticalGradient(listOf(Color(0xFF172B4A), Color(0xFF0A1428))), RoundedCornerShape(17.dp)).border(1.dp, Color(0xFF294B7A), RoundedCornerShape(17.dp)).clickable { navigate(item.second) }.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(Modifier.size(34.dp).background(tint.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) { Text(item.first.take(1), color = tint, style = MaterialTheme.typography.titleMedium) }
                                        Spacer(Modifier.height(6.dp)); Text(item.first, color = Color(0xFFD7E8FF), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("QUICK ACCESS", color = Color.White, style = MaterialTheme.typography.titleSmall, letterSpacing = 1.5.sp); Spacer(Modifier.weight(1f)); Text("Crafted by Sudhamoy", color = Color(0xFF7EA6D8), style = MaterialTheme.typography.labelSmall) }
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            listOf(Triple("Saved", "bookmarks", Color(0xFF4CBFFF)), Triple("History", "history", Color(0xFFB17BFF)), Triple("Downloads", "downloads", Color(0xFF52D6B0)), Triple("Settings", "settings", Color(0xFFFFB86B))).forEach { item ->
                                Column(Modifier.weight(1f).background(Color(0xFF0D192D), RoundedCornerShape(15.dp)).border(1.dp, Color(0xFF223D62), RoundedCornerShape(15.dp)).clickable { screen = item.second }.padding(vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.size(32.dp).background(item.third.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) { Text(item.first.take(1), color = item.third, style = MaterialTheme.typography.titleMedium) }
                                    Spacer(Modifier.height(5.dp)); Text(item.first, color = Color(0xFFD7E8FF), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF0D1E3A), Color(0xFF17113A))), RoundedCornerShape(18.dp)).border(1.dp, Color(0xFF273F77), RoundedCornerShape(18.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("YOUR WEB. YOUR WAY.", color = Color.White, style = MaterialTheme.typography.titleSmall); Text("A cleaner, faster browsing space.", color = Color(0xFF9CB8DC), style = MaterialTheme.typography.bodySmall) }
                            SudhaBrandMark(Modifier.size(48.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                } else {
                key(activeTab.id) {
                    AndroidView(
                        modifier = Modifier.weight(1f).fillMaxWidth().pointerInput(activeTab.id, gestureNavigationEnabled) {
                            if (gestureNavigationEnabled) {
                                var drag = 0f
                                detectHorizontalDragGestures(onHorizontalDrag = { change, amount -> change.consume(); drag += amount }, onDragEnd = { if (drag > 90f) activeTab.webView?.goBack() else if (drag < -90f) activeTab.webView?.goForward(); drag = 0f })
                            }
                        }.pointerInput(activeTab.id, volumeGestureEnabled) {
                            if (volumeGestureEnabled) {
                                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                var accumulated = 0f
                                detectDragGestures(onDragStart = { accumulated = 0f }, onDrag = { change, amount ->
                                    if (change.position.x >= size.width * 0.88f && kotlin.math.abs(amount.y) > kotlin.math.abs(amount.x)) {
                                        change.consume()
                                        accumulated += amount.y
                                        if (kotlin.math.abs(accumulated) >= 24f) {
                                            val step = if (accumulated < 0f) 1 else -1
                                            val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                                            audio.setStreamVolume(AudioManager.STREAM_MUSIC, (current + step).coerceIn(0, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0)
                                            accumulated = 0f
                                        }
                                    }
                                })
                            }
                        },
                        factory = { ctx: Context ->
                        (activeTab.webView ?: WebView(ctx).also { activeTab.webView = it }).apply {
                            settings.javaScriptEnabled = javaScriptEnabled; settings.domStorageEnabled = true; settings.loadsImagesAutomatically = imagesEnabled && !dataSaverEnabled; settings.blockNetworkImage = dataSaverEnabled; settings.javaScriptCanOpenWindowsAutomatically = !popupsBlocked; settings.setSupportMultipleWindows(!popupsBlocked)
                            settings.safeBrowsingEnabled = safeBrowsingEnabled
                            CookieManager.getInstance().setAcceptCookie(cookiesEnabled)
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, thirdPartyCookies)
                            settings.textZoom = textZoom
                            settings.setSupportZoom(true); settings.builtInZoomControls = true; settings.displayZoomControls = false
                            settings.userAgentString = if (desktopMode) "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36" else android.webkit.WebSettings.getDefaultUserAgent(ctx)
                            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                                val request = DownloadManager.Request(Uri.parse(url)).apply {
                                    setMimeType(mimeType); addRequestHeader("User-Agent", userAgent)
                                    setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType)); setDescription("Downloading with SUDHA BROWSER")
                                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType))
                                }
                                downloadManager.enqueue(request)
                            }
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                    val target = request?.url ?: return false
                                    val scheme = target.scheme?.lowercase() ?: return false
                                    if (scheme == "http" || scheme == "https") return false
                                    return runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        true
                                    }.getOrDefault(true)
                                }
                                override fun shouldInterceptRequest(view: WebView?, request: android.webkit.WebResourceRequest?): android.webkit.WebResourceResponse? {
                                    val requestUrl = request?.url?.toString() ?: return null
                                    if (BrowserContentFilter.isBlocked(requestUrl)) {
                                        BrowserContentFilter.blockedRequestCount.incrementAndGet()
                                        return android.webkit.WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) { if (url != null) { activeTab.url = url; if (activeTab.id == activeTabId) address = url } }
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    if (readerMode) view?.evaluateJavascript("(function(){var s=document.getElementById('sudha-reader-style');if(!s){s=document.createElement('style');s.id='sudha-reader-style';document.head.appendChild(s);}s.textContent='body{max-width:780px!important;margin:24px auto!important;padding:24px!important;font-family:Georgia,serif!important;font-size:20px!important;line-height:1.8!important;background:#f5f1e8!important;color:#242424!important}';})();", null)
                                    if (url != null) {
                                        activeTab.url = url; activeTab.title = view?.title?.takeIf { it.isNotBlank() } ?: url; if (activeTab.id == activeTabId) address = url
                                        val item = SavedPage(activeTab.title, url)
                                        history.removeAll { it.url == url }; history.add(0, item)
                                        while (history.size > 500) history.removeAt(history.lastIndex)
                                        persist("history", history)
                                    }
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) { progress = newProgress }
                                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                    val activity = context as? Activity
                                    val decor = activity?.window?.decorView as? FrameLayout
                                    if (view == null || decor == null) { callback?.onCustomViewHidden(); return }
                                    if (fullscreenVideo != null) { callback?.onCustomViewHidden(); return }
                                    originalSystemUi.intValue = activity.window.decorView.systemUiVisibility
                                    fullscreenVideo = view
                                    fullscreenCallback = callback
                                    decor.addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                                    activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                                }
                                override fun onHideCustomView() { exitVideoFullscreen() }
                            }
                            if (this.url.isNullOrBlank() && activeTab.url.isNotBlank()) loadUrl(activeTab.url)
                        }
                    }, update = { view: WebView -> if (view.url != activeTab.url && activeTab.url.isNotBlank()) view.loadUrl(activeTab.url) })
                }
                }
            } else {
                val heading = when (screen) { "tabs" -> "Your tabs"; "bookmarks" -> "Bookmarks"; "history" -> "History"; "settings" -> "Privacy & settings"; else -> "Downloads" }
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(heading, color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    if (screen == "tabs") Text("${tabs.size} open", color = Cyan)
                    if (screen == "tabs") FilledIconButton(onClick = { addTab() }) { Icon(Icons.Default.Add, "New tab") }
                }
                when (screen) {
                    "settings" -> Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("GENERAL", color = Cyan, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Data Saver", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Blocks page images to reduce mobile data use", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = dataSaverEnabled, onCheckedChange = { enabled -> dataSaverEnabled = enabled; prefs.edit().putBoolean("data_saver", enabled).apply(); tabs.forEach { tab -> tab.webView?.settings?.apply { blockNetworkImage = enabled; loadsImagesAutomatically = imagesEnabled && !enabled } } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Swipe navigation", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Swipe right to go back, left to go forward", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = gestureNavigationEnabled, onCheckedChange = { gestureNavigationEnabled = it; prefs.edit().putBoolean("gesture_navigation", it).apply() })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("GESTURES & TOOLS", color = Cyan, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Volume gesture", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Vertical swipe on the right edge adjusts media volume", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = volumeGestureEnabled, onCheckedChange = { volumeGestureEnabled = it; prefs.edit().putBoolean("volume_gesture", it).apply() })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Element blocker", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Tap unwanted items on a page to hide them", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = elementBlockerEnabled, onCheckedChange = { enabled ->
                                elementBlockerEnabled = enabled
                                prefs.edit().putBoolean("element_blocker", enabled).apply()
                                activeTab.webView?.evaluateJavascript(if (enabled) "(function(){if(window.__sudhaElementBlocker)return;window.__sudhaElementBlocker=true;window.__sudhaPickElement=true;document.addEventListener('click',function(e){if(!window.__sudhaPickElement)return;var el=e.target;if(!el||el===document.body)return;e.preventDefault();e.stopPropagation();el.style.setProperty('display','none','important');},true);})();" else "window.__sudhaPickElement=false;", null)
                            })
                        }
                        Text("Hidden items return after a full page reload. This is a basic page-level blocker.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("DNS SETTINGS", color = Cyan, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("DNS provider preference", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("WebView uses Android system DNS. This preference does not create an app-level DoH tunnel.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(onClick = { dnsProvider = when (dnsProvider) { "System default" -> "Cloudflare (1.1.1.1)"; "Cloudflare (1.1.1.1)" -> "Google (dns.google)"; else -> "System default" }; prefs.edit().putString("dns_provider", dnsProvider).apply() }, modifier = Modifier.fillMaxWidth()) { Text("Selected: $dnsProvider") }
                            OutlinedButton(onClick = { runCatching { context.startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }, modifier = Modifier.fillMaxWidth()) { Text("Open Android network / Private DNS settings") }
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("WEBSITE & PRIVACY", color = Cyan, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("JavaScript", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Some websites require JavaScript to work", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = javaScriptEnabled, onCheckedChange = { javaScriptEnabled = it; prefs.edit().putBoolean("javascript_enabled", it).apply(); tabs.forEach { tab -> tab.webView?.settings?.javaScriptEnabled = it } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Load images", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Turn off to reduce image data usage", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = imagesEnabled, onCheckedChange = { imagesEnabled = it; prefs.edit().putBoolean("images_enabled", it).apply(); tabs.forEach { tab -> tab.webView?.settings?.loadsImagesAutomatically = it && !dataSaverEnabled } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Website cookies", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Disabling cookies may sign you out of websites", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = cookiesEnabled, onCheckedChange = { cookiesEnabled = it; prefs.edit().putBoolean("cookies_enabled", it).apply(); CookieManager.getInstance().setAcceptCookie(it); tabs.forEach { tab -> tab.webView?.let { web -> CookieManager.getInstance().setAcceptThirdPartyCookies(web, false) } } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Safe Browsing", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Use Android WebView's known harmful-site protection", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = safeBrowsingEnabled, onCheckedChange = { safeBrowsingEnabled = it; prefs.edit().putBoolean("safe_browsing", it).apply(); tabs.forEach { tab -> tab.webView?.settings?.safeBrowsingEnabled = it } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Block pop-ups", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Prevent websites from opening unexpected windows", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = popupsBlocked, onCheckedChange = { enabled -> popupsBlocked = enabled; prefs.edit().putBoolean("block_popups", enabled).apply(); tabs.forEach { tab -> tab.webView?.settings?.apply { javaScriptCanOpenWindowsAutomatically = !enabled; setSupportMultipleWindows(!enabled) } } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Third-party cookies", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Allow cross-site cookies when a website requires them", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = thirdPartyCookies, onCheckedChange = { enabled -> thirdPartyCookies = enabled; prefs.edit().putBoolean("third_party_cookies", enabled).apply(); tabs.forEach { tab -> tab.webView?.let { CookieManager.getInstance().setAcceptThirdPartyCookies(it, enabled) } } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("HOME & SEARCH", color = Cyan, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Homepage shortcuts", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("Show quick links below the address bar", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = showShortcuts, onCheckedChange = { showShortcuts = it; prefs.edit().putBoolean("show_shortcuts", it).apply() })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Block advertisements", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("Blocks requests to known advertising domains", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = adsEnabled, onCheckedChange = {
                                adsEnabled = it
                                prefs.edit().putBoolean("block_ads", it).apply()
                                BrowserContentFilter.adsEnabled = it
                            })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Tracking protection", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("Blocks requests to selected analytics and tracking domains", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = trackingEnabled, onCheckedChange = {
                                trackingEnabled = it
                                prefs.edit().putBoolean("block_trackers", it).apply()
                                BrowserContentFilter.trackingEnabled = it
                            })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Desktop site", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("Request desktop versions of websites", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = desktopMode, onCheckedChange = {
                                desktopMode = it
                                prefs.edit().putBoolean("desktop_mode", it).apply()
                                tabs.forEach { tab ->
                                    tab.webView?.let { webView ->
                                        webView.settings.userAgentString = if (it) "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36" else android.webkit.WebSettings.getDefaultUserAgent(context)
                                        if (tab.id == activeTabId) webView.reload()
                                    }
                                }
                            })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Search engine", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("Choose the provider used for address-bar searches.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            Box {
                                OutlinedButton(onClick = { showSearchEngines = true }, modifier = Modifier.fillMaxWidth()) { Text(searchEngine); Spacer(Modifier.weight(1f)); Text("Change") }
                                DropdownMenu(expanded = showSearchEngines, onDismissRequest = { showSearchEngines = false }) {
                                    listOf("Google", "Bing", "DuckDuckGo", "Brave").forEach { engine -> DropdownMenuItem(text = { Text(engine) }, onClick = { searchEngine = engine; prefs.edit().putString("search_engine", engine).apply(); showSearchEngines = false }) }
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Website text size", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("$textZoom%", color = Cyan, style = MaterialTheme.typography.labelLarge)
                            Slider(value = textZoom.toFloat(), onValueChange = { textZoom = it.toInt().coerceIn(75, 500) }, valueRange = 75f..500f, onValueChangeFinished = { prefs.edit().putInt("text_zoom", textZoom).apply(); tabs.forEach { it.webView?.settings?.textZoom = textZoom } })
                            Text("Adjust webpage text without changing image or layout zoom.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("PAGE TOOLS", color = Cyan, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Page tools", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("Extra reading and accessibility tools for the current page.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(onClick = {
                                readerMode = !readerMode
                                val js = if (readerMode) "(function(){var s=document.getElementById('sudha-reader-style');if(!s){s=document.createElement('style');s.id='sudha-reader-style';document.head.appendChild(s);}s.textContent='body{max-width:780px!important;margin:24px auto!important;padding:24px!important;font-family:Georgia,serif!important;font-size:20px!important;line-height:1.8!important;background:#f5f1e8!important;color:#242424!important}';})();" else "document.getElementById('sudha-reader-style')?.remove();"
                                activeTab.webView?.evaluateJavascript(js, null)
                            }, modifier = Modifier.fillMaxWidth()) { Text(if (readerMode) "Turn off Reader Mode" else "Enable Reader Mode") }
                            OutlinedButton(onClick = {
                                activeTab.webView?.evaluateJavascript("(function(){if(!('speechSynthesis' in window)){return;}window.speechSynthesis.cancel();var t=document.body.innerText||document.title;var u=new SpeechSynthesisUtterance(t.slice(0,12000));window.speechSynthesis.speak(u);})();", null)
                            }, modifier = Modifier.fillMaxWidth()) { Text("Read aloud") }
                            OutlinedButton(onClick = {
                                val target = "https://translate.google.com/translate?sl=auto&tl=en&u=" + Uri.encode(activeTab.url)
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target))) }
                            }, modifier = Modifier.fillMaxWidth()) { Text("Translate page to English") }
                            OutlinedButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Page URL", activeTab.url))
                            }, modifier = Modifier.fillMaxWidth()) { Text("Copy page link") }
                            OutlinedButton(onClick = {
                                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                                val adapter = activeTab.webView?.createPrintDocumentAdapter("SudhaBrowser-${System.currentTimeMillis()}")
                                if (adapter != null) printManager.print(activeTab.title.take(40), adapter, PrintAttributes.Builder().build())
                            }, modifier = Modifier.fillMaxWidth()) { Text("Save page as PDF") }
                            OutlinedButton(onClick = {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(activeTab.url))) }
                            }, modifier = Modifier.fillMaxWidth()) { Text("Open in another app") }
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Clear browsing data", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("Remove saved history, website cookies and cached files from WebView.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(onClick = { showClearDataDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Delete, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Clear browsing data")
                            }
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("APP & UPDATES", color = Cyan, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("App updates", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(updateStatus, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(
                                onClick = {
                                    if (!checkingUpdate) updateScope.launch {
                                        checkingUpdate = true
                                        updateStatus = "Checking for updates..."
                                        try {
                                            val result = withContext(Dispatchers.IO) {
                                                val connection = (URL("https://api.github.com/repos/Sudhamoy-X/SUDHA-S-BROWSER-/releases/latest").openConnection() as HttpURLConnection)
                                                connection.connectTimeout = 12000
                                                connection.readTimeout = 12000
                                                connection.setRequestProperty("Accept", "application/vnd.github+json")
                                                try {
                                                    val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                                                    val assets = json.optJSONArray("assets") ?: JSONArray()
                                                    var apkUrl: String? = null
                                                    for (i in 0 until assets.length()) {
                                                        val asset = assets.getJSONObject(i)
                                                        if (asset.optString("name") == "sudha-browser.apk") apkUrl = asset.optString("browser_download_url")
                                                    }
                                                    val latestCode = json.optInt("target_commitish", 0)
                                                    val tagCode = json.optString("tag_name").substringAfterLast("-").toIntOrNull() ?: 0
                                                    Pair(tagCode, apkUrl)
                                                } finally { connection.disconnect() }
                                            }
                                            val installedCode = if (Build.VERSION.SDK_INT >= 28) context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt() else @Suppress("DEPRECATION") context.packageManager.getPackageInfo(context.packageName, 0).versionCode
                                            if (result.first <= installedCode || result.second.isNullOrBlank()) {
                                                updateStatus = "No newer update is available."
                                            } else {
                                                val request = DownloadManager.Request(Uri.parse(result.second))
                                                    .setTitle("SUDHA BROWSER update")
                                                    .setDescription("Downloading version ${result.first}")
                                                    .setMimeType("application/vnd.android.package-archive")
                                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "sudha-browser-update.apk")
                                                val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                                val id = manager.enqueue(request)
                                                updateStatus = "Update downloading. Android will ask you to confirm installation when ready."
                                                while (true) {
                                                    delay(1200)
                                                    val cursor = manager.query(DownloadManager.Query().setFilterById(id))
                                                    var statusCode = -1
                                                    var apkUri: Uri? = null
                                                    cursor?.use { if (it.moveToFirst()) { statusCode = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)); apkUri = manager.getUriForDownloadedFile(id) } }
                                                    if (statusCode == DownloadManager.STATUS_SUCCESSFUL && apkUri != null) {
                                                        updateStatus = "Download complete. Opening installer..."
                                                        val installIntent = Intent(Intent.ACTION_VIEW).setDataAndType(apkUri, "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        context.startActivity(installIntent)
                                                        break
                                                    } else if (statusCode == DownloadManager.STATUS_FAILED) {
                                                        updateStatus = "Update download failed. Please try again."
                                                        break
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            updateStatus = "Update check failed: ${e.localizedMessage ?: "network error"}"
                                        } finally { checkingUpdate = false }
                                    }
                                },
                                enabled = !checkingUpdate,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (checkingUpdate) "Checking / downloading..." else "Check for updates") }
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("Protection is based on a built-in domain list. Some ads and trackers may still load, and some websites may need these services to work. This is not a VPN or a complete network-wide blocker.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    "tabs" -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(tabs, key = { it.id }) { tab ->
                            Row(Modifier.fillMaxWidth().background(if (tab.id == activeTabId) Color(0xFF172B48) else SurfaceBlue, RoundedCornerShape(16.dp)).clickable { activeTabId = tab.id; address = tab.url; screen = "browser"; showHomeDashboard = tab.url.isBlank() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) { Text(tab.title, color = Color.White, maxLines = 1); Text(tab.url, color = Color.LightGray, maxLines = 1, style = MaterialTheme.typography.bodySmall) }
                                IconButton(onClick = { closeTab(tab.id) }) { Icon(Icons.Default.Close, "Close tab", tint = Color.LightGray) }
                            }
                        }
                    }
                    "bookmarks", "history" -> {
                        val list = if (screen == "bookmarks") bookmarks else history
                        if (list.isEmpty()) Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(if (screen == "bookmarks") "No bookmarks yet" else "No browsing history yet", color = Color.LightGray) }
                        else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(list.toList()) { item ->
                                Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(14.dp)).clickable { openSaved(item) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) { Text(item.title, color = Color.White, maxLines = 1); Text(item.url, color = Color.LightGray, maxLines = 1, style = MaterialTheme.typography.bodySmall) }
                                    if (screen == "bookmarks") IconButton(onClick = { bookmarks.remove(item); persist("bookmarks", bookmarks) }) { Icon(Icons.Default.Delete, "Delete bookmark", tint = Color.LightGray) }
                                }
                            }
                        }
                        if (screen == "history" && history.isNotEmpty()) TextButton(onClick = { history.clear(); persist("history", history) }, modifier = Modifier.align(Alignment.End).padding(end = 12.dp)) { Text("Clear history", color = Cyan) }
                    }
                    "downloads" -> {
                        if (downloads.isEmpty()) {
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Download, null, tint = Cyan, modifier = Modifier.size(36.dp))
                                    Text("No downloads yet", color = Color.LightGray)
                                    Text("Files downloaded in the browser appear here.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(downloads, key = { it.id }) { item ->
                                    Column(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Download, null, tint = Cyan)
                                            Spacer(Modifier.width(10.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(item.title, color = Color.White, maxLines = 2)
                                                Text(item.status, color = if (item.status == "Failed") Color(0xFFFF7777) else Cyan, style = MaterialTheme.typography.labelSmall)
                                            }
                                            IconButton(onClick = { downloadManager.remove(item.id); refreshDownloads() }) {
                                                Icon(Icons.Default.Delete, "Delete download", tint = Color.LightGray)
                                            }
                                        }
                                        if (item.status == "Downloading" || item.status == "Waiting" || item.status.startsWith("Paused")) {
                                            Spacer(Modifier.height(8.dp))
                                            val fraction = if (item.total > 0L) (item.downloaded.toFloat() / item.total.toFloat()).coerceIn(0f, 1f) else 0f
                                            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth(), color = ElectricBlue, trackColor = Midnight)
                                            Text(if (item.total > 0L) "${item.downloaded / 1024} KB / ${item.total / 1024} KB" else "${item.downloaded / 1024} KB downloaded", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                        } else if (item.status == "Completed") {
                                            Text("Ready to open", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                            TextButton(onClick = {
                                                val uri = item.uri ?: downloadManager.getUriForDownloadedFile(item.id)
                                                if (uri != null) {
                                                    val mime = downloadManager.getMimeTypeForDownloadedFile(item.id) ?: "*/*"
                                                    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    runCatching { context.startActivity(intent) }
                                                }
                                            }) { Text("Open file", color = Cyan) }
                                        } else if (item.status == "Failed") {
                                            Text("Error code: ${item.reason}", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showClearDataDialog) {
                AlertDialog(
                    onDismissRequest = { showClearDataDialog = false },
                    title = { Text("Clear browsing data?") },
                    text = { Text("This removes all saved browsing history, website cookies and cached WebView files. You may be signed out of websites. Bookmarks and downloaded files will remain.") },
                    confirmButton = {
                        TextButton(onClick = {
                            history.clear()
                            persist("history", history)
                            tabs.forEach { tab -> tab.webView?.apply { clearHistory(); clearCache(true) } }
                            CookieManager.getInstance().removeAllCookies {
                                CookieManager.getInstance().flush()
                            }
                            WebStorage.getInstance().deleteAllData()
                            showClearDataDialog = false
                        }) { Text("Clear data", color = Color(0xFFFF7777)) }
                    },
                    dismissButton = { TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") } }
                )
            }
            Row(Modifier.fillMaxWidth().navigationBarsPadding().background(Brush.verticalGradient(listOf(Color(0xFF101D35), Color(0xFF080F20)))).border(1.dp, Color(0xFF1D3B66)).padding(horizontal = 4.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                NavigationBarItem(selected = screen == "browser" && showHomeDashboard, onClick = { screen = "browser"; showHomeDashboard = true; address = "" }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = screen == "bookmarks", onClick = { screen = "bookmarks" }, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text("Saved") })
                NavigationBarItem(selected = screen == "history", onClick = { screen = "history" }, icon = { Icon(Icons.Default.History, null) }, label = { Text("History") })
                NavigationBarItem(selected = screen == "downloads", onClick = { screen = "downloads" }, icon = { Icon(Icons.Default.Download, null) }, label = { Text("Downloads") })
                NavigationBarItem(selected = screen == "tabs", onClick = { screen = "tabs" }, icon = { Icon(Icons.Default.Tab, null) }, label = { Text("Tabs") })
            }
        }
    }
}
