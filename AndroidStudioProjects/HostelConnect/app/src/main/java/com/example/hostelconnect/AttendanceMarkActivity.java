package com.example.hostelconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for students to mark their daily attendance at 9 PM
 * Only accessible if student is currently inside hostel
 */
public class AttendanceMarkActivity extends AppCompatActivity {

    private TextView tvTitle, tvMessage, tvStudentName, tvDateTime, tvStatus;
    private Button btnMarkPresent, btnClose;
    private DatabaseHelper databaseHelper;
    private String phone;
    private String studentName;
    private boolean isInside;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_mark);

        phone = getIntent().getStringExtra("phone");
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
}

//        databaseHelper = new DatabaseHelper(this);
//
//        initializeViews();
//        loadStudentData();
//        checkAttendanceStatus();
//        setupClickListeners();
//    }
//
////    private void initializeViews() {
////        tvTitle = findViewById(R.id.tvTitle);
////        tvMessage = findViewById(R.id.tvMessage);
////        tvStudentName = findViewById(R.id.tvStudentName);
////        tvDateTime = findViewById(R.id.tvDateTime);
////        tvStatus = findViewById(R.id.tvStatus);
////        btnMarkPresent = findViewById(R.id.btnMarkPresent);
////        btnClose = findViewById(R.id.btnClose);
////    }
//
//    private void loadStudentData() {
//        Cursor cursor = databaseHelper.getHostellerByPhone(phone);
//        if (cursor != null && cursor.moveToFirst()) {
//            studentName = cursor.getString(cursor.getColumnIndex("name"));
//            String roomNumber = cursor.getString(cursor.getColumnIndex("room_number"));
//
//            tvStudentName.setText(studentName);
//            cursor.close();
//        }
//
//        // Display current date and time
//        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
//        tvDateTime.setText(sdf.format(new Date()));
//    }
//
//    private void checkAttendanceStatus() {
//        // Check if student is currently inside hostel
//        isInside = databaseHelper.isStudentInside(phone);
//
//        // Check if attendance already marked for today
//        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        Cursor cursor = databaseHelper.getDailyAttendanceForDate(phone, today);
//
//        boolean alreadyMarked = false;
//        if (cursor != null && cursor.moveToFirst()) {
//            String status = cursor.getString(cursor.getColumnIndex("status"));
//            alreadyMarked = "PRESENT".equals(status);
//            cursor.close();
//        }
//
//        if (alreadyMarked) {
//            // Already marked
//            tvTitle.setText("Attendance Already Marked");
//            tvMessage.setText("You have already marked your attendance for today.");
//            tvStatus.setText("Status: PRESENT ✓");
//            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//            tvStatus.setVisibility(View.VISIBLE);
//            btnMarkPresent.setEnabled(false);
//            btnMarkPresent.setText("Already Marked");
//        } else if (!isInside) {
//            // Student is not inside hostel
//            tvTitle.setText("Cannot Mark Attendance");
//            tvMessage.setText("You are currently outside the hostel. You must be inside to mark attendance.");
//            tvStatus.setText("Status: OUTSIDE ✗");
//            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
//            tvStatus.setVisibility(View.VISIBLE);
//            btnMarkPresent.setEnabled(false);
//            btnMarkPresent.setText("Not Inside Hostel");
//        } else {
//            // Can mark attendance
//            tvTitle.setText("Mark Daily Attendance");
//            tvMessage.setText("Please confirm your presence in the hostel by marking attendance.");
//            tvStatus.setVisibility(View.GONE);
//            btnMarkPresent.setEnabled(true);
//            btnMarkPresent.setText("Mark Present");
//        }
//    }
//
//    private void setupClickListeners() {
//        btnMarkPresent.setOnClickListener(v -> markAttendance());
//        btnClose.setOnClickListener(v -> finish());
//    }
//
//    private void markAttendance() {
//        if (!isInside) {
//            Toast.makeText(this, "You must be inside the hostel to mark attendance",
//                    Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                .format(new Date());
//
//        // Mark attendance as PRESENT
//        boolean success = databaseHelper.markDailyAttendance(phone, today, "PRESENT", currentTime);
//
//        if (success) {
//            Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show();
//
//            // Update UI
//            tvTitle.setText("Attendance Marked");
//            tvMessage.setText("Thank you! Your attendance has been recorded.");
//            tvStatus.setText("Status: PRESENT ✓");
//            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//            tvStatus.setVisibility(View.VISIBLE);
//            btnMarkPresent.setEnabled(false);
//            btnMarkPresent.setText("Marked Successfully");
//
//            // Close activity after 2 seconds
//            btnClose.postDelayed(() -> finish(), 2000);
//        } else {
//            Toast.makeText(this, "Failed to mark attendance. Please try again.",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        finish();
//    }
