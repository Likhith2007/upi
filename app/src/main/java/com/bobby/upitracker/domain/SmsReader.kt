package com.bobby.upitracker.domain

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.bobby.upitracker.data.UpiTransaction

class SmsReader(private val context: Context) {
    
    fun readTransactionSms(): List<UpiTransaction> {
        val transactions = mutableListOf<UpiTransaction>()
        val uri = Uri.parse("content://sms/inbox")
        
        val cursor = context.contentResolver.query(
            uri,
            arrayOf("_id", "address", "body", "date"),
            null,
            null,
            "date DESC"
        )
        
        cursor?.use {
            val bodyIndex = it.getColumnIndex("body")
            val dateIndex = it.getColumnIndex("date")
            
            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)
                
                // Parse UPI transaction from SMS
                val transaction = UpiParser.parse(body, date)
                if (transaction != null) {
                    transactions.add(transaction)
                }
            }
        }
        
        return transactions
    }
}
