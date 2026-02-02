package com.bobby.upitracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobby.upitracker.data.UpiTransaction
import com.bobby.upitracker.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val transactions: List<UpiTransaction> = emptyList(),
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val platformBreakdown: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val hasSmsPermission: Boolean = false
)

class HomeViewModel(
    private val repository: TransactionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.allTransactions.collect { transactions ->
                val now = System.currentTimeMillis()
                val todayStart = getStartOfDay(now)
                val weekStart = now - (7 * 24 * 60 * 60 * 1000)
                val monthStart = getStartOfMonth(now)
                
                val todayTotal = repository.getTotalAmountByDateRange(todayStart, now)
                val weekTotal = repository.getTotalAmountByDateRange(weekStart, now)
                val monthTotal = repository.getTotalAmountByDateRange(monthStart, now)
                
                val platformBreakdown = transactions.groupBy { it.platform }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                
                _uiState.value = _uiState.value.copy(
                    transactions = transactions,
                    todayTotal = todayTotal,
                    weekTotal = weekTotal,
                    monthTotal = monthTotal,
                    platformBreakdown = platformBreakdown,
                    isLoading = false
                )
            }
        }
    }
    
    fun updateSmsPermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasSmsPermission = granted)
    }
    
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getStartOfMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
