package com.example.moodtradesimulator.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "portfolio_state")
public class PortfolioStateEntity {

    @PrimaryKey
    public int id = 1;

    public double cash;

    @NonNull
    public String holdingsJson = "{}";

    @NonNull
    public String averageBuyPricesJson = "{}";
}
