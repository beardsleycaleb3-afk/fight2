package com.example.engine

import java.util.ArrayDeque

/**
 * Advanced Data Structures (LUT, Arrays, Lists, Trees, Tries, Queue, Deque)
 * for indexing and resolving sprite animation folders, move links, and combat sequences.
 * Matches assets structure: fight/assets/sprites/fighter/{character}/east/{action}/
 */

// 1. PREFIX TRIE (Tree Structure) for Combo Match & Action Sequence Recognition
class ComboTrieNode(val charVal: Char? = null) {
    val children = mutableMapOf<Char, ComboTrieNode>()
    var isComboEnd: Boolean = false
    var comboName: String? = null
    var moveState: FighterState? = null
}

class MoveComboTrie {
    private val root = ComboTrieNode()

    fun insertCombo(inputSequence: String, comboName: String, resultingState: FighterState) {
        var current = root
        for (ch in inputSequence.uppercase()) {
            current = current.children.getOrPut(ch) { ComboTrieNode(ch) }
        }
        current.isComboEnd = true
        current.comboName = comboName
        current.moveState = resultingState
    }

    fun matchSequence(inputSequence: String): ComboTrieNode? {
        var current = root
        for (ch in inputSequence.uppercase()) {
            current = current.children[ch] ?: return null
        }
        return if (current.isComboEnd) current else null
    }
}

// 2. BINARY SEARCH TREE (Tree Structure) for Frame Data Range Lookups
class FrameTreeNode(
    val frameIndex: Int,
    val state: FighterState,
    val isHitActive: Boolean,
    var left: FrameTreeNode? = null,
    var right: FrameTreeNode? = null
)

class FrameDataTree {
    private var root: FrameTreeNode? = null

    fun insert(frameIndex: Int, state: FighterState, isHitActive: Boolean) {
        root = insertRec(root, frameIndex, state, isHitActive)
    }

    private fun insertRec(node: FrameTreeNode?, frameIndex: Int, state: FighterState, isHitActive: Boolean): FrameTreeNode {
        if (node == null) return FrameTreeNode(frameIndex, state, isHitActive)
        if (frameIndex < node.frameIndex) {
            node.left = insertRec(node.left, frameIndex, state, isHitActive)
        } else if (frameIndex > node.frameIndex) {
            node.right = insertRec(node.right, frameIndex, state, isHitActive)
        }
        return node
    }

    fun find(frameIndex: Int): FrameTreeNode? {
        var curr = root
        while (curr != null) {
            if (curr.frameIndex == frameIndex) return curr
            curr = if (frameIndex < curr.frameIndex) curr.left else curr.right
        }
        return null
    }
}

// 3. LOOKUP TABLE (LUT) for O(1) Sprite Path Resolution
object SpriteLookupTable {
    // LUT mapping: "characterId:stateName:frameIndex" -> Path
    private val pathLUT = mutableMapOf<String, String>()

    // Standard folder actions as seen in fight/assets/sprites/fighter/...
    val SPRITE_FOLDERS = listOf(
        "idle", "walk", "run", "jump", "jab", "cross", "punch",
        "kick", "roundhouse", "headbutt", "uppercut", "victory"
    )

    init {
        // Pre-populate LUT for default fighters (east, flaming, shadow, cyber)
        val characters = listOf("east", "flaming", "shadow", "cyber")
        for (charId in characters) {
            val rootPath = when (charId) {
                "flaming" -> "fight/assets/sprites/fighter/flaming/east/"
                "shadow" -> "fight/assets/sprites/fighter/shadow/east/"
                "cyber" -> "fight/assets/sprites/fighter/cyber/east/"
                else -> "fight/assets/sprites/fighter/east/"
            }

            for (folder in SPRITE_FOLDERS) {
                for (frame in 0..12) {
                    val lutKey = "$charId:${folder.uppercase()}:$frame"
                    val path = "$rootPath$folder/${folder}_${String.format("%03d", frame)}.png"
                    pathLUT[lutKey] = path
                }
            }
        }
    }

    fun getSpritePath(characterId: String, stateName: String, frameIndex: Int): String {
        val lutKey = "$characterId:${stateName.uppercase()}:$frameIndex"
        return pathLUT[lutKey] ?: "fight/assets/sprites/fighter/$characterId/east/${stateName.lowercase()}/${stateName.lowercase()}_000.png"
    }

    fun getAllLutEntries(): Map<String, String> = pathLUT
}

// 4. QUEUE & DEQUE (Double Ended Queue) for Command Buffers and Frame Pipeline Processing
class FighterCommandQueue {
    private val queue: ArrayDeque<String> = ArrayDeque(16)

    fun enqueueCommand(cmd: String) {
        if (queue.size >= 10) {
            queue.pollFirst() // Maintain fixed rolling window (Deque poll from head)
        }
        queue.addLast(cmd) // Enqueue at tail
    }

    fun dequeueCommand(): String? = queue.pollFirst()

    fun peekRecent(): List<String> = queue.toList()

    fun clear() {
        queue.clear()
    }
}

class FramePipelineDeque {
    private val deque: ArrayDeque<FighterState> = ArrayDeque(8)

    fun pushFront(state: FighterState) {
        deque.addFirst(state)
    }

    fun pushBack(state: FighterState) {
        deque.addLast(state)
    }

    fun popFront(): FighterState? = deque.pollFirst()

    fun popBack(): FighterState? = deque.pollLast()

    fun toList(): List<FighterState> = deque.toList()
}
