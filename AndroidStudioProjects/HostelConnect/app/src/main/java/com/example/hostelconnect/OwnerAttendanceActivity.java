package com.example.hostelconnect;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OwnerAttendanceActivity extends AppCompatActivity {
    private RecyclerView rvAttendance;
    private TextView tvNoData, btnBack, tvSelectedDate;
    private TextView tvPresentCount, tvAbsentCount, tvTotalCount;
    private CardView cardSelectDate;
    private DatabaseHelper databaseHelper;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_attendance);
        databaseHelper = new DatabaseHelper(this);
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        initializeViews();
        setupClickListeners();
        loadAttendance();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvAttendance = findViewById(R.id.rvAttendance);
        tvNoData = findViewById(R.id.tvNoData);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        cardSelectDate = findViewById(R.id.cardSelectDate);
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        updateDateDisplay();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        cardSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            updateDateDisplay();
            loadAttendance();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateDisplay() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
            tvSelectedDate.setText(outputFormat.format(inputFormat.parse(selectedDate)));
        } catch (Exception e) {
            tvSelectedDate.setText(selectedDate);
        }
    }

    private void loadAttendance() {
        Cursor cursor = databaseHelper.getAttendanceSummaryByDate(selectedDate);
        int present = 0, absent = 0, total = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                total++;
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                if ("PRESENT".equals(status)) present++;
                else absent++;
            } while (cursor.moveToNext());

            tvPresentCount.setText(String.valueOf(present));
            tvAbsentCount.setText(String.valueOf(absent));
            tvTotalCount.setText(String.valueOf(total));

            cursor.moveToFirst();
            tvNoData.setVisibility(View.GONE);
            rvAttendance.setVisibility(View.VISIBLE);
            rvAttendance.setAdapter(new AttendanceAdapter(cursor));
        } else {
            tvNoData.setVisibility(View.VISIBLE);
            rvAttendance.setVisibility(View.GONE);
            tvPresentCount.setText("0");
            tvAbsentCount.setText("0");
            tvTotalCount.setText("0");
        }
    }

    class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
        private Cursor cursor;
        AttendanceAdapter(Cursor cursor) { this.cursor = cursor; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attendance, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String room = cursor.getString(cursor.getColumnIndexOrThrow("room_number"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                holder.tvName.setText(name);
                holder.tvRoom.setText("Room: " + (room != null ? room : "N/A"));
                holder.tvStatus.setText(status != null ? status : "ABSENT");
                holder.tvStatus.setTextColor(getResources().getColor(
                        "PRESENT".equals(status) ? android.R.color.holo_green_dark : android.R.color.holo_red_dark, null));
            }
        }

        @Override
        public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRoom, tvStatus;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvRoom = itemView.findViewById(R.id.tvRoom);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}
