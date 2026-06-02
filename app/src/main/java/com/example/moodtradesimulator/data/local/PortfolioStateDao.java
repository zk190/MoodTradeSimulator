package com.example.moodtradesimulator.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface PortfolioStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(PortfolioStateEntity state);

    @Query("SELECT * FROM portfolio_state WHERE id = 1")
    PortfolioStateEntity getPortfolioState();

    @Query("DELETE FROM portfolio_state")
    void clearPortfolioState();
}
