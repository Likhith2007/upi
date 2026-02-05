package com.bobby.upitracker.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyResourcesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Resources") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Information card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🛡️ Remove Non-Consensual Content",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "If someone has shared intimate images or videos of you without your consent, StopNCII can help you remove them from the internet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // WebView for StopNCII
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        loadUrl("https://stopncii.org/create-your-case/")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
