package com.example.moodtradesimulator.model;

public class HoldingItem {
    public String symbol;
    public int quantity;
    public double averageBuyPrice;
    public double currentPrice;
    public double holdingValue;
    public double profitLoss;

    public HoldingItem(
            String symbol,
            int quantity,
            double averageBuyPrice,
            double currentPrice,
            double holdingValue,
            double profitLoss
    ) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
        this.currentPrice = currentPrice;
        this.holdingValue = holdingValue;
        this.profitLoss = profitLoss;
    }
}
