package com.example.hostelconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ScrollView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ComplaintsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFFF5F7FA);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView header = new TextView(this);
        header.setText("📝 Complaints");
        header.setTextSize(24);
        header.setTextColor(0xFF1A1A2E);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(0, 0, 0, 40);
        layout.addView(header);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(40, 40, 40, 40);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 40);
        card.setLayoutParams(cardParams);

        TextView label = new TextView(this);
        label.setText("Describe your issue:");
        label.setTextSize(16);
        label.setTextColor(0xFF1A1A2E);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setPadding(0, 0, 0, 20);
        card.addView(label);

        EditText complaintInput = new EditText(this);
        complaintInput.setHint("Enter your complaint here...");
        complaintInput.setMinLines(5);
        complaintInput.setGravity(android.view.Gravity.TOP);
        complaintInput.setBackgroundColor(0xFFF5F7FA);
        complaintInput.setPadding(30, 30, 30, 30);
        card.addView(complaintInput);

        Button submitBtn = new Button(this);
        submitBtn.setText("Submit Complaint");
        submitBtn.setTextColor(0xFFFFFFFF);
        submitBtn.setBackgroundColor(0xFF667EEA);
        submitBtn.setPadding(0, 40, 0, 40);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 30, 0, 0);
        submitBtn.setLayoutParams(btnParams);
        submitBtn.setOnClickListener(v -> {
            String complaint = complaintInput.getText().toString().trim();
            if (!complaint.isEmpty()) {
                String phone = getIntent().getStringExtra("phone");
                String name = getIntent().getStringExtra("name");
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                DatabaseHelper db = new DatabaseHelper(this);
                if (db.insertComplaint(phone, name, complaint, date)) {
                    Toast.makeText(this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                    complaintInput.setText("");
                }
            } else {
                Toast.makeText(this, "Please enter your complaint", Toast.LENGTH_SHORT).show();
            }
        });
        card.addView(submitBtn);

        layout.addView(card);
        scrollView.addView(layout);
        setContentView(scrollView);
    }
}