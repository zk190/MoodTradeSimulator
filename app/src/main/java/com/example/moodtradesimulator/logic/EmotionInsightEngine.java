package com.example.moodtradesimulator.logic;

import com.example.moodtradesimulator.model.Trade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmotionInsightEngine {

    private static final long IMPULSIVE_WINDOW_MS = 2 * 60 * 1000;
    private static final int MAX_INSIGHTS = 4;

    private static final List<String> ALL_MOODS = Arrays.asList(
            "CALM",
            "CONFIDENT",
            "STRESSED",
            "FOMO",
            "IMPULSIVE",
            "FEARFUL"
    );

    public static class InsightResult {
        public Map<String, Double> profitLossByMood;
        public Map<String, Integer> tradeCountByMood;
        public List<String> insights;
        public int disciplineScore;
        public int totalTrades;
        public String mostCommonMood;
        public double totalProfitLoss;
        public String sessionSummaryMessage;

        public InsightResult() {
            profitLossByMood = new HashMap<>();
            tradeCountByMood = new HashMap<>();
            insights = new ArrayList<>();
            disciplineScore = 100;
            totalTrades = 0;
            mostCommonMood = "No trades yet";
            totalProfitLoss = 0.0;
            sessionSummaryMessage = "No trading activity yet.";
        }
    }

    private static class InsightCandidate {
        String message;
        int priority;

        InsightCandidate(String message, int priority) {
            this.message = message;
            this.priority = priority;
        }
    }

    public static InsightResult analyze(List<Trade> trades) {
        InsightResult result = new InsightResult();
        initializeMoodMaps(result);

        if (trades == null || trades.isEmpty()) {
            result.insights.add("No trades yet. Make a few trades and this page will start filling in.");
            return result;
        }

        for (Trade trade : trades) {
            String mood = normalizeMood(trade.getMoodAtTrade());
            result.profitLossByMood.put(mood, result.profitLossByMood.getOrDefault(mood, 0.0) + trade.getProfitLoss());
            result.tradeCountByMood.put(mood, result.tradeCountByMood.getOrDefault(mood, 0) + 1);
            result.totalProfitLoss += trade.getProfitLoss();
        }

        result.totalTrades = trades.size();
        result.disciplineScore = calculateDisciplineScore(result, trades);
        result.mostCommonMood = findMostCommonMood(result.tradeCountByMood);
        result.sessionSummaryMessage = buildSessionSummaryMessage(result);

        List<InsightCandidate> candidates = new ArrayList<>();
        addLossAversionInsight(candidates, result, trades);
        addFomoInsight(candidates, result);
        addImpulsiveTradingInsight(candidates, result, trades);
        addStressInsight(candidates, result);
        addPositiveInsight(candidates, result);

        candidates.sort((a, b) -> Integer.compare(b.priority, a.priority));

        for (InsightCandidate candidate : candidates) {
            if (result.insights.size() >= MAX_INSIGHTS) {
                break;
            }
            result.insights.add(candidate.message);
        }

        if (result.insights.isEmpty()) {
            result.insights.add("Your recent trading looks fairly balanced so far.");
        }

        return result;
    }

    private static void initializeMoodMaps(InsightResult result) {
        for (String mood : ALL_MOODS) {
            result.profitLossByMood.put(mood, 0.0);
            result.tradeCountByMood.put(mood, 0);
        }
    }

    private static String normalizeMood(String mood) {
        if (mood == null || mood.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return mood.trim().toUpperCase(Locale.US);
    }

    private static void addLossAversionInsight(List<InsightCandidate> candidates,
                                               InsightResult result,
                                               List<Trade> trades) {
        int stressedOrFearfulLosses = 0;
        Map<String, Integer> losingTradesByMood = new HashMap<>();

        for (Trade trade : trades) {
            String mood = normalizeMood(trade.getMoodAtTrade());
            if (trade.getProfitLoss() < 0) {
                losingTradesByMood.put(mood, losingTradesByMood.getOrDefault(mood, 0) + 1);
                if ("STRESSED".equals(mood) || "FEARFUL".equals(mood)) {
                    stressedOrFearfulLosses++;
                }
            }
        }

        boolean repeatedNegativeState = false;
        for (Map.Entry<String, Integer> entry : losingTradesByMood.entrySet()) {
            if (entry.getValue() >= 2
                    && ("STRESSED".equals(entry.getKey()) || "FEARFUL".equals(entry.getKey()))) {
                repeatedNegativeState = true;
                break;
            }
        }

        if (stressedOrFearfulLosses >= 2 || repeatedNegativeState) {
            candidates.add(new InsightCandidate(
                    "A few recent losses suggest you may be holding on emotionally instead of sticking to a plan.",
                    95
            ));
            candidates.add(new InsightCandidate(
                    "How to improve: Set exit rules before you enter the trade.",
                    94
            ));
        }
    }

    private static void addFomoInsight(List<InsightCandidate> candidates, InsightResult result) {
        double fomoProfit = result.profitLossByMood.get("FOMO");
        int fomoTrades = result.tradeCountByMood.get("FOMO");

        if (fomoProfit < 0 || (fomoTrades >= 3 && fomoProfit <= 0)) {
            candidates.add(new InsightCandidate(
                    "Your results dip when FOMO shows up, which points to reactive decision-making.",
                    90
            ));
            candidates.add(new InsightCandidate(
                    "How to improve: Wait for a clearer setup instead of chasing quick price moves.",
                    89
            ));
        }
    }

    private static void addImpulsiveTradingInsight(List<InsightCandidate> candidates,
                                                   InsightResult result,
                                                   List<Trade> trades) {
        if (trades.size() < 2) {
            return;
        }

        List<Long> timestamps = new ArrayList<>();
        for (Trade trade : trades) {
            timestamps.add(trade.getTimestamp());
        }
        timestamps.sort(Long::compareTo);

        int shortIntervals = 0;
        for (int i = 1; i < timestamps.size(); i++) {
            if (timestamps.get(i) - timestamps.get(i - 1) <= IMPULSIVE_WINDOW_MS) {
                shortIntervals++;
            }
        }

        if (shortIntervals >= 2 || result.tradeCountByMood.get("IMPULSIVE") >= 3) {
            candidates.add(new InsightCandidate(
                    "Several trades landed close together, which can be a sign of impulsive decision-making.",
                    85
            ));
            candidates.add(new InsightCandidate(
                    "How to improve: Give yourself a short pause between trades.",
                    84
            ));
        }
    }

    private static void addStressInsight(List<InsightCandidate> candidates, InsightResult result) {
        String worstMood = null;
        double worstPnl = Double.MAX_VALUE;

        for (String mood : ALL_MOODS) {
            double pnl = result.profitLossByMood.get(mood);
            if (pnl < worstPnl) {
                worstPnl = pnl;
                worstMood = mood;
            }
        }

        if ("STRESSED".equals(worstMood) && worstPnl < 0) {
            candidates.add(new InsightCandidate(
                    "Your trading results tend to slip when stress is high.",
                    80
            ));
            candidates.add(new InsightCandidate(
                    "How to improve: Step back when you feel under pressure.",
                    79
            ));
        }
    }

    private static void addPositiveInsight(List<InsightCandidate> candidates, InsightResult result) {
        String bestMood = null;
        double bestPnl = -Double.MAX_VALUE;

        for (String mood : ALL_MOODS) {
            double pnl = result.profitLossByMood.get(mood);
            if (pnl > bestPnl) {
                bestPnl = pnl;
                bestMood = mood;
            }
        }

        if (bestPnl <= 0 || bestMood == null) {
            return;
        }

        if ("CALM".equals(bestMood) || "CONFIDENT".equals(bestMood)) {
            candidates.add(new InsightCandidate(
                    "Your better trades usually happen when you feel calm or quietly confident.",
                    70
            ));
        }
    }

    private static int calculateDisciplineScore(InsightResult result, List<Trade> trades) {
        int score = 100;

        score -= result.tradeCountByMood.get("IMPULSIVE") * 6;
        score -= result.tradeCountByMood.get("FOMO") * 5;
        score -= result.tradeCountByMood.get("FEARFUL") * 5;
        score -= result.tradeCountByMood.get("STRESSED") * 4;

        int shortIntervals = countShortIntervalTrades(trades);
        score -= shortIntervals * 4;

        for (Trade trade : trades) {
            String mood = normalizeMood(trade.getMoodAtTrade());
            double profitLoss = trade.getProfitLoss();

            if (isRiskyMood(mood) && profitLoss < 0) {
                score -= 5;
            }

            if ("CALM".equals(mood)) {
                score += 2;
            } else if ("CONFIDENT".equals(mood)) {
                score += 1;
            }

            if (profitLoss >= 0 && ("CALM".equals(mood) || "CONFIDENT".equals(mood))) {
                score += 1;
            }
        }

        return clamp(score, 0, 100);
    }

    private static String findMostCommonMood(Map<String, Integer> tradeCountByMood) {
        String bestMood = "No trades yet";
        int highestCount = 0;

        for (String mood : ALL_MOODS) {
            int count = tradeCountByMood.getOrDefault(mood, 0);
            if (count > highestCount) {
                highestCount = count;
                bestMood = mood;
            }
        }

        return bestMood;
    }

    private static String buildSessionSummaryMessage(InsightResult result) {
        if (result.totalTrades == 0) {
            return "No trades yet this session.";
        }

        if (result.disciplineScore >= 80 && result.totalProfitLoss >= 0) {
            return "This session looks steady. Your decisions have been fairly controlled so far.";
        }

        if (result.disciplineScore >= 60) {
            return "This session is mixed. A few trades look solid, but some decisions seem more emotional.";
        }

        return "This session looks emotionally heavy. Slow the pace down and review your next trade carefully.";
    }

    private static int countShortIntervalTrades(List<Trade> trades) {
        if (trades == null || trades.size() < 2) {
            return 0;
        }

        List<Long> timestamps = new ArrayList<>();
        for (Trade trade : trades) {
            timestamps.add(trade.getTimestamp());
        }
        timestamps.sort(Long::compareTo);

        int shortIntervals = 0;
        for (int i = 1; i < timestamps.size(); i++) {
            if (timestamps.get(i) - timestamps.get(i - 1) <= IMPULSIVE_WINDOW_MS) {
                shortIntervals++;
            }
        }
        return shortIntervals;
    }

    private static boolean isRiskyMood(String mood) {
        return "IMPULSIVE".equals(mood)
                || "FOMO".equals(mood)
                || "FEARFUL".equals(mood)
                || "STRESSED".equals(mood);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
