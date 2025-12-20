package com.example.hostelconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

public class RulesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFFF5F7FA);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView header = new TextView(this);
        header.setText("📋 Hostel Rules");
        header.setTextSize(24);
        header.setTextColor(0xFF1A1A2E);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(0, 0, 0, 40);
        layout.addView(header);

        String[] rules = {
                "Maintain silence after 10:00 PM",
                "Keep your room clean and organized",
                "No smoking or alcohol inside premises",
                "Register all visitors at security desk",
                "Pay rent before 5th of every month",
                "Report any maintenance issues immediately",
                "Respect common area facilities",
                "Follow entry and exit procedures",
                "No unauthorized guests after 9:00 PM",
                "Conserve water and electricity"
        };

        for (int i = 0; i < rules.length; i++) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(40, 30, 40, 30);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, 20);
            card.setLayoutParams(cardParams);

            TextView number = new TextView(this);
            number.setText((i + 1) + ".");
            number.setTextSize(16);
            number.setTextColor(0xFF667EEA);
            number.setTypeface(null, android.graphics.Typeface.BOLD);
            number.setPadding(0, 0, 30, 0);
            card.addView(number);

            TextView rule = new TextView(this);
            rule.setText(rules[i]);
            rule.setTextSize(15);
            rule.setTextColor(0xFF1A1A2E);
            card.addView(rule);

            layout.addView(card);
        }

        scrollView.addView(layout);
        setContentView(scrollView);
    }
}