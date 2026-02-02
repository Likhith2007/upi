package com.bobby.upitracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentTransaction)
    
    @Query("SELECT * FROM payment_transactions ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentTransaction>>
    
    @Query("SELECT * FROM payment_transactions WHERE status = :status ORDER BY timestamp DESC")
    fun getPaymentsByStatus(status: String): Flow<List<PaymentTransaction>>
    
    @Query("UPDATE payment_transactions SET status = :status, razorpayPaymentId = :paymentId WHERE razorpayOrderId = :orderId")
    suspend fun updatePaymentStatus(orderId: String, status: String, paymentId: String?)
}
