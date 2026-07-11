package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: FinanceDatabase? = null

    fun getDatabase(context: Context): FinanceDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = buildDatabase(context)
            INSTANCE = instance
            instance
        }
    }

    private fun buildDatabase(context: Context): FinanceDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) { override fun migrate(db: SupportSQLiteDatabase) {} }
        val MIGRATION_2_3 = object : Migration(2, 3) { override fun migrate(db: SupportSQLiteDatabase) {} }
        val MIGRATION_3_4 = object : Migration(3, 4) { override fun migrate(db: SupportSQLiteDatabase) {} }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE transactions SET uuid = lower(hex(randomblob(16))) WHERE uuid = ''")
                db.execSQL("ALTER TABLE categories ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE categories SET uuid = lower(hex(randomblob(16))) WHERE uuid = ''")
                db.execSQL("ALTER TABLE savings_vault ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE savings_vault SET uuid = lower(hex(randomblob(16))) WHERE uuid = ''")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT " + System.currentTimeMillis())
                db.execSQL("ALTER TABLE categories ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT " + System.currentTimeMillis())
                db.execSQL("ALTER TABLE savings_vault ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT " + System.currentTimeMillis())
            }
        }
        return Room.databaseBuilder(
            context.applicationContext,
            FinanceDatabase::class.java,
            "finance_tracker_v2.db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
        .build()
    }
}
