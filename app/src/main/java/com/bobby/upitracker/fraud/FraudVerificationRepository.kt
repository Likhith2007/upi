package com.bobby.upitracker.fraud

class FraudVerificationRepository {
    
    private val verificationCache = mutableMapOf<String, VerificationResult>()
    
    fun getCachedResult(mobileNumber: String): VerificationResult? {
        val cached = verificationCache[mobileNumber]
        // Cache valid for 24 hours
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 24 * 60 * 60 * 1000) {
            return cached
        }
        return null
    }
    
    fun cacheResult(mobileNumber: String, result: VerificationResult) {
        verificationCache[mobileNumber] = result
    }
    
    fun clearCache() {
        verificationCache.clear()
    }
}
