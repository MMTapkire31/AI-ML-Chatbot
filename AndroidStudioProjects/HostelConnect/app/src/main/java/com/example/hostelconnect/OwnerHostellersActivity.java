package com.example.hostelconnect;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OwnerHostellersActivity extends AppCompatActivity {
    private RecyclerView rvHostellers;
    private TextView tvNoHostellers, btnBack, tvTotalCount;
    private EditText etSearch;
    private DatabaseHelper databaseHelper;
    private HostellerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_hostellers);
        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        loadHostellers();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvHostellers = findViewById(R.id.rvHostellers);
        tvNoHostellers = findViewById(R.id.tvNoHostellers);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        etSearch = findViewById(R.id.etSearch);
        rvHostellers.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHostellers(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadHostellers() {
        Cursor cursor = databaseHelper.getAllHostellers();
        if (cursor != null && cursor.getCount() > 0) {
            tvNoHostellers.setVisibility(View.GONE);
            rvHostellers.setVisibility(View.VISIBLE);
            tvTotalCount.setText("Total: " + cursor.getCount());
            adapter = new HostellerAdapter(cursor);
            rvHostellers.setAdapter(adapter);
        } else {
            tvNoHostellers.setVisibility(View.VISIBLE);
            rvHostellers.setVisibility(View.GONE);
            tvTotalCount.setText("Total: 0");
        }
    }

    private void filterHostellers(String query) {
        loadHostellers();
    }

    class HostellerAdapter extends RecyclerView.Adapter<HostellerAdapter.ViewHolder> {
        private Cursor cursor;
        HostellerAdapter(Cursor cursor) { this.cursor = cursor; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hosteller, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String room = cursor.getString(cursor.getColumnIndexOrThrow("room_number"));
                holder.tvName.setText(name);
                holder.tvPhone.setText(phone);
                holder.tvRoom.setText("Room: " + (room != null ? room : "N/A"));
            }
        }

        @Override
        public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPhone, tvRoom;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvRoom = itemView.findViewById(R.id.tvRoom);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}