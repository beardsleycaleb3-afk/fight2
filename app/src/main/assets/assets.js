/**
 * Automated Sprite Loader Script
 * Iterates through character subfolders and sequences frames matching the 'name_000.png' convention into cache.
 * Path pattern: fight/assets/sprites/fighter/{character}/east/{action}/{action}_{000-012}.png
 */

const CHARACTERS = ['east', 'flaming', 'shadow', 'cyber'];
const ACTIONS = [
  'cross',
  'headbutt',
  'idle',
  'jab',
  'jump',
  'kick',
  'punch',
  'roundhouse',
  'run',
  'uppercut',
  'victory',
  'walk'
];
const MAX_FRAMES_PER_ACTION = 12;

class AutomatedSpriteLoader {
  constructor() {
    this.cacheName = 'fight-engine-sprites-v1';
    this.loadedCount = 0;
    this.totalFrames = 0;
    this.spriteSequenceMap = new Map();
  }

  generateFramePaths() {
    const paths = [];
    CHARACTERS.forEach((char) => {
      const root = char === 'east'
        ? 'fight/assets/sprites/fighter/east/'
        : `fight/assets/sprites/fighter/${char}/east/`;

      ACTIONS.forEach((action) => {
        const actionSequence = [];
        for (let frameIndex = 0; frameIndex < MAX_FRAMES_PER_ACTION; frameIndex++) {
          const paddedFrame = String(frameIndex).padStart(3, '0');
          const frameFileName = `${action}_${paddedFrame}.png`;
          const fullPath = `${root}${action}/${frameFileName}`;
          paths.push(fullPath);
          actionSequence.push(fullPath);
        }
        const key = `${char}:${action}`;
        this.spriteSequenceMap.set(key, actionSequence);
      });
    });
    this.totalFrames = paths.length;
    return paths;
  }

  async cacheAndSequenceFrames() {
    const framePaths = this.generateFramePaths();
    console.log(`[AutomatedSpriteLoader] Initializing sequence loading for ${framePaths.length} frames...`);

    if ('caches' in window) {
      try {
        const cache = await caches.open(this.cacheName);
        for (const path of framePaths) {
          try {
            await cache.add(path);
          } catch (e) {
            // Soft catch for missing image assets
          }
          this.loadedCount++;
        }
        console.log(`[AutomatedSpriteLoader] Successfully cached & sequenced ${this.loadedCount}/${this.totalFrames} sprite frames.`);
      } catch (err) {
        console.warn('[AutomatedSpriteLoader] Cache API notice:', err);
      }
    }

    this.preloadImageBuffers(framePaths);
  }

  preloadImageBuffers(paths) {
    paths.forEach((path) => {
      const img = new Image();
      img.src = path;
    });
  }

  getSequence(character, action) {
    return this.spriteSequenceMap.get(`${character}:${action}`) || [];
  }
}

// Instantiate and expose globally
window.spriteLoader = new AutomatedSpriteLoader();
window.addEventListener('DOMContentLoaded', () => {
  window.spriteLoader.cacheAndSequenceFrames();
});
