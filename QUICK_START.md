# TomatoApp - Quick Start Guide

## 🚀 Getting Started

### Prerequisites
- Android Studio (Latest version)
- Kotlin 1.9+
- Java 11+
- Min SDK: 24 (Android 7.0)
- Target SDK: 36

### Setup Steps

1. **Sync Gradle**
   ```
   File → Sync Now
   ```

2. **Run the App**
   ```
   Run → Run 'app'
   ```

3. **App Launches to Login Screen**
   - Email: Any text (e.g., farmer@example.com)
   - Password: Any text
   - Click "Sign In" → Home Screen

## 📱 Navigating the App

### From Login Screen
- Click "Sign In" or "Continue with Google" → **Home Screen**
- Click "Create one" → Does not navigate (future feature)

### From Home Screen (with Bottom Nav)
- 🏠 **Home** - Currently selected
- 📅 **Program** - Work Program Screen
- 🔍 **IPM** - Pest Management Screen  
- 📊 **Analytics** - Analytics Screen

### Quick Actions on Home
- **Pest Scan Card** → Goes to IPM Screen
- **Finance Card** → Goes to Calculator Screen

### From Each Screen
- **Work Program**: Click "View Current Expenses" → Calculator Screen
- **Calculator**: Click "Save to Analytics" → Analytics Screen
- All screens navigate through bottom nav

## 🎨 Customization Guide

### Changing Colors

Edit `ui/theme/Theme.kt`:
```kotlin
val RedPrimary = Color(0xFFD93025)  // Change this hex code
```

All screens automatically use the new color.

### Changing Typography

Edit `ui/theme/Typography.kt`:
```kotlin
bodyLarge = TextStyle(
    fontFamily = DmSansFamily,
    fontSize = 16.sp,  // Change size
    fontWeight = FontWeight.Normal
)
```

### Adjusting Spacing

Edit `ui/theme/Dimensions.kt`:
```kotlin
object Spacing {
    val lg = 16.dp  // Change this value
}
```

## 🔄 Modifying State

### Example: Update Home Temperature

1. Find `HomeViewModel` in `viewmodel/ScreenViewModels.kt`
2. Add new function:
```kotlin
fun updateTemperature(temp: String) {
    _state.value = _state.value.copy(temperature = temp)
}
```

3. Call from screen:
```kotlin
viewModel.updateTemperature("25°C")
```

## ➕ Adding New Components

### Template for New Component

```kotlin
// File: ui/components/AppNewComponent.kt
@Composable
fun AppNewComponent(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(SurfaceWhite, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}
```

## 🗂️ Adding New Screen

### Template for New Screen

```kotlin
// File: ui/screens/newscreen/NewScreen.kt
@Composable
fun NewScreen(
    viewModel: NewViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // Your content here
    }
}

// File: ui/viewmodel/ScreenViewModels.kt
data class NewState(val title: String = "")
class NewViewModel : ViewModel() {
    private val _state = MutableStateFlow(NewState())
    val state: StateFlow<NewState> = _state.asStateFlow()
}

// File: navigation/TomatoAppNavigation.kt
composable(Routes.NEW_SCREEN) {
    NewScreen(onNavigate = { route -> navController.navigate(route) })
}
```

## 🐛 Debugging Tips

### View State Values
```kotlin
val state by viewModel.state.collectAsState()
Log.d("DEBUG", "Current state: $state")
```

### Check Recompositions
Add in Compose Setup:
```kotlin
CompositionLocalProvider(
    LocalInspectionMode provides true
) {
    // Your composables
}
```

### Verify Colors
Use Color Picker in Android Studio:
- Hover over color hex code
- Click the color square to verify

## 📊 File Structure Quick Reference

```
📂 ui/
├── 📁 theme/          ← Colors, fonts, shapes
├── 📁 components/     ← Reusable UI components
└── 📁 screens/        ← Full screen implementations
     ├── login/
     ├── home/
     ├── workprogram/
     ├── calculator/
     ├── ipm/
     ├── analytics/
     └── disease/
📂 navigation/         ← Routes and navigation
📂 viewmodel/         ← State management
```

## 🔗 Important Classes

| Class | Purpose | Location |
|-------|---------|----------|
| MainActivity | App entry point | Root level |
| TomatoAppNavigation | Navigation graph | navigation/ |
| TomatoAppTheme | Theme provider | ui/theme/ |
| *Screen.kt | Screen UI | ui/screens/*/ |
| *ViewModel | State management | ui/viewmodel/ |

## 💡 Common Tasks

### Navigate to Different Screen
```kotlin
onNavigate(Routes.CALCULATOR)
```

### Update UI State
```kotlin
viewModel.updateField(newValue)
```

### Show Custom Color
```kotlin
backgroundColor = RedPrimary  // Use from theme
```

### Create New Card
```kotlin
AppCard(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(16.dp)
) {
    Text("Card content")
}
```

### Add Progress Bar
```kotlin
AppProgressBar(
    progress = 0.75f,  // 75%
    backgroundColor = GreenLight,
    fillColor = GreenAccent
)
```

## 🎯 Testing Workflow

1. **Test Theme Colors**
   - Change a color value
   - Rebuild
   - Check appearance

2. **Test Navigation**
   - Click buttons
   - Verify correct screen appears
   - Use back button

3. **Test State Updates**
   - Change input fields
   - Verify state updates reflect
   - Check ViewModel logic

## ⚠️ Common Issues & Fixes

### Gradle Sync Error
**Fix:** File → Invalidate Caches → Restart

### Compose Compilation Error
**Fix:** Check `kotlinCompilerExtensionVersion` in build.gradle.kts

### Navigation Not Working
**Fix:** Verify route string matches Routes object

### State Not Updating
**Fix:** Ensure using `collectAsState()` and calling ViewModel methods

## 📖 Documentation Files

- `IMPLEMENTATION_GUIDE.md` - Detailed architecture
- `FILES_CREATED.md` - Complete file listing
- This file - Quick reference

## 🔄 Development Workflow

```
1. Make changes to code
2. Ctrl + S (save)
3. Gradle syncs automatically
4. Run app
5. Test changes
6. Commit to version control
```

## 🚀 Performance Tips

1. Use `remember { }` for expensive computations
2. Use `LazyColumn` for long lists
3. Avoid recompositions with proper state management
4. Use `Modifier.fillMaxWidth()` instead of hardcoding widths

## 📞 Support Commands

### Clean Build
```bash
./gradlew clean build
```

### View Dependencies
```bash
./gradlew dependencies
```

### Run Tests
```bash
./gradlew test
```

---

**Ready to develop!** Start by exploring the screens and modifying them as needed.
