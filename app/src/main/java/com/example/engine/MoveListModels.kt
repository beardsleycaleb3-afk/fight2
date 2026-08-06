package com.example.engine

import androidx.compose.ui.graphics.Color

data class ComboStep(
    val state: FighterState,
    val label: String,          // e.g. "PUNCH", "KICK", "SPECIAL", "CROUCH + KICK"
    val iconText: String,       // e.g. "👊", "🦶", "💥", "⬇️🦶"
    val isCrouching: Boolean = false,
    val isJumping: Boolean = false,
    val requiresEnergy: Int = 0
)

data class FighterCombo(
    val id: String,
    val title: String,          // e.g. "Target Combo Alpha", "Special Cancel", "Low Sweep Ender"
    val category: String,       // e.g. "Basic Chain", "Special Cancel", "Aerial Chain", "Low Mixup"
    val steps: List<ComboStep>,
    val description: String,
    val totalDamageEst: Int
)

object MoveListRepository {

    fun getCombosForCharacter(character: FighterCharacter): List<FighterCombo> {
        val specialCost = character.specialEnergyCost
        val specialName = character.specialMoveName

        return when (character.id) {
            "east" -> listOf(
                FighterCombo(
                    id = "east_basic_chain",
                    title = "Dragon Twin Strike",
                    category = "Basic Chain",
                    steps = listOf(
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.KICK, "KICK", "🦶")
                    ),
                    description = "Fast 2-hit bread-and-butter ground combo.",
                    totalDamageEst = 18
                ),
                FighterCombo(
                    id = "east_fireball_cancel",
                    title = "Dragon Rising Cancel",
                    category = "Special Cancel",
                    steps = listOf(
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.SPECIAL, specialName.uppercase(), "💥", requiresEnergy = specialCost)
                    ),
                    description = "Chain two punches directly into $specialName for heavy knockdown.",
                    totalDamageEst = 36
                ),
                FighterCombo(
                    id = "east_low_sweep",
                    title = "Low Dragon Sweep",
                    category = "Low Mixup",
                    steps = listOf(
                        ComboStep(FighterState.KICK, "CROUCH KICK", "⬇️🦶", isCrouching = true),
                        ComboStep(FighterState.SPECIAL, specialName.uppercase(), "💥", requiresEnergy = specialCost)
                    ),
                    description = "Crouching sweep into special fireball launcher.",
                    totalDamageEst = 30
                ),
                FighterCombo(
                    id = "east_air_assault",
                    title = "Aerial Dragon Dive",
                    category = "Aerial Chain",
                    steps = listOf(
                        ComboStep(FighterState.KICK, "JUMP KICK", "⬆️🦶", isJumping = true),
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊")
                    ),
                    description = "Jump attack into ground combo starter.",
                    totalDamageEst = 22
                )
            )

            "flaming" -> listOf(
                FighterCombo(
                    id = "flaming_brawler_chain",
                    title = "Infernal Brawler",
                    category = "Basic Chain",
                    steps = listOf(
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.KICK, "KICK", "🦶")
                    ),
                    description = "Heavy dual-hit brawler combo.",
                    totalDamageEst = 22
                ),
                FighterCombo(
                    id = "flaming_meteor_cancel",
                    title = "Meteor Punch Impact",
                    category = "Special Cancel",
                    steps = listOf(
                        ComboStep(FighterState.KICK, "KICK", "🦶"),
                        ComboStep(FighterState.SPECIAL, specialName.uppercase(), "💥", requiresEnergy = specialCost)
                    ),
                    description = "Kick into $specialName for high burst damage.",
                    totalDamageEst = 40
                ),
                FighterCombo(
                    id = "flaming_crouch_crush",
                    title = "Flame Sweep Crush",
                    category = "Low Mixup",
                    steps = listOf(
                        ComboStep(FighterState.PUNCH, "CROUCH PUNCH", "⬇️👊", isCrouching = true),
                        ComboStep(FighterState.KICK, "KICK", "🦶")
                    ),
                    description = "Low jab into heavy kick punishment.",
                    totalDamageEst = 20
                )
            )

            "shadow" -> listOf(
                FighterCombo(
                    id = "shadow_ninja_triple",
                    title = "Shadow Triple Blade",
                    category = "Basic Chain",
                    steps = listOf(
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.KICK, "KICK", "🦶")
                    ),
                    description = "Lightning fast 3-hit assassin chain.",
                    totalDamageEst = 24
                ),
                FighterCombo(
                    id = "shadow_flash_strike",
                    title = "Shadow Flash Ender",
                    category = "Special Cancel",
                    steps = listOf(
                        ComboStep(FighterState.KICK, "KICK", "🦶"),
                        ComboStep(FighterState.SPECIAL, specialName.uppercase(), "💥", requiresEnergy = specialCost)
                    ),
                    description = "Kick canceled directly into $specialName.",
                    totalDamageEst = 34
                ),
                FighterCombo(
                    id = "shadow_air_dive",
                    title = "Aerial Katana Slash",
                    category = "Aerial Chain",
                    steps = listOf(
                        ComboStep(FighterState.KICK, "JUMP KICK", "⬆️🦶", isJumping = true),
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.KICK, "KICK", "🦶")
                    ),
                    description = "Air jump kick into full ground string.",
                    totalDamageEst = 28
                )
            )

            else -> listOf( // "cyber" and fallback
                FighterCombo(
                    id = "cyber_mech_chain",
                    title = "Photon Cannon Rush",
                    category = "Basic Chain",
                    steps = listOf(
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.KICK, "KICK", "🦶"),
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊")
                    ),
                    description = "Power punch rush string.",
                    totalDamageEst = 26
                ),
                FighterCombo(
                    id = "cyber_hyper_cannon",
                    title = "Hyper Photon Ender",
                    category = "Special Cancel",
                    steps = listOf(
                        ComboStep(FighterState.PUNCH, "PUNCH", "👊"),
                        ComboStep(FighterState.SPECIAL, specialName.uppercase(), "💥", requiresEnergy = specialCost)
                    ),
                    description = "Punch canceled into $specialName screen blast.",
                    totalDamageEst = 44
                ),
                FighterCombo(
                    id = "cyber_low_blast",
                    title = "Low Cyber Launcher",
                    category = "Low Mixup",
                    steps = listOf(
                        ComboStep(FighterState.PUNCH, "CROUCH PUNCH", "⬇️👊", isCrouching = true),
                        ComboStep(FighterState.SPECIAL, specialName.uppercase(), "💥", requiresEnergy = specialCost)
                    ),
                    description = "Low crouch jab into Hyper Photon Blast.",
                    totalDamageEst = 38
                )
            )
        }
    }
}
