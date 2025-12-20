package com.example.hostelconnect;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ImageView ivProfileImage;
    private TextView tvProfileInitial, tvName, tvPhone, tvEmail, tvRoomNumber;
    private TextView tvRegistrationDate, tvAddress, tvEmergencyContact, tvBloodGroup;
    private TextView tvParentName, tvParentPhone, tvGuardianName, tvGuardianPhone;
    private TextView tvCourse, tvYear, tvBranch, tvRollNumber;
    private TextView tvHostelName, tvFloor, tvBlockName, tvRoomType;
    private MaterialButton btnEditProfile, btnChangePassword;
    private CardView cardPersonalInfo, cardAcademicInfo, cardHostelInfo, cardEmergencyInfo;

    private DatabaseHelper databaseHelper;
    private String phone;
    private boolean hasIncompleteProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);
        phone = getIntent().getStringExtra("phone");

        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadProfileData();
        setupClickListeners();

        // Show incomplete profile dialog if needed
        if (hasIncompleteProfile) {
            showIncompleteProfileDialog();
        }
    }

    private void initializeViews() {
        // Profile Header
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);

        // Personal Information
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvRegistrationDate = findViewById(R.id.tvRegistrationDate);
        tvAddress = findViewById(R.id.tvAddress);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);

        // Academic Information
        tvCourse = findViewById(R.id.tvCourse);
        tvYear = findViewById(R.id.tvYear);
        tvBranch = findViewById(R.id.tvBranch);
        tvRollNumber = findViewById(R.id.tvRollNumber);

        // Hostel Information
        tvHostelName = findViewById(R.id.tvHostelName);
        tvFloor = findViewById(R.id.tvFloor);
        tvBlockName = findViewById(R.id.tvBlockName);
        tvRoomType = findViewById(R.id.tvRoomType);

        // Emergency Contacts
        tvEmergencyContact = findViewById(R.id.tvEmergencyContact);
        tvParentName = findViewById(R.id.tvParentName);
        tvParentPhone = findViewById(R.id.tvParentPhone);
        tvGuardianName = findViewById(R.id.tvGuardianName);
        tvGuardianPhone = findViewById(R.id.tvGuardianPhone);

        // Buttons
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Cards
        cardPersonalInfo = findViewById(R.id.cardPersonalInfo);
        cardAcademicInfo = findViewById(R.id.cardAcademicInfo);
        cardHostelInfo = findViewById(R.id.cardHostelInfo);
        cardEmergencyInfo = findViewById(R.id.cardEmergencyInfo);
    }

    private void loadProfileData() {
        Cursor cursor = null;
        int missingFieldsCount = 0;

        try {
            cursor = databaseHelper.getHostellerByPhone(phone);

            if (cursor != null && cursor.moveToFirst()) {
                // Basic Information (always present from signup)
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = getColumnValue(cursor, "email");
                String roomNumber = getColumnValue(cursor, "room_number");
                byte[] imageData = cursor.getBlob(cursor.getColumnIndexOrThrow("image"));

                // Set basic info
                tvName.setText(name != null ? name : "N/A");
                tvPhone.setText("+91 " + phone);

                // Email
                if (email != null && !email.isEmpty()) {
                    tvEmail.setText(email);
                } else {
                    tvEmail.setText("Not provided");
                    tvEmail.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
                    missingFieldsCount++;
                }

                // Room Number
                if (roomNumber != null && !roomNumber.isEmpty()) {
                    tvRoomNumber.setText(roomNumber);
                } else {
                    tvRoomNumber.setText("Not Assigned");
                    tvRoomNumber.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
                }

                // Registration Date
                String regDate = getCurrentDate();
                tvRegistrationDate.setText(regDate);

                // Load profile image
                if (imageData != null && imageData.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    if (bitmap != null) {
                        ivProfileImage.setImageBitmap(bitmap);
                        ivProfileImage.setVisibility(View.VISIBLE);
                        tvProfileInitial.setVisibility(View.GONE);
                    } else {
                        setProfileInitial(name);
                    }
                } else {
                    setProfileInitial(name);
                }

                // Load optional information and count missing fields
                missingFieldsCount += loadOptionalField(cursor, "address", tvAddress, "Address");
                missingFieldsCount += loadOptionalField(cursor, "blood_group", tvBloodGroup, "Blood Group");
                missingFieldsCount += loadOptionalField(cursor, "course", tvCourse, "Course");
                missingFieldsCount += loadOptionalField(cursor, "year", tvYear, "Year");
                missingFieldsCount += loadOptionalField(cursor, "branch", tvBranch, "Branch");
                missingFieldsCount += loadOptionalField(cursor, "roll_number", tvRollNumber, "Roll Number");
                missingFieldsCount += loadOptionalField(cursor, "hostel_name", tvHostelName, "Hostel Name");
                missingFieldsCount += loadOptionalField(cursor, "floor", tvFloor, "Floor");
                missingFieldsCount += loadOptionalField(cursor, "block_name", tvBlockName, "Block");
                missingFieldsCount += loadOptionalField(cursor, "room_type", tvRoomType, "Room Type");
                missingFieldsCount += loadOptionalField(cursor, "emergency_contact", tvEmergencyContact, "Emergency Contact");
                missingFieldsCount += loadOptionalField(cursor, "parent_name", tvParentName, "Parent Name");
                missingFieldsCount += loadOptionalField(cursor, "parent_phone", tvParentPhone, "Parent Phone");
                missingFieldsCount += loadOptionalField(cursor, "guardian_name", tvGuardianName, "Guardian Name");
                missingFieldsCount += loadOptionalField(cursor, "guardian_phone", tvGuardianPhone, "Guardian Phone");

                // Set flag if profile is incomplete
                hasIncompleteProfile = missingFieldsCount > 3; // More than 3 missing fields

            } else {
                Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Load optional field and return 1 if missing, 0 if present
     */
    private int loadOptionalField(Cursor cursor, String columnName, TextView textView, String fieldLabel) {
        try {
            String value = getColumnValue(cursor, columnName);

            if (value != null && !value.isEmpty()) {
                textView.setText(value);
                textView.setTextColor(getResources().getColor(android.R.color.black, null));
                return 0; // Field is present
            } else {
                textView.setText("Not provided - Tap 'Edit Profile' to add");
                textView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
                return 1; // Field is missing
            }
        } catch (Exception e) {
            textView.setText("Not provided");
            textView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
            return 1;
        }
    }

    /**
     * Safely get column value from cursor
     */
    private String getColumnValue(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting column " + columnName + ": " + e.getMessage());
        }
        return null;
    }

    private void setProfileInitial(String name) {
        if (name != null && !name.isEmpty()) {
            tvProfileInitial.setText(name.substring(0, 1).toUpperCase());
            tvProfileInitial.setVisibility(View.VISIBLE);
            ivProfileImage.setVisibility(View.GONE);
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    private void showIncompleteProfileDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Complete Your Profile")
                .setMessage("Your profile is incomplete. Please add your personal, academic, and emergency contact information for a better experience.\n\n" +
                        "Fields marked in orange are missing.")
                .setPositiveButton("Complete Now", (dialog, which) -> {
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    intent.putExtra("phone", phone);
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .setCancelable(true)
                .show();
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("phone", phone);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showChangePasswordDialog() {
        // Create a custom dialog for changing password
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialogue_chnage_password, null);

        android.widget.EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        android.widget.EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        android.widget.EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            android.widget.Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String oldPassword = etOldPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                // Validate
                if (oldPassword.isEmpty()) {
                    etOldPassword.setError("Enter current password");
                    return;
                }

                if (newPassword.isEmpty()) {
                    etNewPassword.setError("Enter new password");
                    return;
                }

                if (newPassword.length() < 6) {
                    etNewPassword.setError("Password must be at least 6 characters");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    etConfirmPassword.setError("Passwords don't match");
                    return;
                }

                // Verify old password
                Cursor cursor = databaseHelper.checkHostellerLogin(phone, oldPassword);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.close();

                    // Update password
                    boolean success = databaseHelper.updateHostellerPassword(phone, newPassword);
                    if (success) {
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Error changing password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (cursor != null) cursor.close();
                    etOldPassword.setError("Current password is incorrect");
                }
            });
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when returning from edit
        loadProfileData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}