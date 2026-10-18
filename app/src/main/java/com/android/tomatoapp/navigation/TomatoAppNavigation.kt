package com.android.tomatoapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calendar
import androidx.compose.material3.Scaffold
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.tomatoapp.ui.screens.login.LoginScreen
import com.android.tomatoapp.ui.screens.home.HomeScreen
import com.android.tomatoapp.ui.screens.workprogram.WorkProgramScreen
import com.android.tomatoapp.ui.screens.calculator.CalculatorScreen
import com.android.tomatoapp.ui.screens.ipm.IPMScreen
import com.android.tomatoapp.ui.screens.analytics.AnalyticsScreen
import com.android.tomatoapp.ui.screens.disease.DiseaseDetailScreen
import com.android.tomatoapp.ui.components.AppBottomNavBar
import com.android.tomatoapp.ui.components.BottomNavItem

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val WORK_PROGRAM = "work_program"
    const val CALCULATOR = "calculator"
    const val IPM = "ipm"
    const val ANALYTICS = "analytics"
    const val DISEASE_DETAIL = "disease_detail"
}

@Composable
fun TomatoAppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Routes.LOGIN
    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, Routes.HOME),
        BottomNavItem("Program", Icons.Outlined.Calendar, Routes.WORK_PROGRAM),
        BottomNavItem("IPM", Icons.Default.Search, Routes.IPM),
        BottomNavItem("Analytics", Icons.Default.Settings, Routes.ANALYTICS)
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavBar(
                    items = bottomNavItems,
                    selectedRoute = currentRoute,
                    onItemSelected = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onSignInClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoogleLoginClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    // Handle register
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onPestScanClick = {
                    navController.navigate(Routes.IPM)
                },
                onFinanceClick = {
                    navController.navigate(Routes.CALCULATOR)
                }
            )
        }

        composable(Routes.WORK_PROGRAM) {
            WorkProgramScreen(
                onBack = {
                    navController.popBackStack()
                },
                onViewExpensesClick = {
                    navController.navigate(Routes.CALCULATOR)
                }
            )
        }

        composable(Routes.CALCULATOR) {
            CalculatorScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSaveClick = {
                    navController.navigate(Routes.ANALYTICS)
                }
            )
        }

        composable(Routes.IPM) {
            IPMScreen(
                onBack = {
                    navController.popBackStack()
                },
                onScanLeafClick = {
                    // Handle scan leaf
                },
                onScanHistoryClick = {
                    // Handle scan history
                },
                onDiseaseDatabaseClick = {
                    // Handle disease database
                }
            )
        }

        composable(Routes.ANALYTICS) {
            AnalyticsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onExportPdfClick = {
                    // Handle export PDF
                },
                onExportCsvClick = {
                    // Handle export CSV
                }
            )
        }

        composable(Routes.DISEASE_DETAIL) {
            DiseaseDetailScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        }
    }
}
