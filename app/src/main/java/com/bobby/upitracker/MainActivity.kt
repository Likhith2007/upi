package com.bobby.upitracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bobby.upitracker.data.AppDatabase
import com.bobby.upitracker.data.TransactionRepository
import com.bobby.upitracker.data.User
import com.bobby.upitracker.data.UserDao
import com.bobby.upitracker.data.SavedReceiverDao
import com.bobby.upitracker.data.BankTransactionDao
import com.bobby.upitracker.domain.SmsReader
import com.bobby.upitracker.fraud.FraudVerificationRepository
import com.bobby.upitracker.ui.DashboardScreen
import com.bobby.upitracker.ui.FraudVerificationScreen
import com.bobby.upitracker.ui.HomeViewModel
import com.bobby.upitracker.ui.ImageVerificationScreen
import com.bobby.upitracker.ui.LoginScreen
import com.bobby.upitracker.ui.PaymentScreen
import com.bobby.upitracker.ui.SafetyResourcesScreen
import com.bobby.upitracker.ui.InternalWebViewScreen
import com.bobby.upitracker.ui.TransactionHistoryScreen
import com.bobby.upitracker.ui.theme.UPIMoneyTrackerTheme
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity(), PaymentResultListener {
    
    private val fraudRepository = FraudVerificationRepository()
    internal var pendingOrderId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(applicationContext)
        val transactionRepository = TransactionRepository(database.upiTransactionDao())
        val userDao = database.userDao()
        val savedReceiverDao = database.savedReceiverDao()
        val bankTransactionDao = database.bankTransactionDao()
        
        setContent {
            UPIMoneyTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UpiTrackerApp(
                        transactionRepository = transactionRepository,
                        fraudRepository = fraudRepository,
                        userDao = userDao,
                        savedReceiverDao = savedReceiverDao,
                        bankTransactionDao = bankTransactionDao,
                        onScanSms = { scanSmsMessages(transactionRepository) }
                    )
                }
            }
        }
    }
    
    private fun scanSmsMessages(repository: TransactionRepository) {
        val smsReader = SmsReader(applicationContext)
        kotlinx.coroutines.GlobalScope.launch {
            try {
                val transactions = withContext(Dispatchers.IO) {
                    smsReader.readTransactionSms()
                }
                
                // Filter out duplicates before inserting
                var newCount = 0
                withContext(Dispatchers.IO) {
                    transactions.forEach { transaction ->
                        // Check if similar transaction already exists
                        val existing = repository.findSimilarTransaction(
                            transaction.amount,
                            transaction.sender,
                            transaction.timestamp
                        )
                        if (existing == null) {
                            repository.insertTransaction(transaction)
                            newCount++
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        if (newCount > 0) "Added $newCount new transactions" else "No new transactions found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error scanning SMS: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Toast.makeText(this, "Payment Successful: $razorpayPaymentId", Toast.LENGTH_SHORT).show()
        // Update payment status in database
    }
    
    override fun onPaymentError(errorCode: Int, errorDescription: String?) {
        Toast.makeText(this, "Payment Failed: $errorDescription", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun UpiTrackerApp(
    transactionRepository: TransactionRepository,
    fraudRepository: FraudVerificationRepository,
    userDao: UserDao,
    savedReceiverDao: SavedReceiverDao,
    bankTransactionDao: BankTransactionDao,
    onScanSms: () -> Unit
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Check if user is logged in
    val loggedInUser by userDao.getLoggedInUser().collectAsState(initial = null)
    var startDestination by remember { mutableStateOf("login") }
    
    // Payment data persistence
    var savedReceiverIdentifier by remember { mutableStateOf("") }
    var savedAmount by remember { mutableStateOf("") }
    
    LaunchedEffect(loggedInUser) {
        startDestination = if (loggedInUser != null) "dashboard" else "login"
    }
    val viewModel: HomeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(transactionRepository) as T
            }
        }
    )
    
    val uiState by viewModel.uiState.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showFraudResultDialog by remember { mutableStateOf(false) }
    var fraudResult by remember { mutableStateOf<Pair<Boolean, String?>>(false to null) }
    
    // SMS Permission Launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateSmsPermission(isGranted)
        if (isGranted) {
            onScanSms()
        } else {
            showPermissionDialog = true
        }
    }
    
    // Check permission on launch
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        viewModel.updateSmsPermission(hasPermission)
        
        if (hasPermission) {
            onScanSms()
        } else {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }
    
    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("SMS Permission Required") },
            text = { Text("This app needs SMS permission to track your UPI transactions. Please grant the permission in Settings.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Fraud Result Dialog
    if (showFraudResultDialog) {
        AlertDialog(
            onDismissRequest = { showFraudResultDialog = false },
            title = { 
                Text(if (fraudResult.first) "⚠️ Warning!" else "✓ Verified Safe") 
            },
            text = { 
                Text(
                    if (fraudResult.first) {
                        "This number has been reported in the National Cyber Crime Portal.\n\n${fraudResult.second ?: "Proceed with caution!"}"
                    } else {
                        "This number is not found in the cybercrime database. However, always verify before sending money."
                    }
                )
            },
            confirmButton = {
                Button(onClick = { showFraudResultDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    NavHost(navController = navController, startDestination = startDestination) {
        // Login Screen
        composable("login") {
            LoginScreen(
                onLoginSuccess = { name, mobile, email ->
                    kotlinx.coroutines.GlobalScope.launch {
                        val user = User(
                            name = name,
                            mobile = mobile,
                            email = email,
                            isLoggedIn = true
                        )
                        userDao.insertUser(user)
                    }
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                uiState = uiState,
                onPaymentClick = { navController.navigate("payment") },
                onHistoryClick = { navController.navigate("transaction_history") },
                onSafetyResourcesClick = { navController.navigate("safety_resources") },
                onImageVerificationClick = { navController.navigate("image_verification") },
                onRefreshClick = { 
                    if (uiState.hasSmsPermission) {
                        onScanSms()
                    } else {
                        smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                    }
                }
            )
        }
        
        composable("payment") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val activity = context as? MainActivity
            
            PaymentScreen(
                initialIdentifier = savedReceiverIdentifier,
                initialAmount = savedAmount,
                onVerifyClick = { identifier ->
                    // Save the data before navigating
                    savedReceiverIdentifier = identifier
                    navController.navigate("fraud_verification/$identifier")
                },
                onPayClick = { receiverIdentifier, amount ->
                    // Save the data
                    savedReceiverIdentifier = receiverIdentifier
                    savedAmount = amount.toString()
                    
                    if (activity != null) {
                        // Save receiver to database for future use
                        kotlinx.coroutines.GlobalScope.launch {
                            val existing = savedReceiverDao.getReceiverByIdentifier(receiverIdentifier)
                            if (existing != null) {
                                savedReceiverDao.updateLastUsed(
                                    receiverIdentifier,
                                    amount,
                                    System.currentTimeMillis()
                                )
                            } else {
                                savedReceiverDao.insertReceiver(
                                    com.bobby.upitracker.data.SavedReceiver(
                                        identifier = receiverIdentifier,
                                        identifierType = "MOBILE",
                                        lastUsedAmount = amount
                                    )
                                )
                            }
                        }
                        
                        // Initialize Razorpay and start payment
                        val razorpayManager = com.bobby.upitracker.payment.RazorpayManager(
                            activity = activity,
                            apiKey = "rzp_test_SArUKaVk6n08y3"
                        )
                        
                        razorpayManager.initiatePayment(
                            amount = amount,
                            receiverMobile = receiverIdentifier,
                            receiverName = "Receiver",
                            listener = activity
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Error: Unable to process payment",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onBackClick = { 
                    // Clear saved data when going back to dashboard
                    savedReceiverIdentifier = ""
                    savedAmount = ""
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            "fraud_verification/{mobileNumber}",
            arguments = listOf(navArgument("mobileNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val mobileNumber = backStackEntry.arguments?.getString("mobileNumber") ?: ""
            
            FraudVerificationScreen(
                mobileNumber = mobileNumber,
                onBackClick = { navController.popBackStack() },
                onVerificationComplete = { isReported, details ->
                    fraudResult = isReported to details
                    fraudRepository.cacheResult(
                        mobileNumber,
                        com.bobby.upitracker.fraud.VerificationResult(isReported, details)
                    )
                    showFraudResultDialog = true
                    navController.popBackStack()
                }
            )
        }
        
        // Transaction History Screen
        composable("transaction_history") {
            val bankTransactions by bankTransactionDao.getAllTransactions().collectAsState(initial = emptyList())
            
            TransactionHistoryScreen(
                transactions = bankTransactions,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Safety Resources Screen
        composable("safety_resources") {
            SafetyResourcesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Image Verification Screen
        composable("image_verification") {
            ImageVerificationScreen(
                onBackClick = { navController.popBackStack() },
                onBrowserClick = { url, title -> 
                    // Query params handle special characters better
                    val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                    val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                    navController.navigate("webview?url=$encodedUrl&title=$encodedTitle") 
                }
            )
        }
        
        // Generic WebView Screen
        // Generic WebView Screen
        composable(
            "webview?url={url}&title={title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: "Browser"
            
            InternalWebViewScreen(
                url = url, // Query params are automatically decoded by Navigation Compose
                title = title,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
