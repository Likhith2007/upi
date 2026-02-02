package com.bobby.upitracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upi_transactions")
data class UpiTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val sender: String,
    val platform: String, // GPay, PhonePe, Paytm, etc.
    val timestamp: Long,
    val rawMessage: String,
    val transactionId: String? = null
)
