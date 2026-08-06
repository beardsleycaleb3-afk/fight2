package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*

@Composable
fun TacticalMoveListHUD(
    fighter: FighterInstance,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val combos = remember(fighter.character.id) {
        MoveListRepository.getCombosForCharacter(fighter.character)
    }

    // Determine current active or completed combo
    val recentChain = fighter.recentAttackChain
    val p1Energy = fighter.energy
    val isGrounded = fighter.isGrounded

    // Find if any combo is currently partially or fully executed
    val activeComboProgress = combos.map { combo ->
        val matchedSteps = computeMatchedSteps(combo.steps, recentChain)
        val isComplete = matchedSteps == combo.steps.size && combo.steps.isNotEmpty()
        Triple(combo, matchedSteps, isComplete)
    }

    val currentTopActiveCombo = activeComboProgress.maxByOrNull { it.second }

    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // COMPACT TOP HUD BANNER (Always visible when contracted or expanded)
            CompactComboTrackerBanner(
                fighter = fighter,
                topComboTriple = currentTopActiveCombo,
                isExpanded = isExpanded,
                onToggleExpanded = onToggleExpanded
            )

            // FULL EXPANDED TACTICAL MOVE LIST DRAWER
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .heightIn(max = 320.dp)
                        .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                        .testTag("tactical_move_list_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1A).copy(alpha = 0.94f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Header Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TACTICAL COMBO GUIDE - ${fighter.character.name.uppercase()}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(
                                onClick = onToggleExpanded,
                                modifier = Modifier.size(24.dp).testTag("close_move_list")
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // List of Combos
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(combos) { combo ->
                                val matchedCount = computeMatchedSteps(combo.steps, recentChain)
                                val isFullyExecuted = matchedCount == combo.steps.size && combo.steps.isNotEmpty()
                                val isAvailableNow = when (combo.category) {
                                    "Aerial Chain" -> !isGrounded
                                    "Special Cancel" -> p1Energy >= (combo.steps.lastOrNull()?.requiresEnergy ?: 0)
                                    else -> isGrounded
                                }

                                ComboCardItem(
                                    combo = combo,
                                    matchedCount = matchedCount,
                                    isFullyExecuted = isFullyExecuted,
                                    isAvailableNow = isAvailableNow,
                                    playerEnergy = p1Energy
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactComboTrackerBanner(
    fighter: FighterInstance,
    topComboTriple: Triple<FighterCombo, Int, Boolean>?,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        onClick = onToggleExpanded,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF141424).copy(alpha = 0.90f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (topComboTriple?.third == true) Color(0xFFFFD54F) else Color(0xFF3B3B58)
        ),
        modifier = Modifier
            .wrapContentWidth()
            .testTag("toggle_tactical_move_list_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚡ COMBOS",
                color = Color(0xFFFFD54F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            // Live active combo preview
            if (topComboTriple != null && topComboTriple.second > 0) {
                val (combo, matched, isComplete) = topComboTriple
                Box(
                    modifier = Modifier
                        .background(
                            if (isComplete) Color(0xFFFFB300).copy(alpha = 0.25f) else Color(0xFF00E676).copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isComplete) "🔥 ${combo.title} DONE!" else "${combo.title} (${matched}/${combo.steps.size})",
                        color = if (isComplete) Color(0xFFFFD54F) else Color(0xFF69F0AE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Text(
                    text = "${fighter.character.specialMoveName} [${fighter.character.specialEnergyCost}⚡]",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle Move List",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ComboCardItem(
    combo: FighterCombo,
    matchedCount: Int,
    isFullyExecuted: Boolean,
    isAvailableNow: Boolean,
    playerEnergy: Int
) {
    val borderColor = when {
        isFullyExecuted -> Color(0xFFFFD54F)
        matchedCount > 0 -> Color(0xFF00E676)
        isAvailableNow -> Color(0xFF3A3A58)
        else -> Color(0xFF222233)
    }

    val backgroundColor = when {
        isFullyExecuted -> Color(0xFF382C05)
        matchedCount > 0 -> Color(0xFF0A2B18)
        else -> Color(0xFF161626)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = combo.title,
                        color = if (isFullyExecuted) Color(0xFFFFD54F) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${combo.category}",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isFullyExecuted) {
                        Text(
                            text = "🔥 EXECUTED!",
                            color = Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (isAvailableNow) {
                        Text(
                            text = "READY",
                            color = Color(0xFF00E676),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "~${combo.totalDamageEst} DMG",
                        color = Color(0xFFFF8A80),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Step sequence row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                combo.steps.forEachIndexed { index, step ->
                    val isMatched = index < matchedCount
                    val isNextStep = index == matchedCount && (matchedCount > 0 || isAvailableNow)
                    val stepEnergyReq = step.requiresEnergy
                    val hasMeter = stepEnergyReq == 0 || playerEnergy >= stepEnergyReq

                    ComboStepBadge(
                        step = step,
                        isMatched = isMatched,
                        isNextStep = isNextStep,
                        hasMeter = hasMeter
                    )

                    if (index < combo.steps.size - 1) {
                        Text(
                            text = "➔",
                            color = if (index < matchedCount) Color(0xFF00E676) else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = combo.description,
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ComboStepBadge(
    step: ComboStep,
    isMatched: Boolean,
    isNextStep: Boolean,
    hasMeter: Boolean
) {
    val bg = when {
        isMatched -> Color(0xFF00E676)
        isNextStep -> Color(0xFFFFB300)
        else -> Color(0xFF28283B)
    }

    val textColor = when {
        isMatched -> Color.Black
        isNextStep -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .border(
                width = if (isNextStep) 1.5.dp else 0.5.dp,
                color = if (isNextStep) Color.White else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${step.iconText} ${step.label}",
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            if (step.requiresEnergy > 0) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (hasMeter) "⚡" else "🔒",
                    fontSize = 9.sp
                )
            }
        }
    }
}

private fun computeMatchedSteps(steps: List<ComboStep>, recentChain: List<FighterState>): Int {
    if (steps.isEmpty() || recentChain.isEmpty()) return 0

    var matched = 0
    // Compare recentChain sequence against steps prefix
    val maxCheck = minOf(steps.size, recentChain.size)
    for (i in 0 until maxCheck) {
        if (recentChain[recentChain.size - maxCheck + i] == steps[i].state) {
            matched++
        } else {
            // Check if last inputs match from start
            matched = 0
            for (j in 0 until minOf(steps.size, recentChain.size)) {
                if (recentChain[recentChain.size - 1 - j] == steps[j].state) {
                    matched = j + 1
                }
            }
            break
        }
    }
    return matched
}
