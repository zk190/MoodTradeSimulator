package com.example.moodtradesimulator.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trades")
public class Trade {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String symbol;
    private String type; // BUY or SELL
    private int quantity;
    private double price;
    private long timestamp;
    private String moodAtTrade;
    private double profitLoss;

    public Trade(String symbol, String type, int quantity, double price, long timestamp, String moodAtTrade, double profitLoss) {
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
        this.moodAtTrade = moodAtTrade;
        this.profitLoss = profitLoss;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMoodAtTrade() {
        return moodAtTrade;
    }

    public void setMoodAtTrade(String moodAtTrade) {
        this.moodAtTrade = moodAtTrade;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(double profitLoss) {
        this.profitLoss = profitLoss;
    }
}
