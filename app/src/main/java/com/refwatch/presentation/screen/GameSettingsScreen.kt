package com.refwatch.presentation.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.refwatch.presentation.model.Game
import com.refwatch.presentation.model.GameSettings
import com.refwatch.presentation.viewmodel.GameSettingsViewModel

const val GAME_SETTINGS_SCREEN_PATH = "GameSettings_Screen"

@Composable
fun GameSettingsScreen(
    viewModel: GameSettingsViewModel,
    navController: NavController
) {
    val selectedGame by viewModel.selectedGame.collectAsStateWithLifecycle()

    AppScaffold {
        // ScalingLazyColumn is essential for optimized list display on Wear OS
        ScalingLazyColumn {
            item {
                ListHeader {
                    Text(
                        text = "Spielzeit",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Iterate over the available settings
            items(GameSettings.availableSettings) { gameSetting ->
                // ToggleChip is a great component for selection lists in Wear OS
                GameSettingItem(
                    game = gameSetting,
                    isSelected = gameSetting.id == selectedGame.id,
                    onSelected = {
                        viewModel.selectGameSetting(gameSetting)
                    }
                )
            }

            item {
                EdgeButton(
                    onClick = { navController.popBackStack() },
                    buttonSize = EdgeButtonSize.Medium,
                ) {
                    Text("Bestätigen")
                }
            }
        }
    }
}

// --- Item Composable ---

@Composable
fun GameSettingItem(
    game: Game,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The ToggleChip is used here to represent a selectable radio-button like option.
    SwitchButton(
        modifier = modifier.fillMaxWidth(),
        checked = isSelected, // Indicates if this is the currently selected setting
        onCheckedChange = {
            // Only trigger the selection if it's not already selected
            if (!isSelected) {
                onSelected()
            }
        },
        secondaryLabel = { Text(text = game.description) },
        label = {
            Text(
                text = "${game.secondHalf.length.inWholeMinutes} Minuten",
                style = MaterialTheme.typography.bodyLarge
            )
        },
    )
}

// --- Preview ---

@WearPreviewDevices
@Composable
fun GameSettingsScreenPreview() {
    // A mock ViewModel is often used for previews
    val navController = rememberSwipeDismissableNavController()
    GameSettingsScreen(GameSettingsViewModel(), navController)
}