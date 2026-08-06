package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.engine.AttackBox
import com.example.engine.FighterInstance
import com.example.engine.FighterState
import com.example.engine.FightingEngine
import com.example.engine.FolderAnimationLoader
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FightingCanvas(
    engine: FightingEngine,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(engine.stage.skyColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Calculate camera transform offset
            val shakeOffsetX = if (engine.screenShakeTimer > 0) (Random.nextFloat() - 0.5f) * 16f else 0f
            val shakeOffsetY = if (engine.screenShakeTimer > 0) (Random.nextFloat() - 0.5f) * 16f else 0f

            val scale = engine.cameraZoom
            val focusX = engine.cameraOffset.x
            val focusY = engine.cameraOffset.y

            val stageWidth = engine.arenaWidth
            val stageGroundY = engine.groundY

            // Draw Stage Multi-Layer Parallax Background
            drawStageBackground(engine, canvasW, canvasH, shakeOffsetX, shakeOffsetY)

            // Clip fighting arena
            clipRect {
                // Ground Floor Grid / Platform
                drawGroundFloor(engine, canvasW, stageGroundY + shakeOffsetY, stageWidth)

                // Draw P1 and P2 Fighters
                drawFighter(engine.p1, stageGroundY + shakeOffsetY, isP1 = true)
                drawFighter(engine.p2, stageGroundY + shakeOffsetY, isP1 = false)

                // Debug Hitboxes & Hurtboxes overlay (Training Mode)
                if (engine.showHitboxesInTraining || engine.gameMode == com.example.engine.GameMode.TRAINING) {
                    drawHitboxesAndHurtboxes(engine.p1)
                    drawHitboxesAndHurtboxes(engine.p2)
                }

                // Draw Hit Sparks, Shockwave Rings & Damage Text Particles
                for (p in engine.particles) {
                    val alpha = (p.life.toFloat() / p.maxLife.toFloat()).coerceIn(0f, 1f)
                    val pos = p.position + Offset(shakeOffsetX, shakeOffsetY)

                    if (p.text != null) {
                        val textResult = textMeasurer.measure(
                            text = p.text,
                            style = TextStyle(
                                color = p.color.copy(alpha = alpha),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = pos
                        )
                    } else if (p.isRing) {
                        val currentRadius = (p.size * (1f - (p.life.toFloat() / p.maxLife.toFloat()))).coerceAtLeast(4f)
                        drawCircle(
                            color = p.color.copy(alpha = alpha * 0.8f),
                            radius = currentRadius,
                            center = pos,
                            style = Stroke(width = 3.5f * alpha)
                        )
                    } else if (p.isSpark) {
                        // Directional starburst spark line
                        val tailPos = pos - (p.velocity * 1.8f * alpha)
                        drawLine(
                            color = p.color.copy(alpha = alpha),
                            start = tailPos,
                            end = pos,
                            strokeWidth = (p.size * alpha).coerceAtLeast(1.5f),
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = (p.size * 0.4f * alpha).coerceAtLeast(1f),
                            center = pos
                        )
                    } else {
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = p.size * alpha,
                            center = pos
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStageBackground(
    engine: FightingEngine,
    canvasW: Float,
    canvasH: Float,
    shakeX: Float,
    shakeY: Float
) {
    val stage = engine.stage

    // Sky Gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(stage.skyColor, stage.horizonColor)
        ),
        size = Size(canvasW, canvasH)
    )

    // Distant Cyber City Pillars / Temple Pillars
    val pillarCount = 7
    val pillarWidth = canvasW / pillarCount
    for (i in 0 until pillarCount) {
        val x = i * pillarWidth + shakeX
        val h = 200f + (sin(i.toDouble() * 1.5) * 60f).toFloat()
        drawRect(
            color = stage.horizonColor.copy(alpha = 0.6f),
            topLeft = Offset(x, canvasH * 0.45f - h + shakeY),
            size = Size(pillarWidth * 0.7f, h + canvasH * 0.2f)
        )
        // Neon accent lines
        drawLine(
            color = stage.accentColor.copy(alpha = 0.4f),
            start = Offset(x + 10f, canvasH * 0.45f - h + shakeY),
            end = Offset(x + 10f, canvasH * 0.5f + shakeY),
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawGroundFloor(
    engine: FightingEngine,
    canvasW: Float,
    groundY: Float,
    arenaWidth: Float
) {
    val stage = engine.stage

    // Arena Floor
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(stage.floorColor, stage.skyColor)
        ),
        topLeft = Offset(0f, groundY),
        size = Size(canvasW, size.height - groundY)
    )

    // Floor Glowing Boundary Grid
    drawLine(
        color = stage.accentColor,
        start = Offset(0f, groundY),
        end = Offset(canvasW, groundY),
        strokeWidth = 6f
    )

    for (x in 0..canvasW.toInt() step 60) {
        drawLine(
            color = stage.accentColor.copy(alpha = 0.25f),
            start = Offset(x.toFloat(), groundY),
            end = Offset(x.toFloat() - 30f, size.height),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawFighter(
    fighter: FighterInstance,
    groundY: Float,
    isP1: Boolean
) {
    val pos = fighter.position
    val char = fighter.character
    val primaryColor = char.primaryColor
    val secondaryColor = char.secondaryColor
    val facing = if (fighter.facingRight) 1f else -1f

    val isCrouch = fighter.isCrouching
    val crouchScaleY = if (isCrouch) 0.65f else 1.0f

    val headRadius = 22f
    val bodyHeight = 70f * crouchScaleY
    val bodyWidth = 32f

    val headCenter = Offset(pos.x, pos.y - bodyHeight - 35f)
    val bodyCenter = Offset(pos.x, pos.y - bodyHeight / 2f - 10f)

    // Resolve frame path from cached animation sequence
    val framePath = FolderAnimationLoader.getCachedFramePath(char, fighter.state, fighter.currentFrameIndex)

    // Fighter Aura & Flame Transformation on High Energy / Special
    val isPoweredUp = fighter.energy >= 40 || fighter.state == FighterState.SPECIAL || fighter.comboCount > 0
    val isSuperAwakened = fighter.energy >= 80 || fighter.state == FighterState.SPECIAL

    // Spectral Tiger Spirit / Dragon Avatar Silhouette behind Fighter on Special or High Combos
    if (isSuperAwakened) {
        val tigerCenter = bodyCenter + Offset(-20f * facing, -40f)
        // Roaring Spirit Aura Background Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF9100).copy(alpha = 0.55f), Color(0xFFFF1744).copy(alpha = 0.25f), Color.Transparent),
                center = tigerCenter,
                radius = 160f
            ),
            radius = 160f,
            center = tigerCenter
        )
        // Tiger Spirit Eyes
        drawCircle(color = Color(0xFFFFEA00), radius = 6f, center = tigerCenter + Offset(-18f * facing, -25f))
        drawCircle(color = Color(0xFFFFEA00), radius = 6f, center = tigerCenter + Offset(14f * facing, -25f))
        // Flame Tiger Crest Arc
        drawArc(
            color = Color(0xFFFFAB00).copy(alpha = 0.7f),
            startAngle = if (facing > 0) -130f else -50f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = tigerCenter - Offset(80f, 80f),
            size = Size(160f, 160f),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )
    }

    // Fiery Flame Hair Spires & Roaring Flame Aura above head
    if (isPoweredUp) {
        val flameColorPrimary = if (isSuperAwakened) Color(0xFFFF3D00) else Color(0xFFFF9100)
        val flameColorSecondary = Color(0xFFFFEA00)

        // Flaming Hair Spire 1
        val flamePath1 = Path().apply {
            moveTo(headCenter.x - 14f, headCenter.y - 12f)
            quadraticTo(headCenter.x - 22f * facing, headCenter.y - 55f, headCenter.x - 6f, headCenter.y - 70f)
            quadraticTo(headCenter.x + 8f, headCenter.y - 45f, headCenter.x + 14f, headCenter.y - 12f)
            close()
        }
        drawPath(flamePath1, brush = Brush.verticalGradient(listOf(flameColorSecondary, flameColorPrimary)))

        // Flaming Hair Spire 2 (Side Flame)
        val flamePath2 = Path().apply {
            moveTo(headCenter.x - 6f, headCenter.y - 10f)
            quadraticTo(headCenter.x + 30f * facing, headCenter.y - 48f, headCenter.x + 18f * facing, headCenter.y - 62f)
            quadraticTo(headCenter.x + 8f * facing, headCenter.y - 35f, headCenter.x - 2f, headCenter.y - 10f)
            close()
        }
        drawPath(flamePath2, brush = Brush.verticalGradient(listOf(Color.White, flameColorSecondary)))
    }

    // Shadow on ground
    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(pos.x - 40f, groundY - 10f),
        size = Size(80f, 20f)
    )

    // Fighter Aura on Special Attack
    if (fighter.state == FighterState.SPECIAL) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryColor.copy(alpha = 0.8f), Color.Transparent),
                center = bodyCenter,
                radius = 110f
            ),
            radius = 110f,
            center = bodyCenter
        )
    }

    // Block Shield
    if (fighter.state == FighterState.BLOCK) {
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.5f),
            radius = 65f,
            center = bodyCenter,
            style = Stroke(width = 6f)
        )
    }

    // Body Torso
    drawRoundRect(
        color = if (fighter.state == FighterState.HURT) Color.White else primaryColor,
        topLeft = Offset(pos.x - bodyWidth / 2f, pos.y - bodyHeight - 20f),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )

    // Tiger Stripe Power Markings on Torso
    if (isPoweredUp) {
        val stripeColor = Color(0xFFFFD54F)
        drawLine(
            color = stripeColor,
            start = Offset(pos.x - 12f, pos.y - bodyHeight - 10f),
            end = Offset(pos.x - 2f, pos.y - bodyHeight),
            strokeWidth = 3.5f
        )
        drawLine(
            color = stripeColor,
            start = Offset(pos.x + 2f, pos.y - bodyHeight + 10f),
            end = Offset(pos.x + 12f, pos.y - bodyHeight + 20f),
            strokeWidth = 3.5f
        )
    }

    // Head / Mask
    drawCircle(
        color = secondaryColor,
        radius = headRadius,
        center = headCenter
    )

    // Headband / Visor Stripe
    drawLine(
        color = Color.White,
        start = headCenter + Offset(-headRadius * 0.8f, -4f),
        end = headCenter + Offset(headRadius * 0.8f * facing, 4f),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    // Limbs / Attack Poses
    val armPath = Path()
    val legPath = Path()

    when (fighter.state) {
        FighterState.PUNCH -> {
            // Punching Arm extended forward
            val fistPos = Offset(pos.x + 85f * facing, headCenter.y + 15f)
            drawLine(
                color = secondaryColor,
                start = Offset(pos.x, headCenter.y + 15f),
                end = fistPos,
                strokeWidth = 14f,
                cap = StrokeCap.Round
            )
            // Glowing Fist
            drawCircle(
                color = Color.Yellow,
                radius = 16f,
                center = fistPos
            )
        }
        FighterState.KICK -> {
            // Kicking Leg extended
            val footPos = Offset(pos.x + 95f * facing, pos.y - 45f)
            drawLine(
                color = secondaryColor,
                start = Offset(pos.x, pos.y - 60f),
                end = footPos,
                strokeWidth = 16f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color.Red,
                radius = 18f,
                center = footPos
            )
        }
        FighterState.SPECIAL -> {
            // Special Attack Energy Slash / Blast
            val attackX = pos.x + 110f * facing
            val attackY = bodyCenter.y
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, secondaryColor, Color.Transparent),
                    center = Offset(attackX, attackY),
                    radius = 70f
                ),
                radius = 70f,
                center = Offset(attackX, attackY)
            )
        }
        else -> {
            // Default Legs standing
            drawLine(
                color = primaryColor,
                start = Offset(pos.x - 10f, pos.y - 20f),
                end = Offset(pos.x - 18f, pos.y),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = primaryColor,
                start = Offset(pos.x + 10f, pos.y - 20f),
                end = Offset(pos.x + 18f, pos.y),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawHitboxesAndHurtboxes(fighter: FighterInstance) {
    // Green Hurtboxes
    val hurtBoxes = FolderAnimationLoader.getHurtBoxes(fighter)
    for (hb in hurtBoxes) {
        drawRect(
            color = Color.Green.copy(alpha = 0.35f),
            topLeft = hb.bounds.topLeft,
            size = hb.bounds.size,
            style = Stroke(width = 3f)
        )
    }

    // Red Attack Hitboxes
    val attack = FolderAnimationLoader.getAttackBox(fighter)
    if (attack != null) {
        drawRect(
            color = Color.Red.copy(alpha = 0.5f),
            topLeft = attack.bounds.topLeft,
            size = attack.bounds.size
        )
        drawRect(
            color = Color.Red,
            topLeft = attack.bounds.topLeft,
            size = attack.bounds.size,
            style = Stroke(width = 4f)
        )
    }
}
