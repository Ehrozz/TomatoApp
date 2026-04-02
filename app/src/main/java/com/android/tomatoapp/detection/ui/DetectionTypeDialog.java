package com.android.tomatoapp.detection.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;

import com.android.tomatoapp.R;

public class DetectionTypeDialog extends Dialog {
    
    public enum DetectionType {
        FRUIT,
        LEAVES,
        PEST
    }
    
    private OnDetectionTypeSelectedListener listener;
    
    public interface OnDetectionTypeSelectedListener {
        void onDetectionTypeSelected(DetectionType type);
    }
    
    public DetectionTypeDialog(@NonNull Context context, OnDetectionTypeSelectedListener listener) {
        super(context, R.style.DetectionTypeDialogStyle);
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_detection_type);
        setCancelable(true);
        
        // Set window properties for rounded corners and transparency
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.5f);
        }
        
        MaterialCardView btnFruit = findViewById(R.id.btnFruit);
        MaterialCardView btnLeaves = findViewById(R.id.btnLeaves);
        MaterialCardView btnPest = findViewById(R.id.btnPest);
        TextView txtTitle = findViewById(R.id.txtTitle);
        
        if (txtTitle != null) {
            txtTitle.setText("Select Detection Type");
        }
        
        // Add click animations and listeners
        setupCardClickListener(btnFruit, DetectionType.FRUIT);
        setupCardClickListener(btnLeaves, DetectionType.LEAVES);
        setupCardClickListener(btnPest, DetectionType.PEST);
        
        // Animate cards on show
        animateCardsOnShow(btnFruit, btnLeaves, btnPest);
    }
    
    private void setupCardClickListener(MaterialCardView card, DetectionType type) {
        if (card != null) {
            card.setOnClickListener(v -> {
                // Add scale animation on click
                ScaleAnimation scaleAnimation = new ScaleAnimation(
                    1.0f, 0.95f, 1.0f, 0.95f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                );
                scaleAnimation.setDuration(100);
                scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (listener != null) {
                            listener.onDetectionTypeSelected(type);
                        }
                        dismiss();
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                card.startAnimation(scaleAnimation);
            });
        }
    }
    
    private void animateCardsOnShow(MaterialCardView... cards) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                cards[i].setAlpha(0f);
                cards[i].setScaleX(0.8f);
                cards[i].setScaleY(0.8f);
                
                cards[i].animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setStartDelay(i * 100)
                    .start();
            }
        }
    }
}

