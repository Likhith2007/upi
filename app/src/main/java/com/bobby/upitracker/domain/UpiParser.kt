package com.bobby.upitracker.domain

import com.bobby.upitracker.data.UpiTransaction
import java.util.regex.Pattern

object UpiParser {
    
    // Improved regex patterns for better transaction detection
    private val creditedPattern = Pattern.compile(
        "(?:credited|received|deposited|sent to you|paid you|transferred).*?(?:Rs\\.?|INR|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )
    
    // More flexible amount pattern
    private val amountPattern = Pattern.compile(
        "(?:Rs\\.?|INR|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Check for UPI or common payment keywords
    private val upiPattern = Pattern.compile(
        "(?:UPI|VPA|Google Pay|GPay|PhonePe|Paytm|BHIM|Amazon Pay|WhatsApp Pay|credited|received)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Better sender extraction
    private val senderPattern = Pattern.compile(
        "(?:from|by|sender|paid by)\\s+([A-Za-z][A-Za-z\\s]{1,30})(?:\\s+on|\\s+to|\\s+via|\\.|\\s+UPI|\\s+A/c|$)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Alternative sender pattern for different SMS formats
    private val senderPattern2 = Pattern.compile(
        "([A-Za-z][A-Za-z\\s]{2,30})\\s+(?:has sent|sent you|paid you|transferred)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val platformPatterns = mapOf(
        "Google Pay" to Pattern.compile("(?:Google Pay|GPay|G Pay|G-Pay)", Pattern.CASE_INSENSITIVE),
        "PhonePe" to Pattern.compile("PhonePe|Phone Pe", Pattern.CASE_INSENSITIVE),
        "Paytm" to Pattern.compile("Paytm", Pattern.CASE_INSENSITIVE),
        "BHIM" to Pattern.compile("BHIM", Pattern.CASE_INSENSITIVE),
        "Amazon Pay" to Pattern.compile("Amazon Pay", Pattern.CASE_INSENSITIVE),
        "WhatsApp Pay" to Pattern.compile("WhatsApp Pay|WA Pay", Pattern.CASE_INSENSITIVE),
        "Razorpay" to Pattern.compile("Razorpay", Pattern.CASE_INSENSITIVE),
        "HDFC Bank" to Pattern.compile("HDFC|HD-HDFCBK", Pattern.CASE_INSENSITIVE),
        "SBI" to Pattern.compile("SBI|SBIINB", Pattern.CASE_INSENSITIVE),
        "ICICI Bank" to Pattern.compile("ICICI|iMobile", Pattern.CASE_INSENSITIVE),
        "Axis Bank" to Pattern.compile("Axis Bank|AXISBK", Pattern.CASE_INSENSITIVE)
    )
    
    fun parse(smsBody: String, timestamp: Long): UpiTransaction? {
        // Check if it's a UPI/payment transaction
        if (!upiPattern.matcher(smsBody).find()) {
            return null
        }
        
        // Check if it's a credit/received transaction
        val creditedMatcher = creditedPattern.matcher(smsBody)
        if (!creditedMatcher.find()) {
            return null
        }
        
        // Extract amount - remove commas and parse
        val amountStr = creditedMatcher.group(1)?.replace(",", "") ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null
        
        // Amount must be positive
        if (amount <= 0) {
            return null
        }
        
        // Extract sender - try multiple patterns
        var sender = "Unknown"
        
        val senderMatcher = senderPattern.matcher(smsBody)
        if (senderMatcher.find()) {
            sender = senderMatcher.group(1)?.trim() ?: "Unknown"
        } else {
            val senderMatcher2 = senderPattern2.matcher(smsBody)
            if (senderMatcher2.find()) {
                sender = senderMatcher2.group(1)?.trim() ?: "Unknown"
            }
        }
        
        // Clean up sender name
        sender = sender.replace(Regex("\\s+"), " ").trim()
        if (sender.length > 30) {
            sender = sender.substring(0, 30)
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
