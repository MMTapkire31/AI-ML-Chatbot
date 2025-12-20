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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OwnerMaintenanceActivity extends AppCompatActivity {
    private RecyclerView rvMaintenance;
    private TextView tvNoData, btnBack;
    private TextView tvPendingCount, tvInProgressCount, tvResolvedCount;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_maintenance);
        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        loadMaintenance();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvMaintenance = findViewById(R.id.rvMaintenance);
        tvNoData = findViewById(R.id.tvNoData);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        rvMaintenance.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadMaintenance() {
        Cursor cursor = databaseHelper.getAllMaintenanceRequests();
        int pending = 0, inProgress = 0, resolved = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                if ("Pending".equals(status)) pending++;
                else if ("In Progress".equals(status)) inProgress++;
                else if ("Resolved".equals(status)) resolved++;
            } while (cursor.moveToNext());

            tvPendingCount.setText(String.valueOf(pending));
            tvInProgressCount.setText(String.valueOf(inProgress));
            tvResolvedCount.setText(String.valueOf(resolved));

            cursor.moveToFirst();
            tvNoData.setVisibility(View.GONE);
            rvMaintenance.setVisibility(View.VISIBLE);
            MaintenanceAdapter adapter = new MaintenanceAdapter(cursor);
            rvMaintenance.setAdapter(adapter);
        } else {
            tvNoData.setVisibility(View.VISIBLE);
            rvMaintenance.setVisibility(View.GONE);
            tvPendingCount.setText("0");
            tvInProgressCount.setText("0");
            tvResolvedCount.setText("0");
        }
    }

    class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {
        private Cursor cursor;
        MaintenanceAdapter(Cursor cursor) { this.cursor = cursor; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_maintainance, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String room = cursor.getString(cursor.getColumnIndexOrThrow("room_number"));
                String issue = cursor.getString(cursor.getColumnIndexOrThrow("issue"));
                String priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                holder.tvName.setText(name);
                holder.tvRoom.setText("Room: " + room);
                holder.tvIssue.setText(issue);
                holder.tvPriority.setText(priority);
                holder.tvStatus.setText(status);

                if (!"Resolved".equals(status)) {
                    holder.btnResolve.setVisibility(View.VISIBLE);
                    holder.btnResolve.setOnClickListener(v -> resolveMaintenance(id));
                } else {
                    holder.btnResolve.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRoom, tvIssue, tvPriority, tvStatus;
            Button btnResolve;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvRoom = itemView.findViewById(R.id.tvRoom);
                tvIssue = itemView.findViewById(R.id.tvIssue);
                tvPriority = itemView.findViewById(R.id.tvPriority);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnResolve = itemView.findViewById(R.id.btnResolve);
            }
        }
    }

    private void resolveMaintenance(int maintenanceId) {
        new AlertDialog.Builder(this)
                .setTitle("Resolve Maintenance")
                .setMessage("Mark this maintenance request as resolved?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String resolvedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Calendar.getInstance().getTime());
                    boolean success = databaseHelper.updateMaintenanceStatus(maintenanceId, "Resolved", resolvedDate);
                    if (success) {
                        Toast.makeText(this, "Maintenance resolved", Toast.LENGTH_SHORT).show();
                        loadMaintenance();
                    } else {
                        Toast.makeText(this, "Failed to update maintenance", Toast.LENGTH_SHORT).show();
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