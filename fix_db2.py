with open('app/src/main/java/com/example/data/DatabaseProvider.kt', 'r') as f:
    content = f.read()

content = content.replace("""        return Room.databaseBuilder(
            context.applicationContext,
            FinanceDatabase::class.java,
            "finance_tracker_v2.db"
        )
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT " + System.currentTimeMillis())
                db.execSQL("ALTER TABLE categories ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT " + System.currentTimeMillis())
                db.execSQL("ALTER TABLE savings_vault ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT " + System.currentTimeMillis())
            }
        }

        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
        .build()""", """        val MIGRATION_5_6 = object : Migration(5, 6) {
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
        .build()""")

with open('app/src/main/java/com/example/data/DatabaseProvider.kt', 'w') as f:
    f.write(content)
