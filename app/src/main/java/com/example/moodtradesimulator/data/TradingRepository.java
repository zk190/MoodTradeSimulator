package com.example.moodtradesimulator.data;

import android.content.Context;

import com.example.moodtradesimulator.data.local.AppDatabase;
import com.example.moodtradesimulator.data.local.PortfolioStateEntity;
import com.example.moodtradesimulator.data.local.RoomMapper;
import com.example.moodtradesimulator.data.local.TradeEntity;
import com.example.moodtradesimulator.logic.TradingEngine;
import com.example.moodtradesimulator.model.Trade;

import java.util.List;

public class TradingRepository {

    private static final TradingEngine ENGINE = new PersistentTradingEngine();
    private static AppDatabase database;
    private static boolean initialized = false;

    public static void init(Context context) {
        if (initialized) {
            return;
        }

        database = AppDatabase.getInstance(context.getApplicationContext());
        loadStateFromDatabase();
        initialized = true;
    }

    public static TradingEngine getEngine() {
        return ENGINE;
    }

    public static void persistState() {
        if (!initialized || database == null) {
            return;
        }

        database.portfolioStateDao().insertOrReplace(
                RoomMapper.toPortfolioStateEntity(ENGINE.getPortfolio())
        );

        database.tradeDao().deleteAllTrades();
        for (Trade trade : ENGINE.getTradeHistory()) {
            database.tradeDao().insertTrade(RoomMapper.toTradeEntity(trade));
        }
    }

    public static void resetPortfolio() {
        if (!initialized || database == null) {
            return;
        }

        ENGINE.getPortfolio().cash = 10000.0;
        ENGINE.getPortfolio().holdings.clear();
        ENGINE.getPortfolio().averageBuyPrices.clear();
        ENGINE.getTradeHistory().clear();

        database.tradeDao().deleteAllTrades();
        database.portfolioStateDao().clearPortfolioState();
        persistState();
    }

    private static void loadStateFromDatabase() {
        PortfolioStateEntity state = database.portfolioStateDao().getPortfolioState();
        if (state != null) {
            RoomMapper.applyPortfolioState(ENGINE.getPortfolio(), state);
        }

        List<TradeEntity> storedTrades = database.tradeDao().getAllTrades();
        ENGINE.getTradeHistory().clear();
        for (TradeEntity entity : storedTrades) {
            ENGINE.getTradeHistory().add(RoomMapper.toTrade(entity));
        }
    }

    private static class PersistentTradingEngine extends TradingEngine {

        @Override
        public void buy(String symbol, int qty, double price) {
            super.buy(symbol, qty, price);
            TradingRepository.persistState();
        }

        @Override
        public void sell(String symbol, int qty, double price) {
            super.sell(symbol, qty, price);
            TradingRepository.persistState();
        }
    }
}
