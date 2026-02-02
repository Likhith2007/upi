package com.bobby.upitracker.fraud

data class VerificationResult(
    val isReported: Boolean,
    val reportDetails: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
