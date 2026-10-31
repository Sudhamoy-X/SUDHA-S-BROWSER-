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
import java.io.File
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.MoreVert
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

private data class BrowserTab(val id: Int, var url: String = "", var title: String = "New tab", var webView: WebView? = null, var group: String = "Ungrouped")
private data class SavedPage(val title: String, val url: String, val folder: String = "General")
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
        (0 until array.length()).map { i -> array.getJSONObject(i).let { SavedPage(it.optString("title"), it.optString("url"), it.optString("folder", "General")) } }
    }.getOrDefault(emptyList())
    val bookmarks = remember { mutableStateListOf<SavedPage>().also { it.addAll(readList("bookmarks")) } }
    val history = remember { mutableStateListOf<SavedPage>().also { it.addAll(readList("history")) } }
    val tabs = remember {
        mutableStateListOf<BrowserTab>().also { restored ->
            val saved = runCatching {
                val array = JSONArray(prefs.getString("open_tabs", "[]"))
                (0 until array.length()).mapNotNull { index ->
                    val item = array.optJSONObject(index) ?: return@mapNotNull null
                    val url = item.optString("url")
                    if (url.isBlank()) BrowserTab(item.optInt("id", index + 1))
                    else BrowserTab(item.optInt("id", index + 1), url, item.optString("title", url), group = item.optString("group", "Ungrouped"))
                }
            }.getOrDefault(emptyList())
            restored.addAll(saved.ifEmpty { listOf(BrowserTab(1)) })
        }
    }
    val recentlyClosedTabs = remember { mutableStateListOf<SavedPage>().also { it.addAll(readList("recently_closed_tabs").take(10)) } }
    var tabSearch by remember { mutableStateOf("") }
    var selectedTabGroup by remember { mutableStateOf("All") }
    var groupMenuTabId by remember { mutableStateOf<Int?>(null) }
    var savedSearch by remember { mutableStateOf("") }
    var sortBookmarksAZ by remember { mutableStateOf(false) }
    var selectedBookmarkFolder by remember { mutableStateOf("All") }
    val customBookmarkFolders = remember { mutableStateListOf<String>().also { folders -> folders.addAll(prefs.getStringSet("bookmark_folders", emptySet())?.toList()?.sorted() ?: emptyList()) } }
    var showCreateBookmarkFolderDialog by remember { mutableStateOf(false) }
    var newBookmarkFolderName by remember { mutableStateOf("") }
    var showManageBookmarkFoldersDialog by remember { mutableStateOf(false) }
    var renameBookmarkFolderFrom by remember { mutableStateOf<String?>(null) }
    var renameBookmarkFolderTo by remember { mutableStateOf("") }
    var bookmarkFolderMenuUrl by remember { mutableStateOf<String?>(null) }
    var showBookmarkEditDialog by remember { mutableStateOf(false) }
    var bookmarkEditUrl by remember { mutableStateOf("") }
    var bookmarkEditTitle by remember { mutableStateOf("") }
    var showImportBookmarksDialog by remember { mutableStateOf(false) }
    var showPageTools by remember { mutableStateOf(false) }
    var showPageInfoDialog by remember { mutableStateOf(false) }
    var pageInfoText by remember { mutableStateOf("") }
    var importBookmarksText by remember { mutableStateOf("") }
    var nextTabId by remember { mutableIntStateOf((tabs.maxOfOrNull { it.id } ?: 0) + 1) }
    var activeTabId by remember { mutableIntStateOf(prefs.getInt("active_tab_id", tabs.first().id).takeIf { id -> tabs.any { it.id == id } } ?: tabs.first().id) }
    var address by remember { mutableStateOf(tabs.firstOrNull { it.id == activeTabId }?.url ?: "") }
    var progress by remember { mutableIntStateOf(0) }
    var screen by remember { mutableStateOf("browser") }
    var showHomeDashboard by remember { mutableStateOf(tabs.firstOrNull { it.id == activeTabId }?.url.isNullOrBlank()) }
    var adsEnabled by remember { mutableStateOf(prefs.getBoolean("block_ads", true)) }
    var trackingEnabled by remember { mutableStateOf(prefs.getBoolean("block_trackers", true)) }
    var desktopMode by remember { mutableStateOf(prefs.getBoolean("desktop_mode", false)) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var searchEngine by remember { mutableStateOf(prefs.getString("search_engine", "Google") ?: "Google") }
    var showSearchEngines by remember { mutableStateOf(false) }
    var textZoom by remember { mutableIntStateOf(prefs.getInt("text_zoom", 100).coerceIn(75, 125)) }
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
    var downloadSearch by remember { mutableStateOf("") }
    var downloadFilter by remember { mutableStateOf("All") }
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

    fun persistSession() {
        val array = JSONArray()
        tabs.forEach { tab ->
            array.put(JSONObject().put("id", tab.id).put("title", tab.title).put("url", tab.url).put("group", tab.group))
        }
        prefs.edit().putString("open_tabs", array.toString()).putInt("active_tab_id", activeTabId).apply()
    }

    fun persist(key: String, list: List<SavedPage>) {
        val array = JSONArray()
        list.forEach { array.put(JSONObject().put("title", it.title).put("url", it.url).put("folder", it.folder)) }
        prefs.edit().putString(key, array.toString()).apply()
    }
    fun persistRecentlyClosedTabs() = persist("recently_closed_tabs", recentlyClosedTabs)
    fun navigate(raw: String) {
        val value = raw.trim()
        if (value.isBlank()) return
        val target = when {
            value.startsWith("https://") || value.startsWith("http://") -> value
            value.contains(" ") || !value.contains(".") -> (when (searchEngine) { "Bing" -> "https://www.bing.com/search?q="; "DuckDuckGo" -> "https://duckduckgo.com/?q="; "Brave" -> "https://search.brave.com/search?q="; "Startpage" -> "https://www.startpage.com/sp/search?query="; "Ecosia" -> "https://www.ecosia.org/search?q="; "Yahoo" -> "https://search.yahoo.com/search?p="; else -> "https://www.google.com/search?q=" }) + Uri.encode(value)
            else -> "https://$value"
        }
        activeTab.url = target; activeTab.title = target; address = target; screen = "browser"; showHomeDashboard = false
        activeTab.webView?.loadUrl(target)
        persistSession()
    }
    fun addTab() { val tab = BrowserTab(nextTabId++); tabs.add(tab); activeTabId = tab.id; address = ""; screen = "browser"; showHomeDashboard = true; persistSession() }
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
        persistRecentlyClosedTabs()
        persistSession()
    }
    fun duplicateActiveTab() { val source = activeTab; val copy = BrowserTab(nextTabId++, source.url, source.title); tabs.add(copy); activeTabId = copy.id; address = copy.url; screen = "browser"; showHomeDashboard = copy.url.isBlank(); persistSession() }
    fun reopenLastClosedTab() { val last = recentlyClosedTabs.firstOrNull() ?: return; recentlyClosedTabs.removeAt(0); persistRecentlyClosedTabs(); val tab = BrowserTab(nextTabId++, last.url, last.title); tabs.add(tab); activeTabId = tab.id; address = tab.url; screen = "browser"; showHomeDashboard = false; persistSession() }
    fun closeOtherTabs() { val keep = activeTabId; tabs.filter { it.id != keep }.forEach { tab -> if (tab.url.isNotBlank()) recentlyClosedTabs.add(0, SavedPage(tab.title, tab.url)); tab.webView?.apply { stopLoading(); destroy() } }; while (recentlyClosedTabs.size > 10) recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex); persistRecentlyClosedTabs(); tabs.removeAll { it.id != keep }; persistSession() }
    fun closeAllTabs() { tabs.forEach { tab -> if (tab.url.isNotBlank()) recentlyClosedTabs.add(0, SavedPage(tab.title, tab.url)); tab.webView?.apply { stopLoading(); destroy() } }; while (recentlyClosedTabs.size > 10) recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex); persistRecentlyClosedTabs(); tabs.clear(); val fresh = BrowserTab(nextTabId++); tabs.add(fresh); activeTabId = fresh.id; address = ""; screen = "browser"; showHomeDashboard = true; persistSession() }

    fun saveBookmark() {
        if (activeTab.url.isBlank()) return
        val item = SavedPage(activeTab.title, activeTab.url, "General")
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
                    IconButton(onClick = { screen = "tabs" }) { BadgedBox(badge = { Badge { Text(tabs.size.toString()) } }) { Icon(Icons.Default.Tab, "Tabs", tint = Color.White) } }
                }
            }
            if (screen == "browser") {
                if (!showHomeDashboard) Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp).height(44.dp).background(SurfaceBlue, CircleShape).padding(start = 4.dp, end = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (progress in 1..99) activeTab.webView?.stopLoading() else activeTab.webView?.reload() }, modifier = Modifier.size(36.dp)) {
                        Icon(if (progress in 1..99) Icons.Default.Close else Icons.Default.Refresh, if (progress in 1..99) "Stop loading" else "Refresh page", tint = Cyan, modifier = Modifier.size(20.dp))
                    }
                    TextField(value = address, onValueChange = { address = it }, modifier = Modifier.weight(1f).height(44.dp), textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp), placeholder = { Text("Search or enter address", color = Color.LightGray, fontSize = 13.sp) }, singleLine = true,
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Cyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go), keyboardActions = KeyboardActions(onGo = { navigate(address) }))
                    IconButton(onClick = { navigate(address) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.ArrowForward, "Go", tint = Cyan, modifier = Modifier.size(20.dp)) }
                }
                if (showHomeDashboard) {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 18.dp), horizontalAlignment = Alignment.Start) {
                        Spacer(Modifier.height(22.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            SudhaBrandMark(Modifier.size(54.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("SUDHA", color = Color.White, style = MaterialTheme.typography.headlineMedium, letterSpacing = 5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Text("BROWSER", color = Cyan, style = MaterialTheme.typography.labelSmall, letterSpacing = 4.sp)
                            }
                            Box(Modifier.background(Color(0xFF102B2D), RoundedCornerShape(50)).border(1.dp, Color(0xFF24605A), RoundedCornerShape(50)).padding(horizontal = 11.dp, vertical = 7.dp)) {
                                Text("READY TO BROWSE", color = Color(0xFF78E0C3), style = MaterialTheme.typography.labelSmall, letterSpacing = 0.6.sp)
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        Text("Explore without limits.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text("Search the web or enter a website address", color = Color(0xFF91A7C5), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF1B3457), Color(0xFF101C32))), RoundedCornerShape(20.dp)).border(1.dp, Color(0xFF37649A), RoundedCornerShape(20.dp)).padding(start = 15.dp, end = 7.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null, tint = Cyan, modifier = Modifier.size(23.dp))
                            Spacer(Modifier.width(10.dp))
                            TextField(value = address, onValueChange = { address = it }, modifier = Modifier.weight(1f), placeholder = { Text("Search Google or type URL", color = Color(0xFF9BAFCB), fontSize = 14.sp) }, singleLine = true, colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Cyan), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go), keyboardActions = KeyboardActions(onGo = { navigate(address) }))
                            IconButton(onClick = { navigate(address) }, modifier = Modifier.background(Cyan.copy(alpha = 0.14f), CircleShape)) { Icon(Icons.Default.ArrowForward, "Search", tint = Cyan) }
                        }
                        Spacer(Modifier.height(27.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("YOUR SHORTCUTS", color = Color.White, style = MaterialTheme.typography.titleSmall, letterSpacing = 1.4.sp); Text("Your everyday destinations", color = Color(0xFF8299B8), style = MaterialTheme.typography.labelSmall) }
                            Text("EDIT IN SETTINGS", color = Color(0xFF6E9DD6), style = MaterialTheme.typography.labelSmall)
                        }
                        if (showShortcuts) {
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf("Google" to "https://www.google.com", "YouTube" to "https://m.youtube.com", "Gmail" to "https://mail.google.com", "Maps" to "https://maps.google.com").forEachIndexed { index, item ->
                                    val tint = listOf(Color(0xFF55B7FF), Color(0xFFFF5577), Color(0xFFFF657D), Color(0xFF55D6B0))[index]
                                    Column(Modifier.weight(1f).background(Brush.verticalGradient(listOf(Color(0xFF1A2B45), Color(0xFF101A2B))), RoundedCornerShape(18.dp)).border(1.dp, Color(0xFF2A4263), RoundedCornerShape(18.dp)).clickable { navigate(item.second) }.padding(vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(Modifier.size(39.dp).background(tint.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) { Text(item.first.take(1), color = tint, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                                        Spacer(Modifier.height(8.dp)); Text(item.first, color = Color(0xFFE0ECFC), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("QUICK ACCESS", color = Color.White, style = MaterialTheme.typography.titleSmall, letterSpacing = 1.4.sp); Text("Your browser, organised", color = Color(0xFF8299B8), style = MaterialTheme.typography.labelSmall) }
                            Text("CRAFTED BY SUDHAMOY", color = Color(0xFF6E9DD6), style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(Triple("Bookmarks", "bookmarks", Color(0xFF4CBFFF)), Triple("History", "history", Color(0xFFB17BFF)), Triple("Downloads", "downloads", Color(0xFF52D6B0)), Triple("Settings", "settings", Color(0xFFFFB86B))).forEach { item ->
                                Column(Modifier.weight(1f).background(Color(0xFF111D30), RoundedCornerShape(17.dp)).border(1.dp, Color(0xFF263D5C), RoundedCornerShape(17.dp)).clickable { screen = item.second }.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.size(35.dp).background(item.third.copy(alpha = 0.13f), CircleShape), contentAlignment = Alignment.Center) { Text(item.first.take(1), color = item.third, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                                    Spacer(Modifier.height(7.dp)); Text(item.first, color = Color(0xFFD7E8FF), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }
                        }
                        Spacer(Modifier.height(22.dp))
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
                            // Respect mobile viewport metadata so responsive sites use the phone width.
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL
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
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) { if (url != null) { activeTab.url = url; if (activeTab.id == activeTabId) address = url; persistSession() } }
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    if (readerMode) view?.evaluateJavascript("(function(){var s=document.getElementById('sudha-reader-style');if(!s){s=document.createElement('style');s.id='sudha-reader-style';document.head.appendChild(s);}s.textContent='body{max-width:780px!important;margin:24px auto!important;padding:24px!important;font-family:Georgia,serif!important;font-size:20px!important;line-height:1.8!important;background:#f5f1e8!important;color:#242424!important}';})();", null)
                                    if (url != null) {
                                        activeTab.url = url; activeTab.title = view?.title?.takeIf { it.isNotBlank() } ?: url; if (activeTab.id == activeTabId) address = url
                                        val item = SavedPage(activeTab.title, url)
                                        history.removeAll { it.url == url }; history.add(0, item)
                                        while (history.size > 500) history.removeAt(history.lastIndex)
                                        persist("history", history)
                                        persistSession()
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
                if (!showHomeDashboard) {
                    Row(
                        Modifier.fillMaxWidth()
                            .navigationBarsPadding()
                            .height(54.dp)
                            .background(Brush.verticalGradient(listOf(Color(0xFF121D30), Color(0xFF080F1D))))
                            .border(width = 0.5.dp, color = Color(0xFF26364D))
                            .padding(horizontal = 5.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { activeTab.webView?.goBack() }, enabled = activeTab.webView?.canGoBack() == true) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = if (activeTab.webView?.canGoBack() == true) Color.White else Color(0xFF657184), modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = { activeTab.webView?.goForward() }, enabled = activeTab.webView?.canGoForward() == true) {
                            Icon(Icons.Default.ArrowForward, "Forward", tint = if (activeTab.webView?.canGoForward() == true) Color.White else Color(0xFF657184), modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = { showHomeDashboard = true; address = "" }, modifier = Modifier.size(40.dp).background(Color(0xFF17364A), CircleShape)) {
                            Icon(Icons.Default.Home, "Home", tint = Cyan, modifier = Modifier.size(21.dp))
                        }
                        IconButton(onClick = { screen = "tabs" }) {
                            BadgedBox(badge = { Badge { Text(tabs.size.toString()) } }) {
                                Icon(Icons.Default.Tab, "Tabs", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Box {
                            IconButton(onClick = { showPageTools = true }) {
                                Icon(Icons.Default.MoreVert, "More options", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            DropdownMenu(expanded = showPageTools, onDismissRequest = { showPageTools = false }) {
                                DropdownMenuItem(text = { Text("Bookmark page") }, onClick = { showPageTools = false; saveBookmark() })
                                DropdownMenuItem(text = { Text("Find in page") }, onClick = { showPageTools = false; showFindBar = true; findText = "" })
                                DropdownMenuItem(text = { Text("Share page") }, onClick = {
                                    showPageTools = false
                                    if (activeTab.url.isNotBlank()) runCatching { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, activeTab.url), "Share page")) }
                                })
                                DropdownMenuItem(text = { Text("Copy page link") }, onClick = {
                                    showPageTools = false
                                    if (activeTab.url.isNotBlank()) (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Page URL", activeTab.url))
                                })
                                DropdownMenuItem(text = { Text("New tab") }, onClick = { showPageTools = false; addTab() })
                                DropdownMenuItem(text = { Text("Translate page to English") }, onClick = {
                                    showPageTools = false
                                    if (activeTab.url.startsWith("http")) navigate("https://translate.google.com/translate?sl=auto&tl=en&u=" + Uri.encode(activeTab.url))
                                })
                            }
                        }
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
                        Text("GENERAL", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Data Saver", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Blocks page images to reduce mobile data use", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = dataSaverEnabled, onCheckedChange = { enabled -> dataSaverEnabled = enabled; prefs.edit().putBoolean("data_saver", enabled).apply(); tabs.forEach { tab -> tab.webView?.settings?.apply { blockNetworkImage = enabled; loadsImagesAutomatically = imagesEnabled && !enabled } } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Swipe navigation", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Swipe right to go back, left to go forward", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = gestureNavigationEnabled, onCheckedChange = { gestureNavigationEnabled = it; prefs.edit().putBoolean("gesture_navigation", it).apply() })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("GESTURES & TOOLS", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Volume gesture", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Vertical swipe on the right edge adjusts media volume", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = volumeGestureEnabled, onCheckedChange = { volumeGestureEnabled = it; prefs.edit().putBoolean("volume_gesture", it).apply() })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Element blocker", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Tap unwanted items on a page to hide them", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = elementBlockerEnabled, onCheckedChange = { enabled ->
                                elementBlockerEnabled = enabled
                                prefs.edit().putBoolean("element_blocker", enabled).apply()
                                activeTab.webView?.evaluateJavascript(if (enabled) "(function(){if(window.__sudhaElementBlocker)return;window.__sudhaElementBlocker=true;window.__sudhaPickElement=true;document.addEventListener('click',function(e){if(!window.__sudhaPickElement)return;var el=e.target;if(!el||el===document.body)return;e.preventDefault();e.stopPropagation();el.style.setProperty('display','none','important');},true);})();" else "window.__sudhaPickElement=false;", null)
                            })
                        }
                        Text("Hidden items return after a full page reload. This is a basic page-level blocker.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("DNS SETTINGS", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("DNS provider preference", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("WebView uses Android system DNS. This preference does not create an app-level DoH tunnel.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(onClick = { dnsProvider = when (dnsProvider) { "System default" -> "Cloudflare (1.1.1.1)"; "Cloudflare (1.1.1.1)" -> "Google (dns.google)"; else -> "System default" }; prefs.edit().putString("dns_provider", dnsProvider).apply() }, modifier = Modifier.fillMaxWidth()) { Text("Selected: $dnsProvider") }
                            OutlinedButton(onClick = { runCatching { context.startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }, modifier = Modifier.fillMaxWidth()) { Text("Open Android network / Private DNS settings") }
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("WEBSITE & PRIVACY", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("JavaScript", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Some websites require JavaScript to work", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = javaScriptEnabled, onCheckedChange = { javaScriptEnabled = it; prefs.edit().putBoolean("javascript_enabled", it).apply(); tabs.forEach { tab -> tab.webView?.settings?.javaScriptEnabled = it } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Load images", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Turn off to reduce image data usage", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = imagesEnabled, onCheckedChange = { imagesEnabled = it; prefs.edit().putBoolean("images_enabled", it).apply(); tabs.forEach { tab -> tab.webView?.settings?.loadsImagesAutomatically = it && !dataSaverEnabled } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Website cookies", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Disabling cookies may sign you out of websites", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = cookiesEnabled, onCheckedChange = { cookiesEnabled = it; prefs.edit().putBoolean("cookies_enabled", it).apply(); CookieManager.getInstance().setAcceptCookie(it); tabs.forEach { tab -> tab.webView?.let { web -> CookieManager.getInstance().setAcceptThirdPartyCookies(web, false) } } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Safe Browsing", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Use Android WebView's known harmful-site protection", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = safeBrowsingEnabled, onCheckedChange = { safeBrowsingEnabled = it; prefs.edit().putBoolean("safe_browsing", it).apply(); tabs.forEach { tab -> tab.webView?.settings?.safeBrowsingEnabled = it } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Block pop-ups", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Prevent websites from opening unexpected windows", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = popupsBlocked, onCheckedChange = { enabled -> popupsBlocked = enabled; prefs.edit().putBoolean("block_popups", enabled).apply(); tabs.forEach { tab -> tab.webView?.settings?.apply { javaScriptCanOpenWindowsAutomatically = !enabled; setSupportMultipleWindows(!enabled) } } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Third-party cookies", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Allow cross-site cookies when a website requires them", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = thirdPartyCookies, onCheckedChange = { enabled -> thirdPartyCookies = enabled; prefs.edit().putBoolean("third_party_cookies", enabled).apply(); tabs.forEach { tab -> tab.webView?.let { CookieManager.getInstance().setAcceptThirdPartyCookies(it, enabled) } } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("HOME & SEARCH", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Homepage shortcuts", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("Show quick links below the address bar", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = showShortcuts, onCheckedChange = { showShortcuts = it; prefs.edit().putBoolean("show_shortcuts", it).apply() })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
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
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
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
                        Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                    listOf("Google", "Bing", "DuckDuckGo", "Brave", "Startpage", "Ecosia", "Yahoo").forEach { engine -> DropdownMenuItem(text = { Text(engine) }, onClick = { searchEngine = engine; prefs.edit().putString("search_engine", engine).apply(); showSearchEngines = false }) }
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Website text size", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("$textZoom%", color = Cyan, style = MaterialTheme.typography.labelLarge)
                            Slider(value = textZoom.toFloat(), onValueChange = { textZoom = it.toInt().coerceIn(75, 125) }, valueRange = 75f..125f, onValueChangeFinished = { prefs.edit().putInt("text_zoom", textZoom).apply(); tabs.forEach { it.webView?.settings?.textZoom = textZoom } })
                            Text("Adjust webpage text without changing image or layout zoom.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Text("PAGE TOOLS", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
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
                        Text("APP & UPDATES", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
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
                    "tabs" -> Column(Modifier.weight(1f).fillMaxWidth()) {
                        val visibleTabs = tabs.filter { (selectedTabGroup == "All" || it.group == selectedTabGroup) && (tabSearch.isBlank() || it.title.contains(tabSearch, true) || it.url.contains(tabSearch, true)) }
                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = tabSearch,
                                onValueChange = { tabSearch = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                placeholder = { Text("Search your open tabs") },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = Cyan) }
                            )
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                listOf("All", "Personal", "Work", "Shopping", "Ungrouped").forEach { group ->
                                    val count = if (group == "All") tabs.size else tabs.count { it.group == group }
                                    FilterChip(selected = selectedTabGroup == group, onClick = { selectedTabGroup = group }, label = { Text("$group  $count", maxLines = 1) }, shape = RoundedCornerShape(14.dp))
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { reopenLastClosedTab() }, enabled = recentlyClosedTabs.isNotEmpty(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Reopen closed") }
                                OutlinedButton(onClick = { duplicateActiveTab() }, enabled = activeTab.url.isNotBlank(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Duplicate") }
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("${visibleTabs.size} tabs shown", color = Color.LightGray, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                                TextButton(onClick = { closeOtherTabs() }, enabled = tabs.size > 1) { Text("Close others", color = Cyan) }
                                TextButton(onClick = { closeAllTabs() }, enabled = tabs.isNotEmpty()) { Text("Close all", color = Color(0xFFFF8585)) }
                            }
                            if (recentlyClosedTabs.isNotEmpty()) {
                                Row(Modifier.fillMaxWidth().background(Color(0xFF101E33), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, null, tint = Cyan, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Recently closed: ${recentlyClosedTabs.first().title}", color = Color.LightGray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                            }
                        }
                        if (visibleTabs.isEmpty()) {
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Tab, null, tint = Cyan, modifier = Modifier.size(42.dp))
                                    Text(if (tabs.isEmpty()) "No open tabs" else "No matching tabs", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                    Text("Open a new tab to continue browsing.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                    OutlinedButton(onClick = { addTab() }, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("New tab") }
                                }
                            }
                        } else {
                            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(visibleTabs, key = { it.id }) { tab ->
                                    val selected = tab.id == activeTabId
                                    val cardColor by animateColorAsState(targetValue = if (selected) Color(0xFF172F50) else SurfaceBlue, animationSpec = tween(240), label = "tab-card-color")
                                    Column(
                                        Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(260))
                                            .border(if (selected) 1.5.dp else 1.dp, if (selected) ElectricBlue.copy(alpha = 0.9f) else Color(0xFF26364D), RoundedCornerShape(20.dp))
                                            .background(cardColor, RoundedCornerShape(20.dp))
                                            .clickable { activeTabId = tab.id; address = tab.url; screen = "browser"; showHomeDashboard = tab.url.isBlank(); persistSession() }
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(34.dp).background(Color(0xFF203B60), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                                                Text(if (tab.url.isBlank()) "＋" else (Uri.parse(tab.url).host?.firstOrNull()?.uppercase() ?: "W"), color = Cyan, style = MaterialTheme.typography.titleMedium)
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(tab.title.ifBlank { "New tab" }, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                                Text(if (tab.url.isBlank()) "Ready for a new page" else (Uri.parse(tab.url).host ?: tab.url), color = Color.LightGray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                            }
                                            if (selected) Surface(color = Color(0xFF173F70), shape = RoundedCornerShape(20.dp)) {
                                                Text("ACTIVE", color = Cyan, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                                            }
                                            IconButton(onClick = { closeTab(tab.id) }) { Icon(Icons.Default.Close, "Close tab", tint = Color.LightGray) }
                                        }
                                        Text(tab.url.ifBlank { "Blank tab" }, color = Color(0xFF9BAEC8), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(color = Color(0xFF142A45), shape = RoundedCornerShape(10.dp)) {
                                                Text(tab.group, color = Cyan, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                            }
                                            Spacer(Modifier.weight(1f))
                                            Box {
                                                TextButton(onClick = { groupMenuTabId = tab.id }) { Text("Move to group", color = Color.LightGray); Icon(Icons.Default.MoreVert, null, tint = Cyan) }
                                                DropdownMenu(expanded = groupMenuTabId == tab.id, onDismissRequest = { groupMenuTabId = null }) {
                                                    listOf("Personal", "Work", "Shopping", "Ungrouped").forEach { group ->
                                                        DropdownMenuItem(text = { Text(if (tab.group == group) "✓ $group" else group) }, onClick = {
                                                            tab.group = group
                                                            groupMenuTabId = null
                                                            persistSession()
                                                        })
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    "bookmarks", "history" -> {
                        val baseList = if (screen == "bookmarks") bookmarks.toList() else history.toList()
                        val filteredList = baseList.filter { (screen != "bookmarks" || selectedBookmarkFolder == "All" || it.folder == selectedBookmarkFolder) && (savedSearch.isBlank() || it.title.contains(savedSearch, true) || it.url.contains(savedSearch, true)) }
                        val list = if (screen == "bookmarks" && sortBookmarksAZ) filteredList.sortedBy { it.title.lowercase() } else filteredList
                        Column(Modifier.weight(1f).fillMaxWidth()) {
                        OutlinedTextField(value = savedSearch, onValueChange = { savedSearch = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp), singleLine = true, placeholder = { Text("Search saved pages") }, leadingIcon = { Icon(Icons.Default.Search, null) })
                        if (screen == "bookmarks") {
                            Text("${bookmarks.size} saved bookmarks", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp))
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (listOf("All", "General") + (bookmarks.map { it.folder } + customBookmarkFolders).distinct().filter { it != "General" }.sorted()).forEach { folder ->
                                    FilterChip(selected = selectedBookmarkFolder == folder, onClick = { selectedBookmarkFolder = folder }, label = { Text(folder, maxLines = 1) })
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TextButton(onClick = { newBookmarkFolderName = ""; showCreateBookmarkFolderDialog = true }) { Text("New folder +", color = Cyan) }
                                TextButton(onClick = { showManageBookmarkFoldersDialog = true }) { Text("Manage folders", color = Cyan) }
                                TextButton(onClick = { sortBookmarksAZ = !sortBookmarksAZ }) { Text(if (sortBookmarksAZ) "Sort: A–Z" else "Sort: Recent", color = Cyan) }
                                TextButton(onClick = { importBookmarksText = ""; showImportBookmarksDialog = true }) { Text("Import", color = Cyan) }
                                TextButton(onClick = {
                                    val exportText = bookmarks.joinToString(separator = "\n") { it.title + " | " + it.url }
                                    if (exportText.isNotBlank()) runCatching { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, exportText), "Export bookmarks")) }
                                }, enabled = bookmarks.isNotEmpty()) { Text("Export", color = Cyan) }
                            }
                            TextButton(onClick = {
                                val unique = bookmarks.distinctBy { it.url }
                                bookmarks.clear(); bookmarks.addAll(unique); persist("bookmarks", bookmarks)
                            }, enabled = bookmarks.size > 1, modifier = Modifier.align(Alignment.End)) { Text("Remove duplicates", color = Cyan) }
                        }
                        if (list.isEmpty()) Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(if (screen == "bookmarks") "No bookmarks yet" else "No browsing history yet", color = Color.LightGray) }
                        else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(list.toList()) { item ->
                                Row(Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(14.dp)).clickable { openSaved(item) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) { Text(item.title, color = Color.White, maxLines = 1); Text(item.url, color = Color.LightGray, maxLines = 1, style = MaterialTheme.typography.bodySmall); if (screen == "bookmarks") Text(item.folder, color = Cyan, style = MaterialTheme.typography.labelSmall) }
                                    if (screen == "bookmarks") {
                                        Box {
                                            IconButton(onClick = { bookmarkFolderMenuUrl = item.url }) { Icon(Icons.Default.MoreVert, "Bookmark folder", tint = Cyan) }
                                            DropdownMenu(expanded = bookmarkFolderMenuUrl == item.url, onDismissRequest = { bookmarkFolderMenuUrl = null }) {
                                                (listOf("General", "Personal", "Work", "Study", "Shopping") + customBookmarkFolders).distinct().forEach { folder ->
                                                    DropdownMenuItem(text = { Text(if (item.folder == folder) "✓ $folder" else folder) }, onClick = {
                                                        val index = bookmarks.indexOfFirst { it.url == item.url }
                                                        if (index >= 0) bookmarks[index] = bookmarks[index].copy(folder = folder)
                                                        persist("bookmarks", bookmarks)
                                                        bookmarkFolderMenuUrl = null
                                                    })
                                                }
                                            }
                                        }
                                        IconButton(onClick = { bookmarkEditUrl = item.url; bookmarkEditTitle = item.title; showBookmarkEditDialog = true }) { Icon(Icons.Default.Settings, "Edit bookmark", tint = Cyan) }
                                        IconButton(onClick = { runCatching { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, item.url), "Share bookmark")) } }) { Icon(Icons.Default.Share, "Share bookmark", tint = Color.LightGray) }
                                        IconButton(onClick = { bookmarks.remove(item); persist("bookmarks", bookmarks) }) { Icon(Icons.Default.Delete, "Delete bookmark", tint = Color.LightGray) }
                                    } else {
                                        IconButton(onClick = { history.remove(item); persist("history", history) }) { Icon(Icons.Default.Delete, "Delete history item", tint = Color.LightGray) }
                                    }
                                }
                            }
                        }
                        if (screen == "history" && history.isNotEmpty()) TextButton(onClick = { history.clear(); persist("history", history); savedSearch = "" }, modifier = Modifier.align(Alignment.End).padding(end = 12.dp)) { Text("Clear history", color = Cyan) }
                        }
                    }
                    "downloads" -> {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = downloadSearch,
                                onValueChange = { downloadSearch = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Search downloads") },
                                leadingIcon = { Icon(Icons.Default.Search, null) }
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                listOf("All", "Active", "Completed", "Failed").forEach { filter ->
                                    FilterChip(
                                        selected = downloadFilter == filter,
                                        onClick = { downloadFilter = filter },
                                        label = { Text(filter) }
                                    )
                                }
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("${downloads.size} files", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = {
                                        downloads.filter { it.status == "Completed" }.forEach { downloadManager.remove(it.id) }
                                        refreshDownloads()
                                    },
                                    enabled = downloads.any { it.status == "Completed" }
                                ) { Text("Clear completed", color = Cyan) }
                                IconButton(onClick = { refreshDownloads() }) { Icon(Icons.Default.Refresh, "Refresh downloads", tint = Cyan) }
                            }
                        }
                        val visibleDownloads = downloads.filter { item ->
                            val matchesFilter = when (downloadFilter) {
                                "Active" -> item.status == "Downloading" || item.status == "Waiting" || item.status.startsWith("Paused")
                                "Completed" -> item.status == "Completed"
                                "Failed" -> item.status == "Failed"
                                else -> true
                            }
                            matchesFilter && (downloadSearch.isBlank() || item.title.contains(downloadSearch, true))
                        }
                        if (visibleDownloads.isEmpty()) {
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Download, null, tint = Cyan, modifier = Modifier.size(36.dp))
                                    Text(if (downloads.isEmpty()) "No downloads yet" else "No matching downloads", color = Color.LightGray)
                                    Text("Files downloaded in the browser appear here.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(visibleDownloads, key = { it.id }) { item ->
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
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                TextButton(onClick = {
                                                    val uri = item.uri ?: downloadManager.getUriForDownloadedFile(item.id)
                                                    if (uri != null) {
                                                        val mime = downloadManager.getMimeTypeForDownloadedFile(item.id) ?: "*/*"
                                                        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        runCatching { context.startActivity(intent) }
                                                    }
                                                }) { Text("Open file", color = Cyan) }
                                                TextButton(onClick = {
                                                    val uri = item.uri ?: downloadManager.getUriForDownloadedFile(item.id)
                                                    if (uri != null) {
                                                        val mime = downloadManager.getMimeTypeForDownloadedFile(item.id) ?: "*/*"
                                                        val intent = Intent(Intent.ACTION_SEND).setType(mime)
                                                            .putExtra(Intent.EXTRA_STREAM, uri)
                                                            .putExtra(Intent.EXTRA_SUBJECT, item.title)
                                                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        runCatching {
                                                            context.startActivity(Intent.createChooser(intent, "Share downloaded file"))
                                                        }
                                                    }
                                                }) { Text("Share", color = Cyan) }
                                            }
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
            if (showPageInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showPageInfoDialog = false },
                    title = { Text("Page information") },
                    text = { Text(pageInfoText) },
                    confirmButton = { TextButton(onClick = { showPageInfoDialog = false }) { Text("Close") } }
                )
            }
            if (showBookmarkEditDialog) {
                AlertDialog(
                    onDismissRequest = { showBookmarkEditDialog = false },
                    title = { Text("Edit bookmark") },
                    text = { OutlinedTextField(value = bookmarkEditTitle, onValueChange = { bookmarkEditTitle = it }, singleLine = true, label = { Text("Bookmark title") }) },
                    confirmButton = {
                        TextButton(onClick = {
                            val index = bookmarks.indexOfFirst { it.url == bookmarkEditUrl }
                            if (index >= 0 && bookmarkEditTitle.isNotBlank()) {
                                bookmarks[index] = bookmarks[index].copy(title = bookmarkEditTitle.trim())
                                persist("bookmarks", bookmarks)
                            }
                            showBookmarkEditDialog = false
                        }) { Text("Save") }
                    },
                    dismissButton = { TextButton(onClick = { showBookmarkEditDialog = false }) { Text("Cancel") } }
                )
            }
            if (showManageBookmarkFoldersDialog) {
                AlertDialog(
                    onDismissRequest = { showManageBookmarkFoldersDialog = false },
                    title = { Text("Manage bookmark folders") },
                    text = {
                        if (customBookmarkFolders.isEmpty()) Text("No custom folders yet. Create one to manage it here.")
                        else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            customBookmarkFolders.toList().forEach { folder ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(folder, color = Color.White)
                                        Text("${bookmarks.count { it.folder == folder }} bookmarks", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                    }
                                    TextButton(onClick = { renameBookmarkFolderFrom = folder; renameBookmarkFolderTo = folder; showManageBookmarkFoldersDialog = false }) { Text("Rename") }
                                    TextButton(onClick = {
                                        bookmarks.indices.forEach { index -> if (bookmarks[index].folder == folder) bookmarks[index] = bookmarks[index].copy(folder = "General") }
                                        persist("bookmarks", bookmarks)
                                        customBookmarkFolders.remove(folder)
                                        prefs.edit().putStringSet("bookmark_folders", customBookmarkFolders.toSet()).apply()
                                        if (selectedBookmarkFolder == folder) selectedBookmarkFolder = "All"
                                    }) { Text("Delete", color = Color(0xFFFF7777)) }
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { showManageBookmarkFoldersDialog = false }) { Text("Done") } }
                )
            }
            if (renameBookmarkFolderFrom != null) {
                AlertDialog(
                    onDismissRequest = { renameBookmarkFolderFrom = null },
                    title = { Text("Rename folder") },
                    text = { OutlinedTextField(value = renameBookmarkFolderTo, onValueChange = { renameBookmarkFolderTo = it }, singleLine = true, label = { Text("New folder name") }) },
                    confirmButton = {
                        TextButton(onClick = {
                            val oldName = renameBookmarkFolderFrom
                            val newName = renameBookmarkFolderTo.trim().take(30)
                            if (oldName != null && newName.isNotBlank() && newName.lowercase() != "all" && newName.lowercase() != "general" && (newName.equals(oldName, true) || (customBookmarkFolders.none { it.equals(newName, true) } && bookmarks.none { it.folder.equals(newName, true) }))) {
                                bookmarks.indices.forEach { index -> if (bookmarks[index].folder == oldName) bookmarks[index] = bookmarks[index].copy(folder = newName) }
                                persist("bookmarks", bookmarks)
                                val folderIndex = customBookmarkFolders.indexOf(oldName)
                                if (folderIndex >= 0) customBookmarkFolders[folderIndex] = newName
                                prefs.edit().putStringSet("bookmark_folders", customBookmarkFolders.toSet()).apply()
                                if (selectedBookmarkFolder == oldName) selectedBookmarkFolder = newName
                            }
                            renameBookmarkFolderFrom = null
                            showManageBookmarkFoldersDialog = true
                        }, enabled = renameBookmarkFolderTo.trim().isNotBlank()) { Text("Save") }
                    },
                    dismissButton = { TextButton(onClick = { renameBookmarkFolderFrom = null; showManageBookmarkFoldersDialog = true }) { Text("Cancel") } }
                )
            }
            if (showCreateBookmarkFolderDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateBookmarkFolderDialog = false },
                    title = { Text("Create bookmark folder") },
                    text = {
                        OutlinedTextField(
                            value = newBookmarkFolderName,
                            onValueChange = { newBookmarkFolderName = it },
                            singleLine = true,
                            label = { Text("Folder name") },
                            placeholder = { Text("e.g. Finance, Travel") }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val name = newBookmarkFolderName.trim().take(30)
                            if (name.isNotBlank() && name.lowercase() != "all" && name.lowercase() != "general" &&
                                customBookmarkFolders.none { it.equals(name, true) } &&
                                bookmarks.none { it.folder.equals(name, true) }) {
                                customBookmarkFolders.add(name)
                                prefs.edit().putStringSet("bookmark_folders", customBookmarkFolders.toSet()).apply()
                                selectedBookmarkFolder = name
                            }
                            showCreateBookmarkFolderDialog = false
                        }, enabled = newBookmarkFolderName.trim().isNotBlank()) { Text("Create") }
                    },
                    dismissButton = { TextButton(onClick = { showCreateBookmarkFolderDialog = false }) { Text("Cancel") } }
                )
            }
            if (showImportBookmarksDialog) {
                AlertDialog(
                    onDismissRequest = { showImportBookmarksDialog = false },
                    title = { Text("Import bookmarks") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Paste one URL per line. Existing URLs will be skipped.")
                            OutlinedTextField(value = importBookmarksText, onValueChange = { importBookmarksText = it }, modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 7, placeholder = { Text("https://example.com") })
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            importBookmarksText.lines().map { it.trim() }.filter { it.startsWith("https://") || it.startsWith("http://") }.forEach { url ->
                                if (bookmarks.none { it.url == url }) bookmarks.add(SavedPage(Uri.parse(url).host ?: url, url))
                            }
                            persist("bookmarks", bookmarks)
                            showImportBookmarksDialog = false
                        }) { Text("Import") }
                    },
                    dismissButton = { TextButton(onClick = { showImportBookmarksDialog = false }) { Text("Cancel") } }
                )
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
            if (screen != "browser" || showHomeDashboard) {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().background(Brush.verticalGradient(listOf(Color(0xFF101D35), Color(0xFF080F20)))).border(1.dp, Color(0xFF1D3B66)).padding(horizontal = 4.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    NavigationBarItem(selected = screen == "browser" && showHomeDashboard, onClick = { screen = "browser"; showHomeDashboard = true; address = "" }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                    NavigationBarItem(selected = screen == "bookmarks", onClick = { screen = "bookmarks" }, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text("Saved") })
                    NavigationBarItem(selected = screen == "history", onClick = { screen = "history" }, icon = { Icon(Icons.Default.History, null) }, label = { Text("History") })
                    NavigationBarItem(selected = screen == "downloads", onClick = { screen = "downloads" }, icon = { Icon(Icons.Default.Download, null) }, label = { Text("Downloads") })
                    NavigationBarItem(selected = screen == "tabs", onClick = { screen = "tabs" }, icon = { Icon(Icons.Default.Tab, null) }, label = { Text("Tabs") })
                    NavigationBarItem(selected = screen == "settings", onClick = { screen = "settings" }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
                }
            }
        }
    }
}
