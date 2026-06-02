package com.example.moodtradesimulator.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {TradeEntity.class, PortfolioStateEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE trades ADD COLUMN price REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE trades ADD COLUMN moodAtTrade TEXT");
            database.execSQL("ALTER TABLE trades ADD COLUMN profitLoss REAL NOT NULL DEFAULT 0.0");
        }
    };

    public abstract TradeDao tradeDao();

    public abstract PortfolioStateDao portfolioStateDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mood_trade_simulator.db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }
}
