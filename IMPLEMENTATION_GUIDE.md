# TomatoApp - Jetpack Compose Implementation Guide

## 📋 Project Overview

This document provides a complete guide to the TomatoApp implementation using Kotlin and Jetpack Compose. The application follows MVVM architecture with clean architecture principles.

## 🎯 Architecture

### Directory Structure

```
com/android/tomatoapp/
├── MainActivity.kt                    # Main entry point
├── navigation/
│   └── TomatoAppNavigation.kt        # Navigation graph and routes
├── ui/
│   ├── theme/
│   │   ├── Theme.kt                  # Color palette & theme setup
│   │   ├── Typography.kt             # Font families and text styles
│   │   ├── Shapes.kt                 # Border radiuses
│   │   └── Dimensions.kt             # Spacing & sizing constants
│   ├── components/
│   │   ├── AppCard.kt                # Reusable card component
│   │   ├── AppButton.kt              # Primary, outlined, and small buttons
│   │   ├── AppTextField.kt           # Text input fields
│   │   ├── AppChip.kt                # Chip/tag components
│   │   ├── AppProgressBar.kt         # Progress bar component
│   │   ├── AppBottomNavBar.kt        # Bottom navigation
│   │   ├── AppHeaderGradient.kt      # Gradient header section
│   │   ├── AppStatCard.kt            # Statistics display card
│   │   ├── ExpenseItemRow.kt         # Expense list item
│   │   ├── CalendarDayCell.kt        # Calendar day cell
│   │   └── Components.kt             # Component exports
│   ├── screens/
│   │   ├── login/
│   │   │   └── LoginScreen.kt        # Login screen implementation
│   │   ├── home/
│   │   │   └── HomeScreen.kt         # Home screen implementation
│   │   ├── workprogram/
│   │   │   └── WorkProgramScreen.kt  # Work program with calendar
│   │   ├── calculator/
│   │   │   └── CalculatorScreen.kt   # Financial calculator
│   │   ├── ipm/
│   │   │   └── IPMScreen.kt          # Integrated Pest Management
│   │   ├── analytics/
│   │   │   └── AnalyticsScreen.kt    # Analytics dashboard
│   │   └── disease/
│   │       └── DiseaseDetailScreen.kt # Disease information detail
└── viewmodel/
    └── ScreenViewModels.kt           # All ViewModels and state classes
```

## 🎨 Design System

### Colors
- **Primary Red**: #D93025 - Main brand color
- **Dark Red**: #9B1C1C - Header/dark elements
- **Light Red**: #FDECEA - Background/highlights
- **Green Primary**: #2D7A3A - Secondary actions
- **Green Accent**: #3DBE5A - Active states
- **Green Light**: #E6F4EA - Light backgrounds
- **Orange**: #E67E22 - Warnings/tertiary actions
- **Blue**: #1976D2 - Information/complementary
- **Cream**: #FFF9F5 - App background
- **White**: #FFFFFF - Surfaces

### Typography
- **Serif Font**: Playfair Display (titles, headings)
- **Sans-serif Font**: DM Sans (body, UI elements)

### Spacing
- xs: 4dp | sm: 8dp | md: 12dp | lg: 16dp | xl: 20dp | xxl: 24dp

### Corner Radius
- Small: 10dp
- Medium: 16dp
- Large: 18dp-22dp
- Pill: 999dp

## 🧩 Reusable Components

### AppCard
Card with rounded corners, shadow, and optional border.
```kotlin
AppCard(
    modifier = Modifier.fillMaxWidth(),
    backgroundColor = SurfaceWhite,
    contentPadding = PaddingValues(16.dp)
) {
    // Card content
}
```

### AppButtonPrimary
Full-width button with rounded pill shape.
```kotlin
AppButtonPrimary(
    text = "Sign In",
    onClick = { /* action */ },
    backgroundColor = RedPrimary
)
```

### AppTextField
Pill-shaped input field with label and placeholder.
```kotlin
AppTextField(
    value = email,
    onValueChange = { email = it },
    label = "Email Address",
    placeholder = "farmer@example.com"
)
```

### AppProgressBar
Linear progress indicator with rounded ends.
```kotlin
AppProgressBar(
    progress = 0.27f, // 27%
    backgroundColor = GreenLight,
    fillColor = GreenAccent
)
```

### CalendarDayCell
Calendar cell with different states (vegetative, flowering, fruiting, today).
```kotlin
CalendarDayCell(
    state = CalendarDayState.Vegetative(15)
)
```

## 📱 Screen Implementations

### 1. Login Screen
- Gradient red header with logo
- Email and password input fields
- Sign in button
- Google login option
- Registration link

