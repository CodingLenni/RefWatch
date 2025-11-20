package com.refwatch.data

import com.refwatch.presentation.model.Game
import com.refwatch.presentation.model.GameSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface GameSettingsRepository {
    val selectedGame: Flow<Game>
    suspend fun selectGame(game: Game)
}

object GameSettingsRepositoryImpl : GameSettingsRepository {
    private val _selectedGame = MutableStateFlow(GameSettings.defaultSetting)
    override val selectedGame: Flow<Game> = _selectedGame.asStateFlow()

    override suspend fun selectGame(game: Game) {
        _selectedGame.value = game
    }
}
