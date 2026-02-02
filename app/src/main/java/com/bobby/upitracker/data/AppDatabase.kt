package com.bobby.upitracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UpiTransaction::class, 
        PaymentTransaction::class,
        User::class,
        SavedReceiver::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun upiTransactionDao(): UpiTransactionDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao
    abstract fun userDao(): UserDao
    abstract fun savedReceiverDao(): SavedReceiverDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create users table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        mobile TEXT NOT NULL,
                        email TEXT NOT NULL,
                        upiId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isLoggedIn INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create saved_receivers table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS saved_receivers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        identifier TEXT NOT NULL,
                        identifierType TEXT NOT NULL,
                        lastUsedAmount REAL NOT NULL,
                        lastUsedAt INTEGER NOT NULL,
                        isFavorite INTEGER NOT NULL,
                        fraudCheckStatus TEXT,
                        fraudCheckDate INTEGER
                    )
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "upi_tracker_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
