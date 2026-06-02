package com.example.moodtradesimulator.logic;

import com.example.moodtradesimulator.model.Portfolio;
import com.example.moodtradesimulator.model.Trade;

import java.util.ArrayList;
import java.util.List;

public class TradingEngine {

    private Portfolio portfolio;
    private List<Trade> tradeHistory;

    public TradingEngine() {
        portfolio = new Portfolio();
        tradeHistory = new ArrayList<>();
    }

    public void buy(String symbol, int qty, double price) {
        int currentQty = portfolio.holdings.getOrDefault(symbol, 0);
        double currentAvgPrice = portfolio.averageBuyPrices.getOrDefault(symbol, 0.0);
        int newQty = currentQty + qty;

        // Weighted average buy price after adding new shares
        double totalCostBefore = currentQty * currentAvgPrice;
        double totalCostAfter = totalCostBefore + (qty * price);
        double newAvgPrice = totalCostAfter / newQty;

        portfolio.cash -= qty * price;
        portfolio.holdings.put(symbol, newQty);
        portfolio.averageBuyPrices.put(symbol, newAvgPrice);
        String moodAtTrade = EmotionDetectionEngine
                .detectMoodForTrade(tradeHistory, "BUY", 0.0)
                .name();
        tradeHistory.add(new Trade(
                symbol,
                "BUY",
                qty,
                price,
                System.currentTimeMillis(),
                moodAtTrade,
                0.0
        ));
    }

    public void sell(String symbol, int qty, double price) {
        int currentQty = portfolio.holdings.getOrDefault(symbol, 0);
        double averageBuyPrice = portfolio.averageBuyPrices.getOrDefault(symbol, 0.0);
        int newQty = currentQty - qty;

        portfolio.cash += qty * price;
        if (newQty > 0) {
            portfolio.holdings.put(symbol, newQty);
        } else {
            portfolio.holdings.remove(symbol);
            portfolio.averageBuyPrices.remove(symbol);
        }

        double profitLoss = (price - averageBuyPrice) * qty;
        String moodAtTrade = EmotionDetectionEngine
                .detectMoodForTrade(tradeHistory, "SELL", profitLoss)
                .name();
        tradeHistory.add(new Trade(
                symbol,
                "SELL",
                qty,
                price,
                System.currentTimeMillis(),
                moodAtTrade,
                profitLoss
        ));
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public List<Trade> getTradeHistory() {
        return tradeHistory;
    }
}
