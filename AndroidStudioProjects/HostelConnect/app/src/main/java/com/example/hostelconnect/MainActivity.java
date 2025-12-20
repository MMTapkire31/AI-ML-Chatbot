package com.example.hostelconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class MainActivity extends AppCompatActivity
        {

    private CardView hostelerCard, ownerCard, securityCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize card views
        hostelerCard = findViewById(R.id.hostelerCard);
        ownerCard = findViewById(R.id.ownerCard);
        securityCard = findViewById(R.id.securityCard);

        // Set click listeners with animations
        setupCardClickListener(hostelerCard, HostellerLoginActivity.class);
     setupCardClickListener(ownerCard, OwnerLogin.class);
       setupCardClickListener(securityCard, SecurityLogin.class);
    }

    private void setupCardClickListener(final CardView card, final Class<?> targetActivity) {
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add scale animation on click
                animateCard(card);

                // Navigate to the target activity after animation
                card.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, targetActivity);
                        startActivity(intent);
                        // Add transition animation
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }, 200);
            }
        });
    }

    private void animateCard(CardView card) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 0.95f,  // Start and end values for the X axis scaling
                1.0f, 0.95f,  // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f   // Pivot point of Y scaling
        );
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(false);

        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Scale back to normal
                ScaleAnimation scaleBack = new ScaleAnimation(
                        0.95f, 1.0f,
                        0.95f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                scaleBack.setDuration(200);
                scaleBack.setFillAfter(true);
                card.startAnimation(scaleBack);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        card.startAnimation(scaleAnimation);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}