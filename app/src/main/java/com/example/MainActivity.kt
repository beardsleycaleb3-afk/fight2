package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FightViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D0D12)
                ) {
                    FightAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun FightAppContent(viewModel: FightViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val p1Char by viewModel.p1Character.collectAsState()
    val p2Char by viewModel.p2Character.collectAsState()
    val stage by viewModel.selectedStage.collectAsState()
    val replays by viewModel.replays.collectAsState()

    when (currentScreen) {
        AppScreen.CHARACTER_SELECT -> {
            CharacterSelectScreen(
                selectedP1 = p1Char,
                selectedP2 = p2Char,
                onSelectP1 = { viewModel.setP1Character(it) },
                onSelectP2 = { viewModel.setP2Character(it) },
                onConfirmSelection = { viewModel.navigateTo(AppScreen.STAGE_SELECT) },
                onOpenHtmlEngine = { viewModel.navigateTo(AppScreen.HTML_SINGLE_FILE_ENGINE) },
                onOpenReplays = { viewModel.navigateTo(AppScreen.REPLAY_LIST) }
            )
        }
        AppScreen.STAGE_SELECT -> {
            StageSelectScreen(
                selectedStage = stage,
                onSelectStage = { viewModel.setSelectedStage(it) },
                onConfirmStage = { viewModel.startMatch() },
                onBack = { viewModel.navigateTo(AppScreen.CHARACTER_SELECT) }
            )
        }
        AppScreen.FIGHT_MATCH -> {
            viewModel.activeEngine?.let { engine ->
                FightGameScreen(
                    engine = engine,
                    onOpenTrainingMode = { viewModel.navigateTo(AppScreen.TRAINING_MODE) },
                    onExitToCharSelect = { viewModel.navigateTo(AppScreen.CHARACTER_SELECT) }
                )
            }
        }
        AppScreen.TRAINING_MODE -> {
            viewModel.activeEngine?.let { engine ->
                TrainingModeScreen(
                    engine = engine,
                    onBackToMatch = { viewModel.navigateTo(AppScreen.FIGHT_MATCH) }
                )
            }
        }
        AppScreen.REPLAY_LIST -> {
            ReplayScreen(
                replays = replays,
                onDeleteReplay = { id -> viewModel.deleteReplay(id) },
                onBack = { viewModel.navigateTo(AppScreen.CHARACTER_SELECT) }
            )
        }
        AppScreen.HTML_SINGLE_FILE_ENGINE -> {
            SingleFileEngineScreen(
                onBackToNative = { viewModel.navigateTo(AppScreen.CHARACTER_SELECT) }
            )
        }
    }
}

