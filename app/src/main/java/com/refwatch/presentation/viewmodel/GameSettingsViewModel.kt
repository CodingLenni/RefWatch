package com.refwatch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refwatch.data.GameSettingsRepository
import com.refwatch.data.GameSettingsRepositoryImpl
import com.refwatch.presentation.model.Game
import com.refwatch.presentation.model.GameSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// State for the screen
data class GameSettingsState(
    val availableSettings: List<Game> = GameSettings.availableSettings,
)

class GameSettingsViewModel(
    private val gameSettingsRepository: GameSettingsRepository = GameSettingsRepositoryImpl
) : ViewModel() {

    val selectedGame: StateFlow<Game> = gameSettingsRepository.selectedGame
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GameSettings.defaultSetting
        )


    fun selectGameSetting(game: Game) {
        viewModelScope.launch {
            gameSettingsRepository.selectGame(game)
        }
    }
}