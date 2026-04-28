# TomatoApp Frontend Design Summary

This document outlines the frontend design, layout flow, and XML source codes for the Android application `TomatoApp`. You can use the provided XML blocks to prompt Figma's AI tools to generate the design mockups.

All pages share a common `NavigationView` (Drawer) at the root level (`androidx.drawerlayout.widget.DrawerLayout`), using a `@color/soft_cream` background, and generally feature rounded `MaterialCardView` components with a consistent green/white color palette.

---

## 1. Work Programs Flow

The Work Programs section represents the core farming/cultivation tracking flow.

### **Flow Summary:**
1. **Selection (`WorkProgramSelection`)**: User sees an overview card and a list of existing Work Programs. They can add a new one via a Floating Action Button.
2. **Details (`Workprogram`)**: Tapping a program opens its details. It shows a calendar, phases (1 to 5) legend, task warnings, and buttons to view expenses.
3. **Daily Tasks (`DailyTask`)**: Shows tasks for the specific day along with buttons to Skip or Mark Complete.

<details>
<summary><b>Code: Work Program Selection (activity_work_program_selection.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream">

    <FrameLayout android:id="@+id/main" android:layout_width="match_parent" android:layout_height="match_parent">
        <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical">
            <View android:layout_width="match_parent" android:layout_height="?attr/actionBarSize" android:layout_marginBottom="60dp"/>
            
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent" android:layout_height="wrap_content"
                android:layout_margin="16dp" app:cardCornerRadius="16dp" app:cardElevation="6dp" app:cardBackgroundColor="@color/fresh_green">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:padding="24dp">
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="🌱" android:textSize="32sp" android:layout_marginBottom="8dp" />
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Work Programs" android:textColor="@color/white" android:textSize="24sp" android:textStyle="bold" />
                        <TextView android:id="@+id/programCountText" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="0 programs" android:textColor="@color/white" android:textSize="14sp" android:alpha="0.9" />
                    </LinearLayout>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <androidx.recyclerview.widget.RecyclerView android:id="@+id/workProgramRecycler" android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1" />
        </LinearLayout>

        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/addButton" android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:layout_gravity="end|bottom" android:layout_margin="24dp"
            app:srcCompat="@android:drawable/ic_input_add" app:tint="@android:color/white" app:backgroundTint="@color/tomato_red"/>
    </FrameLayout>
</androidx.drawerlayout.widget.DrawerLayout>
```
</details>

<details>
<summary><b>Code: Work Program Specific Details (activity_workprogram.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawer_layout" android:layout_width="match_parent" android:layout_height="match_parent" android:background="@color/soft_cream">

    <LinearLayout android:id="@+id/main" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical">
        <ScrollView android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                
                <com.google.android.material.card.MaterialCardView android:id="@+id/headerCard" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/fresh_green" app:cardCornerRadius="16dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:padding="24dp">
                        <ImageView android:id="@+id/cultivarImage" android:layout_width="64dp" android:layout_height="64dp" android:src="@mipmap/ic_logo" android:scaleType="centerCrop"/>
                        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_marginStart="16dp">
                            <TextView android:id="@+id/cultivarNameText" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Cultivar Name" android:textColor="@color/white" android:textSize="24sp" android:textStyle="bold" />
                            <TextView android:id="@+id/startDateText" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Start Date: YYYY-MM-DD" android:textColor="@color/white" />
                        </LinearLayout>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <com.google.android.material.card.MaterialCardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                        <com.prolificinteractive.materialcalendarview.MaterialCalendarView android:id="@+id/CalendarView" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" />
                        <!-- Legend configuration for plant phases 1-5 here -->
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <com.google.android.material.button.MaterialButton android:id="@+id/btnCurrentExpenses" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Current Expenses" app:cornerRadius="12dp" android:backgroundTint="@color/warm_orange"/>
            </LinearLayout>
        </ScrollView>
    </LinearLayout>
</androidx.drawerlayout.widget.DrawerLayout>
```
</details>

<details>
<summary><b>Code: Daily Tasks (activity_daily_task.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="match_parent" android:background="@color/soft_cream">

    <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical">
        <ScrollView android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                
                <com.google.android.material.card.MaterialCardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp">
                    <RelativeLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:padding="20dp">
                        <LinearLayout android:layout_width="wrap_content" android:layout_height="wrap_content" android:orientation="vertical" android:layout_centerVertical="true">
                            <TextView android:id="@+id/cultivarNameHeader" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Cultivar Name" android:textSize="24sp" android:textStyle="bold" />
                            <TextView android:id="@+id/dateHeader" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Date: YYYY-MM-DD" android:textSize="12sp" />
                        </LinearLayout>
                    </RelativeLayout>
                </com.google.android.material.card.MaterialCardView>

                <TextView android:id="@+id/taskSectionTitle" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Today's Tasks" android:textSize="18sp" android:textStyle="bold" />
                
                <androidx.recyclerview.widget.RecyclerView android:id="@+id/taskRecyclerView" android:layout_width="match_parent" android:layout_height="wrap_content" />

                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginTop="24dp" android:orientation="horizontal" android:weightSum="2">
                    <com.google.android.material.button.MaterialButton android:id="@+id/btnSkipTasks" style="@style/Widget.Material3.Button.OutlinedButton" android:layout_width="0dp" android:layout_height="72dp" android:layout_weight="1" android:text="Skip Today's Tasks" app:cornerRadius="12dp" />
                    <com.google.android.material.button.MaterialButton android:id="@+id/btnComplete" android:layout_width="0dp" android:layout_height="72dp" android:layout_weight="1" android:text="Mark All Tasks Complete" android:backgroundTint="@color/fresh_green" app:cornerRadius="12dp" />
                </LinearLayout>

            </LinearLayout>
        </ScrollView>
    </LinearLayout>
</androidx.drawerlayout.widget.DrawerLayout>
```
</details>

---

## 2. Expenses Flow

The Expenses section shows Current cumulative expenses and allows the logging of Daily Expenses structured into specific categories.

### **Flow Summary:**
1. **Current Expenses (`CurrentExpensesActivity`)**: Contains an export functionality (PDF and CSV) at the top, and scrollable horizontal tables showing itemized lists of expenses broken down by developmental phases (1 to 5).
2. **Daily Expenses Logging (`DailyExpensesActivity`)**: Shows the total expense and provides categories (Hired Work, Material, Equipment/Tools, Miscellaneous) where users can dynamically add items, edit, and save.

<details>
<summary><b>Code: Current Expenses Overview (activity_current_expenses.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawer_layout" android:layout_width="match_parent" android:layout_height="match_parent" android:background="@color/soft_cream">

    <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical">
        <ScrollView android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">

                <com.google.android.material.card.MaterialCardView android:layout_width="match_parent" android:layout_height="wrap_content" app:cardBackgroundColor="@color/fresh_green" app:cardCornerRadius="16dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="24dp">
                        <TextView android:id="@+id/cultivarHeader" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Cultivar: " android:textColor="@color/white" android:textSize="24sp" android:textStyle="bold" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Action Buttons -->
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center" android:layout_marginTop="16dp">
                    <com.google.android.material.button.MaterialButton android:id="@+id/btnExportPDF" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="Export PDF" android:backgroundTint="@color/tomato_red" app:cornerRadius="8dp" />
                    <com.google.android.material.button.MaterialButton android:id="@+id/btnExportCSV" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="Export CSV" android:backgroundTint="@color/fresh_green" app:cornerRadius="8dp" />
                </LinearLayout>

                <!-- Repeating Card logic for Phase 1 to Phase 5 Tables -->
                <com.google.android.material.card.MaterialCardView android:id="@+id/phase1Card" android:layout_width="match_parent" android:layout_height="wrap_content" app:cardBackgroundColor="@color/white" app:cardCornerRadius="12dp" android:layout_marginTop="16dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Phase 1: Land &amp; Soil Preparation" android:textSize="18sp" android:textStyle="bold" />
                        <HorizontalScrollView android:layout_width="match_parent" android:layout_height="wrap_content">
                            <LinearLayout android:layout_width="wrap_content" android:layout_height="wrap_content" android:orientation="vertical">
                                <include layout="@layout/expense_table_header" />
                                <androidx.recyclerview.widget.RecyclerView android:id="@+id/phase1Table" android:layout_width="wrap_content" android:layout_height="wrap_content" />
                            </LinearLayout>
                        </HorizontalScrollView>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>
                <!-- Imagine this card repeats for Phase 2, 3, 4, and 5 -->
            </LinearLayout>
        </ScrollView>
    </LinearLayout>
</androidx.drawerlayout.widget.DrawerLayout>
```
</details>

<details>
<summary><b>Code: Daily Expenses Entry (activity_daily_expenses.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawer_layout" android:layout_width="match_parent" android:layout_height="match_parent" android:background="@color/soft_cream">

    <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical">
        <ScrollView android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                
                <!-- Total Display -->
                <com.google.android.material.card.MaterialCardView android:layout_width="match_parent" android:layout_height="wrap_content" app:cardBackgroundColor="@color/fresh_green" app:cardCornerRadius="20dp">
                    <TextView android:id="@+id/tvTotalExpenses" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Total: ₱0.00" android:textColor="@color/white" android:textSize="24sp" android:textStyle="bold" android:gravity="center" android:padding="20dp" />
                </com.google.android.material.card.MaterialCardView>

                <!-- Category: Labor -->
                <com.google.android.material.card.MaterialCardView android:layout_width="match_parent" android:layout_height="wrap_content" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp" android:layout_marginTop="16dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="20dp">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="1. Hired Work (Labor)" android:textSize="18sp" android:textStyle="bold" />
                        <LinearLayout android:id="@+id/laborItemsContainer" android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" />
                        <com.google.android.material.button.MaterialButton android:id="@+id/btnAddLaborItem" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="+ Add Labor Item" style="@style/Widget.Material3.Button.OutlinedButton" app:strokeColor="@color/fresh_green"/>
                        <TextView android:id="@+id/tvLaborTotal" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Labor Sub-total: ₱0.00" android:gravity="right" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Imagine repeating Category cards for: 2. Material, 3. Equipment/Tools, 4. Miscellaneous -->

                <!-- Controls -->
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="16dp">
                    <com.google.android.material.button.MaterialButton android:id="@+id/btnEdit" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="Edit" android:backgroundTint="@color/warm_orange" app:cornerRadius="12dp" />
                    <com.google.android.material.button.MaterialButton android:id="@+id/btnSave" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="Save" android:backgroundTint="@color/fresh_green" app:cornerRadius="12dp" />
                </LinearLayout>

            </LinearLayout>
        </ScrollView>
    </LinearLayout>
</androidx.drawerlayout.widget.DrawerLayout>
```
</details>

---

## 3. Integrated Pest Management (IPM) Flow

The IPM section is a hub dashboard allowing users to either scan their crops for pests, view past detection history, or browse the encyclopedia of farming diseases.

### **Flow Summary:**
1. **IPM Dashboard (`IPMActivity`)**: Features a banner header and interactive cards.
2. **Scan Card**: Takes user to a camera feature (`btnMonitorPlant`).
3. **Detection History Card**: Navigates to a list item page of historical records.
4. **Information Card**: A database glossary.
5. **Quick Tips**: Reads tips out securely below the action cards.

<details>
<summary><b>Code: IPM Dashboard (activity_ipm.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawer_layout" android:layout_width="match_parent" android:layout_height="match_parent" android:background="@color/soft_cream">

    <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical">
        <ScrollView android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">

                <!-- Header -->
                <com.google.android.material.card.MaterialCardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="24dp" app:cardBackgroundColor="@color/fresh_green" app:cardCornerRadius="16dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="24dp" android:gravity="center">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="🌱" android:textSize="32sp" />
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Integrated Pest Management" android:textColor="@color/white" android:textSize="24sp" android:textStyle="bold" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Scan Card -->
                <com.google.android.material.card.MaterialCardView android:id="@+id/scanCard" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp" android:clickable="true">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="horizontal" android:padding="24dp" android:gravity="center_vertical">
                        <com.google.android.material.card.MaterialCardView android:layout_width="67dp" android:layout_height="67dp" app:cardBackgroundColor="@color/scan_blue" app:cardCornerRadius="40dp">
                            <ImageView android:layout_width="match_parent" android:layout_height="match_parent" android:src="@android:drawable/ic_menu_camera" app:tint="@color/white" android:padding="5dp"/>
                        </com.google.android.material.card.MaterialCardView>
                        <LinearLayout android:layout_width="wrap_content" android:layout_height="wrap_content" android:orientation="vertical" android:layout_marginStart="16dp">
                            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Scan" android:textSize="20sp" android:textStyle="bold" />
                            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Scan pests and diseases with camera" />
                        </LinearLayout>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- History Card -->
                <com.google.android.material.card.MaterialCardView android:id="@+id/historyCard" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp" android:clickable="true">
                     <!-- Content similar to Scan Card but with @color/warm_orange & ic_menu_recent_history -->
                </com.google.android.material.card.MaterialCardView>

                <!-- Info Card -->
                <com.google.android.material.card.MaterialCardView android:id="@+id/infoCard" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp" android:clickable="true">
                     <!-- Content similar to Scan Card but with @color/info_green & ic_menu_info_details -->
                </com.google.android.material.card.MaterialCardView>

                <!-- Quick Tips -->
                <com.google.android.material.card.MaterialCardView android:layout_width="match_parent" android:layout_height="wrap_content" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="20dp">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="💡 Quick Tips" android:textSize="18sp" android:textStyle="bold" />
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="• Scan leaves regularly to catch pests early\n• Check both sides of leaves for hidden pests\n• Use information database for treatment options\n• Monitor weather conditions for pest activity" android:layout_marginTop="8dp"/>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

            </LinearLayout>
        </ScrollView>
    </LinearLayout>
</androidx.drawerlayout.widget.DrawerLayout>
```
</details>

---

## 4. Projected Expenses / Forecast

The system captures the projected/forecast models for the farming area or weather forecasts potentially mapped to locations.

### **Flow Summary:**
1. **Forecast View (`ForecastActivity`)**: Uses a `NestedScrollView` to display the active "Location" on a map-like card, with a `LinearLayout` (`forecastContainer`) which dynamically mounts Forecast rows. 

<details>
<summary><b>Code: Forecast (activity_forecast.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    android:fillViewport="true">

    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="20dp" android:paddingTop="?attr/actionBarSize">
        <View android:layout_width="match_parent" android:layout_height="?attr/actionBarSize" android:layout_marginBottom="60dp" />

        <com.google.android.material.card.MaterialCardView android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="16dp" app:cardElevation="4dp">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:padding="16dp">
                <ImageView android:layout_width="40dp" android:layout_height="40dp" android:src="@android:drawable/ic_menu_mylocation" app:tint="@color/fresh_green" android:layout_marginEnd="12dp" />
                <TextView android:id="@+id/locationTitle" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="Location" android:textStyle="bold" android:textSize="18sp" android:textColor="@color/text_primary" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Dynamic Forecast Container -->
        <LinearLayout android:id="@+id/forecastContainer" android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" />
    </LinearLayout>

</androidx.core.widget.NestedScrollView>
```
</details>

---

## Design System & Figma Properties Guide
- **Backgrounds**: Soft Cream `#soft_cream` for backgrounds.
- **Accents**: Fresh Green `#fresh_green` for Headers and major Confirm buttons. Tomato Red `#tomato_red` for distinct floats and warning actions. Warm Orange `#warm_orange` for edits and History.
- **Card Styling:** `MaterialCardView` uses `app:cardCornerRadius="16dp"` to `20dp`, with internal paddings of `16dp`-`24dp`, producing a bubbly, friendly, and elevated 3D effect.
- **Navigation Drawer**: All pages utilize `com.google.android.material.navigation.NavigationView` injected from the left as an overlay drawer element.

---

## 5. Main Dashboard & Navigation

The Home screen acts as the central hub of features alongside the shared sidebar.

### **Flow Summary:**
1. **Main Dashboard (`MainActivity`)**: A card-based layout featuring a quick weather widget and large navigational cards to Work Programs, IPM, and Projected Income/Expenses.
2. **Sidebar (`nav_drawer_sidebar`)**: Represents the header of the `NavigationView`, showing the user profile picture, name, and email.

<details>
<summary><b>Code: Main Dashboard (activity_main.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawer_layout" android:layout_width="match_parent" android:layout_height="match_parent" android:background="@color/soft_cream">

    <ScrollView android:layout_width="match_parent" android:layout_height="match_parent" android:fillViewport="true">
        <LinearLayout android:id="@+id/main" android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">

            <!-- Notification Bell Icon -->
            <RelativeLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="8dp">
                <ImageView android:id="@+id/notificationBellIcon" android:layout_width="48dp" android:layout_height="48dp" android:layout_alignParentEnd="true" android:src="@drawable/ic_bell" app:tint="@color/tomato_red" />
            </RelativeLayout>

            <!-- Weather Forecast Card -->
            <com.google.android.material.card.MaterialCardView android:id="@+id/weatherCard" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="12dp">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:padding="12dp">
                    <ImageView android:id="@+id/weatherIcon" android:layout_width="40dp" android:layout_height="40dp" android:src="@android:drawable/ic_menu_compass" app:tint="@color/tomato_red" android:layout_marginEnd="12dp"/>
                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
                        <TextView android:id="@+id/weatherCondition" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Sunny" android:textStyle="bold" />
                        <TextView android:id="@+id/weatherTemp" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="28°C" android:textColor="@color/fresh_green" android:textStyle="bold" />
                    </LinearLayout>
                    <LinearLayout android:layout_width="wrap_content" android:layout_height="wrap_content" android:orientation="vertical" android:gravity="end">
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Current Weather" android:textSize="12sp" android:textStyle="bold" />
                        <TextView android:id="@+id/weatherLocation" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Lopez, Quezon" android:textSize="11sp" />
                    </LinearLayout>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Work Program Card -->
            <com.google.android.material.card.MaterialCardView android:id="@+id/wpsCard" android:layout_width="match_parent" android:layout_height="160dp" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp">
                <RelativeLayout android:layout_width="match_parent" android:layout_height="match_parent" android:padding="20dp">
                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_centerVertical="true">
                        <com.google.android.material.card.MaterialCardView android:layout_width="64dp" android:layout_height="64dp" app:cardBackgroundColor="@color/tomato_red" app:cardCornerRadius="32dp">
                            <ImageView android:layout_width="match_parent" android:layout_height="match_parent" android:src="@android:drawable/ic_menu_agenda" app:tint="@color/white" android:padding="16dp"/>
                        </com.google.android.material.card.MaterialCardView>
                        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:layout_marginStart="16dp">
                            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Work Program" android:textSize="20sp" android:textStyle="bold" />
                            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Manage your cultivation programs" />
                        </LinearLayout>
                    </LinearLayout>
                </RelativeLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- IPM Card -->
            <com.google.android.material.card.MaterialCardView android:id="@+id/ipmCard" android:layout_width="match_parent" android:layout_height="160dp" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp">
                 <!-- Similar structure to Work Program Card but color: fresh_green and icon: info_details -->
            </com.google.android.material.card.MaterialCardView>

            <!-- Projected Income/Expenses Card -->
            <com.google.android.material.card.MaterialCardView android:id="@+id/projectedIncomeCard" android:layout_width="match_parent" android:layout_height="160dp" android:layout_marginBottom="16dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="20dp">
                 <!-- Similar structure to Work Program Card but color: warm_orange and icon: sort_by_size -->
            </com.google.android.material.card.MaterialCardView>

        </LinearLayout>
    </ScrollView>

    <com.google.android.material.navigation.NavigationView android:id="@+id/navigation_view" android:layout_width="280dp" android:layout_height="match_parent" android:layout_gravity="start" />
</androidx.drawerlayout.widget.DrawerLayout>
```
</details>

<details>
<summary><b>Code: Sidebar Header (nav_drawer_sidebar.xml)</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawerHeader"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@color/sidebar_dark_green"
    android:padding="24dp"
    android:gravity="center_horizontal">

    <ImageView
        android:id="@+id/sidebarProfileImage"
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:src="@mipmap/ic_logo"
        android:layout_marginTop="16dp"
        android:layout_marginBottom="16dp"
        android:scaleType="centerCrop"
        android:background="@drawable/circle_background"
        android:padding="3dp"
        android:clipToOutline="true" />

    <TextView
        android:id="@+id/sidebarUserName"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="User Name"
        android:textColor="@color/white"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="4dp" />

    <TextView
        android:id="@+id/sidebarUserEmail"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="user@example.com"
        android:textColor="@color/white"
        android:textSize="12sp"
        android:alpha="0.8"
        android:layout_marginBottom="16dp" />
</LinearLayout>
```
</details>

---

## 6. Authentication Flow

Initial onboarding handles login and signup endpoints.

### **Flow Summary:**
1. **Login (`LoginActivity`)**: Has the logo, fields for Email and Password, and a Google Sign-In button alternative. 
2. **Register (`RegisterActivity`)**: Expands with full name and address input alongside standard email and password.

<details>
<summary><b>Code: Login (activity_login.xml)</b></summary>

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white">

    <ImageView android:id="@+id/logoImage" android:layout_width="120dp" android:layout_height="120dp" android:layout_centerHorizontal="true" android:layout_above="@id/tomatoText" android:src="@mipmap/ic_logo" android:layout_marginTop="40dp" android:layout_marginBottom="12dp"/>
    <TextView android:id="@+id/tomatoText" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Tomato App" android:layout_above="@id/loginCard" android:gravity="center" android:textSize="24sp" android:textStyle="bold" android:textColor="#2E7D32" android:layout_marginBottom="24dp"/>

    <androidx.cardview.widget.CardView android:id="@+id/loginCard" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_margin="24dp" android:layout_centerInParent="true" android:padding="20dp" app:cardCornerRadius="15dp" app:cardElevation="4dp" app:cardBackgroundColor="@color/white">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">
            <TextView android:id="@+id/loginTitle" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginTop="15dp" android:layout_marginBottom="20dp" android:gravity="center" android:text="Login" android:textStyle="bold" android:textSize="22sp"/>

            <com.google.android.material.textfield.TextInputLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_margin="10dp" app:boxStrokeColor="@color/fresh_green" app:hintTextColor="@color/fresh_green">
                <com.google.android.material.textfield.TextInputEditText android:id="@+id/email" android:layout_width="match_parent" android:layout_height="wrap_content" android:hint="Email" android:inputType="textEmailAddress"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_margin="10dp" app:boxStrokeColor="@color/fresh_green" app:hintTextColor="@color/fresh_green">
                <com.google.android.material.textfield.TextInputEditText android:id="@+id/password" android:layout_width="match_parent" android:layout_height="wrap_content" android:hint="Password" android:inputType="textPassword"/>
            </com.google.android.material.textfield.TextInputLayout>

            <Button android:id="@+id/btn_login" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Login" android:backgroundTint="@color/fresh_green" android:layout_margin="10dp"/>
            <com.google.android.gms.common.SignInButton android:id="@+id/btn_google_signin" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_margin="10dp"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <TextView android:id="@+id/registerNow" android:layout_width="match_parent" android:layout_height="wrap_content" android:gravity="center" android:text="New user? Register here" android:textColor="@color/text_secondary" android:layout_below="@id/loginCard" android:layout_marginTop="16dp"/>
</RelativeLayout>
```
</details>

<details>
<summary><b>Code: Register (activity_register.xml)</b></summary>

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white">

    <ImageView android:id="@+id/logoImage" android:layout_width="120dp" android:layout_height="120dp" android:layout_centerHorizontal="true" android:layout_above="@id/tomatoText" android:src="@mipmap/ic_logo" android:layout_marginTop="40dp" android:layout_marginBottom="12dp"/>
    <TextView android:id="@+id/tomatoText" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Tomato App" android:layout_above="@id/registerCard" android:gravity="center" android:textSize="24sp" android:textStyle="bold" android:textColor="#2E7D32" android:layout_marginBottom="24dp"/>

    <androidx.cardview.widget.CardView android:id="@+id/registerCard" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_centerInParent="true" android:layout_margin="20dp" app:cardBackgroundColor="@color/white" app:cardCornerRadius="12dp" app:cardElevation="4dp">
        <ScrollView android:layout_width="match_parent" android:layout_height="wrap_content" android:fillViewport="true">
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="16dp">
                <TextView android:id="@+id/registerTitle" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginBottom="20dp" android:gravity="center" android:text="Register" android:textSize="22sp" android:textStyle="bold" />

                <!-- Add TextInput for Full Name, Address, Email, Password here -->
                <com.google.android.material.textfield.TextInputLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginVertical="6dp" app:boxStrokeColor="@color/tomato_red" app:hintTextColor="@color/tomato_red">
                    <com.google.android.material.textfield.TextInputEditText android:id="@+id/fullName" android:layout_width="match_parent" android:layout_height="wrap_content" android:hint="Full Name" android:inputType="textPersonName" />
                </com.google.android.material.textfield.TextInputLayout>

                <!-- Repeated TextInputLayout blocks omit for brevity ... -->
                
                <Button android:id="@+id/btn_register" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginVertical="10dp" android:backgroundTint="@color/tomato_red" android:text="Register" />
            </LinearLayout>
        </ScrollView>
    </androidx.cardview.widget.CardView>

    <TextView android:id="@+id/loginNow" android:layout_width="match_parent" android:layout_height="wrap_content" android:gravity="center" android:text="Already have an account? Login here" android:textColor="@color/text_secondary" android:layout_below="@id/registerCard" android:layout_marginTop="16dp"/>
</RelativeLayout>
```
</details>


---

## 7. All Layout Files Directory Dump


<details>
<summary><b>Code: activity_analytics.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".AnalyticsActivity">

    <!-- Main Content -->
    <LinearLayout
        android:id="@+id/analyticsRoot"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
    android:padding="16dp"
        android:fitsSystemWindows="true">

        <!-- Header Card with Gradient Background -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="@color/tomato_red"
            app:strokeWidth="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="📊 Analytics Dashboard"
                    android:textColor="@color/white"
                    android:textSize="24sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="8dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Track your cultivar performance and profitability"
                    android:textColor="@color/white"
                    android:textSize="14sp"
                    android:alpha="0.9" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Filters Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="@color/white">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
                    android:layout_marginBottom="12dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
                        android:text="📈 View Mode"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:layout_marginEnd="12dp" />

        <Spinner
            android:id="@+id/viewModeSpinner"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:background="@android:drawable/edit_text"
                        android:padding="8dp" />
                </LinearLayout>

        <View
                    android:layout_width="match_parent"
            android:layout_height="1dp"
                    android:background="@color/divider"
                    android:layout_marginVertical="8dp" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="12dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
                        android:text="🌱 Cultivar Filter"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:layout_marginEnd="12dp" />

        <Spinner
            android:id="@+id/cultivarFilterSpinner"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:background="@android:drawable/edit_text"
                        android:padding="8dp" />
                </LinearLayout>

        <View
                    android:layout_width="match_parent"
            android:layout_height="1dp"
                    android:background="@color/divider"
                    android:layout_marginVertical="8dp" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="12dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
                        android:text="📅 Season Filter"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:layout_marginEnd="12dp" />

        <Spinner
            android:id="@+id/seasonFilterSpinner"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:background="@android:drawable/edit_text"
                        android:padding="8dp" />
                </LinearLayout>

        <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginTop="8dp">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnExportPdf"
                    android:layout_width="0dp"
            android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="📄 PDF"
                    android:layout_marginEnd="6dp"
                    android:textColor="@color/white"
            app:icon="@android:drawable/ic_menu_share"
                    app:iconTint="@color/white"
                    app:backgroundTint="@color/sidebar_dark_green"
                    style="@style/Widget.Material3.Button" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnExportCsv"
                    android:layout_width="0dp"
            android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="📊 CSV"
                    android:layout_marginStart="6dp"
                    android:textColor="@color/white"
            app:icon="@android:drawable/ic_menu_share"
                    app:iconTint="@color/white"
                    app:backgroundTint="@color/scan_blue"
                    style="@style/Widget.Material3.Button" />
                </LinearLayout>
    </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    <ProgressBar
        android:id="@+id/analyticsProgress"
        style="?android:attr/progressBarStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

    <!-- Enhanced Empty State -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/emptyStateCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:cardBackgroundColor="@color/white"
        android:visibility="gone">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="32dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="📊"
                android:textSize="64sp"
                android:layout_marginBottom="16dp" />

            <TextView
                android:id="@+id/emptyText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="No Analytics Data Yet"
                android:textAlignment="center"
                android:textColor="@color/text_primary"
                android:textSize="18sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Run a calculation to see analytics here"
        android:textAlignment="center"
        android:textColor="@color/text_secondary"
                android:textSize="14sp" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/tableRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:paddingTop="4dp"
        android:clipToPadding="false"
        android:paddingBottom="8dp" />

    <!-- Charts Container -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true"
        android:visibility="gone"
        android:id="@+id/chartsContainer"
        android:background="@color/soft_cream">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="8dp">

            <!-- Profit Chart Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="💰 Profit per Area"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:layout_marginBottom="12dp" />

                    <com.github.mikephil.charting.charts.BarChart
                        android:id="@+id/barChart"
                        android:layout_width="match_parent"
                        android:layout_height="300dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Completion Chart Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:cardCornerRadius="12dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="✅ Completion Rate"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:layout_marginBottom="12dp" />

                    <com.github.mikephil.charting.charts.LineChart
                        android:id="@+id/completionChart"
                        android:layout_width="match_parent"
                        android:layout_height="300dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>
    </ScrollView>

</LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>



```
</details>

<details>
<summary><b>Code: activity_calculator.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/white"
    tools:context=".Calculator">

    <!-- Main Content -->
    <ScrollView
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@android:color/white"
        android:fillViewport="true"
        android:fitsSystemWindows="true">

    <LinearLayout
        android:orientation="vertical"
        android:padding="20dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <!-- Summary Card: Projected Net Income -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="6dp"
            app:cardUseCompatPadding="true"
            app:cardBackgroundColor="@color/fresh_green">

            <LinearLayout
                android:orientation="vertical"
                android:padding="20dp"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <!-- Small header: cultivar + date + pill -->
                <LinearLayout
                    android:orientation="horizontal"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content">

                    <LinearLayout
                        android:orientation="vertical"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1">

                        <TextView
                            android:id="@+id/tvCultivarName"
                            android:text="Cultivar: "
                            android:textStyle="bold"
                            android:textSize="14sp"
                            android:textColor="@android:color/white"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content" />

                        <TextView
                            android:id="@+id/tvDateSaved"
                            android:text="Date Saved: "
                            android:textSize="12sp"
                            android:textColor="@android:color/white"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content" />
                    </LinearLayout>

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnDailyExpensesHistory"
                        android:text="Daily Expenses History"
                        android:textAllCaps="false"
                        android:textSize="12sp"
                        android:textColor="@color/fresh_green"
                        android:backgroundTint="@android:color/white"
                        android:paddingLeft="16dp"
                        android:paddingRight="16dp"
                        android:paddingTop="8dp"
                        android:paddingBottom="8dp"
                        android:layout_gravity="center_vertical"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="8dp"
                        android:layout_marginTop="4dp"
                        android:layout_marginBottom="4dp"
                        app:cornerRadius="8dp"
                        android:visibility="gone" />
                </LinearLayout>

                <!-- Big projected net income -->
                <TextView
                    android:id="@+id/tvNetIncomeCard"
                    android:text="₱0.00"
                    android:textSize="32sp"
                    android:textStyle="bold"
                    android:textColor="@android:color/white"
                    android:gravity="center_horizontal"
                    android:layout_marginTop="16dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <!-- Subtitle -->
                <TextView
                    android:id="@+id/tvSummarySubtitle"
                    android:text="Projected net income"
                    android:textSize="12sp"
                    android:textColor="@android:color/white"
                    android:layout_marginTop="4dp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvCompletionRate"
                    android:text="Completion rate: —"
                    android:textSize="12sp"
                    android:textColor="@android:color/white"
                    android:layout_marginTop="8dp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvAdjustedNetIncome"
                    android:text="₱—"
                    android:textSize="20sp"
                    android:textStyle="bold"
                    android:textColor="@android:color/white"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvAdjustedSubtitle"
                    android:text="Adjusted net income (based on completion)"
                    android:textSize="12sp"
                    android:textColor="@android:color/white"
                    android:layout_marginTop="2dp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvCompletionWarning"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:padding="10dp"
                    android:text="Completion impact warning"
                    android:textColor="@color/text_primary"
                    android:textSize="12sp"
                    android:background="@drawable/warning_banner_background"
                    android:visibility="gone" />

            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Harvest Prediction Card -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#E8F5E9">

            <LinearLayout
                android:orientation="vertical"
                android:padding="16dp"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <LinearLayout
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="12dp">

                    <TextView
                        android:text="Harvest Prediction"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="#333333"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1" />
                </LinearLayout>

                <TextView
                    android:text="Yield Prediction per kg/hectare"
                    android:textSize="14sp"
                    android:textColor="#666666"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvHarvestPredictionYieldPerHa"
                    android:text="— kg/hectare"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:text="Predicted Total Yield in kg per hectare"
                    android:textSize="14sp"
                    android:textColor="#666666"
                    android:layout_marginTop="12dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvHarvestPredictionTotalYield"
                    android:text="— kg"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:text="Predicted Harvest Date"
                    android:textSize="14sp"
                    android:textColor="#666666"
                    android:layout_marginTop="12dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvHarvestPredictionDate"
                    android:text="—"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Project Input title -->
        <TextView
            android:id="@+id/tvProjectInputTitle"
            android:text="Project Input"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="#333333"
            android:layout_marginBottom="8dp"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

        <!-- Project Input Card -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#FFFFFF">

            <LinearLayout
                android:orientation="vertical"
                android:padding="16dp"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <TextView
                    android:text="Area/Hectare"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:textColor="#666666"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <EditText
                    android:id="@+id/etHectare"
                    android:hint="Enter Hectare"
                    android:inputType="numberDecimal"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp" />

                <TextView
                    android:id="@+id/tvNP"
                    android:text="Number of Plants (NP): "
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:textColor="#666666"
                    android:layout_marginTop="16dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:text="Average Weight per Fruit (grams)"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:textColor="#666666"
                    android:layout_marginTop="16dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <EditText
                    android:id="@+id/etAWF"
                    android:hint="Enter Average Weight per Fruit (grams)"
                    android:inputType="numberDecimal"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp" />

                <TextView
                    android:text="Average Number of Fruits per Plant"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:textColor="#666666"
                    android:layout_marginTop="16dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <EditText
                    android:id="@+id/etAFP"
                    android:hint="Enter Average Number of Fruits per Plant"
                    android:inputType="numberDecimal"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp" />

                <TextView
                    android:text="Market Value/Price"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:textColor="#666666"
                    android:layout_marginTop="16dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <EditText
                    android:id="@+id/etMarketValue"
                    android:hint="Enter Market Value per KG (₱)"
                    android:inputType="numberDecimal"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp" />

                <TextView
                    android:text="Sub-total Harvest:"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:textColor="#666666"
                    android:layout_marginTop="20dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <androidx.cardview.widget.CardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    app:cardCornerRadius="4dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="@android:color/transparent"
                    app:strokeWidth="1dp"
                    app:strokeColor="#CCCCCC">

                    <LinearLayout
                        android:orientation="horizontal"
                        android:gravity="center_vertical"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:minHeight="48dp"
                        android:paddingStart="12dp"
                        android:paddingEnd="12dp"
                        android:paddingTop="8dp"
                        android:paddingBottom="8dp">

                        <TextView
                            android:id="@+id/tvSubTotalHarvest"
                            android:text="₱—"
                            android:textSize="16sp"
                            android:textColor="#333333"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:gravity="start|center_vertical" />

                        <View
                            android:layout_width="1dp"
                            android:layout_height="24dp"
                            android:background="#CCCCCC"
                            android:layout_marginStart="8dp"
                            android:layout_marginEnd="8dp" />

                        <Spinner
                            android:id="@+id/spinnerHarvestUnit"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:minWidth="80dp"
                            android:background="@android:color/transparent" />
                    </LinearLayout>
                </androidx.cardview.widget.CardView>

            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Expenses Breakdown title -->
        <TextView
            android:text="Expenses Breakdown"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="#333333"
                    android:layout_marginTop="8dp"
            android:layout_marginBottom="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

        <!-- Labor Expense Card -->
        <androidx.cardview.widget.CardView
            android:id="@+id/laborCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#FFFFFF">

            <LinearLayout
                android:orientation="vertical"
                android:padding="16dp"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <TextView
                    android:text="Labor"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:text="Fetched from current expenses"
                    android:textSize="12sp"
                    android:textColor="#777777"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <LinearLayout
                    android:id="@+id/laborItemsContainer"
                    android:orientation="vertical"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp" />

                <TextView
                    android:id="@+id/tvLaborTotalWorkers"
                    android:text="Total Number of Workers: 0"
                    android:textSize="14sp"
                    android:textColor="#666666"
                    android:layout_marginTop="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvLaborTotalCost"
                    android:text="Labor Total Cost: ₱0.00"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginTop="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Material Expense Card -->
        <androidx.cardview.widget.CardView
            android:id="@+id/materialCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#FFFFFF">

            <LinearLayout
                android:orientation="vertical"
                android:padding="16dp"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <TextView
                    android:text="Material"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:text="Fetched from current expenses"
                    android:textSize="12sp"
                    android:textColor="#777777"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <LinearLayout
                    android:id="@+id/materialItemsContainer"
                    android:orientation="vertical"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp" />

                <TextView
                    android:id="@+id/tvMaterialTotalCost"
                    android:text="Material Total Cost: ₱0.00"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginTop="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Equipment/Tools Expense Card -->
        <androidx.cardview.widget.CardView
            android:id="@+id/equipmentCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#FFFFFF">

            <LinearLayout
                android:orientation="vertical"
                android:padding="16dp"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <TextView
                    android:text="Equipment/Tools"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:text="Fetched from current expenses"
                    android:textSize="12sp"
                    android:textColor="#777777"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <LinearLayout
                    android:id="@+id/equipmentItemsContainer"
                    android:orientation="vertical"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp" />

                <TextView
                    android:id="@+id/tvEquipmentTotalUsage"
                    android:text="Total Usage: 0 hours"
                    android:textSize="14sp"
                    android:textColor="#666666"
                    android:layout_marginTop="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvEquipmentTotalCost"
                    android:text="Equipment/Tools Total Cost: ₱0.00"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginTop="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Miscellaneous Expense Card -->
        <androidx.cardview.widget.CardView
            android:id="@+id/miscellaneousCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#FFFFFF">

            <LinearLayout
                android:orientation="vertical"
                android:padding="16dp"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <TextView
                    android:text="Miscellaneous"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:text="Fetched from current expenses"
                    android:textSize="12sp"
                    android:textColor="#777777"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <LinearLayout
                    android:id="@+id/miscItemsContainer"
                    android:orientation="vertical"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp" />

                <TextView
                    android:id="@+id/tvMiscellaneousTotalCost"
                    android:text="Miscellaneous Total Cost: ₱0.00"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginTop="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Expenses Section -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#FFFFFF">

            <LinearLayout
                android:orientation="vertical"
                android:padding="16dp"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <TextView
                    android:text="Expenses"
                    android:textSize="20sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:text="Total cost computation"
                    android:textSize="12sp"
                    android:textColor="#777777"
                    android:layout_marginBottom="12dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <!-- Aggregated Expense Totals from Daily Expenses -->
                <TextView
                    android:id="@+id/tvExpensesLaborTotal"
                    android:text="Labor Total Cost: ₱0.00"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginBottom="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvExpensesEquipmentTotal"
                    android:text="Equipment/Tools Total Cost: ₱0.00"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginBottom="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvExpensesMaterialTotal"
                    android:text="Material Total Cost: ₱0.00"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginBottom="8dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvExpensesMiscellaneousTotal"
                    android:text="Miscellaneous Total Cost: ₱0.00"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginBottom="12dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvTotalExpenses"
                    android:text="Total Expenses: ₱0.00"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:layout_marginTop="8dp"
                    android:textColor="#444444"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvAdjustedExpenses"
                    android:text="Adjusted Expenses: ₱0.00"
                    android:textSize="14sp"
                    android:textColor="#666666"
                    android:layout_marginTop="4dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Save to Analytics Button -->
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnSaveToAnalytics"
            android:text="Save to Analytics"
            android:textAllCaps="false"
            android:textSize="16sp"
            android:textColor="@android:color/white"
            android:backgroundTint="@color/sidebar_dark_green"
            android:paddingLeft="24dp"
            android:paddingRight="24dp"
            android:paddingTop="16dp"
            android:paddingBottom="16dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:visibility="gone"
            app:cornerRadius="12dp" />

    </LinearLayout>
    </ScrollView>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_camera_interface.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/black"
    tools:context=".CameraInterface">

    <!-- Camera Preview -->
    <androidx.camera.view.PreviewView
        android:id="@+id/previewView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="fitCenter"
        tools:ignore="MissingConstraints"
        tools:background="@android:color/black" />

    <!-- Focus Point Indicator (White circle with dot) -->
    <View
        android:id="@+id/focusIndicator"
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:layout_centerInParent="true"
        android:background="@drawable/focus_indicator"
        android:visibility="visible" />

    <!-- Top Header Bar (Translucent) -->
    <LinearLayout
        android:id="@+id/headerBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:paddingTop="48dp"
        android:paddingBottom="16dp"
        android:background="@drawable/header_gradient_overlay"
        android:fitsSystemWindows="true">

        <!-- Back Arrow -->
        <ImageButton
            android:id="@+id/backButton"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:src="@android:drawable/ic_menu_revert"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="Back"
            app:tint="@color/white"
            android:scaleType="centerInside"
            android:padding="8dp" />

        <!-- Title -->
        <TextView
            android:id="@+id/headerTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Scan"
            android:textColor="@color/white"
            android:textSize="18sp"
            android:textStyle="bold"
            android:gravity="center"
            android:maxLines="1"
            android:ellipsize="end" />
    </LinearLayout>

    <LinearLayout
        android:id="@+id/detectionContextPanel"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/headerBar"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginTop="8dp"
        android:orientation="vertical"
        android:background="@drawable/legend_background"
        android:padding="12dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@string/detection_select_cultivar_label"
            android:textColor="@color/text_primary"
            android:textStyle="bold"
            android:textSize="14sp" />

        <Spinner
            android:id="@+id/spinnerCultivar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="6dp"
            android:background="@drawable/legend_background" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="10dp"
            android:text="@string/detection_select_phase_label"
            android:textColor="@color/text_primary"
            android:textStyle="bold"
            android:textSize="14sp" />

        <Spinner
            android:id="@+id/spinnerPhase"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="6dp"
            android:background="@drawable/legend_background" />
    </LinearLayout>

    <!-- Bottom Navigation Bar (Semi-transparent dark) -->
    <RelativeLayout
        android:id="@+id/bottomNavBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:paddingStart="24dp"
        android:paddingEnd="24dp"
        android:paddingTop="20dp"
        android:paddingBottom="32dp"
        android:background="@drawable/bottom_nav_gradient"
        android:fitsSystemWindows="true">

        <!-- Scan Button (Centered) -->
        <LinearLayout
            android:id="@+id/scanButtonContainer"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerHorizontal="true"
            android:orientation="vertical"
            android:gravity="center">

            <!-- Large Orange Shutter Button -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/captureBtn"
                android:layout_width="80dp"
                android:layout_height="80dp"
                app:cardBackgroundColor="@color/warm_orange"
                app:cardCornerRadius="40dp"
                app:cardElevation="8dp"
                android:clickable="true"
                android:focusable="true"
                android:foreground="?android:attr/selectableItemBackground">

                <!-- Inner darker orange circle -->
                <View
                    android:layout_width="64dp"
                    android:layout_height="64dp"
                    android:layout_gravity="center"
                    android:background="@drawable/capture_button_inner" />
            </com.google.android.material.card.MaterialCardView>

            <!-- "Scan" Text Below Button -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="Scan"
                android:textColor="@color/white"
                android:textSize="14sp"
                android:textStyle="bold" />
        </LinearLayout>

        <!-- Menu Button (Right - Hamburger) -->
        <ImageButton
            android:id="@+id/menuButton"
            android:layout_width="56dp"
            android:layout_height="56dp"
            android:layout_alignParentEnd="true"
            android:layout_centerVertical="true"
            android:src="@android:drawable/ic_menu_more"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="Menu"
            app:tint="@color/white"
            android:scaleType="centerInside"
            android:padding="14dp" />

        <!-- Model Selector (Hidden, kept for code compatibility) -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/modelSelectorBtn"
            android:layout_width="56dp"
            android:layout_height="56dp"
            android:visibility="gone"
            app:cardBackgroundColor="@color/warm_orange"
            app:cardCornerRadius="28dp"
            app:cardElevation="4dp"
            android:clickable="true"
            android:focusable="true">

            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:src="@android:drawable/ic_menu_view"
                android:scaleType="centerInside"
                android:padding="14dp"
                app:tint="@color/white" />
        </com.google.android.material.card.MaterialCardView>
    </RelativeLayout>

    <!-- Gallery Button (Hidden, can be accessed via menu) -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/openGalleryButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_alignParentEnd="true"
        android:layout_marginEnd="24dp"
        android:layout_marginBottom="120dp"
        android:src="@android:drawable/ic_menu_gallery"
        android:contentDescription="@string/information"
        android:visibility="gone"
        app:fabSize="mini"
        app:elevation="4dp"
        app:tint="@color/white"
        app:backgroundTint="@color/primary" />

</RelativeLayout>

```
</details>

<details>
<summary><b>Code: activity_cost_selection.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".CostSelection">

    <!-- Main Content -->
    <LinearLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <!-- Spacer for Action Bar -->
        <View
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:layout_marginBottom="60dp"/>

        <!-- Header Section -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:layout_marginEnd="16dp"
            android:layout_marginTop="24dp"
            android:layout_marginBottom="8dp"
            app:cardBackgroundColor="@color/white"
            app:cardCornerRadius="20dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal"
                android:padding="20dp">

                <ImageView
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:layout_marginEnd="16dp"
                    android:contentDescription="Tomato App Logo"
                    android:src="@mipmap/ic_logo" />

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Projected Income/Expenses"
                        android:textColor="@color/text_primary"
                        android:textSize="24sp"
                        android:textStyle="bold" />

                    <TextView
                        android:id="@+id/programCountText"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="0 programs"
                        android:textColor="@color/text_secondary"
                        android:textSize="14sp" />
                </LinearLayout>

                <ImageView
                    android:id="@+id/headerMenuButton"
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@android:drawable/ic_menu_more"
                    android:clickable="true"
                    android:focusable="true"
                    android:contentDescription="Sort options"
                    app:tint="@color/text_secondary" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- RecyclerView for Cards -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/costRecycler"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:clipToPadding="false"
            android:padding="12dp"
            android:scrollbars="vertical" />

        <!-- Empty State (initially hidden) -->
        <LinearLayout
            android:id="@+id/emptyState"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="32dp"
            android:visibility="gone">

            <ImageView
                android:layout_width="120dp"
                android:layout_height="120dp"
                android:src="@mipmap/ic_logo"
                android:alpha="0.3"
                android:layout_marginBottom="24dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="No Calculations Yet"
                android:textColor="@color/text_primary"
                android:textSize="20sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Select a work program to calculate projected income and expenses"
                android:textColor="@color/text_secondary"
                android:textSize="14sp"
                android:gravity="center" />
        </LinearLayout>

        <!-- Floating Action Button -->
        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/addButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="end|bottom"
            android:layout_margin="24dp"
            android:contentDescription="Add New Calculation"
            app:srcCompat="@android:drawable/ic_input_add"
            app:tint="@android:color/white"
            app:backgroundTint="@color/tomato_red"
            app:elevation="8dp"
            app:pressedTranslationZ="12dp" />
    </LinearLayout>

    <!-- Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_cultivar_details.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".CultivarDetailsActivity">

    <!-- Main Content -->
    <LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
        android:fitsSystemWindows="true">

        <!-- Header Card with Cultivar Name -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="@color/warm_orange"
            app:strokeWidth="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="🌱"
                    android:textSize="32sp"
                    android:layout_marginBottom="8dp" />

    <TextView
        android:id="@+id/cultivarTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Cultivar Name"
                    android:textSize="26sp"
        android:textStyle="bold"
                    android:textColor="@color/white" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Work Program Details"
                    android:textSize="14sp"
                    android:textColor="@color/white"
                    android:alpha="0.9"
                    android:layout_marginTop="4dp" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Controls Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="@color/white">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
                    android:layout_marginBottom="12dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
                        android:text="📊 View Mode"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:layout_marginEnd="12dp" />

        <Spinner
            android:id="@+id/viewModeSpinner"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:background="@android:drawable/edit_text"
                        android:padding="8dp" />
                </LinearLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnExportPdf"
                    android:layout_width="match_parent"
            android:layout_height="wrap_content"
                    android:text="📄 Export to PDF"
                    android:textColor="@color/white"
            app:icon="@android:drawable/ic_menu_share"
                    app:iconTint="@color/white"
                    app:backgroundTint="@color/sidebar_dark_green"
                    style="@style/Widget.Material3.Button" />
    </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    <ProgressBar
        android:id="@+id/progressBar"
        style="?android:attr/progressBarStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

    <!-- Enhanced Empty State -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/emptyStateCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:cardBackgroundColor="@color/white"
        android:visibility="gone">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="32dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="📋"
                android:textSize="64sp"
                android:layout_marginBottom="16dp" />

            <TextView
                android:id="@+id/emptyText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="No Work Programs Found"
                android:textAlignment="center"
                android:textColor="@color/text_primary"
                android:textSize="18sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <TextView
                android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="No work programs found for this cultivar."
        android:textAlignment="center"
        android:textColor="@color/text_secondary"
                android:textSize="14sp" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <!-- Spreadsheet/Table View -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/spreadsheetContainer"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_marginBottom="8dp"
        app:cardCornerRadius="12dp"
        app:cardElevation="4dp"
        app:cardBackgroundColor="@color/white">

        <HorizontalScrollView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:fillViewport="true"
            android:scrollbars="horizontal">

            <ScrollView
                android:layout_width="wrap_content"
                android:layout_height="match_parent"
                android:fillViewport="true"
                android:scrollbars="vertical">

                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="vertical">

                    <!-- Header Row -->
                    <LinearLayout
                        android:id="@+id/spreadsheetHeader"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:background="@color/sidebar_dark_green"
                        android:padding="0dp"
                        android:layout_margin="0dp">

                        <TextView
                            android:layout_width="120dp"
                            android:layout_height="48dp"
                            android:text="Cultivar"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="120dp"
                            android:maxWidth="120dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="100dp"
                            android:layout_height="48dp"
                            android:text="Area"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="100dp"
                            android:maxWidth="100dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="110dp"
                            android:layout_height="48dp"
                            android:text="Start Date"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="110dp"
                            android:maxWidth="110dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="100dp"
                            android:layout_height="48dp"
                            android:text="Phase 1"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="100dp"
                            android:maxWidth="100dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="100dp"
                            android:layout_height="48dp"
                            android:text="Phase 2"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="100dp"
                            android:maxWidth="100dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="100dp"
                            android:layout_height="48dp"
                            android:text="Phase 3"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="100dp"
                            android:maxWidth="100dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="100dp"
                            android:layout_height="48dp"
                            android:text="Phase 4"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="100dp"
                            android:maxWidth="100dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="100dp"
                            android:layout_height="48dp"
                            android:text="Phase 5"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="100dp"
                            android:maxWidth="100dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="100dp"
                            android:layout_height="48dp"
                            android:text="Detections"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="start|center_vertical"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="100dp"
                            android:maxWidth="100dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="130dp"
                            android:layout_height="48dp"
                            android:text="Income"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="center"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="130dp"
                            android:maxWidth="130dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="130dp"
                            android:layout_height="48dp"
                            android:text="Expenses"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="center"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="130dp"
                            android:maxWidth="130dp"
                            android:layout_margin="0dp" />

                        <TextView
                            android:layout_width="130dp"
                            android:layout_height="48dp"
                            android:text="Profit"
                            android:textColor="@color/white"
                            android:textStyle="bold"
                            android:gravity="center"
                            android:padding="8dp"
                            android:background="@drawable/spreadsheet_cell_border"
                            android:minWidth="130dp"
                            android:maxWidth="130dp" />
                    </LinearLayout>

                    <!-- Data Rows Container -->
                    <LinearLayout
                        android:id="@+id/spreadsheetRows"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:orientation="vertical" />
                </LinearLayout>
            </ScrollView>
        </HorizontalScrollView>
    </com.google.android.material.card.MaterialCardView>

    <!-- Charts View -->
    <ScrollView
        android:id="@+id/chartsContainer"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true"
        android:visibility="gone"
        android:background="@color/soft_cream">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="8dp">

            <!-- Income vs Expenses Chart Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
        android:orientation="vertical"
                    android:padding="16dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="💰 Income vs Expenses"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:layout_marginBottom="12dp" />

        <com.github.mikephil.charting.charts.BarChart
            android:id="@+id/barChart"
            android:layout_width="match_parent"
                        android:layout_height="300dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Profit Trend Chart Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:cardCornerRadius="12dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="📈 Profit Trend"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:layout_marginBottom="12dp" />

        <com.github.mikephil.charting.charts.LineChart
            android:id="@+id/lineChart"
            android:layout_width="match_parent"
                        android:layout_height="300dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>
    </ScrollView>

    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>


```
</details>

<details>
<summary><b>Code: activity_current_expenses.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".CurrentExpensesActivity">

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:fitsSystemWindows="true">


        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:fillViewport="true">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <!-- Header Card -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/fresh_green"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="6dp"
                    app:strokeWidth="0dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="24dp">

                        <TextView
                            android:id="@+id/cultivarHeader"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Cultivar: "
                            android:textColor="@color/white"
                            android:textSize="24sp"
                            android:textStyle="bold" />

                        <TextView
                            android:id="@+id/dateRangeHeader"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:text="Date Range: "
                            android:textColor="@color/white"
                            android:textSize="14sp"
                            android:alpha="0.9" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Export Buttons -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginBottom="16dp"
                    android:gravity="center">

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnExportPDF"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginEnd="8dp"
                        android:text="Export PDF"
                        android:textColor="@color/white"
                        android:backgroundTint="@color/tomato_red"
                        android:textSize="14sp"
                        android:padding="12dp"
                        app:cornerRadius="8dp" />

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnExportCSV"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginStart="8dp"
                        android:text="Export CSV"
                        android:textColor="@color/white"
                        android:backgroundTint="@color/fresh_green"
                        android:textSize="14sp"
                        android:padding="12dp"
                        app:cornerRadius="8dp" />
                </LinearLayout>

                <!-- Phase 1 Table Container -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/phase1Card"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="12dp"
                    app:cardElevation="2dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Phase 1: Land &amp; Soil Preparation"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="12dp" />

                        <!-- Table Header -->
                        <View
                            android:layout_width="match_parent"
                            android:layout_height="1dp"
                            android:background="@color/text_secondary"
                            android:alpha="0.3"
                            android:layout_marginBottom="8dp" />

                        <!-- Horizontal Scrollable Table -->
                        <HorizontalScrollView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:scrollbars="horizontal"
                            android:fadeScrollbars="false">

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:orientation="vertical">

                                <include layout="@layout/expense_table_header" />

                                <androidx.recyclerview.widget.RecyclerView
                                    android:id="@+id/phase1Table"
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content" />
                            </LinearLayout>
                        </HorizontalScrollView>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Phase 2 Table Container -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/phase2Card"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="12dp"
                    app:cardElevation="2dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Phase 2: Vegetative"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="12dp" />

                        <View
                            android:layout_width="match_parent"
                            android:layout_height="1dp"
                            android:background="@color/text_secondary"
                            android:alpha="0.3"
                            android:layout_marginBottom="8dp" />

                        <!-- Horizontal Scrollable Table -->
                        <HorizontalScrollView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:scrollbars="horizontal"
                            android:fadeScrollbars="false">

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:orientation="vertical">

                                <include layout="@layout/expense_table_header" />

                                <androidx.recyclerview.widget.RecyclerView
                                    android:id="@+id/phase2Table"
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content" />
                            </LinearLayout>
                        </HorizontalScrollView>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Phase 3 Table Container -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/phase3Card"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="12dp"
                    app:cardElevation="2dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Phase 3: Flowering"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="12dp" />

                        <View
                            android:layout_width="match_parent"
                            android:layout_height="1dp"
                            android:background="@color/text_secondary"
                            android:alpha="0.3"
                            android:layout_marginBottom="8dp" />

                        <!-- Horizontal Scrollable Table -->
                        <HorizontalScrollView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:scrollbars="horizontal"
                            android:fadeScrollbars="false">

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:orientation="vertical">

                                <include layout="@layout/expense_table_header" />

                                <androidx.recyclerview.widget.RecyclerView
                                    android:id="@+id/phase3Table"
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content" />
                            </LinearLayout>
                        </HorizontalScrollView>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Phase 4 Table Container -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/phase4Card"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="12dp"
                    app:cardElevation="2dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Phase 4: Maturity"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="12dp" />

                        <View
                            android:layout_width="match_parent"
                            android:layout_height="1dp"
                            android:background="@color/text_secondary"
                            android:alpha="0.3"
                            android:layout_marginBottom="8dp" />

                        <!-- Horizontal Scrollable Table -->
                        <HorizontalScrollView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:scrollbars="horizontal"
                            android:fadeScrollbars="false">

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:orientation="vertical">

                                <include layout="@layout/expense_table_header" />

                                <androidx.recyclerview.widget.RecyclerView
                                    android:id="@+id/phase4Table"
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content" />
                            </LinearLayout>
                        </HorizontalScrollView>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Phase 5 Table Container -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/phase5Card"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="12dp"
                    app:cardElevation="2dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Phase 5: Post-harvest"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="12dp" />

                        <View
                            android:layout_width="match_parent"
                            android:layout_height="1dp"
                            android:background="@color/text_secondary"
                            android:alpha="0.3"
                            android:layout_marginBottom="8dp" />

                        <!-- Horizontal Scrollable Table -->
                        <HorizontalScrollView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:scrollbars="horizontal"
                            android:fadeScrollbars="false">

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:orientation="vertical">

                                <include layout="@layout/expense_table_header" />

                                <androidx.recyclerview.widget.RecyclerView
                                    android:id="@+id/phase5Table"
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content" />
                            </LinearLayout>
                        </HorizontalScrollView>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>
            </LinearLayout>
        </ScrollView>
    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>


```
</details>

<details>
<summary><b>Code: activity_daily_expenses.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".DailyExpensesActivity">

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:fitsSystemWindows="true">

        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:fillViewport="true">

            <LinearLayout
                android:id="@+id/mainContentLayout"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <!-- Header Card -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="20dp">

                        <TextView
                            android:id="@+id/cultivarHeader"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Cultivar: "
                            android:textColor="@color/text_primary"
                            android:textSize="20sp"
                            android:textStyle="bold" />

                        <TextView
                            android:id="@+id/dateHeader"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="8dp"
                            android:text="Date: "
                            android:textColor="@color/text_secondary"
                            android:textSize="14sp" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Total Expenses Display -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/fresh_green"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <TextView
                        android:id="@+id/tvTotalExpenses"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Total: ₱0.00"
                        android:textColor="@color/white"
                        android:textSize="24sp"
                        android:textStyle="bold"
                        android:gravity="center"
                        android:padding="20dp" />
                </com.google.android.material.card.MaterialCardView>

                <!-- 1. Hired Work (Labor) Card -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="20dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="1. Hired Work (Labor)"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="16dp" />

                        <LinearLayout
                            android:id="@+id/laborItemsContainer"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical">

                            <!-- Labor items will be added dynamically here -->
                        </LinearLayout>

                        <!-- Add Labor Item Button -->
                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnAddLaborItem"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="+ Add Labor Item"
                            android:textColor="@color/fresh_green"
                            android:backgroundTint="@android:color/transparent"
                            android:outlineProvider="none"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            app:strokeColor="@color/fresh_green"
                            android:layout_marginTop="8dp"
                            android:enabled="false" />

                        <TextView
                            android:id="@+id/tvLaborTotal"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="Labor Sub-total: ₱0.00"
                            android:textColor="@color/fresh_green"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            android:gravity="right"
                            android:layout_marginTop="12dp"
                            android:padding="8dp" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- 2. Material Card -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="20dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="2. Material"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="16dp" />

                        <LinearLayout
                            android:id="@+id/materialItemsContainer"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical">

                            <!-- Material items will be added dynamically here -->
                        </LinearLayout>

                        <!-- Add Material Item Button -->
                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnAddMaterialItem"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="+ Add Material Item"
                            android:textColor="@color/fresh_green"
                            android:backgroundTint="@android:color/transparent"
                            android:outlineProvider="none"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            app:strokeColor="@color/fresh_green"
                            android:layout_marginTop="8dp"
                            android:enabled="false" />

                        <TextView
                            android:id="@+id/tvMaterialTotal"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="Material Sub-total: ₱0.00"
                            android:textColor="@color/fresh_green"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            android:gravity="right"
                            android:layout_marginTop="12dp"
                            android:padding="8dp" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- 3. Equipment / Tools Card -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="20dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="3. Equipment / Tools"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="16dp" />

                        <LinearLayout
                            android:id="@+id/equipmentItemsContainer"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical">

                            <!-- Equipment items will be added dynamically here -->
                        </LinearLayout>

                        <!-- Add Equipment Item Button -->
                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnAddEquipmentItem"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="+ Add Equipment Item"
                            android:textColor="@color/fresh_green"
                            android:backgroundTint="@android:color/transparent"
                            android:outlineProvider="none"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            app:strokeColor="@color/fresh_green"
                            android:layout_marginTop="8dp"
                            android:enabled="false" />

                        <TextView
                            android:id="@+id/tvEquipmentTotal"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="Equipment Sub-total: ₱0.00"
                            android:textColor="@color/fresh_green"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            android:gravity="right"
                            android:layout_marginTop="12dp"
                            android:padding="8dp" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- 4. Miscellaneous Card -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="20dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="4. Miscellaneous"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="16dp" />

                        <LinearLayout
                            android:id="@+id/miscItemsContainer"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical">

                            <!-- Miscellaneous items will be added dynamically here -->
                        </LinearLayout>

                        <!-- Add Miscellaneous Item Button -->
                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnAddMiscItem"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="+ Add Miscellaneous Item"
                            android:textColor="@color/fresh_green"
                            android:backgroundTint="@android:color/transparent"
                            android:outlineProvider="none"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            app:strokeColor="@color/fresh_green"
                            android:layout_marginTop="8dp"
                            android:enabled="false" />

                        <TextView
                            android:id="@+id/tvMiscTotal"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="Miscellaneous Sub-total: ₱0.00"
                            android:textColor="@color/fresh_green"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            android:gravity="right"
                            android:layout_marginTop="12dp"
                            android:padding="8dp" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Edit and Save Buttons -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginTop="16dp"
                    android:layout_marginBottom="24dp">

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnEdit"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginEnd="8dp"
                        android:text="Edit"
                        android:textColor="@color/white"
                        android:backgroundTint="@color/warm_orange"
                        android:textSize="16sp"
                        android:padding="16dp"
                        app:cornerRadius="12dp" />

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnSave"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginStart="8dp"
                        android:text="Save"
                        android:textColor="@color/white"
                        android:backgroundTint="@color/fresh_green"
                        android:textSize="16sp"
                        android:padding="16dp"
                        app:cornerRadius="12dp"
                        android:enabled="false" />
                </LinearLayout>
            </LinearLayout>
        </ScrollView>
    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>
```
</details>

<details>
<summary><b>Code: activity_daily_expenses_history.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".DailyExpensesHistoryActivity">

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:fitsSystemWindows="true">


        <!-- Header Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"
            app:cardBackgroundColor="@color/fresh_green"
            app:cardCornerRadius="16dp"
            app:cardElevation="6dp"
            app:strokeWidth="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:id="@+id/headerText"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Daily Expenses History"
                    android:textColor="@color/white"
                    android:textSize="20sp"
                    android:textStyle="bold" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- RecyclerView -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:padding="16dp"
            android:clipToPadding="false" />

        <!-- Empty State -->
        <TextView
            android:id="@+id/emptyState"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:gravity="center"
            android:text="No daily expenses recorded yet"
            android:textColor="@color/text_secondary"
            android:textSize="16sp"
            android:visibility="gone" />
    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>


```
</details>

<details>
<summary><b>Code: activity_daily_task.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".DailyTask">

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:fitsSystemWindows="true">

        <!-- Spacer for Action Bar -->
        <View
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize" />

        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:fillViewport="true">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <!-- Header Card with Cultivar Info -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <RelativeLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:padding="20dp">

                        <!-- Left Side: Text Info -->
                        <LinearLayout
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:layout_alignParentStart="true"
                            android:layout_centerVertical="true"
                            android:layout_toStartOf="@+id/cultivarImageHeader">

                            <TextView
                                android:id="@+id/cultivarNameHeader"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Cultivar Name"
                                android:textColor="@color/text_primary"
                                android:textSize="24sp"
                                android:textStyle="bold" />

                            <TextView
                                android:id="@+id/cultivarDescription"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="4dp"
                                android:text="planting tasks"
                                android:textColor="@color/text_secondary"
                                android:textSize="14sp"
                                android:maxLines="2"
                                android:ellipsize="end" />

                            <TextView
                                android:id="@+id/dateHeader"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="8dp"
                                android:text="Date: YYYY-MM-DD"
                                android:textColor="@color/text_secondary"
                                android:textSize="12sp" />
                        </LinearLayout>

                        <!-- Right Side: Cultivar Image -->
                        <ImageView
                            android:id="@+id/cultivarImageHeader"
                            android:layout_width="100dp"
                            android:layout_height="100dp"
                            android:layout_alignParentEnd="true"
                            android:layout_centerVertical="true"
                            android:src="@mipmap/ic_logo"
                            android:contentDescription="Cultivar Image"
                            android:scaleType="centerCrop"
                            android:layout_marginStart="16dp" />
                    </RelativeLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Task List Section Header -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="12dp"
                    android:paddingStart="4dp"
                    android:paddingEnd="4dp">

                    <TextView
                        android:id="@+id/taskSectionTitle"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Today's Tasks"
                        android:textColor="@color/text_primary"
                        android:textSize="18sp"
                        android:textStyle="bold" />

                    <TextView
                        android:id="@+id/taskCountText"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0 tasks"
                        android:textColor="@color/fresh_green"
                        android:textSize="14sp" />
                </LinearLayout>

                <!-- Task List -->
                <androidx.recyclerview.widget.RecyclerView
                    android:id="@+id/taskRecyclerView"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:nestedScrollingEnabled="false" />

                <!-- Buttons Section -->
                <!-- Skip and Complete Buttons (Side by Side) -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="24dp"
                    android:orientation="horizontal"
                    android:weightSum="2">

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnSkipTasks"
                        style="@style/Widget.Material3.Button.OutlinedButton"
                        android:layout_width="0dp"
                        android:layout_height="72dp"
                        android:layout_weight="1"
                        android:layout_marginEnd="8dp"
                        android:text="Skip Today's Tasks"
                        android:textColor="@color/sidebar_dark_green"
                        android:textSize="14sp"
                        android:maxLines="2"
                        android:ellipsize="end"
                        android:padding="16dp"
                        app:cornerRadius="12dp" />

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnComplete"
                        android:layout_width="0dp"
                        android:layout_height="72dp"
                        android:layout_weight="1"
                        android:layout_marginStart="8dp"
                        android:text="Mark All Tasks Complete"
                        android:textColor="@color/white"
                        android:backgroundTint="@color/fresh_green"
                        android:textSize="14sp"
                        android:maxLines="2"
                        android:ellipsize="end"
                        android:padding="16dp"
                        app:cornerRadius="12dp" />
                </LinearLayout>

                <!-- Daily Expenses Button -->
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnDailyExpenses"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Daily Expenses"
                    android:textAllCaps="false"
                    android:icon="@android:drawable/ic_menu_recent_history"
                    android:padding="16dp"
                    app:cornerRadius="12dp" />

                <!-- Monitor Plant Button -->
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnMonitorPlant"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="@string/monitor_button_label"
                    android:textAllCaps="false"
                    android:icon="@android:drawable/ic_menu_camera"
                    android:padding="16dp"
                    app:cornerRadius="12dp" />
            </LinearLayout>
        </ScrollView>
    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_detection_history.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".DetectionHistoryActivity">

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:fitsSystemWindows="true">


        <!-- Header Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"
            app:cardBackgroundColor="@color/fresh_green"
            app:cardCornerRadius="16dp"
            app:cardElevation="6dp"
            app:strokeWidth="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="24dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Detection History"
                    android:textColor="@color/white"
                    android:textSize="24sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="View your past disease and pest detections"
                    android:textColor="@color/white"
                    android:textSize="14sp"
                    android:alpha="0.9" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- History List -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/historyRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:paddingTop="8dp"
            android:paddingBottom="16dp"
            android:clipToPadding="false"
            android:scrollbars="vertical" />

        <!-- Empty State -->
        <LinearLayout
            android:id="@+id/emptyState"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="32dp"
            android:visibility="gone">

            <ImageView
                android:layout_width="120dp"
                android:layout_height="120dp"
                android:src="@mipmap/ic_logo"
                android:alpha="0.3"
                android:layout_marginBottom="24dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="No Detection History"
                android:textColor="@color/text_primary"
                android:textSize="20sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Your past detections will appear here"
                android:textColor="@color/text_secondary"
                android:textSize="14sp"
                android:gravity="center" />
        </LinearLayout>
    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_detection_results.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"
    tools:context=".DetectionResults">

    <!-- Main Content -->
    <LinearLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:fitsSystemWindows="true">

        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:fillViewport="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
                android:paddingTop="8dp">

                <!-- Detection Image with Badge -->
                <RelativeLayout
                    android:layout_width="match_parent"
                android:layout_height="wrap_content"
                    android:background="@color/white">

            <ImageView
                android:id="@+id/detectionImage"
                android:layout_width="match_parent"
                        android:layout_height="300dp"
                android:scaleType="centerCrop"
                android:src="@drawable/ic_launcher_foreground"
                android:contentDescription="Detected image"
                        android:background="@color/divider" />

                    <!-- Image Badge (White circle with number) -->
            <com.google.android.material.card.MaterialCardView
                        android:id="@+id/imageBadge"
                        android:layout_width="32dp"
                        android:layout_height="32dp"
                        android:layout_alignParentStart="true"
                        android:layout_alignParentTop="true"
                        android:layout_margin="12dp"
                        app:cardBackgroundColor="@color/white"
                        app:cardCornerRadius="16dp"
                        app:cardElevation="4dp">
                
                <TextView
                            android:id="@+id/badgeNumber"
                    android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:text="1"
                    android:textColor="@color/text_primary"
                            android:textSize="14sp"
                            android:textStyle="bold"
                            android:gravity="center" />
            </com.google.android.material.card.MaterialCardView>
                </RelativeLayout>

                <!-- Results Section -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp"
                    android:background="@color/white">

                    <!-- Header: "Identify View, 3879" and "Results" -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:layout_marginBottom="12dp">

                        <TextView
                            android:id="@+id/identifyViewText"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Identify View, 3879"
                            android:textColor="@color/text_secondary"
                            android:textSize="12sp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:id="@+id/detectionContextInfo"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textColor="@color/text_secondary"
                        android:textSize="12sp"
                        android:layout_marginBottom="4dp" />
                    
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                            android:text="Results"
                        android:textColor="@color/text_primary"
                            android:textSize="24sp"
                            android:textStyle="bold" />
                    </LinearLayout>

                    <!-- Score/ID Tag (Orange) -->
                    <com.google.android.material.card.MaterialCardView
                        android:id="@+id/scoreTag"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="20dp"
                        app:cardBackgroundColor="@color/warm_orange"
                        app:cardCornerRadius="8dp"
                        app:cardElevation="2dp">

            <TextView
                            android:id="@+id/scoreText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                            android:text="1011 - 628"
                            android:textColor="@color/white"
                            android:textSize="12sp"
                android:textStyle="bold"
                            android:paddingStart="12dp"
                            android:paddingEnd="12dp"
                            android:paddingTop="6dp"
                            android:paddingBottom="6dp" />
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Findings List with Orange Circles -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginTop="8dp">

                        <!-- Left: Orange circles with connecting line -->
                        <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:layout_marginEnd="16dp"
                            android:gravity="center_horizontal">

                            <!-- First Circle -->
                            <View
                                android:layout_width="16dp"
                                android:layout_height="16dp"
                                android:background="@drawable/orange_circle_dot" />

                            <!-- Connecting Line -->
                            <View
                                android:layout_width="2dp"
                                android:layout_height="40dp"
                                android:background="@color/warm_orange"
                                android:layout_marginTop="4dp"
                                android:layout_marginBottom="4dp" />

                            <!-- Second Circle -->
                            <View
                                android:layout_width="16dp"
                                android:layout_height="16dp"
                                android:background="@drawable/orange_circle_dot" />

                            <!-- Connecting Line -->
                            <View
                                android:layout_width="2dp"
                                android:layout_height="40dp"
                                android:background="@color/warm_orange"
                                android:layout_marginTop="4dp"
                                android:layout_marginBottom="4dp" />

                            <!-- Third Circle -->
            <View
                                android:layout_width="16dp"
                                android:layout_height="16dp"
                                android:background="@drawable/orange_circle_dot" />
                        </LinearLayout>

                        <!-- Right: Findings Text -->
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical">

                            <!-- Finding 1 -->
                            <TextView
                                android:id="@+id/finding1"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:text="Frake to co pect ancy aroco K susterma complomerity and diseases."
                                android:textColor="@color/text_primary"
                                android:textSize="14sp"
                                android:lineSpacingExtra="4dp"
                                android:layout_marginBottom="16dp" />

                            <!-- Finding 2 -->
                            <TextView
                                android:id="@+id/finding2"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:text="Rest atunaticall Foolfano Tomato fps."
                                android:textColor="@color/text_primary"
                                android:textSize="14sp"
                                android:lineSpacingExtra="4dp"
                                android:layout_marginBottom="16dp" />

                            <!-- Finding 3 -->
                            <TextView
                                android:id="@+id/finding3"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:text="Epigen imageete the roots to comctist angonithe andompers your prests."
                                android:textColor="@color/text_primary"
                                android:textSize="14sp"
                                android:lineSpacingExtra="4dp" />
                        </LinearLayout>
                    </LinearLayout>

                    <!-- Additional Details Section (Collapsible) -->
                    <com.google.android.material.card.MaterialCardView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="24dp"
                        app:cardBackgroundColor="@color/soft_cream"
                        app:cardCornerRadius="12dp"
                        app:cardElevation="2dp">

                        <LinearLayout
                android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:padding="16dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                                android:text="Detection Details"
                                android:textColor="@color/text_primary"
                                android:textSize="18sp"
                android:textStyle="bold"
                                android:layout_marginBottom="12dp" />

            <TextView
                                android:id="@+id/detectionDescription"
                                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                                android:text="Details about the detected result."
                                android:textColor="@color/text_secondary"
                                android:textSize="14sp"
                                android:lineSpacingExtra="4dp"
                                android:layout_marginBottom="12dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                                android:text="Symptoms"
                                android:textColor="@color/text_primary"
                                android:textSize="16sp"
                android:textStyle="bold"
                                android:layout_marginBottom="8dp" />

            <TextView
                                android:id="@+id/detectionSymptoms"
                                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                                android:text="Symptoms go here."
                                android:textColor="@color/text_secondary"
                                android:textSize="14sp"
                                android:lineSpacingExtra="4dp"
                                android:layout_marginBottom="16dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                                android:text="Treatment"
                                android:textColor="@color/text_primary"
                                android:textSize="16sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <TextView
                                android:id="@+id/detectionCure"
                                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                                android:text="Treatment information goes here."
                                android:textColor="@color/text_secondary"
                                android:textSize="14sp"
                                android:lineSpacingExtra="4dp" />
                        </LinearLayout>
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Accuracy Section -->
                    <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="16dp"
                        app:cardBackgroundColor="@color/white"
                        app:cardCornerRadius="12dp"
                        app:cardElevation="2dp"
                        app:strokeWidth="1dp"
                        app:strokeColor="@color/divider">

            <TextView
                android:id="@+id/detectionAccuracy"
                            android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Detection Accuracy: "
                            android:textSize="14sp"
                            android:textColor="@color/text_secondary"
                            android:padding="16dp"
                            android:lineSpacingExtra="4dp" />
                    </com.google.android.material.card.MaterialCardView>
                </LinearLayout>
        </LinearLayout>
    </ScrollView>
    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_disease_view.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/info_green"
    tools:context=".DiseaseView">

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:background="@color/info_green">

        <!-- Light Green Header -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@color/info_green"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:paddingTop="48dp"
            android:paddingBottom="16dp"
            android:fitsSystemWindows="true">

            <!-- Back Button -->
            <ImageButton
                android:id="@+id/backButton"
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:src="@android:drawable/ic_menu_revert"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:contentDescription="Back"
                app:tint="@color/white"
                android:scaleType="centerInside"
                android:padding="8dp" />

            <!-- Title -->
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="INFORMATION"
                android:textColor="@color/white"
                android:textSize="18sp"
                android:textStyle="bold"
                android:gravity="center"
                android:letterSpacing="0.1" />
        </LinearLayout>

        <!-- Main Content Card -->
        <ScrollView
            android:id="@+id/scrollView"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:fillViewport="true"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:paddingBottom="16dp">

            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                app:cardBackgroundColor="@color/white"
                app:cardCornerRadius="20dp"
                app:cardElevation="8dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <!-- Title with Orange Icon -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center_vertical"
                        android:layout_marginBottom="16dp">

                        <!-- Disease Title -->
                        <TextView
                            android:id="@+id/diseaseTitle"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Disease Title"
                            android:textColor="@color/text_primary"
                            android:textSize="24sp"
                            android:textStyle="bold" />
                    </LinearLayout>

                    <!-- Large Illustration Area -->
                    <com.google.android.material.card.MaterialCardView
                        android:layout_width="match_parent"
                        android:layout_height="280dp"
                        android:layout_marginBottom="16dp"
                        app:cardBackgroundColor="@color/soft_cream"
                        app:cardCornerRadius="12dp"
                        app:cardElevation="2dp"
                        app:strokeWidth="1dp"
                        app:strokeColor="@color/divider">

                        <ImageView
                            android:id="@+id/diseaseImage"
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:src="@mipmap/ic_logo"
                            android:scaleType="centerCrop"
                            android:contentDescription="Disease illustration"
                            android:background="@color/soft_cream" />
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Scientific Name -->
                    <TextView
                        android:id="@+id/scientificName"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text=""
                        android:textColor="@color/text_secondary"
                        android:textSize="14sp"
                        android:textStyle="italic"
                        android:layout_marginBottom="20dp"
                        android:visibility="gone" />

                    <!-- Action Buttons -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center"
                        android:layout_marginBottom="24dp">

                        <!-- Symptoms Button -->
                        <LinearLayout
                            android:id="@+id/symptomsButton"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical"
                            android:gravity="center"
                            android:padding="12dp"
                            android:background="?attr/selectableItemBackground"
                            android:clickable="true"
                            android:focusable="true">

                            <ImageView
                                android:layout_width="32dp"
                                android:layout_height="32dp"
                                android:src="@android:drawable/ic_menu_view"
                                app:tint="@color/info_green"
                                android:scaleType="centerInside" />

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Symptoms"
                                android:textColor="@color/text_primary"
                                android:textSize="12sp"
                                android:layout_marginTop="4dp" />
                        </LinearLayout>

                        <!-- Treatment Button -->
                        <LinearLayout
                            android:id="@+id/treatmentButton"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical"
                            android:gravity="center"
                            android:padding="12dp"
                            android:background="?attr/selectableItemBackground"
                            android:clickable="true"
                            android:focusable="true">

                            <ImageView
                                android:layout_width="32dp"
                                android:layout_height="32dp"
                                android:src="@android:drawable/ic_menu_camera"
                                app:tint="@color/info_green"
                                android:scaleType="centerInside" />

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Treatment"
                                android:textColor="@color/text_primary"
                                android:textSize="12sp"
                                android:layout_marginTop="4dp" />
                        </LinearLayout>

                        <!-- Prevention Button -->
                        <LinearLayout
                            android:id="@+id/preventionButton"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical"
                            android:gravity="center"
                            android:padding="12dp"
                            android:background="?attr/selectableItemBackground"
                            android:clickable="true"
                            android:focusable="true">

                            <ImageView
                                android:layout_width="32dp"
                                android:layout_height="32dp"
                                android:src="@android:drawable/ic_menu_agenda"
                                app:tint="@color/info_green"
                                android:scaleType="centerInside" />

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Prevention"
                                android:textColor="@color/text_primary"
                                android:textSize="12sp"
                                android:layout_marginTop="4dp" />
                        </LinearLayout>
                    </LinearLayout>

                    <!-- Information Section Title -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Disease Information"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="12dp" />

                    <!-- Divider -->
                    <View
                        android:layout_width="match_parent"
                        android:layout_height="1dp"
                        android:background="@color/divider"
                        android:layout_marginBottom="16dp" />

                    <!-- Description Section -->
                    <TextView
                        android:id="@+id/diseaseDescription"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Disease description goes here."
                        android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="20dp" />

                    <!-- Symptoms Section -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Symptoms"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:id="@+id/diseaseSymptoms"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Disease symptoms go here."
                        android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="20dp" />

                    <!-- Cause Section -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Cause"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:id="@+id/diseaseCause"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Disease cause goes here."
                        android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="20dp" />

                    <!-- Treatment Section -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Treatment"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:id="@+id/diseaseCure"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Disease treatment goes here."
                        android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="20dp" />

                    <!-- Prevention Section -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Prevention"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:id="@+id/diseasePrevention"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Prevention measures go here."
                        android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="20dp" />

                    <!-- Pest Information Section -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Pest Information"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="8dp" />

                    <!-- Pest Image Placeholder -->
                    <com.google.android.material.card.MaterialCardView
                        android:id="@+id/pestImageCard"
                        android:layout_width="match_parent"
                        android:layout_height="200dp"
                        android:layout_marginBottom="16dp"
                        app:cardBackgroundColor="@color/soft_cream"
                        app:cardCornerRadius="12dp"
                        app:cardElevation="2dp"
                        app:strokeWidth="1dp"
                        app:strokeColor="@color/divider"
                        android:visibility="gone">

                        <ImageView
                            android:id="@+id/pestImage"
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:src="@mipmap/ic_logo"
                            android:scaleType="centerCrop"
                            android:contentDescription="Pest illustration"
                            android:background="@color/soft_cream" />
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Pest Common Name -->
                    <TextView
                        android:id="@+id/pestCommonName"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text=""
                        android:textColor="@color/text_primary"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="8dp"
                        android:visibility="gone" />

                    <!-- Pest Scientific Name -->
                    <TextView
                        android:id="@+id/pestScientificName"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text=""
                        android:textColor="@color/text_secondary"
                        android:textSize="14sp"
                        android:textStyle="italic"
                        android:layout_marginBottom="12dp"
                        android:visibility="gone" />

                    <TextView
                        android:id="@+id/pestDescription"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Pest description goes here."
                        android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </ScrollView>
    </LinearLayout>

    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_forecast.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp"
        android:paddingTop="?attr/actionBarSize">

        <!-- Spacer for Action Bar -->
        <View
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:layout_marginBottom="60dp" />

        <!-- Location Header Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="@color/white"
            app:cardCornerRadius="16dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:padding="16dp">

                <ImageView
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:src="@android:drawable/ic_menu_mylocation"
                    app:tint="@color/fresh_green"
                    android:layout_marginEnd="12dp" />

                <TextView
                    android:id="@+id/locationTitle"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Location"
                    android:textStyle="bold"
                    android:textSize="18sp"
                    android:textColor="@color/text_primary" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Forecast Container (will be populated programmatically) -->
        <LinearLayout
            android:id="@+id/forecastContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />

    </LinearLayout>

</androidx.core.widget.NestedScrollView>





```
</details>

<details>
<summary><b>Code: activity_information_interface.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".InformationInterface">

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <!-- Spacer for Action Bar -->
        <View
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:layout_marginBottom="60dp"/>

        <!-- Header Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:layout_marginEnd="16dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="@color/warm_orange"
            app:strokeWidth="0dp">


            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="24dp"
                android:gravity="center">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="📚"
                    android:textSize="32sp"
                    android:layout_marginBottom="8dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Disease and Pest Information"
                    android:textColor="@color/white"
                    android:textSize="24sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Learn about common tomato diseases and pests"
                    android:textColor="@color/white"
                    android:textSize="14sp"
                    android:layout_marginTop="4dp"
                    android:alpha="0.9"
                    android:gravity="center" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Disease/Pest List -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/diseaseRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:clipToPadding="false"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:paddingTop="4dp"
            android:paddingBottom="16dp"
            android:scrollbars="vertical" />
    </LinearLayout>

    <!-- Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_ipm.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".IPM">

    <!-- Main Content -->
    <LinearLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:fitsSystemWindows="true">

        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:fillViewport="true">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <!-- Header Section -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="24dp"
                    app:cardBackgroundColor="@color/fresh_green"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="6dp"
                    app:strokeWidth="0dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="24dp"
                        android:gravity="center">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_gravity="center_horizontal"
                            android:text="🌱"
                            android:textSize="32sp"
                            android:layout_marginBottom="8dp" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_gravity="center_horizontal"
                            android:text="Integrated Pest Management"
                            android:textColor="@color/white"
                            android:textSize="24sp"
                            android:textStyle="bold" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_gravity="center_horizontal"
                            android:layout_marginTop="4dp"
                            android:text="Identify and manage pests and diseases effectively"
                            android:textColor="@color/white"
                            android:textSize="14sp"
                            android:alpha="0.9"
                            android:gravity="center" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Scan Card -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/scanCard"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="6dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?android:attr/selectableItemBackground">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:gravity="center_vertical"
                        android:orientation="horizontal"
                        android:padding="24dp">

                        <!-- Icon Container -->
                        <com.google.android.material.card.MaterialCardView
                            android:layout_width="67dp"
                            android:layout_height="67dp"
                            android:layout_marginEnd="16dp"
                            app:cardBackgroundColor="@color/scan_blue"
                            app:cardCornerRadius="40dp"
                            app:cardElevation="4dp">

                            <ImageView
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:padding="5dp"
                                android:scaleType="centerInside"
                                android:src="@android:drawable/ic_menu_camera"
                                app:tint="@color/white" />
                        </com.google.android.material.card.MaterialCardView>

                        <!-- Text Content -->
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical">

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginBottom="4dp"
                                android:text="@string/scan"
                                android:textColor="@color/text_primary"
                                android:textSize="20sp"
                                android:textStyle="bold" />

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Scan pests and diseases with camera"
                                android:textColor="@color/text_secondary"
                                android:textSize="14sp" />
                        </LinearLayout>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Detection History Card -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/historyCard"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="6dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?android:attr/selectableItemBackground">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center_vertical"
                        android:padding="24dp">

                        <!-- Icon Container -->
                        <com.google.android.material.card.MaterialCardView
                            android:layout_width="67dp"
                            android:layout_height="67dp"
                            android:layout_marginEnd="16dp"
                            app:cardBackgroundColor="@color/warm_orange"
                            app:cardCornerRadius="40dp"
                            app:cardElevation="4dp">

                            <ImageView
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:src="@android:drawable/ic_menu_recent_history"
                                android:scaleType="centerInside"
                                android:padding="20dp"
                                app:tint="@color/white" />
                        </com.google.android.material.card.MaterialCardView>

                        <!-- Text Content -->
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical">

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Detection History"
                                android:textColor="@color/text_primary"
                                android:textSize="20sp"
                                android:textStyle="bold"
                                android:layout_marginBottom="4dp" />

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="View your past disease and pest detections"
                                android:textColor="@color/text_secondary"
                                android:textSize="14sp" />
                        </LinearLayout>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Information Card -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/infoCard"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="6dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?android:attr/selectableItemBackground">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center_vertical"
                        android:padding="24dp">

                        <!-- Icon Container -->
                        <com.google.android.material.card.MaterialCardView
                            android:layout_width="67dp"
                            android:layout_height="67dp"
                            android:layout_marginEnd="16dp"
                            app:cardBackgroundColor="@color/info_green"
                            app:cardCornerRadius="40dp"
                            app:cardElevation="4dp">

                            <ImageView
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:src="@android:drawable/ic_menu_info_details"
                                android:scaleType="centerInside"
                                android:padding="20dp"
                                app:tint="@color/white" />
                        </com.google.android.material.card.MaterialCardView>

                        <!-- Text Content -->
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical">

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="@string/information"
                                android:textColor="@color/text_primary"
                                android:textSize="20sp"
                                android:textStyle="bold"
                                android:layout_marginBottom="4dp" />

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Browse pest and diseases information database"
                                android:textColor="@color/text_secondary"
                                android:textSize="14sp" />
                        </LinearLayout>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Quick Tips Card -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="20dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="💡 Quick Tips"
                            android:textColor="@color/text_primary"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="12dp" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="• Scan leaves regularly to catch pests early\n• Check both sides of leaves for hidden pests\n• Use information database for treatment options\n• Monitor weather conditions for pest activity"
                            android:textColor="@color/text_secondary"
                            android:textSize="14sp"
                            android:lineSpacingExtra="4dp" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>
            </LinearLayout>
        </ScrollView>
    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_login.xml</b></summary>

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"
    tools:context=".Login">

    <!-- Logo -->
    <ImageView
        android:id="@+id/logoImage"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:layout_centerHorizontal="true"
        android:layout_above="@id/tomatoText"
        android:src="@mipmap/ic_logo"
        android:contentDescription="Tomato App Logo"
        android:layout_marginTop="40dp"
        android:layout_marginBottom="12dp"/>

    <!-- Tomato App Text -->
    <TextView
        android:id="@+id/tomatoText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Tomato App"
        android:layout_above="@id/loginCard"
        android:gravity="center"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="#2E7D32"
        android:fontFamily="sans-serif"
        android:layout_marginBottom="24dp"/>

    <!-- White Card for Login Form -->
    <androidx.cardview.widget.CardView
        android:id="@+id/loginCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="24dp"
        android:layout_centerInParent="true"
        android:padding="20dp"
        app:cardCornerRadius="15dp"
        app:cardElevation="4dp"
        app:cardBackgroundColor="@color/white">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- Login Title -->
            <TextView
                android:id="@+id/loginTitle"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="15dp"
                android:layout_marginBottom="20dp"
                android:gravity="center"
                android:text="Login"
                android:textStyle="bold"
                android:textSize="22sp"
                android:textColor="@android:color/black"/>

            <!-- Email -->
            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginStart="10dp"
                android:layout_marginEnd="10dp"
                android:layout_marginBottom="12dp"
                app:boxStrokeColor="@color/fresh_green"
                app:hintTextColor="@color/fresh_green">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/email"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:hint="Email"
                    android:inputType="textEmailAddress"/>
            </com.google.android.material.textfield.TextInputLayout>

            <!-- Password -->
            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginStart="10dp"
                android:layout_marginEnd="10dp"
                android:layout_marginBottom="16dp"
                app:boxStrokeColor="@color/fresh_green"
                app:hintTextColor="@color/fresh_green">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/password"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:hint="Password"
                    android:inputType="textPassword"/>
            </com.google.android.material.textfield.TextInputLayout>

            <!-- Login Button -->
            <Button
                android:id="@+id/btn_login"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Login"
                android:textColor="@android:color/white"
                android:backgroundTint="@color/fresh_green"
                android:layout_marginBottom="12dp"
                android:layout_marginStart="10dp"
                android:layout_marginEnd="10dp"/>

            <!-- Google Sign-In Button -->
            <com.google.android.gms.common.SignInButton
                android:id="@+id/btn_google_signin"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginStart="10dp"
                android:layout_marginEnd="10dp"
                android:layout_marginBottom="12dp"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <!-- Register Text -->
    <TextView
        android:id="@+id/registerNow"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="New user? Register here"
        android:textSize="14sp"
        android:textColor="@color/text_secondary"
        android:layout_below="@id/loginCard"
        android:layout_marginTop="16dp"/>

    <!-- Progress Bar -->
    <ProgressBar
        android:id="@+id/progressBarLogin"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:visibility="gone"/>
</RelativeLayout>

```
</details>

<details>
<summary><b>Code: activity_main.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".MainActivity">

    <!-- Drawer Menu - Dark Green Sidebar -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true">

        <LinearLayout
            android:id="@+id/main"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- Notification Bell Icon - Top Right above Weather Card -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp">
            
            <ImageView
                android:id="@+id/notificationBellIcon"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:layout_alignParentEnd="true"
                android:src="@drawable/ic_bell"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:padding="8dp"
                android:clickable="true"
                android:focusable="true"
                android:contentDescription="Notifications"
                app:tint="@color/tomato_red" />
        </RelativeLayout>

        <!-- Weather Forecast Card - Compact Size -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/weatherCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="@color/white"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:padding="12dp">

                <ImageView
                    android:id="@+id/weatherIcon"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:layout_marginEnd="12dp"
                    android:src="@android:drawable/ic_menu_compass"
                    app:tint="@color/tomato_red" />

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">

                    <TextView
                        android:id="@+id/weatherCondition"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="2dp"
                        android:text="Sunny"
                        android:textColor="@color/text_primary"
                        android:textSize="14sp"
                        android:textStyle="bold" />

                    <TextView
                        android:id="@+id/weatherTemp"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="28°C"
                        android:textColor="@color/fresh_green"
                        android:textSize="16sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="end">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Current Weather"
                        android:textColor="@color/text_primary"
                        android:textSize="12sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="2dp" />

                    <TextView
                        android:id="@+id/weatherLocation"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Lopez, Quezon"
                        android:textColor="@color/text_secondary"
                        android:textSize="11sp" />
                </LinearLayout>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Saved Cultivars Section (hidden by default) -->
        <TextView
            android:id="@+id/savedCultivarsLabel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="4dp"
            android:text="Saved Cultivars:"
            android:textColor="@color/dark_gray"
            android:textSize="16sp"
            android:textStyle="bold"
            android:visibility="gone" />

        <ListView
            android:id="@+id/savedCultivarsListView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:divider="@android:color/darker_gray"
            android:dividerHeight="1dp"
            android:visibility="gone" />

        <!-- Work Program Card -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/wpsCard"
            android:layout_width="match_parent"
            android:layout_height="160dp"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="@color/white"
            app:cardCornerRadius="20dp"
            app:cardElevation="6dp"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?android:attr/selectableItemBackground">

            <RelativeLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="20dp">

                <!-- Background Gradient Effect -->
                <View
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:background="@drawable/work_program_gradient"
                    android:alpha="0.1" />

                <!-- Content -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_centerVertical="true"
                    android:gravity="center_vertical">

                    <!-- Icon Container -->
                    <com.google.android.material.card.MaterialCardView
                        android:layout_width="64dp"
                        android:layout_height="64dp"
                        android:layout_marginEnd="16dp"
                        app:cardBackgroundColor="@color/tomato_red"
                        app:cardCornerRadius="32dp"
                        app:cardElevation="4dp">

                        <ImageView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:src="@android:drawable/ic_menu_agenda"
                            android:scaleType="centerInside"
                            android:padding="16dp"
                            app:tint="@color/white" />
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Text Content -->
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@string/work_program"
                            android:textColor="@color/text_primary"
                            android:textSize="20sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="4dp" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Manage your cultivation programs"
                            android:textColor="@color/text_secondary"
                            android:textSize="13sp" />
                    </LinearLayout>
                </LinearLayout>
            </RelativeLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- IPM Card -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/ipmCard"
            android:layout_width="match_parent"
            android:layout_height="160dp"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="@color/white"
            app:cardCornerRadius="20dp"
            app:cardElevation="6dp"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?android:attr/selectableItemBackground">

            <RelativeLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="20dp">

                <!-- Background Gradient Effect -->
                <View
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:background="@drawable/ipm_gradient"
                    android:alpha="0.1" />

                <!-- Content -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_centerVertical="true"
                    android:gravity="center_vertical">

                    <!-- Icon Container -->
                    <com.google.android.material.card.MaterialCardView
                        android:layout_width="64dp"
                        android:layout_height="64dp"
                        android:layout_marginEnd="16dp"
                        app:cardBackgroundColor="@color/fresh_green"
                        app:cardCornerRadius="32dp"
                        app:cardElevation="4dp">

                        <ImageView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:src="@android:drawable/ic_menu_info_details"
                            android:scaleType="centerInside"
                            android:padding="16dp"
                            app:tint="@color/white" />
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Text Content -->
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Integrated Pest Management"
                            android:textColor="@color/text_primary"
                            android:textSize="20sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="4dp" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Identify and manage pests and diseases effectively"
                            android:textColor="@color/text_secondary"
                            android:textSize="13sp" />
                    </LinearLayout>
                </LinearLayout>
            </RelativeLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Projected Income/Expenses Card -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/projectedIncomeCard"
            android:layout_width="match_parent"
            android:layout_height="160dp"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="@color/white"
            app:cardCornerRadius="20dp"
            app:cardElevation="6dp"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?android:attr/selectableItemBackground">

            <RelativeLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="20dp">

                <!-- Background Gradient Effect -->
                <View
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:background="@drawable/income_gradient"
                    android:alpha="0.1" />

                <!-- Content -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_centerVertical="true"
                    android:gravity="center_vertical">

                    <!-- Icon Container -->
                    <com.google.android.material.card.MaterialCardView
                        android:layout_width="64dp"
                        android:layout_height="64dp"
                        android:layout_marginEnd="16dp"
                        app:cardBackgroundColor="@color/warm_orange"
                        app:cardCornerRadius="32dp"
                        app:cardElevation="4dp">

                        <ImageView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:src="@android:drawable/ic_menu_sort_by_size"
                            android:scaleType="centerInside"
                            android:padding="16dp"
                            app:tint="@color/white" />
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Text Content -->
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Projected Income/Expenses"
                            android:textColor="@color/text_primary"
                            android:textSize="20sp"
                            android:textStyle="bold"
                            android:layout_marginBottom="4dp" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Calculate your financial projections"
                            android:textColor="@color/text_secondary"
                            android:textSize="13sp" />
                    </LinearLayout>
                </LinearLayout>
            </RelativeLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Calendar Calculation Section -->

            <!-- Daily and Total Cards Row -->

            <!-- Financial Overview Section -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="@color/white"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"/>

    </LinearLayout>
    </ScrollView>

    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_notification_list.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".NotificationListActivity">

    <!-- Main Content -->
    <androidx.core.widget.NestedScrollView
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fitsSystemWindows="true"
        android:fillViewport="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- Notifications List -->
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/notificationRecyclerView"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:padding="8dp"
                android:clipToPadding="false"
                android:nestedScrollingEnabled="false" />

            <!-- Empty State -->
            <TextView
                android:id="@+id/emptyStateText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center"
                android:layout_marginTop="32dp"
                android:text="No notifications"
                android:textSize="16sp"
                android:textColor="@color/text_secondary"
                android:visibility="gone" />

            <!-- Clear All Button - At the end -->
            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnClearAll"
                android:layout_width="match_parent"
                android:layout_margin="16dp"
                android:layout_height="wrap_content"
                android:text="Clear All Notifications"
                android:textAllCaps="false"
                app:cornerRadius="8dp" />
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_plant_monitoring.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <!-- Spacer for Action Bar -->
        <View
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:layout_marginBottom="60dp"/>

        <androidx.cardview.widget.CardView
            android:id="@+id/referenceCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="18dp"
            app:cardElevation="6dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">


                <ImageView
                    android:id="@+id/referenceImage"
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:scaleType="centerCrop"
                    android:contentDescription="@string/monitor_reference_image" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <TextView
                        android:id="@+id/referencePhaseLabel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:text="@string/monitor_phase_label" />

                    <TextView
                        android:id="@+id/referenceDescription"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:textColor="@color/text_secondary"
                        android:textSize="14sp"
                        android:text="@string/monitor_reference_hint" />
                </LinearLayout>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <com.google.android.material.textfield.TextInputLayout
            style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            android:hint="@string/monitor_notes">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/inputNotes"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:minLines="3"
                android:gravity="top|start" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- Captured Image Holder -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/capturedImageCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            android:visibility="gone"
            app:cardCornerRadius="16dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="12dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Captured Image"
                        android:textColor="@color/text_primary"
                        android:textSize="16sp"
                        android:textStyle="bold" />

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnDeleteImage"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Delete"
                        android:textAllCaps="false"
                        android:textColor="@color/error"
                        style="@style/Widget.Material3.Button.TextButton"
                        android:icon="@android:drawable/ic_menu_delete"
                        app:iconTint="@color/error" />
                </LinearLayout>

                <ImageView
                    android:id="@+id/capturedImageView"
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:scaleType="centerCrop"
                    android:contentDescription="Captured image"
                    android:background="@android:color/darker_gray" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Side-by-side buttons: Scan (left) and Capture (right) -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="12dp">

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnScanDiseases"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="6dp"
                android:text="@string/monitor_scan_button"
                android:textAllCaps="false"
                android:icon="@android:drawable/ic_menu_camera" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnCapture"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="6dp"
                android:text="Capture"
                android:textAllCaps="false"
                android:icon="@android:drawable/ic_menu_camera" />
        </LinearLayout>

        <!-- Save Button -->
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnSave"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Save"
            android:textAllCaps="false"
            android:icon="@android:drawable/ic_menu_save" />

    </LinearLayout>
</androidx.core.widget.NestedScrollView>


```
</details>

<details>
<summary><b>Code: activity_profile.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".ProfileActivity">

    <!-- Main Content -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true"
        android:fitsSystemWindows="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Header Card with User Icon -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="6dp"
                app:cardBackgroundColor="@color/warm_orange"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="24dp"
                    android:gravity="center">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="👤"
                        android:textSize="48sp"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="My Profile"
                        android:textSize="24sp"
                        android:textStyle="bold"
                        android:textColor="@color/white" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Manage your personal information"
                        android:textSize="14sp"
                        android:textColor="@color/white"
                        android:layout_marginTop="4dp"
                        android:alpha="0.9" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Profile Form Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <!-- Full Name -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutFullName"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Full Name"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextFullName"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="textPersonName"
                            android:maxLines="1"
                            android:imeOptions="actionNext" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Email (Read-only) -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutEmail"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Email"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextEmail"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="textEmailAddress"
                            android:maxLines="1"
                            android:enabled="false"
                            android:focusable="false"
                            android:textColorHint="@color/text_secondary"
                            android:imeOptions="actionNext" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Address -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutAddress"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Address"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextAddress"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="textPostalAddress|textMultiLine"
                            android:minLines="2"
                            android:maxLines="4"
                            android:gravity="top|start"
                            android:imeOptions="actionDone" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Save Button -->
                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnSaveProfile"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="8dp"
                        android:text="Save Profile"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:padding="16dp"
                        app:cornerRadius="12dp"
                        app:backgroundTint="@color/fresh_green" />

                    <!-- Progress Bar -->
                    <ProgressBar
                        android:id="@+id/progressBarProfile"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center"
                        android:layout_marginTop="16dp"
                        android:visibility="gone" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>
    </ScrollView>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>


```
</details>

<details>
<summary><b>Code: activity_register.xml</b></summary>

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"
    tools:context=".Register">

    <!-- Logo -->
    <ImageView
        android:id="@+id/logoImage"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:layout_centerHorizontal="true"
        android:layout_above="@id/tomatoText"
        android:src="@mipmap/ic_logo"
        android:contentDescription="Tomato App Logo"
        android:layout_marginTop="40dp"
        android:layout_marginBottom="12dp"/>

    <!-- Tomato App Text -->
    <TextView
        android:id="@+id/tomatoText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Tomato App"
        android:layout_above="@id/registerCard"
        android:gravity="center"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="#2E7D32"
        android:fontFamily="sans-serif"
        android:layout_marginBottom="24dp"/>

    <!-- White Card with ScrollView -->
    <androidx.cardview.widget.CardView
        android:id="@+id/registerCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:layout_margin="20dp"
        app:cardBackgroundColor="@color/white"
        app:cardCornerRadius="12dp"
        app:cardElevation="4dp">

        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:fillViewport="true">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <!-- Register Title -->
                <TextView
                    android:id="@+id/registerTitle"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="20dp"
                    android:gravity="center"
                    android:text="Register"
                    android:textColor="@android:color/black"
                    android:textSize="22sp"
                    android:textStyle="bold" />

                <!-- Full Name -->
                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginVertical="6dp"
                    app:boxStrokeColor="@color/tomato_red"
                    app:hintTextColor="@color/tomato_red">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/fullName"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Full Name"
                        android:inputType="textPersonName" />
                </com.google.android.material.textfield.TextInputLayout>

                <!-- Address -->
                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginVertical="6dp"
                    app:boxStrokeColor="@color/tomato_red"
                    app:hintTextColor="@color/tomato_red">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/address"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Address"
                        android:inputType="textPostalAddress" />
                </com.google.android.material.textfield.TextInputLayout>

                <!-- Email -->
                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginVertical="6dp"
                    app:boxStrokeColor="@color/tomato_red"
                    app:hintTextColor="@color/tomato_red">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/email"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Email"
                        android:inputType="textEmailAddress" />
                </com.google.android.material.textfield.TextInputLayout>

                <!-- Password -->
                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginVertical="6dp"
                    app:boxStrokeColor="@color/tomato_red"
                    app:hintTextColor="@color/tomato_red">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/password"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Password"
                        android:inputType="textPassword" />
                </com.google.android.material.textfield.TextInputLayout>

                <!-- Register Button -->
                <Button
                    android:id="@+id/btn_register"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginVertical="10dp"
                    android:backgroundTint="@color/tomato_red"
                    android:text="Register"
                    android:textColor="@android:color/white" />
            </LinearLayout>
        </ScrollView>
    </androidx.cardview.widget.CardView>

    <!-- Login Link -->
    <TextView
        android:id="@+id/loginNow"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="Already have an account? Login here"
        android:textSize="14sp"
        android:textColor="@color/text_secondary"
        android:layout_below="@id/registerCard"
        android:layout_marginTop="16dp"/>

    <!-- Progress Bar -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:visibility="gone"/>
</RelativeLayout>

```
</details>

<details>
<summary><b>Code: activity_season_comparison.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".SeasonComparisonActivity">

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp"
        android:fitsSystemWindows="true">


        <!-- Header Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="@color/tomato_red"
            app:strokeWidth="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="📊 Season Comparison"
                    android:textColor="@color/white"
                    android:textSize="24sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="8dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Compare on-season vs off-season performance"
                    android:textColor="@color/white"
                    android:textSize="14sp"
                    android:alpha="0.9" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <ProgressBar
            android:id="@+id/comparisonProgress"
            style="?android:attr/progressBarStyle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:visibility="gone" />

        <!-- Empty State -->
        <TextView
            android:id="@+id/comparisonEmptyText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="No data available for comparison"
            android:textSize="16sp"
            android:textColor="@color/text_secondary"
            android:visibility="gone" />

        <!-- Comparison Chart -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="@color/white">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="💰 Profit per Area Comparison"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:textSize="16sp"
                    android:layout_marginBottom="12dp" />

                <com.github.mikephil.charting.charts.BarChart
                    android:id="@+id/comparisonChart"
                    android:layout_width="match_parent"
                    android:layout_height="300dp" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Comparison List -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/comparisonRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:paddingTop="4dp"
            android:clipToPadding="false"
            android:paddingBottom="8dp" />

    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>


```
</details>

<details>
<summary><b>Code: activity_settings.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".SettingsActivity">

    <!-- Main Content -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true"
        android:fitsSystemWindows="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Header Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="6dp"
                app:cardBackgroundColor="@color/warm_orange"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="24dp"
                    android:gravity="center">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="⚙️"
                        android:textSize="48sp"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Settings"
                        android:textSize="24sp"
                        android:textStyle="bold"
                        android:textColor="@color/white" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Manage your app preferences"
                        android:textSize="14sp"
                        android:textColor="@color/white"
                        android:layout_marginTop="4dp"
                        android:alpha="0.9" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- General Settings Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="General Settings"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="16dp" />

                    <!-- Language -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutLanguage"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Language"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextLanguage"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Theme -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutTheme"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Theme"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextTheme"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Default Cultivar -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutDefaultCultivar"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Default Cultivar"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextDefaultCultivar"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Display Settings Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Display Settings"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="16dp" />

                    <!-- Weather Unit -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutWeatherUnit"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Weather Unit"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextWeatherUnit"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Measurement Unit -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutMeasurementUnit"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Measurement Unit"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextMeasurementUnit"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Date Format -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutDateFormat"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Date Format"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextDateFormat"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Notification Settings Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/profile_notifications_title"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="12dp" />

                    <com.google.android.material.materialswitch.MaterialSwitch
                        android:id="@+id/switchTaskNotifications"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="@string/notification_switch_tasks"
                        android:textSize="14sp"
                        android:layout_marginBottom="12dp" />

                    <com.google.android.material.materialswitch.MaterialSwitch
                        android:id="@+id/switchMonitoringNotifications"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="@string/notification_switch_monitoring"
                        android:textSize="14sp"
                        android:layout_marginBottom="12dp" />

                    <com.google.android.material.materialswitch.MaterialSwitch
                        android:id="@+id/switchGeneralNotifications"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="@string/notification_switch_general"
                        android:textSize="14sp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Advanced Settings Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="32dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Advanced Settings"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="16dp" />

                    <!-- Notification Sound -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutNotificationSound"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Notification Sound"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextNotificationSound"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Notification Time -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutNotificationTime"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Notification Time"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextNotificationTime"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Quiet Hours Switch -->
                    <com.google.android.material.materialswitch.MaterialSwitch
                        android:id="@+id/switchQuietHours"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Enable Quiet Hours"
                        android:textSize="14sp"
                        android:layout_marginBottom="16dp" />

                    <!-- Quiet Hours Start Time -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutQuietHoursStart"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:hint="Quiet Hours Start"
                        android:visibility="gone"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextQuietHoursStart"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Quiet Hours End Time -->
                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutQuietHoursEnd"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Quiet Hours End"
                        android:visibility="gone"
                        app:hintEnabled="true"
                        app:boxStrokeWidth="2dp"
                        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editTextQuietHoursEnd"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:focusable="false"
                            android:clickable="true"
                            android:maxLines="1" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <!-- Show Tutorial Button -->
                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnShowTutorial"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Show Tutorial"
                        android:textAllCaps="false"
                        android:icon="@android:drawable/ic_menu_help"
                        android:layout_marginTop="8dp"
                        style="@style/Widget.MaterialComponents.Button.OutlinedButton" />

                    <!-- Export Data Button -->
                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnExportData"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Export Data"
                        android:textAllCaps="false"
                        android:icon="@android:drawable/ic_menu_share"
                        android:layout_marginTop="8dp"
                        style="@style/Widget.MaterialComponents.Button.OutlinedButton" />

                    <!-- Import Data Button -->
                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnImportData"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Import Data"
                        android:textAllCaps="false"
                        android:icon="@android:drawable/ic_menu_upload"
                        android:layout_marginTop="8dp"
                        style="@style/Widget.MaterialComponents.Button.OutlinedButton" />

                    <!-- Clear Local Data Button -->
                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnClearLocalData"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Clear Local Data"
                        android:textAllCaps="false"
                        android:icon="@android:drawable/ic_menu_delete"
                        android:layout_marginTop="8dp"
                        style="@style/Widget.MaterialComponents.Button.OutlinedButton" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

        </LinearLayout>
    </ScrollView>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>


```
</details>

<details>
<summary><b>Code: activity_simple_capture.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/black"
    tools:context=".SimpleCaptureActivity">

    <!-- Camera Preview -->
    <androidx.camera.view.PreviewView
        android:id="@+id/previewView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="fitCenter"
        tools:ignore="MissingConstraints"
        tools:background="@android:color/black" />

    <!-- Top Header Bar (Translucent) -->
    <LinearLayout
        android:id="@+id/headerBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:paddingTop="48dp"
        android:paddingBottom="16dp"
        android:background="@drawable/header_gradient_overlay"
        android:fitsSystemWindows="true">

        <!-- Back Arrow -->
        <ImageButton
            android:id="@+id/backButton"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:src="@android:drawable/ic_menu_revert"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="Back"
            app:tint="@color/white"
            android:scaleType="centerInside"
            android:padding="8dp" />

        <!-- Title -->
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Capture"
            android:textColor="@color/white"
            android:textSize="18sp"
            android:textStyle="bold"
            android:gravity="center"
            android:maxLines="1"
            android:ellipsize="end" />
    </LinearLayout>

    <!-- Bottom Navigation Bar (Semi-transparent dark) -->
    <LinearLayout
        android:id="@+id/bottomNavBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:orientation="horizontal"
        android:gravity="center"
        android:paddingStart="24dp"
        android:paddingEnd="24dp"
        android:paddingTop="20dp"
        android:paddingBottom="32dp"
        android:background="@drawable/bottom_nav_gradient"
        android:fitsSystemWindows="true">

        <!-- Large Orange Shutter Button -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/captureBtn"
            android:layout_width="80dp"
            android:layout_height="80dp"
            app:cardBackgroundColor="@color/warm_orange"
            app:cardCornerRadius="40dp"
            app:cardElevation="8dp"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?android:attr/selectableItemBackground">

            <!-- Inner darker orange circle -->
            <View
                android:layout_width="64dp"
                android:layout_height="64dp"
                android:layout_gravity="center"
                android:background="@drawable/capture_button_inner" />
        </com.google.android.material.card.MaterialCardView>
    </LinearLayout>

</RelativeLayout>


```
</details>

<details>
<summary><b>Code: activity_user_manual.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".UserManualActivity">

    <!-- Main Content -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true"
        android:fitsSystemWindows="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Header Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="6dp"
                app:cardBackgroundColor="@color/fresh_green"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="24dp"
                    android:gravity="center">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="📖"
                        android:textSize="48sp"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="User Manual"
                        android:textColor="@color/white"
                        android:textSize="24sp"
                        android:textStyle="bold" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Learn how to use TomatoApp"
                        android:textColor="@color/white"
                        android:textSize="14sp"
                        android:layout_marginTop="4dp"
                        android:alpha="0.9" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Section 1: Getting Started -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="1. Getting Started"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Welcome to TomatoApp! This guide will help you get started with managing your tomato farming operations."
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Registration"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• Create an account using your email address\n• Fill in your profile information (name, address)\n• Once registered, you can start creating work programs"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Navigation"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• Use the drawer menu (☰) to navigate between sections\n• Home: View dashboard and weather information\n• Work Programs: Manage your farming programs\n• Settings: Customize app preferences"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Section 2: Features Overview -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="2. Features Overview"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="TomatoApp provides comprehensive tools for managing your tomato farming operations:"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• Work Programs: Plan and track your farming activities\n• Disease Detection: Identify pests and diseases using AI\n• Financial Calculator: Project income and expenses\n• Analytics: Analyze performance across seasons\n• Daily Tasks: Manage routine farming activities\n• Weather Forecast: Stay informed about weather conditions"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Section 3: Work Programs -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="3. Work Programs"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Creating a Work Program"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="1. Go to Work Programs from the drawer menu\n2. Tap the + button to create a new program\n3. Select your cultivar from the dropdown\n4. Enter your land area (in hectares)\n5. Choose your program start date\n6. Tap Submit to create the program"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Daily Tasks"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• View tasks for each day on the calendar\n• Mark tasks as complete when finished\n• Skip tasks if not applicable\n• Tasks are organized by growth phases"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Phase Tracking"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="The app divides your program into 5 phases:\n• Phase 1: Nursery &amp; Land Prep (30 days)\n• Phase 2: Transplant &amp; Establishment\n• Phase 3: Vegetative Growth\n• Phase 4: Flowering &amp; Fruit Set\n• Phase 5: Harvest"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Section 4: Disease Detection -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="4. Disease Detection"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Using the Camera Scanner"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="1. Go to IPM section from the drawer\n2. Tap Scan for Diseases\n3. Allow camera permissions if prompted\n4. Point camera at tomato leaves or fruits\n5. Tap capture button to scan\n6. View detection results with accuracy percentage"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Detection Results"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• View detected disease or pest name\n• Read symptoms and description\n• Learn about causes and prevention\n• Access treatment recommendations\n• Save detection to history for tracking"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Information Interface"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Browse comprehensive information about common tomato diseases and pests. Tap on any disease to view detailed information including symptoms, causes, and treatment methods."
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Section 5: Financial Calculations -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="5. Financial Calculations"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Income Projection"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="1. Enter land area (hectares)\n2. Input Average Weight per Fruit (AWF) in grams\n3. Enter Average Fruits per Plant (AFP)\n4. Set market value per kilogram\n5. The app calculates projected income automatically"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Expense Breakdown"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• Fertilizer: Auto-calculated based on NP and selected types\n• Manpower: Enter labor costs\n• Pesticide: Suggested cost based on cultivar and season\n• Seedlings: Enter seedling costs\n• Other Expenses: Add any additional costs"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Adjusted Projections"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="The app adjusts projections based on task completion rates. Higher completion rates result in more accurate income projections. Focus on completing critical phases (Flowering and Harvest) for best results."
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Section 6: Analytics &amp; Reports -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/white">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="6. Analytics &amp; Reports"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Cultivar Analytics"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="View performance metrics for each cultivar:\n• Total area planted\n• Total profit and profit per hectare\n• Average completion rate\n• Adjusted profit based on task completion\n• Compare performance across cultivars"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Season Comparison"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Compare on-season vs off-season performance to understand which planting periods yield better results for your farm."
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginBottom="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Export Reports"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• Export to PDF: Generate detailed reports with charts\n• Export to CSV: Export data for analysis in spreadsheet applications\n• Select specific work programs to include in reports\n• Reports include financial data, completion rates, and yield information"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

        </LinearLayout>
    </ScrollView>

    <!-- Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>


```
</details>

<details>
<summary><b>Code: activity_work_program_selection.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".WorkProgramSelection">

    <!-- Main Content -->
    <FrameLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical">

            <!-- Spacer for Action Bar -->
            <View
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                android:layout_marginBottom="60dp"/>

            <!-- Header Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginStart="16dp"
                android:layout_marginEnd="16dp"
                android:layout_marginTop="16dp"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="6dp"
                app:cardBackgroundColor="@color/fresh_green"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:padding="24dp">

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="🌱"
                            android:textSize="32sp"
                            android:layout_marginBottom="8dp" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Work Programs"
                            android:textColor="@color/white"
                            android:textSize="24sp"
                            android:textStyle="bold" />

                        <TextView
                            android:id="@+id/programCountText"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:text="0 programs"
                            android:textColor="@color/white"
                            android:textSize="14sp"
                            android:alpha="0.9" />
                    </LinearLayout>

                    <ImageView
                        android:id="@+id/headerMenuButton"
                        android:layout_width="32dp"
                        android:layout_height="32dp"
                        android:src="@android:drawable/ic_menu_more"
                        android:clickable="true"
                        android:focusable="true"
                        android:background="?attr/selectableItemBackgroundBorderless"
                        android:contentDescription="Sort options"
                        android:padding="4dp"
                        app:tint="@color/white" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- RecyclerView for Cards -->
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/workProgramRecycler"
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:clipToPadding="false"
                android:paddingStart="12dp"
                android:paddingEnd="12dp"
                android:paddingTop="4dp"
                android:paddingBottom="12dp"
                android:scrollbars="vertical" />

            <!-- Empty State (initially hidden) -->
            <LinearLayout
                android:id="@+id/emptyState"
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center"
                android:padding="48dp"
                android:visibility="gone">

                <com.google.android.material.card.MaterialCardView
                    android:layout_width="120dp"
                    android:layout_height="120dp"
                    android:layout_marginBottom="24dp"
                    app:cardCornerRadius="60dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="@color/soft_cream">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:gravity="center">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="🌱"
                            android:textSize="64sp"
                            android:alpha="0.4" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="No Work Programs Yet"
                    android:textColor="@color/text_primary"
                    android:textSize="20sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="8dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Create your first work program\nto get started with your farming journey"
                    android:textColor="@color/text_secondary"
                    android:textSize="14sp"
                    android:gravity="center"
                    android:lineSpacingExtra="4dp" />
            </LinearLayout>
        </LinearLayout>

        <!-- Floating Action Button -->
        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/addButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="end|bottom"
            android:layout_margin="24dp"
            android:contentDescription="Add New Work Program"
            app:srcCompat="@android:drawable/ic_input_add"
            app:tint="@android:color/white"
            app:backgroundTint="@color/tomato_red"
            app:elevation="8dp"
            app:pressedTranslationZ="12dp"
            app:borderWidth="0dp" />
    </FrameLayout>

    <!-- Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: activity_workprogram.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/soft_cream"
    tools:context=".Workprogram">

    <!-- Main Content -->
    <LinearLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:fitsSystemWindows="true">


        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:fillViewport="true">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <!-- Header Card with Cultivar Info -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/headerCard"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/fresh_green"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="6dp"
                    app:strokeWidth="0dp"
                    android:visibility="gone">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:padding="24dp">

                    <!-- Cultivar Image -->
                    <ImageView
                        android:id="@+id/cultivarImage"
                        android:layout_width="64dp"
                        android:layout_height="64dp"
                        android:layout_marginEnd="16dp"
                        android:src="@mipmap/ic_logo"
                        android:contentDescription="Cultivar Image"
                        android:scaleType="centerCrop"
                        android:background="@drawable/circular_image_border"
                        android:clipToOutline="true" />

                    <!-- Cultivar Info -->
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:id="@+id/cultivarNameText"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Cultivar Name"
                            android:textColor="@color/white"
                            android:textSize="24sp"
                            android:textStyle="bold" />

                        <TextView
                            android:id="@+id/startDateText"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:text="Start Date: YYYY-MM-DD"
                            android:textColor="@color/white"
                            android:textSize="14sp"
                            android:alpha="0.9" />
                    </LinearLayout>
                </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Calendar Card -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp"
                    app:cardBackgroundColor="@color/white"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">

                        <!-- Calendar Title with Integrated Info (for new programs) -->
                        <LinearLayout
                            android:id="@+id/calendarHeader"
                            android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                            android:gravity="center_vertical"
                            android:layout_marginBottom="12dp"
                            android:visibility="gone">

                            <ImageView
                                android:id="@+id/headerCultivarImage"
                                android:layout_width="48dp"
                                android:layout_height="48dp"
                                android:layout_marginEnd="12dp"
                                android:src="@mipmap/ic_logo"
                                android:contentDescription="Cultivar"
                                android:scaleType="centerCrop"
                                android:background="@drawable/circular_image_border"
                                android:clipToOutline="true" />

                            <LinearLayout
                                android:layout_width="0dp"
                                android:layout_height="wrap_content"
                                android:layout_weight="1"
                                android:orientation="vertical">

                                <TextView
                                    android:id="@+id/headerCultivarName"
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="Cultivar Name"
                                    android:textColor="@color/text_primary"
                                    android:textSize="18sp"
                                    android:textStyle="bold" />

                        <TextView
                                    android:id="@+id/headerStartDate"
                            android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="2dp"
                                    android:text="Start: YYYY-MM-DD"
                                    android:textColor="@color/text_secondary"
                                    android:textSize="12sp" />
                            </LinearLayout>
                        </LinearLayout>

                        <TextView
                            android:id="@+id/taskWarningBanner"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:padding="12dp"
                            android:textColor="@color/text_primary"
                            android:textSize="14sp"
                            android:background="@drawable/warning_banner_background"
                            android:visibility="gone"
                            android:text="Stay on track by logging daily tasks."
                            android:layout_marginBottom="12dp" />

                        <!-- Calendar View -->
                        <com.prolificinteractive.materialcalendarview.MaterialCalendarView
                            android:id="@+id/CalendarView"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            app:mcv_showOtherDates="all"
                            android:layout_marginBottom="16dp" />

                        <!-- Integrated Legends (Horizontal Compact Layout) -->
                        <LinearLayout
                            android:id="@+id/legendContainer"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:paddingTop="8dp"
                            android:paddingStart="4dp"
                            android:paddingEnd="4dp"
                            android:paddingBottom="4dp"
                            android:background="@drawable/legend_background">

                            <!-- First Row: Phases 1-3 -->
                            <LinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:orientation="horizontal"
                                android:layout_marginBottom="8dp">

                                <LinearLayout
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:orientation="horizontal"
                                    android:gravity="center_vertical">

                                    <View
                                        android:layout_width="16dp"
                                        android:layout_height="16dp"
                                        android:background="@drawable/phase1"
                                        android:layout_marginEnd="6dp" />
                                    <TextView
                                        android:layout_width="wrap_content"
                                        android:layout_height="wrap_content"
                                        android:text="Phase 1: Land &amp; Soil Preparation"
                                        android:textSize="11sp"
                                        android:textColor="@color/text_secondary" />
                    </LinearLayout>

                    <LinearLayout
                                    android:layout_width="0dp"
                        android:layout_height="wrap_content"
                                    android:layout_weight="1"
                        android:orientation="horizontal"
                                    android:gravity="center_vertical">

                        <View
                                        android:layout_width="16dp"
                                        android:layout_height="16dp"
                                        android:background="@drawable/phase2"
                                        android:layout_marginEnd="6dp" />
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                                        android:text="Phase 2: Vegetative"
                                        android:textSize="11sp"
                                        android:textColor="@color/text_secondary" />
                    </LinearLayout>

                    <LinearLayout
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:orientation="horizontal"
                                    android:gravity="center_vertical">

                                    <View
                                        android:layout_width="16dp"
                                        android:layout_height="16dp"
                                        android:background="@drawable/phase3"
                                        android:layout_marginEnd="6dp" />
                                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                                        android:text="Phase 3: Flowering"
                                        android:textSize="11sp"
                                        android:textColor="@color/text_secondary" />
                                </LinearLayout>
                            </LinearLayout>

                            <!-- Second Row: Phases 4-5 and Status -->
                            <LinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:orientation="horizontal">

                                <LinearLayout
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                        android:orientation="horizontal"
                                    android:gravity="center_vertical">

                        <View
                                        android:layout_width="16dp"
                                        android:layout_height="16dp"
                                        android:background="@drawable/phase4"
                                        android:layout_marginEnd="6dp" />
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                                        android:text="Phase 4: Maturity"
                                        android:textSize="11sp"
                                        android:textColor="@color/text_secondary" />
                    </LinearLayout>

                    <LinearLayout
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:orientation="horizontal"
                                    android:gravity="center_vertical">

                                    <View
                                        android:layout_width="16dp"
                                        android:layout_height="16dp"
                                        android:background="@drawable/phase5"
                                        android:layout_marginEnd="6dp" />
                                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                                        android:text="Phase 5: Post-harvest"
                                        android:textSize="11sp"
                                        android:textColor="@color/text_secondary" />
                                </LinearLayout>

                                <!-- Status Indicators -->
                                <LinearLayout
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                        android:orientation="horizontal"
                                    android:gravity="center_vertical">

                        <View
                                        android:layout_width="8dp"
                                        android:layout_height="8dp"
                                        android:background="@drawable/circle_green"
                                        android:layout_marginEnd="6dp" />
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                                        android:text="Done"
                                        android:textSize="11sp"
                                        android:textColor="@color/text_secondary" />
                    </LinearLayout>

                    <LinearLayout
                                    android:layout_width="0dp"
                        android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:orientation="horizontal"
                                    android:gravity="center_vertical">

                        <View
                                        android:layout_width="8dp"
                                        android:layout_height="8dp"
                                        android:background="@drawable/circle_missed"
                                        android:layout_marginEnd="6dp" />
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                                        android:text="Missed"
                                        android:textSize="11sp"
                                        android:textColor="@color/text_secondary" />
                                </LinearLayout>
                            </LinearLayout>
                        </LinearLayout>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Current Expenses Button -->
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnCurrentExpenses"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="16dp"
                    android:text="Current Expenses"
                    android:textColor="@color/white"
                    android:backgroundTint="@color/warm_orange"
                    android:textSize="16sp"
                    android:padding="16dp"
                    android:icon="@android:drawable/ic_menu_sort_by_size"
                    app:cornerRadius="12dp"
                    android:visibility="gone" />
            </LinearLayout>
        </ScrollView>

        <!-- Form Card (for new programs) -->
        <androidx.cardview.widget.CardView
            android:id="@+id/floatingFormCard"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_margin="24dp"
            android:visibility="gone"
            android:backgroundTint="@color/white"
            app:cardElevation="16dp"
            app:cardCornerRadius="20dp">

            <ScrollView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:fillViewport="true">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Land Area"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="4dp"/>

                    <EditText
                        android:id="@+id/landAreaInput"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="12dp"
                        android:hint="Enter Land Area (hectare)"
                        android:inputType="number"
                        android:minHeight="48dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Select Cultivar"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:layout_marginBottom="4dp"/>

                    <Spinner
                        android:id="@+id/cultivarSpinner"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:minHeight="48dp"
                        android:spinnerMode="dropdown"
                        android:layout_marginBottom="12dp" />

                    <DatePicker
                        android:id="@+id/startDatePicker"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:calendarViewShown="true"
                        android:layout_marginBottom="16dp" />

                    <Button
                        android:id="@+id/btnSelectCultivar"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Save Work Program"
                        android:backgroundTint="@color/tomato_red"
                        android:textColor="@color/white" />
                </LinearLayout>
            </ScrollView>
        </androidx.cardview.widget.CardView>
    </LinearLayout>

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="280dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu"
        app:headerLayout="@layout/nav_drawer_sidebar"
        app:itemIconTint="@color/white"
        app:itemTextColor="@color/white"
        android:background="@color/sidebar_dark_green" />

</androidx.drawerlayout.widget.DrawerLayout>

```
</details>

<details>
<summary><b>Code: dialog_add_calculation.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">

    <!-- Cultivar Spinner -->
    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/cultivarInputLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Cultivar"
        app:boxStrokeWidthFocused="2dp"
        android:layout_marginBottom="16dp"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox.ExposedDropdownMenu">

        <AutoCompleteTextView
            android:id="@+id/cultivarSpinner"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="none"
            android:focusable="false"
            android:clickable="true" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/hectareInputLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Hectare"
        app:boxStrokeWidthFocused="2dp"
        app:suffixText="ha"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/hectareEditText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="numberDecimal"
            android:maxLines="1" />
    </com.google.android.material.textfield.TextInputLayout>

</LinearLayout>


```
</details>

<details>
<summary><b>Code: dialog_detection_type.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@drawable/dialog_background"
    android:padding="@dimen/spacing_large">

    <!-- Header Section -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:layout_marginBottom="@dimen/spacing_large"
        android:paddingBottom="@dimen/spacing_medium"
        android:background="?attr/selectableItemBackgroundBorderless">

        <ImageView
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:src="@drawable/ic_tomato_logo_green"
            android:contentDescription="App Icon"
            android:layout_marginEnd="@dimen/spacing_medium" />

        <TextView
            android:id="@+id/txtTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select Detection Type"
            android:textSize="@dimen/text_size_title"
            android:textStyle="bold"
            android:textColor="@color/text_primary" />

    </LinearLayout>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Choose what you want to detect"
        android:textSize="@dimen/text_size_caption"
        android:textColor="@color/text_secondary"
        android:layout_marginBottom="@dimen/spacing_large"
        android:gravity="center" />

    <!-- Fruit Option -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/btnFruit"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="@dimen/spacing_medium"
        app:cardCornerRadius="@dimen/corner_radius_medium"
        app:cardElevation="@dimen/elevation_card"
        app:cardBackgroundColor="@android:color/transparent"
        android:clickable="true"
        android:focusable="true"
        android:foreground="@drawable/ripple_detection_type"
        android:background="@drawable/card_fruit_background">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="@dimen/spacing_large"
            android:gravity="center_vertical"
            android:minHeight="80dp">

            <!-- Icon Container -->
            <FrameLayout
                android:layout_width="64dp"
                android:layout_height="64dp"
                android:layout_marginEnd="@dimen/spacing_medium"
                android:background="@drawable/circle_icon_background"
                android:gravity="center"
                android:padding="8dp">

                <ImageView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center"
                    android:src="@drawable/ic_fruit_detection"
                    android:contentDescription="Fruit Detection"
                    android:scaleType="fitCenter"
                    android:adjustViewBounds="true" />
            </FrameLayout>

            <!-- Text Content -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Fruit"
                    android:textSize="@dimen/text_size_body"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:layout_marginBottom="4dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Detect fruit quality and ripeness"
                    android:textSize="@dimen/text_size_caption"
                    android:textColor="@color/text_secondary" />
            </LinearLayout>

            <!-- Arrow Icon -->
            <ImageView
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@android:drawable/ic_menu_more"
                android:contentDescription="Select"
                app:tint="@color/warm_orange"
                android:rotation="90" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <!-- Leaves Option -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/btnLeaves"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="@dimen/spacing_medium"
        app:cardCornerRadius="@dimen/corner_radius_medium"
        app:cardElevation="@dimen/elevation_card"
        app:cardBackgroundColor="@android:color/transparent"
        android:clickable="true"
        android:focusable="true"
        android:foreground="@drawable/ripple_detection_type"
        android:background="@drawable/card_leaves_background">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="@dimen/spacing_large"
            android:gravity="center_vertical"
            android:minHeight="80dp">

            <!-- Icon Container -->
            <FrameLayout
                android:layout_width="64dp"
                android:layout_height="64dp"
                android:layout_marginEnd="@dimen/spacing_medium"
                android:background="@drawable/circle_icon_background"
                android:gravity="center"
                android:padding="8dp">

                <ImageView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center"
                    android:src="@drawable/ic_leaves_detection"
                    android:contentDescription="Leaves Detection"
                    android:scaleType="fitCenter"
                    android:adjustViewBounds="true" />
            </FrameLayout>

            <!-- Text Content -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Leaves"
                    android:textSize="@dimen/text_size_body"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:layout_marginBottom="4dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Detect leaf diseases and health"
                    android:textSize="@dimen/text_size_caption"
                    android:textColor="@color/text_secondary" />
            </LinearLayout>

            <!-- Arrow Icon -->
            <ImageView
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@android:drawable/ic_menu_more"
                android:contentDescription="Select"
                app:tint="@color/fresh_green"
                android:rotation="90" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <!-- Pest Option -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/btnPest"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="@dimen/corner_radius_medium"
        app:cardElevation="@dimen/elevation_card"
        app:cardBackgroundColor="@android:color/transparent"
        android:clickable="true"
        android:focusable="true"
        android:foreground="@drawable/ripple_detection_type"
        android:background="@drawable/card_pest_background">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="@dimen/spacing_large"
            android:gravity="center_vertical"
            android:minHeight="80dp">

            <!-- Icon Container -->
            <FrameLayout
                android:layout_width="64dp"
                android:layout_height="64dp"
                android:layout_marginEnd="@dimen/spacing_medium"
                android:background="@drawable/circle_icon_background"
                android:gravity="center"
                android:padding="8dp">

                <ImageView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center"
                    android:src="@drawable/ic_pest_detection"
                    android:contentDescription="Pest Detection"
                    android:scaleType="fitCenter"
                    android:adjustViewBounds="true" />
            </FrameLayout>

            <!-- Text Content -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Pest"
                    android:textSize="@dimen/text_size_body"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:layout_marginBottom="4dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Detect pests and insects"
                    android:textSize="@dimen/text_size_caption"
                    android:textColor="@color/text_secondary" />
            </LinearLayout>

            <!-- Arrow Icon -->
            <ImageView
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@android:drawable/ic_menu_more"
                android:contentDescription="Select"
                app:tint="@color/primary"
                android:rotation="90" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

</LinearLayout>


```
</details>

<details>
<summary><b>Code: dialog_select_work_programs.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Select Work Programs to Export"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/text_primary"
        android:layout_marginBottom="16dp" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/workProgramsRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        android:scrollbars="vertical" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="16dp"
        android:gravity="end">

        <Button
            android:id="@+id/btnSelectAll"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Select All"
            android:layout_marginEnd="8dp" />

        <Button
            android:id="@+id/btnDeselectAll"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Deselect All" />

    </LinearLayout>

</LinearLayout>






```
</details>

<details>
<summary><b>Code: dialog_tutorial_step.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="32dp"
    android:minHeight="500dp"
    android:background="@color/white">

    <!-- Title -->
    <TextView
        android:id="@+id/txtTutorialTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Welcome to TomatoApp!"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="@color/text_primary"
        android:layout_marginBottom="12dp" />

    <!-- Progress Indicator -->
    <TextView
        android:id="@+id/txtTutorialProgress"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Step 1 of 8"
        android:textSize="14sp"
        android:textColor="@color/text_secondary"
        android:layout_marginBottom="20dp" />

    <!-- Content -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true"
        android:minHeight="300dp"
        android:maxHeight="500dp">

        <TextView
            android:id="@+id/txtTutorialContent"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Tutorial content will appear here."
            android:textSize="16sp"
            android:textColor="@color/text_secondary"
            android:lineSpacingExtra="6dp"
            android:padding="8dp" />
    </ScrollView>

    <!-- Button Layout -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="24dp"
        android:gravity="end">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnTutorialPrevious"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Previous"
            android:textAllCaps="false"
            style="@style/Widget.MaterialComponents.Button.TextButton"
            android:layout_marginEnd="8dp"
            android:visibility="gone" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnTutorialSkip"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Skip"
            android:textAllCaps="false"
            style="@style/Widget.MaterialComponents.Button.TextButton"
            android:layout_marginEnd="8dp" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnTutorialNext"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Next"
            android:textAllCaps="false"
            style="@style/Widget.MaterialComponents.Button" />
    </LinearLayout>

</LinearLayout>


```
</details>

<details>
<summary><b>Code: dialog_user_agreement.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:id="@+id/txtTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/terms_title"
        android:textSize="20sp"
        android:textStyle="bold"
        android:textColor="@color/text_primary"
        android:layout_marginBottom="16dp" />

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true"
        android:maxHeight="400dp">

        <TextView
            android:id="@+id/txtAgreement"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            android:lineSpacingExtra="4dp"
            android:text="@string/terms_full_text"
            android:textColor="@color/text_primary"
            android:textSize="14sp" />
    </ScrollView>

    <com.google.android.material.checkbox.MaterialCheckBox
        android:id="@+id/chkAgree"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/terms_agree_checkbox"
        android:textSize="14sp"
        android:paddingTop="8dp"
        android:paddingBottom="8dp" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnAccept"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/terms_accept_button"
        android:enabled="false"
        android:layout_marginTop="16dp"
        app:backgroundTint="@color/tomato_red" />

</LinearLayout>

```
</details>

<details>
<summary><b>Code: dialog_work_program_selection.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@drawable/dialog_background"
    android:padding="@dimen/spacing_large">

    <!-- Header Section -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:layout_marginBottom="@dimen/spacing_medium"
        android:paddingBottom="@dimen/spacing_medium">

        <ImageView
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:src="@drawable/ic_tomato_logo_green"
            android:contentDescription="App Icon"
            android:layout_marginEnd="@dimen/spacing_medium" />

        <TextView
            android:id="@+id/txtTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select Work Program"
            android:textSize="@dimen/text_size_title"
            android:textStyle="bold"
            android:textColor="@color/text_primary" />

    </LinearLayout>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Link this detection to a work program"
        android:textSize="@dimen/text_size_caption"
        android:textColor="@color/text_secondary"
        android:layout_marginBottom="@dimen/spacing_large"
        android:gravity="center" />

    <!-- Scrollable container for work programs -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:maxHeight="400dp"
        android:fillViewport="true">

        <LinearLayout
            android:id="@+id/programsContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />

    </ScrollView>

    <!-- Continue without linking option -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/btnContinueWithout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="@dimen/spacing_medium"
        app:cardCornerRadius="@dimen/corner_radius_medium"
        app:cardElevation="@dimen/elevation_card"
        app:cardBackgroundColor="@android:color/transparent"
        android:clickable="true"
        android:focusable="true"
        android:foreground="@drawable/ripple_detection_type"
        android:background="@drawable/card_pest_background">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="@dimen/spacing_medium"
            android:gravity="center_vertical"
            android:minHeight="60dp">

            <ImageView
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@android:drawable/ic_menu_close_clear_cancel"
                android:contentDescription="Continue without linking"
                app:tint="@color/text_secondary"
                android:layout_marginEnd="@dimen/spacing_medium" />

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Continue without linking to a program"
                android:textSize="@dimen/text_size_body"
                android:textColor="@color/text_primary" />

        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

</LinearLayout>


```
</details>

<details>
<summary><b>Code: expense_table_header.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="8dp"
    android:background="@drawable/legend_background"
    android:minWidth="1000dp">

    <TextView
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="Date"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp" />

    <TextView
        android:layout_width="120dp"
        android:layout_height="wrap_content"
        android:text="Activity"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp" />

    <TextView
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="Category"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp" />

    <TextView
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="Labor Cost"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp"
        android:gravity="end" />

    <TextView
        android:layout_width="80dp"
        android:layout_height="wrap_content"
        android:text="Workers"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp"
        android:gravity="center" />

    <TextView
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="Material Cost"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp"
        android:gravity="end" />

    <TextView
        android:layout_width="110dp"
        android:layout_height="wrap_content"
        android:text="Equipment Cost"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp"
        android:gravity="end" />

    <TextView
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="Misc Cost"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp"
        android:gravity="end" />

    <TextView
        android:layout_width="150dp"
        android:layout_height="wrap_content"
        android:text="Notes"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp" />

    <TextView
        android:layout_width="120dp"
        android:layout_height="wrap_content"
        android:text="Total Cost"
        android:textColor="@color/fresh_green"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp"
        android:gravity="end" />
</LinearLayout>

```
</details>

<details>
<summary><b>Code: item_cultivar.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cultivarCard"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="6dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp"
    app:strokeWidth="0dp"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?attr/selectableItemBackground">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="18dp">

        <!-- Header Row with Icon and Date -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="12dp">

            <ImageView
                android:id="@+id/cultivarImage"
                android:layout_width="56dp"
                android:layout_height="56dp"
                android:scaleType="centerCrop"
                android:src="@mipmap/ic_logo"
                android:layout_marginEnd="14dp" />

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/cultivarName"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Cultivar Name"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:maxLines="2"
                    android:ellipsize="end"
                    android:lineSpacingExtra="2dp" />

                <TextView
                    android:id="@+id/cultivarDate"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Started: 2025-09-23"
                    android:textSize="13sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="6dp" />
            </LinearLayout>
        </LinearLayout>

        <!-- Footer with Status Indicator -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginTop="4dp">

            <View
                android:id="@+id/statusIndicator"
                android:layout_width="10dp"
                android:layout_height="10dp"
                android:background="@drawable/circle_green"
                android:layout_marginEnd="10dp" />

            <TextView
                android:id="@+id/statusText"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Active"
                android:textSize="13sp"
                android:textColor="@color/text_secondary"
                android:textStyle="normal" />

            <ImageView
                android:id="@+id/deleteButton"
                android:layout_width="28dp"
                android:layout_height="28dp"
                android:src="@android:drawable/ic_menu_delete"
                app:tint="@android:color/white"
                android:padding="6dp"
                android:clickable="true"
                android:focusable="true"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:contentDescription="Delete Work Program" />
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>

```
</details>

<details>
<summary><b>Code: item_cultivar_expandable.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="8dp"
    android:layout_marginEnd="8dp"
    android:layout_marginBottom="12dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="6dp"
    app:cardBackgroundColor="@color/white"
    android:foreground="?attr/selectableItemBackground"
    android:clickable="true"
    android:focusable="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <!-- Cultivar Header (always visible) -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/cultivarNameHeader"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Cultivar Name"
                    android:textStyle="bold"
                    android:textSize="20sp"
                    android:textColor="@color/tomato_red" />

                <TextView
                    android:id="@+id/cultivarProgramCount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="0 programs"
                    android:textColor="@color/text_secondary"
                    android:textSize="12sp"
                    android:layout_marginTop="4dp" />
            </LinearLayout>

            <!-- Expand/Collapse Icon -->
            <ImageView
                android:id="@+id/expandIcon"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@android:drawable/arrow_down_float"
                android:contentDescription="Expand"
                app:tint="@color/text_secondary" />
        </LinearLayout>

        <!-- Divider -->
        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="@color/divider"
            android:layout_marginTop="12dp"
            android:layout_marginBottom="12dp" />

        <!-- Summary Stats (always visible) -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:weightSum="3">

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="6dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="#F5F5F5"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="12dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="📏"
                        android:textSize="20sp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Total Area"
                        android:textSize="10sp"
                        android:textColor="@color/text_secondary" />
                    <TextView
                        android:id="@+id/totalArea"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="2dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="6dp"
                android:layout_marginEnd="6dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="#E8F5E9"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="12dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="💰"
                        android:textSize="20sp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Total Profit"
                        android:textSize="10sp"
                        android:textColor="@color/text_secondary" />
                    <TextView
                        android:id="@+id/totalProfit"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₱0"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:textColor="@color/sidebar_dark_green"
                        android:layout_marginTop="2dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="6dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="#FFF3E0"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="12dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="📊"
                        android:textSize="20sp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Profit/Area"
                        android:textSize="10sp"
                        android:textColor="@color/text_secondary" />
                    <TextView
                        android:id="@+id/profitPerArea"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₱0"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:textColor="@color/warm_orange"
                        android:layout_marginTop="2dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="12dp"
            android:weightSum="2">

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="6dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="#E3F2FD"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="12dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="✅"
                        android:textSize="20sp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Completion"
                        android:textSize="10sp"
                        android:textColor="@color/text_secondary" />
                    <TextView
                        android:id="@+id/avgCompletionRate"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0%"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:textColor="@color/scan_blue"
                        android:layout_marginTop="2dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="6dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="#F3E5F5"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="12dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="📈"
                        android:textSize="20sp"
                        android:layout_marginBottom="4dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Adjusted"
                        android:textSize="10sp"
                        android:textColor="@color/text_secondary" />
                    <TextView
                        android:id="@+id/adjustedProfit"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₱0"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:textColor="#9C27B0"
                        android:layout_marginTop="2dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

        <!-- Expanded Content (work programs list) -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/workProgramsRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:visibility="gone"
            android:nestedScrollingEnabled="false" />

    </LinearLayout>

</androidx.cardview.widget.CardView>


```
</details>

<details>
<summary><b>Code: item_cultivar_summary.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp"
    android:foreground="?attr/selectableItemBackground"
    android:clickable="false"
    android:focusable="false">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">

        <TextView
            android:id="@+id/summaryCultivarName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Cultivar Name"
            android:textStyle="bold"
            android:textSize="16sp"
            android:textColor="@color/text_primary" />

        <TextView
            android:id="@+id/summaryArea"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Area"
            android:textColor="@color/text_secondary"
            android:textSize="13sp"
            android:layout_marginTop="4dp" />

        <TextView
            android:id="@+id/summaryIncome"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Income"
            android:textColor="@color/text_secondary"
            android:textSize="13sp" />

        <TextView
            android:id="@+id/summaryExpenses"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Expenses"
            android:textColor="@color/text_secondary"
            android:textSize="13sp" />

        <TextView
            android:id="@+id/summaryProfit"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Profit"
            android:textColor="@color/text_secondary"
            android:textSize="13sp" />

        <TextView
            android:id="@+id/summaryProfitPerArea"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Profit per area"
            android:textColor="@color/text_secondary"
            android:textSize="13sp" />

        <TextView
            android:id="@+id/summaryCompletionRate"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Completion rate"
            android:textColor="@color/text_secondary"
            android:textSize="13sp" />

        <TextView
            android:id="@+id/summaryAdjustedProfit"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Adjusted profit"
            android:textColor="@color/text_secondary"
            android:textSize="13sp" />

    </LinearLayout>

</androidx.cardview.widget.CardView>



```
</details>

<details>
<summary><b>Code: item_daily_expense_history.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="12dp"
    app:cardBackgroundColor="@color/white"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- Date Header with Expand/Collapse -->
        <LinearLayout
            android:id="@+id/headerLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:clickable="true"
            android:focusable="true"
            android:background="?android:attr/selectableItemBackground"
            android:padding="4dp"
            android:layout_marginBottom="8dp">
            
            <TextView
                android:id="@+id/dateText"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Date"
                android:textColor="@color/text_primary"
                android:textSize="16sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/totalTextSummary"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="₱0.00"
                android:textColor="@color/fresh_green"
                android:textSize="14sp"
                android:textStyle="bold"
                android:layout_marginEnd="8dp" />

            <TextView
                android:id="@+id/expandIcon"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:text="▼"
                android:textColor="@color/text_secondary"
                android:textSize="16sp"
                android:gravity="center" />
        </LinearLayout>

        <!-- Expense Details Grid (Collapsible) -->
        <LinearLayout
            android:id="@+id/detailsContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginBottom="4dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Labor:"
                    android:textColor="@color/text_secondary"
                    android:textSize="12sp" />

                <TextView
                    android:id="@+id/laborText"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₱0.00"
                    android:textColor="@color/text_primary"
                    android:textSize="12sp"
                    android:textStyle="bold" />
            </LinearLayout>

            <TextView
                android:id="@+id/workersText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text=""
                android:textColor="@color/text_secondary"
                android:textSize="11sp"
                android:layout_marginStart="16dp"
                android:layout_marginBottom="4dp"
                android:visibility="gone" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginBottom="4dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Material:"
                    android:textColor="@color/text_secondary"
                    android:textSize="12sp" />

                <TextView
                    android:id="@+id/materialText"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₱0.00"
                    android:textColor="@color/text_primary"
                    android:textSize="12sp"
                    android:textStyle="bold" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginBottom="4dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Equipment/Tools:"
                    android:textColor="@color/text_secondary"
                    android:textSize="12sp" />

                <TextView
                    android:id="@+id/equipmentText"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₱0.00"
                    android:textColor="@color/text_primary"
                    android:textSize="12sp"
                    android:textStyle="bold" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginBottom="8dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Miscellaneous:"
                    android:textColor="@color/text_secondary"
                    android:textSize="12sp" />

                <TextView
                    android:id="@+id/miscellaneousText"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₱0.00"
                    android:textColor="@color/text_primary"
                    android:textSize="12sp"
                    android:textStyle="bold" />
            </LinearLayout>

        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>


```
</details>

<details>
<summary><b>Code: item_detection_history.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="12dp"
    app:cardBackgroundColor="@color/white"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp"
    app:strokeWidth="0dp"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?android:attr/selectableItemBackground">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="18dp"
        android:gravity="center_vertical">

        <!-- Circular Image -->
        <ImageView
            android:id="@+id/historyItemImage"
            android:layout_width="64dp"
            android:layout_height="64dp"
            android:layout_marginEnd="16dp"
            android:src="@mipmap/ic_logo"
            android:scaleType="centerCrop"
            android:contentDescription="Detection image"
            android:clipToOutline="true"
            android:background="@drawable/circular_image_background" />

        <!-- Text Content -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/historyItemTitle"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Detection Title"
                android:textColor="@color/text_primary"
                android:textSize="17sp"
                android:textStyle="bold"
                android:layout_marginBottom="6dp"
                android:maxLines="2"
                android:ellipsize="end" />

            <TextView
                android:id="@+id/historyItemDescription"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Detection description and details will appear here. This provides context about the detected pest or disease."
                android:textColor="@color/text_secondary"
                android:textSize="14sp"
                android:maxLines="3"
                android:ellipsize="end"
                android:lineSpacingExtra="2dp" />

            <TextView
                android:id="@+id/historyItemContext"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:textColor="@color/text_secondary"
                android:textSize="12sp" />
        </LinearLayout>

        <!-- Delete Button -->
        <ImageButton
            android:id="@+id/deleteButton"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_marginStart="8dp"
            android:src="@android:drawable/ic_menu_delete"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="Delete"
            app:tint="@color/error"
            android:scaleType="centerInside"
            android:padding="8dp"
            android:clickable="true"
            android:focusable="true" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>


```
</details>

<details>
<summary><b>Code: item_disease_info.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="0dp"
    android:layout_marginEnd="0dp"
    android:layout_marginTop="0dp"
    android:layout_marginBottom="12dp"
    app:cardBackgroundColor="@color/white"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp"
    app:strokeWidth="0dp"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?attr/selectableItemBackground">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="18dp"
        android:gravity="center_vertical">

        <!-- Circular Image/Icon -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="64dp"
            android:layout_height="64dp"
            android:layout_marginEnd="16dp"
            app:cardBackgroundColor="@color/soft_cream"
            app:cardCornerRadius="32dp"
            app:cardElevation="0dp"
            app:strokeWidth="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:gravity="center">

                <ImageView
                    android:id="@+id/diseaseImage"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:src="@mipmap/ic_logo"
                    android:scaleType="centerCrop"
                    android:contentDescription="Disease image" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Text Content -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/diseaseTitle"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Disease Title"
                android:textColor="@color/text_primary"
                android:textSize="17sp"
                android:textStyle="bold"
                android:layout_marginBottom="6dp"
                android:maxLines="2"
                android:ellipsize="end" />

            <TextView
                android:id="@+id/diseaseDescription"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Disease description and details will appear here. This provides context about the detected pest or disease."
                android:textColor="@color/text_secondary"
                android:textSize="14sp"
                android:maxLines="3"
                android:ellipsize="end"
                android:lineSpacingExtra="2dp" />
        </LinearLayout>

        <!-- Arrow Icon -->
        <ImageView
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:src="@android:drawable/ic_menu_more"
            android:rotation="90"
            app:tint="@color/text_secondary"
            android:alpha="0.6"
            android:layout_marginStart="8dp"
            android:contentDescription="View details" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>


```
</details>

<details>
<summary><b>Code: item_expense_table_row.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="8dp"
    android:background="?android:attr/selectableItemBackground"
    android:minWidth="1000dp">

    <TextView
        android:id="@+id/dateText"
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="Date"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp" />

    <TextView
        android:id="@+id/activityText"
        android:layout_width="120dp"
        android:layout_height="wrap_content"
        android:text="Activity"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp" />

    <TextView
        android:id="@+id/categoryText"
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="Category"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp" />

    <TextView
        android:id="@+id/laborCostText"
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="₱0"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp"
        android:gravity="end" />

    <TextView
        android:id="@+id/workersText"
        android:layout_width="80dp"
        android:layout_height="wrap_content"
        android:text="0"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp"
        android:gravity="center" />

    <TextView
        android:id="@+id/materialCostText"
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="₱0"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp"
        android:gravity="end" />

    <TextView
        android:id="@+id/equipmentCostText"
        android:layout_width="110dp"
        android:layout_height="wrap_content"
        android:text="₱0"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp"
        android:gravity="end" />

    <TextView
        android:id="@+id/miscCostText"
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="₱0"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp"
        android:gravity="end" />

    <TextView
        android:id="@+id/notesText"
        android:layout_width="150dp"
        android:layout_height="wrap_content"
        android:text="Notes"
        android:textColor="@color/text_primary"
        android:textSize="11sp"
        android:padding="4dp" />

    <TextView
        android:id="@+id/totalText"
        android:layout_width="120dp"
        android:layout_height="wrap_content"
        android:text="₱0"
        android:textColor="@color/fresh_green"
        android:textSize="11sp"
        android:textStyle="bold"
        android:padding="4dp"
        android:gravity="end" />
</LinearLayout>

```
</details>

<details>
<summary><b>Code: item_forecast_row.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    app:cardBackgroundColor="@color/white"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:padding="16dp">

        <ImageView
            android:id="@+id/icon"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginEnd="16dp"
            android:scaleType="fitCenter"
            android:src="@android:drawable/ic_menu_compass" />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/date"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Mon, Jan 1"
                android:textStyle="bold"
                android:textSize="16sp"
                android:textColor="@color/text_primary"
                android:layout_marginBottom="4dp" />

            <TextView
                android:id="@+id/condition"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Partly cloudy"
                android:textSize="14sp"
                android:textColor="@color/text_secondary"
                android:layout_marginBottom="2dp" />

            <TextView
                android:id="@+id/details"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="25°/31° · Rain 40%"
                android:textSize="13sp"
                android:textColor="@color/fresh_green" />
        </LinearLayout>
    </LinearLayout>

</com.google.android.material.card.MaterialCardView>



```
</details>

<details>
<summary><b>Code: item_monitoring_image.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="150dp"
    android:layout_height="150dp"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">

    <ImageView
        android:id="@+id/monitoringImage"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        android:contentDescription="Monitoring image" />

</com.google.android.material.card.MaterialCardView>


```
</details>

<details>
<summary><b>Code: item_notification.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/notificationCard"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp"
    app:cardBackgroundColor="@color/white">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/notificationTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Notification Title"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
            android:layout_marginBottom="4dp" />

        <TextView
            android:id="@+id/notificationMessage"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Notification message content goes here"
            android:textSize="14sp"
            android:textColor="@color/text_secondary"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/notificationTime"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Jan 01, 2024 12:00"
            android:textSize="12sp"
            android:textColor="@color/text_secondary" />
    </LinearLayout>

</com.google.android.material.card.MaterialCardView>

```
</details>

<details>
<summary><b>Code: item_season_comparison.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="8dp"
    android:layout_marginEnd="8dp"
    android:layout_marginBottom="12dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp"
    app:cardBackgroundColor="@color/white">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/comparisonTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Season Comparison"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
            android:layout_marginBottom="12dp" />

        <TextView
            android:id="@+id/comparisonMetric1"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Metric 1"
            android:textSize="14sp"
            android:textColor="@color/text_secondary"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/comparisonMetric2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Metric 2"
            android:textSize="14sp"
            android:textColor="@color/text_secondary"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/comparisonMetric3"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Metric 3"
            android:textSize="14sp"
            android:textColor="@color/text_secondary"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/comparisonMetric4"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Metric 4"
            android:textSize="14sp"
            android:textColor="@color/text_secondary" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>


```
</details>

<details>
<summary><b>Code: item_task.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    app:cardBackgroundColor="@color/white"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?android:attr/selectableItemBackground">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp"
        android:gravity="center_vertical">

        <!-- Task Icon -->
        <View
            android:id="@+id/taskIcon"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginEnd="16dp"
            android:background="@drawable/circle_green" />

        <!-- Task Info -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/taskName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Task Name"
                android:textColor="@color/text_primary"
                android:textSize="16sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/taskSubtext"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:text="Task category or phase"
                android:textColor="@color/text_secondary"
                android:textSize="12sp" />
        </LinearLayout>

        <!-- Checkbox -->
        <com.google.android.material.checkbox.MaterialCheckBox
            android:id="@+id/taskCheckbox"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:clickable="true"
            android:focusable="true" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>


```
</details>

<details>
<summary><b>Code: item_work_program_checkbox.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp"
    android:backgroundTint="@color/white">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:gravity="center_vertical">

        <CheckBox
            android:id="@+id/checkbox"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginEnd="12dp" />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/programCultivar"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Cultivar Name"
                android:textSize="14sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary" />

            <TextView
                android:id="@+id/programDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Start Date: YYYY-MM-DD"
                android:textSize="12sp"
                android:textColor="@color/text_secondary"
                android:layout_marginTop="4dp" />

            <TextView
                android:id="@+id/programProfit"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Profit: ₱0"
                android:textSize="12sp"
                android:textColor="@color/text_primary"
                android:layout_marginTop="4dp" />
        </LinearLayout>
    </LinearLayout>

</androidx.cardview.widget.CardView>






```
</details>

<details>
<summary><b>Code: item_work_program_details.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="8dp"
    android:layout_marginEnd="8dp"
    android:layout_marginBottom="16dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="6dp"
    app:cardBackgroundColor="@color/white">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <!-- Header Row with Colored Background -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="2dp"
            app:cardBackgroundColor="#E3F2FD"
            app:strokeWidth="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:padding="12dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="🌱 Cultivar"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary" />

                <TextView
                    android:id="@+id/detailsCultivarName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Cultivar Name"
                    android:textSize="15sp"
                    android:textStyle="bold"
                    android:textColor="@color/tomato_red"
                    android:gravity="end" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Info Grid -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginBottom="16dp">

            <!-- Row 1: Area & Date -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginBottom="8dp">

                <com.google.android.material.card.MaterialCardView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginEnd="6dp"
                    app:cardCornerRadius="10dp"
                    app:cardElevation="2dp"
                    app:cardBackgroundColor="#F5F5F5"
                    app:strokeWidth="0dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="12dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="📏 Area"
                            android:textSize="11sp"
                            android:textColor="@color/text_secondary"
                            android:layout_marginBottom="4dp" />

                        <TextView
                            android:id="@+id/detailsAreaSize"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="0 hectare"
                            android:textSize="14sp"
                            android:textStyle="bold"
                            android:textColor="@color/text_primary" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <com.google.android.material.card.MaterialCardView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginStart="6dp"
                    app:cardCornerRadius="10dp"
                    app:cardElevation="2dp"
                    app:cardBackgroundColor="#FFF3E0"
                    app:strokeWidth="0dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="12dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="📅 Start Date"
                            android:textSize="11sp"
                            android:textColor="@color/text_secondary"
                            android:layout_marginBottom="4dp" />

                        <TextView
                            android:id="@+id/detailsStartingDate"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="YYYY-MM-DD"
                            android:textSize="14sp"
                            android:textStyle="bold"
                            android:textColor="@color/warm_orange" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>
            </LinearLayout>

            <!-- Row 2: Phases (with detections integrated) -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    app:cardCornerRadius="10dp"
                    app:cardElevation="2dp"
                    app:cardBackgroundColor="#E8F5E9"
                    app:strokeWidth="0dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="12dp">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="🌱 Phases"
                            android:textSize="11sp"
                            android:textColor="@color/text_secondary"
                            android:layout_marginBottom="4dp" />

                        <TextView
                            android:id="@+id/detailsPhases"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="N/A"
                            android:textSize="14sp"
                            android:textStyle="bold"
                            android:textColor="@color/sidebar_dark_green" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>
            </LinearLayout>
        </LinearLayout>

        <!-- Financial Section Header -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="💰 Financial Summary"
            android:textStyle="bold"
            android:textSize="16sp"
            android:textColor="@color/text_primary"
            android:layout_marginTop="8dp"
            android:layout_marginBottom="12dp" />

        <!-- Financial Cards -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- Income Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="8dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="3dp"
                app:cardBackgroundColor="#E8F5E9"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:padding="14dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="💵 Income"
                        android:textSize="14sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary" />

                    <TextView
                        android:id="@+id/detailsProjectedIncome"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₱0.00"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/sidebar_dark_green" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Expenses Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="8dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="3dp"
                app:cardBackgroundColor="#FFF3E0"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:padding="14dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="💸 Expenses"
                        android:textSize="14sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary" />

                    <TextView
                        android:id="@+id/detailsProjectedExpenses"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₱0.00"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/chart_orange" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Profit Card (Highlighted) -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:cardCornerRadius="12dp"
                app:cardElevation="4dp"
                app:cardBackgroundColor="@color/tomato_red"
                app:strokeWidth="0dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:padding="16dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="💰 Net Profit"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/white" />

                    <TextView
                        android:id="@+id/detailsProfit"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₱0.00"
                        android:textSize="20sp"
                        android:textStyle="bold"
                        android:textColor="@color/white" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

    </LinearLayout>

</androidx.cardview.widget.CardView>


```
</details>

<details>
<summary><b>Code: item_work_program_nested.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="16dp"
    android:layout_marginEnd="16dp"
    android:layout_marginBottom="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp"
    android:backgroundTint="@color/white">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">

        <!-- First Row: Date and Area -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">

            <TextView
                android:id="@+id/nestedStartDate"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Start Date"
                android:textSize="13sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary" />

            <TextView
                android:id="@+id/nestedArea"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Area"
                android:textSize="12sp"
                android:textColor="@color/text_secondary" />
        </LinearLayout>

        <!-- Second Row: Phases (with detections integrated) -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="4dp"
            android:gravity="center_vertical">

            <TextView
                android:id="@+id/nestedPhases"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Phases"
                android:textSize="11sp"
                android:textColor="@color/text_secondary" />
        </LinearLayout>

        <!-- Third Row: Income, Expenses, Profit -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="8dp"
            android:weightSum="3">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Income"
                    android:textSize="10sp"
                    android:textColor="@color/text_secondary" />
                <TextView
                    android:id="@+id/nestedIncome"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₱0"
                    android:textSize="12sp"
                    android:textStyle="bold"
                    android:textColor="@color/sidebar_dark_green"
                    android:layout_marginTop="2dp" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Expenses"
                    android:textSize="10sp"
                    android:textColor="@color/text_secondary" />
                <TextView
                    android:id="@+id/nestedExpenses"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₱0"
                    android:textSize="12sp"
                    android:textStyle="bold"
                    android:textColor="@color/chart_orange"
                    android:layout_marginTop="2dp" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Profit"
                    android:textSize="10sp"
                    android:textColor="@color/text_secondary" />
                <TextView
                    android:id="@+id/nestedProfit"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₱0"
                    android:textSize="12sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:layout_marginTop="2dp" />
            </LinearLayout>
        </LinearLayout>

    </LinearLayout>

</androidx.cardview.widget.CardView>


```
</details>

<details>
<summary><b>Code: item_work_program_option.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cardProgram"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/spacing_medium"
    app:cardCornerRadius="@dimen/corner_radius_medium"
    app:cardElevation="@dimen/elevation_card"
    app:cardBackgroundColor="@android:color/transparent"
    android:clickable="true"
    android:focusable="true"
    android:foreground="@drawable/ripple_detection_type"
    android:background="@drawable/card_leaves_background">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="@dimen/spacing_large"
        android:gravity="center_vertical"
        android:minHeight="80dp">

        <!-- Icon Container -->
        <FrameLayout
            android:layout_width="56dp"
            android:layout_height="56dp"
            android:layout_marginEnd="@dimen/spacing_medium"
            android:background="@drawable/circle_icon_background"
            android:gravity="center"
            android:padding="2dp">

            <ImageView
                android:id="@+id/imgCultivar"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_gravity="center"
                android:contentDescription="Cultivar Image"
                android:scaleType="centerCrop"
                android:adjustViewBounds="true" />
        </FrameLayout>

        <!-- Text Content -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/txtCultivarName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Cultivar Name"
                android:textSize="@dimen/text_size_body"
                android:textStyle="bold"
                android:textColor="@color/text_primary"
                android:layout_marginBottom="4dp" />

            <TextView
                android:id="@+id/txtStartDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Start Date: N/A"
                android:textSize="@dimen/text_size_caption"
                android:textColor="@color/text_secondary" />
        </LinearLayout>

        <!-- Arrow Icon -->
        <ImageView
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:src="@android:drawable/ic_menu_more"
            android:contentDescription="Select"
            app:tint="@color/fresh_green"
            android:rotation="90" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>


```
</details>

<details>
<summary><b>Code: item_work_program_table.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginBottom="4dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp"
    android:backgroundTint="@color/white"
    android:minWidth="1000dp">

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:gravity="center_vertical"
        android:minWidth="1000dp">

        <!-- Cultivar Column -->
        <TextView
            android:id="@+id/tableCultivar"
            android:layout_width="120dp"
            android:layout_height="wrap_content"
            android:text="Cultivar"
            android:textSize="12sp"
            android:textColor="@color/text_primary"
            android:maxLines="2"
            android:ellipsize="end" />

        <!-- Area Size Column -->
        <TextView
            android:id="@+id/tableArea"
            android:layout_width="80dp"
            android:layout_height="wrap_content"
            android:text="Area"
            android:textSize="12sp"
            android:textColor="@color/text_primary"
            android:gravity="end" />

        <!-- Starting Date Column -->
        <TextView
            android:id="@+id/tableStartDate"
            android:layout_width="100dp"
            android:layout_height="wrap_content"
            android:text="Date"
            android:textSize="12sp"
            android:textColor="@color/text_primary"
            android:gravity="center" />

        <!-- Phases Summary Column (with detections integrated) -->
        <TextView
            android:id="@+id/tablePhases"
            android:layout_width="180dp"
            android:layout_height="wrap_content"
            android:text="Phases"
            android:textSize="12sp"
            android:textColor="@color/text_secondary"
            android:gravity="center"
            android:maxLines="2"
            android:ellipsize="end" />

        <!-- Projected Income Column -->
        <TextView
            android:id="@+id/tableIncome"
            android:layout_width="120dp"
            android:layout_height="wrap_content"
            android:text="Income"
            android:textSize="12sp"
            android:textColor="@color/sidebar_dark_green"
            android:gravity="end"
            android:textStyle="bold" />

        <!-- Projected Expenses Column -->
        <TextView
            android:id="@+id/tableExpenses"
            android:layout_width="120dp"
            android:layout_height="wrap_content"
            android:text="Expenses"
            android:textSize="12sp"
            android:textColor="@color/chart_orange"
            android:gravity="end"
            android:textStyle="bold" />

        <!-- Projected Profit Column -->
        <TextView
            android:id="@+id/tableProfit"
            android:layout_width="120dp"
            android:layout_height="wrap_content"
            android:text="Profit"
            android:textSize="12sp"
            android:textColor="@color/text_primary"
            android:gravity="end"
            android:textStyle="bold" />

    </LinearLayout>

</androidx.cardview.widget.CardView>


```
</details>

<details>
<summary><b>Code: nav_drawer_sidebar.xml</b></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawerHeader"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@color/sidebar_dark_green"
    android:padding="24dp"
    android:gravity="center_horizontal"
    android:clickable="true"
    android:focusable="true"
    android:backgroundTint="@color/sidebar_dark_green">

    <!-- Profile Picture at Top -->
    <ImageView
        android:id="@+id/sidebarProfileImage"
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:src="@mipmap/ic_logo"
        android:layout_marginTop="16dp"
        android:layout_marginBottom="16dp"
        android:contentDescription="Profile Picture"
        android:scaleType="centerCrop"
        android:background="@drawable/circle_background"
        android:padding="3dp"
        android:clipToOutline="true" />

    <!-- User Name -->
    <TextView
        android:id="@+id/sidebarUserName"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="User Name"
        android:textColor="@color/white"
        android:textSize="18sp"
        android:textStyle="bold"
        android:gravity="center"
        android:layout_marginBottom="4dp" />

    <!-- User Email -->
    <TextView
        android:id="@+id/sidebarUserEmail"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="user@example.com"
        android:textColor="@color/white"
        android:textSize="12sp"
        android:gravity="center"
        android:alpha="0.8"
        android:layout_marginBottom="16dp"
        android:maxLines="1"
        android:ellipsize="end" />

</LinearLayout>


```
</details>
