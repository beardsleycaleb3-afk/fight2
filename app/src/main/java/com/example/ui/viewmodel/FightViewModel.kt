package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FightDatabase
import com.example.data.FightRepository
import com.example.data.ReplayEntity
import com.example.engine.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    CHARACTER_SELECT,
    STAGE_SELECT,
    FIGHT_MATCH,
    TRAINING_MODE,
    REPLAY_LIST,
    HTML_SINGLE_FILE_ENGINE
}

class FightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FightRepository(FightDatabase.getDatabase(application).fightDao())

    val replays: StateFlow<List<ReplayEntity>> = repository.allReplays
        .stateInScope(emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.CHARACTER_SELECT)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _p1Character = MutableStateFlow(FolderAnimationLoader.DEFAULT_CHARACTERS[0])
    val p1Character: StateFlow<FighterCharacter> = _p1Character.asStateFlow()

    private val _p2Character = MutableStateFlow(FolderAnimationLoader.DEFAULT_CHARACTERS[1])
    val p2Character: StateFlow<FighterCharacter> = _p2Character.asStateFlow()

    private val _selectedStage = MutableStateFlow(FolderAnimationLoader.DEFAULT_STAGES[0])
    val selectedStage: StateFlow<StageDefinition> = _selectedStage.asStateFlow()

    var activeEngine: FightingEngine? = null
        private set

    fun setP1Character(char: FighterCharacter) {
        _p1Character.value = char
    }

    fun setP2Character(char: FighterCharacter) {
        _p2Character.value = char
    }

    fun setSelectedStage(stage: StageDefinition) {
        _selectedStage.value = stage
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun startMatch(mode: GameMode = GameMode.ARCADE) {
        activeEngine = FightingEngine(
            p1Char = _p1Character.value,
            p2Char = _p2Character.value,
            stage = _selectedStage.value,
            gameMode = mode
        )
        _currentScreen.value = AppScreen.FIGHT_MATCH
    }

    fun saveMatchReplay(winner: String, durationSec: Int, maxCombo: Int) {
        viewModelScope.launch {
            repository.saveReplay(
                ReplayEntity(
                    p1Character = _p1Character.value.name,
                    p2Character = _p2Character.value.name,
                    stageId = _selectedStage.value.name,
                    winner = winner,
                    durationSeconds = durationSec,
                    maxCombo = maxCombo,
                    inputLogJson = "{}"
                )
            )
        }
    }

    fun deleteReplay(id: Long) {
        viewModelScope.launch {
            repository.deleteReplay(id)
        }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInScope(initialValue: T): StateFlow<T> {
        val stateFlow = MutableStateFlow(initialValue)
        viewModelScope.launch {
            collect { stateFlow.value = it }
        }
        return stateFlow.asStateFlow()
    }
}
