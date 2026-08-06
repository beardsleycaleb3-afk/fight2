package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

enum class FighterState {
    IDLE,
    WALK_FORWARD,
    WALK_BACKWARD,
    WALK,
    RUN,
    CROUCH,
    JUMP,
    JAB,
    CROSS,
    PUNCH,
    KICK,
    ROUNDHOUSE,
    HEADBUTT,
    UPPERCUT,
    SPECIAL,
    HURT,
    HITSTOP,
    FALL,
    LAND,
    BLOCK,
    WIN,
    VICTORY,
    LOSE
}

enum class FrameNamingPattern(val displayName: String, val example: String) {
    NUMERIC("Numeric (1.png)", "1.png, 2.png, 3.png"),
    ZERO_PADDED_3("3-Digit Padded (idle_000.png)", "idle_000.png, idle_001.png"),
    FRAME_PADDED_4("Frame 4-Digit (frame0001.png)", "frame0001.png, frame0002.png");

    fun formatFrame(stateName: String, frameIndex: Int): String {
        return when (this) {
            NUMERIC -> "${frameIndex + 1}.png"
            ZERO_PADDED_3 -> "${stateName.lowercase()}_${String.format("%03d", frameIndex)}.png"
            FRAME_PADDED_4 -> "frame${String.format("%04d", frameIndex + 1)}.png"
        }
    }
}

data class FrameDataInfo(
    val startup: Int,
    val active: Int,
    val recovery: Int,
    val advantageOnBlock: Int,
    val totalFrames: Int = startup + active + recovery
)

data class AttackBox(
    val bounds: Rect,
    val damage: Int,
    val hitstun: Int,
    val blockstun: Int,
    val knockbackX: Float,
    val knockbackY: Float,
    val isLow: Boolean = false,
    val isOverhead: Boolean = false
)

data class HurtBox(
    val bounds: Rect,
    val bodyPart: String = "torso"
)

data class FighterCharacter(
    val id: String,
    val name: String,
    val title: String,
    val folderRoot: String,
    val framePattern: FrameNamingPattern = FrameNamingPattern.ZERO_PADDED_3,
    val primaryColor: Color,
    val secondaryColor: Color,
    val specialMoveName: String,
    val specialEnergyCost: Int = 30,
    val maxHealth: Int = 100,
    val speed: Float = 6.0f,
    val jumpPower: Float = 18.0f
)

data class FighterInstance(
    val id: String,
    val character: FighterCharacter,
    var position: Offset,
    var velocity: Offset = Offset.Zero,
    var state: FighterState = FighterState.IDLE,
    var facingRight: Boolean = true,
    var currentFrameIndex: Int = 0,
    var stateTimer: Int = 0,
    var health: Int = 100,
    var energy: Int = 0, // 0 to 100 Super Meter
    var isGrounded: Boolean = true,
    var isCrouching: Boolean = false,
    var isBlocking: Boolean = false,
    var hitstunTimer: Int = 0,
    var blockstunTimer: Int = 0,
    var comboCount: Int = 0,
    var comboDamageTotal: Int = 0,
    var wins: Int = 0,
    val recentAttackChain: MutableList<FighterState> = mutableListOf(),
    var comboResetTimer: Int = 0
)

data class StageDefinition(
    val id: String,
    val name: String,
    val skyColor: Color,
    val horizonColor: Color,
    val floorColor: Color,
    val accentColor: Color,
    val description: String
)

enum class GameMode {
    ARCADE,
    TRAINING,
    REPLAY_VIEWER,
    HTML_ENGINE_EXPORTER
}

enum class AiDifficulty {
    EASY,
    MEDIUM,
    HARD,
    BOSS
}

data class HitParticle(
    var position: Offset,
    val velocity: Offset,
    val color: Color,
    var life: Int,
    val maxLife: Int,
    val text: String? = null,
    val size: Float = 10f,
    val isSpark: Boolean = false,
    val isRing: Boolean = false
)
