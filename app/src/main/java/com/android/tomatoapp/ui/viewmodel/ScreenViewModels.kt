package com.android.tomatoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// LOGIN SCREEN
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun signIn() {
        _state.value = _state.value.copy(isLoading = true)
        // Simulate API call
        _state.value = _state.value.copy(
            isLoading = false,
            isSignedIn = true
        )
    }

    fun signInWithGoogle() {
        _state.value = _state.value.copy(isLoading = true)
        // Simulate Google Sign In
        _state.value = _state.value.copy(
            isLoading = false,
            isSignedIn = true
        )
    }
}

// HOME SCREEN
data class HomeState(
    val userName: String = "Juan Dela Cruz",
    val userInitials: String = "JD",
    val temperature: String = "31°C",
    val feelsLike: String = "34°",
    val weather: String = "Partly Cloudy",
    val humidity: String = "72%",
    val location: String = "Batangas, PH",
    val currentStage: String = "Vegetative Stage",
    val stageProgress: Float = 0.27f, // 24/90 days
    val stageDay: Int = 24,
    val stageTotalDays: Int = 90
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        // Load home data from repository
        // For now, using default values
    }
}

// WORK PROGRAM SCREEN
data class WorkProgramState(
    val cropName: String = "Roma VF Tomato",
    val startDate: String = "March 1, 2025",
    val season: String = "Season 2025",
    val currentMonth: String = "April 2025",
    val daysActive: Int = 54,
    val expenses: String = "₱12.4k",
    val tasksDue: Int = 3,
    val calendarDays: List<CalendarDay> = emptyList()
)

data class CalendarDay(
    val day: Int,
    val state: String // "vegetative", "flowering", "fruiting", "today", "empty"
)

class WorkProgramViewModel : ViewModel() {
    private val _state = MutableStateFlow(WorkProgramState())
    val state: StateFlow<WorkProgramState> = _state.asStateFlow()

    init {
        loadCalendarData()
    }

    private fun loadCalendarData() {
        val calendarDays = generateApril2025Calendar()
        _state.value = _state.value.copy(calendarDays = calendarDays)
    }

    private fun generateApril2025Calendar(): List<CalendarDay> {
        return listOf(
            // Empty days at start
            CalendarDay(0, "empty"), CalendarDay(0, "empty"),
            // Vegetative
            CalendarDay(1, "vegetative"), CalendarDay(2, "vegetative"), CalendarDay(3, "vegetative"),
            CalendarDay(4, "vegetative"), CalendarDay(5, "vegetative"),
            CalendarDay(6, "vegetative"), CalendarDay(7, "vegetative"), CalendarDay(8, "vegetative"),
            CalendarDay(9, "vegetative"), CalendarDay(10, "vegetative"), CalendarDay(11, "vegetative"),
            CalendarDay(12, "vegetative"),
            // Flowering
            CalendarDay(13, "flowering"), CalendarDay(14, "flowering"), CalendarDay(15, "flowering"),
            CalendarDay(16, "flowering"), CalendarDay(17, "flowering"), CalendarDay(18, "flowering"),
            CalendarDay(19, "flowering"),
            CalendarDay(20, "flowering"), CalendarDay(21, "flowering"), CalendarDay(22, "flowering"),
            CalendarDay(23, "flowering"), CalendarDay(24, "flowering"), CalendarDay(25, "flowering"),
            CalendarDay(26, "flowering"),
            // Today and Fruiting
            CalendarDay(27, "today"), CalendarDay(28, "fruiting"), CalendarDay(29, "fruiting"),
            CalendarDay(30, "fruiting"),
            // Empty
            CalendarDay(0, "empty"), CalendarDay(0, "empty"), CalendarDay(0, "empty")
        )
    }

    fun previousMonth() {
        // Handle previous month
    }

    fun nextMonth() {
        // Handle next month
    }
}

// CALCULATOR SCREEN
data class CalculatorState(
    val cropName: String = "Roma VF Tomato",
    val completion: String = "78%",
    val netIncome: String = "₱84,250.00",
    val adjustedNetIncome: String = "₱65,715.00",
    val yieldPrediction: String = "32,000 kg/ha",
    val totalYield: String = "64,000 kg",
    val harvestDate: String = "Jun 8, 2025",
    val areaHectare: String = "2.0",
    val avgWeightFruit: String = "150",
    val fruitsPerPlant: String = "28",
    val marketValue: String = "35.00",
    val subTotalHarvest: String = "₱112,000.00",
    val laborExpense: String = "₱14,200",
    val materialsExpense: String = "₱8,750",
    val equipmentExpense: String = "₱3,800",
    val miscExpense: String = "₱1,000"
)

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun updateAreaHectare(value: String) {
        _state.value = _state.value.copy(areaHectare = value)
    }

    fun updateAvgWeightFruit(value: String) {
        _state.value = _state.value.copy(avgWeightFruit = value)
    }

    fun updateFruitsPerPlant(value: String) {
        _state.value = _state.value.copy(fruitsPerPlant = value)
    }

    fun updateMarketValue(value: String) {
        _state.value = _state.value.copy(marketValue = value)
    }

    fun saveToAnalytics() {
        // Save data and navigate
    }
}

// IPM SCREEN
data class IPMState(
    val actions: List<IPMAction> = emptyList()
)

data class IPMAction(
    val icon: String,
    val title: String,
    val description: String,
    val backgroundColor: String
)

class IPMViewModel : ViewModel() {
    private val _state = MutableStateFlow(IPMState())
    val state: StateFlow<IPMState> = _state.asStateFlow()

    init {
        loadIPMActions()
    }

    private fun loadIPMActions() {
        val actions = listOf(
            IPMAction("📸", "Scan Leaf", "Real-time disease detection", "blue"),
            IPMAction("🕐", "Scan History", "View past detections", "orange"),
            IPMAction("📖", "Disease Database", "Browse 40+ tomato diseases", "green")
        )
        _state.value = _state.value.copy(actions = actions)
    }
}

// ANALYTICS SCREEN
data class AnalyticsState(
    val netProfit: String = "₱84k",
    val profitTrend: String = "↑ 12% vs last",
    val completion: String = "78%",
    val seasonCount: String = "3 seasons",
    val filterViewMode: String = "Per Area",
    val filterCultivar: String = "All Varieties",
    val filterSeason: String = "2024–2025"
)

class AnalyticsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    fun updateViewMode(value: String) {
        _state.value = _state.value.copy(filterViewMode = value)
    }

    fun updateCultivar(value: String) {
        _state.value = _state.value.copy(filterCultivar = value)
    }

    fun updateSeason(value: String) {
        _state.value = _state.value.copy(filterSeason = value)
    }

    fun exportPdf() {
        // Handle export
    }

    fun exportCsv() {
        // Handle export
    }
}

// DISEASE DETAIL SCREEN
data class DiseaseDetailState(
    val diseaseName: String = "Early Blight",
    val scientificName: String = "Alternaria solani",
    val severity: String = "Moderate Severity",
    val symptoms: String = "Dark brown to black lesions with concentric rings appear on older, lower leaves first. Lesions are surrounded by yellow halos and may coalesce as the disease progresses.",
    val cause: String = "Caused by the fungus Alternaria solani. Spreads rapidly in warm, humid conditions between 24–29°C. Favored by wet foliage and poor air circulation.",
    val treatment: String = "Apply copper-based or chlorothalonil fungicides at first sign. Remove infected leaves promptly. Repeat every 7–10 days."
)

class DiseaseDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow(DiseaseDetailState())
    val state: StateFlow<DiseaseDetailState> = _state.asStateFlow()
}
