package com.example.hostelconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ScrollView;

public class PaymentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFFF5F7FA);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView header = new TextView(this);
        header.setText("💰 Payment Details");
        header.setTextSize(24);
        header.setTextColor(0xFF1A1A2E);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(0, 0, 0, 40);
        layout.addView(header);

        // Pending payment card
        LinearLayout pendingCard = createPaymentCard("November 2024", "₹5,000", "Pending", 0xFFFEF3C7, 0xFFF59E0B);
        layout.addView(pendingCard);

        Button payBtn = new Button(this);
        payBtn.setText("Pay Now");
        payBtn.setTextColor(0xFFFFFFFF);
        payBtn.setBackgroundColor(0xFF667EEA);
        payBtn.setPadding(0, 40, 0, 40);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 0, 0, 40);
        payBtn.setLayoutParams(btnParams);
        payBtn.setOnClickListener(v -> Toast.makeText(this, "Payment gateway integration pending", Toast.LENGTH_SHORT).show());
        layout.addView(payBtn);

        TextView historyHeader = new TextView(this);
        historyHeader.setText("Payment History");
        historyHeader.setTextSize(18);
        historyHeader.setTextColor(0xFF1A1A2E);
        historyHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        historyHeader.setPadding(0, 20, 0, 30);
        layout.addView(historyHeader);

        // History
        layout.addView(createPaymentCard("October 2024", "₹5,000", "Paid", 0xFFDCFCE7, 0xFF10B981));
        layout.addView(createPaymentCard("September 2024", "₹5,000", "Paid", 0xFFDCFCE7, 0xFF10B981));

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    private LinearLayout createPaymentCard(String month, String amount, String status, int bgColor, int statusColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(bgColor);
        card.setPadding(40, 40, 40, 40);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        card.setLayoutParams(params);

        TextView monthTv = new TextView(this);
        monthTv.setText(month);
        monthTv.setTextSize(16);
        monthTv.setTextColor(0xFF1A1A2E);
        monthTv.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(monthTv);

        TextView amountTv = new TextView(this);
        amountTv.setText(amount);
        amountTv.setTextSize(24);
        amountTv.setTextColor(0xFF1A1A2E);
        amountTv.setTypeface(null, android.graphics.Typeface.BOLD);
        amountTv.setPadding(0, 10, 0, 10);
        card.addView(amountTv);

        TextView statusTv = new TextView(this);
        statusTv.setText("Status: " + status);
        statusTv.setTextSize(14);
        statusTv.setTextColor(statusColor);
        statusTv.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(statusTv);

        return card;
    }
}