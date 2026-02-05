package com.bobby.upitracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_transactions")
data class BankTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,              // "DEBIT" or "CREDIT"
    val amount: Double,
    val balance: Double? = null,
    val bankName: String,
    val accountNumber: String,     // Last 4 digits (e.g., "*3358")
    val referenceNumber: String? = null,
    val timestamp: Long,
    val description: String? = null,
    val rawMessage: String
)
