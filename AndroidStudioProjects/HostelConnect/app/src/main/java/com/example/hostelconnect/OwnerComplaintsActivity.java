package com.example.hostelconnect;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OwnerComplaintsActivity extends AppCompatActivity {
    private RecyclerView rvComplaints;
    private TextView tvNoComplaints, btnBack, tvPendingCount, tvResolvedCount;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_complaints);
        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        loadComplaints();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvComplaints = findViewById(R.id.rvComplaints);
        tvNoComplaints = findViewById(R.id.tvNoComplaints);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadComplaints() {
        Cursor cursor = databaseHelper.getAllComplaints();
        int pending = 0, resolved = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                if ("Pending".equals(status)) pending++;
                else resolved++;
            } while (cursor.moveToNext());

            tvPendingCount.setText(String.valueOf(pending));
            tvResolvedCount.setText(String.valueOf(resolved));

            cursor.moveToFirst();
            tvNoComplaints.setVisibility(View.GONE);
            rvComplaints.setVisibility(View.VISIBLE);
            ComplaintAdapter adapter = new ComplaintAdapter(cursor);
            rvComplaints.setAdapter(adapter);
        } else {
            tvNoComplaints.setVisibility(View.VISIBLE);
            rvComplaints.setVisibility(View.GONE);
            tvPendingCount.setText("0");
            tvResolvedCount.setText("0");
        }
    }

    class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {
        private Cursor cursor;
        ComplaintAdapter(Cursor cursor) { this.cursor = cursor; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_complaint, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String complaint = cursor.getString(cursor.getColumnIndexOrThrow("complaint"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                holder.tvName.setText(name);
                holder.tvComplaint.setText(complaint);
                holder.tvStatus.setText(status);
                holder.tvDate.setText(date);

                if ("Pending".equals(status)) {
                    holder.btnResolve.setVisibility(View.VISIBLE);
                    holder.btnResolve.setOnClickListener(v -> resolveComplaint(id));
                } else {
                    holder.btnResolve.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvComplaint, tvStatus, tvDate;
            Button btnResolve;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvComplaint = itemView.findViewById(R.id.tvComplaint);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvDate = itemView.findViewById(R.id.tvDate);
                btnResolve = itemView.findViewById(R.id.btnResolve);
            }
        }
    }

    private void resolveComplaint(int complaintId) {
        new AlertDialog.Builder(this)
                .setTitle("Resolve Complaint")
                .setMessage("Mark this complaint as resolved?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = databaseHelper.updateComplaintStatus(complaintId, "Resolved");
                    if (success) {
                        Toast.makeText(this, "Complaint resolved", Toast.LENGTH_SHORT).show();
                        loadComplaints();
                    } else {
                        Toast.makeText(this, "Failed to update complaint", Toast.LENGTH_SHORT).show();
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
