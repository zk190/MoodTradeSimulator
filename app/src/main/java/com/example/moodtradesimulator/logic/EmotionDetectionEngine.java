package com.example.moodtradesimulator.logic;

import com.example.moodtradesimulator.model.Trade;

import java.util.List;

public class EmotionDetectionEngine {

    private static final long SHORT_WINDOW_MS = 10 * 60 * 1000;
    private static final long IMPULSIVE_GAP_MS = 2 * 60 * 1000;

    public static MoodBarLogic.MoodState detectMoodForTrade(List<Trade> tradeHistory,
                                                            String newTradeType,
                                                            double newTradeProfitLoss) {
        long now = System.currentTimeMillis();
        int recentTrades = 0;
        int recentBuys = 0;
        int recentSells = 0;
        int recentNegativeTrades = 0;

        for (Trade trade : tradeHistory) {
            if (now - trade.getTimestamp() <= SHORT_WINDOW_MS) {
                recentTrades++;

                if ("BUY".equalsIgnoreCase(trade.getType())) {
                    recentBuys++;
                } else if ("SELL".equalsIgnoreCase(trade.getType())) {
                    recentSells++;
                }

                if (trade.getProfitLoss() < 0) {
                    recentNegativeTrades++;
                }
            }
        }

        recentTrades++;
        if ("BUY".equalsIgnoreCase(newTradeType)) {
            recentBuys++;
        } else if ("SELL".equalsIgnoreCase(newTradeType)) {
            recentSells++;
        }

        if (newTradeProfitLoss < 0) {
            recentNegativeTrades++;
        }

        if (recentTrades >= 5 && recentBuys >= 3) {
            return MoodBarLogic.MoodState.FOMO;
        }

        if (recentTrades >= 5 && recentSells >= 3) {
            return MoodBarLogic.MoodState.FEARFUL;
        }

        if ("SELL".equalsIgnoreCase(newTradeType) && recentNegativeTrades >= 2) {
            return MoodBarLogic.MoodState.STRESSED;
        }

        if (isImpulsiveTrade(tradeHistory, now) || recentTrades >= 3) {
            return MoodBarLogic.MoodState.IMPULSIVE;
        }

        if ("BUY".equalsIgnoreCase(newTradeType) && recentNegativeTrades == 0) {
            return MoodBarLogic.MoodState.CONFIDENT;
        }

        return MoodBarLogic.MoodState.CALM;
    }

    public static MoodBarLogic.MoodState detectCurrentMood(List<Trade> tradeHistory) {
        if (tradeHistory == null || tradeHistory.isEmpty()) {
            return MoodBarLogic.MoodState.CALM;
        }

        Trade latestTrade = tradeHistory.get(tradeHistory.size() - 1);
        return detectMoodForTrade(
                tradeHistory.subList(0, tradeHistory.size() - 1),
                latestTrade.getType(),
                latestTrade.getProfitLoss()
        );
    }

    private static boolean isImpulsiveTrade(List<Trade> tradeHistory, long now) {
        if (tradeHistory.isEmpty()) {
            return false;
        }

        Trade latestTrade = tradeHistory.get(tradeHistory.size() - 1);
        return now - latestTrade.getTimestamp() <= IMPULSIVE_GAP_MS;
    }
}
