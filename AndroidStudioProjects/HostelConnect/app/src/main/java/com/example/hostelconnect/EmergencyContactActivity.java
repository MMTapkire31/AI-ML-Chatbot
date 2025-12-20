package com.example.hostelconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_CODE = 100;
    private String pendingPhoneNumber = null; // Store phone number while waiting for permission

    private RecyclerView recyclerViewEmergency;
    private MaterialCardView cardQuickDial911, cardQuickDial100, cardQuickDial108;
    private DatabaseHelper databaseHelper;
    private EmergencyAdapter adapter;
    private List<EmergencyContact> emergencyContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        databaseHelper = new DatabaseHelper(this);

        // Setup Toolbar
        setupToolbar();

        initializeViews();
        setupQuickDialButtons();
        loadEmergencyContacts();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button click
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        recyclerViewEmergency = findViewById(R.id.recyclerViewEmergencyContacts);
        cardQuickDial911 = findViewById(R.id.cardQuickDial911);
        cardQuickDial100 = findViewById(R.id.cardQuickDial100);
        cardQuickDial108 = findViewById(R.id.cardQuickDial108);

        recyclerViewEmergency.setLayoutManager(new LinearLayoutManager(this));
        emergencyContacts = new ArrayList<>();
        adapter = new EmergencyAdapter(emergencyContacts);
        recyclerViewEmergency.setAdapter(adapter);
    }

    private void setupQuickDialButtons() {
        // Fire Emergency - 101
        cardQuickDial911.setOnClickListener(v -> makeEmergencyCall("101"));

        // Police - 100
        cardQuickDial100.setOnClickListener(v -> makeEmergencyCall("100"));

        // Ambulance - 108
        cardQuickDial108.setOnClickListener(v -> makeEmergencyCall("108"));
    }

    private void loadEmergencyContacts() {
        emergencyContacts.clear();
        Cursor cursor = null;

        try {
            cursor = databaseHelper.getAllEmergencyContacts();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String designation = cursor.getString(cursor.getColumnIndexOrThrow("designation"));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));

                    emergencyContacts.add(new EmergencyContact(id, name, designation, phone, type));
                } while (cursor.moveToNext());
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading contacts: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void makeEmergencyCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Store the phone number and request permission
            pendingPhoneNumber = phoneNumber;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_CODE);
        } else {
            // Permission already granted, make the call
            initiateCall(phoneNumber);
        }
    }

    private void initiateCall(String phoneNumber) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission denied. Cannot make calls.",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error making call: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, make the pending call
                if (pendingPhoneNumber != null) {
                    initiateCall(pendingPhoneNumber);
                    pendingPhoneNumber = null;
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Call permission denied. Cannot make emergency calls.",
                        Toast.LENGTH_LONG).show();
                pendingPhoneNumber = null;
            }
        }
    }

    // Emergency Contact Model
    private static class EmergencyContact {
        int id;
        String name;
        String designation;
        String phone;
        String type;

        EmergencyContact(int id, String name, String designation, String phone, String type) {
            this.id = id;
            this.name = name;
            this.designation = designation;
            this.phone = phone;
            this.type = type;
        }
    }

    // Emergency Contact Adapter
    private class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {
        private List<EmergencyContact> contacts;

        EmergencyAdapter(List<EmergencyContact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(
                    R.layout.item_emergency_contact, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            EmergencyContact contact = contacts.get(position);

            holder.tvName.setText(contact.name);
            holder.tvDesignation.setText(contact.designation);
            holder.tvPhone.setText(contact.phone);
            holder.tvType.setText(contact.type);

            // Set type color based on category
            int colorRes;
            switch (contact.type) {
                case "Medical":
                    colorRes = android.R.color.holo_red_light;
                    break;
                case "Fire":
                    colorRes = android.R.color.holo_orange_dark;
                    break;
                case "Police":
                    colorRes = android.R.color.holo_blue_dark;
                    break;
                case "Hostel":
                    colorRes = android.R.color.holo_purple;
                    break;
                default:
                    colorRes = android.R.color.holo_green_dark;
            }

            holder.tvType.setTextColor(getResources().getColor(colorRes, null));

            // Call button click - directly call
            holder.btnCall.setOnClickListener(v -> makeEmergencyCall(contact.phone));

            // Dial button click - opens dialer without calling
            holder.btnDial.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + contact.phone));
                startActivity(dialIntent);
            });
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDesignation, tvPhone, tvType;
            Button btnCall, btnDial;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvEmergencyName);
                tvDesignation = itemView.findViewById(R.id.tvEmergencyDesignation);
                tvPhone = itemView.findViewById(R.id.tvEmergencyPhone);
                tvType = itemView.findViewById(R.id.tvEmergencyType);
                btnCall = itemView.findViewById(R.id.btnEmergencyCall);
                btnDial = itemView.findViewById(R.id.btnEmergencyDial);
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