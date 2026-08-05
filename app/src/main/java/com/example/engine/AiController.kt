package com.example.engine

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.random.Random

class AiController(val difficulty: AiDifficulty) {
    private var decisionTimer = 0
    private var currentIntent: AiIntent = AiIntent.NEUTRAL

    enum class AiIntent {
        NEUTRAL,
        APPROACH,
        RETREAT,
        ATTACK_PUNCH,
        ATTACK_KICK,
        ATTACK_SPECIAL,
        BLOCK_HIGH,
        BLOCK_LOW,
        JUMP_ATTACK
    }

    fun updateIntent(
        ai: FighterInstance,
        opponent: FighterInstance,
        distanceX: Float
    ): AiIntent {
        decisionTimer++
        val interval = when (difficulty) {
            AiDifficulty.EASY -> 25
            AiDifficulty.MEDIUM -> 15
            AiDifficulty.HARD -> 8
            AiDifficulty.BOSS -> 4
        }

        if (decisionTimer % interval != 0) {
            return currentIntent
        }

        // If AI is in hitstun or blockstun, maintain state
        if (ai.hitstunTimer > 0 || ai.blockstunTimer > 0) {
            return AiIntent.NEUTRAL
        }

        val absDist = abs(distanceX)

        // Reaction logic to opponent attack
        val opponentAttacking = opponent.state == FighterState.PUNCH ||
                opponent.state == FighterState.KICK ||
                opponent.state == FighterState.SPECIAL

        val blockChance = when (difficulty) {
            AiDifficulty.EASY -> 0.2f
            AiDifficulty.MEDIUM -> 0.5f
            AiDifficulty.HARD -> 0.85f
            AiDifficulty.BOSS -> 0.98f
        }

        if (opponentAttacking && absDist < 120f && Random.nextFloat() < blockChance) {
            currentIntent = if (opponent.isCrouching) AiIntent.BLOCK_LOW else AiIntent.BLOCK_HIGH
            return currentIntent
        }

        // Offense vs Spacing logic
        if (absDist > 160f) {
            currentIntent = if (Random.nextFloat() < 0.8f) AiIntent.APPROACH else AiIntent.RETREAT
        } else {
            // Close range - choose attack or block
            val roll = Random.nextFloat()
            currentIntent = when {
                ai.energy >= 30 && roll < 0.35f -> AiIntent.ATTACK_SPECIAL
                roll < 0.65f -> AiIntent.ATTACK_PUNCH
                roll < 0.90f -> AiIntent.ATTACK_KICK
                else -> AiIntent.JUMP_ATTACK
            }
        }

        return currentIntent
    }
}
