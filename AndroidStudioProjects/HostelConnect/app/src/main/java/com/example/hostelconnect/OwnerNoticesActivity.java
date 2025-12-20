package com.example.hostelconnect;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

public class OwnerNoticesActivity extends AppCompatActivity {
    private RecyclerView rvNotices;
    private TextView tvNoNotices, btnBack;
    private MaterialCardView cardNewNotice;
    private LinearLayout layoutNoticeForm;
    private EditText etTitle, etMessage;
    private Button btnPostNotice;
    private DatabaseHelper databaseHelper;
    private boolean isFormVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_notices);
        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        loadNotices();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvNotices = findViewById(R.id.rvNotices);
        tvNoNotices = findViewById(R.id.tvNoNotices);
        cardNewNotice = findViewById(R.id.cardNewNotice);
        layoutNoticeForm = findViewById(R.id.layoutNoticeForm);
        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        btnPostNotice = findViewById(R.id.btnPostNotice);
        rvNotices.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        cardNewNotice.setOnClickListener(v -> toggleNoticeForm());
        btnPostNotice.setOnClickListener(v -> postNotice());
    }

    private void toggleNoticeForm() {
        if (isFormVisible) {
            layoutNoticeForm.setVisibility(View.GONE);
            isFormVisible = false;
        } else {
            layoutNoticeForm.setVisibility(View.VISIBLE);
            isFormVisible = true;
        }
    }

    private void postNotice() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.insertNotice(title, message, "Owner");
        if (success) {
            Toast.makeText(this, "Notice posted successfully", Toast.LENGTH_SHORT).show();
            etTitle.setText("");
            etMessage.setText("");
            layoutNoticeForm.setVisibility(View.GONE);
            isFormVisible = false;
            loadNotices();
        } else {
            Toast.makeText(this, "Failed to post notice", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNotices() {
        Cursor cursor = databaseHelper.getAllNotices();
        if (cursor != null && cursor.getCount() > 0) {
            tvNoNotices.setVisibility(View.GONE);
            rvNotices.setVisibility(View.VISIBLE);
            NoticeAdapter adapter = new NoticeAdapter(cursor);
            rvNotices.setAdapter(adapter);
        } else {
            tvNoNotices.setVisibility(View.VISIBLE);
            rvNotices.setVisibility(View.GONE);
        }
    }

    class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {
        private Cursor cursor;
        NoticeAdapter(Cursor cursor) { this.cursor = cursor; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notices, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("posted_date"));

                holder.tvTitle.setText(title);
                holder.tvMessage.setText(message);
                holder.tvDate.setText(date);
                holder.btnDelete.setOnClickListener(v -> deleteNotice(id));
            }
        }

        @Override
        public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMessage, tvDate;
            Button btnDelete;
            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvDate = itemView.findViewById(R.id.tvDate);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    private void deleteNotice(int noticeId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Notice")
                .setMessage("Are you sure you want to delete this notice?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = databaseHelper.deleteNotice(noticeId);
                    if (success) {
                        Toast.makeText(this, "Notice deleted", Toast.LENGTH_SHORT).show();
                        loadNotices();
                    } else {
                        Toast.makeText(this, "Failed to delete notice", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}
