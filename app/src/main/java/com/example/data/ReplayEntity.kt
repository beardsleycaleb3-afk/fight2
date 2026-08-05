package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_replays")
data class ReplayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val p1Character: String,
    val p2Character: String,
    val stageId: String,
    val winner: String,
    val durationSeconds: Int,
    val maxCombo: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val inputLogJson: String
)

@Entity(tableName = "fighter_configs")
data class FighterConfigEntity(
    @PrimaryKey val id: String, // e.g., "east", "flaming_east", "shadow_ninja"
    val name: String,
    val rootFolder: String,
    val frameNamingFormat: String, // e.g., "1.png", "idle_000.png", "frame0001.png"
    val maxHealth: Int = 100,
    val speed: Float = 1.0f,
    val specialMoveName: String = "Special Attack"
)
