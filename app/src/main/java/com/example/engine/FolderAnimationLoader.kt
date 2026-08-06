package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

object FolderAnimationLoader {

    // Default built-in fighter characters matching user's exact folder specification
    val DEFAULT_CHARACTERS = listOf(
        FighterCharacter(
            id = "east",
            name = "East Guardian",
            title = "Striking Dragon Clan",
            folderRoot = "assets/sprites/fighter/east/",
            framePattern = FrameNamingPattern.ZERO_PADDED_3,
            primaryColor = Color(0xFFE53935), // Red
            secondaryColor = Color(0xFFFFB300), // Gold
            specialMoveName = "Dragon Rising Fireball",
            specialEnergyCost = 30,
            maxHealth = 100,
            speed = 6.5f
        ),
        FighterCharacter(
            id = "flaming",
            name = "Flaming East",
            title = "Infernal Flame Master",
            folderRoot = "assets/sprites/fighter/flaming/east/",
            framePattern = FrameNamingPattern.NUMERIC,
            primaryColor = Color(0xFFFF6D00), // Deep Orange
            secondaryColor = Color(0xFFD50000), // Dark Red
            specialMoveName = "Inferno Meteor Punch",
            specialEnergyCost = 35,
            maxHealth = 110,
            speed = 7.0f
        ),
        FighterCharacter(
            id = "shadow",
            name = "Shadow Blade",
            title = "Katana Assassin",
            folderRoot = "assets/sprites/fighter/shadow/east/",
            framePattern = FrameNamingPattern.FRAME_PADDED_4,
            primaryColor = Color(0xFF7C4DFF), // Purple
            secondaryColor = Color(0xFF00E676), // Neon Green
            specialMoveName = "Shadow Flash Strike",
            specialEnergyCost = 25,
            maxHealth = 90,
            speed = 8.2f
        ),
        FighterCharacter(
            id = "cyber",
            name = "Cyber V",
            title = "Neo-Metropolis Mech",
            folderRoot = "assets/sprites/fighter/cyber/east/",
            framePattern = FrameNamingPattern.ZERO_PADDED_3,
            primaryColor = Color(0xFF00E5FF), // Cyan
            secondaryColor = Color(0xFFFF4081), // Neon Pink
            specialMoveName = "Hyper Photon Cannon",
            specialEnergyCost = 40,
            maxHealth = 120,
            speed = 5.8f
        )
    )

    val DEFAULT_STAGES = listOf(
        StageDefinition(
            id = "stage1",
            name = "Neon Roof Coliseum",
            skyColor = Color(0xFF0D0221),
            horizonColor = Color(0xFF261447),
            floorColor = Color(0xFF2A004E),
            accentColor = Color(0xFFFF007F),
            description = "High-tech rooftop with dynamic neon billboards and laser grid floor."
        ),
        StageDefinition(
            id = "stage2",
            name = "Dragon Temple Sunset",
            skyColor = Color(0xFF4A0E17),
            horizonColor = Color(0xFF8C1D2F),
            floorColor = Color(0xFF2C0B12),
            accentColor = Color(0xFFFFB300),
            description = "Ancient martial arts shrine bathed in crimson sunset clouds."
        ),
        StageDefinition(
            id = "stage3",
            name = "Cyberpunk Alley",
            skyColor = Color(0xFF001219),
            horizonColor = Color(0xFF0A9396),
            floorColor = Color(0xFF005F73),
            accentColor = Color(0xFF94D2BD),
            description = "Rain-slicked city streets with glowing holo-ads and damp pavement."
        )
    )

    // Returns frame count for each fighter animation state
    fun getFrameCount(state: FighterState): Int {
        return when (state) {
            FighterState.IDLE -> 6
            FighterState.WALK_FORWARD, FighterState.WALK_BACKWARD, FighterState.WALK -> 8
            FighterState.RUN -> 8
            FighterState.CROUCH -> 4
            FighterState.JUMP -> 6
            FighterState.JAB -> 4
            FighterState.CROSS -> 5
            FighterState.PUNCH -> 5
            FighterState.KICK -> 6
            FighterState.ROUNDHOUSE -> 7
            FighterState.HEADBUTT -> 6
            FighterState.UPPERCUT -> 7
            FighterState.SPECIAL -> 8
            FighterState.HURT -> 4
            FighterState.FALL -> 5
            FighterState.LAND -> 3
            FighterState.BLOCK -> 3
            FighterState.WIN, FighterState.VICTORY -> 8
            FighterState.LOSE -> 6
        }
    }

