package com.example.hostelconnect;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import java.util.Calendar;

public class AttendanceCheckReceiver extends BroadcastReceiver {

    private static final String TAG = "AttendanceCheckReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AttendanceCheckReceiver triggered");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reschedule alarm after device reboot
            scheduleAttendanceCheck(context);
        } else {
            // Handle attendance check
            performAttendanceCheck(context);
            // Reschedule for next check
            scheduleAttendanceCheck(context);
        }
    }

    private void performAttendanceCheck(Context context) {
        // Your attendance check logic here
        Log.d(TAG, "Performing attendance check");

        // TODO: Add your actual attendance check logic
        // Example: Check database, show notification, etc.
    }

    public static void scheduleAttendanceCheck(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        // Check if we can schedule exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. Using inexact alarm instead.");
                scheduleInexactAlarm(context, alarmManager);
                return;
            }
        }

        // Schedule exact alarm
        Intent intent = new Intent(context, AttendanceCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set alarm for next day at 10 PM (22:00)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If time has passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
            Log.d(TAG, "Exact alarm scheduled successfully for: " + calendar.getTime());
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException scheduling exact alarm: " + e.getMessage());
            // Fallback to inexact alarm
            scheduleInexactAlarm(context, alarmManager);
        }
    }

    /**
     * Fallback method using inexact alarms (doesn't require special permission)
     * This is the RECOMMENDED approach for most use cases
     */
    private static void scheduleInexactAlarm(Context context, AlarmManager alarmManager) {
        Intent intent = new Intent(context, AttendanceCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set inexact repeating alarm (doesn't require SCHEDULE_EXACT_ALARM permission)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Use setInexactRepeating (doesn't require special permission)
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );

        Log.d(TAG, "Inexact alarm scheduled successfully for approximately: " + calendar.getTime());
    }

    public static void cancelAttendanceCheck(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AttendanceCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Attendance check cancelled");
        }
    }
}