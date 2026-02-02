package com.bobby.upitracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_transactions")
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiverMobile: String,
    val amount: Double,
    val status: String, // SUCCESS, FAILED, PENDING
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null,
    val timestamp: Long,
    val fraudCheckPerformed: Boolean = false,
    val fraudCheckResult: String? = null // SAFE, REPORTED, SKIPPED
)
