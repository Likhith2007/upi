package com.bobby.upitracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BankTransactionDao {
    
    @Query("SELECT * FROM bank_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<BankTransaction>>
    
    @Query("SELECT * FROM bank_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<BankTransaction>>
    
    @Query("SELECT * FROM bank_transactions WHERE bankName LIKE '%' || :bankName || '%' ORDER BY timestamp DESC")
    fun getTransactionsByBank(bankName: String): Flow<List<BankTransaction>>
    
    @Query("SELECT * FROM bank_transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<BankTransaction>>
    
    @Query("SELECT SUM(amount) FROM bank_transactions WHERE type = 'DEBIT' AND timestamp >= :startTime")
    suspend fun getTotalDebits(startTime: Long): Double?
    
    @Query("SELECT SUM(amount) FROM bank_transactions WHERE type = 'CREDIT' AND timestamp >= :startTime")
    suspend fun getTotalCredits(startTime: Long): Double?
    
    @Insert
    suspend fun insertTransaction(transaction: BankTransaction): Long
    
    @Query("DELETE FROM bank_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
    
    @Query("DELETE FROM bank_transactions")
    suspend fun deleteAllTransactions()
    
    @Query("SELECT * FROM bank_transactions WHERE referenceNumber = :refNumber LIMIT 1")
    suspend fun getTransactionByReference(refNumber: String): BankTransaction?
}
