package com.example.moodtradesimulator.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtradesimulator.R;
import com.example.moodtradesimulator.data.MockStockProvider;
import com.example.moodtradesimulator.data.TradingRepository;
import com.example.moodtradesimulator.logic.MoodBarLogic;
import com.example.moodtradesimulator.logic.TradingEngine;
import com.example.moodtradesimulator.model.HoldingItem;
import com.example.moodtradesimulator.model.Stock;
import com.example.moodtradesimulator.model.Trade;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PortfolioActivity extends AppCompatActivity {

    private TradingEngine tradingEngine;
    private TextView moodText;
    private TextView moodDescriptionText;
    private TextView behaviourWarningText;
    private TextView balanceText;
    private TextView totalValueText;
    private TextView profitLossText;
    private TextView tradeCountText;
    private TextView emptyHoldingsText;

    private Button historyButton;

    private Button resetButton;
    private RecyclerView portfolioRecycler;
    private PortfolioAdapter portfolioAdapter;
    private final List<HoldingItem> holdingsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        TradingRepository.init(getApplicationContext());
        tradingEngine = TradingRepository.getEngine();

        moodText = findViewById(R.id.moodText);
        moodDescriptionText = findViewById(R.id.moodDescriptionText);
        behaviourWarningText = findViewById(R.id.behaviourWarningText);
        balanceText = findViewById(R.id.balanceText);
        totalValueText = findViewById(R.id.totalValueText);
        profitLossText = findViewById(R.id.profitLossText);
        tradeCountText = findViewById(R.id.tradeCountText);
        emptyHoldingsText = findViewById(R.id.emptyHoldingsText);
        //marketsButton = findViewById(R.id.marketsButton);
        historyButton = findViewById(R.id.historyButton);
       // viewInsightsButton = findViewById(R.id.viewInsightsButton);
        resetButton = findViewById(R.id.resetButton);
        portfolioRecycler = findViewById(R.id.portfolioRecycler);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        portfolioRecycler.setLayoutManager(new LinearLayoutManager(this));
        portfolioAdapter = new PortfolioAdapter(holdingsList);
        portfolioRecycler.setAdapter(portfolioAdapter);
        MainBottomNavHelper.setup(this, bottomNavigationView, R.id.navigation_portfolio);



        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(PortfolioActivity.this, TradeHistoryActivity.class);
            startActivity(intent);
        });


        resetButton.setOnClickListener(v -> new AlertDialog.Builder(PortfolioActivity.this)
                .setTitle("Reset Portfolio")
                .setMessage("This will clear your trades and holdings. Do you want to continue?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    TradingRepository.resetPortfolio();
                    refreshUI();
                })
                .setNegativeButton("Cancel", null)
                .show());

        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        MoodBarLogic.MoodState mood = getLatestMoodState();

        moodText.setText("Current Mood: " + mood.name());

        switch (mood) {
            case CALM:
                moodDescriptionText.setText("Your recent trading looks steady.");
                behaviourWarningText.setText("No warning signs right now.");
                behaviourWarningText.setTextColor(Color.BLACK);
                break;
            case CONFIDENT:
                moodDescriptionText.setText("You look more confident in your recent positions.");
                behaviourWarningText.setText("Confidence is building. Keep checking your reasons before each trade.");
                behaviourWarningText.setTextColor(Color.BLACK);
                break;
            case STRESSED:
                moodDescriptionText.setText("Recent selling suggests you're trading under pressure.");
                behaviourWarningText.setText("Warning: stress may be creeping into your decisions.");
                behaviourWarningText.setTextColor(Color.RED);
                break;
            case FOMO:
                moodDescriptionText.setText("A burst of buying suggests you may be chasing momentum.");
                behaviourWarningText.setText("Warning: this looks a bit like chasing the market.");
                behaviourWarningText.setTextColor(Color.RED);
                break;
            case IMPULSIVE:
                moodDescriptionText.setText("Your recent trading pace is quite fast.");
                behaviourWarningText.setText("Warning: slow down and give each trade a second look.");
                behaviourWarningText.setTextColor(Color.RED);
                break;
            case FEARFUL:
                moodDescriptionText.setText("Heavy selling suggests fear may be driving this session.");
                behaviourWarningText.setText("Warning: take a breath and reassess before making the next move.");
                behaviourWarningText.setTextColor(Color.RED);
                break;
            default:
                moodDescriptionText.setText("We do not have enough trading data to read your mood yet.");
                behaviourWarningText.setText("No warning signs right now.");
                behaviourWarningText.setTextColor(Color.BLACK);
                break;
        }

        double cashBalance = tradingEngine.getPortfolio().cash;

        balanceText.setText("Balance: " + String.format(Locale.US, "$%.2f", cashBalance));

        tradeCountText.setText("Trades: " + tradingEngine.getTradeHistory().size());

        holdingsList.clear();
        double totalHoldingsValue = 0.0;
        for (Map.Entry<String, Integer> entry : tradingEngine.getPortfolio().holdings.entrySet()) {
            int quantity = entry.getValue();
            if (quantity > 0) {
                String symbol = entry.getKey();
                double averageBuyPrice = tradingEngine.getPortfolio()
                        .averageBuyPrices.getOrDefault(symbol, 0.0);
                double currentPrice = 0.0;

                for (Stock stock : MockStockProvider.getStocks()) {
                    if (stock.symbol.equals(symbol)) {
                        currentPrice = stock.price;
                        break;
                    }
                }

                double holdingValue = quantity * currentPrice;
                double costBasis = quantity * averageBuyPrice;
                double holdingProfitLoss = holdingValue - costBasis;
                totalHoldingsValue += holdingValue;
                holdingsList.add(new HoldingItem(
                        symbol,
                        quantity,
                        averageBuyPrice,
                        currentPrice,
                        holdingValue,
                        holdingProfitLoss
                ));
            }
        }
        double totalPortfolioValue = cashBalance + totalHoldingsValue;
        double profitLoss = totalPortfolioValue - 10000.0;

        if (holdingsList.isEmpty()) {
            emptyHoldingsText.setVisibility(View.VISIBLE);
            portfolioRecycler.setVisibility(View.GONE);
        } else {
            emptyHoldingsText.setVisibility(View.GONE);
            portfolioRecycler.setVisibility(View.VISIBLE);
        }

        totalValueText.setText("Total Value: " + String.format(Locale.US, "$%.2f", totalPortfolioValue));

        String profitLossFormatted;
        if (profitLoss > 0) {
            profitLossFormatted = "+" + String.format(Locale.US, "$%.2f", profitLoss);
            profitLossText.setTextColor(Color.GREEN);
        } else if (profitLoss < 0) {
            profitLossFormatted = "-" + String.format(Locale.US, "$%.2f", Math.abs(profitLoss));
            profitLossText.setTextColor(Color.RED);
        } else {
            profitLossFormatted = String.format(Locale.US, "$%.2f", 0.0);
            profitLossText.setTextColor(Color.BLACK);
        }
        profitLossText.setText("Profit/Loss: " + profitLossFormatted);

        portfolioAdapter.notifyDataSetChanged();
    }

    private MoodBarLogic.MoodState getLatestMoodState() {
        List<Trade> tradeHistory = tradingEngine.getTradeHistory();
        if (tradeHistory == null || tradeHistory.isEmpty()) {
            return MoodBarLogic.MoodState.CALM;
        }

        Trade latestTrade = tradeHistory.get(tradeHistory.size() - 1);
        String moodAtTrade = latestTrade.getMoodAtTrade();

        if (moodAtTrade == null || moodAtTrade.trim().isEmpty()) {
            return MoodBarLogic.evaluate(tradeHistory);
        }

        try {
            return MoodBarLogic.MoodState.valueOf(moodAtTrade.trim().toUpperCase(Locale.US));
        } catch (IllegalArgumentException e) {
            return MoodBarLogic.evaluate(tradeHistory);
        }
    }
}
