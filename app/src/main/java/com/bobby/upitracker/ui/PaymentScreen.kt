package com.bobby.upitracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

enum class PaymentMethod(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MOBILE("Mobile Number", Icons.Default.Phone),
    UPI_ID("UPI ID", Icons.Default.AccountBox),
    ACCOUNT("Account Number", Icons.Default.AccountBalance)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onVerifyClick: (String) -> Unit,
    onPayClick: (String, Double) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.MOBILE) }
    var receiverIdentifier by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Money") },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                "Enter Payment Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            // Payment Method Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Select Payment Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    PaymentMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedMethod == method,
                                    onClick = { 
                                        selectedMethod = method
                                        receiverIdentifier = ""
                                        showError = false
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = null
                            )
                            Icon(
                                method.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                method.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            // Receiver Input
            OutlinedTextField(
                value = receiverIdentifier,
                onValueChange = { 
                    receiverIdentifier = when(selectedMethod) {
                        PaymentMethod.MOBILE -> if (it.length <= 10) it else receiverIdentifier
                        PaymentMethod.ACCOUNT -> if (it.length <= 18) it else receiverIdentifier
                        PaymentMethod.UPI_ID -> it
                    }
                    showError = false
                },
                label = { 
                    Text(when(selectedMethod) {
                        PaymentMethod.MOBILE -> "Receiver Mobile Number"
                        PaymentMethod.UPI_ID -> "Receiver UPI ID"
                        PaymentMethod.ACCOUNT -> "Receiver Account Number"
                    })
                },
                placeholder = { 
                    Text(when(selectedMethod) {
                        PaymentMethod.MOBILE -> "10-digit mobile number"
                        PaymentMethod.UPI_ID -> "example@upi"
                        PaymentMethod.ACCOUNT -> "Account number"
                    })
                },
                leadingIcon = { Icon(selectedMethod.icon, null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = when(selectedMethod) {
                        PaymentMethod.MOBILE, PaymentMethod.ACCOUNT -> KeyboardType.Number
                        PaymentMethod.UPI_ID -> KeyboardType.Email
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = showError,
                supportingText = if (showError) {
                    { Text(when(selectedMethod) {
                        PaymentMethod.MOBILE -> "Please enter a valid 10-digit mobile number"
                        PaymentMethod.UPI_ID -> "Please enter a valid UPI ID"
                        PaymentMethod.ACCOUNT -> "Please enter a valid account number"
                    }) }
                } else null
            )
            
            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                placeholder = { Text("Enter amount in ₹") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Verify Button
            OutlinedButton(
                onClick = {
                    if (isValidInput(selectedMethod, receiverIdentifier)) {
                        onVerifyClick(receiverIdentifier)
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = receiverIdentifier.isNotEmpty()
            ) {
                Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify Receiver (Fraud Check)")
            }
            
            // Pay Button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (isValidInput(selectedMethod, receiverIdentifier) && amountValue != null && amountValue > 0) {
                        onPayClick(receiverIdentifier, amountValue)
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = receiverIdentifier.isNotEmpty() && amount.toDoubleOrNull() != null
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Proceed to Pay")
            }
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Column {
                        Text(
                            "Safety First!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Always verify the receiver before sending money. We check against the National Cyber Crime Portal to help protect you from fraud.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun isValidInput(method: PaymentMethod, input: String): Boolean {
    return when(method) {
        PaymentMethod.MOBILE -> input.length == 10 && input.all { it.isDigit() }
        PaymentMethod.UPI_ID -> input.contains("@") && input.length >= 5
        PaymentMethod.ACCOUNT -> input.length >= 9 && input.all { it.isDigit() }
    }
}
