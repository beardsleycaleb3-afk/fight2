package com.example.engine

/**
 * Formal Finite State Machine (FSM) for Fighter Animation & Action State Management.
 * Enforces valid state transition paths (e.g., IDLE -> JUMP allowed, IDLE -> HITSTOP prohibited).
 */
object FighterAnimationFSM {

    data class RejectedTransitionLog(
        val fromState: FighterState,
        val targetState: FighterState,
        val reason: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val rejectedLogs = mutableListOf<RejectedTransitionLog>()

    // State Transition Matrix defining permissible next states for every FighterState
    private val allowedTransitions: Map<FighterState, Set<FighterState>> = mapOf(
        FighterState.IDLE to setOf(
            FighterState.WALK_FORWARD, FighterState.WALK_BACKWARD, FighterState.WALK, FighterState.RUN,
            FighterState.CROUCH, FighterState.JUMP, FighterState.JAB, FighterState.CROSS, FighterState.PUNCH,
            FighterState.KICK, FighterState.ROUNDHOUSE, FighterState.HEADBUTT, FighterState.UPPERCUT,
            FighterState.SPECIAL, FighterState.BLOCK, FighterState.HURT, FighterState.FALL,
            FighterState.WIN, FighterState.VICTORY, FighterState.LOSE
            // HITSTOP, LAND explicitly excluded from IDLE
        ),
        FighterState.WALK_FORWARD to setOf(
            FighterState.IDLE, FighterState.WALK_BACKWARD, FighterState.WALK, FighterState.RUN,
            FighterState.CROUCH, FighterState.JUMP, FighterState.PUNCH, FighterState.KICK,
            FighterState.SPECIAL, FighterState.BLOCK, FighterState.HURT, FighterState.FALL
        ),
        FighterState.WALK_BACKWARD to setOf(
            FighterState.IDLE, FighterState.WALK_FORWARD, FighterState.WALK, FighterState.RUN,
            FighterState.CROUCH, FighterState.JUMP, FighterState.PUNCH, FighterState.KICK,
            FighterState.SPECIAL, FighterState.BLOCK, FighterState.HURT, FighterState.FALL
        ),
        FighterState.WALK to setOf(
            FighterState.IDLE, FighterState.WALK_FORWARD, FighterState.WALK_BACKWARD, FighterState.RUN,
            FighterState.CROUCH, FighterState.JUMP, FighterState.PUNCH, FighterState.KICK,
            FighterState.SPECIAL, FighterState.BLOCK, FighterState.HURT, FighterState.FALL
        ),
        FighterState.RUN to setOf(
            FighterState.IDLE, FighterState.WALK_FORWARD, FighterState.CROUCH, FighterState.JUMP,
            FighterState.PUNCH, FighterState.KICK, FighterState.SPECIAL, FighterState.BLOCK, FighterState.HURT, FighterState.FALL
        ),
        FighterState.CROUCH to setOf(
            FighterState.IDLE, FighterState.PUNCH, FighterState.KICK, FighterState.SPECIAL,
            FighterState.BLOCK, FighterState.HURT, FighterState.FALL
        ),
        FighterState.JUMP to setOf(
            FighterState.LAND, FighterState.PUNCH, FighterState.KICK, FighterState.SPECIAL,
            FighterState.HURT, FighterState.FALL
        ),
        FighterState.JAB to setOf(
            FighterState.IDLE, FighterState.SPECIAL, FighterState.HURT, FighterState.HITSTOP, FighterState.FALL, FighterState.WIN
        ),
        FighterState.CROSS to setOf(
            FighterState.IDLE, FighterState.SPECIAL, FighterState.HURT, FighterState.HITSTOP, FighterState.FALL, FighterState.WIN
        ),
        FighterState.PUNCH to setOf(
            FighterState.IDLE, FighterState.SPECIAL, FighterState.HURT, FighterState.HITSTOP, FighterState.FALL, FighterState.WIN
        ),
        FighterState.KICK to setOf(
            FighterState.IDLE, FighterState.SPECIAL, FighterState.HURT, FighterState.HITSTOP, FighterState.FALL, FighterState.WIN
        ),
        FighterState.ROUNDHOUSE to setOf(
            FighterState.IDLE, FighterState.SPECIAL, FighterState.HURT, FighterState.HITSTOP, FighterState.FALL, FighterState.WIN
        ),
        FighterState.HEADBUTT to setOf(
            FighterState.IDLE, FighterState.SPECIAL, FighterState.HURT, FighterState.HITSTOP, FighterState.FALL, FighterState.WIN
        ),
        FighterState.UPPERCUT to setOf(
            FighterState.IDLE, FighterState.SPECIAL, FighterState.HURT, FighterState.HITSTOP, FighterState.FALL, FighterState.WIN
        ),
        FighterState.SPECIAL to setOf(
            FighterState.IDLE, FighterState.HURT, FighterState.HITSTOP, FighterState.FALL, FighterState.WIN
        ),
        FighterState.HURT to setOf(
            FighterState.IDLE, FighterState.FALL, FighterState.HITSTOP, FighterState.LOSE
        ),
        FighterState.HITSTOP to setOf(
            FighterState.HURT, FighterState.FALL, FighterState.IDLE, FighterState.BLOCK
        ),
        FighterState.BLOCK to setOf(
            FighterState.IDLE, FighterState.CROUCH, FighterState.HITSTOP, FighterState.HURT, FighterState.FALL
        ),
        FighterState.LAND to setOf(
            FighterState.IDLE, FighterState.WALK_FORWARD, FighterState.WALK_BACKWARD, FighterState.CROUCH, FighterState.PUNCH, FighterState.KICK
        ),
        FighterState.FALL to setOf(
            FighterState.LAND, FighterState.HURT, FighterState.HITSTOP, FighterState.LOSE
        ),
        FighterState.WIN to setOf(FighterState.IDLE),
        FighterState.VICTORY to setOf(FighterState.IDLE),
        FighterState.LOSE to setOf(FighterState.IDLE)
    )

    /**
     * Checks whether transitioning from [currentState] to [targetState] is valid according to the FSM.
     */
    fun canTransition(currentState: FighterState, targetState: FighterState): Boolean {
        if (currentState == targetState) return true
        val validNextSet = allowedTransitions[currentState] ?: return false
        return validNextSet.contains(targetState)
    }

    /**
     * Attempts to transition [fighter] to [targetState]. Returns true if successful.
     * If the transition is prohibited by FSM rules, logs the rejection and returns false.
     */
    fun transition(fighter: FighterInstance, targetState: FighterState, force: Boolean = false): Boolean {
        val currentState = fighter.state

        if (currentState == targetState) return true

        if (!force && !canTransition(currentState, targetState)) {
            val log = RejectedTransitionLog(
                fromState = currentState,
                targetState = targetState,
                reason = "FSM Prohibited: Direct transition from ${currentState.name} to ${targetState.name} is invalid."
            )
            synchronized(rejectedLogs) {
                if (rejectedLogs.size >= 20) rejectedLogs.removeAt(0)
                rejectedLogs.add(log)
            }
            return false
        }

        // Apply state change
        fighter.state = targetState
        fighter.currentFrameIndex = 0
        fighter.stateTimer = 0
        return true
    }

    /**
     * Gets set of valid destination states from [currentState].
     */
    fun getValidNextStates(currentState: FighterState): Set<FighterState> {
        return allowedTransitions[currentState] ?: emptySet()
    }

    /**
     * Gets immutable snapshot of recently rejected transitions for debugging/HUD telemetry.
     */
    fun getRejectedLogs(): List<RejectedTransitionLog> {
        synchronized(rejectedLogs) {
            return rejectedLogs.toList()
        }
    }
}
