package com.sudha.browser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.print.PrintAttributes
import android.print.PrintManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.app.DownloadManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.io.ByteArrayInputStream
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
import androidx.compose.foundation.background
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
import org.json.JSONObject

private val Midnight = Color(0xFF080B14)
private val SurfaceBlue = Color(0xFF111A2B)
private val ElectricBlue = Color(0xFF398BFF)
private val Cyan = Color(0xFF45D7F5)

private data class BrowserTab(val id: Int, var url: String = "https://www.google.com", var title: String = "New tab", var webView: WebView? = null)
private data class SavedPage(val title: String, val url: String)
private data class DownloadEntry(val id: Long, val title: String, val status: String, val downloaded: Long, val total: Long, val reason: Int, val uri: Uri?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { SudhaBrowserApp(this) } }
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
    var nextTabId by remember { mutableIntStateOf(2) }
    var activeTabId by remember { mutableIntStateOf(1) }
    var address by remember { mutableStateOf("") }
    var progress by remember { mutableIntStateOf(0) }
    var screen by remember { mutableStateOf("browser") }
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
        activeTab.url = target; activeTab.title = target; address = target; screen = "browser"
        activeTab.webView?.loadUrl(target)
    }
    fun addTab() { val tab = BrowserTab(nextTabId++); tabs.add(tab); activeTabId = tab.id; address = ""; screen = "browser" }
    fun closeTab(id: Int) {
        val index = tabs.indexOfFirst { it.id == id }; if (index < 0) return
        tabs[index].webView?.apply { stopLoading(); destroy() }; tabs.removeAt(index)
        if (tabs.isEmpty()) { val fresh = BrowserTab(nextTabId++); tabs.add(fresh); activeTabId = fresh.id }
        else if (activeTabId == id) activeTabId = tabs[(index - 1).coerceAtLeast(0)].id
        address = tabs.first { it.id == activeTabId }.url
    }
    fun saveBookmark() {
        val item = SavedPage(activeTab.title, activeTab.url)
        if (bookmarks.none { it.url == item.url }) { bookmarks.add(0, item); persist("bookmarks", bookmarks) }
    }
    fun openSaved(item: SavedPage) { activeTab.url = item.url; activeTab.title = item.title; address = item.url; screen = "browser"; activeTab.webView?.loadUrl(item.url) }

    BackHandler(enabled = screen != "browser" || activeTab.webView?.canGoBack() == true) {
        if (screen != "browser") screen = "browser" else activeTab.webView?.goBack()
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = ElectricBlue, secondary = Cyan, background = Midnight, surface = SurfaceBlue)) {
        Column(Modifier.fillMaxSize().background(Midnight).statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Brush.linearGradient(listOf(ElectricBlue, Color(0xFF6548D8), Cyan)), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("S", color = Color.White, style = MaterialTheme.typography.headlineMedium) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text("SUDHA BROWSER", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Your Web. Your Way.", color = Cyan, style = MaterialTheme.typography.labelSmall) }
                IconButton(onClick = { saveBookmark() }) { Icon(Icons.Default.Bookmark, "Bookmark page", tint = Color.White) }
                IconButton(onClick = { showFindBar = !showFindBar; findText = "" }) { Icon(Icons.Default.FindInPage, "Find in page", tint = Color.White) }
                IconButton(onClick = { val url = activeTab.url; if (url.isNotBlank()) runCatching { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url), "Share page")) } }) { Icon(Icons.Default.Share, "Share page", tint = Color.White) }
                IconButton(onClick = { activeTab.webView?.reload() }) { Icon(Icons.Default.Refresh, "Reload", tint = Color.White) }
                IconButton(onClick = { screen = "settings" }) { Icon(Icons.Default.Settings, "Settings", tint = Color.White) }
                IconButton(onClick = { screen = "tabs" }) { BadgedBox(badge = { Badge { Text(tabs.size.toString()) } }) { Icon(Icons.Default.Tab, "Tabs", tint = Color.White) } }
            }
            if (screen == "browser") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).background(SurfaceBlue, CircleShape).padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = Cyan); Spacer(Modifier.width(8.dp))
                    TextField(value = address, onValueChange = { address = it }, modifier = Modifier.weight(1f), placeholder = { Text("Search or enter address", color = Color.LightGray) }, singleLine = true,
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Cyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go), keyboardActions = KeyboardActions(onGo = { navigate(address) }))
                    IconButton(onClick = { navigate(address) }) { Icon(Icons.Default.ArrowForward, "Go", tint = Cyan) }
                }
                if (prefs.getBoolean("show_shortcuts", true)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Google" to "https://www.google.com", "YouTube" to "https://m.youtube.com", "Gmail" to "https://mail.google.com", "Maps" to "https://maps.google.com").forEach { (label, link) ->
                            Text(label, color = Cyan, style = MaterialTheme.typography.labelMedium, modifier = Modifier.background(SurfaceBlue, RoundedCornerShape(18.dp)).clickable { navigate(link) }.padding(horizontal = 12.dp, vertical = 9.dp))
                        }
                    }
                }
                if (showFindBar) Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = findText, onValueChange = { findText = it; activeTab.webView?.findAllAsync(it) }, modifier = Modifier.weight(1f), placeholder = { Text("Find on page") }, singleLine = true)
                    TextButton(onClick = { activeTab.webView?.findAllAsync(findText); activeTab.webView?.findNext(true) }) { Text("Find") }
                    TextButton(onClick = { activeTab.webView?.findNext(true) }) { Text("Next") }
                    TextButton(onClick = { activeTab.webView?.clearMatches(); showFindBar = false; findText = "" }) { Text("Close") }
                }
                if (progress in 1..99) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(2.dp), color = ElectricBlue, trackColor = Midnight)
                key(activeTab.id) {
                    AndroidView(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        factory = { ctx: Context ->
                        (activeTab.webView ?: WebView(ctx).also { activeTab.webView = it }).apply {
                            settings.javaScriptEnabled = javaScriptEnabled; settings.domStorageEnabled = true; settings.loadsImagesAutomatically = imagesEnabled; settings.javaScriptCanOpenWindowsAutomatically = false
                            settings.safeBrowsingEnabled = safeBrowsingEnabled
                            CookieManager.getInstance().setAcceptCookie(cookiesEnabled)
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
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
                                override fun shouldInterceptRequest(view: WebView?, request: android.webkit.WebResourceRequest?): android.webkit.WebResourceResponse? {
                                    val requestUrl = request?.url?.toString() ?: return null
                                    if (BrowserContentFilter.isBlocked(requestUrl)) {
                                        BrowserContentFilter.blockedRequestCount.incrementAndGet()
                                        return android.webkit.WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) { if (url != null) { activeTab.url = url; address = url } }
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    if (readerMode) view?.evaluateJavascript("(function(){var s=document.getElementById('sudha-reader-style');if(!s){s=document.createElement('style');s.id='sudha-reader-style';document.head.appendChild(s);}s.textContent='body{max-width:780px!important;margin:24px auto!important;padding:24px!important;font-family:Georgia,serif!important;font-size:20px!important;line-height:1.8!important;background:#f5f1e8!important;color:#242424!important}';})();", null)
                                    if (url != null) {
                                        activeTab.url = url; activeTab.title = view?.title?.takeIf { it.isNotBlank() } ?: url; address = url
                                        val item = SavedPage(activeTab.title, url)
                                        history.removeAll { it.url == url }; history.add(0, item)
                                        while (history.size > 500) history.removeAt(history.lastIndex)
                                        persist("history", history)
                                    }
                                }
                            }
                            webChromeClient = object : WebChromeClient() { override fun onProgressChanged(view: WebView?, newProgress: Int) { progress = newProgress } }
                            if (this.url.isNullOrBlank()) loadUrl(activeTab.url)
                        }
                    }, update = { view: WebView -> if (view.url != activeTab.url && activeTab.url.isNotBlank()) view.loadUrl(activeTab.url) })
                }
            } else {
                val heading = when (screen) { "tabs" -> "Your tabs"; "bookmarks" -> "Bookmarks"; "history" -> "History"; "settings" -> "Privacy & settings"; else -> "Downloads" }
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(heading, color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    if (screen == "tabs") Text("${tabs.size} open", color = Cyan)
                    if (screen == "tabs") FilledIconButton(onClick = { addTab() }) { Icon(Icons.Default.Add, "New tab") }
                }
                when (screen) {
                    "settings" -> Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("JavaScript", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Some websites require JavaScript to work", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = javaScriptEnabled, onCheckedChange = { javaScriptEnabled = it; prefs.edit().putBoolean("javascript_enabled", it).apply(); tabs.forEach { tab -> tab.webView?.settings?.javaScriptEnabled = it } })
                        }
                        HorizontalDivider(color = Color(0xFF29364B))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Load images", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("Turn off to reduce image data usage", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = imagesEnabled, onCheckedChange = { imagesEnabled = it; prefs.edit().putBoolean("images_enabled", it).apply(); tabs.forEach { tab -> tab.webView?.settings?.loadsImagesAutomatically = it } })
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
                            Column(Modifier.weight(1f)) {
                                Text("Homepage shortcuts", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("Show quick links below the address bar", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = prefs.getBoolean("show_shortcuts", true), onCheckedChange = { prefs.edit().putBoolean("show_shortcuts", it).apply() })
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
                                activeTab.webView?.let { webView ->
                                    webView.settings.userAgentString = if (it) "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36" else android.webkit.WebSettings.getDefaultUserAgent(context)
                                    webView.reload()
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
                            Row(Modifier.fillMaxWidth().background(if (tab.id == activeTabId) Color(0xFF172B48) else SurfaceBlue, RoundedCornerShape(16.dp)).clickable { activeTabId = tab.id; address = tab.url; screen = "browser" }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
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
            Row(Modifier.fillMaxWidth().navigationBarsPadding().background(SurfaceBlue).padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                NavigationBarItem(selected = screen == "browser", onClick = { screen = "browser" }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = screen == "bookmarks", onClick = { screen = "bookmarks" }, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text("Saved") })
                NavigationBarItem(selected = screen == "history", onClick = { screen = "history" }, icon = { Icon(Icons.Default.History, null) }, label = { Text("History") })
                NavigationBarItem(selected = screen == "downloads", onClick = { screen = "downloads" }, icon = { Icon(Icons.Default.Download, null) }, label = { Text("Downloads") })
                NavigationBarItem(selected = screen == "tabs", onClick = { screen = "tabs" }, icon = { Icon(Icons.Default.Tab, null) }, label = { Text("Tabs") })
            }
        }
    }
}
