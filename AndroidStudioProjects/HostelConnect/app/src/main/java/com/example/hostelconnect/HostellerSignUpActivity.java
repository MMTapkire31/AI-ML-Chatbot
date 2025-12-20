package com.example.hostelconnect;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class HostellerSignUpActivity extends AppCompatActivity {

    private static final int PICK_RECEIPT_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;

    TextInputEditText etSignupName, etSignupPhone, etSignupEmail, etSignupAddress, etSignupRoom, etSignupPassword;
    Button btnUploadReceipt, btnCaptureFace, btnSignup;
    TextView tvReceiptFileName, tvFaceStatus, tvGoToLogin;
    DatabaseHelper databaseHelper;

    private String receiptPath = "";
    private byte[] faceImageBytes = null;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosteller_sign_up);

        // Initialize views
        etSignupName = findViewById(R.id.etSignupName);
        etSignupPhone = findViewById(R.id.etSignupPhone);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupAddress = findViewById(R.id.etSignupAddress);
        etSignupRoom = findViewById(R.id.etSignupRoom);
        etSignupPassword = findViewById(R.id.etSignupPassword);

        btnUploadReceipt = findViewById(R.id.btnUploadReceipt);
        btnCaptureFace = findViewById(R.id.btnCaptureFace);
        btnSignup = findViewById(R.id.btnSignup);

        tvReceiptFileName = findViewById(R.id.tvReceiptFileName);
        tvFaceStatus = findViewById(R.id.tvFaceStatus);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        databaseHelper = new DatabaseHelper(this);

        // Upload Receipt Button
        btnUploadReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Capture Face Button
        btnCaptureFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            }
        });

        // Sign Up Button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerHosteller();
            }
        });

        // Go to Login
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_RECEIPT_REQUEST);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_RECEIPT_REQUEST && data != null && data.getData() != null) {
                Uri uri = data.getData();
                receiptPath = uri.toString();
                String fileName = uri.getLastPathSegment();
                tvReceiptFileName.setText("✓ " + (fileName != null ? fileName : "Receipt uploaded"));
                tvReceiptFileName.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (requestCode == CAMERA_REQUEST && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                // Convert bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                faceImageBytes = stream.toByteArray();

                tvFaceStatus.setText("✓ Photo captured successfully");
                tvFaceStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to capture photo", Toast.LENGTH_SHORT).show();
            }
        }
    }


        private void registerHosteller() {
            String name = etSignupName.getText().toString().trim();
            String phone = etSignupPhone.getText().toString().trim();
            String email = etSignupEmail.getText().toString().trim();
            String address = etSignupAddress.getText().toString().trim();
            String room = etSignupRoom.getText().toString().trim();
            String password = etSignupPassword.getText().toString().trim();

            Log.d("SignUp", "Starting registration for: " + name);

            // Validation
            if (name.isEmpty()) {
                etSignupName.setError("Name is required");
                etSignupName.requestFocus();
                return;
            }

            if (phone.isEmpty()) {
                etSignupPhone.setError("Phone number is required");
                etSignupPhone.requestFocus();
                return;
            }

            if (phone.length() != 10) {
                etSignupPhone.setError("Enter valid 10 digit phone number");
                etSignupPhone.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                etSignupEmail.setError("Email is required");
                etSignupEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etSignupEmail.setError("Enter valid email");
                etSignupEmail.requestFocus();
                return;
            }

            if (address.isEmpty()) {
                etSignupAddress.setError("Address is required");
                etSignupAddress.requestFocus();
                return;
            }

            if (room.isEmpty()) {
                etSignupRoom.setError("Room number is required");
                etSignupRoom.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etSignupPassword.setError("Password is required");
                etSignupPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etSignupPassword.setError("Password must be at least 6 characters");
                etSignupPassword.requestFocus();
                return;
            }

            if (receiptPath.isEmpty()) {
                Toast.makeText(this, "Please upload payment receipt", Toast.LENGTH_SHORT).show();
                Log.e("SignUp", "Receipt path is empty");
                return;
            }

            if (faceImageBytes == null) {
                Toast.makeText(this, "Please capture your photo", Toast.LENGTH_SHORT).show();
                Log.e("SignUp", "Face image bytes is null");
                return;
            }

            Log.d("SignUp", "All validations passed");

            // Check if phone already exists
            boolean phoneExists = databaseHelper.checkPhoneExists(phone);
            Log.d("SignUp", "Phone exists check: " + phoneExists);

            if (phoneExists) {
                Toast.makeText(this, "Phone number already registered", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("SignUp", "Calling insertHosteller...");

            // Insert into database
            boolean isInserted = databaseHelper.insertHosteller(name, phone, email, address, room, password, receiptPath, faceImageBytes);

            Log.d("SignUp", "Insert result: " + isInserted);

            if (isInserted) {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Registration Failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
