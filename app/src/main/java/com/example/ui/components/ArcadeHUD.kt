package com.example.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FighterInstance
import com.example.engine.FightingEngine
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun ArcadeHUD(
    engine: FightingEngine,
    onPauseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
    var showTacticalMoveList by remember { mutableStateOf(false) }

    fun triggerHaptic() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // TOP HUD: Health Bars, Energy Gauges, Names, Timer
        TopFightHeader(
            p1 = engine.p1,
            p2 = engine.p2,
            roundTimer = engine.roundTimer,
            onPauseClicked = onPauseClicked,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 24.dp)
        )

        // TACTICAL MOVE LIST HUD OVERLAY (Floating under Top Header)
        TacticalMoveListHUD(
            fighter = engine.p1,
            isExpanded = showTacticalMoveList,
            onToggleExpanded = { showTacticalMoveList = !showTacticalMoveList },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 82.dp)
        )

        // REAL-TIME PREFIX COMBO TRIE PROMPTS & NOTIFICATIONS (Centered under Top Header)
        PrefixComboTrieHUD(
            fighter = engine.p1,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 84.dp)
        )

        // MATCH STATUS BANNER OVERLAY (FIGHT!, K.O.!, WINNER!)
        if (engine.isGameOver || engine.matchWinnerText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = engine.matchWinnerText,
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { engine.resetMatch() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        modifier = Modifier.testTag("rematch_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rematch")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REMATCH", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TOUCH CONTROLS (BOTTOM): JOYSTICK ON LEFT, ATTACK BUTTONS ON RIGHT
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Touch D-Pad / Virtual Joystick (Left)
            TouchJoystick(
                onInputChange = { inputs ->
                    engine.p1Inputs.clear()
                    engine.p1Inputs.addAll(inputs)
                },
                onHaptic = { triggerHaptic() },
                modifier = Modifier.size(150.dp)
            )

            // Touch Action Buttons (Right)
            TouchActionButtons(
                p1Energy = engine.p1.energy,
                specialCost = engine.p1Char.specialEnergyCost,
                onButtonPress = { input ->
                    triggerHaptic()
                    engine.p1Inputs.add(input)
                },
                onButtonRelease = { input ->
                    engine.p1Inputs.remove(input)
                }
            )
        }
    }
}

