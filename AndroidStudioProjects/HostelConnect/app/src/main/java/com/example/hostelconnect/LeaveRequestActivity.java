package com.example.hostelconnect;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class LeaveRequestActivity extends AppCompatActivity {

    private static final String TAG = "LeaveRequestActivity";

    private EditText etFromDate, etToDate, etReason;
    private Button btnSubmitLeave;
    private RecyclerView rvLeaveRequests;
    private TextView tvNoLeaves, btnBack;
    private LinearLayout layoutNewRequest;
    private MaterialCardView cardNewRequest;

    private DatabaseHelper databaseHelper;
    private LeaveRequestAdapter adapter;
    private String phone, userName;
    private Calendar fromCalendar, toCalendar;
    private boolean isNewRequestVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_request);

        databaseHelper = new DatabaseHelper(this);

        phone = getIntent().getStringExtra("phone");
        Log.d(TAG, "onCreate - Phone: " + phone);

        // Get user name from database
        Cursor cursor = databaseHelper.getHostellerByPhone(phone);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");
            if (nameIndex != -1) {
                userName = cursor.getString(nameIndex);
                Log.d(TAG, "User name: " + userName);
            }
            cursor.close();
        }

        initializeViews();
        setupClickListeners();
        loadLeaveRequests();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        cardNewRequest = findViewById(R.id.cardNewRequest);
        layoutNewRequest = findViewById(R.id.layoutNewRequest);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        etReason = findViewById(R.id.etReason);
        btnSubmitLeave = findViewById(R.id.btnSubmitLeave);
        rvLeaveRequests = findViewById(R.id.rvLeaveRequests);
        tvNoLeaves = findViewById(R.id.tvNoLeaves);

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();

        rvLeaveRequests.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardNewRequest.setOnClickListener(v -> {
            if (isNewRequestVisible) {
                layoutNewRequest.setVisibility(View.GONE);
                isNewRequestVisible = false;
            } else {
                layoutNewRequest.setVisibility(View.VISIBLE);
                isNewRequestVisible = true;
            }
        });

        etFromDate.setOnClickListener(v -> showDatePicker(true));
        etToDate.setOnClickListener(v -> showDatePicker(false));

        btnSubmitLeave.setOnClickListener(v -> submitLeaveRequest());
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar calendar = isFromDate ? fromCalendar : toCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String dateString = sdf.format(calendar.getTime());

                    if (isFromDate) {
                        etFromDate.setText(dateString);
                    } else {
                        etToDate.setText(dateString);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void submitLeaveRequest() {
        String fromDate = etFromDate.getText().toString().trim();
        String toDate = etToDate.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        if (fromDate.isEmpty()) {
            Toast.makeText(this, "Please select from date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (toDate.isEmpty()) {
            Toast.makeText(this, "Please select to date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter reason for leave", Toast.LENGTH_SHORT).show();
            return;
        }

        if (toCalendar.before(fromCalendar)) {
            Toast.makeText(this, "To date cannot be before from date", Toast.LENGTH_SHORT).show();
            return;
        }

        String requestedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        long result = databaseHelper.submitLeaveRequest(
                phone, userName, fromDate, toDate, reason, requestedDate
        );

        if (result != -1) {
            Toast.makeText(this, "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Leave request submitted - ID: " + result);

            etFromDate.setText("");
            etToDate.setText("");
            etReason.setText("");

            layoutNewRequest.setVisibility(View.GONE);
            isNewRequestVisible = false;

            loadLeaveRequests();
        } else {
            Toast.makeText(this, "Failed to submit leave request", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to submit leave request");
        }
    }

    private void loadLeaveRequests() {
        Log.d(TAG, "loadLeaveRequests called for phone: " + phone);

        Cursor cursor = databaseHelper.getLeaveRequests(phone);

        if (cursor != null) {
            Log.d(TAG, "Cursor count: " + cursor.getCount());

            // Log all leave requests for debugging
            if (cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex("id");
                    int statusIndex = cursor.getColumnIndex("status");
                    if (idIndex != -1 && statusIndex != -1) {
                        int id = cursor.getInt(idIndex);
                        String status = cursor.getString(statusIndex);
                        Log.d(TAG, "Leave ID: " + id + ", Status: " + status);
                    }
                } while (cursor.moveToNext());

                // Reset cursor to beginning
                cursor.moveToPosition(-1);
            }
        }

        if (cursor != null && cursor.getCount() > 0) {
            tvNoLeaves.setVisibility(View.GONE);
            rvLeaveRequests.setVisibility(View.VISIBLE);

            if (adapter == null) {
                Log.d(TAG, "Creating new adapter");
                adapter = new LeaveRequestAdapter(cursor, false);
                rvLeaveRequests.setAdapter(adapter);
            } else {
                Log.d(TAG, "Updating existing adapter");
                adapter.updateCursor(cursor);
            }
        } else {
            Log.d(TAG, "No leave requests found");
            tvNoLeaves.setVisibility(View.VISIBLE);
            rvLeaveRequests.setVisibility(View.GONE);

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        loadLeaveRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");

        if (adapter != null) {
            adapter.closeCursor();
        }

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}