package com.bobby.upitracker.payment

import com.bobby.upitracker.data.PaymentTransaction
import com.bobby.upitracker.data.PaymentTransactionDao
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val dao: PaymentTransactionDao) {
    
    val allPayments: Flow<List<PaymentTransaction>> = dao.getAllPayments()
    
    suspend fun insertPayment(payment: PaymentTransaction) {
        dao.insert(payment)
    }
    
    suspend fun updatePaymentStatus(orderId: String, status: String, paymentId: String?) {
        dao.updatePaymentStatus(orderId, status, paymentId)
    }
    
    fun getPaymentsByStatus(status: String): Flow<List<PaymentTransaction>> {
        return dao.getPaymentsByStatus(status)
    }
}
