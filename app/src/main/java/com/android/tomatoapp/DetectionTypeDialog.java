package com.android.tomatoapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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
        super(context);
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_detection_type);
        setCancelable(true);
        
        MaterialCardView btnFruit = findViewById(R.id.btnFruit);
        MaterialCardView btnLeaves = findViewById(R.id.btnLeaves);
        MaterialCardView btnPest = findViewById(R.id.btnPest);
        TextView txtTitle = findViewById(R.id.txtTitle);
        
        if (txtTitle != null) {
            txtTitle.setText("Select Detection Type");
        }
        
        if (btnFruit != null) {
            btnFruit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetectionTypeSelected(DetectionType.FRUIT);
                }
                dismiss();
            });
        }
        
        if (btnLeaves != null) {
            btnLeaves.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetectionTypeSelected(DetectionType.LEAVES);
                }
                dismiss();
            });
        }
        
        if (btnPest != null) {
            btnPest.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetectionTypeSelected(DetectionType.PEST);
                }
                dismiss();
            });
        }
    }
}

