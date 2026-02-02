package com.bobby.upitracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_receivers")
data class SavedReceiver(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val identifier: String, // Mobile/UPI/Account
    val identifierType: String, // "MOBILE", "UPI_ID", "ACCOUNT"
    val lastUsedAmount: Double = 0.0,
    val lastUsedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val fraudCheckStatus: String? = null, // "SAFE", "REPORTED", null
    val fraudCheckDate: Long? = null
)