@Composable
private fun TopFightHeader(
    p1: FighterInstance,
    p2: FighterInstance,
    roundTimer: Int,
    onPauseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // P1 Health & Energy Bar
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = p1.character.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (p1.wins > 0) {
                    Text(
                        text = "★".repeated(p1.wins),
                        color = Color(0xFFFFD54F),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            HealthBar(
                currentHealth = p1.health,
                maxHealth = p1.character.maxHealth,
                barColor = Color(0xFF00E676)
            )
            Spacer(modifier = Modifier.height(4.dp))
            SuperMeter(
                energy = p1.energy,
                cost = p1.character.specialEnergyCost,
                label = "P1 SUPER"
            )
        }

        // Round Timer in Center
        OnScreenMatchTimerHUD(
            roundTimer = roundTimer,
            maxTime = 99,
            onPauseClicked = onPauseClicked,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // P2 Health & Energy Bar
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (p2.wins > 0) {
                    Text(
                        text = "★".repeated(p2.wins),
                        color = Color(0xFFFFD54F),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = p2.character.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            HealthBar(
                currentHealth = p2.health,
                maxHealth = p2.character.maxHealth,
                barColor = Color(0xFFE53935),
                isReversed = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            SuperMeter(
                energy = p2.energy,
                cost = p2.character.specialEnergyCost,
                label = "P2 SUPER",
                isReversed = true
            )
        }
    }
}

@Composable
private fun HealthBar(
    currentHealth: Int,
    maxHealth: Int,
    barColor: Color,
    isReversed: Boolean = false
) {
    val fraction = (currentHealth.toFloat() / maxHealth.toFloat()).coerceIn(0f, 1f)
    val healthGradient = if (barColor == Color(0xFF00E676)) {
        Brush.horizontalGradient(listOf(Color(0xFF22C55E), Color(0xFF34D399)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFF87171)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
            .border(1.5.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .align(if (isReversed) Alignment.CenterEnd else Alignment.CenterStart)
                .background(
                    brush = healthGradient,
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
private fun SuperMeter(
    energy: Int,
    cost: Int,
    label: String,
    isReversed: Boolean = false
) {
    val fraction = (energy / 100f).coerceIn(0f, 1f)
    val isReady = energy >= cost
    val meterGradient = if (isReady) {
        Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF00E5FF)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF60A5FA)))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isReversed) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isReversed) {
            Text(
                text = if (isReady) "MAX!" else "$energy%",
                color = if (isReady) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(Color(0xFF020617), RoundedCornerShape(3.dp))
                .border(1.dp, if (isReady) Color(0xFF00E5FF) else Color(0xFF1E293B), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .align(if (isReversed) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(meterGradient, shape = RoundedCornerShape(2.dp))
            )
        }
        if (isReversed) {
            Text(
                text = if (isReady) "MAX!" else "$energy%",
                color = if (isReady) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun TouchJoystick(
    onInputChange: (Set<FightingEngine.ControlInput>) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .background(Color(0x77000000), CircleShape)
            .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        knobOffset = clampOffset(delta, maxRadius = size.width / 2.2f)
                        updateInputsFromOffset(knobOffset, onInputChange, onHaptic)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        knobOffset = clampOffset(knobOffset + dragAmount, maxRadius = size.width / 2.2f)
                        updateInputsFromOffset(knobOffset, onInputChange, onHaptic)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onInputChange(emptySet())
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onInputChange(emptySet())
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // D-Pad Cross overlay indicator
        Text(
            text = "▲\n◄   ►\n▼",
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Joystick Knob
        Box(
            modifier = Modifier
                .offset(x = (knobOffset.x / 3f).dp, y = (knobOffset.y / 3f).dp)
                .size(54.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFF00838F))
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

private fun clampOffset(offset: Offset, maxRadius: Float): Offset {
    val distance = sqrt(offset.x * offset.x + offset.y * offset.y)
    return if (distance > maxRadius) {
        Offset(offset.x / distance * maxRadius, offset.y / distance * maxRadius)
    } else {
        offset
    }
}

private fun updateInputsFromOffset(
    offset: Offset,
    onInputChange: (Set<FightingEngine.ControlInput>) -> Unit,
    onHaptic: () -> Unit
) {
    val inputs = mutableSetOf<FightingEngine.ControlInput>()
    val threshold = 18f

    if (offset.x < -threshold) inputs.add(FightingEngine.ControlInput.LEFT)
    if (offset.x > threshold) inputs.add(FightingEngine.ControlInput.RIGHT)
    if (offset.y < -threshold) inputs.add(FightingEngine.ControlInput.UP)
    if (offset.y > threshold) inputs.add(FightingEngine.ControlInput.DOWN)

    if (inputs.isNotEmpty()) onHaptic()
    onInputChange(inputs)
}

@Composable
private fun TouchActionButtons(
    p1Energy: Int,
    specialCost: Int,
    onButtonPress: (FightingEngine.ControlInput) -> Unit,
    onButtonRelease: (FightingEngine.ControlInput) -> Unit
) {
    val canSpecial = p1Energy >= specialCost

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Top Row: JUMP & SPECIAL
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton(
                label = "JMP",
                color = Color(0xFF00E676),
                input = FightingEngine.ControlInput.UP,
                onPress = onButtonPress,
                onRelease = onButtonRelease,
                tag = "jump_button"
            )
            ActionButton(
                label = "SPECIAL",
                color = if (canSpecial) Color(0xFFFF4081) else Color.Gray,
                input = FightingEngine.ControlInput.SPECIAL,
                onPress = onButtonPress,
                onRelease = onButtonRelease,
                tag = "special_button"
            )
        }
        // Bottom Row: PUNCH, KICK, BLOCK
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton(
                label = "PUNCH",
                color = Color(0xFFFFB300),
                input = FightingEngine.ControlInput.PUNCH,
                onPress = onButtonPress,
                onRelease = onButtonRelease,
                tag = "punch_button"
            )
            ActionButton(
                label = "KICK",
                color = Color(0xFFE53935),
                input = FightingEngine.ControlInput.KICK,
                onPress = onButtonPress,
                onRelease = onButtonRelease,
                tag = "kick_button"
            )
            ActionButton(
                label = "BLK",
                color = Color(0xFF7C4DFF),
                input = FightingEngine.ControlInput.BLOCK,
                onPress = onButtonPress,
                onRelease = onButtonRelease,
                tag = "block_button"
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    color: Color,
    input: FightingEngine.ControlInput,
    onPress: (FightingEngine.ControlInput) -> Unit,
    onRelease: (FightingEngine.ControlInput) -> Unit,
    tag: String
) {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .size(if (label == "SPECIAL") 62.dp else 52.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.pressed) {
                            onPress(input)
                        } else {
                            onRelease(input)
                        }
                    }
                }
            }
            .testTag(tag)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = if (label == "SPECIAL") 10.sp else 11.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OnScreenMatchTimerHUD(
    roundTimer: Int,
    maxTime: Int = 99,
    onPauseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCriticalTime = roundTimer in 1..15
    val isTimeOver = roundTimer == 0

    val infiniteTransition = rememberInfiniteTransition(label = "TimerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isCriticalTime) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val timerColor = when {
        isTimeOver -> Color(0xFFD50000)
        isCriticalTime -> Color(0xFFFF1744)
        roundTimer <= 30 -> Color(0xFFFFAB00)
        else -> Color(0xFFFFD54F)
    }

    val progress = (roundTimer.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("countdown_timer_hud")
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(pulseScale)
                .background(Color(0xFF0F0E17), CircleShape)
                .border(2.dp, timerColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                color = timerColor,
                trackColor = Color(0xFF232233),
                strokeWidth = 3.dp
            )

            Text(
                text = if (isTimeOver) "00" else String.format("%02d", roundTimer),
                color = timerColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("round_timer_text")
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        IconButton(
            onClick = onPauseClicked,
            modifier = Modifier
                .size(28.dp)
                .testTag("pause_button")
        ) {
            Icon(
                Icons.Default.Pause,
                contentDescription = "Pause Game",
                tint = Color.White
            )
        }
    }
}

private fun String.repeated(n: Int): String = buildString { repeat(n) { append(this@repeated) } }
