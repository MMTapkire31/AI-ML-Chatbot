package com.example.hostelconnect;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class QRCodeGenerator {

    // Secret key - keep this secure (in production, store in secure storage)
    private static final String SECRET_KEY = "HOSTEL_CONNECT_2024_SECRET";

    // QR code validity duration in minutes
    private static final int VALIDITY_MINUTES = 2;

    /**
     * Generate a time-based QR code that changes every VALIDITY_MINUTES
     * @param type "ENTRY" or "EXIT"
     * @return QR code content string
     */
    public static String generateQRContent(String type) {
        long currentTime = System.currentTimeMillis();

        // Round to nearest time window (e.g., every 2 minutes)
        long timeWindow = (currentTime / (VALIDITY_MINUTES * 60 * 1000)) * (VALIDITY_MINUTES * 60 * 1000);

        // Create QR content with timestamp and type
        String timestamp = String.valueOf(timeWindow);
        String data = type + "|" + timestamp + "|" + SECRET_KEY;

        // Generate hash for security
        String hash = generateHash(data);

        // Final QR content format: TYPE|TIMESTAMP|HASH
        return type + "|" + timestamp + "|" + hash;
    }

    /**
     * Validate scanned QR code
     * @param qrContent Content from scanned QR code
     * @param expectedType Expected type ("ENTRY" or "EXIT")
     * @return true if valid, false otherwise
     */
    public static boolean validateQRCode(String qrContent, String expectedType) {
        try {
            String[] parts = qrContent.split("\\|");
            if (parts.length != 3) {
                return false;
            }

            String type = parts[0];
            String timestamp = parts[1];
            String hash = parts[2];

            // Check if type matches
            if (!type.equals(expectedType)) {
                return false;
            }

            // Verify hash
            String data = type + "|" + timestamp + "|" + SECRET_KEY;
            String expectedHash = generateHash(data);

            if (!hash.equals(expectedHash)) {
                return false;
            }

            // Check if QR code is still valid (within time window)
            long qrTimestamp = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - qrTimestamp;

            // Valid for VALIDITY_MINUTES from creation
            return timeDiff >= 0 && timeDiff <= (VALIDITY_MINUTES * 60 * 1000);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate SHA-256 hash
     */
    private static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Generate QR code bitmap
     * @param content QR code content
     * @param width Bitmap width
     * @param height Bitmap height
     * @return QR code Bitmap
     */
    public static Bitmap generateQRCodeBitmap(String content, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get remaining seconds until QR code expires
     */
    public static long getRemainingSeconds(String qrContent) {
        try {
            String[] parts = qrContent.split("\\|");
            if (parts.length != 3) {
                return 0;
            }

            long qrTimestamp = Long.parseLong(parts[1]);
            long expiryTime = qrTimestamp + (VALIDITY_MINUTES * 60 * 1000);
            long currentTime = System.currentTimeMillis();

            long remaining = (expiryTime - currentTime) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Format time for display
     */
    public static String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
    }
}