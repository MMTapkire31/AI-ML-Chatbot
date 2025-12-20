package com.example.hostelconnect;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String LEAVE_CHANNEL_ID = "leave_notifications";
    private static final String LEAVE_CHANNEL_NAME = "Leave Request Updates";
    private static final String LEAVE_CHANNEL_DESC = "Notifications for leave request status updates";

    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    LEAVE_CHANNEL_ID,
                    LEAVE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(LEAVE_CHANNEL_DESC);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Send notification when leave is approved
     */
    public void sendLeaveApprovedNotification(String phone, String fromDate, String toDate) {
        String title = "✓ Leave Request Approved";
        String message = "Your leave request from " + fromDate + " to " + toDate + " has been approved.";

        sendLeaveNotification(phone, title, message, 1);
    }

    /**
     * Send notification when leave is rejected
     */
    public void sendLeaveRejectedNotification(String phone, String fromDate, String toDate) {
        String title = "✗ Leave Request Rejected";
        String message = "Your leave request from " + fromDate + " to " + toDate + " has been rejected.";

        sendLeaveNotification(phone, title, message, 2);
    }

    /**
     * Send leave status notification
     */
    private void sendLeaveNotification(String phone, String title, String message, int notificationId) {
        try {
            // Create intent to open LeaveRequestActivity when notification is tapped
            Intent intent = new Intent(context, LeaveRequestActivity.class);
            intent.putExtra("phone", phone);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, LEAVE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{0, 500, 250, 500})
                    .setDefaults(NotificationCompat.DEFAULT_SOUND);

            // Show notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // Check if notification permission is granted (for Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.app.ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(notificationId, builder.build());
                }
            } else {
                notificationManager.notify(notificationId, builder.build());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send notification for new leave request (for owner)
     */
    public void sendNewLeaveRequestNotification(String studentName, String fromDate, String toDate) {
        try {
            String title = "New Leave Request";
            String message = studentName + " has requested leave from " + fromDate + " to " + toDate;

            Intent intent = new Intent(context, OwnerLeaveRequestsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    100,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, LEAVE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.app.ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(100, builder.build());
                }
            } else {
                notificationManager.notify(100, builder.build());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}