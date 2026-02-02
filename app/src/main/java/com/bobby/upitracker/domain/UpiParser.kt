package com.bobby.upitracker.domain

import com.bobby.upitracker.data.UpiTransaction
import java.util.regex.Pattern

object UpiParser {
    
    // Regex patterns for different banks and UPI apps
    private val creditedPattern = Pattern.compile(
        "(?:credited|received|deposited).*?(?:Rs\\.?|INR|₹)\\s*([\\d,]+(?:\\.\\d{2})?)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val amountPattern = Pattern.compile(
        "(?:Rs\\.?|INR|₹)\\s*([\\d,]+(?:\\.\\d{2})?)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val upiPattern = Pattern.compile(
        "UPI",
        Pattern.CASE_INSENSITIVE
    )
    
    private val senderPattern = Pattern.compile(
        "(?:from|by)\\s+([A-Za-z\\s]+?)(?:\\s+on|\\s+to|\\.|\\s+UPI)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val platformPatterns = mapOf(
        "Google Pay" to Pattern.compile("(?:Google Pay|GPay|G Pay)", Pattern.CASE_INSENSITIVE),
        "PhonePe" to Pattern.compile("PhonePe", Pattern.CASE_INSENSITIVE),
        "Paytm" to Pattern.compile("Paytm", Pattern.CASE_INSENSITIVE),
        "BHIM" to Pattern.compile("BHIM", Pattern.CASE_INSENSITIVE),
        "Amazon Pay" to Pattern.compile("Amazon Pay", Pattern.CASE_INSENSITIVE),
        "Razorpay" to Pattern.compile("Razorpay", Pattern.CASE_INSENSITIVE)
    )
    
    fun parse(smsBody: String, timestamp: Long): UpiTransaction? {
        // Check if it's a UPI transaction
        if (!upiPattern.matcher(smsBody).find()) {
            return null
        }
        
        // Check if it's a credit/received transaction
        val creditedMatcher = creditedPattern.matcher(smsBody)
        if (!creditedMatcher.find()) {
            return null
        }
        
        // Extract amount
        val amountStr = creditedMatcher.group(1)?.replace(",", "") ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null
        
        // Extract sender
        val senderMatcher = senderPattern.matcher(smsBody)
        val sender = if (senderMatcher.find()) {
            senderMatcher.group(1)?.trim() ?: "Unknown"
        } else {
            "Unknown"
        }
        
        // Detect platform
        var platform = "UPI"
        for ((name, pattern) in platformPatterns) {
            if (pattern.matcher(smsBody).find()) {
                platform = name
                break
            }
        }
        
        return UpiTransaction(
            amount = amount,
            sender = sender,
            platform = platform,
            timestamp = timestamp,
            rawMessage = smsBody
        )
    }
}
