package com.android.tomatoapp.common.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.android.tomatoapp.R;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import com.android.tomatoapp.common.managers.TutorialManager;

/**
 * Dialog-based tutorial that guides users through app features.
 * Shows step-by-step instructions with navigation controls.
 */
public class TutorialDialog extends Dialog {
    
    private Context context;
    private String userId;
    private int currentStep = 0;
    private List<TutorialStep> steps;
    
    private TextView txtTitle;
    private TextView txtContent;
    private TextView txtProgress;
    private MaterialButton btnPrevious;
    private MaterialButton btnSkip;
    private MaterialButton btnNext;
    
    /**
     * Tutorial step data structure.
     */
    private static class TutorialStep {
        String title;
        String content;
        
        TutorialStep(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
    
    public TutorialDialog(@NonNull Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
        initializeSteps();
    }
    
    private void initializeSteps() {
        steps = new ArrayList<>();
        
        // Step 1: Welcome
        steps.add(new TutorialStep(
            "Welcome to TomatoApp!",
            "TomatoApp helps you manage your tomato farming operations efficiently. " +
            "This tutorial will guide you through the main features. " +
            "You can skip at any time or go back to review previous steps."
        ));
        
        // Step 2: Home Dashboard
        steps.add(new TutorialStep(
            "Home Dashboard",
            "Your home screen shows:\n\n" +
            "• Weather Card: Current weather and forecast for your location\n" +
            "  - Tap to view detailed 7-day forecast\n" +
            "  - Long press to change location\n" +
            "• Work Programs: Access and manage your farming programs\n" +
            "• IPM (Integrated Pest Management): Disease detection and information\n" +
            "• Financial: Income and expense projections\n\n" +
            "Tap any card to open that section."
        ));
        
        // Step 3: Drawer Navigation
        steps.add(new TutorialStep(
            "Navigation Menu",
            "Access all app features from the drawer menu:\n\n" +
            "• Tap the ☰ icon (top left) to open the menu\n" +
            "• View your profile picture and name at the top\n" +
            "• Tap your profile to edit your information\n" +
            "• Navigate to: Home, Profile, History, Financial, Analytics, Settings, and more\n" +
            "• The menu is available from all screens\n" +
            "• Swipe from the left edge or tap the menu icon to open"
        ));
        
        // Step 4: Work Programs
        steps.add(new TutorialStep(
            "Creating Work Programs",
            "Work programs help you plan and track your farming activities:\n\n" +
            "1. Go to Work Programs from the home screen or drawer\n" +
            "2. Tap the + button to create a new program\n" +
            "3. Select your cultivar (tomato variety)\n" +
            "4. Enter your land area in hectares\n" +
            "5. Choose your program start date\n" +
            "6. Submit to create the program\n\n" +
            "The app will generate daily tasks based on your cultivar and start date."
        ));
        
        // Step 5: Daily Tasks
        steps.add(new TutorialStep(
            "Managing Daily Tasks",
            "Track your farming activities with daily tasks:\n\n" +
            "• View tasks on the calendar for each day\n" +
            "• Mark tasks as complete when finished\n" +
            "• Skip tasks if not applicable\n" +
            "• Tasks are organized into 5 growth phases:\n" +
            "  - Phase 1: Nursery &amp; Land Prep\n" +
            "  - Phase 2: Transplant &amp; Establishment\n" +
            "  - Phase 3: Vegetative Growth\n" +
            "  - Phase 4: Flowering &amp; Fruit Set\n" +
            "  - Phase 5: Harvest\n\n" +
            "• Access Daily Expenses from tasks screen\n" +
            "• Monitor plants and view captured images\n" +
            "• See cultivar images throughout the app"
        ));
        
        // Step 6: Disease Detection
        steps.add(new TutorialStep(
            "Disease &amp; Pest Detection",
            "Use AI-powered detection to identify plant issues:\n\n" +
            "1. Go to IPM section from home or drawer\n" +
            "2. Tap 'Scan for Diseases'\n" +
            "3. Allow camera permissions\n" +
            "4. Point camera at tomato leaves or fruits\n" +
            "5. Tap capture to scan\n" +
            "6. View detection results with accuracy percentage\n" +
            "7. Read symptoms, causes, and treatment recommendations\n\n" +
            "You can also browse disease information from the Information section."
        ));
        
        // Step 7: Daily Expenses
        steps.add(new TutorialStep(
            "Daily Expenses Tracking",
            "Track your daily farming expenses efficiently:\n\n" +
            "• Access from Daily Tasks screen or Calculator\n" +
            "• Record expenses by category:\n" +
            "  - Labor: Track workers and daily wages\n" +
            "  - Equipment/Tools: Record rental costs and usage (minutes/hours)\n" +
            "  - Material: Input fertilizers, pesticides, and other materials\n" +
            "  - Miscellaneous: Track fuel, electricity, and other expenses\n\n" +
            "• View expense history from Calculator screen\n" +
            "• Expenses are automatically aggregated and displayed as ranges\n" +
            "• Use expense history to review past spending patterns"
        ));
        
        // Step 8: Financial Calculator
        steps.add(new TutorialStep(
            "Financial Projections",
            "Calculate projected income and expenses:\n\n" +
            "• Enter average fruit weight and fruits per plant\n" +
            "• Set market value per kilogram\n" +
            "• View Harvest Prediction with yield ranges:\n" +
            "  - Yield per hectare (kg/hectare range)\n" +
            "  - Total yield range\n" +
            "  - Predicted harvest date\n\n" +
            "• Expenses automatically loaded from daily expenses:\n" +
            "  - Labor, Equipment, Material, Miscellaneous costs\n" +
            "  - Displayed as cost ranges (min-max-average)\n\n" +
            "• See projected net income and adjusted projections\n" +
            "• Higher task completion rates result in more accurate projections"
        ));
        
        // Step 9: Plant Monitoring
        steps.add(new TutorialStep(
            "Plant Monitoring",
            "Monitor your plants and capture their current state:\n\n" +
            "• Access from Daily Tasks screen\n" +
            "• View reference images for the current growth phase\n" +
            "• Capture images of your plants:\n" +
            "  - Use 'Capture' button for simple photo capture\n" +
            "  - Use 'Scan for Diseases' for AI-powered disease detection\n" +
            "  - Delete captured images before saving if needed\n\n" +
            "• Add notes about plant condition\n" +
            "• Saved monitoring entries appear in Daily Tasks\n" +
            "• Track plant progress throughout the growing season"
        ));
        
        // Step 10: Analytics & Reports
        steps.add(new TutorialStep(
            "Analytics &amp; Reports",
            "Analyze your farming performance:\n\n" +
            "• View cultivar performance metrics\n" +
            "• Compare profit per hectare across cultivars\n" +
            "• See average completion rates\n" +
            "• Compare on-season vs off-season performance\n" +
            "• Export reports to PDF or CSV\n" +
            "• Select specific work programs to include in reports\n" +
            "• Analytics uses expense data from daily expenses tracking\n\n" +
            "Use analytics to make informed decisions for future plantings."
        ));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_tutorial_step);
        setCancelable(false);
        
        // Make dialog larger
        Window window = getWindow();
        if (window != null) {
            android.view.WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90); // 90% of screen width
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
        
        txtTitle = findViewById(R.id.txtTutorialTitle);
        txtContent = findViewById(R.id.txtTutorialContent);
        txtProgress = findViewById(R.id.txtTutorialProgress);
        btnPrevious = findViewById(R.id.btnTutorialPrevious);
        btnSkip = findViewById(R.id.btnTutorialSkip);
        btnNext = findViewById(R.id.btnTutorialNext);
        
        if (txtTitle == null || txtContent == null || txtProgress == null ||
            btnPrevious == null || btnSkip == null || btnNext == null) {
            dismiss();
            return;
        }
        
        btnPrevious.setOnClickListener(v -> goToPreviousStep());
        btnSkip.setOnClickListener(v -> skipTutorial());
        btnNext.setOnClickListener(v -> goToNextStep());
        
        updateStep();
    }
    
    private void updateStep() {
        if (currentStep < 0 || currentStep >= steps.size()) {
            return;
        }
        
        TutorialStep step = steps.get(currentStep);
        txtTitle.setText(step.title);
        txtContent.setText(step.content);
        
        // Update progress
        txtProgress.setText(String.format("Step %d of %d", currentStep + 1, steps.size()));
        
        // Update button visibility
        btnPrevious.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);
        
        // Update Next button text
        if (currentStep == steps.size() - 1) {
            btnNext.setText("Finish");
        } else {
            btnNext.setText("Next");
        }
    }
    
    private void goToPreviousStep() {
        if (currentStep > 0) {
            currentStep--;
            updateStep();
        }
    }
    
    private void goToNextStep() {
        if (currentStep < steps.size() - 1) {
            currentStep++;
            updateStep();
        } else {
            // Last step - finish tutorial
            finishTutorial();
        }
    }
    
    private void skipTutorial() {
        finishTutorial();
    }
    
    private void finishTutorial() {
        if (userId != null && !userId.isEmpty()) {
            TutorialManager.markTutorialCompleted(context, userId);
        }
        dismiss();
    }
}

