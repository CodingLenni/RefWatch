package com.refwatch.presentation

import TimerViewModel
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.refwatch.presentation.screen.GAME_SETTINGS_SCREEN_PATH
import com.refwatch.presentation.screen.GameSettingsScreen
import com.refwatch.presentation.screen.TIMER_SCREEN_PATH
import com.refwatch.presentation.screen.TimerScreen
import com.refwatch.presentation.theme.RefwatchTheme
import com.refwatch.presentation.viewmodel.GameSettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val timerViewModel = TimerViewModel();
        val gameSettingsViewModel = GameSettingsViewModel();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            RefwatchTheme {
                RefwatchApp(timerViewModel, gameSettingsViewModel)
            }
        }
    }
}

@Composable
fun RefwatchApp(timerViewModel: TimerViewModel, gameSettingsViewModel: GameSettingsViewModel) {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(navController, startDestination = TIMER_SCREEN_PATH) {
        composable(TIMER_SCREEN_PATH) {
            TimerScreen(timerViewModel, navController)
        }
        composable(GAME_SETTINGS_SCREEN_PATH) {
            GameSettingsScreen(gameSettingsViewModel, navController)
        }
    }
}


