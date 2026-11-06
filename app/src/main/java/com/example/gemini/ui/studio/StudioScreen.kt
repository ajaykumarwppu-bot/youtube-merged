package com.example.gemini.ui.studio

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gemini.data.studio.StudioServerManager

/**
 * Creator Studio — pura YouTube automation web app app ke andar hi local Node
 * server par chalta hai aur yahan WebView me render hota hai. Screen switch karne
 * par composable alive rehta hai (Browser/Notes jaisa persistent pattern), isliye
 * Studio ka page state survive karta hai.
 */
private sealed interface StudioState {
    data object Loading : StudioState
    data object Ready : StudioState
    data class Error(val message: String) : StudioState
}

@Composable
fun StudioScreen(
    isVisible: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf<StudioState>(StudioState.Loading) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(isVisible, reloadKey) {
        if (!isVisible) return@LaunchedEffect
        state = StudioState.Loading
        try {
            StudioServerManager.ensureStarted(context)
            state = StudioState.Ready
        } catch (e: Exception) {
            state = StudioState.Error(e.message ?: "Studio server start nahi hua")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top bar — back, title, reload
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Creator Studio",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "YouTube Automation — local server",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { reloadKey++ },
                enabled = state == StudioState.Ready
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = "Reload",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        when (val s = state) {
            StudioState.Loading -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                Text(
                    "Studio server start ho raha hai…",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    "Pehli baar thoda time lagta hai (extract + Node cold start).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            is StudioState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "Studio start nahi hua",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    s.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Button(
                    onClick = { reloadKey++ },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        "Retry",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            StudioState.Ready -> Box(modifier = Modifier.fillMaxSize()) {
                StudioWebView(
                    url = StudioServerManager.baseUrl(),
                    reloadKey = reloadKey
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun StudioWebView(url: String, reloadKey: Int) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.userAgentString = "CreatorStudioApp/1.0 $settings.userAgentString"
            webViewClient = WebViewClient()
            // loopback-only http — cleartext manifest me already allowed hai
            loadUrl(url)
        }
    }

    LaunchedEffect(reloadKey) {
        if (reloadKey > 0) webView.loadUrl(url)
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize(),
        onRelease = { it.stopLoading() }
    )
}
