/**
 * InputBuffer Module - Accurate Combo Detection & Touch Command Processing
 * Manages a rolling FIFO queue of player touch & key inputs for arcade fighting engine.
 */

export class InputBuffer {
  constructor(maxBufferSize = 16, comboWindowMs = 800) {
    this.maxBufferSize = maxBufferSize;
    this.comboWindowMs = comboWindowMs;
    // Rolling queue holding input objects: { command: string, timestamp: number, type: 'direction' | 'button' }
    this.buffer = [];
    this.listeners = [];
  }

  /**
   * Push a new input command into the rolling queue.
   * Evicts oldest inputs when maxBufferSize is exceeded.
   */
  pushInput(command, type = 'button') {
    const now = performance.now();
    const inputEntry = {
      command: command.toUpperCase(),
      timestamp: now,
      type: type
    };

    this.buffer.push(inputEntry);
    if (this.buffer.length > this.maxBufferSize) {
      this.buffer.shift(); // Maintain fixed rolling queue size
    }

    this.cleanExpiredInputs(now);
    this.notifyListeners(inputEntry);
  }

  /**
   * Remove inputs older than comboWindowMs
   */
  cleanExpiredInputs(currentTime = performance.now()) {
    this.buffer = this.buffer.filter(
      entry => (currentTime - entry.timestamp) <= this.comboWindowMs
    );
  }

  /**
   * Check if a specific sequence of commands matches the recent input queue.
   * @param {Array<string>} sequence - Array of expected commands e.g. ['DOWN', 'FORWARD', 'PUNCH']
   * @param {number} maxTimeWindowMs - Optional custom window duration
   * @returns {boolean} True if combo sequence is matched
   */
  checkComboSequence(sequence, maxTimeWindowMs = this.comboWindowMs) {
    const now = performance.now();
    this.cleanExpiredInputs(now);

    if (sequence.length > this.buffer.length) return false;

    // Filter to valid window
    const recentInputs = this.buffer.filter(
      entry => (now - entry.timestamp) <= maxTimeWindowMs
    );

    if (sequence.length > recentInputs.length) return false;

    // Match suffix sequence
    let seqIndex = sequence.length - 1;
    for (let i = recentInputs.length - 1; i >= 0 && seqIndex >= 0; i--) {
      if (recentInputs[i].command === sequence[seqIndex].toUpperCase()) {
        seqIndex--;
      }
    }

    return seqIndex < 0; // All sequence elements matched in order
  }

  /**
   * Bind touch event handlers to a target HTML element (e.g. virtual D-Pad or touch canvas).
   */
  attachTouchListeners(targetElement) {
    if (!targetElement) return;

    targetElement.addEventListener('touchstart', (e) => {
      e.preventDefault();
      const touch = e.changedTouches[0];
      const command = targetElement.dataset.command || 'PUNCH';
      this.pushInput(command, 'touch');
    }, { passive: false });
  }

  /**
   * Get all currently active buffered input commands.
   */
  getRecentCommands() {
    this.cleanExpiredInputs();
    return this.buffer.map(entry => entry.command);
  }

  /**
   * Clear buffer
   */
  clear() {
    this.buffer = [];
  }

  /**
   * Subscribe to new input events
   */
  onInput(callback) {
    this.listeners.push(callback);
  }

  notifyListeners(entry) {
    this.listeners.forEach(cb => cb(entry));
  }
}

// Global exposure for non-module script usage
if (typeof window !== 'undefined') {
  window.InputBuffer = InputBuffer;
}

/**
 * Prefix Combo Trie for Web Engine HUD Tactical Prompts
 */
export class PrefixComboTrieJS {
  constructor() {
    this.root = { children: {}, isComboEnd: false, comboName: null, hint: null };
    this.initDefaultCombos();
  }

  insertCombo(sequence, comboName, hint) {
    let current = this.root;
    for (const char of sequence.toUpperCase()) {
      if (!current.children[char]) {
        current.children[char] = { children: {}, isComboEnd: false, comboName: null, hint: null };
      }
      current = current.children[char];
    }
    current.isComboEnd = true;
    current.comboName = comboName;
    current.hint = hint;
  }

  initDefaultCombos() {
    this.insertCombo('PK', 'Target Combo Alpha', 'Press KICK for 2-hit string');
    this.insertCombo('PPS', 'Special Cancel Burst', 'Cancel into SPECIAL!');
    this.insertCombo('PPK', 'Triple Ender', 'Finish with Heavy Kick!');
    this.insertCombo('DKS', 'Low Sweep Cancel', 'Cancel crouching kick!');
  }

  evaluateSequence(seqString) {
    let current = this.root;
    for (const char of seqString.toUpperCase()) {
      if (!current.children[char]) return null;
      current = current.children[char];
    }
    return current;
  }
}

if (typeof window !== 'undefined') {
  window.PrefixComboTrieJS = PrefixComboTrieJS;
}
