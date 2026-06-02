package com.example.moodtradesimulator.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtradesimulator.R;
import com.example.moodtradesimulator.data.TradingRepository;
import com.example.moodtradesimulator.logic.TradingEngine;
import com.example.moodtradesimulator.model.Trade;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeHistoryActivity extends AppCompatActivity {

    private RecyclerView tradeHistoryRecycler;
    private TextView emptyTradeHistoryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_history);

        TradingRepository.init(this);
        tradeHistoryRecycler = findViewById(R.id.tradeHistoryRecycler);
        emptyTradeHistoryText = findViewById(R.id.emptyTradeHistoryText);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        tradeHistoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        MainBottomNavHelper.setup(this, bottomNavigationView, R.id.navigation_profile);

        TradingEngine engine = TradingRepository.getEngine();
        List<Trade> trades = new ArrayList<>(engine.getTradeHistory());
        Collections.reverse(trades);

        TradeHistoryAdapter adapter = new TradeHistoryAdapter(trades);
        tradeHistoryRecycler.setAdapter(adapter);

        if (trades.isEmpty()) {
            emptyTradeHistoryText.setVisibility(View.VISIBLE);
            tradeHistoryRecycler.setVisibility(View.GONE);
        } else {
            emptyTradeHistoryText.setVisibility(View.GONE);
            tradeHistoryRecycler.setVisibility(View.VISIBLE);
        }
    }
}
