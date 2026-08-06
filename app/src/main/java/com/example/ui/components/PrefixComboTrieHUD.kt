package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * On-screen HUD component that reads live input sequences from the 'Prefix Combo Trie'
 * to render real-time tactical prompts and combo success notifications during gameplay.
 */
@Composable
fun PrefixComboTrieHUD(
    fighter: FighterInstance,
    comboTrie: MoveComboTrie = remember { DefaultComboTrieFactory.createDefaultTrie() },
    modifier: Modifier = Modifier
) {
    // Build character sequence from fighter's recent attack chain
    val inputSeqString = remember(fighter.recentAttackChain.toList()) {
        fighter.recentAttackChain.mapNotNull { state ->
            when (state) {
                FighterState.PUNCH, FighterState.JAB, FighterState.CROSS -> 'P'
                FighterState.KICK, FighterState.ROUNDHOUSE, FighterState.HEADBUTT -> 'K'
                FighterState.SPECIAL, FighterState.UPPERCUT -> 'S'
                FighterState.JUMP -> 'J'
                FighterState.CROUCH -> 'D'
                else -> null
            }
        }.takeLast(4).joinToString("")
    }

    val matchedEndNode = remember(inputSeqString) {
        if (inputSeqString.isNotEmpty()) comboTrie.matchSequence(inputSeqString) else null
    }

    val matchedPrefixNode = remember(inputSeqString) {
        if (inputSeqString.isNotEmpty()) comboTrie.matchPrefix(inputSeqString) else null
    }

    val nextInputs = remember(inputSeqString) {
        if (inputSeqString.isNotEmpty()) comboTrie.getNextPossibleInputs(inputSeqString) else emptyList()
    }

    // Pulse animation for tactical prompts
    val infiniteTransition = rememberInfiniteTransition(label = "TriePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Column(
        modifier = modifier.testTag("prefix_combo_trie_hud"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. COMBO SUCCESS BANNER (Triggered on Trie end node match)
        AnimatedVisibility(
            visible = matchedEndNode != null && matchedEndNode.isComboEnd,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -15 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -15 })
        ) {
            val comboName = matchedEndNode?.comboName ?: "COMBO FINISHER"
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFFB300), Color(0xFFFF6D00))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("combo_success_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Combo Success",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "⚡ TRIE MATCH: $comboName EXECUTED!",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 2. REAL-TIME TACTICAL PROMPTS (Triggered when inputs match a Trie prefix)
        AnimatedVisibility(
            visible = matchedPrefixNode != null && nextInputs.isNotEmpty() && !matchedPrefixNode.isComboEnd,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF0F1424).copy(alpha = 0.92f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF00E676), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("tactical_prompt_hud")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Tactical Hint",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(15.dp)
                    )

                    Column {
                        Text(
                            text = "TACTICAL PROMPT: Next Move",
                            color = Color(0xFF00E676),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current: $inputSeqString ➔ Press: ",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            nextInputs.forEach { (nextChar, hint) ->
                                val (buttonLabel, badgeColor) = when (nextChar) {
                                    'P' -> "👊 PUNCH" to Color(0xFFFFB300)
                                    'K' -> "🦶 KICK" to Color(0xFFE53935)
                                    'S' -> "💥 SPECIAL" to Color(0xFFFF4081)
                                    else -> "$nextChar" to Color(0xFF00E5FF)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(badgeColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = buttonLabel,
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
