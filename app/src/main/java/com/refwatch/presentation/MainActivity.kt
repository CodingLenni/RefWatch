package com.refwatch.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.refwatch.presentation.screen.GAME_SETTINGS_SCREEN_PATH
import com.refwatch.presentation.screen.GameSettingsScreen
import com.refwatch.presentation.screen.TIMER_SCREEN_PATH
import com.refwatch.presentation.screen.TimerScreen
import com.refwatch.presentation.theme.RefwatchTheme
import com.refwatch.presentation.viewmodel.GameSettingsViewModel
import com.refwatch.presentation.viewmodel.TimerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            RefwatchTheme {
                RefwatchApp()
            }
        }
    }
}

@Composable
fun RefwatchApp() {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(navController, startDestination = TIMER_SCREEN_PATH) {
        composable(TIMER_SCREEN_PATH) {
            TimerScreen(viewModel(), navController)
        }
        composable(GAME_SETTINGS_SCREEN_PATH) {
            GameSettingsScreen(viewModel(), navController)
        }
    }
}
