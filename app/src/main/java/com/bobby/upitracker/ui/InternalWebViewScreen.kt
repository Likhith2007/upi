package com.bobby.upitracker.ui

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternalWebViewScreen(
    url: String,
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val result = uri?.let { arrayOf(it) }
        filePathCallback?.onReceiveValue(result)
        filePathCallback = null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        allowFileAccess = true
                        allowContentAccess = true
                        // Use Desktop User Agent to ensure Google Lens and TinEye work fully
                        userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    }
                    
                    webViewClient = WebViewClient()
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            newCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            filePathCallback = newCallback
                            filePickerLauncher.launch("image/*")
                            return true
                        }
                    }
                    
                    loadUrl(url)
                }
            }
        )
    }
}
