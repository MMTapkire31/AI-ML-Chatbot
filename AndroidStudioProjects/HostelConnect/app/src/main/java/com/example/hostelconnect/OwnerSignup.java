package com.example.hostelconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OwnerSignup extends AppCompatActivity {

    private EditText etOwnerName, etOwnerEmail, etOwnerPhone, etPropertyName,
            etOwnerPassword, etOwnerConfirmPassword;
    private Button btnOwnerSignup;
    private TextView tvOwnerLogin, tvBackToHome;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_signup);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        etOwnerName = findViewById(R.id.etOwnerName);
        etOwnerEmail = findViewById(R.id.etOwnerEmail);
        etOwnerPhone = findViewById(R.id.etOwnerPhone);
        etPropertyName = findViewById(R.id.etPropertyName);
        etOwnerPassword = findViewById(R.id.etOwnerPassword);
        etOwnerConfirmPassword = findViewById(R.id.etOwnerConfirmPassword);
        btnOwnerSignup = findViewById(R.id.btnOwnerSignup);
        tvOwnerLogin = findViewById(R.id.tvOwnerLogin);
        tvBackToHome = findViewById(R.id.tvBackToHome);

        // Signup button click
        btnOwnerSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerOwner();
            }
        });

        // Navigate to login
        tvOwnerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnerSignup.this, OwnerLogin.class);
                startActivity(intent);
                finish();
            }
        });

        // Back to home
        tvBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void registerOwner() {
        String name = etOwnerName.getText().toString().trim();
        String email = etOwnerEmail.getText().toString().trim();
        String phone = etOwnerPhone.getText().toString().trim();
        String propertyName = etPropertyName.getText().toString().trim();
        String password = etOwnerPassword.getText().toString().trim();
        String confirmPassword = etOwnerConfirmPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etOwnerName.setError("Name is required");
            etOwnerName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etOwnerEmail.setError("Email is required");
            etOwnerEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etOwnerEmail.setError("Enter a valid email");
            etOwnerEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etOwnerPhone.setError("Phone number is required");
            etOwnerPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            etOwnerPhone.setError("Enter a valid phone number");
            etOwnerPhone.requestFocus();
            return;
        }

        if (propertyName.isEmpty()) {
            etPropertyName.setError("Property name is required");
            etPropertyName.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etOwnerPassword.setError("Password is required");
            etOwnerPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etOwnerPassword.setError("Password must be at least 6 characters");
            etOwnerPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etOwnerConfirmPassword.setError("Passwords do not match");
            etOwnerConfirmPassword.requestFocus();
            return;
        }

        // Check if email already exists
        if (databaseHelper.checkOwnerEmailExists(email)) {
            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert owner into database
        long result = databaseHelper.insertOwner(name, email, phone, propertyName, password);

        if (result != -1) {
            Toast.makeText(this, "Registration successful! Please login", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(OwnerSignup.this, OwnerLogin.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again", Toast.LENGTH_SHORT).show();
        }
    }
}