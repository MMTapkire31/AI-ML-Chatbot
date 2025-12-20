package com.example.hostelconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendaceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;
    private DatabaseHelper databaseHelper;
    private String phone;
    private TextView tvTotalDays, tvPresentDays, tvPercentage, tvAbsentDays;
    private ToggleButton toggleViewMode;
    private boolean showDailyView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendace);

        phone = getIntent().getStringExtra("phone");
        databaseHelper = new DatabaseHelper(this);

        tvTotalDays = findViewById(R.id.tvTotalDays);
        tvPresentDays = findViewById(R.id.tvPresentDays);
        tvAbsentDays = findViewById(R.id.tvAbsentDays);
        tvPercentage = findViewById(R.id.tvPercentage);
        toggleViewMode = findViewById(R.id.toggleViewMode);
        recyclerView = findViewById(R.id.recyclerViewAttendance);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        toggleViewMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showDailyView = isChecked;
            loadAttendanceData();
        });

        loadAttendanceData();
    }

    private void loadAttendanceData() {
        if (showDailyView) {
            loadDailyAttendance();
        } else {
            loadEntryExitLogs();
        }
    }

    private void loadDailyAttendance() {
        List<DailyAttendanceRecord> records = new ArrayList<>();
        Cursor cursor = databaseHelper.getDailyAttendance(phone);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String status = cursor.getString(cursor.getColumnIndex("status"));
                String checkTime = cursor.getString(cursor.getColumnIndex("check_time"));
                String entryTime = cursor.getString(cursor.getColumnIndex("entry_time"));
                String exitTime = cursor.getString(cursor.getColumnIndex("exit_time"));
                int totalEntries = cursor.getInt(cursor.getColumnIndex("total_entries"));
                int totalExits = cursor.getInt(cursor.getColumnIndex("total_exits"));

                records.add(new DailyAttendanceRecord(date, status, checkTime,
                        entryTime, exitTime, totalEntries, totalExits));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new AttendanceAdapter(records, true);
        recyclerView.setAdapter(adapter);

        // Calculate stats from daily attendance
        int totalDays = records.size();
        int presentDays = 0;
        for (DailyAttendanceRecord record : records) {
            if ("PRESENT".equals(record.status)) {
                presentDays++;
            }
        }
        int absentDays = totalDays - presentDays;
        int percentage = totalDays > 0 ? (presentDays * 100 / totalDays) : 0;

        tvTotalDays.setText(String.valueOf(totalDays));
        tvPresentDays.setText(String.valueOf(presentDays));
        tvAbsentDays.setText(String.valueOf(absentDays));
        tvPercentage.setText(percentage + "%");
    }

    private void loadEntryExitLogs() {
        List<EntryExitRecord> records = new ArrayList<>();
        Cursor cursor = databaseHelper.getEntryExitRecords(phone);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
                String reason = cursor.getString(cursor.getColumnIndex("reason"));

                records.add(new EntryExitRecord(type, timestamp, reason));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new AttendanceAdapter(records, false);
        recyclerView.setAdapter(adapter);
    }

    // Daily attendance record class
    private static class DailyAttendanceRecord {
        String date;
        String status;
        String checkTime;
        String entryTime;
        String exitTime;
        int totalEntries;
        int totalExits;

        DailyAttendanceRecord(String date, String status, String checkTime,
                              String entryTime, String exitTime,
                              int totalEntries, int totalExits) {
            this.date = date;
            this.status = status;
            this.checkTime = checkTime;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
            this.totalEntries = totalEntries;
            this.totalExits = totalExits;
        }
    }

    // Entry/Exit record class
    private static class EntryExitRecord {
        String type;
        String timestamp;
        String reason;

        EntryExitRecord(String type, String timestamp, String reason) {
            this.type = type;
            this.timestamp = timestamp;
            this.reason = reason;
        }
    }

    // Unified RecyclerView Adapter
    private class AttendanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_DAILY = 0;
        private static final int VIEW_TYPE_ENTRY_EXIT = 1;

        private List<?> records;
        private boolean isDailyView;

        AttendanceAdapter(List<?> records, boolean isDailyView) {
            this.records = records;
            this.isDailyView = isDailyView;
        }

        @Override
        public int getItemViewType(int position) {
            return isDailyView ? VIEW_TYPE_DAILY : VIEW_TYPE_ENTRY_EXIT;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_DAILY) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_daily_attendance, parent, false);
                return new DailyViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_attendance_hosteller, parent, false);
                return new EntryExitViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof DailyViewHolder) {
                DailyAttendanceRecord record = (DailyAttendanceRecord) records.get(position);
                ((DailyViewHolder) holder).bind(record);
            } else if (holder instanceof EntryExitViewHolder) {
                EntryExitRecord record = (EntryExitRecord) records.get(position);
                ((EntryExitViewHolder) holder).bind(record);
            }
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        // ViewHolder for daily attendance
        class DailyViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvStatus, tvCheckTime, tvEntryTime, tvExitTime, tvMovements;

            DailyViewHolder(View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvCheckTime = itemView.findViewById(R.id.tvCheckTime);
                tvEntryTime = itemView.findViewById(R.id.tvEntryTime);
                tvExitTime = itemView.findViewById(R.id.tvExitTime);
                tvMovements = itemView.findViewById(R.id.tvMovements);
            }

            void bind(DailyAttendanceRecord record) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = sdf.parse(record.date);
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, EEEE",
                            Locale.getDefault());
                    tvDate.setText(displayFormat.format(date));
                } catch (ParseException e) {
                    tvDate.setText(record.date);
                }

                tvStatus.setText(record.status);
                tvStatus.setTextColor(record.status.equals("PRESENT") ?
                        getResources().getColor(android.R.color.holo_green_dark) :
                        getResources().getColor(android.R.color.holo_red_dark));

                if (record.checkTime != null) {
                    tvCheckTime.setText("Checked at: " + formatTime(record.checkTime));
                }

                if (record.entryTime != null) {
                    tvEntryTime.setText("First Entry: " + formatTime(record.entryTime));
                    tvEntryTime.setVisibility(View.VISIBLE);
                } else {
                    tvEntryTime.setVisibility(View.GONE);
                }

                if (record.exitTime != null) {
                    tvExitTime.setText("Last Exit: " + formatTime(record.exitTime));
                    tvExitTime.setVisibility(View.VISIBLE);
                } else {
                    tvExitTime.setVisibility(View.GONE);
                }

                tvMovements.setText("Entries: " + record.totalEntries +
                        " | Exits: " + record.totalExits);
            }
        }

        // ViewHolder for entry/exit logs
        class EntryExitViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvTimestamp, tvReason;

            EntryExitViewHolder(View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvType);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
                tvReason = itemView.findViewById(R.id.tvReason);
            }

            void bind(EntryExitRecord record) {
                tvType.setText(record.type);
                tvType.setTextColor(record.type.equals("ENTRY") ?
                        getResources().getColor(android.R.color.holo_green_dark) :
                        getResources().getColor(android.R.color.holo_red_dark));

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault());
                    Date date = sdf.parse(record.timestamp);
                    SimpleDateFormat displayFormat = new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a", Locale.getDefault());
                    tvTimestamp.setText(displayFormat.format(date));
                } catch (ParseException e) {
                    tvTimestamp.setText(record.timestamp);
                }

                if (record.reason != null && !record.reason.isEmpty()) {
                    tvReason.setVisibility(View.VISIBLE);
                    tvReason.setText("Reason: " + record.reason);
                } else {
                    tvReason.setVisibility(View.GONE);
                }
            }
        }

        private String formatTime(String timestamp) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Date date = sdf.parse(timestamp);
                SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a",
                        Locale.getDefault());
                return displayFormat.format(date);
            } catch (ParseException e) {
                return timestamp;
            }
        }
    }
}