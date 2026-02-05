package com.bobby.upitracker.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast

object UpiAppLauncher {
    
    /**
     * Launch the UPI app based on platform name
     * Returns true if app was launched successfully
     */
    fun launchApp(context: Context, platform: String): Boolean {
        val packageNames = getPackageNames(platform)
        
        // Try each package name until one works
        for (packageName in packageNames) {
            if (isAppInstalled(context, packageName)) {
                return try {
                    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        return true
                    } else {
                        continue // Try next package
                    }
                } catch (e: Exception) {
                    continue // Try next package
                }
            }
        }
        
        // None of the packages worked
        showNotInstalledMessage(context, platform)
        return false
    }
    
    /**
     * Get possible package names for UPI platform
     * Returns list to try multiple variants
     */
    private fun getPackageNames(platform: String): List<String> {
        return when {
            platform.contains("PhonePe", ignoreCase = true) -> listOf("com.phonepe.app")
            
            platform.contains("Google Pay", ignoreCase = true) ||
            platform.contains("GPay", ignoreCase = true) ||
            platform.contains("UPI", ignoreCase = true) -> listOf(
                "com.google.android.apps.nbu.paisa.user", // GPay India
                "com.google.android.apps.walletnfcrel",    // Google Wallet
                "com.phonepe.app",                          // Fallback to PhonePe
                "net.one97.paytm"                           // Fallback to Paytm
            )
            
            platform.contains("Paytm", ignoreCase = true) -> listOf("net.one97.paytm")
            platform.contains("WhatsApp", ignoreCase = true) -> listOf("com.whatsapp")
            platform.contains("Amazon Pay", ignoreCase = true) -> listOf("in.amazon.mShop.android.shopping")
            platform.contains("BHIM", ignoreCase = true) -> listOf("in.org.npci.upiapp")
            
            else -> listOf(
                "com.google.android.apps.nbu.paisa.user",
                "com.phonepe.app",
                "net.one97.paytm"
            )
        }
    }
    
    /**
     * Check if app is installed
     */
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Show message when app is not installed
     */
    private fun showNotInstalledMessage(context: Context, platform: String) {
        Toast.makeText(
            context,
            "$platform app not found. Please install a UPI app.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
