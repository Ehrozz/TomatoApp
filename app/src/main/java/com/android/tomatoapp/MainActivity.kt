package com.android.tomatoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.android.tomatoapp.navigation.TomatoAppNavigation
import com.android.tomatoapp.ui.theme.CreamBackground
import com.android.tomatoapp.ui.theme.TomatoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomatoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CreamBackground
                ) {
                    val navController = rememberNavController()
                    TomatoAppNavigation(navController = navController)
                }
            }
        }
    }
}
