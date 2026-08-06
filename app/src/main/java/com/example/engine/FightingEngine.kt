package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class FightingEngine(
    val p1Char: FighterCharacter,
    val p2Char: FighterCharacter,
    val stage: StageDefinition,
    val gameMode: GameMode = GameMode.ARCADE,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val groundY: Float = 550f,
    val arenaWidth: Float = 900f
) {
    var p1 = FighterInstance(
        id = "p1",
        character = p1Char,
        position = Offset(groundY * 0.4f, groundY),
        facingRight = true
    )

    var p2 = FighterInstance(
        id = "p2",
        character = p2Char,
        position = Offset(groundY * 1.2f, groundY),
        facingRight = false
    )

    var roundTimer = 99
    var timerTicks = 0
    var isPaused = false
    var isGameOver = false
    var matchWinnerText = ""

    var cameraOffset = Offset.Zero
    var cameraZoom = 1.0f
    var screenShakeTimer = 0
    var hitStopTimer = 0

    val particles = mutableListOf<HitParticle>()
    val aiController = AiController(aiDifficulty)

    var showHitboxesInTraining = false
    var trainingDummyAction = "STAND" // "STAND", "CROUCH", "GUARD_ALL", "JUMP", "AI"

    init {
        FolderAnimationLoader.loadAndCacheCharacterAnimations(p1Char)
        FolderAnimationLoader.loadAndCacheCharacterAnimations(p2Char)
    }

    // Inputs active for P1
    val p1Inputs = mutableSetOf<ControlInput>()

    enum class ControlInput {
        LEFT, RIGHT, UP, DOWN, PUNCH, KICK, SPECIAL, BLOCK
    }

    fun resetMatch() {
        p1 = FighterInstance(
            id = "p1",
            character = p1Char,
            position = Offset(200f, groundY),
            facingRight = true,
            health = p1Char.maxHealth,
            energy = 0,
            wins = p1.wins
        )
        p2 = FighterInstance(
            id = "p2",
            character = p2Char,
            position = Offset(650f, groundY),
            facingRight = false,
            health = p2Char.maxHealth,
            energy = 0,
            wins = p2.wins
        )
        roundTimer = 99
        timerTicks = 0
        isGameOver = false
        matchWinnerText = ""
        particles.clear()
        hitStopTimer = 0
        screenShakeTimer = 0
    }

    fun update() {
        if (isPaused || isGameOver) return

        // Hitstop Freeze Frame for punchy hit confirmations
        if (hitStopTimer > 0) {
            hitStopTimer--
            if (screenShakeTimer > 0) screenShakeTimer--
            val particleIterator = particles.iterator()
            while (particleIterator.hasNext()) {
                val p = particleIterator.next()
                p.life--
                p.position = p.position + p.velocity
                if (p.life <= 0) particleIterator.remove()
            }
            return
        }

        // Round timer countdown
        if (gameMode != GameMode.TRAINING) {
            timerTicks++
            if (timerTicks >= 60) {
                timerTicks = 0
                roundTimer = max(0, roundTimer - 1)
                if (roundTimer == 0) {
                    evaluateTimeout()
                }
            }
        }

        // Handle P1 Input Processing
        processFighterInput(p1, p1Inputs)

        // Handle P2 AI or Dummy Processing
        if (gameMode == GameMode.TRAINING && trainingDummyAction != "AI") {
            processTrainingDummy(p2, trainingDummyAction)
        } else {
            val dist = p2.position.x - p1.position.x
            val aiIntent = aiController.updateIntent(p2, p1, dist)
            processAiIntent(p2, aiIntent, dist)
        }

        // Facing direction update
        if (p1.hitstunTimer == 0 && p1.state != FighterState.SPECIAL && p1.state != FighterState.WIN && p1.state != FighterState.LOSE) {
            p1.facingRight = p1.position.x < p2.position.x
        }
        if (p2.hitstunTimer == 0 && p2.state != FighterState.SPECIAL && p2.state != FighterState.WIN && p2.state != FighterState.LOSE) {
            p2.facingRight = p2.position.x >= p1.position.x
        }

        // Physics & Animation ticks for both fighters
        updateFighterPhysicsAndState(p1)
        updateFighterPhysicsAndState(p2)

        // Fighter push collision (prevent clipping through each other)
        val fighterDist = abs(p1.position.x - p2.position.x)
        val minDistance = 70f
        if (fighterDist < minDistance) {
            val overlap = (minDistance - fighterDist) / 2f
            if (p1.position.x < p2.position.x) {
                p1.position = p1.position.copy(x = max(50f, p1.position.x - overlap))
                p2.position = p2.position.copy(x = min(arenaWidth - 50f, p2.position.x + overlap))
            } else {
                p1.position = p1.position.copy(x = min(arenaWidth - 50f, p1.position.x + overlap))
                p2.position = p2.position.copy(x = max(50f, p2.position.x - overlap))
            }
        }

        // Collision & Hitbox Detection
        checkCombatCollisions(attacker = p1, defender = p2)
        checkCombatCollisions(attacker = p2, defender = p1)

        // Screen Shake decay
        if (screenShakeTimer > 0) screenShakeTimer--

        // Particle updates
        val particleIterator = particles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.life--
            p.position = p.position + p.velocity
            if (p.life <= 0) particleIterator.remove()
        }

        // Camera calculations
        val midX = (p1.position.x + p2.position.x) / 2f
        val distance = abs(p1.position.x - p2.position.x)
        cameraOffset = Offset(midX, groundY - 100f)
        cameraZoom = (1.2f - (distance / 1200f)).coerceIn(0.85f, 1.3f)

        // Health Checks
        if (p1.health <= 0 && !isGameOver) {
            p1.health = 0
            p1.state = FighterState.LOSE
            p2.state = FighterState.WIN
            p2.wins++
            isGameOver = true
            matchWinnerText = "${p2.character.name} WINS!"
        } else if (p2.health <= 0 && !isGameOver) {
            if (gameMode == GameMode.TRAINING) {
                // Infinite health reset in training mode
                p2.health = p2.character.maxHealth
            } else {
                p2.health = 0
                p2.state = FighterState.LOSE
                p1.state = FighterState.WIN
                p1.wins++
                isGameOver = true
                matchWinnerText = "${p1.character.name} WINS!"
            }
        }
    }

    private fun processFighterInput(fighter: FighterInstance, inputs: Set<ControlInput>) {
        if (fighter.hitstunTimer > 0 || fighter.blockstunTimer > 0) return
        if (fighter.state == FighterState.HURT || fighter.state == FighterState.FALL) return
        if (fighter.state == FighterState.WIN || fighter.state == FighterState.LOSE) return

        val speed = fighter.character.speed

        // Check attacks first
        if (inputs.contains(ControlInput.SPECIAL) && fighter.energy >= fighter.character.specialEnergyCost) {
            if (fighter.state != FighterState.SPECIAL) {
                fighter.state = FighterState.SPECIAL
                fighter.currentFrameIndex = 0
                fighter.stateTimer = 0
                fighter.energy -= fighter.character.specialEnergyCost
            }
            return
        }

        if (inputs.contains(ControlInput.PUNCH) && fighter.state != FighterState.PUNCH) {
            fighter.state = FighterState.PUNCH
            fighter.currentFrameIndex = 0
            fighter.stateTimer = 0
            return
        }

        if (inputs.contains(ControlInput.KICK) && fighter.state != FighterState.KICK) {
            fighter.state = FighterState.KICK
            fighter.currentFrameIndex = 0
            fighter.stateTimer = 0
            return
        }

        // Blocking
        if (inputs.contains(ControlInput.BLOCK)) {
            fighter.isBlocking = true
            fighter.state = FighterState.BLOCK
            return
        } else {
            fighter.isBlocking = false
        }

        // Crouching
        if (inputs.contains(ControlInput.DOWN)) {
            fighter.isCrouching = true
            fighter.state = FighterState.CROUCH
            return
        } else {
            fighter.isCrouching = false
        }

        // Jump
        if (inputs.contains(ControlInput.UP) && fighter.isGrounded) {
            fighter.isGrounded = false
            fighter.velocity = fighter.velocity.copy(y = -fighter.character.jumpPower)
            fighter.state = FighterState.JUMP
            fighter.currentFrameIndex = 0
            return
        }

        // Left / Right Movement
        var moveX = 0f
        if (inputs.contains(ControlInput.LEFT)) moveX -= speed
        if (inputs.contains(ControlInput.RIGHT)) moveX += speed

        if (moveX != 0f && fighter.isGrounded) {
            fighter.velocity = fighter.velocity.copy(x = moveX)
            fighter.state = if ((moveX > 0 && fighter.facingRight) || (moveX < 0 && !fighter.facingRight)) {
                FighterState.WALK_FORWARD
            } else {
                FighterState.WALK_BACKWARD
            }
        } else if (fighter.isGrounded && fighter.state != FighterState.PUNCH && fighter.state != FighterState.KICK && fighter.state != FighterState.SPECIAL) {
            fighter.velocity = fighter.velocity.copy(x = 0f)
            fighter.state = FighterState.IDLE
        }
    }

    private fun processAiIntent(ai: FighterInstance, intent: AiController.AiIntent, distanceX: Float) {
        val inputs = mutableSetOf<ControlInput>()
        when (intent) {
            AiController.AiIntent.APPROACH -> {
                if (distanceX > 0) inputs.add(ControlInput.LEFT) else inputs.add(ControlInput.RIGHT)
            }
            AiController.AiIntent.RETREAT -> {
                if (distanceX > 0) inputs.add(ControlInput.RIGHT) else inputs.add(ControlInput.LEFT)
            }
            AiController.AiIntent.ATTACK_PUNCH -> inputs.add(ControlInput.PUNCH)
            AiController.AiIntent.ATTACK_KICK -> inputs.add(ControlInput.KICK)
            AiController.AiIntent.ATTACK_SPECIAL -> inputs.add(ControlInput.SPECIAL)
            AiController.AiIntent.BLOCK_HIGH -> inputs.add(ControlInput.BLOCK)
            AiController.AiIntent.BLOCK_LOW -> {
                inputs.add(ControlInput.BLOCK)
                inputs.add(ControlInput.DOWN)
            }
            AiController.AiIntent.JUMP_ATTACK -> {
                inputs.add(ControlInput.UP)
                inputs.add(ControlInput.PUNCH)
            }
            AiController.AiIntent.NEUTRAL -> {}
        }
        processFighterInput(ai, inputs)
    }

    private fun processTrainingDummy(dummy: FighterInstance, mode: String) {
        val inputs = mutableSetOf<ControlInput>()
        when (mode) {
            "CROUCH" -> inputs.add(ControlInput.DOWN)
            "GUARD_ALL" -> inputs.add(ControlInput.BLOCK)
            "JUMP" -> if (dummy.isGrounded) inputs.add(ControlInput.UP)
            else -> {} // STAND
        }
        processFighterInput(dummy, inputs)
    }

    private fun updateFighterPhysicsAndState(fighter: FighterInstance) {
        // 1. Apply Dynamic Gravity & Terminal Velocity
        if (!fighter.isGrounded) {
            val gravityAcc = if (fighter.velocity.y < 0) 0.75f else 0.95f // Lighter rising, punchy falling
            val newVy = (fighter.velocity.y + gravityAcc).coerceAtMost(18f)
            fighter.velocity = fighter.velocity.copy(y = newVy)
        }

        // 2. Horizontal Friction & Drag Decay
        val dragFactor = if (fighter.isGrounded) 0.82f else 0.98f
        var newVx = fighter.velocity.x * dragFactor
        if (abs(newVx) < 0.05f) newVx = 0f
        fighter.velocity = fighter.velocity.copy(x = newVx)

        // 3. Wall Collision & Wall Bounce
        var newX = fighter.position.x + fighter.velocity.x
        val minBoundary = 40f
        val maxBoundary = arenaWidth - 40f

        if (newX <= minBoundary || newX >= maxBoundary) {
            if (abs(fighter.velocity.x) > 8f && (fighter.state == FighterState.HURT || fighter.state == FighterState.FALL)) {
                // Wall Bounce on high velocity knockback!
                fighter.velocity = fighter.velocity.copy(x = -fighter.velocity.x * 0.55f)
                newX = newX.coerceIn(minBoundary, maxBoundary)
                screenShakeTimer = 6
                particles.add(
                    HitParticle(
                        position = Offset(if (newX <= minBoundary) minBoundary else maxBoundary, fighter.position.y - 80f),
                        velocity = Offset(0f, -2f),
                        color = Color(0xFFFFD54F),
                        life = 14,
                        maxLife = 14,
                        text = "WALL BOUNCE!"
                    )
                )
            } else {
                newX = newX.coerceIn(minBoundary, maxBoundary)
                fighter.velocity = fighter.velocity.copy(x = 0f)
            }
        }

        // 4. Floor Collisions & Ground Bounce
        var newY = fighter.position.y + fighter.velocity.y

        if (newY >= groundY) {
            newY = groundY
            if (!fighter.isGrounded) {
                if (fighter.velocity.y > 7f && (fighter.state == FighterState.FALL || fighter.state == FighterState.HURT)) {
                    // Ground Bounce!
                    fighter.velocity = fighter.velocity.copy(y = -fighter.velocity.y * 0.42f)
                    screenShakeTimer = 5
                    particles.add(
                        HitParticle(
                            position = Offset(newX, groundY - 10f),
                            velocity = Offset(0f, -1f),
                            color = Color(0xFF80CBC4),
                            life = 12,
                            maxLife = 12,
                            text = "SLAM!"
                        )
                    )
                } else {
                    // Solid landing
                    fighter.isGrounded = true
                    fighter.velocity = fighter.velocity.copy(y = 0f)
                    if (fighter.state == FighterState.JUMP || fighter.state == FighterState.FALL) {
                        fighter.state = FighterState.LAND
                        fighter.currentFrameIndex = 0
                    }
                }
            } else {
                fighter.velocity = fighter.velocity.copy(y = 0f)
            }
        }

        fighter.position = Offset(newX, newY)

        // 5. Hitstun & Blockstun Timers & Combo Reset Decay
        if (fighter.comboResetTimer > 0) {
            fighter.comboResetTimer--
            if (fighter.comboResetTimer == 0) {
                fighter.recentAttackChain.clear()
                fighter.comboCount = 0
                fighter.comboDamageTotal = 0
            }
        }

        if (fighter.hitstunTimer > 0) {
            fighter.hitstunTimer--
            if (fighter.hitstunTimer == 0 && fighter.isGrounded) {
                fighter.state = FighterState.IDLE
            }
        }
        if (fighter.blockstunTimer > 0) {
            fighter.blockstunTimer--
            if (fighter.blockstunTimer == 0) {
                fighter.state = FighterState.IDLE
            }
        }

        // 6. Frame Animation Ticks
        fighter.stateTimer++
        val maxFrames = FolderAnimationLoader.getFrameCount(fighter.state)
        val ticksPerFrame = FolderAnimationLoader.getFrameDuration(fighter.state)

        if (fighter.stateTimer >= ticksPerFrame) {
            fighter.stateTimer = 0
            fighter.currentFrameIndex++

            if (fighter.currentFrameIndex >= maxFrames) {
                when (fighter.state) {
                    FighterState.PUNCH, FighterState.KICK, FighterState.SPECIAL, FighterState.LAND, FighterState.HURT -> {
                        fighter.state = FighterState.IDLE
                        fighter.currentFrameIndex = 0
                    }
                    FighterState.WIN, FighterState.LOSE -> {
                        fighter.currentFrameIndex = maxFrames - 1
                    }
                    else -> {
                        fighter.currentFrameIndex = 0
                    }
                }
            }
        }
    }

    private fun checkCombatCollisions(attacker: FighterInstance, defender: FighterInstance) {
        val attackBox = FolderAnimationLoader.getAttackBox(attacker) ?: return
        val hurtBoxes = FolderAnimationLoader.getHurtBoxes(defender)

        // Check intersection
        for (hurt in hurtBoxes) {
            if (attackBox.bounds.overlaps(hurt.bounds)) {
                // Hit connects!
                handleHitImpact(attacker, defender, attackBox)
                break
            }
        }
    }

    private fun handleHitImpact(attacker: FighterInstance, defender: FighterInstance, attack: AttackBox) {
        val isBlocked = defender.isBlocking || (defender.state == FighterState.BLOCK)
        val knockbackDir = if (attacker.position.x <= defender.position.x) 1f else -1f
        val impactPoint = Offset(
            x = (attacker.position.x + defender.position.x) / 2f,
            y = defender.position.y - 100f
        )

        if (isBlocked) {
            // Blocked hit
            val blockDamage = max(1, attack.damage / 4)
            defender.health = max(0, defender.health - blockDamage)
            defender.blockstunTimer = attack.blockstun
            defender.state = FighterState.BLOCK

            val blockKnockback = knockbackDir * abs(attack.knockbackX) * 0.45f
            defender.velocity = Offset(blockKnockback, 0f)
            attacker.velocity = Offset(-knockbackDir * abs(attack.knockbackX) * 0.2f, 0f)

            // Gain small energy on block
            attacker.energy = min(100, attacker.energy + 5)
            defender.energy = min(100, defender.energy + 8)

            screenShakeTimer = 4
            hitStopTimer = 2

            // Spawn cyan block spark ring and blue sparks
            particles.add(
                HitParticle(
                    position = impactPoint,
                    velocity = Offset.Zero,
                    color = Color(0xFF00E5FF),
                    life = 10,
                    maxLife = 10,
                    size = 25f,
                    isRing = true
                )
            )
            for (i in 0 until 6) {
                val angle = (i * 60f + Random.nextFloat() * 20f) * (Math.PI.toFloat() / 180f)
                val speed = Random.nextFloat() * 6f + 3f
                particles.add(
                    HitParticle(
                        position = impactPoint,
                        velocity = Offset(kotlin.math.cos(angle) * speed, kotlin.math.sin(angle) * speed),
                        color = Color(0xFF80DEEA),
                        life = 12,
                        maxLife = 12,
                        size = Random.nextFloat() * 8f + 4f,
                        isSpark = true
                    )
                )
            }
            particles.add(
                HitParticle(
                    position = impactPoint + Offset(0f, -20f),
                    velocity = Offset(0f, -2f),
                    color = Color(0xFF00E5FF),
                    life = 14,
                    maxLife = 14,
                    text = "BLOCK!"
                )
            )
        } else {
            // Clean Hit!
            defender.health = max(0, defender.health - attack.damage)
            defender.hitstunTimer = attack.hitstun

            val hitKnockbackX = knockbackDir * abs(attack.knockbackX)
            val hitKnockbackY = attack.knockbackY

            defender.velocity = Offset(hitKnockbackX, hitKnockbackY)
            if (hitKnockbackY < -3f) {
                defender.isGrounded = false
                defender.state = FighterState.FALL
            } else {
                defender.state = FighterState.HURT
            }

            attacker.velocity = Offset(-knockbackDir * abs(attack.knockbackX) * 0.22f, 0f)

            // Combo escalation
            attacker.comboCount++
            attacker.comboDamageTotal += attack.damage
            attacker.recentAttackChain.add(attacker.state)
            attacker.comboResetTimer = 65 // 65 frame combo window
            attacker.energy = min(100, attacker.energy + 12)

            // Punchy Camera Shake & Hitstop Freeze Frame
            screenShakeTimer = (attack.damage / 2f).toInt().coerceIn(8, 22)
            hitStopTimer = if (attack.damage > 20) 6 else 4

            // 1. Expanding Shockwave Ring
            particles.add(
                HitParticle(
                    position = impactPoint,
                    velocity = Offset.Zero,
                    color = Color(0xFFFFD54F),
                    life = 14,
                    maxLife = 14,
                    size = 35f + attack.damage,
                    isRing = true
                )
            )

            // 2. Radial Starburst Hit-Sparks (8-14 sparks exploding outwards)
            val sparkCount = if (attack.damage > 20) 14 else 9
            val sparkColors = listOf(Color(0xFFFFD54F), Color(0xFFFF9100), Color(0xFFFF1744), Color(0xFF00E5FF))
            for (i in 0 until sparkCount) {
                val angle = (i * (360f / sparkCount) + Random.nextFloat() * 15f) * (Math.PI.toFloat() / 180f)
                val speed = Random.nextFloat() * 10f + 5f
                particles.add(
                    HitParticle(
                        position = impactPoint,
                        velocity = Offset(kotlin.math.cos(angle) * speed, kotlin.math.sin(angle) * speed),
                        color = sparkColors[i % sparkColors.size],
                        life = 16 + Random.nextInt(6),
                        maxLife = 22,
                        size = Random.nextFloat() * 10f + 6f,
                        isSpark = true
                    )
                )
            }

            // 3. Floating Damage Numbers & Combo Counter
            particles.add(
                HitParticle(
                    position = defender.position.copy(y = defender.position.y - 110f),
                    velocity = Offset(0f, -3.5f),
                    color = Color(0xFFFFD54F),
                    life = 22,
                    maxLife = 22,
                    text = "-${attack.damage}"
                )
            )

            if (attacker.comboCount > 1) {
                particles.add(
                    HitParticle(
                        position = attacker.position.copy(y = attacker.position.y - 140f),
                        velocity = Offset(0f, -4f),
                        color = Color(0xFFFF4081),
                        life = 26,
                        maxLife = 26,
                        text = "${attacker.comboCount} HITS COMBO!"
                    )
                )
            }
        }
    }

    private fun evaluateTimeout() {
        isGameOver = true
        when {
            p1.health > p2.health -> {
                p1.state = FighterState.WIN
                p2.state = FighterState.LOSE
                p1.wins++
                matchWinnerText = "TIME OVER - ${p1.character.name} WINS!"
            }
            p2.health > p1.health -> {
                p2.state = FighterState.WIN
                p1.state = FighterState.LOSE
                p2.wins++
                matchWinnerText = "TIME OVER - ${p2.character.name} WINS!"
            }
            else -> {
                matchWinnerText = "DRAW GAME!"
            }
        }
    }
}
