package com.example.hostelconnect;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HostellerDashboard extends AppCompatActivity {

    private static final String TAG = "HostellerDashboard";

    // UI Components - Profile Section
    private TextView tvUserName, tvUserPhone, tvRoomNumber, tvProfileInitial;
    private ImageView ivProfileImage;
    private TextView btnLogout, btnEditProfile;

    // UI Components - Statistics Cards
    private TextView tvAttendanceCount, tvPendingPayment, tvNoticeCount;
    private TextView tvCurrentStatus, tvTodayEntries, tvTodayExits;
    private TextView tvWeeklyAttendance, tvMonthlyAttendance;
    private CircularProgressIndicator progressAttendance;

    // UI Components - Quick Actions
    private CardView cardEntry, cardExit;
    private MaterialCardView cardAttendance, cardPayment, cardNotices, cardRules;
    private MaterialCardView cardComplaints, cardMess, cardProfile, cardEmergency;
    private MaterialCardView cardLaundry, cardVisitors, cardFeedback;
    private MaterialCardView cardLeaveRequest;  // NEW: Leave Request Card

    // UI Components - New Features
    private TextView tvGreeting, tvDateTime, tvLastEntry, tvLastExit;
    private TextView tvPendingLeaves;  // NEW: Badge for pending leave count
    private View layoutQuickStats, layoutRecentActivity;

    private DatabaseHelper databaseHelper;
    private String phone;
    private String userName;
    private byte[] profileImage;

    private final ActivityResultLauncher<Intent> qrScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getBooleanExtra("success", false)) {
                        loadStatistics();
                        loadRecentActivity();
                        Toast.makeText(this, "Entry/Exit recorded successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                    // After notification permission, request location permission
                    requestLocationPermission();
                } else {
                    Toast.makeText(this, "Notification permission denied. Attendance may not work properly", Toast.LENGTH_LONG).show();
                    // Still try to request location permission
                    requestLocationPermission();
                }
            }
    );

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean fineLocationGranted = permissions.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if (Boolean.TRUE.equals(fineLocationGranted) || Boolean.TRUE.equals(coarseLocationGranted)) {
                    Toast.makeText(this, "Location permission granted for automatic attendance", Toast.LENGTH_SHORT).show();
                    scheduleDailyAttendanceWorker();
                } else {
                    showLocationPermissionRationale();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_hosteller_dashboard);

            databaseHelper = new DatabaseHelper(this);

            phone = getIntent().getStringExtra("phone");
            if (phone == null || phone.isEmpty()) {
                Log.e(TAG, "Phone number is null or empty");
                Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Save phone to SharedPreferences for Worker
            getSharedPreferences("HostelConnectPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("phone", phone)
                    .apply();

            createNotificationChannel();
            initializeViews();
            loadUserData();
            setupClickListeners();
            initializeAttendanceSystem();
            updateGreeting();
            updateDateTime();
            checkAttendanceStatus();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading dashboard", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            // Profile Section
            tvUserName = findViewById(R.id.tvUserName);
            tvUserPhone = findViewById(R.id.tvUserPhone);
            tvRoomNumber = findViewById(R.id.tvRoomNumber);
            tvProfileInitial = findViewById(R.id.tvProfileInitial);
            ivProfileImage = findViewById(R.id.ivProfileImage);
            btnLogout = findViewById(R.id.btnLogout);
            btnEditProfile = findViewById(R.id.btnEditProfile);

            // Statistics
            tvAttendanceCount = findViewById(R.id.tvAttendanceCount);
            tvPendingPayment = findViewById(R.id.tvPendingPayment);
            tvNoticeCount = findViewById(R.id.tvNoticeCount);
            tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
            tvTodayEntries = findViewById(R.id.tvTodayEntries);
            tvTodayExits = findViewById(R.id.tvTodayExits);
            tvWeeklyAttendance = findViewById(R.id.tvWeeklyAttendance);
            tvMonthlyAttendance = findViewById(R.id.tvMonthlyAttendance);
            progressAttendance = findViewById(R.id.progressAttendance);

            // Quick Actions
            cardEntry = findViewById(R.id.cardEntry);
            cardExit = findViewById(R.id.cardExit);

            // Main Features
            cardAttendance = findViewById(R.id.cardAttendance);
            cardPayment = findViewById(R.id.cardPayment);
            cardNotices = findViewById(R.id.cardNotices);
            cardRules = findViewById(R.id.cardRules);
            cardComplaints = findViewById(R.id.cardComplaints);
            cardMess = findViewById(R.id.cardMess);
            cardProfile = findViewById(R.id.cardProfile);
            cardEmergency = findViewById(R.id.cardEmergency);

            // New Features
            cardLaundry = findViewById(R.id.cardLaundry);
            cardVisitors = findViewById(R.id.cardVisitors);
            cardFeedback = findViewById(R.id.cardFeedback);
            cardLeaveRequest = findViewById(R.id.cardLeaveRequest);  // NEW: Initialize Leave Request card

            // Badge for pending leave count
            tvPendingLeaves = findViewById(R.id.tvPendingLeaves);  // NEW: Initialize badge

            // Additional UI
            tvGreeting = findViewById(R.id.tvGreeting);
            tvDateTime = findViewById(R.id.tvDateTime);
            tvLastEntry = findViewById(R.id.tvLastEntry);
            tvLastExit = findViewById(R.id.tvLastExit);
            layoutQuickStats = findViewById(R.id.layoutQuickStats);
            layoutRecentActivity = findViewById(R.id.layoutRecentActivity);

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    private void setupClickListeners() {
        try {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
            btnEditProfile.setOnClickListener(v -> openEditProfile());

            // Quick Actions
            cardEntry.setOnClickListener(v -> openQRScanner("ENTRY"));
            cardExit.setOnClickListener(v -> openQRScanner("EXIT"));

            // Main Features
            cardAttendance.setOnClickListener(v -> openAttendance());
            cardPayment.setOnClickListener(v -> openPayment());
            cardNotices.setOnClickListener(v -> openNotices());
            cardRules.setOnClickListener(v -> openRules());
            cardComplaints.setOnClickListener(v -> openComplaints());
            cardMess.setOnClickListener(v -> openMessMenu());
            cardProfile.setOnClickListener(v -> openProfileView());
            cardEmergency.setOnClickListener(v -> showEmergencyContacts());

            // New Features
            cardLaundry.setOnClickListener(v -> openLaundryService());
            cardVisitors.setOnClickListener(v -> openVisitorManagement());
            cardFeedback.setOnClickListener(v -> openFeedback());
            cardLeaveRequest.setOnClickListener(v -> openLeaveRequest());  // NEW: Leave Request click listener

            Log.d(TAG, "All click listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void initializeAttendanceSystem() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    requestLocationPermission();
                }
            } else {
                requestLocationPermission();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing attendance system: " + e.getMessage(), e);
        }
    }

    /**
     * Request location permission for automatic attendance
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Show rationale dialog
            new AlertDialog.Builder(this)
                    .setTitle("Location Permission Required")
                    .setMessage("Hostel Connect needs location access to automatically mark your attendance at 10 PM daily. " +
                            "This checks if you're within hostel premises.\n\n" +
                            "Your location is only checked at 10 PM and is not stored or shared.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        locationPermissionLauncher.launch(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        });
                    })
                    .setNegativeButton("Skip", (dialog, which) -> {
                        Toast.makeText(this, "Automatic attendance will not work without location permission",
                                Toast.LENGTH_LONG).show();
                    })
                    .show();
        } else {
            // Permission already granted, schedule worker
            scheduleDailyAttendanceWorker();
        }
    }

    /**
     * Show rationale for location permission if denied
     */
    private void showLocationPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("Automatic attendance marking requires location permission to verify if you're in the hostel at 10 PM.\n\n" +
                        "Without this permission, you'll need to manually mark attendance daily.\n\n" +
                        "You can enable it later from App Settings.")
                .setPositiveButton("OK", null)
                .show();
    }


    /**
     * Schedule the daily attendance worker to run at 9 PM every day
     */
    private void scheduleDailyAttendanceWorker() {
        try {
            Calendar current = Calendar.getInstance();
            Calendar due = Calendar.getInstance();

            // Set to 9 PM (21:00)
            due.set(Calendar.HOUR_OF_DAY, 21);  // 9 PM
            due.set(Calendar.MINUTE, 0);
            due.set(Calendar.SECOND, 0);

            long delay = due.getTimeInMillis() - current.getTimeInMillis();

            // If already past 9 PM today, schedule for tomorrow
            if (delay < 0) {
                delay += TimeUnit.DAYS.toMillis(1);
            }

            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                    AutoAttendanceWorker.class,
                    24,  // Repeat every 24 hours
                    TimeUnit.HOURS
            )
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "AutoAttendance",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
            );

            // Calculate when it will run
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String runTime = timeFormat.format(due.getTime());

            Log.d(TAG, "Automatic attendance scheduled for 9 PM daily");
            Toast.makeText(this, "Automatic attendance enabled at 9 PM daily", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling attendance worker: " + e.getMessage(), e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Attendance notifications channel
            NotificationChannel attendanceChannel = new NotificationChannel(
                    "attendance_channel",
                    "Attendance Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            attendanceChannel.setDescription("Notifications for daily attendance status");

            // Summary notifications channel
            NotificationChannel summaryChannel = new NotificationChannel(
                    "attendance_summary_channel",
                    "Attendance Summary",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            summaryChannel.setDescription("Summary of daily attendance marking");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(attendanceChannel);
            notificationManager.createNotificationChannel(summaryChannel);
        }
    }

    private void loadUserData() {
        Cursor cursor = null;
        try {
            cursor = databaseHelper.getHostellerByPhone(phone);

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndexOrThrow("name");
                int roomIndex = cursor.getColumnIndexOrThrow("room_number");
                int imageIndex = cursor.getColumnIndexOrThrow("image");

                userName = cursor.getString(nameIndex);
                String roomNumber = cursor.getString(roomIndex);
                profileImage = cursor.getBlob(imageIndex);

                if (userName != null && !userName.isEmpty()) {
                    tvUserName.setText(userName);
                }

                tvUserPhone.setText("+91 " + phone);
                tvRoomNumber.setText("Room: " + (roomNumber != null && !roomNumber.isEmpty() ? roomNumber : "Not Assigned"));

                if (profileImage != null && profileImage.length > 0) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(profileImage, 0, profileImage.length);
                        if (bitmap != null) {
                            ivProfileImage.setImageBitmap(bitmap);
                            ivProfileImage.setVisibility(View.VISIBLE);
                            tvProfileInitial.setVisibility(View.GONE);
                        } else {
                            setProfileInitial();
                        }
                    } catch (Exception e) {
                        setProfileInitial();
                    }
                } else {
                    setProfileInitial();
                }

                loadStatistics();
                loadRecentActivity();

            } else {
                throw new Exception("User not found");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_LONG).show();
            finish();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setProfileInitial() {
        if (userName != null && !userName.isEmpty()) {
            tvProfileInitial.setText(userName.substring(0, 1).toUpperCase());
            tvProfileInitial.setVisibility(View.VISIBLE);
            ivProfileImage.setVisibility(View.GONE);
        }
    }

    private void loadStatistics() {
        try {
            // Attendance Statistics
            int attendancePercentage = databaseHelper.getAttendancePercentage(phone);
            tvAttendanceCount.setText(attendancePercentage + "%");
            progressAttendance.setProgress(attendancePercentage);

            // Today's Entry/Exit Count
            int todayEntries = databaseHelper.getTodayEntryExitCount(phone, "ENTRY");
            int todayExits = databaseHelper.getTodayEntryExitCount(phone, "EXIT");
            tvTodayEntries.setText(String.valueOf(todayEntries));
            tvTodayExits.setText(String.valueOf(todayExits));

            // Current Status
            boolean isInside = databaseHelper.isStudentInside(phone);
            tvCurrentStatus.setText(isInside ? "Inside Hostel" : "Outside Hostel");
            tvCurrentStatus.setTextColor(getResources().getColor(
                    isInside ? android.R.color.holo_green_dark : android.R.color.holo_orange_dark, null
            ));

            // Payment Statistics
            double pendingAmount = databaseHelper.getTotalPendingAmount(phone);
            tvPendingPayment.setText("₹" + String.format(Locale.getDefault(), "%.0f", pendingAmount));

            // Notice Count
            Cursor noticesCursor = null;
            try {
                noticesCursor = databaseHelper.getAllNotices();
                int noticeCount = noticesCursor != null ? noticesCursor.getCount() : 0;
                tvNoticeCount.setText(String.valueOf(noticeCount));
            } finally {
                if (noticesCursor != null) {
                    noticesCursor.close();
                }
            }

            // Weekly and Monthly Attendance
            loadWeeklyMonthlyStats();

            // NEW: Load Pending Leave Requests Count
            int pendingLeaves = databaseHelper.getPendingLeaveCount(phone);
            if (pendingLeaves > 0) {
                tvPendingLeaves.setVisibility(View.VISIBLE);
                tvPendingLeaves.setText(String.valueOf(pendingLeaves));
            } else {
                tvPendingLeaves.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading statistics: " + e.getMessage(), e);
        }
    }

    private void loadWeeklyMonthlyStats() {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String currentMonth = sdf.format(calendar.getTime());

            Cursor monthlyCursor = databaseHelper.getMonthlyAttendance(phone, currentMonth);

            int presentDays = 0;
            int totalDays = 0;
            int weekPresentDays = 0;
            int weekTotalDays = 0;

            if (monthlyCursor != null && monthlyCursor.moveToFirst()) {
                do {
                    totalDays++;
                    String status = monthlyCursor.getString(monthlyCursor.getColumnIndexOrThrow("status"));
                    if ("PRESENT".equals(status)) {
                        presentDays++;
                    }

                    weekTotalDays++;
                    if ("PRESENT".equals(status)) {
                        weekPresentDays++;
                    }
                } while (monthlyCursor.moveToNext());
                monthlyCursor.close();
            }

            int weeklyPercentage = weekTotalDays > 0 ? (weekPresentDays * 100 / weekTotalDays) : 0;
            int monthlyPercentage = totalDays > 0 ? (presentDays * 100 / totalDays) : 0;

            tvWeeklyAttendance.setText(weeklyPercentage + "%");
            tvMonthlyAttendance.setText(monthlyPercentage + "%");

        } catch (Exception e) {
            Log.e(TAG, "Error loading weekly/monthly stats: " + e.getMessage(), e);
        }
    }

    private void loadRecentActivity() {
        try {
            Cursor cursor = databaseHelper.getLastEntryExit(phone);

            if (cursor != null && cursor.moveToFirst()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                if ("ENTRY".equals(type)) {
                    tvLastEntry.setText("Last Entry: " + formatTime(timestamp));
                    tvLastExit.setText("Last Exit: N/A");
                } else {
                    tvLastExit.setText("Last Exit: " + formatTime(timestamp));
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading recent activity: " + e.getMessage(), e);
        }
    }

    private String formatTime(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(timestamp));
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void updateGreeting() {
        try {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            String greeting;
            if (hour < 12) {
                greeting = "Good Morning";
            } else if (hour < 17) {
                greeting = "Good Afternoon";
            } else {
                greeting = "Good Evening";
            }

            tvGreeting.setText(greeting + ", " + (userName != null ? userName.split(" ")[0] : ""));
        } catch (Exception e) {
            Log.e(TAG, "Error updating greeting: " + e.getMessage(), e);
        }
    }

    private void updateDateTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
            tvDateTime.setText(sdf.format(Calendar.getInstance().getTime()));
        } catch (Exception e) {
            Log.e(TAG, "Error updating date time: " + e.getMessage(), e);
        }
    }

    private void checkAttendanceStatus() {
        try {
            boolean isOverdue = databaseHelper.isAttendanceOverdue(phone);
            if (isOverdue) {
                showAttendanceReminder();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking attendance status: " + e.getMessage(), e);
        }
    }

    private void showAttendanceReminder() {
        new AlertDialog.Builder(this)
                .setTitle("Attendance Reminder")
                .setMessage("You haven't marked your attendance today. Please mark your attendance before 9 PM.")
                .setPositiveButton("Mark Now", (dialog, which) -> markQuickAttendance())
                .setNegativeButton("Later", null)
                .show();
    }

    private void markQuickAttendance() {
        new AlertDialog.Builder(this)
                .setTitle("Mark Attendance")
                .setMessage("Are you present in the hostel today?")
                .setPositiveButton("Yes, I'm Present", (dialog, which) -> {
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
                    String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

                    boolean success = databaseHelper.markDailyAttendance(phone, today, "PRESENT", currentTime);
                    if (success) {
                        Toast.makeText(this, "Attendance marked successfully", Toast.LENGTH_SHORT).show();
                        loadStatistics();
                    } else {
                        Toast.makeText(this, "Error marking attendance", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openQRScanner(String scanType) {
        try {
            Intent intent = new Intent(this, QRScannerActivity.class);
            intent.putExtra("phone", phone);
            intent.putExtra("scanType", scanType);
            qrScannerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening scanner", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAttendance() {
        Intent intent = new Intent(this, AttendaceActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void openPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void openNotices() {
        Intent intent = new Intent(this, NoticesActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void openRules() {
        Intent intent = new Intent(this, RulesActivity.class);
        startActivity(intent);
    }

    private void openComplaints() {
        Intent intent = new Intent(this, ComplaintsActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("name", userName);
        startActivity(intent);
    }

    private void openMessMenu() {
        Intent intent = new Intent(this, MessMenuActivity.class);
        startActivity(intent);
    }

    private void openProfileView() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void showEmergencyContacts() {
        Intent intent = new Intent(this, EmergencyContactActivity.class);
        startActivity(intent);
    }

    private void openLaundryService() {
        Intent intent = new Intent(this, LaundryServiceActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void openVisitorManagement() {
        Intent intent = new Intent(this, VisitorManagementActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void openFeedback() {
        Intent intent = new Intent(this, FeedBackActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("name", userName);
        startActivity(intent);
    }

    // NEW: Open Leave Request Activity
    private void openLeaveRequest() {
        Intent intent = new Intent(this, LeaveRequestActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (phone != null && !phone.isEmpty()) {
            loadStatistics();
            loadRecentActivity();
            updateDateTime();
        }
    }

    @Override
    public void onBackPressed() {
        showLogoutDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}