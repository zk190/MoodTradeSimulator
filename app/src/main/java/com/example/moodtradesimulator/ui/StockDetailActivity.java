package com.example.moodtradesimulator.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moodtradesimulator.R;
import com.example.moodtradesimulator.data.TradingRepository;
import com.example.moodtradesimulator.logic.EmotionDetectionEngine;
import com.example.moodtradesimulator.logic.MoodBarLogic;
import com.example.moodtradesimulator.logic.TradingEngine;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class StockDetailActivity extends AppCompatActivity {

    private static final long COOLDOWN_MS = 10_000L;

    private final Handler cooldownHandler = new Handler(Looper.getMainLooper());
    private final Runnable endCooldownRunnable = new Runnable() {
        @Override
        public void run() {
            if (buyButton != null) {
                buyButton.setEnabled(true);
            }
            if (sellButton != null) {
                sellButton.setEnabled(true);
            }
            if (cooldownMessageText != null) {
                cooldownMessageText.setVisibility(View.GONE);
            }
        }
    };

    private TextView ownedSharesText;
    private TextView averageBuyPriceText;
    private TextView positionProfitLossText;
    private TextView cooldownMessageText;
    private EditText quantityInput;
    private Button buyButton;
    private Button sellButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        TextView nameText = findViewById(R.id.detailNameText);
        TextView symbolText = findViewById(R.id.detailSymbolText);
        TextView priceText = findViewById(R.id.detailPriceText);
        ownedSharesText = findViewById(R.id.ownedSharesText);
        averageBuyPriceText = findViewById(R.id.averageBuyPriceText);
        positionProfitLossText = findViewById(R.id.positionProfitLossText);
        TextView trendText = findViewById(R.id.trendText);
        TextView trendSummaryText = findViewById(R.id.trendSummaryText);
        cooldownMessageText = findViewById(R.id.cooldownMessageText);
        LineChart stockChart = findViewById(R.id.stockChart);
        quantityInput = findViewById(R.id.quantityInput);
        buyButton = findViewById(R.id.buyButton);
        sellButton = findViewById(R.id.sellButton);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String symbol = intent.getStringExtra("symbol");
        double price = intent.getDoubleExtra("price", 0.0);

        nameText.setText(name);
        symbolText.setText(symbol);
        priceText.setText(String.format(Locale.US, "$%.2f", price));

        TradingEngine tradingEngine = TradingRepository.getEngine();
        refreshPositionSummary(tradingEngine, symbol, price);
        setupMockChart(stockChart, trendText, trendSummaryText, symbol, price);

        buyButton.setOnClickListener(v -> {
            Integer qty = parseValidQuantity(quantityInput);
            if (qty == null) {
                Toast.makeText(this, "Enter a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            runTradeWithMoodWarning(
                    tradingEngine,
                    "BUY",
                    estimatedMood -> {
                        tradingEngine.buy(symbol, qty, price);
                        Toast.makeText(this, "Bought " + qty + " " + symbol, Toast.LENGTH_SHORT).show();
                        handleTradeCompletion(estimatedMood, tradingEngine, symbol, price);
                    }
            );
        });

        sellButton.setOnClickListener(v -> {
            Integer qty = parseValidQuantity(quantityInput);
            if (qty == null) {
                Toast.makeText(this, "Enter a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int ownedQtyForSell = tradingEngine.getPortfolio().holdings.getOrDefault(symbol, 0);
            if (ownedQtyForSell <= 0 || qty > ownedQtyForSell) {
                Toast.makeText(this, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                return;
            }

            double averageBuyPrice = tradingEngine.getPortfolio()
                    .averageBuyPrices.getOrDefault(symbol, 0.0);
            double estimatedProfitLoss = (price - averageBuyPrice) * qty;

            runTradeWithMoodWarning(
                    tradingEngine,
                    "SELL",
                    estimatedMood -> {
                        tradingEngine.sell(symbol, qty, price);
                        Toast.makeText(this, "Sold " + qty + " " + symbol, Toast.LENGTH_SHORT).show();
                        handleTradeCompletion(estimatedMood, tradingEngine, symbol, price);
                    },
                    estimatedProfitLoss
            );
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cooldownHandler.removeCallbacks(endCooldownRunnable);
    }

    private Integer parseValidQuantity(EditText quantityInput) {
        String qtyText = quantityInput.getText().toString().trim();
        if (qtyText.isEmpty()) {
            return null;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            if (qty <= 0) {
                return null;
            }
            return qty;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void runTradeWithMoodWarning(TradingEngine tradingEngine,
                                         String tradeType,
                                         TradeAction tradeAction) {
        runTradeWithMoodWarning(tradingEngine, tradeType, tradeAction, 0.0);
    }

    private void runTradeWithMoodWarning(TradingEngine tradingEngine,
                                         String tradeType,
                                         TradeAction tradeAction,
                                         double estimatedProfitLoss) {
        MoodBarLogic.MoodState detectedMood = EmotionDetectionEngine.detectMoodForTrade(
                tradingEngine.getTradeHistory(),
                tradeType,
                estimatedProfitLoss
        );

        if (requiresWarning(detectedMood)) {
            new AlertDialog.Builder(this)
                    .setTitle("Emotional Trading Warning")
                    .setMessage(getWarningMessage(detectedMood))
                    .setPositiveButton("Continue Trade", (dialog, which) -> tradeAction.run(detectedMood))
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        tradeAction.run(detectedMood);
    }

    private void handleTradeCompletion(MoodBarLogic.MoodState mood,
                                       TradingEngine tradingEngine,
                                       String symbol,
                                       double price) {
        if (requiresWarning(mood)) {
            refreshPositionSummary(tradingEngine, symbol, price);
            quantityInput.setText("");
            startCooldown();
            return;
        }

        finish();
    }

    private void startCooldown() {
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
        cooldownMessageText.setText("Cooldown active: take a moment before making another trade.");
        cooldownMessageText.setVisibility(View.VISIBLE);

        cooldownHandler.removeCallbacks(endCooldownRunnable);
        cooldownHandler.postDelayed(endCooldownRunnable, COOLDOWN_MS);
    }

    private void refreshPositionSummary(TradingEngine tradingEngine, String symbol, double price) {
        int ownedQty = tradingEngine.getPortfolio().holdings.getOrDefault(symbol, 0);
        ownedSharesText.setText("Owned Shares: " + ownedQty);

        if (ownedQty > 0) {
            double averageBuyPrice = tradingEngine.getPortfolio()
                    .averageBuyPrices.getOrDefault(symbol, 0.0);
            averageBuyPriceText.setText("Average Buy Price: " + String.format(Locale.US, "$%.2f", averageBuyPrice));

            double positionProfitLoss = (price - averageBuyPrice) * ownedQty;
            if (positionProfitLoss > 0) {
                positionProfitLossText.setText("Position P/L: +" + String.format(Locale.US, "$%.2f", positionProfitLoss));
                positionProfitLossText.setTextColor(Color.GREEN);
            } else if (positionProfitLoss < 0) {
                positionProfitLossText.setText("Position P/L: -" + String.format(Locale.US, "$%.2f", Math.abs(positionProfitLoss)));
                positionProfitLossText.setTextColor(Color.RED);
            } else {
                positionProfitLossText.setText("Position P/L: " + String.format(Locale.US, "$%.2f", 0.0));
                positionProfitLossText.setTextColor(Color.BLACK);
            }
        } else {
            averageBuyPriceText.setText("Average Buy Price: N/A");
            positionProfitLossText.setText("Position P/L: N/A");
            positionProfitLossText.setTextColor(Color.BLACK);
        }
    }

    private boolean requiresWarning(MoodBarLogic.MoodState mood) {
        return mood == MoodBarLogic.MoodState.IMPULSIVE
                || mood == MoodBarLogic.MoodState.FOMO
                || mood == MoodBarLogic.MoodState.STRESSED
                || mood == MoodBarLogic.MoodState.FEARFUL;
    }

    private String getWarningMessage(MoodBarLogic.MoodState mood) {
        if (mood == MoodBarLogic.MoodState.IMPULSIVE) {
            return "You are trading very quickly. This may be impulsive.";
        }
        if (mood == MoodBarLogic.MoodState.FOMO) {
            return "You may be chasing the market (FOMO).";
        }
        if (mood == MoodBarLogic.MoodState.STRESSED) {
            return "You are trading after losses. This may be emotional.";
        }
        if (mood == MoodBarLogic.MoodState.FEARFUL) {
            return "You may be reacting out of fear.";
        }
        return "";
    }

    // TODO: Replace mock chart data with real historical market data in a future version.
    private void setupMockChart(LineChart stockChart, TextView trendText, TextView trendSummaryText, String symbol, double currentPrice) {
        float base = (float) currentPrice;
        Random random = new Random(symbol == null ? 0 : symbol.hashCode());

        List<Entry> entries = new ArrayList<>();
        float pointPrice = base;
        entries.add(new Entry(0, pointPrice));

        // Build 7 points with small seeded random moves (about +/-2% per step)
        for (int i = 1; i < 7; i++) {
            float percentMove = (random.nextFloat() * 0.04f) - 0.02f;
            pointPrice = pointPrice * (1f + percentMove);
            entries.add(new Entry(i, pointPrice));
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        float firstPoint = entries.get(0).getY();
        float lastPoint = entries.get(entries.size() - 1).getY();
        if (lastPoint >= firstPoint) {
            dataSet.setColor(Color.GREEN);
            dataSet.setCircleColor(Color.GREEN);
        } else {
            dataSet.setColor(Color.RED);
            dataSet.setCircleColor(Color.RED);
        }

        double trendPercent = 0.0;
        if (firstPoint != 0f) {
            trendPercent = ((lastPoint - firstPoint) / firstPoint) * 100.0;
        }

        if (trendPercent > 0) {
            trendText.setText(String.format(Locale.US, "+%.1f%% recent trend", trendPercent));
            trendText.setTextColor(Color.GREEN);
        } else if (trendPercent < 0) {
            trendText.setText(String.format(Locale.US, "%.1f%% recent trend", trendPercent));
            trendText.setTextColor(Color.RED);
        } else {
            trendText.setText(String.format(Locale.US, "%.1f%% recent trend", trendPercent));
            trendText.setTextColor(Color.BLACK);
        }

        if (trendPercent > 0.5) {
            trendSummaryText.setText("Trend: Upward");
            trendSummaryText.setTextColor(Color.GREEN);
        } else if (trendPercent < -0.5) {
            trendSummaryText.setText("Trend: Downward");
            trendSummaryText.setTextColor(Color.RED);
        } else {
            trendSummaryText.setText("Trend: Stable");
            trendSummaryText.setTextColor(Color.BLACK);
        }

        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        stockChart.setData(lineData);

        Description description = new Description();
        description.setText("");
        stockChart.setDescription(description);
        stockChart.getAxisRight().setEnabled(false);
        stockChart.getLegend().setEnabled(false);
        stockChart.getXAxis().setDrawGridLines(false);
        stockChart.getAxisLeft().setDrawGridLines(false);
        stockChart.animateX(500);
        stockChart.invalidate();
    }

    private interface TradeAction {
        void run(MoodBarLogic.MoodState mood);
    }
}
