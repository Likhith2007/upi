package com.bobby.upitracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UpiTransactionDao {
    @Insert
    suspend fun insert(transaction: UpiTransaction)
    
    @Insert
    suspend fun insertAll(transactions: List<UpiTransaction>)
    
    @Query("SELECT * FROM upi_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<UpiTransaction>>
    
    @Query("SELECT * FROM upi_transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<UpiTransaction>>
    
    @Query("SELECT SUM(amount) FROM upi_transactions WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalAmountByDateRange(startTime: Long, endTime: Long): Double?
    
    @Query("SELECT * FROM upi_transactions WHERE platform = :platform ORDER BY timestamp DESC")
    fun getTransactionsByPlatform(platform: String): Flow<List<UpiTransaction>>

    @Query("SELECT * FROM upi_transactions WHERE amount = :amount AND sender = :sender AND timestamp BETWEEN :timestamp - 5000 AND :timestamp + 5000 LIMIT 1")
    suspend fun findSimilarTransaction(amount: Double, sender: String, timestamp: Long): UpiTransaction?
    
    @Query("DELETE FROM upi_transactions")
    suspend fun deleteAll()
}
