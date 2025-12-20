package com.example.hostelconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    // UI Components - Personal (matching XML)
    private EditText etName, etEmail, etAddress, etEmergencyContact, etBloodGroup;

    // UI Components - Academic
    private EditText etCourse, etYear, etBranch, etRollNumber;

    // UI Components - Emergency
    private EditText etParentName, etParentPhone, etGuardianName, etGuardianPhone;

    // UI Components - Other
    private ImageView ivProfileImage;
    private TextView tvProfileInitial, tvChangePhoto;
    private Button btnSaveProfile, btnCancel;

    private DatabaseHelper databaseHelper;
    private String phone;
    private byte[] selectedImageBytes = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // Resize bitmap to reasonable size
                        Bitmap resizedBitmap = resizeBitmap(bitmap, 500, 500);

                        // Convert to byte array
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                        selectedImageBytes = outputStream.toByteArray();

                        // Display in ImageView
                        ivProfileImage.setImageBitmap(resizedBitmap);
                        ivProfileImage.setVisibility(android.view.View.VISIBLE);
                        tvProfileInitial.setVisibility(android.view.View.GONE);

                    } catch (Exception e) {
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        databaseHelper = new DatabaseHelper(this);
        phone = getIntent().getStringExtra("phone");

        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadCurrentData();
        setupClickListeners();
    }

    private void initializeViews() {
        // Personal Information (matching XML IDs)
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etAddress = findViewById(R.id.etAddress);

        // Academic Information
        etCourse = findViewById(R.id.etCourse);
        etYear = findViewById(R.id.etYear);
        etBranch = findViewById(R.id.etBranch);
        etRollNumber = findViewById(R.id.etRollNumber);

        // Emergency Contacts
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        etParentName = findViewById(R.id.etParentName);
        etParentPhone = findViewById(R.id.etParentPhone);
        etGuardianName = findViewById(R.id.etGuardianName);
        etGuardianPhone = findViewById(R.id.etGuardianPhone);

        // Other
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadCurrentData() {
        Cursor cursor = null;
        try {
            cursor = databaseHelper.getHostellerByPhone(phone);

            if (cursor != null && cursor.moveToFirst()) {
                // Load basic info
                String name = getColumnValue(cursor, "name");
                etName.setText(name);
                etEmail.setText(getColumnValue(cursor, "email"));
                etAddress.setText(getColumnValue(cursor, "address"));

                // Set profile initial
                if (name != null && !name.isEmpty()) {
                    tvProfileInitial.setText(name.substring(0, 1).toUpperCase());
                }

                // Load blood group
                etBloodGroup.setText(getColumnValue(cursor, "blood_group"));

                // Load academic info
                etCourse.setText(getColumnValue(cursor, "course"));
                etYear.setText(getColumnValue(cursor, "year"));
                etBranch.setText(getColumnValue(cursor, "branch"));
                etRollNumber.setText(getColumnValue(cursor, "roll_number"));

                // Load emergency contacts
                etEmergencyContact.setText(getColumnValue(cursor, "emergency_contact"));
                etParentName.setText(getColumnValue(cursor, "parent_name"));
                etParentPhone.setText(getColumnValue(cursor, "parent_phone"));
                etGuardianName.setText(getColumnValue(cursor, "guardian_name"));
                etGuardianPhone.setText(getColumnValue(cursor, "guardian_phone"));

                // Load profile image
                byte[] imageData = cursor.getBlob(cursor.getColumnIndexOrThrow("image"));
                if (imageData != null && imageData.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    ivProfileImage.setImageBitmap(bitmap);
                    ivProfileImage.setVisibility(android.view.View.VISIBLE);
                    tvProfileInitial.setVisibility(android.view.View.GONE);
                    selectedImageBytes = imageData; // Keep existing image
                }
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

    private String getColumnValue(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting column " + columnName, e);
        }
        return "";
    }

    private void setupClickListeners() {
        tvChangePhoto.setOnClickListener(v -> selectImage());

        ivProfileImage.setOnClickListener(v -> selectImage());

        tvProfileInitial.setOnClickListener(v -> selectImage());

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnCancel.setOnClickListener(v -> finish());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void selectImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        }
    }

    private void saveProfile() {
        // Get all values
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String bloodGroup = etBloodGroup.getText().toString().trim();
        String emergencyContact = etEmergencyContact.getText().toString().trim();

        // Academic
        String course = etCourse.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String rollNumber = etRollNumber.getText().toString().trim();

        // Emergency
        String parentName = etParentName.getText().toString().trim();
        String parentPhone = etParentPhone.getText().toString().trim();
        String guardianName = etGuardianName.getText().toString().trim();
        String guardianPhone = etGuardianPhone.getText().toString().trim();

        // Validate required fields
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        // Update database - NOTE: Hostel fields set to empty since not in layout
        boolean success = databaseHelper.updateHostellerProfile(
                phone, name, email, address, bloodGroup, emergencyContact,
                parentName, parentPhone, guardianName, guardianPhone,
                course, year, branch, rollNumber,
                "", "", "", "",  // Empty hostel fields (not in current layout)
                selectedImageBytes
        );

        if (success) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}