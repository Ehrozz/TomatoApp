package com.android.tomatoapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkProgramSelectionDialog extends Dialog {
    
    private OnWorkProgramSelectedListener listener;
    private List<WorkProgramEntity> programs;
    
    public interface OnWorkProgramSelectedListener {
        void onWorkProgramSelected(WorkProgramEntity program);
        void onContinueWithoutProgram();
    }
    
    public WorkProgramSelectionDialog(@NonNull Context context, 
                                     List<WorkProgramEntity> programs,
                                     OnWorkProgramSelectedListener listener) {
        super(context, R.style.DetectionTypeDialogStyle);
        this.programs = programs;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_work_program_selection);
        setCancelable(true);
        
        // Set window properties for rounded corners and transparency
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.5f);
        }
        
        LinearLayout programsContainer = findViewById(R.id.programsContainer);
        MaterialCardView btnContinueWithout = findViewById(R.id.btnContinueWithout);
        
        // Populate work programs
        if (programsContainer != null && programs != null) {
            for (int i = 0; i < programs.size(); i++) {
                WorkProgramEntity program = programs.get(i);
                View programView = createProgramView(program, i);
                programsContainer.addView(programView);
            }
        }
        
        // Continue without linking button
        if (btnContinueWithout != null) {
            btnContinueWithout.setOnClickListener(v -> {
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
                            listener.onContinueWithoutProgram();
                        }
                        dismiss();
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                btnContinueWithout.startAnimation(scaleAnimation);
            });
        }
        
        // Animate cards on show
        animateCardsOnShow();
    }
    
    private View createProgramView(WorkProgramEntity program, int index) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout container = findViewById(R.id.programsContainer);
        View view = inflater.inflate(R.layout.item_work_program_option, container, false);
        
        MaterialCardView card = view.findViewById(R.id.cardProgram);
        ImageView imgCultivar = view.findViewById(R.id.imgCultivar);
        TextView txtCultivarName = view.findViewById(R.id.txtCultivarName);
        TextView txtStartDate = view.findViewById(R.id.txtStartDate);
        
        // Set cultivar name
        if (txtCultivarName != null) {
            String cultivarName = program.cultivarName != null ? program.cultivarName : "Unknown";
            txtCultivarName.setText(cultivarName);
        }
        
        // Set start date
        if (txtStartDate != null) {
            String startDate = program.startingDate != null ? program.startingDate : "N/A";
            // Format date if possible
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(startDate);
                if (date != null) {
                    startDate = outputFormat.format(date);
                }
            } catch (Exception e) {
                // Keep original format if parsing fails
            }
            txtStartDate.setText("Start Date: " + startDate);
        }
        
        // Set cultivar image with circular clipping
        if (imgCultivar != null && program.cultivarName != null) {
            int imageRes = CultivarImageHelper.getCultivarImageResource(program.cultivarName);
            if (imageRes != 0) {
                imgCultivar.setImageResource(imageRes);
            } else {
                // Default image if not found
                imgCultivar.setImageResource(R.drawable.ic_tomato_logo_green);
            }
            
            // Make image circular - set outline provider
            imgCultivar.setClipToOutline(true);
            imgCultivar.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(android.view.View view, android.graphics.Outline outline) {
                    int size = Math.min(view.getWidth(), view.getHeight());
                    int left = (view.getWidth() - size) / 2;
                    int top = (view.getHeight() - size) / 2;
                    outline.setOval(left, top, left + size, top + size);
                }
            });
            
            // Ensure it's applied after layout
            imgCultivar.post(() -> {
                imgCultivar.invalidateOutline();
            });
        }
        
        // Set click listener
        if (card != null) {
            card.setOnClickListener(v -> {
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
                            listener.onWorkProgramSelected(program);
                        }
                        dismiss();
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                card.startAnimation(scaleAnimation);
            });
        }
        
        // Set initial state for animation
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setTag(index);
        
        return view;
    }
    
    private void animateCardsOnShow() {
        LinearLayout container = findViewById(R.id.programsContainer);
        if (container != null) {
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                if (child != null) {
                    int delay = i * 100;
                    child.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setStartDelay(delay)
                        .start();
                }
            }
        }
        
        MaterialCardView btnContinueWithout = findViewById(R.id.btnContinueWithout);
        if (btnContinueWithout != null) {
            int delay = (container != null ? container.getChildCount() : 0) * 100;
            btnContinueWithout.setAlpha(0f);
            btnContinueWithout.setScaleX(0.8f);
            btnContinueWithout.setScaleY(0.8f);
            btnContinueWithout.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay(delay)
                .start();
        }
    }
}

