package com.android.tomatoapp.workprogram.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.tomatoapp.R;
import com.android.tomatoapp.common.models.CultivarNPData;
import com.android.tomatoapp.workprogram.data.IntakeAnswers;
import com.android.tomatoapp.common.utils.PhaseHelper;
import com.android.tomatoapp.common.utils.AppClock;
import com.google.android.material.button.MaterialButton;

public class WorkProgramIntakeDialog {

    public interface Callback {
        void onCompleted(IntakeAnswers answers, IntakeAnswers.Evaluation evaluation);
        void onCancelled();
    }

    private enum Step { LAND, SEEDLING, TRANSPLANT, FLOWERS, PHASE4, HARVEST }

    private final Context context;
    private final String cultivar;
    private final Callback callback;
    private final IntakeAnswers answers = new IntakeAnswers();
    private final boolean trellisNeeded;

    private AlertDialog dialog;
    private TextView stepLabel;
    private TextView questionText;
    private TextView errorText;
    private RadioGroup mainChoices;
    private LinearLayout subQuestions;
    private com.google.android.material.card.MaterialCardView subQuestionsCard;
    private LinearLayout subQuestionsHeader;
    private TextView subQuestionsTitle;
    private android.widget.ImageView subQuestionsChevron;
    private MaterialButton btnBack;
    private MaterialButton btnNext;

    private Step currentStep = Step.LAND;

