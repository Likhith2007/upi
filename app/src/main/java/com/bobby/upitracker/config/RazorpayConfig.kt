package com.bobby.upitracker.config

import android.content.Context
import java.util.Properties

object RazorpayConfig {
    private var properties: Properties? = null
    
    fun initialize(context: Context) {
        if (properties == null) {
            properties = Properties()
            try {
                context.assets.open("razorpay.properties").use { inputStream ->
                    properties?.load(inputStream)
                }
            } catch (e: Exception) {
                // If file doesn't exist, use defaults
                properties?.setProperty("RAZORPAY_MODE", "test")
                properties?.setProperty("RAZORPAY_TEST_KEY", "rzp_test_SArUKaVk6n08y3")
            }
        }
    }
    
    fun getApiKey(context: Context): String {
        initialize(context)
        val mode = properties?.getProperty("RAZORPAY_MODE", "test") ?: "test"
        
        return if (mode == "live") {
            properties?.getProperty("RAZORPAY_LIVE_KEY") 
                ?: throw IllegalStateException("Live key not configured!")
        } else {
            properties?.getProperty("RAZORPAY_TEST_KEY", "rzp_test_SArUKaVk6n08y3")
                ?: "rzp_test_SArUKaVk6n08y3"
        }
    }
    
    fun isLiveMode(context: Context): Boolean {
        initialize(context)
        return properties?.getProperty("RAZORPAY_MODE", "test") == "live"
    }
    
    fun getMode(context: Context): String {
        initialize(context)
        return properties?.getProperty("RAZORPAY_MODE", "test") ?: "test"
    }
}
