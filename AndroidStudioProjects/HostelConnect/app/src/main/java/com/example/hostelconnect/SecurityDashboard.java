package com.example.hostelconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

public class SecurityDashboard extends AppCompatActivity {

    private TextView tvSecurityName, tvSecurityId, tvShift, btnLogout,
            tvTodayEntries, tvCurrentVisitors, tvNoActiveVisitors;
    private LinearLayout btnScanQR, btnShowQR;
    private RecyclerView rvActiveVisitors;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private String userName, securityId, shift;

    private static final int QR_SCAN_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_dashboard);

        // Initialize database and preferences
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("VisitorManagementPrefs", MODE_PRIVATE);

        // Get session data
        userId = sharedPreferences.getInt("userId", -1);
        userName = sharedPreferences.getString("userName", "Security");
        securityId = sharedPreferences.getString("securityId", "SEC001");
        shift = sharedPreferences.getString("shift", "Morning");

        // Initialize views
        tvSecurityName = findViewById(R.id.tvSecurityName);
        tvSecurityId = findViewById(R.id.tvSecurityId);
        tvShift = findViewById(R.id.tvShift);
        btnLogout = findViewById(R.id.btnLogout);
        tvTodayEntries = findViewById(R.id.tvTodayEntries);
        tvCurrentVisitors = findViewById(R.id.tvCurrentVisitors);
        tvNoActiveVisitors = findViewById(R.id.tvNoActiveVisitors);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnShowQR = findViewById(R.id.btnShowQR);
        rvActiveVisitors = findViewById(R.id.rvActiveVisitors);

        // Set security info
        tvSecurityName.setText(userName);
        tvSecurityId.setText("ID: " + securityId);
        tvShift.setText("Shift: " + shift);

        // Load dashboard data
        loadDashboardData();

        // Logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Show QR Codes button
        btnShowQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecurityDashboard.this, QRDisplayActivity.class);
                startActivity(intent);
            }
        });

        // Scan QR button - NOW FUNCTIONAL
        btnScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRScanner();
            }
        });
    }

    private void loadDashboardData() {
        // Get today's entries count (ENTRY type only)
        int todayEntries = databaseHelper.getTodayEntriesCount();
        tvTodayEntries.setText(String.valueOf(todayEntries));

        // Get current visitors count (visitors inside hostel now)
        int currentVisitors = databaseHelper.getCurrentlyInsideCount();
        tvCurrentVisitors.setText(String.valueOf(currentVisitors));

        // Load active visitors
        if (currentVisitors == 0) {
            tvNoActiveVisitors.setVisibility(View.VISIBLE);
            rvActiveVisitors.setVisibility(View.GONE);
        } else {
            tvNoActiveVisitors.setVisibility(View.GONE);
            rvActiveVisitors.setVisibility(View.VISIBLE);
            // Setup RecyclerView with adapter
            // TODO: Implement VisitorAdapter to show currently inside hostellers
            // rvActiveVisitors.setLayoutManager(new LinearLayoutManager(this));
            // rvActiveVisitors.setAdapter(new VisitorAdapter(visitorList));
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan Hosteller QR Code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                processQRCode(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processQRCode(String qrContent) {
        try {
            // Parse QR code JSON
            JSONObject jsonObject = new JSONObject(qrContent);
            String phone = jsonObject.getString("phone");
            String name = jsonObject.getString("name");
            String roomNumber = jsonObject.getString("room_number");

            // Check if hosteller exists
            if (databaseHelper.checkPhoneExists(phone)) {
                // Determine entry or exit
                boolean isInside = databaseHelper.isStudentInside(phone);
                String type = isInside ? "EXIT" : "ENTRY";

                // Record entry/exit
                boolean success = databaseHelper.recordEntryExit(phone, type,
                        "Scanned by Security: " + securityId);

                if (success) {
                    String action = isInside ? "Exit" : "Entry";
                    Toast.makeText(this,
                            action + " recorded for " + name + " (Room: " + roomNumber + ")",
                            Toast.LENGTH_LONG).show();

                    // Refresh dashboard data
                    loadDashboardData();
                } else {
                    Toast.makeText(this, "Failed to record " + (isInside ? "exit" : "entry"),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid QR Code - Hosteller not found",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid QR Code format", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        // Clear session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        Intent intent = new Intent(SecurityDashboard.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
}