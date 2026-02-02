package com.bobby.upitracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedReceiverDao {
    @Query("SELECT * FROM saved_receivers ORDER BY lastUsedAt DESC")
    fun getAllReceivers(): Flow<List<SavedReceiver>>
    
    @Query("SELECT * FROM saved_receivers WHERE isFavorite = 1 ORDER BY lastUsedAt DESC")
    fun getFavoriteReceivers(): Flow<List<SavedReceiver>>
    
    @Query("SELECT * FROM saved_receivers WHERE identifier = :identifier LIMIT 1")
    suspend fun getReceiverByIdentifier(identifier: String): SavedReceiver?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiver(receiver: SavedReceiver): Long
    
    @Update
    suspend fun updateReceiver(receiver: SavedReceiver)
    
    @Query("UPDATE saved_receivers SET lastUsedAt = :timestamp, lastUsedAmount = :amount WHERE identifier = :identifier")
    suspend fun updateLastUsed(identifier: String, amount: Double, timestamp: Long)
    
    @Query("UPDATE saved_receivers SET fraudCheckStatus = :status, fraudCheckDate = :date WHERE identifier = :identifier")
    suspend fun updateFraudStatus(identifier: String, status: String, date: Long)
    
    @Query("DELETE FROM saved_receivers WHERE id = :id")
    suspend fun deleteReceiver(id: Long)
}