### 2. Home Screen
- Gradient red header with greeting and avatar
- Weather card with temperature
- Crop progress card with progress bar
- Quick action cards (Pest Scan, Finance)
- Bottom navigation bar

### 3. Work Program Screen
- Green gradient header with crop info
- Interactive calendar with color-coded states
- Legend explaining calendar states
- Season summary statistics
- View expenses button

### 4. Calculator Screen
- Green gradient income summary header
- Harvest metrics card
- Farm metrics input fields
- Expense breakdown section
- Save to analytics button

### 5. IPM (Pest Management) Screen
- Green gradient header
- Action cards (Scan Leaf, Scan History, Disease Database)
- Quick scan tips section
- Tips with bullet points

### 6. Analytics Screen
- Red gradient header
- Net profit and completion stats
- Filter controls (View Mode, Cultivar, Season)
- Export buttons (PDF, CSV)
- Bar chart (Profit per Area)
- Line chart (Completion Rate Trend)

### 7. Disease Detail Screen
- Dark header with back button
- Severity chip badge
- Disease title and scientific name
- Disease image placeholder
- Symptom/Treatment/Prevention pills
- Detailed content sections

## 🧠 State Management

### ViewModels

Each screen has its own ViewModel managing state:

```kotlin
// Example: HomeState
data class HomeState(
    val userName: String = "Juan Dela Cruz",
    val temperature: String = "31°C",
    // ... other properties
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    fun updateTemperature(temp: String) {
        _state.value = _state.value.copy(temperature = temp)
    }
}
```

Screens observe state using `collectAsState()`:

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    
    // Use state.temperature, etc.
}
```

## 🧭 Navigation

All navigation is handled through Jetpack Navigation Compose:

```kotlin
Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val WORK_PROGRAM = "work_program"
    const val CALCULATOR = "calculator"
    const val IPM = "ipm"
    const val ANALYTICS = "analytics"
    const val DISEASE_DETAIL = "disease_detail"
}
```

Navigation happens through NavController callbacks passed to screens.

## 🚀 Build & Run

### Dependencies Added
- Jetpack Compose UI
- Jetpack Compose Material 3
- Navigation Compose
- Lifecycle Compose
- Activity Compose

### Gradle Configuration
```gradle
buildFeatures {
    compose = true
}

composeOptions {
    kotlinCompilerExtensionVersion = "1.5.11"
}
```

### Running the App

1. Sync Gradle
2. Run on emulator or device
3. App launches to LoginScreen
4. Navigate through screens using bottom nav or action buttons

## 📝 Key Implementation Details

### No Hardcoding
All UI strings, colors, and dimensions use design system constants from `theme/`.

### Component Reusability
Components are generic and accept parameters rather than hardcoding values.

### Elevation & Shadows
All components use soft shadows (2dp elevation) for modern, clean aesthetic.

### Responsive Design
Layouts use `fillMaxWidth()`, `weight()`, and `Spacer()` for proper alignment on different screen sizes.

### State Immutability
All state is immutable using data classes and `copy()` for updates.

## 🎯 Future Enhancements

1. **Real API Integration**: Replace mock data with actual API calls
2. **Local Database**: Add Room database for offline storage
3. **Images**: Implement actual image loading with Coil or Glide
4. **Charts**: Replace simple chart implementations with Compose Charts library
5. **Animations**: Add transitions and animations between screens
6. **Testing**: Add unit tests and UI tests
7. **Accessibility**: Enhance accessibility features

## 📖 Code Quality

- **Kotlin**: All code follows Kotlin conventions
- **Compose**: Uses latest Compose patterns and best practices
- **Architecture**: Clean architecture with separation of concerns
- **Naming**: Clear, descriptive naming for all classes and functions
- **Comments**: Code is self-documenting with minimal comments needed

## ✅ Checklist

- [x] Theme system with colors, typography, shapes
- [x] All reusable components created
- [x] Navigation graph configured
- [x] All ViewModels implemented
- [x] All 7 screens implemented
- [x] State management with StateFlow
- [x] Gradient headers on screens
- [x] Calendar implementation
- [x] Chart placeholders
- [x] Filter controls
- [x] Export buttons
- [x] Jetpack Compose dependencies added
- [x] Material 3 implementation

## 📞 Support

For any issues or questions about implementation, refer to:
- Jetpack Compose documentation: https://developer.android.com/jetpack/compose
- Material 3 Compose: https://developer.android.com/jetpack/androidx/releases/compose-material3
- Navigation Compose: https://developer.android.com/jetpack/androidx/releases/navigation