    // Frame timing - tick delay per frame
    fun getFrameDuration(state: FighterState): Int {
        return when (state) {
            FighterState.JAB -> 2
            FighterState.CROSS -> 3
            FighterState.PUNCH -> 3
            FighterState.KICK -> 4
            FighterState.ROUNDHOUSE -> 4
            FighterState.HEADBUTT -> 4
            FighterState.UPPERCUT -> 3
            FighterState.SPECIAL -> 3
            FighterState.RUN -> 4
            FighterState.HURT -> 4
            FighterState.FALL -> 5
            FighterState.LAND -> 2
            else -> 6
        }
    }

    // Returns frame data info (startup, active, recovery) for frame data display in Training Mode
    fun getFrameDataInfo(state: FighterState): FrameDataInfo {
        return when (state) {
            FighterState.PUNCH -> FrameDataInfo(startup = 4, active = 3, recovery = 8, advantageOnBlock = +2)
            FighterState.KICK -> FrameDataInfo(startup = 7, active = 4, recovery = 12, advantageOnBlock = -1)
            FighterState.SPECIAL -> FrameDataInfo(startup = 10, active = 6, recovery = 16, advantageOnBlock = +5)
            FighterState.BLOCK -> FrameDataInfo(startup = 1, active = 99, recovery = 3, advantageOnBlock = 0)
            else -> FrameDataInfo(startup = 0, active = 0, recovery = 0, advantageOnBlock = 0)
        }
    }

    // Computes Hitbox / AttackBox relative to fighter position and facing direction
    fun getAttackBox(
        fighter: FighterInstance,
        characterWidth: Float = 90f,
        characterHeight: Float = 160f
    ): AttackBox? {
        val frame = fighter.currentFrameIndex
        val pos = fighter.position
        val isRight = fighter.facingRight
        val dir = if (isRight) 1f else -1f

        return when (fighter.state) {
            FighterState.PUNCH -> {
                // Active on frames 2 and 3
                if (frame in 1..2) {
                    val boxWidth = 60f
                    val boxHeight = 35f
                    val left = if (isRight) pos.x + 20f else pos.x - 20f - boxWidth
                    val top = pos.y - characterHeight + 40f
                    AttackBox(
                        bounds = Rect(left, top, left + boxWidth, top + boxHeight),
                        damage = 8,
                        hitstun = 14,
                        blockstun = 8,
                        knockbackX = dir * 8f,
                        knockbackY = -2f
                    )
                } else null
            }
            FighterState.KICK -> {
                // Active on frames 2, 3, 4
                if (frame in 2..3) {
                    val boxWidth = 80f
                    val boxHeight = 45f
                    val left = if (isRight) pos.x + 15f else pos.x - 15f - boxWidth
                    val top = pos.y - characterHeight + 70f
                    AttackBox(
                        bounds = Rect(left, top, left + boxWidth, top + boxHeight),
                        damage = 14,
                        hitstun = 18,
                        blockstun = 10,
                        knockbackX = dir * 14f,
                        knockbackY = -4f,
                        isLow = fighter.isCrouching
                    )
                } else null
            }
            FighterState.SPECIAL -> {
                // Active on frames 3 to 6
                if (frame in 3..5) {
                    val boxWidth = 110f
                    val boxHeight = 80f
                    val left = if (isRight) pos.x + 10f else pos.x - 10f - boxWidth
                    val top = pos.y - characterHeight + 30f
                    AttackBox(
                        bounds = Rect(left, top, left + boxWidth, top + boxHeight),
                        damage = 25,
                        hitstun = 26,
                        blockstun = 16,
                        knockbackX = dir * 24f,
                        knockbackY = -12f,
                        isOverhead = true
                    )
                } else null
            }
            else -> null
        }
    }

    // Returns body hurtbox bounding rects
    fun getHurtBoxes(
        fighter: FighterInstance,
        characterWidth: Float = 80f,
        characterHeight: Float = 160f
    ): List<HurtBox> {
        val pos = fighter.position
        val crouchFactor = if (fighter.isCrouching) 0.6f else 1.0f
        val h = characterHeight * crouchFactor
        val w = characterWidth

        val torsoLeft = pos.x - w / 2f
        val torsoTop = pos.y - h
        val headLeft = pos.x - w / 3f
        val headTop = pos.y - h - 25f * crouchFactor

        return listOf(
            HurtBox(Rect(torsoLeft, torsoTop, torsoLeft + w, pos.y), "torso"),
            HurtBox(Rect(headLeft, headTop, headLeft + w * 0.66f, torsoTop), "head")
        )
    }

    // Animation Cache storing cached sequences: Key -> AnimationSequence
    private val animationCache = mutableMapOf<String, AnimationSequence>()

