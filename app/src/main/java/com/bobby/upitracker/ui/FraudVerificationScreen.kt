package com.bobby.upitracker.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

enum class VerificationType(val displayName: String, val fieldName: String) {
    MOBILE("Mobile Number", "mobile"),
    BANK_ACCOUNT("Bank Account Number", "bank"),
    EMAIL("Email ID", "email"),
    UPI_ID("UPI ID", "upi")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FraudVerificationScreen(
    mobileNumber: String,
    onBackClick: () -> Unit,
    onVerificationComplete: (Boolean, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var checkingResults by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(VerificationType.MOBILE) }
    var verificationValue by remember { mutableStateOf(mobileNumber) }
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // Auto-check results periodically after page loads
    LaunchedEffect(Unit) {
        delay(5000)
        checkingResults = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fraud Verification") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Instructions Card - Scrollable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "How to Verify",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            "Verifying: $verificationValue",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        Text(
                            "Steps:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        listOf(
                            "1. Select verification type below",
                            "2. Portal will load with auto-filled details",
                            "3. Solve the CAPTCHA manually",
                            "4. Click 'Search' button",
                            "5. App will auto-detect results"
                        ).forEach { step ->
                            Text(
                                step,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        if (checkingResults) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    "Monitoring for results...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Verification Type Selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Verification Type",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        VerificationType.values().forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selectedType == type,
                                        onClick = { 
                                            selectedType = type
                                            verificationValue = when(type) {
                                                VerificationType.MOBILE -> mobileNumber
                                                else -> ""
                                            }
                                            webView?.evaluateJavascript(
                                                """
                                                (function() {
                                                    var radios = document.querySelectorAll('input[type="radio"]');
                                                    radios.forEach(function(radio) {
                                                        if (radio.value && radio.value.toLowerCase().includes('${type.fieldName}')) {
                                                            radio.checked = true;
                                                            radio.click();
                                                        }
                                                    });
                                                })();
                                                """.trimIndent(),
                                                null
                                            )
                                        }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedType == type,
                                    onClick = null,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    type.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // WebView - Takes more space, easier to see CAPTCHA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webView = this
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    
                                    // Select radio button
                                    view?.evaluateJavascript(
                                        """
                                        (function() {
                                            var radios = document.querySelectorAll('input[type="radio"]');
                                            radios.forEach(function(radio) {
                                                if (radio.value && radio.value.toLowerCase().includes('${selectedType.fieldName}')) {
                                                    radio.checked = true;
                                                    radio.click();
                                                }
                                            });
                                        })();
                                        """.trimIndent(),
                                        null
                                    )
                                    
                                    // Auto-fill value
                                    view?.evaluateJavascript(
                                        """
                                        (function() {
                                            setTimeout(function() {
                                                var inputs = document.querySelectorAll('input[type="text"]');
                                                if (inputs.length > 0) {
                                                    inputs[0].value = '$verificationValue';
                                                }
                                            }, 500);
                                        })();
                                        """.trimIndent(),
                                        null
                                    )
                                    
                                    if (checkingResults) {
                                        view?.let { monitorResults(it, onVerificationComplete, context) }
                                    }
                                }
                            }
                            
                            loadUrl("https://cybercrime.gov.in/Webform/suspect_search_repository.aspx")
                        }
                    },
                    update = { view ->
                        if (checkingResults) {
                            monitorResults(view, onVerificationComplete, context)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            // Manual override - Compact
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Manual Override:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onVerificationComplete(false, "User marked as safe")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Safe", style = MaterialTheme.typography.labelLarge)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                onVerificationComplete(true, "User marked as reported")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Warning, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reported", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

private fun monitorResults(
    webView: WebView,
    onComplete: (Boolean, String?) -> Unit,
    context: android.content.Context
) {
    webView.evaluateJavascript(
        """
        (function() {
            var pageText = document.body.innerText.toLowerCase();
            
            if (pageText.includes('found') && !pageText.includes('not found') && !pageText.includes('no record')) {
                return 'REPORTED:Found in cybercrime database';
            }
            
            if (pageText.includes('no record') || pageText.includes('no data') || 
                pageText.includes('not found') || pageText.includes('no result')) {
                return 'SAFE:No reports found';
            }
            
            var tables = document.querySelectorAll('table');
            if (tables.length > 0) {
                for (var i = 0; i < tables.length; i++) {
                    var tableText = tables[i].innerText.toLowerCase();
                    if (tableText.includes('complaint') || tableText.includes('report')) {
                        return 'REPORTED:Found in database';
                    }
                }
            }
            
            return 'PENDING';
        })();
        """.trimIndent()
    ) { result ->
        if (result != null && result != "null" && result != "\"PENDING\"") {
            val cleanResult = result.replace("\"", "")
            when {
                cleanResult.startsWith("SAFE:") -> {
                    val message = cleanResult.substring(5)
                    Toast.makeText(context, "✓ $message", Toast.LENGTH_SHORT).show()
                    onComplete(false, message)
                }
                cleanResult.startsWith("REPORTED:") -> {
                    val message = cleanResult.substring(9)
                    Toast.makeText(context, "⚠ $message", Toast.LENGTH_LONG).show()
                    onComplete(true, message)
                }
            }
        }
    }
}
