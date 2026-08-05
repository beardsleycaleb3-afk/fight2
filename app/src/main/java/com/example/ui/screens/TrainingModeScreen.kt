package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.engine.FighterState
import com.example.engine.FightingEngine
import com.example.engine.FolderAnimationLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingModeScreen(
    engine: FightingEngine,
    onBackToMatch: () -> Unit
) {
    var dummyMode by remember { mutableStateOf(engine.trainingDummyAction) }
    var showHitboxes by remember { mutableStateOf(engine.showHitboxesInTraining) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TRAINING MODE & FRAME DATA",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFFD54F)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToMatch) {
                        Text("◀", color = Color.White, fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D12))
            )
        },
        containerColor = Color(0xFF0D0D12)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Training Controls Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A26)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hitbox & Hurtbox Overlay",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = showHitboxes,
                            onCheckedChange = {
                                showHitboxes = it
                                engine.showHitboxesInTraining = it
                            },
                            modifier = Modifier.testTag("hitbox_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Dummy Reaction State",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("STAND", "CROUCH", "GUARD_ALL", "JUMP", "AI").forEach { mode ->
                            FilterChip(
                                selected = dummyMode == mode,
                                onClick = {
                                    dummyMode = mode
                                    engine.trainingDummyAction = mode
                                },
                                label = { Text(mode, fontSize = 10.sp) },
                                modifier = Modifier.testTag("dummy_chip_$mode")
                            )
                        }
                    }
                }
            }

            // Frame Data Table Card
            Text(
                text = "MOVE FRAME DATA TABLE",
                color = Color(0xFFFFD54F),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222233))
                            .padding(8.dp)
                    ) {
                        Text("MOVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        Text("STARTUP", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("ACTIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("RECOVER", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("ON BLOCK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
                    }

                    listOf(FighterState.PUNCH, FighterState.KICK, FighterState.SPECIAL, FighterState.BLOCK).forEach { move ->
                        val data = FolderAnimationLoader.getFrameDataInfo(move)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color(0xFF2A2A3D))
                                .padding(8.dp)
                        ) {
                            Text(move.name, color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("${data.startup}f", color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("${data.active}f", color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("${data.recovery}f", color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            val advColor = if (data.advantageOnBlock >= 0) Color(0xFF00E676) else Color(0xFFE53935)
                            Text("${if (data.advantageOnBlock > 0) "+" else ""}${data.advantageOnBlock}f", color = advColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
                        }
                    }
                }
            }
        }
    }
}
