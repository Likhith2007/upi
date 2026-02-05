package com.bobby.upitracker.data

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepository(private val context: Context) {
    
    /**
     * Get contact photo URI by phone number
     * Returns null if contact not found or no photo available
     */
    suspend fun getContactPhotoUri(phoneNumber: String): Uri? = withContext(Dispatchers.IO) {
        try {
            // Clean phone number (remove spaces, dashes, etc.)
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            
            // Query contacts by phone number
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(cleanNumber)
            )
            
            val projection = arrayOf(
                ContactsContract.PhoneLookup.PHOTO_URI,
                ContactsContract.PhoneLookup.DISPLAY_NAME
            )
            
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val photoUriString = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI)
                    )
                    return@withContext photoUriString?.let { Uri.parse(it) }
                }
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get contact name by phone number
     * Returns null if contact not found
     */
    suspend fun getContactName(phoneNumber: String): String? = withContext(Dispatchers.IO) {
        try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(cleanNumber)
            )
            
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return@withContext cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    )
                }
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Check if contact exists for phone number
     */
    suspend fun hasContact(phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        getContactName(phoneNumber) != null
    }
}
