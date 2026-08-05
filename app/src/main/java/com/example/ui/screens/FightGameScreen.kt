package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FightingEngine
import com.example.ui.components.ArcadeHUD
import com.example.ui.components.FightingCanvas
import kotlinx.coroutines.delay

@Composable
fun FightGameScreen(
    engine: FightingEngine,
    onOpenTrainingMode: () -> Unit,
    onExitToCharSelect: () -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }

    // 60 FPS Game Loop Effect
    LaunchedEffect(Unit) {
        while (true) {
            if (!isPaused) {
                engine.update()
            }
            delay(16) // ~60 FPS update interval
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main 2D Fighting Canvas Rendering Engine
        FightingCanvas(
            engine = engine,
            modifier = Modifier.fillMaxSize()
        )

        // CSS3 Touch HUD Overlay
        ArcadeHUD(
            engine = engine,
            onPauseClicked = {
                isPaused = true
                engine.isPaused = true
            },
            modifier = Modifier.fillMaxSize()
        )

        // PAUSE MENU MODAL OVERLAY
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14141F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFD54F)
                        )

                        Button(
                            onClick = {
                                isPaused = false
                                engine.isPaused = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            modifier = Modifier.fillMaxWidth().testTag("resume_button")
                        ) {
                            Text("RESUME BATTLE", fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Button(
                            onClick = {
                                engine.resetMatch()
                                isPaused = false
                                engine.isPaused = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E2E)),
                            modifier = Modifier.fillMaxWidth().testTag("restart_button")
                        ) {
                            Text("RESTART MATCH", color = Color.White)
                        }

                        Button(
                            onClick = {
                                isPaused = false
                                engine.isPaused = false
                                onOpenTrainingMode()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332042)),
                            modifier = Modifier.fillMaxWidth().testTag("training_mode_button")
                        ) {
                            Text("TRAINING / FRAME DATA", color = Color(0xFFFFD54F))
                        }

                        Button(
                            onClick = {
                                isPaused = false
                                engine.isPaused = false
                                onExitToCharSelect()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            modifier = Modifier.fillMaxWidth().testTag("exit_menu_button")
                        ) {
                            Text("CHARACTER SELECT", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
