package com.example.moodtradesimulator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moodtradesimulator.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView nameText;
    private TextView emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        nameText = findViewById(R.id.profileNameText);
        emailText = findViewById(R.id.profileEmailText);
        Button viewTradeHistoryButton = findViewById(R.id.viewTradeHistoryButton);
        Button signOutButton = findViewById(R.id.signOutButton);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        MainBottomNavHelper.setup(this, bottomNavigationView, R.id.navigation_profile);

        viewTradeHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, TradeHistoryActivity.class);
            startActivity(intent);
        });

        signOutButton.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            firebaseAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String displayName = currentUser.getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty()) {
            nameText.setText(displayName);
        } else {
            nameText.setText(currentUser.getEmail());
        }
        emailText.setText(currentUser.getEmail());
    }
}