    data class AnimationSequence(
        val cacheKey: String,
        val folderPath: String,
        val actionName: String,
        val framePaths: List<String>,
        val frameCount: Int
    )

    /**
     * Automated animation loader function.
     * Parses files named with the 'name_000.png' convention (e.g., 'idle_000.png', 'punch_001.png')
     * or other frame naming patterns, extracts numeric indices, sorts them sequentially,
     * and automatically sequences them into the animation cache.
     */
    fun parseAndSequenceFolder(
        folderPath: String,
        actionName: String,
        fileNames: List<String>
    ): AnimationSequence {
        val cleanAction = actionName.lowercase()
        val regexZeroPadded = Regex("""^${cleanAction}_(\d+)\.(png|jpg|webp)$""", RegexOption.IGNORE_CASE)
        val regexGenericNumber = Regex("""^(\d+)\.(png|jpg|webp)$""", RegexOption.IGNORE_CASE)
        val regexFramePrefix = Regex("""^frame(\d+)\.(png|jpg|webp)$""", RegexOption.IGNORE_CASE)

        val indexedFrames = mutableListOf<Pair<Int, String>>()

        for (fileName in fileNames) {
            val matchZeroPadded = regexZeroPadded.find(fileName)
            val matchGeneric = regexGenericNumber.find(fileName)
            val matchFrame = regexFramePrefix.find(fileName)

            val index = when {
                matchZeroPadded != null -> matchZeroPadded.groupValues[1].toIntOrNull()
                matchGeneric != null -> (matchGeneric.groupValues[1].toIntOrNull() ?: 1) - 1
                matchFrame != null -> (matchFrame.groupValues[1].toIntOrNull() ?: 1) - 1
                else -> null
            }

            if (index != null) {
                val fullPath = if (folderPath.endsWith("/")) "$folderPath$fileName" else "$folderPath/$fileName"
                indexedFrames.add(index to fullPath)
            }
        }

        // Sort sequentially by frame index
        val sortedPaths = indexedFrames.sortedBy { it.first }.map { it.second }

        val cacheKey = "${folderPath.trimEnd('/')}/$cleanAction"
        val sequence = AnimationSequence(
            cacheKey = cacheKey,
            folderPath = folderPath,
            actionName = cleanAction,
            framePaths = sortedPaths,
            frameCount = sortedPaths.size
        )

        animationCache[cacheKey] = sequence
        return sequence
    }

    /**
     * Automatically parses and pre-populates animation sequence cache for a character's
     * actions based on the standard 'name_000.png' convention.
     */
    fun loadAndCacheCharacterAnimations(character: FighterCharacter): Map<String, AnimationSequence> {
        val result = mutableMapOf<String, AnimationSequence>()
        for (state in FighterState.values()) {
            val actionName = state.name.lowercase()
            val folderPath = "${character.folderRoot}$actionName/"
            val frameCount = getFrameCount(state)

            // Generate standard candidate file names for name_000.png convention
            val candidateFileNames = (0 until frameCount).map { i ->
                character.framePattern.formatFrame(actionName, i)
            }

            val sequence = parseAndSequenceFolder(folderPath, actionName, candidateFileNames)
            result[actionName] = sequence
        }
        return result
    }

    /**
     * Retrieves a cached animation sequence for a specific character and action.
     */
    fun getCachedSequence(character: FighterCharacter, state: FighterState): AnimationSequence? {
        val actionName = state.name.lowercase()
        val cacheKey = "${character.folderRoot.trimEnd('/')}/$actionName/$actionName"
        var cached = animationCache[cacheKey]
        if (cached == null) {
            loadAndCacheCharacterAnimations(character)
            cached = animationCache[cacheKey]
        }
        return cached
    }

    /**
     * Retrieves frame asset path from animation cache.
     */
    fun getCachedFramePath(character: FighterCharacter, state: FighterState, frameIndex: Int): String {
        val sequence = getCachedSequence(character, state)
        if (sequence != null && sequence.framePaths.isNotEmpty()) {
            val safeIndex = frameIndex % sequence.framePaths.size
            return sequence.framePaths[safeIndex]
        }
        return resolveAssetPath(character, state, frameIndex)
    }

    /**
     * Clears or inspects current animation cache.
     */
    fun clearCache() {
        animationCache.clear()
    }

    fun getCacheSize(): Int = animationCache.size

    // Resolves file path string based on folder structure
    fun resolveAssetPath(character: FighterCharacter, state: FighterState, frameIndex: Int): String {
        val folder = state.name.lowercase()
        val filename = character.framePattern.formatFrame(folder, frameIndex)
        return "${character.folderRoot}$folder/$filename"
    }
}
