package com.example.hostelconnect;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OwnerVisitorsActivity extends AppCompatActivity {
    private RecyclerView rvVisitors;
    private TextView tvNoVisitors, btnBack;
    private TextView tvPendingCount, tvApprovedCount, tvTotalCount;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_visitors);
        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        loadVisitors();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvVisitors = findViewById(R.id.rvVisitors);
        tvNoVisitors = findViewById(R.id.tvNoVisitors);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        rvVisitors.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadVisitors() {
        Cursor cursor = databaseHelper.getAllVisitorsForOwner();
        int pending = 0, approved = 0, total = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                total++;
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                if ("Pending".equals(status)) pending++;
                else if ("Approved".equals(status)) approved++;
            } while (cursor.moveToNext());

            tvPendingCount.setText(String.valueOf(pending));
            tvApprovedCount.setText(String.valueOf(approved));
            tvTotalCount.setText(String.valueOf(total));

            cursor.moveToFirst();
            tvNoVisitors.setVisibility(View.GONE);
            rvVisitors.setVisibility(View.VISIBLE);
            VisitorAdapter adapter = new VisitorAdapter(cursor);
            rvVisitors.setAdapter(adapter);
        } else {
            tvNoVisitors.setVisibility(View.VISIBLE);
            rvVisitors.setVisibility(View.GONE);
            tvPendingCount.setText("0");
            tvApprovedCount.setText("0");
            tvTotalCount.setText("0");
        }
    }

    class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.ViewHolder> {
        private Cursor cursor;
        VisitorAdapter(Cursor cursor) { this.cursor = cursor; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_visitor_owner, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String hostellerName = cursor.getString(cursor.getColumnIndexOrThrow("hosteller_name"));
                String visitorName = cursor.getString(cursor.getColumnIndexOrThrow("visitor_name"));
                String visitorPhone = cursor.getString(cursor.getColumnIndexOrThrow("visitor_phone"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String visitDate = cursor.getString(cursor.getColumnIndexOrThrow("visit_date"));

                holder.tvHostellerName.setText("Hosteller: " + hostellerName);
                holder.tvVisitorName.setText(visitorName);
                holder.tvVisitorPhone.setText(visitorPhone);
                holder.tvStatus.setText(status);
                holder.tvVisitDate.setText(visitDate);

                if ("Pending".equals(status)) {
                    holder.layoutActions.setVisibility(View.VISIBLE);
                    holder.btnApprove.setOnClickListener(v -> handleVisitor(id, "Approved"));
                    holder.btnReject.setOnClickListener(v -> handleVisitor(id, "Rejected"));
                } else {
                    holder.layoutActions.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHostellerName, tvVisitorName, tvVisitorPhone, tvStatus, tvVisitDate;
            LinearLayout layoutActions;
            Button btnApprove, btnReject;
            ViewHolder(View itemView) {
                super(itemView);
                tvHostellerName = itemView.findViewById(R.id.tvHostellerName);
                tvVisitorName = itemView.findViewById(R.id.tvVisitorName);
                tvVisitorPhone = itemView.findViewById(R.id.tvVisitorPhone);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvVisitDate = itemView.findViewById(R.id.tvVisitDate);
                layoutActions = itemView.findViewById(R.id.layoutActions);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }

    private void handleVisitor(int visitorId, String action) {
        new AlertDialog.Builder(this)
                .setTitle(action + " Visitor")
                .setMessage("Are you sure you want to " + action.toLowerCase() + " this visitor request?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = databaseHelper.updateVisitorStatus(visitorId, action);
                    if (success) {
                        Toast.makeText(this, "Visitor " + action.toLowerCase(), Toast.LENGTH_SHORT).show();
                        loadVisitors();
                    } else {
                        Toast.makeText(this, "Failed to update visitor status", Toast.LENGTH_SHORT).show();
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
