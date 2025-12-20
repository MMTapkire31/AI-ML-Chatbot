package com.example.hostelconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OwnerDashboard extends AppCompatActivity {

    private static final String TAG = "OwnerDashboard";

    // Header Views
    private TextView tvOwnerName, tvPropertyName, btnLogout;

    // Statistics Cards
    private TextView tvTotalHostellers, tvPresentToday, tvCurrentlyInside;
    private TextView tvTotalVisitors, tvPendingVisitors, tvTodayVisitors;
    private TextView tvPendingPayments, tvTotalPendingAmount, tvCollectedThisMonth;
    private TextView tvPendingComplaints, tvPendingMaintenance, tvPendingLeaves;
    private TextView tvAverageRating, tvTotalFeedbacks;

    // Quick Action Buttons
    private MaterialCardView cardViewHostellers, cardViewAttendance, cardViewPayments;
    private MaterialCardView cardViewComplaints, cardViewVisitors, cardViewMaintenance;
    private MaterialCardView cardViewLeaveRequests, cardViewFeedbacks, cardViewNotices;
    private MaterialCardView cardViewReports, cardViewSettings;

    // Recent Activity
    private RecyclerView rvRecentActivity;
    private TextView tvNoRecentActivity;

    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private int ownerId;
    private String ownerName, propertyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        // Initialize database and preferences
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("VisitorManagementPrefs", MODE_PRIVATE);

        // Get session data
        ownerId = sharedPreferences.getInt("userId", -1);
        ownerName = sharedPreferences.getString("userName", "Owner");
        propertyName = sharedPreferences.getString("propertyName", "Property");

        initializeViews();
        loadDashboardData();
        setupClickListeners();
    }

    private void initializeViews() {
        // Header
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvPropertyName = findViewById(R.id.tvPropertyName);
        btnLogout = findViewById(R.id.btnLogout);

        // Statistics - Hostellers
        tvTotalHostellers = findViewById(R.id.tvTotalHostellers);
        tvPresentToday = findViewById(R.id.tvPresentToday);
        tvCurrentlyInside = findViewById(R.id.tvCurrentlyInside);

        // Statistics - Visitors
        tvTotalVisitors = findViewById(R.id.tvTotalVisitors);
        tvPendingVisitors = findViewById(R.id.tvPendingVisitors);
        tvTodayVisitors = findViewById(R.id.tvTodayVisitors);

        // Statistics - Payments
        tvPendingPayments = findViewById(R.id.tvPendingPayments);
        tvTotalPendingAmount = findViewById(R.id.tvTotalPendingAmount);
        tvCollectedThisMonth = findViewById(R.id.tvCollectedThisMonth);

        // Statistics - Requests
        tvPendingComplaints = findViewById(R.id.tvPendingComplaints);
        tvPendingMaintenance = findViewById(R.id.tvPendingMaintenance);
        tvPendingLeaves = findViewById(R.id.tvPendingLeaves);

        // Statistics - Feedback
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalFeedbacks = findViewById(R.id.tvTotalFeedbacks);

        // Quick Actions
        cardViewHostellers = findViewById(R.id.cardViewHostellers);
        cardViewAttendance = findViewById(R.id.cardViewAttendance);
        cardViewPayments = findViewById(R.id.cardViewPayments);
        cardViewComplaints = findViewById(R.id.cardViewComplaints);
        cardViewVisitors = findViewById(R.id.cardViewVisitors);
        cardViewMaintenance = findViewById(R.id.cardViewMaintenance);
        cardViewLeaveRequests = findViewById(R.id.cardViewLeaveRequests);
        cardViewFeedbacks = findViewById(R.id.cardViewFeedbacks);
        cardViewNotices = findViewById(R.id.cardViewNotices);
        cardViewReports = findViewById(R.id.cardViewReports);
        cardViewSettings = findViewById(R.id.cardViewSettings);

        // Recent Activity
        rvRecentActivity = findViewById(R.id.rvRecentActivity);
        tvNoRecentActivity = findViewById(R.id.tvNoRecentActivity);

        // Set owner info
        tvOwnerName.setText(ownerName);
        tvPropertyName.setText(propertyName);
    }

    private void loadDashboardData() {
        try {
            loadHostellerStatistics();
            loadVisitorStatistics();
            loadPaymentStatistics();
            loadRequestStatistics();
            loadFeedbackStatistics();
            loadRecentActivity();
        } catch (Exception e) {
            Log.e(TAG, "Error loading dashboard data", e);
            Toast.makeText(this, "Error loading dashboard data", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHostellerStatistics() {
        try {
            // Total Hostellers
            int totalHostellers = databaseHelper.getTotalHostellersCount();
            tvTotalHostellers.setText(String.valueOf(totalHostellers));

            // Present Today
            int presentToday = databaseHelper.getTodayPresentCount();
            tvPresentToday.setText(String.valueOf(presentToday));

            // Currently Inside
            int currentlyInside = databaseHelper.getCurrentlyInsideCount();
            tvCurrentlyInside.setText(String.valueOf(currentlyInside));

            Log.d(TAG, "Loaded hosteller stats: total=" + totalHostellers +
                    ", present=" + presentToday + ", inside=" + currentlyInside);
        } catch (Exception e) {
            Log.e(TAG, "Error loading hosteller statistics", e);
        }
    }

    private void loadVisitorStatistics() {
        try {
            // Total Visitors
            int totalVisitors = databaseHelper.getTotalVisitorsCount();
            tvTotalVisitors.setText(String.valueOf(totalVisitors));

            // Pending Visitors
            int pendingVisitors = databaseHelper.getPendingVisitorsCount();
            tvPendingVisitors.setText(String.valueOf(pendingVisitors));

            // Today's Visitors
            int todayVisitors = databaseHelper.getTodayVisitorsCount();
            tvTodayVisitors.setText(String.valueOf(todayVisitors));

            Log.d(TAG, "Loaded visitor stats: total=" + totalVisitors +
                    ", pending=" + pendingVisitors + ", today=" + todayVisitors);
        } catch (Exception e) {
            Log.e(TAG, "Error loading visitor statistics", e);
        }
    }

    private void loadPaymentStatistics() {
        Cursor cursor = null;
        try {
            // Get pending payments cursor
            cursor = databaseHelper.getAllPendingPayments();
            int pendingCount = cursor != null ? cursor.getCount() : 0;

            tvPendingPayments.setText(String.valueOf(pendingCount));

            // Total pending amount
            double totalPending = databaseHelper.getTotalPendingPaymentsAmount();
            tvTotalPendingAmount.setText("₹" + String.format(Locale.getDefault(), "%.0f", totalPending));

            // Collected this month (you can implement this method)
            tvCollectedThisMonth.setText("₹0");

            Log.d(TAG, "Loaded payment stats: pending=" + pendingCount +
                    ", amount=" + totalPending);
        } catch (Exception e) {
            Log.e(TAG, "Error loading payment statistics", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void loadRequestStatistics() {
        try {
            // Pending Complaints
            int pendingComplaints = databaseHelper.getPendingComplaintsCount();
            tvPendingComplaints.setText(String.valueOf(pendingComplaints));

            // Pending Maintenance
            int pendingMaintenance = databaseHelper.getPendingMaintenanceCount();
            tvPendingMaintenance.setText(String.valueOf(pendingMaintenance));

            // Pending Leaves
            int pendingLeaves = databaseHelper.getPendingLeaveCount();
            tvPendingLeaves.setText(String.valueOf(pendingLeaves));

            Log.d(TAG, "Loaded request stats: complaints=" + pendingComplaints +
                    ", maintenance=" + pendingMaintenance + ", leaves=" + pendingLeaves);
        } catch (Exception e) {
            Log.e(TAG, "Error loading request statistics", e);
        }
    }

    private void loadFeedbackStatistics() {
        Cursor feedbackCursor = null;
        try {
            // Average Rating
            double avgRating = databaseHelper.getAverageRatingForOwner();
            tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", avgRating));

            // Total Feedbacks
            feedbackCursor = databaseHelper.getAllFeedbacksForOwner();
            int totalFeedbacks = feedbackCursor != null ? feedbackCursor.getCount() : 0;
            tvTotalFeedbacks.setText(String.valueOf(totalFeedbacks));

            Log.d(TAG, "Loaded feedback stats: rating=" + avgRating +
                    ", total=" + totalFeedbacks);
        } catch (Exception e) {
            Log.e(TAG, "Error loading feedback statistics", e);
        } finally {
            if (feedbackCursor != null) feedbackCursor.close();
        }
    }

    private void loadRecentActivity() {
        Cursor cursor = null;
        try {
            cursor = databaseHelper.getRecentActivityForOwner(10);

            if (cursor != null && cursor.getCount() > 0) {
                tvNoRecentActivity.setVisibility(View.GONE);
                rvRecentActivity.setVisibility(View.VISIBLE);
                // Setup adapter here if needed
                Log.d(TAG, "Loaded recent activity: " + cursor.getCount() + " items");
            } else {
                tvNoRecentActivity.setVisibility(View.VISIBLE);
                rvRecentActivity.setVisibility(View.GONE);
                Log.d(TAG, "No recent activity");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading recent activity", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> logout());

        cardViewHostellers.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerHostellersActivity.class);
            startActivity(intent);
        });

        cardViewAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerAttendanceActivity.class);
            startActivity(intent);
        });

        cardViewPayments.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerPaymentsActivity.class);
            startActivity(intent);
        });

        cardViewComplaints.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerComplaintsActivity.class);
            startActivity(intent);
        });

        cardViewVisitors.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerVisitorsActivity.class);
            startActivity(intent);
        });

        cardViewMaintenance.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerMaintenanceActivity.class);
            startActivity(intent);
        });

        cardViewLeaveRequests.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerLeaveRequestsActivity.class);
            startActivity(intent);
        });

        cardViewFeedbacks.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerFeedbacksActivity.class);
            startActivity(intent);
        });

        cardViewNotices.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboard.this, OwnerNoticesActivity.class);
            startActivity(intent);
        });

        cardViewReports.setOnClickListener(v -> {
            Toast.makeText(this, "Reports feature - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardViewSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings feature - Coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void logout() {
        // Clear session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        Intent intent = new Intent(OwnerDashboard.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}