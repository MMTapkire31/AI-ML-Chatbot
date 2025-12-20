package com.example.hostelconnect;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;

public class OwnerFeedbacksActivity extends AppCompatActivity {
    private RecyclerView rvFeedbacks;
    private TextView tvNoFeedbacks, btnBack;
    private TextView tvAverageRating, tvTotalFeedbacks;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_feedbacks);
        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        loadFeedbacks();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvFeedbacks = findViewById(R.id.rvFeedbacks);
        tvNoFeedbacks = findViewById(R.id.tvNoFeedbacks);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalFeedbacks = findViewById(R.id.tvTotalFeedbacks);
        rvFeedbacks.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadFeedbacks() {
        Cursor cursor = databaseHelper.getAllFeedbacksForOwner();
        double avgRating = databaseHelper.getAverageRatingForOwner();

        tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", avgRating));
        tvTotalFeedbacks.setText(String.valueOf(cursor != null ? cursor.getCount() : 0));

        if (cursor != null && cursor.getCount() > 0) {
            tvNoFeedbacks.setVisibility(View.GONE);
            rvFeedbacks.setVisibility(View.VISIBLE);
            FeedbackAdapter adapter = new FeedbackAdapter(cursor);
            rvFeedbacks.setAdapter(adapter);
        } else {
            tvNoFeedbacks.setVisibility(View.VISIBLE);
            rvFeedbacks.setVisibility(View.GONE);
        }
    }

    class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {
        private Cursor cursor;
        FeedbackAdapter(Cursor cursor) { this.cursor = cursor; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_feedback1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                int rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                holder.tvName.setText(name);
                holder.tvCategory.setText(category);
                holder.tvRating.setText(String.valueOf(rating) + " ⭐");
                holder.tvMessage.setText(message);
                holder.tvDate.setText(date);
            }
        }

        @Override
        public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCategory, tvRating, tvMessage, tvDate;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}