    public WorkProgramIntakeDialog(@NonNull Context context, String cultivar, @NonNull Callback callback) {
        this.context = context;
        this.cultivar = cultivar;
        this.callback = callback;
        String habit = CultivarNPData.getGrowthHabit(cultivar);
        this.trellisNeeded = habit != null && (habit.contains("Semi") || habit.contains("Indeterminate"));
        if (!trellisNeeded) {
            answers.trellisOrStakes = IntakeAnswers.NOT_NEEDED;
            answers.replacedSupports = IntakeAnswers.NOT_NEEDED;
        }
    }

    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_work_program_intake, null);
        stepLabel = view.findViewById(R.id.intakeStepLabel);
        questionText = view.findViewById(R.id.intakeQuestionText);
        errorText = view.findViewById(R.id.intakeErrorText);
        mainChoices = view.findViewById(R.id.intakeMainChoices);
        subQuestions = view.findViewById(R.id.intakeSubQuestions);
        subQuestionsCard = view.findViewById(R.id.subQuestionsCard);
        subQuestionsHeader = view.findViewById(R.id.subQuestionsHeader);
        subQuestionsTitle = view.findViewById(R.id.subQuestionsTitle);
        subQuestionsChevron = view.findViewById(R.id.subQuestionsChevron);
        btnBack = view.findViewById(R.id.btnIntakeBack);
        btnNext = view.findViewById(R.id.btnIntakeNext);

        btnBack.setOnClickListener(v -> goBack());
        btnNext.setOnClickListener(v -> goNext());

        // Setup collapsible sub-questions
        subQuestionsHeader.setOnClickListener(v -> toggleSubQuestions());
        subQuestionsCard.setVisibility(View.GONE);

        dialog = new AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setView(view)
                .setCancelable(true)
                .setOnCancelListener(d -> callback.onCancelled())
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        renderStep();
        dialog.show();
    }

    private void renderStep() {
        errorText.setVisibility(View.GONE);
        mainChoices.removeAllViews();
        subQuestions.removeAllViews();
        btnBack.setEnabled(currentStep != Step.LAND);
        
        // Hide sub-questions card initially
        subQuestionsCard.setVisibility(View.GONE);

        switch (currentStep) {
            case LAND:
                stepLabel.setText("Step 1 of 5 · Land");
                questionText.setText("Is your land ready for planting?");
                bindMain(new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET}, answers.landReady);
                addSub("How many times have you plowed the land?",
                        new String[]{"1", "2", "3", IntakeAnswers.NOT_DONE, IntakeAnswers.ONGOING},
                        answers.plowCount, value -> answers.plowCount = value);
                addSub("Did you harrow on the last plowing?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.harrowLastPlowing, value -> answers.harrowLastPlowing = value);
                addSub("Have you made furrows?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.madeFurrows, value -> answers.madeFurrows = value);
                addSub("Have you made the beds (1 m wide, 20 cm high)?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.madeBeds, value -> answers.madeBeds = value);
                btnNext.setText("Next");
                break;
            case SEEDLING:
                stepLabel.setText("Step 2 of 5 · Seedlings");
                questionText.setText("Is your seedling ready?");
                bindMain(new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET}, answers.seedlingReady);
                addSub("Is your growing medium ready?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.growingMediumReady, value -> answers.growingMediumReady = value);
                addSub("Did you sterilize or solarize the medium?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.sterilizedMedium, value -> answers.sterilizedMedium = value);
                addSub("Is your seed ready (180-200 g per hectare)?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.seedReady, value -> {
                            answers.seedReady = value;
                            renderStep();
                        });
                addSub("Have you sown the seeds?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.sownSeeds, value -> {
                            answers.sownSeeds = value;
                            renderStep();
                        });
                addSub("Have you started seedling hardening?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.seedlingHardening, value -> answers.seedlingHardening = value);
                if (IntakeAnswers.YES.equals(answers.seedReady)) {
                    addDatePicker("When did you confirm you have seeds?", answers.seedsConfirmedDate,
                            value -> answers.seedsConfirmedDate = value, null);
                }
                if (IntakeAnswers.YES.equals(answers.sownSeeds)
                        || IntakeAnswers.ONGOING.equals(answers.sownSeeds)) {
                    addDatePicker("When did you sow the seeds?", answers.sowingDate,
                            value -> answers.sowingDate = value, () -> answers.seedsConfirmedDate);
                }
                btnNext.setText("Next");
                break;
            case TRANSPLANT:
                stepLabel.setText("Step 3 of 5 · Transplant");
                questionText.setText("Have you transplanted?");
                bindMain(new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING}, answers.transplanted);
                addSub("Have you transplanted all your seedlings?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.transplantedAll, value -> answers.transplantedAll = value);
                addSub("Have you laid the mulch?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.laidMulch, value -> answers.laidMulch = value);
                if (trellisNeeded) {
                    addSub("Have you installed trellis or stakes?",
                            new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                            answers.trellisOrStakes, value -> answers.trellisOrStakes = value);
                } else {
                    addInfo("Trellis / stakes: not needed for this determinate variety.");
                }
                addSub("Did you apply basal fertilizer?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.NOT_YET},
                        answers.basalFertilizer, value -> answers.basalFertilizer = value);
                addSub("Have you applied the first side-dressing (10-15 days after transplanting)?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.firstSideDressing, value -> answers.firstSideDressing = value);
                if (IntakeAnswers.YES.equals(answers.transplanted)
                        || IntakeAnswers.ONGOING.equals(answers.transplanted)) {
                    addDatePicker("When did you transplant?", answers.transplantDate,
                            value -> answers.transplantDate = value, () -> answers.sowingDate);
                }
                btnNext.setText("Next");
                break;
            case FLOWERS:
                stepLabel.setText("Step 4 of 5 · Flowers");
                questionText.setText("Do your plants show flowers?");
                bindMain(new String[]{IntakeAnswers.NO_FLOWERS, IntakeAnswers.SOME,
                        IntakeAnswers.ALL, "Finished flowering"}, answers.flowers);
                renderFlowerSubs();
                btnNext.setText("Next");
                break;
            case PHASE4:
                stepLabel.setText("Step 4 follow-up · Fruit set");
                questionText.setText("Flowering is complete. Capture remaining Phase 4 work.");
                bindMain(new String[]{IntakeAnswers.ALL}, answers.flowers.isEmpty() ? IntakeAnswers.ALL : answers.flowers);
                addSub("Have you applied the fourth side-dressing at peak flowering (2 weeks after the second)?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                        answers.fourthSideDressing, value -> answers.fourthSideDressing = value);
                if (trellisNeeded) {
                    addSub("Have you replaced weak supports and retied the plants?",
                            new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                            answers.replacedSupports, value -> answers.replacedSupports = value);
                } else {
                    addInfo("Support replacement: not needed for this determinate variety.");
                }
                btnNext.setText("Start program");
                break;
            case HARVEST:
                stepLabel.setText("Step 5 of 5 · Fruit & harvest");
                questionText.setText("Do your plants have fruits that are starting to mature?");
                bindMain(new String[]{IntakeAnswers.NOT_YET, IntakeAnswers.YES}, answers.fruitsMaturing);
                addSub("Have you started harvesting?",
                        new String[]{IntakeAnswers.YES, IntakeAnswers.NOT_YET},
                        answers.startedHarvesting, value -> answers.startedHarvesting = value);
                btnNext.setText("Start program");
                break;
        }
    }

    private void bindMain(String[] options, String selected) {
        mainChoices.setOnCheckedChangeListener(null);
        for (int i = 0; i < options.length; i++) {
            RadioButton button = new RadioButton(context);
            button.setId(View.generateViewId());
            button.setText(options[i]);
            button.setTextColor(android.graphics.Color.BLACK);
            button.setPadding(8, 12, 8, 12);
            if (options[i].equals(selected)) button.setChecked(true);
            mainChoices.addView(button);
        }
        mainChoices.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedButton = group.findViewById(checkedId);
            if (selectedButton == null) return;
            String value = selectedButton.getText().toString();
            switch (currentStep) {
                case LAND:
                    answers.landReady = value;
                    break;
                case SEEDLING:
                    answers.seedlingReady = value;
                    break;
                case TRANSPLANT:
                    answers.transplanted = value;
                    break;
                case FLOWERS:
                    answers.flowers = value;
                    renderFlowerSubs();
                    break;
                case PHASE4:
                    answers.flowers = IntakeAnswers.ALL;
                    break;
                case HARVEST:
                    answers.fruitsMaturing = value;
                    break;
            }
                if (currentStep == Step.SEEDLING || currentStep == Step.TRANSPLANT) {
                    renderStep();
                }
        });
    }

    private void renderFlowerSubs() {
        subQuestions.removeAllViews();
        if (!IntakeAnswers.SOME.equals(answers.flowers)) {
            if (IntakeAnswers.ALL.equals(answers.flowers)) {
                addInfo("Next: Phase 4 remaining work (side-dressing and supports).");
            } else if (IntakeAnswers.FINISH.equals(answers.flowers)) {
                addInfo("Next: fruit maturity and harvest questions.");
            }
            return;
        }
        addSub("Have you applied the second side-dressing (3-4 weeks after transplanting)?",
                new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                answers.secondSideDressing, value -> answers.secondSideDressing = value);
        addSub("Have you applied the third side-dressing (2 weeks after the last application)?",
                new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                answers.thirdSideDressing, value -> answers.thirdSideDressing = value);
        addSub("Have you removed the weeds?",
                new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                answers.removedWeeds, value -> answers.removedWeeds = value);
        addSub("Have you hilled-up the plants?",
                new String[]{IntakeAnswers.YES, IntakeAnswers.ONGOING, IntakeAnswers.NOT_YET},
                answers.hilledUp, value -> answers.hilledUp = value);
    }

    private interface ChoiceListener {
        void onChosen(String value);
    }

    private interface DateValue {
        String get();
    }

    private void addDatePicker(String label, String selected, ChoiceListener listener, DateValue minimum) {
        addMilestoneNotice();
        TextView field = new TextView(context);
        field.setText(label + (selected == null || selected.isEmpty() ? "" : "\n" + selected));
        field.setTextColor(android.graphics.Color.BLACK);
        field.setTextSize(13);
        field.setPadding(12, 14, 12, 14);
        field.setBackgroundColor(0xFFFFF3CD);
        field.setTypeface(null, android.graphics.Typeface.BOLD);
        field.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            DatePickerDialog picker = new DatePickerDialog(context, (view, year, month, day) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, day, 0, 0, 0);
                String value = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(chosen.getTime());
                listener.onChosen(value);
                field.setText(label + "\n" + value);
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            Calendar lower = Calendar.getInstance();
            lower.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
            Calendar upper = Calendar.getInstance();
            upper.set(2100, Calendar.DECEMBER, 31, 23, 59, 59);
            picker.getDatePicker().setMinDate(lower.getTimeInMillis());
            picker.getDatePicker().setMaxDate(upper.getTimeInMillis());
            picker.show();
        });
        subQuestions.addView(field);
    }

    private void addMilestoneNotice() {
        if (subQuestions.findViewWithTag("milestone_notice") != null) return;
        TextView notice = new TextView(context);
        notice.setTag("milestone_notice");
        notice.setText("REQUIRED MILESTONE DATE\nTap the highlighted question below to record it.");
        notice.setTextColor(android.graphics.Color.BLACK);
        notice.setTextSize(12);
        notice.setTypeface(null, android.graphics.Typeface.BOLD);
        notice.setPadding(12, 12, 12, 12);
        notice.setBackgroundColor(0xFFE8F5E9);
        subQuestions.addView(notice);
    }

    private void addSub(String title, String[] options, String selected, ChoiceListener listener) {
        TextView label = new TextView(context);
        label.setText(title);
        label.setTextColor(android.graphics.Color.BLACK);
        label.setTextSize(13);
        label.setPadding(0, 16, 0, 4);
        subQuestions.addView(label);

        RadioGroup group = new RadioGroup(context);
        group.setOrientation(RadioGroup.VERTICAL);
        for (String option : options) {
            RadioButton button = new RadioButton(context);
            button.setId(View.generateViewId());
            button.setText(option);
            button.setTextColor(android.graphics.Color.BLACK);
            button.setTextSize(12);
            if (option.equals(selected)) button.setChecked(true);
            group.addView(button);
        }
        group.setOnCheckedChangeListener((g, checkedId) -> {
            RadioButton selectedButton = g.findViewById(checkedId);
            if (selectedButton != null) listener.onChosen(selectedButton.getText().toString());
        });
        subQuestions.addView(group);
        
        // Show sub-questions card when sub-questions are added
        subQuestionsCard.setVisibility(View.VISIBLE);
        subQuestions.setVisibility(View.VISIBLE);
        subQuestionsChevron.setImageResource(android.R.drawable.arrow_down_float);
    }

    private void addInfo(String text) {
        TextView info = new TextView(context);
        info.setText(text);
        info.setTextColor(0xFF6B7280);
        info.setTextSize(12);
        info.setPadding(0, 12, 0, 4);
        subQuestions.addView(info);
        
        // Show sub-questions card when info is added
        subQuestionsCard.setVisibility(View.VISIBLE);
        subQuestions.setVisibility(View.VISIBLE);
        subQuestionsChevron.setImageResource(android.R.drawable.arrow_down_float);
    }

    private void goBack() {
        switch (currentStep) {
            case SEEDLING:
                currentStep = Step.LAND;
                break;
            case TRANSPLANT:
                currentStep = Step.SEEDLING;
                break;
            case FLOWERS:
                currentStep = Step.TRANSPLANT;
                break;
            case PHASE4:
            case HARVEST:
                currentStep = Step.FLOWERS;
                break;
            default:
                return;
        }
        renderStep();
    }

    private boolean captureMainSelection() {
        int checkedId = mainChoices.getCheckedRadioButtonId();
        if (checkedId == -1) {
            showError("Please choose an answer to continue.");
            return false;
        }
        RadioButton selectedButton = mainChoices.findViewById(checkedId);
        if (selectedButton == null) {
            showError("Please choose an answer to continue.");
            return false;
        }
        String value = selectedButton.getText().toString();
        switch (currentStep) {
            case LAND:
                answers.landReady = value;
                break;
            case SEEDLING:
                answers.seedlingReady = value;
                break;
            case TRANSPLANT:
                answers.transplanted = value;
                break;
            case FLOWERS:
                answers.flowers = value;
                break;
            case PHASE4:
                answers.flowers = IntakeAnswers.ALL;
                break;
            case HARVEST:
                answers.fruitsMaturing = value;
                break;
        }
        return true;
    }

    private void goNext() {
        if (!captureMainSelection()) return;

        if (currentStep == Step.LAND) {
            currentStep = Step.SEEDLING;
            renderStep();
            return;
        }

        if (currentStep == Step.SEEDLING) {
            IntakeAnswers.Evaluation preview = answers.evaluate();
            if (!preview.canCreate) {
                showError(preview.blockReason);
                return;
            }
            boolean bothYes = IntakeAnswers.YES.equals(answers.landReady) && IntakeAnswers.YES.equals(answers.seedlingReady);
            boolean anyOngoing = IntakeAnswers.ONGOING.equals(answers.landReady) || IntakeAnswers.ONGOING.equals(answers.seedlingReady);
            boolean anyNotYet = IntakeAnswers.NOT_YET.equals(answers.landReady) || IntakeAnswers.NOT_YET.equals(answers.seedlingReady);
            if (anyNotYet && !bothYes) {
                finishIntake();
                return;
            }
            if (bothYes || anyOngoing) {
                currentStep = Step.TRANSPLANT;
                renderStep();
                return;
            }
            finishIntake();
            return;
        }

        if (currentStep == Step.TRANSPLANT) {
            if (IntakeAnswers.ONGOING.equals(answers.transplanted)) {
                finishIntake();
                return;
            }
            currentStep = Step.FLOWERS;
            renderStep();
            return;
        }

        if (currentStep == Step.FLOWERS) {
            if (IntakeAnswers.SOME.equals(answers.flowers)) {
                finishIntake();
                return;
            }
            if (IntakeAnswers.ALL.equals(answers.flowers)) {
                currentStep = Step.PHASE4;
                renderStep();
                return;
            }
            currentStep = Step.HARVEST;
            renderStep();
            return;
        }

        finishIntake();
    }

    private void finishIntake() {
        if ("Finished flowering".equals(answers.flowers)) answers.flowers = IntakeAnswers.FINISH;
        IntakeAnswers.Evaluation evaluation = answers.evaluate();
        if (!evaluation.canCreate) {
            showError(evaluation.blockReason);
            return;
        }
        String dateError = answers.validateDates(evaluation.startPhase, AppClock.today(context));
        if (dateError != null) {
            showError(dateError);
            return;
        }
        if (!answers.sowingDate.isEmpty() && !answers.transplantDate.isEmpty()) {
            try {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.parse(answers.sowingDate),
                        java.time.LocalDate.parse(answers.transplantDate));
                if (days < 25) {
                    Toast.makeText(context,
                            "Warning: transplanting is less than 25 days after sowing.",
                            Toast.LENGTH_LONG).show();
                }
            } catch (java.time.format.DateTimeParseException ignored) {
                // validateDates reports malformed values.
            }
        }

        // Show confirmation summary dialog to the farmer before proceeding
        StringBuilder summaryMsg = new StringBuilder();
        summaryMsg.append("Based on your answers, your crop is currently in:\n\n");
        summaryMsg.append("🌱 ").append(PhaseHelper.getPhaseNameWithDescription(evaluation.startPhase)).append("\n\n");
        if (evaluation.startPhase > 1) {
            summaryMsg.append("✓ Tasks for earlier phases (Phases 1 to ")
                      .append(evaluation.startPhase - 1)
                      .append(") will be marked completed so you don't repeat them.\n\n");
            summaryMsg.append("💰 You will be offered to log any expenses you already incurred for completed work.\n\n");
        } else {
            summaryMsg.append("Tasks will begin from Phase 1 (Land and Seedling Preparation).\n\n");
        }
        summaryMsg.append("Would you like to start your work program with this setup?");

        new AlertDialog.Builder(context)
                .setTitle("Crop Stage Summary")
                .setMessage(summaryMsg.toString())
                .setPositiveButton("Confirm & Start", (d, which) -> {
                    dialog.dismiss();
                    callback.onCompleted(answers, evaluation);
                })
                .setNegativeButton("Review Answers", null)
                .show();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void toggleSubQuestions() {
        if (subQuestions.getVisibility() == View.VISIBLE) {
            subQuestions.setVisibility(View.GONE);
            subQuestionsChevron.setImageResource(android.R.drawable.arrow_down_float);
        } else {
            subQuestions.setVisibility(View.VISIBLE);
            subQuestionsChevron.setImageResource(android.R.drawable.arrow_up_float);
        }
    }
}
