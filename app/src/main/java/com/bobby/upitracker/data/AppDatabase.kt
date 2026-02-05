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
        SavedReceiver::class,
        BankTransaction::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun upiTransactionDao(): UpiTransactionDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao
    abstract fun userDao(): UserDao
    abstract fun savedReceiverDao(): SavedReceiverDao
    abstract fun bankTransactionDao(): BankTransactionDao
    
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
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create bank_transactions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS bank_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        balance REAL,
                        bankName TEXT NOT NULL,
                        accountNumber TEXT NOT NULL,
                        referenceNumber TEXT,
                        timestamp INTEGER NOT NULL,
                        description TEXT,
                        rawMessage TEXT NOT NULL
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
