package com.example.moodtradesimulator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moodtradesimulator.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText fullNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        fullNameInput = findViewById(R.id.signUpFullNameInput);
        emailInput = findViewById(R.id.signUpEmailInput);
        passwordInput = findViewById(R.id.signUpPasswordInput);
        signUpButton = findViewById(R.id.signUpButton);
        TextView loginLink = findViewById(R.id.loginLinkText);

        signUpButton.setOnClickListener(v -> attemptSignUp());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignUp() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your name, email, and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Use a password with at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        signUpButton.setEnabled(false);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    signUpButton.setEnabled(true);
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "That sign-up did not go through. Try a different email.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Something went wrong while creating the account. Please try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(profileTask ->
                            user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                firebaseAuth.signOut();
                                if (verificationTask.isSuccessful()) {
                                    Toast.makeText(this, "Verification email sent. Please check your inbox and your junk/spam folder before logging in.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Your account was created, but we could not send the verification email.", Toast.LENGTH_LONG).show();
                                }
                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }));
                });
    }
}
