package com.example.hostelconnect;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class QRDisplayActivity extends AppCompatActivity {

    private ImageView ivQRCodeEntry, ivQRCodeExit;
    private TextView tvTimerEntry, tvTimerExit, tvQRStatusEntry, tvQRStatusExit;
    private CardView cardQREntry, cardQRExit;

    private CountDownTimer timerEntry, timerExit;
    private String currentQRContentEntry, currentQRContentExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        initializeViews();
        setupActionBar();
        generateAndDisplayQRCodes();
    }

    private void initializeViews() {
        ivQRCodeEntry = findViewById(R.id.ivQRCodeEntry);
        ivQRCodeExit = findViewById(R.id.ivQRCodeExit);
        tvTimerEntry = findViewById(R.id.tvTimerEntry);
        tvTimerExit = findViewById(R.id.tvTimerExit);
        tvQRStatusEntry = findViewById(R.id.tvQRStatusEntry);
        tvQRStatusExit = findViewById(R.id.tvQRStatusExit);
        cardQREntry = findViewById(R.id.cardQREntry);
        cardQRExit = findViewById(R.id.cardQRExit);
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("QR Code Scanner");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void generateAndDisplayQRCodes() {
        // Generate Entry QR Code
        generateEntryQRCode();

        // Generate Exit QR Code
        generateExitQRCode();

        // Start timers
        startEntryTimer();
        startExitTimer();
    }

    private void generateEntryQRCode() {
        currentQRContentEntry = QRCodeGenerator.generateQRContent("ENTRY");
        Bitmap qrBitmap = QRCodeGenerator.generateQRCodeBitmap(currentQRContentEntry, 400, 400);

        if (qrBitmap != null) {
            ivQRCodeEntry.setImageBitmap(qrBitmap);
            tvQRStatusEntry.setText("Active - Scan to mark Entry");
        }
    }

    private void generateExitQRCode() {
        currentQRContentExit = QRCodeGenerator.generateQRContent("EXIT");
        Bitmap qrBitmap = QRCodeGenerator.generateQRCodeBitmap(currentQRContentExit, 400, 400);

        if (qrBitmap != null) {
            ivQRCodeExit.setImageBitmap(qrBitmap);
            tvQRStatusExit.setText("Active - Scan to mark Exit");
        }
    }

    private void startEntryTimer() {
        long remainingSeconds = QRCodeGenerator.getRemainingSeconds(currentQRContentEntry);

        if (timerEntry != null) {
            timerEntry.cancel();
        }

        timerEntry = new CountDownTimer(remainingSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimerEntry.setText("Expires in: " + QRCodeGenerator.formatTime(seconds));
            }

            @Override
            public void onFinish() {
                tvTimerEntry.setText("Generating new code...");
                generateEntryQRCode();
                startEntryTimer();
            }
        }.start();
    }

    private void startExitTimer() {
        long remainingSeconds = QRCodeGenerator.getRemainingSeconds(currentQRContentExit);

        if (timerExit != null) {
            timerExit.cancel();
        }

        timerExit = new CountDownTimer(remainingSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimerExit.setText("Expires in: " + QRCodeGenerator.formatTime(seconds));
            }

            @Override
            public void onFinish() {
                tvTimerExit.setText("Generating new code...");
                generateExitQRCode();
                startExitTimer();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerEntry != null) {
            timerEntry.cancel();
        }
        if (timerExit != null) {
            timerExit.cancel();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}