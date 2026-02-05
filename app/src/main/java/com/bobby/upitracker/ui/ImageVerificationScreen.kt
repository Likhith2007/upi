package com.bobby.upitracker.ui

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageVerificationScreen(
    onBackClick: () -> Unit,
    onBrowserClick: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Image captured successfully
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Profile Photo") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔍 Detect Fake Profiles",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Upload a suspicious profile photo to check if it's stolen or fake. This helps protect you from scammers and catfish.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Warning Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Column {
                        Text(
                            "Common Scam Signs:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Too good to be true photos\n• Asks for money quickly\n• Avoids video calls\n• Inconsistent stories",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Upload Options
            Text(
                "Choose Image Source:",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Gallery Button
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select from Gallery")
            }
            
            // Camera Button
            OutlinedButton(
                onClick = { 
                    // Create temp file for camera
                    val photoFile = File.createTempFile("profile_check", ".jpg", context.cacheDir)
                    val photoUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    selectedImageUri = photoUri
                    cameraLauncher.launch(photoUri)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo")
            }
            
            // TinEye Browse Button
            OutlinedButton(
                onClick = { onBrowserClick("https://tineye.com", "TinEye Reverse Search") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Language, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse TinEye.com")
            }
            
            // Yandex Image Search Browse Button
            OutlinedButton(
                onClick = { onBrowserClick("https://yandex.com/images/", "Yandex Images") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse Yandex Images")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Button (only show if image selected)
            if (selectedImageUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Image Selected",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Google Images Search
                        Button(
                            onClick = {
                                try {
                                    // Create intent to share image to Google app
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/*"
                                        putExtra(Intent.EXTRA_STREAM, selectedImageUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        setPackage("com.google.android.googlequicksearchbox") // Google app
                                    }
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    // Fallback: Open Google Images in browser
                                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://images.google.com")
                                    }
                                    context.startActivity(browserIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search with Google")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Alternative: TinEye
                        OutlinedButton(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://tineye.com")
                                }
                                context.startActivity(browserIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Or try TinEye.com")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Tip: Save the image, then upload it manually to Google Images or TinEye for best results.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // How it works
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "How It Works:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "1. Upload the suspicious photo\n" +
                        "2. Search on Google Images\n" +
                        "3. Check if photo appears elsewhere\n" +
                        "4. Verify if it's a real person",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
