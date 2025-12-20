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

public class OwnerPaymentsActivity extends AppCompatActivity {
    private RecyclerView rvPayments;
    private TextView tvNoPayments, btnBack;
    private TextView tvPendingAmount, tvPendingCount;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_payments);
        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        loadPayments();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvPayments = findViewById(R.id.rvPayments);
        tvNoPayments = findViewById(R.id.tvNoPayments);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadPayments() {
        Cursor cursor = databaseHelper.getAllPendingPayments();
        double totalPending = databaseHelper.getTotalPendingPaymentsAmount();

        tvPendingAmount.setText("₹" + String.format(Locale.getDefault(), "%.0f", totalPending));
        tvPendingCount.setText(String.valueOf(cursor != null ? cursor.getCount() : 0));

        if (cursor != null && cursor.getCount() > 0) {
            tvNoPayments.setVisibility(View.GONE);
            rvPayments.setVisibility(View.VISIBLE);
            rvPayments.setAdapter(new PaymentAdapter(cursor));
        } else {
            tvNoPayments.setVisibility(View.VISIBLE);
            rvPayments.setVisibility(View.GONE);
        }
    }

    class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
        private Cursor cursor;
        PaymentAdapter(Cursor cursor) { this.cursor = cursor; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_payment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String room = cursor.getString(cursor.getColumnIndexOrThrow("room_number"));
                String amount = cursor.getString(cursor.getColumnIndexOrThrow("amount"));
                String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));

                holder.tvName.setText(name);
                holder.tvRoom.setText("Room: " + (room != null ? room : "N/A"));
                holder.tvAmount.setText("₹" + amount);
                holder.tvMonth.setText(month);
            }
        }

        @Override
        public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRoom, tvAmount, tvMonth;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvRoom = itemView.findViewById(R.id.tvRoom);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvMonth = itemView.findViewById(R.id.tvMonth);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}
