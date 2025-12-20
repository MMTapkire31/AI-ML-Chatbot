package com.example.hostelconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecuritySignup extends AppCompatActivity {

    private EditText etSecurityName, etSecurityId, etSecurityPhone,
            etSecurityPassword, etSecurityConfirmPassword;
    private Spinner spinnerShift;
    private Button btnSecuritySignup;
    private TextView tvSecurityLogin, tvBackToHome;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_signup);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        etSecurityName = findViewById(R.id.etSecurityName);
        etSecurityId = findViewById(R.id.etSecurityId);
        etSecurityPhone = findViewById(R.id.etSecurityPhone);
        etSecurityPassword = findViewById(R.id.etSecurityPassword);
        etSecurityConfirmPassword = findViewById(R.id.etSecurityConfirmPassword);
        spinnerShift = findViewById(R.id.spinnerShift);
        btnSecuritySignup = findViewById(R.id.btnSecuritySignup);
        tvSecurityLogin = findViewById(R.id.tvSecurityLogin);
        tvBackToHome = findViewById(R.id.tvBackToHome);

        // Setup shift spinner
        String[] shiftOptions = {"Select Shift", "Morning (6 AM - 2 PM)", "Afternoon (2 PM - 10 PM)", "Night (10 PM - 6 AM)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, shiftOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShift.setAdapter(adapter);

        // Signup button click
        btnSecuritySignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerSecurity();
            }
        });

        // Navigate to login
        tvSecurityLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SecurityLogin.class);
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

    private void registerSecurity() {
        String name = etSecurityName.getText().toString().trim();
        String securityId = etSecurityId.getText().toString().trim();
        String phone = etSecurityPhone.getText().toString().trim();
        String shift = spinnerShift.getSelectedItem().toString();
        String password = etSecurityPassword.getText().toString().trim();
        String confirmPassword = etSecurityConfirmPassword.getText().toString().trim();

        Log.d("SecuritySignup", "Attempting registration for: " + name);

        // Validation
        if (name.isEmpty()) {
            etSecurityName.setError("Name is required");
            etSecurityName.requestFocus();
            return;
        }

        if (securityId.isEmpty()) {
            etSecurityId.setError("Security ID is required");
            etSecurityId.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etSecurityPhone.setError("Phone number is required");
            etSecurityPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            etSecurityPhone.setError("Enter a valid phone number");
            etSecurityPhone.requestFocus();
            return;
        }

        if (shift.equals("Select Shift")) {
            Toast.makeText(this, "Please select a shift", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            etSecurityPassword.setError("Password is required");
            etSecurityPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etSecurityPassword.setError("Password must be at least 6 characters");
            etSecurityPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etSecurityConfirmPassword.setError("Passwords do not match");
            etSecurityConfirmPassword.requestFocus();
            return;
        }

        // Check if security ID already exists
        if (databaseHelper.checkSecurityIdExists(securityId)) {
            Toast.makeText(this, "Security ID already registered", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert security into database
        try {
            long result = databaseHelper.insertSecurity(name, securityId, phone, shift, password);

            if (result != -1) {
                Log.d("SecuritySignup", "Registration successful with ID: " + result);
                Toast.makeText(this, "Registration successful! Please login", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SecuritySignup.this, SecurityLogin.class);
                startActivity(intent);
                finish();
            } else {
                Log.e("SecuritySignup", "Registration failed - database returned -1");
                Toast.makeText(this, "Registration failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("SecuritySignup", "Registration error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}