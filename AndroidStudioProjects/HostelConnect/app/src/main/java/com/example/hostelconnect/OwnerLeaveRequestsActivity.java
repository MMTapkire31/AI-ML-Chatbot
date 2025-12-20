package com.example.hostelconnect;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OwnerLeaveRequestsActivity extends AppCompatActivity {

    private static final String TAG = "OwnerLeaveRequests";

    private TextView btnBack, tvPendingCount, tvApprovedCount, tvRejectedCount;
    private RecyclerView rvLeaveRequests;
    private TextView tvNoLeaves;

    private DatabaseHelper databaseHelper;
    private NotificationHelper notificationHelper;
    private LeaveRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_leave_requests);

        databaseHelper = new DatabaseHelper(this);
        notificationHelper = new NotificationHelper(this);

        initializeViews();
        setupClickListeners();
        loadLeaveRequests();
        updateStatistics();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);
        tvRejectedCount = findViewById(R.id.tvRejectedCount);
        rvLeaveRequests = findViewById(R.id.rvLeaveRequests);
        tvNoLeaves = findViewById(R.id.tvNoLeaves);

        rvLeaveRequests.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLeaveRequests() {
        Log.d(TAG, "loadLeaveRequests called");

        Cursor cursor = databaseHelper.getAllLeaveRequests();
        Log.d(TAG, "Cursor count: " + (cursor != null ? cursor.getCount() : 0));

        if (cursor != null && cursor.getCount() > 0) {
            tvNoLeaves.setVisibility(View.GONE);
            rvLeaveRequests.setVisibility(View.VISIBLE);

            if (adapter == null) {
                Log.d(TAG, "Creating new adapter");
                adapter = new LeaveRequestAdapter(cursor, true);
                adapter.setOnActionListener(this::handleLeaveAction);
                rvLeaveRequests.setAdapter(adapter);
            } else {
                Log.d(TAG, "Updating existing adapter");
                adapter.updateCursor(cursor);
            }
        } else {
            tvNoLeaves.setVisibility(View.VISIBLE);
            rvLeaveRequests.setVisibility(View.GONE);

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void handleLeaveAction(int leaveId, String action) {
        Log.d(TAG, "handleLeaveAction - LeaveId: " + leaveId + ", Action: " + action);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(action + " Leave Request");
        builder.setMessage("Are you sure you want to " + action.toLowerCase() + " this leave request?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            String status = action.equals("Approve") ? "Approved" : "Rejected";
            String responseDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            Log.d(TAG, "Updating leave status to: " + status);

            // Get leave request details before updating
            String phone = null;
            String fromDate = null;
            String toDate = null;

            Cursor leaveCursor = databaseHelper.getAllLeaveRequests();
            if (leaveCursor != null && leaveCursor.moveToFirst()) {
                do {
                    int idIndex = leaveCursor.getColumnIndex("id");
                    if (idIndex != -1 && leaveCursor.getInt(idIndex) == leaveId) {
                        int phoneIndex = leaveCursor.getColumnIndex("phone");
                        int fromDateIndex = leaveCursor.getColumnIndex("from_date");
                        int toDateIndex = leaveCursor.getColumnIndex("to_date");

                        if (phoneIndex != -1) phone = leaveCursor.getString(phoneIndex);
                        if (fromDateIndex != -1) fromDate = leaveCursor.getString(fromDateIndex);
                        if (toDateIndex != -1) toDate = leaveCursor.getString(toDateIndex);

                        Log.d(TAG, "Found leave details - Phone: " + phone + ", From: " + fromDate + ", To: " + toDate);
                        break;
                    }
                } while (leaveCursor.moveToNext());
                leaveCursor.close();
            }

            // Update database
            boolean success = databaseHelper.updateLeaveStatus(leaveId, status, responseDate);
            Log.d(TAG, "Database update success: " + success);

            if (success) {
                Toast.makeText(this, "Leave request " + status.toLowerCase(),
                        Toast.LENGTH_SHORT).show();

                // Send notification to student
                if (phone != null && fromDate != null && toDate != null) {
                    Log.d(TAG, "Sending notification to phone: " + phone);
                    if ("Approved".equals(status)) {
                        notificationHelper.sendLeaveApprovedNotification(phone, fromDate, toDate);
                    } else {
                        notificationHelper.sendLeaveRejectedNotification(phone, fromDate, toDate);
                    }
                }

                // Reload data
                Log.d(TAG, "Reloading leave requests");
                loadLeaveRequests();
                updateStatistics();

                // Verify update
                verifyDatabaseUpdate(leaveId);
            } else {
                Toast.makeText(this, "Failed to update leave request",
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to update database");
            }
        });

        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void verifyDatabaseUpdate(int leaveId) {
        Cursor verifyCursor = databaseHelper.getAllLeaveRequests();
        if (verifyCursor != null && verifyCursor.moveToFirst()) {
            do {
                int idIndex = verifyCursor.getColumnIndex("id");
                if (idIndex != -1 && verifyCursor.getInt(idIndex) == leaveId) {
                    int statusIndex = verifyCursor.getColumnIndex("status");
                    if (statusIndex != -1) {
                        String updatedStatus = verifyCursor.getString(statusIndex);
                        Log.d(TAG, "Verified - LeaveId: " + leaveId + " has status: " + updatedStatus);
                    }
                    break;
                }
            } while (verifyCursor.moveToNext());
            verifyCursor.close();
        }
    }

    private void updateStatistics() {
        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;

        Cursor statsCursor = databaseHelper.getAllLeaveRequests();
        if (statsCursor != null && statsCursor.moveToFirst()) {
            int statusIndex = statsCursor.getColumnIndex("status");

            if (statusIndex != -1) {
                do {
                    String status = statsCursor.getString(statusIndex);
                    if ("Pending".equals(status)) {
                        pendingCount++;
                    } else if ("Approved".equals(status)) {
                        approvedCount++;
                    } else if ("Rejected".equals(status)) {
                        rejectedCount++;
                    }
                } while (statsCursor.moveToNext());
            }
            statsCursor.close();
        }

        tvPendingCount.setText(String.valueOf(pendingCount));
        tvApprovedCount.setText(String.valueOf(approvedCount));
        tvRejectedCount.setText(String.valueOf(rejectedCount));

        Log.d(TAG, "Statistics - Pending: " + pendingCount + ", Approved: " + approvedCount + ", Rejected: " + rejectedCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.closeCursor();
        }
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        loadLeaveRequests();
        updateStatistics();
    }
}