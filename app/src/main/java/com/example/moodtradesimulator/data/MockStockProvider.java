package com.example.moodtradesimulator.data;

import com.example.moodtradesimulator.model.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockStockProvider {

    private static final List<Stock> STOCKS = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        STOCKS.add(new Stock("AAPL", "Apple Inc.", 163.22));
        STOCKS.add(new Stock("TSLA", "Tesla", 217.44));
        STOCKS.add(new Stock("MSFT", "Microsoft", 300.12));
        STOCKS.add(new Stock("AMZN", "Amazon", 115.90));
        STOCKS.add(new Stock("NVDA", "Nvidia", 470.30));
        STOCKS.add(new Stock("GOOGL", "Google", 132.55));
        STOCKS.add(new Stock("META", "Meta", 328.10));
        STOCKS.add(new Stock("NFLX", "Netflix", 421.85));
        STOCKS.add(new Stock("BRK.B", "Berkshire Hathaway", 365.40));
        STOCKS.add(new Stock("AMD", "AMD", 145.70));
    }

    public static List<Stock> getStocks() {
        return STOCKS;
    }

    public static void refreshPrices() {
        for (Stock stock : STOCKS) {
            // Random percent between -5% and +5%
            double changePercent = (RANDOM.nextDouble() * 10.0) - 5.0;
            double multiplier = 1.0 + (changePercent / 100.0);
            stock.price = stock.price * multiplier;

            // Keep prices positive in case of repeated drops
            if (stock.price < 1.0) {
                stock.price = 1.0;
            }
        }
    }
}
