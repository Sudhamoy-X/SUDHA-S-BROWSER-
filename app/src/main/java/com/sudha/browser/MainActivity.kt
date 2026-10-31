package com.sudha.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private val Midnight = Color(0xFF080B14)
private val SurfaceBlue = Color(0xFF111A2B)
private val ElectricBlue = Color(0xFF398BFF)
private val Cyan = Color(0xFF45D7F5)

private data class BrowserTab(
    val id: Int,
    var url: String = "https://www.google.com",
    var title: String = "New tab",
    var webView: WebView? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SudhaBrowserApp() }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SudhaBrowserApp() {
    val tabs = remember { mutableStateListOf(BrowserTab(id = 1)) }
    var nextTabId by remember { mutableIntStateOf(2) }
    var activeTabId by remember { mutableIntStateOf(1) }
    var address by remember { mutableStateOf("") }
    var progress by remember { mutableIntStateOf(0) }
    var showTabManager by remember { mutableStateOf(false) }
    val activeTab = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()

    fun navigate(raw: String) {
        val value = raw.trim()
        if (value.isBlank()) return
        val target = when {
            value.startsWith("https://") || value.startsWith("http://") -> value
            value.contains(" ") || !value.contains(".") -> "https://www.google.com/search?q=" + value.replace(" ", "+")
            else -> "https://$value"
        }
        activeTab.url = target
        activeTab.title = target
        address = target
        activeTab.webView?.loadUrl(target)
    }

    fun addTab() {
        val tab = BrowserTab(id = nextTabId++)
        tabs.add(tab)
        activeTabId = tab.id
        address = ""
        showTabManager = false
    }

    fun closeTab(tabId: Int) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index < 0) return
        val removed = tabs[index]
        removed.webView?.apply { stopLoading(); destroy() }
        tabs.removeAt(index)
        if (tabs.isEmpty()) {
            val fresh = BrowserTab(id = nextTabId++)
            tabs.add(fresh)
            activeTabId = fresh.id
        } else if (activeTabId == tabId) {
            activeTabId = tabs[(index - 1).coerceAtLeast(0)].id
        }
        address = tabs.first { it.id == activeTabId }.url
    }

    BackHandler(enabled = showTabManager || activeTab.webView?.canGoBack() == true) {
        if (showTabManager) showTabManager = false else activeTab.webView?.goBack()
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = ElectricBlue, secondary = Cyan, background = Midnight, surface = SurfaceBlue)) {
        Column(Modifier.fillMaxSize().background(Midnight).statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Brush.linearGradient(listOf(ElectricBlue, Color(0xFF6548D8), Cyan)), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Text("S", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("SUDHA BROWSER", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Your Web. Your Way.", color = Cyan, style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = { activeTab.webView?.reload() }) { Icon(Icons.Default.Refresh, "Reload", tint = Color.White) }
                IconButton(onClick = { showTabManager = true }) {
                    BadgedBox(badge = { Badge { Text(tabs.size.toString()) } }) {
                        Icon(Icons.Default.Tab, "Tabs", tint = Color.White)
                    }
                }
            }

            if (!showTabManager) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).background(SurfaceBlue, CircleShape).padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = Cyan)
                    Spacer(Modifier.width(8.dp))
                    TextField(
                        value = address, onValueChange = { address = it }, modifier = Modifier.weight(1f),
                        placeholder = { Text("Search or enter address", color = Color.LightGray) }, singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Cyan
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { navigate(address) })
                    )
                    IconButton(onClick = { navigate(address) }) { Icon(Icons.Default.ArrowForward, "Go", tint = Cyan) }
                }
                if (progress in 1..99) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(2.dp), color = ElectricBlue, trackColor = Midnight)

                key(activeTab.id) {
                    AndroidView(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        factory = { context ->
                            (activeTab.webView ?: WebView(context).also { activeTab.webView = it }).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadsImagesAutomatically = true
                                settings.javaScriptCanOpenWindowsAutomatically = false
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        if (url != null) { activeTab.url = url; address = url }
                                    }
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        if (url != null) {
                                            activeTab.url = url
                                            activeTab.title = view?.title?.takeIf { it.isNotBlank() } ?: url
                                            address = url
                                        }
                                    }
                                }
                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) { progress = newProgress }
                                }
                                if (this.url.isNullOrBlank()) loadUrl(activeTab.url)
                            }
                        },
                        update = { view ->
                            if (view.url != activeTab.url && activeTab.url.isNotBlank()) view.loadUrl(activeTab.url)
                        }
                    )
                }
            } else {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Your tabs", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("${tabs.size} open", color = Cyan, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(onClick = { addTab() }) { Icon(Icons.Default.Add, "New tab") }
                }
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(tabs, key = { it.id }) { tab ->
                        Row(
                            Modifier.fillMaxWidth()
                                .background(if (tab.id == activeTabId) Color(0xFF172B48) else SurfaceBlue, RoundedCornerShape(16.dp))
                                .clickable { activeTabId = tab.id; address = tab.url; showTabManager = false }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(34.dp).background(Color(0xFF203B60), CircleShape), contentAlignment = Alignment.Center) {
                                Text((tabs.indexOf(tab) + 1).toString(), color = Cyan)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(tab.title, color = Color.White, maxLines = 1, style = MaterialTheme.typography.titleSmall)
                                Text(tab.url, color = Color.LightGray, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { closeTab(tab.id) }) { Icon(Icons.Default.Close, "Close tab", tint = Color.LightGray) }
                        }
                    }
                }
                Button(onClick = { addTab() }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("New tab")
                }
            }

            Row(Modifier.fillMaxWidth().navigationBarsPadding().background(SurfaceBlue).padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { activeTab.webView?.let { if (it.canGoBack()) it.goBack() } }) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                IconButton(onClick = { activeTab.webView?.let { if (it.canGoForward()) it.goForward() } }) { Icon(Icons.Default.ArrowForward, "Forward", tint = Color.White) }
                IconButton(onClick = { navigate("https://www.google.com") }) { Icon(Icons.Default.Home, "Home", tint = Color.White) }
                Text("${tabs.size} tabs", color = Cyan, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { showTabManager = true })
            }
        }
    }
}
