package com.example.hostelconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.view.Gravity;

public class NoticesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFFF5F7FA);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView header = new TextView(this);
        header.setText("📢 Notices");
        header.setTextSize(24);
        header.setTextColor(0xFF1A1A2E);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(0, 0, 0, 40);
        layout.addView(header);

        DatabaseHelper db = new DatabaseHelper(this);
        Cursor cursor = db.getAllNotices();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundColor(0xFFFFFFFF);
                card.setPadding(40, 40, 40, 40);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.setMargins(0, 0, 0, 30);
                card.setLayoutParams(cardParams);

                TextView title = new TextView(this);
                title.setText(cursor.getString(cursor.getColumnIndex("title")));
                title.setTextSize(18);
                title.setTextColor(0xFF1A1A2E);
                title.setTypeface(null, android.graphics.Typeface.BOLD);
                card.addView(title);

                TextView message = new TextView(this);
                message.setText(cursor.getString(cursor.getColumnIndex("message")));
                message.setTextSize(14);
                message.setTextColor(0xFF6B7280);
                message.setPadding(0, 20, 0, 20);
                card.addView(message);

                TextView footer = new TextView(this);
                footer.setText("Posted by " + cursor.getString(cursor.getColumnIndex("posted_by")) +
                        " on " + cursor.getString(cursor.getColumnIndex("posted_date")));
                footer.setTextSize(12);
                footer.setTextColor(0xFF9CA3AF);
                card.addView(footer);

                layout.addView(card);
            } while (cursor.moveToNext());
            cursor.close();
        }

        scrollView.addView(layout);
        setContentView(scrollView);
    }
}