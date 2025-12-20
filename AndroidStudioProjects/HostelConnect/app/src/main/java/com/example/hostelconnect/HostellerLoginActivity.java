package com.example.hostelconnect;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Random;

public class HostellerLoginActivity extends AppCompatActivity {

    TextInputEditText etLoginPhone, etLoginPassword;
    Button btnLogin, btnGoToSignup;
    TextView tvForgotPassword;
    DatabaseHelper databaseHelper;

    // OTP Variables
    private String generatedOTP = "";
    private String verifiedPhoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosteller_login);

        etLoginPhone = findViewById(R.id.etLoginPhone);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToSignup = findViewById(R.id.btnGoToSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        databaseHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginHosteller();
            }
        });

        btnGoToSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HostellerLoginActivity.this, HostellerSignUpActivity.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhoneVerificationDialog();
            }
        });
    }

    private void loginHosteller() {
        String phone = etLoginPhone.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (phone.isEmpty()) {
            etLoginPhone.setError("Phone number is required");
            etLoginPhone.requestFocus();
            return;
        }

        if (phone.length() != 10) {
            etLoginPhone.setError("Enter valid 10 digit phone number");
            etLoginPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Password is required");
            etLoginPassword.requestFocus();
            return;
        }

        Cursor cursor = databaseHelper.checkHostellerLogin(phone, password);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int nameIndex = cursor.getColumnIndex("name");
            String name = "";
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex);
            }
            cursor.close();

            Toast.makeText(this, "Login Successful! Welcome " + name, Toast.LENGTH_SHORT).show();

            // Navigate to Hosteller Dashboard
            Intent intent = new Intent(HostellerLoginActivity.this, HostellerDashboard.class);
            intent.putExtra("phone", phone);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // STEP 1: PHONE VERIFICATION DIALOG
    // ═══════════════════════════════════════════════════════════════════
    private void showPhoneVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_verify_phone, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText etVerifyPhone = dialogView.findViewById(R.id.etVerifyPhone);
        Button btnSendOTP = dialogView.findViewById(R.id.btnSendOTP);
        Button btnCancelVerify = dialogView.findViewById(R.id.btnCancelVerify);

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etVerifyPhone.getText().toString().trim();

                // Validation
                if (phone.isEmpty()) {
                    etVerifyPhone.setError("Phone number is required");
                    etVerifyPhone.requestFocus();
                    return;
                }

                if (phone.length() != 10) {
                    etVerifyPhone.setError("Enter valid 10 digit phone number");
                    etVerifyPhone.requestFocus();
                    return;
                }

                // Check if phone exists in database
//                if (!databaseHelper.checkPhoneExists(phone)) {
//                    Toast.makeText(HostellerLoginActivity.this,
//                            "Phone number not registered", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                // Generate OTP
                generatedOTP = generateOTP();
                verifiedPhoneNumber = phone;

                // Simulate sending OTP (In real app, integrate SMS gateway)
                Toast.makeText(HostellerLoginActivity.this,
                        "OTP sent to " + phone, Toast.LENGTH_SHORT).show();

                dialog.dismiss();
                showOTPVerificationDialog(phone);
            }
        });

        btnCancelVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // STEP 2: OTP VERIFICATION DIALOG
    // ═══════════════════════════════════════════════════════════════════
    private void showOTPVerificationDialog(String phoneNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_verify_otp, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false); // Prevent dismissing without verification

        TextView tvOtpSentTo = dialogView.findViewById(R.id.tvOtpSentTo);
        TextView tvOtpDisplay = dialogView.findViewById(R.id.tvOtpDisplay);
        TextView tvResendOtp = dialogView.findViewById(R.id.tvResendOtp);
        TextInputEditText etOtp = dialogView.findViewById(R.id.etOtp);
        Button btnVerifyOtp = dialogView.findViewById(R.id.btnVerifyOtp);
        Button btnCancelOtp = dialogView.findViewById(R.id.btnCancelOtp);

        // Display phone number
        String maskedPhone = phoneNumber.substring(0, 2) + "XXXXXX" + phoneNumber.substring(8);
        tvOtpSentTo.setText("OTP sent to +91 " + maskedPhone);

        // Display OTP (For testing - Remove in production)
        tvOtpDisplay.setText("OTP: " + generatedOTP);
        tvOtpDisplay.setVisibility(View.VISIBLE); // For testing only

        // Resend OTP
        tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatedOTP = generateOTP();
                tvOtpDisplay.setText("OTP: " + generatedOTP);
                Toast.makeText(HostellerLoginActivity.this,
                        "New OTP sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
            }
        });

        // Verify OTP Button
        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOTP = etOtp.getText().toString().trim();

                if (enteredOTP.isEmpty()) {
                    etOtp.setError("Enter OTP");
                    etOtp.requestFocus();
                    return;
                }

                if (enteredOTP.length() != 6) {
                    etOtp.setError("OTP must be 6 digits");
                    etOtp.requestFocus();
                    return;
                }

                // Verify OTP
                if (enteredOTP.equals(generatedOTP)) {
                    Toast.makeText(HostellerLoginActivity.this,
                            "OTP Verified Successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    showResetPasswordDialog(phoneNumber);
                } else {
                    Toast.makeText(HostellerLoginActivity.this,
                            "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    etOtp.setError("Invalid OTP");
                    etOtp.requestFocus();
                }
            }
        });

        btnCancelOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                generatedOTP = ""; // Clear OTP
                verifiedPhoneNumber = ""; // Clear phone
            }
        });

        dialog.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // STEP 3: RESET PASSWORD DIALOG (After OTP Verification)
    // ═══════════════════════════════════════════════════════════════════
    private void showResetPasswordDialog(String phoneNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

//        TextView tvResetPhoneNumber = dialogView.findViewById(R.id.tvResetPhoneNumber);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnResetPassword = dialogView.findViewById(R.id.btnResetPassword);
        Button btnCancelReset = dialogView.findViewById(R.id.btnCancelReset);

        // Display phone number
//        tvResetPhoneNumber.setText("Phone: +91 " + phoneNumber);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                // Validation
                if (newPassword.isEmpty()) {
                    etNewPassword.setError("New password is required");
                    etNewPassword.requestFocus();
                    return;
                }

                if (newPassword.length() < 6) {
                    etNewPassword.setError("Password must be at least 6 characters");
                    etNewPassword.requestFocus();
                    return;
                }

                if (confirmPassword.isEmpty()) {
                    etConfirmPassword.setError("Confirm password is required");
                    etConfirmPassword.requestFocus();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    etConfirmPassword.setError("Passwords do not match");
                    etConfirmPassword.requestFocus();
                    return;
                }

                // Update password in database
                boolean isUpdated = databaseHelper.updateHostellerPassword(phoneNumber, newPassword);

                if (isUpdated) {
                    Toast.makeText(HostellerLoginActivity.this,
                            "Password reset successful! Please login with new password",
                            Toast.LENGTH_LONG).show();
                    dialog.dismiss();

                    // Clear OTP data
                    generatedOTP = "";
                    verifiedPhoneNumber = "";
                } else {
                    Toast.makeText(HostellerLoginActivity.this,
                            "Failed to reset password. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancelReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER METHOD: GENERATE 6-DIGIT OTP
    // ═══════════════════════════════════════════════════════════════════
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates 6-digit number
        return String.valueOf(otp);
    }
}