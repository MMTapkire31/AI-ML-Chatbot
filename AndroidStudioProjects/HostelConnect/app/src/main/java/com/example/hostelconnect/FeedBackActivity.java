package com.example.hostelconnect;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedBackActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private Button btnSubmitFeedback;
    private Spinner spinnerCategory;
    private RatingBar ratingBar;
    private EditText etFeedbackMessage;
    private TextView tvAverageRating, tvTotalFeedbacks;

    private DatabaseHelper databaseHelper;
    private String phone, name;
    private FeedbackAdapter adapter;
    private List<Feedback> feedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        phone = getIntent().getStringExtra("phone");
        name = getIntent().getStringExtra("name");
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupCategorySpinner();
        setupTabs();
        loadMyFeedbacks();
        setupClickListeners();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayoutFeedback);
        recyclerView = findViewById(R.id.recyclerViewFeedback);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);
        spinnerCategory = findViewById(R.id.spinnerFeedbackCategory);
        ratingBar = findViewById(R.id.ratingBarFeedback);
        etFeedbackMessage = findViewById(R.id.etFeedbackMessage);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalFeedbacks = findViewById(R.id.tvTotalFeedbacks);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackList = new ArrayList<>();
        adapter = new FeedbackAdapter(feedbackList);
        recyclerView.setAdapter(adapter);
    }

    private void setupCategorySpinner() {
        String[] categories = {"Food Quality", "Room Facilities", "Staff Behavior",
                "Cleanliness", "Security", "Maintenance", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Submit Feedback"));
        tabLayout.addTab(tabLayout.newTab().setText("My Feedbacks"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Show submit form
                    findViewById(R.id.layoutSubmitFeedback).setVisibility(android.view.View.VISIBLE);
                    recyclerView.setVisibility(android.view.View.GONE);
                } else {
                    // Show feedbacks list
                    findViewById(R.id.layoutSubmitFeedback).setVisibility(android.view.View.GONE);
                    recyclerView.setVisibility(android.view.View.VISIBLE);
                    loadMyFeedbacks();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String category = spinnerCategory.getSelectedItem().toString();
        float rating = ratingBar.getRating();
        String message = etFeedbackMessage.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());

        long result = databaseHelper.submitFeedback(
                phone, name, category, (int) rating, message, currentDate
        );

        if (result != -1) {
            Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();

            // Clear form
            ratingBar.setRating(0);
            etFeedbackMessage.setText("");
            spinnerCategory.setSelection(0);

            // Switch to feedbacks tab
            tabLayout.getTabAt(1).select();
        } else {
            Toast.makeText(this, "Error submitting feedback", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMyFeedbacks() {
        feedbackList.clear();
        Cursor cursor = databaseHelper.getFeedbackHistory(phone);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                int rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                feedbackList.add(new Feedback(id, category, rating, message, date, status));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
        updateStatistics();
    }

    private void updateStatistics() {
        // Calculate average rating from user's feedbacks
        double avgRating = 0;
        if (!feedbackList.isEmpty()) {
            int totalRating = 0;
            for (Feedback f : feedbackList) {
                totalRating += f.rating;
            }
            avgRating = (double) totalRating / feedbackList.size();
        }

        tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", avgRating));
        tvTotalFeedbacks.setText(String.valueOf(feedbackList.size()));
    }

    // Feedback Model Class
    private static class Feedback {
        int id;
        String category;
        int rating;
        String message;
        String date;
        String status;

        Feedback(int id, String category, int rating, String message,
                 String date, String status) {
            this.id = id;
            this.category = category;
            this.rating = rating;
            this.message = message;
            this.date = date;
            this.status = status;
        }
    }

    // Feedback Adapter
    private class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {
        private List<Feedback> feedbacks;

        FeedbackAdapter(List<Feedback> feedbacks) {
            this.feedbacks = feedbacks;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = getLayoutInflater().inflate(
                    R.layout.item_feedback, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Feedback feedback = feedbacks.get(position);

            holder.tvCategory.setText(feedback.category);
            holder.tvDate.setText(formatDate(feedback.date));
            holder.tvMessage.setText(feedback.message.isEmpty() ?
                    "No additional comments" : feedback.message);
            holder.ratingBar.setRating(feedback.rating);
            holder.tvStatus.setText(feedback.status);

            // Set status color
            int color = feedback.status.equals("Submitted") ?
                    android.R.color.holo_orange_dark :
                    android.R.color.holo_green_dark;
            holder.tvStatus.setTextColor(getResources().getColor(color, null));
        }

        @Override
        public int getItemCount() {
            return feedbacks.size();
        }

        private String formatDate(String date) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat(
                        "MMM dd, yyyy hh:mm a", Locale.getDefault());
                Date parsedDate = inputFormat.parse(date);
                return parsedDate != null ? outputFormat.format(parsedDate) : date;
            } catch (Exception e) {
                return date;
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategory, tvDate, tvMessage, tvStatus;
            RatingBar ratingBar;

            ViewHolder(android.view.View itemView) {
                super(itemView);
                tvCategory = itemView.findViewById(R.id.tvFeedbackCategory);
                tvDate = itemView.findViewById(R.id.tvFeedbackDate);
                tvMessage = itemView.findViewById(R.id.tvFeedbackMessage);
                tvStatus = itemView.findViewById(R.id.tvFeedbackStatus);
                ratingBar = itemView.findViewById(R.id.ratingBarItem);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}