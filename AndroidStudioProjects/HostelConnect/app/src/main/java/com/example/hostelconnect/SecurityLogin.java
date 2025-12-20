package com.example.hostelconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecurityLogin extends AppCompatActivity {

    private EditText etSecurityId, etSecurityPassword;
    private Button btnSecurityLogin;
    private TextView tvSecuritySignup, tvBackToHome;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_login);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("VisitorManagementPrefs", MODE_PRIVATE);

        // Initialize views
        etSecurityId = findViewById(R.id.etSecurityId);
        etSecurityPassword = findViewById(R.id.etSecurityPassword);
        btnSecurityLogin = findViewById(R.id.btnSecurityLogin);
        tvSecuritySignup = findViewById(R.id.tvSecuritySignup);
        tvBackToHome = findViewById(R.id.tvBackToHome);

        // Login button click
        btnSecurityLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSecurity();
            }
        });

        // Navigate to signup
        tvSecuritySignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecurityLogin.this, SecuritySignup.class);
                startActivity(intent);
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

    private void loginSecurity() {
        String securityId = etSecurityId.getText().toString().trim();
        String password = etSecurityPassword.getText().toString().trim();

        Log.d("SecurityLogin", "Attempting login with ID: " + securityId);

        // Validation
        if (securityId.isEmpty()) {
            etSecurityId.setError("Security ID is required");
            etSecurityId.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etSecurityPassword.setError("Password is required");
            etSecurityPassword.requestFocus();
            return;
        }

        // Check credentials in database
        Cursor cursor = databaseHelper.getSecurityByIdAndPassword(securityId, password);

        Log.d("SecurityLogin", "Cursor count: " + (cursor != null ? cursor.getCount() : "null"));

        if (cursor != null && cursor.moveToFirst()) {
            // Login successful
            try {
                // Use the actual column names from the database
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String securityName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String shift = cursor.getString(cursor.getColumnIndexOrThrow("shift"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));

                Log.d("SecurityLogin", "Login successful for: " + securityName);

                // Save session
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("userType", "security");
                editor.putInt("userId", id);
                editor.putString("userName", securityName);
                editor.putString("securityId", securityId);
                editor.putString("shift", shift);
                editor.putString("phone", phone);
                editor.apply();

                Toast.makeText(this, "Login successful! Welcome " + securityName, Toast.LENGTH_SHORT).show();

                // Navigate to dashboard
                Intent intent = new Intent(SecurityLogin.this, SecurityDashboard.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } catch (Exception e) {
                Log.e("SecurityLogin", "Error reading cursor: " + e.getMessage());
                Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                cursor.close();
            }
        } else {
            Log.d("SecurityLogin", "Invalid credentials");
            Toast.makeText(this, "Invalid security ID or password", Toast.LENGTH_SHORT).show();
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}