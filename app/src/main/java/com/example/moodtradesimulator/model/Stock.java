package com.example.moodtradesimulator.model;

public class Stock {
    public String symbol;
    public String name;
    public double price;

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }
}
