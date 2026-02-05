package com.bobby.upitracker.ui

import com.bobby.upitracker.R

object UpiAppLogos {
    
    /**
     * Get the drawable resource ID for a UPI platform logo
     */
    fun getLogoResource(platform: String): Int {
        return when {
            platform.contains("PhonePe", ignoreCase = true) -> R.drawable.ic_phonepe
            platform.contains("Google Pay", ignoreCase = true) ||
            platform.contains("GPay", ignoreCase = true) -> R.drawable.ic_gpay
            platform.contains("Paytm", ignoreCase = true) -> R.drawable.ic_paytm
            platform.contains("WhatsApp", ignoreCase = true) -> R.drawable.ic_whatsapp_pay
            platform.contains("Amazon Pay", ignoreCase = true) -> android.R.drawable.ic_menu_gallery
            platform.contains("BHIM", ignoreCase = true) -> android.R.drawable.ic_menu_send
            else -> android.R.drawable.ic_menu_send // Default UPI icon
        }
    }
    
    /**
     * Check if a platform has a custom logo
     */
    fun hasCustomLogo(platform: String): Boolean {
        return platform.contains("PhonePe", ignoreCase = true) ||
               platform.contains("Google Pay", ignoreCase = true) ||
               platform.contains("GPay", ignoreCase = true) ||
               platform.contains("Paytm", ignoreCase = true) ||
               platform.contains("WhatsApp", ignoreCase = true)
    }
}
