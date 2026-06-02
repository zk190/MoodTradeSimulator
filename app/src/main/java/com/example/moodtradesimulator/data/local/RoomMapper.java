package com.example.moodtradesimulator.data.local;

import com.example.moodtradesimulator.model.Portfolio;
import com.example.moodtradesimulator.model.Trade;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class RoomMapper {

    private static final Gson GSON = new Gson();

    private static final Type HOLDINGS_TYPE = new TypeToken<HashMap<String, Integer>>() {
    }.getType();

    private static final Type AVERAGE_PRICES_TYPE = new TypeToken<HashMap<String, Double>>() {
    }.getType();

    public static TradeEntity toTradeEntity(Trade trade) {
        TradeEntity entity = new TradeEntity();
        entity.symbol = trade.getSymbol();
        entity.qty = trade.getQuantity();
        entity.isBuy = "BUY".equalsIgnoreCase(trade.getType());
        entity.price = trade.getPrice();
        entity.timestamp = trade.getTimestamp();
        entity.moodAtTrade = trade.getMoodAtTrade();
        entity.profitLoss = trade.getProfitLoss();
        return entity;
    }

    public static Trade toTrade(TradeEntity entity) {
        String type = entity.isBuy ? "BUY" : "SELL";
        return new Trade(
                entity.symbol,
                type,
                entity.qty,
                entity.price,
                entity.timestamp,
                entity.moodAtTrade,
                entity.profitLoss
        );
    }

    public static PortfolioStateEntity toPortfolioStateEntity(Portfolio portfolio) {
        PortfolioStateEntity state = new PortfolioStateEntity();
        state.id = 1;
        state.cash = portfolio.cash;
        state.holdingsJson = GSON.toJson(portfolio.holdings);
        state.averageBuyPricesJson = GSON.toJson(portfolio.averageBuyPrices);
        return state;
    }

    public static void applyPortfolioState(Portfolio portfolio, PortfolioStateEntity state) {
        portfolio.cash = state.cash;

        HashMap<String, Integer> holdings = GSON.fromJson(state.holdingsJson, HOLDINGS_TYPE);
        HashMap<String, Double> averageBuyPrices = GSON.fromJson(state.averageBuyPricesJson, AVERAGE_PRICES_TYPE);

        portfolio.holdings.clear();
        if (holdings != null) {
            portfolio.holdings.putAll(holdings);
        }

        portfolio.averageBuyPrices.clear();
        if (averageBuyPrices != null) {
            portfolio.averageBuyPrices.putAll(averageBuyPrices);
        }
    }
}
