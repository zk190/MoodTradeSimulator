package com.example.moodtradesimulator.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trades")
public class TradeEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String symbol = "";

    public int qty;
    public boolean isBuy;
    public double price;
    public long timestamp;
    public String moodAtTrade;
    public double profitLoss;
}
