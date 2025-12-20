package com.example.hostelconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import java.util.List;

public class QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private DecoratedBarcodeView barcodeView;
    private TextView tvScanType;
    private String phone;
    private String scanType;
    private DatabaseHelper databaseHelper;
    private boolean hasScanned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        phone = getIntent().getStringExtra("phone");
        scanType = getIntent().getStringExtra("scanType");
        databaseHelper = new DatabaseHelper(this);

        barcodeView = findViewById(R.id.barcodeView);
        tvScanType = findViewById(R.id.tvScanType);

        if (barcodeView == null) {
            Toast.makeText(this, "Error: Scanner not initialized", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set scan type text
        if (tvScanType != null) {
            tvScanType.setText("Scanning for " + scanType);
        }

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scan QR Code - " + scanType);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        barcodeView.decodeContinuous(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null && !hasScanned) {
                hasScanned = true;
                barcodeView.pause();
                handleScan(result.getText());
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private void handleScan(String qrContent) {
        if (QRCodeGenerator.validateQRCode(qrContent, scanType)) {
            boolean success = databaseHelper.recordEntryExit(phone, scanType, null);

            if (success) {
                Toast.makeText(this, scanType + " recorded successfully!",
                        Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("success", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Failed to record " + scanType,
                        Toast.LENGTH_SHORT).show();
                hasScanned = false;
                barcodeView.resume();
            }
        } else {
            Toast.makeText(this, "Invalid or expired QR code",
                    Toast.LENGTH_LONG).show();
            hasScanned = false;
            barcodeView.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null && !hasScanned) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}