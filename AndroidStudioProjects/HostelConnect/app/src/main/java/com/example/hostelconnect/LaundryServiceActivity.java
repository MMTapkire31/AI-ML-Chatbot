package com.example.hostelconnect;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LaundryServiceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabNewRequest;
    private DatabaseHelper databaseHelper;
    private String phone;
    private LaundryAdapter adapter;
    private List<LaundryRequest> laundryList;

    // Laundry pricing
    private final Map<String, Double> laundryPrices = new HashMap<String, Double>() {{
        put("Shirt", 20.0);
        put("T-Shirt", 15.0);
        put("Trousers", 25.0);
        put("Jeans", 30.0);
        put("Bedsheet", 40.0);
        put("Towel", 10.0);
        put("Blanket", 50.0);
        put("Jacket", 35.0);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundry_service);

        phone = getIntent().getStringExtra("phone");
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        loadLaundryRequests();
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewLaundry);
        fabNewRequest = findViewById(R.id.fabNewLaundryRequest);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        laundryList = new ArrayList<>();
        adapter = new LaundryAdapter(laundryList);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabNewRequest.setOnClickListener(v -> showNewRequestDialog());
    }

    private void loadLaundryRequests() {
        laundryList.clear();
        Cursor cursor = databaseHelper.getLaundryHistory(phone);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String items = cursor.getString(cursor.getColumnIndexOrThrow("items"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                String pickupDate = cursor.getString(cursor.getColumnIndexOrThrow("pickup_date"));
                String deliveryDate = cursor.getString(cursor.getColumnIndexOrThrow("delivery_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));

                laundryList.add(new LaundryRequest(id, items, quantity, pickupDate,
                        deliveryDate, status, amount));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();

        if (laundryList.isEmpty()) {
            Toast.makeText(this, "No laundry requests found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNewRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_laundry_request, null);
        builder.setView(dialogView);

        LinearLayout itemsLayout = dialogView.findViewById(R.id.layoutLaundryItems);
        EditText etPickupDate = dialogView.findViewById(R.id.etPickupDate);
        TextView tvTotalAmount = dialogView.findViewById(R.id.tvTotalAmount);
        Button btnCalculate = dialogView.findViewById(R.id.btnCalculate);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitLaundry);

        // Create checkboxes and quantity inputs for each item
        Map<String, View> itemViews = new HashMap<>();
        for (Map.Entry<String, Double> entry : laundryPrices.entrySet()) {
            View itemView = getLayoutInflater().inflate(R.layout.item_laundry_checkbox, null);
            CheckBox checkbox = itemView.findViewById(R.id.checkboxItem);
            EditText etQuantity = itemView.findViewById(R.id.etItemQuantity);
            TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);

            checkbox.setText(entry.getKey());
            tvPrice.setText("₹" + entry.getValue());
            etQuantity.setEnabled(false);

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                etQuantity.setEnabled(isChecked);
                if (!isChecked) {
                    etQuantity.setText("");
                }
            });

            itemsLayout.addView(itemView);
            itemViews.put(entry.getKey(), itemView);
        }

        // Date picker
        etPickupDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Minimum next day

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        etPickupDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        // Calculate total
        btnCalculate.setOnClickListener(v -> {
            double total = 0;
            for (Map.Entry<String, View> entry : itemViews.entrySet()) {
                View itemView = entry.getValue();
                CheckBox checkbox = itemView.findViewById(R.id.checkboxItem);
                EditText etQuantity = itemView.findViewById(R.id.etItemQuantity);

                if (checkbox.isChecked()) {
                    String qtyStr = etQuantity.getText().toString();
                    if (!qtyStr.isEmpty()) {
                        int qty = Integer.parseInt(qtyStr);
                        double price = laundryPrices.get(entry.getKey());
                        total += qty * price;
                    }
                }
            }
            tvTotalAmount.setText(String.format(Locale.getDefault(), "Total: ₹%.2f", total));
        });

        // Submit request
        btnSubmit.setOnClickListener(v -> {
            String pickupDate = etPickupDate.getText().toString();
            if (pickupDate.isEmpty()) {
                Toast.makeText(this, "Please select pickup date", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder items = new StringBuilder();
            int totalQuantity = 0;
            double totalAmount = 0;

            for (Map.Entry<String, View> entry : itemViews.entrySet()) {
                View itemView = entry.getValue();
                CheckBox checkbox = itemView.findViewById(R.id.checkboxItem);
                EditText etQuantity = itemView.findViewById(R.id.etItemQuantity);

                if (checkbox.isChecked()) {
                    String qtyStr = etQuantity.getText().toString();
                    if (!qtyStr.isEmpty()) {
                        int qty = Integer.parseInt(qtyStr);
                        double price = laundryPrices.get(entry.getKey());

                        if (items.length() > 0) items.append(", ");
                        items.append(entry.getKey()).append(" x").append(qty);

                        totalQuantity += qty;
                        totalAmount += qty * price;
                    }
                }
            }

            if (totalQuantity == 0) {
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
                return;
            }

            long result = databaseHelper.submitLaundryRequest(
                    phone, items.toString(), totalQuantity, pickupDate, totalAmount
            );

            if (result != -1) {
                Toast.makeText(this, "Laundry request submitted successfully", Toast.LENGTH_SHORT).show();
                loadLaundryRequests();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Error submitting request", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // LaundryRequest Model Class
    private static class LaundryRequest {
        int id;
        String items;
        int quantity;
        String pickupDate;
        String deliveryDate;
        String status;
        double amount;

        LaundryRequest(int id, String items, int quantity, String pickupDate,
                       String deliveryDate, String status, double amount) {
            this.id = id;
            this.items = items;
            this.quantity = quantity;
            this.pickupDate = pickupDate;
            this.deliveryDate = deliveryDate;
            this.status = status;
            this.amount = amount;
        }
    }

    // LaundryAdapter Class
    private class LaundryAdapter extends RecyclerView.Adapter<LaundryAdapter.ViewHolder> {
        private List<LaundryRequest> requests;

        LaundryAdapter(List<LaundryRequest> requests) {
            this.requests = requests;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_laundry_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LaundryRequest request = requests.get(position);
            holder.tvItems.setText(request.items);
            holder.tvQuantity.setText("Quantity: " + request.quantity);
            holder.tvPickupDate.setText("Pickup: " + request.pickupDate);
            holder.tvStatus.setText(request.status);
            holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", request.amount));

            // Set status color
            int color;
            switch (request.status) {
                case "Pending":
                    color = android.R.color.holo_orange_dark;
                    break;
                case "In Progress":
                    color = android.R.color.holo_blue_dark;
                    break;
                case "Delivered":
                    color = android.R.color.holo_green_dark;
                    break;
                default:
                    color = android.R.color.darker_gray;
            }
            holder.tvStatus.setTextColor(getResources().getColor(color, null));

            if (request.deliveryDate != null && !request.deliveryDate.isEmpty()) {
                holder.tvDeliveryDate.setVisibility(View.VISIBLE);
                holder.tvDeliveryDate.setText("Delivered: " + request.deliveryDate);
            } else {
                holder.tvDeliveryDate.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItems, tvQuantity, tvPickupDate, tvDeliveryDate, tvStatus, tvAmount;

            ViewHolder(View itemView) {
                super(itemView);
                tvItems = itemView.findViewById(R.id.tvLaundryItems);
                tvQuantity = itemView.findViewById(R.id.tvLaundryQuantity);
                tvPickupDate = itemView.findViewById(R.id.tvPickupDate);
                tvDeliveryDate = itemView.findViewById(R.id.tvDeliveryDate);
                tvStatus = itemView.findViewById(R.id.tvLaundryStatus);
                tvAmount = itemView.findViewById(R.id.tvLaundryAmount);
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