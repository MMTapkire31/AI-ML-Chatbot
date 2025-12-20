package com.example.hostelconnect;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Worker that automatically checks location and marks attendance at 9 PM daily
 * Location: Om Mangal Girls Hostel, Hanuman Nagar, near VIIT College, Pune
 */
public class AutoAttendanceWorker extends Worker {

    private static final String TAG = "AutoAttendanceWorker";

    // Om Mangal Girls Hostel location coordinates (Near VIIT College, Kondhwa Budruk, Pune)
    // Address: Sr.no. 5 Hanuman nagar, Patan chowk, VIIT College Rd, Pune 411048
    private static final double HOSTEL_LATITUDE = 18.4614;   // Kondhwa Budruk area coordinates
    private static final double HOSTEL_LONGITUDE = 73.8854;  // Near VIIT College Road
    private static final float HOSTEL_RADIUS_METERS = 250.0f; // 250 meters radius (covers hostel area)

    public AutoAttendanceWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "=== AutoAttendanceWorker started at 9 PM ===");

            // Get currently logged-in user's phone from SharedPreferences
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences("HostelConnectPrefs", Context.MODE_PRIVATE);
            String currentUserPhone = prefs.getString("phone", null);

            if (currentUserPhone == null || currentUserPhone.isEmpty()) {
                Log.e(TAG, "No user logged in. Skipping attendance check.");
                return Result.failure();
            }

            DatabaseHelper db = new DatabaseHelper(getApplicationContext());
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            // Get currently logged-in user's info
            HostellerInfo currentUser = getHostellerInfo(db, currentUserPhone);

            if (currentUser == null) {
                Log.e(TAG, "User not found in database: " + currentUserPhone);
                return Result.failure();
            }

            Log.d(TAG, "Checking attendance for: " + currentUser.name + " (" + currentUserPhone + ")");

            // ALWAYS check current location and mark attendance
            boolean isPresent = checkLocationAndMarkAttendance(db, currentUser, today);

            Log.d(TAG, "=== Worker completed. Status: " + (isPresent ? "PRESENT" : "ABSENT") + " ===");

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error in AutoAttendanceWorker: " + e.getMessage(), e);
            return Result.failure();
        }
    }

    /**
     * Get hosteller info for a specific phone number
     */
    private HostellerInfo getHostellerInfo(DatabaseHelper db, String phone) {
        Cursor cursor = null;
        try {
            cursor = db.getHostellerByPhone(phone);
            if (cursor != null && cursor.moveToFirst()) {
                int phoneIndex = cursor.getColumnIndex("phone");
                int nameIndex = cursor.getColumnIndex("name");

                if (phoneIndex != -1 && nameIndex != -1) {
                    HostellerInfo info = new HostellerInfo();
                    info.phone = cursor.getString(phoneIndex);
                    info.name = cursor.getString(nameIndex);
                    return info;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting hosteller info: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Check if hosteller is within hostel premises and mark attendance
     */
    private boolean checkLocationAndMarkAttendance(DatabaseHelper db, HostellerInfo hosteller, String today) {
        try {
            Log.d(TAG, "Starting location check for: " + hosteller.name);

            // Check if location permission is granted
            if (!hasLocationPermission()) {
                Log.w(TAG, "Location permission not granted");
                markAttendance(db, hosteller.phone, today, "ABSENT");
                sendNotification(hosteller.phone, hosteller.name, false,
                        "location permission is required. Please enable it in app settings.");
                return false;
            }

            // Get current location
            Location location = getLastKnownLocation();

            if (location == null) {
                Log.w(TAG, "Unable to get location");
                markAttendance(db, hosteller.phone, today, "ABSENT");
                sendNotification(hosteller.phone, hosteller.name, false,
                        "could not determine your location. Please ensure GPS is enabled.");
                return false;
            }

            // Log location details
            Log.d(TAG, "Current location: " + location.getLatitude() + ", " + location.getLongitude());
            Log.d(TAG, "Location accuracy: " + location.getAccuracy() + "m");

            // Calculate distance from hostel
            float distance = calculateDistance(location);
            Log.d(TAG, "Distance from Om Mangal Hostel: " + String.format("%.2f", distance) + "m (threshold: " + HOSTEL_RADIUS_METERS + "m)");

            // Check if within hostel premises
            boolean isWithinHostel = distance <= HOSTEL_RADIUS_METERS;

            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            if (isWithinHostel) {
                // Mark as PRESENT
                boolean success = markAttendance(db, hosteller.phone, today, "PRESENT");
                if (success) {
                    Log.d(TAG, "✓ Marked PRESENT (within " + String.format("%.0fm", distance) + " of hostel)");
                    sendNotification(hosteller.phone, hosteller.name, true,
                            "you are present in Om Mangal Girls Hostel (" + String.format("%.0fm", distance) + " away). Your attendance is marked as PRESENT! ✓");
                    return true;
                } else {
                    Log.e(TAG, "Failed to mark attendance in database");
                    return false;
                }
            } else {
                // Mark as ABSENT
                boolean success = markAttendance(db, hosteller.phone, today, "ABSENT");
                if (success) {
                    Log.d(TAG, "✗ Marked ABSENT (outside hostel: " + String.format("%.0fm", distance) + ")");
                    sendNotification(hosteller.phone, hosteller.name, false,
                            "you are " + String.format("%.0fm", distance) + " meters away from Om Mangal Girls Hostel. Your attendance is marked as ABSENT. ✗");
                } else {
                    Log.e(TAG, "Failed to mark attendance in database");
                }
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error checking location: " + e.getMessage(), e);
            markAttendance(db, hosteller.phone, today, "ABSENT");
            sendNotification(hosteller.phone, hosteller.name, false,
                    "an error occurred while checking your location.");
            return false;
        }
    }

    /**
     * Calculate distance from hostel location
     */
    private float calculateDistance(Location userLocation) {
        Location hostelLocation = new Location("Om_Mangal_Hostel");
        hostelLocation.setLatitude(HOSTEL_LATITUDE);
        hostelLocation.setLongitude(HOSTEL_LONGITUDE);
        return userLocation.distanceTo(hostelLocation);
    }

    /**
     * Get last known location using Android's LocationManager
     */
    private Location getLastKnownLocation() {
        try {
            if (!hasLocationPermission()) {
                Log.w(TAG, "Location permission check failed");
                return null;
            }

            LocationManager locationManager = (LocationManager)
                    getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            if (locationManager == null) {
                Log.e(TAG, "LocationManager is null");
                return null;
            }

            // Try GPS first (most accurate)
            Location gpsLocation = null;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation != null) {
                    Log.d(TAG, "GPS location available (accuracy: " + gpsLocation.getAccuracy() + "m)");
                }
            } else {
                Log.w(TAG, "GPS provider is disabled");
            }

            // Try Network location as backup
            Location networkLocation = null;
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (networkLocation != null) {
                    Log.d(TAG, "Network location available (accuracy: " + networkLocation.getAccuracy() + "m)");
                }
            } else {
                Log.w(TAG, "Network provider is disabled");
            }

            // Return the most recent/accurate location
            if (gpsLocation != null && networkLocation != null) {
                long gpsAge = System.currentTimeMillis() - gpsLocation.getTime();
                long networkAge = System.currentTimeMillis() - networkLocation.getTime();

                Log.d(TAG, "GPS age: " + (gpsAge / 1000) + "s, Network age: " + (networkAge / 1000) + "s");

                // Prefer GPS if it's more recent (within 2 minutes)
                if (gpsAge < 120000) { // 2 minutes
                    Log.d(TAG, "Using GPS location (recent and accurate)");
                    return gpsLocation;
                } else if (gpsLocation.getAccuracy() < networkLocation.getAccuracy()) {
                    Log.d(TAG, "Using GPS location (more accurate)");
                    return gpsLocation;
                } else {
                    Log.d(TAG, "Using Network location");
                    return networkLocation;
                }
            } else if (gpsLocation != null) {
                Log.d(TAG, "Using GPS location (only available)");
                return gpsLocation;
            } else if (networkLocation != null) {
                Log.d(TAG, "Using Network location (only available)");
                return networkLocation;
            }

            Log.e(TAG, "No location available from any provider");
            return null;

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting location: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if location permission is granted
     */
    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Mark attendance in database
     * Returns true if successful
     */
    private boolean markAttendance(DatabaseHelper db, String phone, String date, String status) {
        try {
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            boolean success = db.markDailyAttendance(phone, date, status, currentTime);

            if (success) {
                Log.d(TAG, "✓ Database updated: " + phone + " - " + status);
            } else {
                Log.e(TAG, "✗ Failed to update database for: " + phone);
            }

            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error marking attendance: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send notification to user
     */
    private void sendNotification(String phone, String name, boolean isPresent, String message) {
        try {
            String channelId = "attendance_channel";
            NotificationManager notificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager is null");
                return;
            }

            // Create notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Attendance Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Daily automatic attendance notifications at 9 PM");
                notificationManager.createNotificationChannel(channel);
            }

            // Get first name
            String firstName = name;
            if (name != null && name.contains(" ")) {
                String[] parts = name.trim().split("\\s+");
                firstName = parts[0];
            }

            // Build notification
            String title = isPresent ? "✓ Attendance: PRESENT" : "✗ Attendance: ABSENT";
            String content = "Hi " + firstName + ", " + message;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500, 250, 500})
                    .setColor(getApplicationContext().getResources().getColor(
                            isPresent ? android.R.color.holo_green_dark : android.R.color.holo_red_dark,
                            null
                    ));

            // Use timestamp as notification ID to ensure uniqueness
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());

            Log.d(TAG, "✓ Notification sent: " + title + " to " + firstName);

        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage(), e);
        }
    }

    /**
     * Helper class to store hosteller information
     */
    private static class HostellerInfo {
        String phone;
        String name;
    }
}