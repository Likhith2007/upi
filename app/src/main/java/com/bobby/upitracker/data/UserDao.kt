package com.bobby.upitracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<User?>
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUserOnce(): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()
    
    @Query("UPDATE users SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun loginUser(userId: Long)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)
}
