package com.bobby.upitracker.domain

import com.bobby.upitracker.data.BankTransaction
import java.util.regex.Pattern

object BankSmsParser {
    
    // Transaction type patterns
    private val debitPattern = Pattern.compile(
        "(?:debited|withdrawn|spent|paid|debit|purchase|transfer)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val creditPattern = Pattern.compile(
        "(?:credited|received|deposited|credit|refund)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Amount patterns - flexible to handle different formats
    private val amountPattern = Pattern.compile(
        "(?:Rs:?|INR|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Balance patterns
    private val balancePattern = Pattern.compile(
        "(?:Avl Bal|Available Balance|Bal|Balance)\\s*(?:Rs:?|INR|₹)?\\s*([\\d,]+(?:\\.\\d{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Reference number patterns
    private val referencePattern = Pattern.compile(
        "(?:ref no|Ref No|UTR|Transaction ID|Txn ID|Reference)\\s*:?\\s*([A-Z0-9]+)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Account number pattern
    private val accountPattern = Pattern.compile(
        "A/c\\s*(?:No\\.?)?\\s*(\\*?\\d{3,4})",
        Pattern.CASE_INSENSITIVE
    )
    
    // Date/time pattern
    private val dateTimePattern = Pattern.compile(
        "(\\d{2}-\\d{2}-\\d{4})\\s+(\\d{2}:\\d{2}(?::\\d{2})?)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Bank name patterns
    private val bankPatterns = mapOf(
        "Union Bank of India" to Pattern.compile("Union Bank", Pattern.CASE_INSENSITIVE),
        "HDFC Bank" to Pattern.compile("HDFC|HD-HDFCBK", Pattern.CASE_INSENSITIVE),
        "State Bank of India" to Pattern.compile("SBI|SBIINB|State Bank", Pattern.CASE_INSENSITIVE),
        "ICICI Bank" to Pattern.compile("ICICI|iMobile", Pattern.CASE_INSENSITIVE),
        "Axis Bank" to Pattern.compile("Axis Bank|AXISBK", Pattern.CASE_INSENSITIVE),
        "Kotak Mahindra Bank" to Pattern.compile("Kotak|KMB", Pattern.CASE_INSENSITIVE),
        "Punjab National Bank" to Pattern.compile("PNB|Punjab National", Pattern.CASE_INSENSITIVE),
        "Bank of Baroda" to Pattern.compile("Bank of Baroda|BOB", Pattern.CASE_INSENSITIVE),
        "Canara Bank" to Pattern.compile("Canara Bank", Pattern.CASE_INSENSITIVE),
        "IDBI Bank" to Pattern.compile("IDBI", Pattern.CASE_INSENSITIVE),
        "IndusInd Bank" to Pattern.compile("IndusInd", Pattern.CASE_INSENSITIVE),
        "Yes Bank" to Pattern.compile("Yes Bank|YESBNK", Pattern.CASE_INSENSITIVE),
        "Federal Bank" to Pattern.compile("Federal Bank", Pattern.CASE_INSENSITIVE),
        "Bank of India" to Pattern.compile("Bank of India|BOI", Pattern.CASE_INSENSITIVE)
    )
    
    fun parse(smsBody: String, timestamp: Long): BankTransaction? {
        // Determine transaction type
        val isDebit = debitPattern.matcher(smsBody).find()
        val isCredit = creditPattern.matcher(smsBody).find()
        
        if (!isDebit && !isCredit) {
            return null // Not a transaction SMS
        }
        
        val type = if (isDebit) "DEBIT" else "CREDIT"
        
        // Extract amount
        val amountMatcher = amountPattern.matcher(smsBody)
        if (!amountMatcher.find()) {
            return null
        }
        val amountStr = amountMatcher.group(1)?.replace(",", "") ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null
        
        if (amount <= 0) {
            return null
        }
        
        // Extract balance (optional)
        val balanceMatcher = balancePattern.matcher(smsBody)
        val balance = if (balanceMatcher.find()) {
            balanceMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        } else {
            null
        }
        
        // Extract account number
        val accountMatcher = accountPattern.matcher(smsBody)
        val accountNumber = if (accountMatcher.find()) {
            accountMatcher.group(1) ?: "Unknown"
        } else {
            "Unknown"
        }
        
        // Extract reference number
        val referenceMatcher = referencePattern.matcher(smsBody)
        val referenceNumber = if (referenceMatcher.find()) {
            referenceMatcher.group(1)
        } else {
            null
        }
        
        // Detect bank name
        var bankName = "Unknown Bank"
        for ((name, pattern) in bankPatterns) {
            if (pattern.matcher(smsBody).find()) {
                bankName = name
                break
            }
        }
        
        // Extract description (transaction method/type)
        val description = when {
            smsBody.contains("UPI", ignoreCase = true) -> "UPI Transaction"
            smsBody.contains("ATM", ignoreCase = true) -> "ATM Withdrawal"
            smsBody.contains("POS", ignoreCase = true) -> "Card Payment"
            smsBody.contains("Mob Bk", ignoreCase = true) || 
            smsBody.contains("Mobile", ignoreCase = true) -> "Mobile Banking"
            smsBody.contains("Net Banking", ignoreCase = true) -> "Net Banking"
            smsBody.contains("IMPS", ignoreCase = true) -> "IMPS Transfer"
            smsBody.contains("NEFT", ignoreCase = true) -> "NEFT Transfer"
            smsBody.contains("RTGS", ignoreCase = true) -> "RTGS Transfer"
            else -> if (type == "DEBIT") "Debit" else "Credit"
        }
        
        return BankTransaction(
            type = type,
            amount = amount,
            balance = balance,
            bankName = bankName,
            accountNumber = accountNumber,
            referenceNumber = referenceNumber,
            timestamp = timestamp,
            description = description,
            rawMessage = smsBody
        )
    }
    
    // Get bank initials for UI display
    fun getBankInitials(bankName: String): String {
        return when {
            bankName.contains("Union Bank", ignoreCase = true) -> "UBI"
            bankName.contains("HDFC", ignoreCase = true) -> "HDFC"
            bankName.contains("State Bank", ignoreCase = true) || 
            bankName.contains("SBI", ignoreCase = true) -> "SBI"
            bankName.contains("ICICI", ignoreCase = true) -> "ICICI"
            bankName.contains("Axis", ignoreCase = true) -> "AXIS"
            bankName.contains("Kotak", ignoreCase = true) -> "KOTAK"
            bankName.contains("PNB", ignoreCase = true) || 
            bankName.contains("Punjab", ignoreCase = true) -> "PNB"
            bankName.contains("BOB", ignoreCase = true) || 
            bankName.contains("Baroda", ignoreCase = true) -> "BOB"
            bankName.contains("Canara", ignoreCase = true) -> "CANARA"
            bankName.contains("IDBI", ignoreCase = true) -> "IDBI"
            bankName.contains("IndusInd", ignoreCase = true) -> "IIB"
            bankName.contains("Yes Bank", ignoreCase = true) -> "YES"
            bankName.contains("Federal", ignoreCase = true) -> "FED"
            bankName.contains("Bank of India", ignoreCase = true) -> "BOI"
            else -> bankName.take(3).uppercase()
        }
    }
}
