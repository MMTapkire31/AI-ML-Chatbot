package com.example.hostelconnect;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessMenuActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextView tvCurrentDay, tvCurrentDate;
    private CardView cardBreakfast, cardLunch, cardDinner;
    private TextView tvBreakfastTitle, tvBreakfastItems, tvBreakfastSpecial;
    private TextView tvLunchTitle, tvLunchItems, tvLunchSpecial;
    private TextView tvDinnerTitle, tvDinnerItems, tvDinnerSpecial;

    private DatabaseHelper databaseHelper;
    private String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"};
    private String currentDay;
    private Map<String, MenuData> menuCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_menu);

        databaseHelper = new DatabaseHelper(this);
        menuCache = new HashMap<>();

        initializeViews();
        setupTabs();
        loadCurrentDayMenu();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayoutDays);
        tvCurrentDay = findViewById(R.id.tvCurrentDay);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);

        // Breakfast card
        cardBreakfast = findViewById(R.id.cardBreakfast);
        tvBreakfastTitle = findViewById(R.id.tvBreakfastTitle);
        tvBreakfastItems = findViewById(R.id.tvBreakfastItems);
        tvBreakfastSpecial = findViewById(R.id.tvBreakfastSpecial);

        // Lunch card
        cardLunch = findViewById(R.id.cardLunch);
        tvLunchTitle = findViewById(R.id.tvLunchTitle);
        tvLunchItems = findViewById(R.id.tvLunchItems);
        tvLunchSpecial = findViewById(R.id.tvLunchSpecial);

        // Dinner card
        cardDinner = findViewById(R.id.cardDinner);
        tvDinnerTitle = findViewById(R.id.tvDinnerTitle);
        tvDinnerItems = findViewById(R.id.tvDinnerItems);
        tvDinnerSpecial = findViewById(R.id.tvDinnerSpecial);
    }

    private void setupTabs() {
        for (String day : daysOfWeek) {
            tabLayout.addTab(tabLayout.newTab().setText(day.substring(0, 3)));
        }

        // Get current day
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0-6
        currentDay = daysOfWeek[dayOfWeek];

        // Select current day tab
        tabLayout.selectTab(tabLayout.getTabAt(dayOfWeek));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedDay = daysOfWeek[tab.getPosition()];
                loadMenuForDay(selectedDay);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadCurrentDayMenu() {
        // Update current date display
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        tvCurrentDate.setText(sdf.format(Calendar.getInstance().getTime()));

        loadMenuForDay(currentDay);
    }

    private void loadMenuForDay(String day) {
        tvCurrentDay.setText(day + "'s Menu");

        // Check cache first
        if (menuCache.containsKey(day)) {
            displayMenu(menuCache.get(day));
            return;
        }

        // Load from database
        MenuData menuData = new MenuData();
        Cursor cursor = databaseHelper.getMessMenuForDay(day);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String mealType = cursor.getString(cursor.getColumnIndex("meal_type"));
                String items = cursor.getString(cursor.getColumnIndex("items"));
                int specialIndex = cursor.getColumnIndex("special_note");
                String special = !cursor.isNull(specialIndex) ?
                        cursor.getString(specialIndex) : null;

                switch (mealType) {
                    case "Breakfast":
                        menuData.breakfastItems = items;
                        menuData.breakfastSpecial = special;
                        break;
                    case "Lunch":
                        menuData.lunchItems = items;
                        menuData.lunchSpecial = special;
                        break;
                    case "Dinner":
                        menuData.dinnerItems = items;
                        menuData.dinnerSpecial = special;
                        break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        menuCache.put(day, menuData);
        displayMenu(menuData);
    }

    private void displayMenu(MenuData menuData) {
        // Breakfast
        if (menuData.breakfastItems != null) {
            tvBreakfastItems.setText(menuData.breakfastItems);
            if (menuData.breakfastSpecial != null && !menuData.breakfastSpecial.isEmpty()) {
                tvBreakfastSpecial.setVisibility(View.VISIBLE);
                tvBreakfastSpecial.setText("⭐ " + menuData.breakfastSpecial);
            } else {
                tvBreakfastSpecial.setVisibility(View.GONE);
            }
        } else {
            tvBreakfastItems.setText("Menu not available");
            tvBreakfastSpecial.setVisibility(View.GONE);
        }

        // Lunch
        if (menuData.lunchItems != null) {
            tvLunchItems.setText(menuData.lunchItems);
            if (menuData.lunchSpecial != null && !menuData.lunchSpecial.isEmpty()) {
                tvLunchSpecial.setVisibility(View.VISIBLE);
                tvLunchSpecial.setText("⭐ " + menuData.lunchSpecial);
            } else {
                tvLunchSpecial.setVisibility(View.GONE);
            }
        } else {
            tvLunchItems.setText("Menu not available");
            tvLunchSpecial.setVisibility(View.GONE);
        }

        // Dinner
        if (menuData.dinnerItems != null) {
            tvDinnerItems.setText(menuData.dinnerItems);
            if (menuData.dinnerSpecial != null && !menuData.dinnerSpecial.isEmpty()) {
                tvDinnerSpecial.setVisibility(View.VISIBLE);
                tvDinnerSpecial.setText("⭐ " + menuData.dinnerSpecial);
            } else {
                tvDinnerSpecial.setVisibility(View.GONE);
            }
        } else {
            tvDinnerItems.setText("Menu not available");
            tvDinnerSpecial.setVisibility(View.GONE);
        }

        // Highlight current meal
        highlightCurrentMeal();
    }

    private void highlightCurrentMeal() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Reset all cards
        cardBreakfast.setCardElevation(4f);
        cardLunch.setCardElevation(4f);
        cardDinner.setCardElevation(4f);

        // Highlight current meal
        if (hour >= 6 && hour < 11) {
            // Breakfast time (6 AM - 11 AM)
            cardBreakfast.setCardElevation(12f);
            cardBreakfast.setCardBackgroundColor(
                    getResources().getColor(android.R.color.holo_orange_light));
        } else if (hour >= 11 && hour < 17) {
            // Lunch time (11 AM - 5 PM)
            cardLunch.setCardElevation(12f);
            cardLunch.setCardBackgroundColor(
                    getResources().getColor(android.R.color.holo_orange_light));
        } else {
            // Dinner time
            cardDinner.setCardElevation(12f);
            cardDinner.setCardBackgroundColor(
                    getResources().getColor(android.R.color.holo_orange_light));
        }
    }

    // Menu Data Model
    private static class MenuData {
        String breakfastItems;
        String breakfastSpecial;
        String lunchItems;
        String lunchSpecial;
        String dinnerItems;
        String dinnerSpecial;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}