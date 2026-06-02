package com.example.moodtradesimulator.logic;
import com.example.moodtradesimulator.model.Trade;


import java.util.List;

public class MoodBarLogic {

    public enum MoodState {
        CALM, CONFIDENT, STRESSED, FOMO, IMPULSIVE, FEARFUL
    }

    public static MoodState evaluate(List<Trade> trades) {
        return EmotionDetectionEngine.detectCurrentMood(trades);
    }
}
