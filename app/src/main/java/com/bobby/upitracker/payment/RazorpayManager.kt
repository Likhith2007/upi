package com.bobby.upitracker.payment

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class RazorpayManager(
    private val activity: Activity,
    private val apiKey: String
) {
    
    private val TAG = "RazorpayManager"
    
    init {
        try {
            Checkout.preload(activity.applicationContext)
            Log.d(TAG, "Razorpay preloaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading Razorpay", e)
        }
    }
    
    fun initiatePayment(
        amount: Double,
        receiverMobile: String,
        receiverName: String = "Receiver",
        listener: PaymentResultListener
    ) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(apiKey)
            
            // Generate a unique receipt ID (not order_id)
            val receiptId = "rcpt_${System.currentTimeMillis()}"
            
            val options = JSONObject().apply {
                // Basic details
                put("name", "UPI Money Tracker")
                put("description", "Payment to $receiverMobile")
                put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
                put("currency", "INR")
                put("amount", (amount * 100).toInt()) // Amount in paise
                
                // Receipt (optional, for tracking)
                put("receipt", receiptId)
                
                // Send SMS/Email notifications
                put("send_sms_hash", true)
                
                // Theme
                val theme = JSONObject()
                theme.put("color", "#3399cc")
                put("theme", theme)
                
                // Prefill customer details
                val prefill = JSONObject()
                prefill.put("contact", receiverMobile)
                prefill.put("name", receiverName)
                put("prefill", prefill)
                
                // Notes (optional metadata)
                val notes = JSONObject()
                notes.put("receiver", receiverMobile)
                notes.put("timestamp", System.currentTimeMillis())
                put("notes", notes)
                
                // Retry options
                val retry = JSONObject()
                retry.put("enabled", true)
                retry.put("max_count", 4)
                put("retry", retry)
                
                // Modal options - prevent accidental closure
                put("modal", JSONObject().apply {
                    put("backdropclose", false)
                    put("escape", false)
                    put("handleback", true)
                    put("confirm_close", true)
                    put("ondismiss", "function(){console.log('Payment dismissed');}")
                })
            }
            
            Log.d(TAG, "Opening Razorpay with amount: ₹$amount")
            Log.d(TAG, "Options: $options")
            
            checkout.open(activity, options)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating payment", e)
            e.printStackTrace()
            Toast.makeText(
                activity,
                "Payment error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
