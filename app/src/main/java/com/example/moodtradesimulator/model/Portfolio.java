package com.example.moodtradesimulator.model;

import java.util.HashMap;

public class Portfolio {
    public HashMap<String, Integer> holdings = new HashMap<>();
    public HashMap<String, Double> averageBuyPrices = new HashMap<>();
    public double cash = 10000.0;
}
