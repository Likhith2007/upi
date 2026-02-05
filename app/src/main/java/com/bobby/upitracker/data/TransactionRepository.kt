package com.bobby.upitracker.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: UpiTransactionDao) {
    
    val allTransactions: Flow<List<UpiTransaction>> = dao.getAllTransactions()
    
    suspend fun insertTransaction(transaction: UpiTransaction) {
        dao.insert(transaction)
    }
    
    suspend fun insertTransactions(transactions: List<UpiTransaction>) {
        dao.insertAll(transactions)
    }
    
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<UpiTransaction>> {
        return dao.getTransactionsByDateRange(startTime, endTime)
    }
    
    suspend fun getTotalAmountByDateRange(startTime: Long, endTime: Long): Double {
        return dao.getTotalAmountByDateRange(startTime, endTime) ?: 0.0
    }
    
    fun getTransactionsByPlatform(platform: String): Flow<List<UpiTransaction>> {
        return dao.getTransactionsByPlatform(platform)
    }
    
    suspend fun findSimilarTransaction(amount: Double, sender: String, timestamp: Long): UpiTransaction? {
        return dao.findSimilarTransaction(amount, sender, timestamp)
    }
}
