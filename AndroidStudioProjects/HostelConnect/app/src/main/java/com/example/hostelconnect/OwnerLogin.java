package com.example.hostelconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OwnerLogin extends AppCompatActivity {

    private EditText etOwnerEmail, etOwnerPassword;
    private Button btnOwnerLogin;
    private TextView tvOwnerSignup, tvBackToHome;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_login);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("VisitorManagementPrefs", MODE_PRIVATE);

        // Initialize views
        etOwnerEmail = findViewById(R.id.etOwnerEmail);
        etOwnerPassword = findViewById(R.id.etOwnerPassword);
        btnOwnerLogin = findViewById(R.id.btnOwnerLogin);
        tvOwnerSignup = findViewById(R.id.tvOwnerSignup);
        tvBackToHome = findViewById(R.id.tvBackToHome);

        // Login button click
        btnOwnerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginOwner();
            }
        });

        // Navigate to signup
        tvOwnerSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnerLogin.this, OwnerSignup.class);
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

    private void loginOwner() {
        String email = etOwnerEmail.getText().toString().trim();
        String password = etOwnerPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etOwnerEmail.setError("Email is required");
            etOwnerEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etOwnerPassword.setError("Password is required");
            etOwnerPassword.requestFocus();
            return;
        }

        // Check credentials in database
        Cursor cursor = databaseHelper.getOwnerByEmailAndPassword(email, password);

        if (cursor != null && cursor.moveToFirst()) {
            // Login successful
            int ownerId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String ownerName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String propertyName = cursor.getString(cursor.getColumnIndexOrThrow("property_name"));

            // Save session
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("userType", "owner");
            editor.putInt("userId", ownerId);
            editor.putString("userName", ownerName);
            editor.putString("propertyName", propertyName);
            editor.apply();

            Toast.makeText(this, "Login successful! Welcome " + ownerName, Toast.LENGTH_SHORT).show();

            // Navigate to dashboard
            Intent intent = new Intent(OwnerLogin.this, OwnerDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            cursor.close();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}