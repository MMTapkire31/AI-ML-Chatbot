package com.example.hostelconnect;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class VisitorManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddVisitor;
    private DatabaseHelper databaseHelper;
    private String phone;
    private VisitorAdapter adapter;
    private List<VisitorEntry> visitorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_management);

        phone = getIntent().getStringExtra("phone");
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        loadVisitors();
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewVisitors);
        fabAddVisitor = findViewById(R.id.fabAddVisitor);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        visitorList = new ArrayList<>();
        adapter = new VisitorAdapter(visitorList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        fabAddVisitor.setOnClickListener(v -> showAddVisitorDialog());
    }

    private void loadVisitors() {
        visitorList.clear();
        Cursor cursor = databaseHelper.getVisitorHistory(phone);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String visitorName = cursor.getString(cursor.getColumnIndexOrThrow("visitor_name"));
                String visitorPhone = cursor.getString(cursor.getColumnIndexOrThrow("visitor_phone"));
                String purpose = cursor.getString(cursor.getColumnIndexOrThrow("purpose"));
                String visitDate = cursor.getString(cursor.getColumnIndexOrThrow("visit_date"));
                String visitTime = cursor.getString(cursor.getColumnIndexOrThrow("visit_time"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                visitorList.add(new VisitorEntry(id, visitorName, visitorPhone, purpose, visitDate, visitTime, status));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void showAddVisitorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_visitor, null);
        builder.setView(dialogView);

        EditText etVisitorName = dialogView.findViewById(R.id.etVisitorName);
        EditText etVisitorPhone = dialogView.findViewById(R.id.etVisitorPhone);
        EditText etPurpose = dialogView.findViewById(R.id.etPurpose);
        EditText etVisitDate = dialogView.findViewById(R.id.etVisitDate);
        EditText etVisitTime = dialogView.findViewById(R.id.etVisitTime);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitVisitor);

        // Date Picker
        etVisitDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        etVisitDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Time Picker
        etVisitTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        etVisitTime.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            String visitorName = etVisitorName.getText().toString().trim();
            String visitorPhone = etVisitorPhone.getText().toString().trim();
            String purpose = etPurpose.getText().toString().trim();
            String visitDate = etVisitDate.getText().toString().trim();
            String visitTime = etVisitTime.getText().toString().trim();

            if (visitorName.isEmpty()) {
                etVisitorName.setError("Required");
                return;
            }

            if (visitorPhone.isEmpty() || visitorPhone.length() < 10) {
                etVisitorPhone.setError("Valid phone required");
                return;
            }

            if (visitDate.isEmpty()) {
                Toast.makeText(this, "Please select visit date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (visitTime.isEmpty()) {
                Toast.makeText(this, "Please select visit time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add visitor entry to database
            long result = databaseHelper.addVisitorEntry(phone, visitorName, visitorPhone, purpose, visitDate, visitTime);

            if (result != -1) {
                Toast.makeText(this, "Visitor entry added successfully", Toast.LENGTH_SHORT).show();
                loadVisitors();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Error adding visitor entry", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // VisitorEntry Model
    private static class VisitorEntry {
        int id;
        String visitorName;
        String visitorPhone;
        String purpose;
        String visitDate;
        String visitTime;
        String status;

        VisitorEntry(int id, String visitorName, String visitorPhone, String purpose,
                     String visitDate, String visitTime, String status) {
            this.id = id;
            this.visitorName = visitorName;
            this.visitorPhone = visitorPhone;
            this.purpose = purpose;
            this.visitDate = visitDate;
            this.visitTime = visitTime;
            this.status = status;
        }
    }

    // Visitor Adapter
    private class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.ViewHolder> {
        private List<VisitorEntry> visitors;

        VisitorAdapter(List<VisitorEntry> visitors) {
            this.visitors = visitors;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_visitor, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            VisitorEntry visitor = visitors.get(position);

            holder.tvVisitorName.setText(visitor.visitorName);
            holder.tvVisitorPhone.setText(visitor.visitorPhone);
            holder.tvPurpose.setText(visitor.purpose.isEmpty() ? "No purpose specified" : visitor.purpose);
            holder.tvVisitDateTime.setText(visitor.visitDate + " at " + visitor.visitTime);
            holder.tvStatus.setText(visitor.status);

            // Set status color
            int color;
            switch (visitor.status) {
                case "Pending":
                    color = android.R.color.holo_orange_dark;
                    break;
                case "Approved":
                    color = android.R.color.holo_green_dark;
                    break;
                case "Rejected":
                    color = android.R.color.holo_red_dark;
                    break;
                case "Completed":
                    color = android.R.color.darker_gray;
                    break;
                default:
                    color = android.R.color.darker_gray;
            }
            holder.tvStatus.setTextColor(getResources().getColor(color, null));
        }

        @Override
        public int getItemCount() {
            return visitors.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvVisitorName, tvVisitorPhone, tvPurpose, tvVisitDateTime, tvStatus;

            ViewHolder(View itemView) {
                super(itemView);
                tvVisitorName = itemView.findViewById(R.id.tvVisitorName);
                tvVisitorPhone = itemView.findViewById(R.id.tvVisitorPhone);
                tvPurpose = itemView.findViewById(R.id.tvPurpose);
                tvVisitDateTime = itemView.findViewById(R.id.tvVisitDateTime);
                tvStatus = itemView.findViewById(R.id.tvVisitorStatus);
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