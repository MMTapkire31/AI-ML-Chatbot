package com.example.hostelconnect;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class AbsentMarkReceiver extends BroadcastReceiver {

    private static final String TAG = "AbsentMarkReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AbsentMarkReceiver triggered - Marking absent for unmarked students");

        // Mark students as absent who haven't marked attendance
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.markAbsentForUnmarked();

        // Reschedule for next day
        scheduleAbsentMarking(context);
    }

    public static void scheduleAbsentMarking(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AbsentMarkReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                102, // Different request code from AttendanceCheckReceiver
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set time to 11:59 PM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule daily alarm
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
            Log.d(TAG, "Absent marking scheduled for 11:59 PM daily");
        }
    }

    public static void cancelAbsentMarking(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AbsentMarkReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Absent marking cancelled");
        }
    }
}