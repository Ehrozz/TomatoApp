# 🏗️ TomatoApp Modular Architecture

## Overview

TomatoApp is organized into a **modular, feature-based architecture** following SOLID principles and DRY (Don't Repeat Yourself) patterns. Each module is self-contained and responsible for a specific business domain.

---

## Module Organization

### 🔐 **auth** - Authentication & User Management
- **Location**: `auth/`
- **Sub-packages**:
  - `auth.data` - User data models and authentication state
  - `auth.ui` - Login, Register, and Profile activities
- **Key Classes**:
  - `User.java` - User data model
  - `Login.java` - Login UI screen
  - `Register.java` - Registration UI screen
  - `ProfileActivity.java` - User profile management
- **Responsibility**: Handles user authentication, registration, and profile management

---

### 🌾 **workprogram** - Work Program Management
- **Location**: `workprogram/`
- **Sub-packages**:
  - `workprogram.data` - Database entities and DAOs for work programs
  - `workprogram.ui` - Work program creation and selection screens
  - `workprogram.domain` - Business logic (currently empty, ready for services)
- **Key Classes**:
  - `WorkProgramEntity.java` - Database entity for work programs
  - `WorkProgramDao.java` - Data access object for Room database
  - `WorkProgramRepository.java` - Repository pattern for data abstraction
  - `WorkProgramDataHelper.java` - Helper utilities for work program data
  - `Workprogram.java` - Create/edit work program activity
  - `WorkProgramSelection.java` - Select active work program activity
- **Responsibility**: Manages tomato cultivation programs from creation to harvest

---

### 📅 **season** - Seasonal Classification
- **Location**: `season/`
- **Sub-packages**:
  - `season.data` - Season detection logic and data
  - `season.domain` - Business logic (currently empty, ready for services)
- **Key Classes**:
  - `SeasonHelper.java` - Detects on-season vs off-season based on date
- **Business Logic**:
  - On-Season: October-February (dry season - traditional)
  - Off-Season: March-September (wet season - research focus)
- **Responsibility**: Classifies programs by season for research analysis

---

### ✅ **task** - Task Management
- **Location**: `task/`
- **Sub-packages**:
  - `task.data` - Task entities, DAOs, and models
  - `task.ui` - Task display and management screens
- **Key Classes**:
  - `TaskEntity.java` - Database entity for daily tasks
  - `TaskDao.java` - Data access for tasks
  - `TaskModel.java` - Task business model
  - `TaskSchedule.java` - Task scheduling logic
  - `DailyTask.java` - Daily task management activity
- **Task Workflow**:
  - Generated based on growth phase
  - Categories: Pest Control, Fertilization, Watering, Disease Management
  - Statuses: Pending, Completed, Missed, Skipped
- **Responsibility**: Manages daily tasks across 5 growth phases

---

### 🔍 **detection** - Disease/Pest Detection (ML)
- **Location**: `detection/`
- **Sub-packages**:
  - `detection.data` - Detection history entities and DAOs
  - `detection.ui` - Camera and detection result screens
  - `detection.ml` - TensorFlow Lite model management (ready for MLModelLoader)
  - `detection.domain` - Detection business logic (ready for services)
- **Key Classes**:
  - `CameraInterface.java` - Camera capture using CameraX
  - `DetectionResults.java` - Display ML inference results
  - `DiseaseView.java` - Show disease details and remedies
  - `DetectionHistoryEntity.java` - Store detection records
  - `DetectionHistoryDao.java` - Access detection history
  - `SimpleCaptureActivity.java` - Simple image capture
- **ML Models**:
  - `model_fruits.tflite` - Fruit disease detection
  - `model_leaves.tflite` - Leaf disease detection
  - `model_pest.tflite` - Pest detection
- **Responsibility**: Captures plant images and identifies diseases/pests using TensorFlow Lite

---

### 💰 **financial** - Financial Management
- **Location**: `financial/`
- **Sub-packages**:
  - `financial.data` - Financial entities and calculations
  - `financial.ui` - Expenses and income tracking screens
  - `financial.domain` - Financial business logic (FinancialCalculator service ready)
- **Key Classes**:
  - `CalculationEntity.java` - Financial calculation records
  - `CalculationDao.java` - Financial data access
  - `CalculationModel.java` - Financial models
  - `Calculator.java` - Financial calculations screen
  - `CostSelection.java` - Expense categorization
  - `CurrentExpensesActivity.java` - Active expenses view
  - `DailyExpensesActivity.java` - Daily expense entry
  - `DailyExpensesHistoryActivity.java` - Expense history
- **Calculations**:
  - Gross income tracking
  - Projected vs. adjusted expenses
  - Net income calculations
  - Per-hectare metrics
- **Responsibility**: Tracks income, expenses, and profit calculations

---

### 🌤️ **weather** - Weather Data Collection
- **Location**: `weather/`
- **Sub-packages**:
  - `weather.data` - Weather data entities and API integration
  - `weather.ui` - Weather display screens
  - `weather.domain` - Weather business logic (WeatherService ready)
- **Key Classes**:
  - `WeatherData.java` - Weather data entity
  - `WeatherDataDao.java` - Weather data access
  - `WeatherDataCollector.java` - Real-time API integration (Open-Meteo)
  - `ForecastActivity.java` - Weather forecast display
- **API Integration**:
  - **Provider**: Open-Meteo API (free, no API key required)
  - **Endpoint**: `https://api.open-meteo.com/v1/forecast`
  - **Data Collected**:
    - Current temperature, min/max, precipitation, humidity
    - Running averages over program duration
- **Responsibility**: Fetches and stores weather data for research analysis

---

### 📸 **monitoring** - Plant Monitoring
- **Location**: `monitoring/`
- **Sub-packages**:
  - `monitoring.data` - Plant photo entities and storage
  - `monitoring.ui` - Plant monitoring display screens
- **Key Classes**:
  - `PlantMonitoringEntity.java` - Database entity for plant observations
  - `PlantMonitoringDao.java` - Data access for plant data
  - `PlantMonitoringRepository.java` - Repository abstraction
  - `PlantPhotoStorage.java` - Photo file storage management
  - `PlantMonitoringActivity.java` - Plant monitoring UI
- **Responsibility**: Tracks plant photos and health observations over time

---

### 📊 **analytics** - Analytics & Research Reporting
- **Location**: `analytics/`
- **Sub-packages**:
  - `analytics.data` - Analysis engines and exporters
  - `analytics.ui` - Analytics display and report screens
- **Key Classes**:
  - `AnalyticsManager.java` - Aggregation and analysis calculations
  - `ResearchExporter.java` - CSV export for statistical analysis
  - `AnalyticsPdfExporter.java` - PDF report generation
  - `AnalyticsActivity.java` - Main analytics dashboard
  - `SeasonComparisonActivity.java` - On-season vs off-season comparison
  - `DetectionHistoryActivity.java` - Detection records view
  - `CultivarDetailsActivity.java` - Cultivar performance details
- **Analysis Features**:
  - Seasonal comparison (on-season vs off-season)
  - Cultivar performance aggregation
  - Yield metrics and tracking
  - Profit analysis
  - CSV export with weather data
- **Responsibility**: Analyzes data and generates research reports

---

### 🔔 **notifications** - Notification System
- **Location**: `notifications/`
- **Features**:
  - Task reminders and updates
  - General notifications
  - Boot receiver for persistence
  - Notification channels for categorization
- **Key Classes**:
  - `NotificationChannels.java` - Notification channel setup
  - `NotificationScheduler.java` - Schedule notifications
  - `GeneralUpdateScheduler.java` - General updates
  - `MonitoringReminderScheduler.java` - Plant monitoring reminders
  - `NotificationBootReceiver.java` - Boot completion receiver
  - `NotificationReceiver.java` - Notification receiver
  - `NotificationManager.java` - Notification management
  - `NotificationHelper.java` - Helper utilities
  - `NotificationPermissionHelper.java` - Permission handling
  - `NotificationPreferences.java` - User notification preferences
  - `NotificationUseCases.java` - Notification use cases
- **Responsibility**: Manages all in-app and system notifications

---

### ⚙️ **settings** - Settings & Localization
- **Location**: `settings/`
- **Sub-packages**:
  - `settings.data` - Settings entities and preferences
  - `settings.ui` - Settings screens
- **Key Classes**:
  - `SettingsEntity.java` - Settings database entity
  - `SettingsDao.java` - Settings data access
  - `SettingsPreferences.java` - SharedPreferences wrapper
  - `SettingsActivity.java` - Settings UI screen
  - `UserManualActivity.java` - User manual/help screen
- **Settings managed**:
  - Language selection (multi-language support)
  - Theme selection (dark mode, light mode, system)
  - User preferences
- **Responsibility**: User settings and localization management

---

### 🔧 **core** - Core Infrastructure
- **Location**: `core/`
- **Sub-packages**:
  - `core.database` - Room database configuration
  - `core.network` - Network/Firebase operations
  - `core.ui` - Base activities and main entry point
- **Key Classes**:
  - `AppDatabase.java` - Room database singleton
    - Entities: WorkProgram, Task, Calculation, DetectionHistory, PlantMonitoring, WeatherData, Settings
  - `LocalDataManager.java` - Firebase sync and offline-first logic
  - `FirebaseErrorHandler.java` - Firebase exception handling
  - `MainActivity.java` - Main entry point after login
  - `BaseDrawerActivity.java` - Base class for drawer navigation
- **Responsibility**: Infrastructure and cross-cutting concerns

---

### 📦 **common** - Shared Utilities & Components
- **Location**: `common/`
- **Sub-packages**:
  - `common.models` - Shared data models
  - `common.ui.dialogs` - Reusable dialogs
  - `common.ui.components` - Custom UI components
  - `common.utils` - Utility functions
  - `common.managers` - Cross-cutting managers
- **Key Classes**:
  - **Models**:
    - `LocationEntry.java` - Location data
    - `DiseaseInfo.java` - Disease information
    - `DiseaseData.java` - Disease data
    - `CultivarNPData.java` - Cultivar nutrient/pesticide data
  - **Dialogs**:
    - `TermsDialog.java` - Terms and conditions
    - `TutorialDialog.java` - In-app tutorials
  - **Components**:
    - `DonutChartView.java` - Custom donut chart visualization
  - **Utils**:
    - `PhaseHelper.java` - Growth phase calculations
    - `PhilippineLocations.java` - Philippine location data
    - `PhoneUtils.java` - Phone utility functions
    - `ReferenceImageProvider.java` - Reference image management
    - `CultivarImageHelper.java` - Cultivar image handling
    - `TutorialManager.java` - Tutorial content management
  - **Managers**:
    - `AppNotificationManager.java` - Notification coordination
    - `CompletedDecorator.java` - Completed task decorator
    - `MissedDecorator.java` - Missed task decorator
    - `SkippedDecorator.java` - Skipped task decorator
  - **Activities**:
    - `InformationInterface.java` - Information display
    - `IPM.java` - Integrated Pest Management screen
    - `NotificationListActivity.java` - Notification list display
- **Responsibility**: Shared code avoiding repetition (DRY principle)

---

## Key Architectural Principles

### 1. **Modular Design**
- Each module has a single, well-defined responsibility
- Modules are loosely coupled and highly cohesive
- Easy to test, maintain, and extend

### 2. **Layered Architecture within Modules**
- **UI Layer** (Activities) - User interface
- **Domain Layer** (Services) - Business logic
- **Data Layer** (Entities, DAOs, Repositories) - Data access

### 3. **DRY Principle**
- Shared code in `common/` module
- No code duplication across modules
- Utilities vs UI vs Services clearly separated

### 4. **SOLID Principles**
- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes are substitutable for their base types
- **I**nterface Segregation: Many specific interfaces vs one general-purpose
- **D**ependency Inversion: Depend on abstractions, not concretions

### 5. **Offline-First Architecture**
- Room database for local-first operations
- Firebase for backup and sync
- LocalDataManager handles sync logic

---

## Future Enhancement: Dependency Injection

To further improve the architecture, consider implementing **Hilt** for dependency injection:

```gradle
implementation("com.google.dagger:hilt-android:VERSION")
```

This would enable:
- Repository injection into Activities/ViewModels
- Service/Manager injection
- Lifecycle-aware object management
- Easier testing with mock objects

---

## Best Practices for Developers

1. **Keep modules isolated** - Minimize cross-module dependencies
2. **Use repositories** - Don't access DAOs directly from Activities
3. **Create services** - Implement business logic in `domain/` packages
4. **Extend common components** - Use utilities and base classes from `common/`
5. **Follow naming conventions** - Use consistent naming across modules
6. **Document public APIs** - Add JavaDoc to public methods
7. **Test in layers** - Unit test logic, integration test data layer

---

## Module Dependency Graph

```
┌─────────────────────────────────────┐
│          UI Activities              │
│  (auth.ui, workprogram.ui, etc.)   │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│      Core Infrastructure            │
│  (BaseDrawerActivity, MainActivity) │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│   Business Logic & Services         │
│  (AnalyticsManager, SeasonHelper)   │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│      Data Access Layer              │
│  (DAOs, Repositories, Entities)     │
└────────────────┬────────────────────┘
                 │
      ┌──────────┴──────────┐
      │                     │
┌─────▼────────┐   ┌───────▼───────┐
│ Room Database │   │  Firebase DB  │
└──────────────┘   └───────────────┘
```

---

## File Tree Overview

```
com.android.tomatoapp/
├── auth/
│   ├── data/
│   │   └── User.java
│   └── ui/
│       ├── Login.java
│       ├── Register.java
│       └── ProfileActivity.java
├── workprogram/
│   ├── data/
│   ├── ui/
│   └── domain/
├── season/
│   ├── data/
│   │   └── SeasonHelper.java
│   └── domain/
├── task/
│   ├── data/
│   └── ui/
├── detection/
│   ├── data/
│   ├── ui/
│   ├── ml/
│   └── domain/
├── financial/
│   ├── data/
│   ├── ui/
│   └── domain/
├── weather/
│   ├── data/
│   ├── ui/
│   └── domain/
├── monitoring/
│   ├── data/
│   └── ui/
├── analytics/
│   ├── data/
│   └── ui/
├── notifications/
│   └── (10 notification classes)
├── settings/
│   ├── data/
│   └── ui/
├── core/
│   ├── database/
│   ├── network/
│   └── ui/
├── common/
│   ├── models/
│   ├── ui/
│   │   ├── dialogs/
│   │   └── components/
│   ├── utils/
│   └── managers/
└── TomatoAppApplication.java
```

---

## Next Steps

1. **Implement Repository Pattern** across all modules
2. **Add Domain/Service Layer** to each module's `domain/` package
3. **Migrate to MVVM** using LiveData and ViewModel
4. **Add Hilt Dependency Injection**
5. **Implement comprehensive Unit Tests**
6. **Add Integration Tests** for data layer

This modular structure provides a strong foundation for scalable, maintainable Android development! 🚀
