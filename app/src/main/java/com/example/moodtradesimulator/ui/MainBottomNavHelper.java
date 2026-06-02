package com.example.moodtradesimulator.ui;

import android.app.Activity;
import android.content.Intent;

import com.example.moodtradesimulator.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class MainBottomNavHelper {

    private MainBottomNavHelper() {
    }

    public static void setup(Activity activity, BottomNavigationView bottomNavigationView, int selectedItemId) {
        bottomNavigationView.setSelectedItemId(selectedItemId);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == selectedItemId) {
                return true;
            }

            Class<?> targetActivity = null;
            if (itemId == R.id.navigation_portfolio) {
                targetActivity = PortfolioActivity.class;
            } else if (itemId == R.id.navigation_markets) {
                targetActivity = MarketsActivity.class;
            } else if (itemId == R.id.navigation_insights) {
                targetActivity = InsightsActivity.class;
            } else if (itemId == R.id.navigation_profile) {
                targetActivity = ProfileActivity.class;
            }

            if (targetActivity == null) {
                return false;
            }

            Intent intent = new Intent(activity, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
            return true;
        });
    }
}
