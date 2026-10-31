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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SudhaBrowserApp() }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SudhaBrowserApp() {
    var address by remember { mutableStateOf("") }
    var requestedUrl by remember { mutableStateOf("https://www.google.com") }
    var currentUrl by remember { mutableStateOf("https://www.google.com") }
    var progress by remember { mutableIntStateOf(0) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun navigate(raw: String) {
        val value = raw.trim()
        if (value.isBlank()) return
        val target = when {
            value.startsWith("https://") || value.startsWith("http://") -> value
            value.contains(" ") || !value.contains(".") -> "https://www.google.com/search?q=" + value.replace(" ", "+")
            else -> "https://" + value
        }
        address = target
        requestedUrl = target
    }

    BackHandler(enabled = webView?.canGoBack() == true) { webView?.goBack() }

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
                IconButton(onClick = { webView?.reload() }) { Icon(Icons.Default.Refresh, "Reload", tint = Color.White) }
                IconButton(onClick = { }) { Icon(Icons.Default.Tab, "Tabs", tint = Color.White) }
            }
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
            AndroidView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.javaScriptCanOpenWindowsAutomatically = false
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                if (url != null) { currentUrl = url; address = url }
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (url != null) { currentUrl = url; address = url }
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) { progress = newProgress }
                        }
                        webView = this
                        loadUrl(requestedUrl)
                    }
                },
                update = { view -> if (view.url != requestedUrl && requestedUrl != currentUrl) view.loadUrl(requestedUrl) }
            )
            Row(Modifier.fillMaxWidth().navigationBarsPadding().background(SurfaceBlue).padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (webView?.canGoBack() == true) webView?.goBack() }) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                IconButton(onClick = { if (webView?.canGoForward() == true) webView?.goForward() }) { Icon(Icons.Default.ArrowForward, "Forward", tint = Color.White) }
                IconButton(onClick = { navigate("https://www.google.com") }) { Icon(Icons.Default.Home, "Home", tint = Color.White) }
                Text("1 tab", color = Cyan, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
