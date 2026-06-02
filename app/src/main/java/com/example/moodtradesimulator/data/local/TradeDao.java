package com.example.moodtradesimulator.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TradeDao {

    @Insert
    void insertTrade(TradeEntity trade);

    @Query("SELECT * FROM trades ORDER BY id ASC")
    List<TradeEntity> getAllTrades();

    @Query("DELETE FROM trades")
    void deleteAllTrades();
}
