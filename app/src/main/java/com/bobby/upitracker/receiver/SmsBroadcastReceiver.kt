package com.bobby.upitracker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bobby.upitracker.MainActivity
import com.bobby.upitracker.R
import com.bobby.upitracker.data.AppDatabase
import com.bobby.upitracker.domain.BankSmsParser
import com.bobby.upitracker.domain.UpiParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val CHANNEL_ID = "transaction_notifications"
        private const val NOTIFICATION_ID_BASE = 1000
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            messages.forEach { message ->
                val body = message.messageBody
                val timestamp = message.timestampMillis
                
                // Try parsing as bank transaction first
                val bankTransaction = BankSmsParser.parse(body, timestamp)
                if (bankTransaction != null) {
                    saveBankTransaction(context, bankTransaction)
                    showNotification(context, bankTransaction)
                    return@forEach
                }
                
                // Try parsing as UPI transaction
                val upiTransaction = UpiParser.parse(body, timestamp)
                if (upiTransaction != null) {
                    saveUpiTransaction(context, upiTransaction)
                    showUpiNotification(context, upiTransaction)
                }
            }
        }
    }
    
    private fun saveBankTransaction(context: Context, transaction: com.bobby.upitracker.data.BankTransaction) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            
            // Check if transaction already exists (by reference number)
            transaction.referenceNumber?.let { refNo ->
                val existing = database.bankTransactionDao().getTransactionByReference(refNo)
                if (existing != null) {
                    return@launch // Duplicate transaction
                }
            }
            
            database.bankTransactionDao().insertTransaction(transaction)
        }
    }
    
    private fun saveUpiTransaction(context: Context, transaction: com.bobby.upitracker.data.UpiTransaction) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            
            val existing = database.upiTransactionDao().findSimilarTransaction(
                transaction.amount,
                transaction.sender,
                transaction.timestamp
            )
            if (existing != null) {
                return@launch // Duplicate transaction
            }
            
            database.upiTransactionDao().insert(transaction)
        }
    }
    
    private fun showNotification(context: Context, transaction: com.bobby.upitracker.data.BankTransaction) {
        createNotificationChannel(context)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val icon = if (transaction.type == "DEBIT") "💸" else "💰"
        val title = "$icon Transaction Alert"
        val amountText = "₹${String.format("%.2f", transaction.amount)}"
        val typeText = if (transaction.type == "DEBIT") "debited from" else "credited to"
        val message = "$amountText $typeText ${transaction.bankName}"
        val balanceText = transaction.balance?.let { 
            "\nBalance: ₹${String.format("%.2f", it)}" 
        } ?: ""
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$message$balanceText\n${transaction.description ?: ""}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_BASE + transaction.hashCode(), notification)
        }
    }
    
    private fun showUpiNotification(context: Context, transaction: com.bobby.upitracker.data.UpiTransaction) {
        createNotificationChannel(context)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = "💰 Money Received"
        val amountText = "₹${String.format("%.2f", transaction.amount)}"
        val message = "$amountText from ${transaction.sender} via ${transaction.platform}"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_BASE + transaction.hashCode(), notification)
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Transaction Notifications"
            val descriptionText = "Notifications for bank and UPI transactions"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
