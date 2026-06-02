package com.example.moodtradesimulator.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moodtradesimulator.R;
import com.example.moodtradesimulator.data.TradingRepository;
import com.example.moodtradesimulator.logic.EmotionInsightEngine;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class InsightsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        TradingRepository.init(this);

        LinearLayout moodBarsContainer = findViewById(R.id.moodBarsContainer);
        TextView sessionTotalTradesText = findViewById(R.id.sessionTotalTradesText);
        TextView sessionMostCommonMoodText = findViewById(R.id.sessionMostCommonMoodText);
        TextView sessionTotalProfitLossText = findViewById(R.id.sessionTotalProfitLossText);
        TextView sessionDisciplineScoreText = findViewById(R.id.sessionDisciplineScoreText);
        TextView sessionSummaryMessageText = findViewById(R.id.sessionSummaryMessageText);
        TextView disciplineScoreText = findViewById(R.id.disciplineScoreText);
        TextView disciplineScoreLabelText = findViewById(R.id.disciplineScoreLabelText);
        TextView moodBarsTitleText = findViewById(R.id.moodBarsTitleText);
        LinearLayout moodBarsSectionContent = findViewById(R.id.moodBarsSectionContent);
        TextView profitLossTitleText = findViewById(R.id.insightProfitLossTitle);
        LinearLayout profitLossSectionContent = findViewById(R.id.profitLossSectionContent);
        TextView messagesTitleText = findViewById(R.id.insightMessagesTitle);
        LinearLayout messagesSectionContent = findViewById(R.id.messagesSectionContent);
        TextView improveTitleText = findViewById(R.id.howToImproveTitle);
        LinearLayout improveSectionContent = findViewById(R.id.improveSectionContent);
        TextView profitLossText = findViewById(R.id.insightProfitLossText);
        TextView messagesText = findViewById(R.id.insightMessagesText);
        TextView improveText = findViewById(R.id.howToImproveText);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        EmotionInsightEngine.InsightResult result =
                EmotionInsightEngine.analyze(TradingRepository.getEngine().getTradeHistory());

        MainBottomNavHelper.setup(this, bottomNavigationView, R.id.navigation_insights);
        sessionTotalTradesText.setText("Total Trades: " + result.totalTrades);
        sessionMostCommonMoodText.setText("Most Common Mood: " + result.mostCommonMood);
        sessionTotalProfitLossText.setText("Total Profit/Loss: " + formatMoney(result.totalProfitLoss));
        sessionDisciplineScoreText.setText("Current Discipline Score: " + result.disciplineScore + " / 100");
        sessionSummaryMessageText.setText(result.sessionSummaryMessage);
        disciplineScoreText.setText("Discipline Score: " + result.disciplineScore + " / 100");
        disciplineScoreLabelText.setText(getDisciplineLabel(result.disciplineScore));
        buildMoodBars(moodBarsContainer, result);
        profitLossText.setText(buildProfitLossText(result));
        messagesText.setText(buildMessagesText(result));
        improveText.setText(buildHowToImproveText(result));

        setupCollapsibleSection(moodBarsTitleText, moodBarsSectionContent, "Mood Bars");
        setupCollapsibleSection(profitLossTitleText, profitLossSectionContent, "Profit/Loss by Mood");
        setupCollapsibleSection(messagesTitleText, messagesSectionContent, "Insight Messages");
        setupCollapsibleSection(improveTitleText, improveSectionContent, "How to Improve");
    }

    private void buildMoodBars(LinearLayout container, EmotionInsightEngine.InsightResult result) {
        container.removeAllViews();

        if (result.tradeCountByMood.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No mood activity to show yet.");
            container.addView(emptyText);
            return;
        }

        List<String> moods = new ArrayList<>(result.tradeCountByMood.keySet());
        Collections.sort(moods);

        int maxCount = 0;
        for (String mood : moods) {
            int count = result.tradeCountByMood.get(mood);
            if (count > maxCount) {
                maxCount = count;
            }
        }

        int maxBarWidth = dpToPx(240);
        for (String mood : moods) {
            int count = result.tradeCountByMood.get(mood);

            TextView label = new TextView(this);
            label.setText(mood + ": " + count + " trades");
            label.setTextSize(14f);
            container.addView(label);

            FrameLayout barTrack = new FrameLayout(this);
            LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(
                    maxBarWidth,
                    dpToPx(16)
            );
            trackParams.bottomMargin = dpToPx(12);
            barTrack.setLayoutParams(trackParams);
            barTrack.setBackgroundColor(Color.parseColor("#DDDDDD"));

            View barFill = new View(this);
            int barWidth = maxCount == 0
                    ? 0
                    : (int) (((double) count / maxCount) * maxBarWidth);
            FrameLayout.LayoutParams fillParams = new FrameLayout.LayoutParams(
                    Math.max(barWidth, dpToPx(4)),
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            barFill.setLayoutParams(fillParams);
            barFill.setBackgroundColor(Color.parseColor("#3F51B5"));

            barTrack.addView(barFill);
            container.addView(barTrack);
        }
    }

    private String buildProfitLossText(EmotionInsightEngine.InsightResult result) {
        if (result.profitLossByMood.isEmpty()) {
            return "Nothing to show yet.";
        }

        StringBuilder builder = new StringBuilder();
        List<String> moods = new ArrayList<>(result.profitLossByMood.keySet());
        Collections.sort(moods);

        for (String mood : moods) {
            double value = result.profitLossByMood.get(mood);
            builder.append(mood)
                    .append(": ")
                    .append(formatMoney(value))
                    .append("\n");
        }

        return builder.toString().trim();
    }

    private String buildMessagesText(EmotionInsightEngine.InsightResult result) {
        if (result.insights.isEmpty()) {
            return "No clear patterns to highlight yet.";
        }

        StringBuilder builder = new StringBuilder();
        for (String insight : result.insights) {
            if (!insight.startsWith("How to improve:")) {
                builder.append("- ").append(insight).append("\n");
            }
        }

        String messageText = builder.toString().trim();
        return messageText.isEmpty() ? "No clear patterns to highlight yet." : messageText;
    }

    private String buildHowToImproveText(EmotionInsightEngine.InsightResult result) {
        StringBuilder builder = new StringBuilder();

        for (String insight : result.insights) {
            if (insight.startsWith("How to improve:")) {
                builder.append("- ")
                        .append(insight.replace("How to improve:", "").trim())
                        .append("\n");
            }
        }

        String improveText = builder.toString().trim();
        if (improveText.isEmpty()) {
            return "Suggestions will show up here once there is a bit more trading data.";
        }
        return improveText;
    }

    private String formatMoney(double value) {
        if (value > 0) {
            return String.format(Locale.US, "+$%.2f", value);
        }
        if (value < 0) {
            return String.format(Locale.US, "-$%.2f", Math.abs(value));
        }
        return String.format(Locale.US, "$%.2f", value);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    private String getDisciplineLabel(int score) {
        if (score >= 80) {
            return "Strong Discipline";
        }
        if (score >= 60) {
            return "Moderate Discipline";
        }
        return "High Emotional Risk";
    }

    private void setupCollapsibleSection(TextView header, View content, String title) {
        header.setOnClickListener(v -> {
            boolean isExpanded = content.getVisibility() == View.VISIBLE;
            content.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            header.setText(title + (isExpanded ? " ▼" : " ▲"));
        });
    }
}
