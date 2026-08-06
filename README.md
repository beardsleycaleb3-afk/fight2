# 🥊 2D Fighting Engine (Android & ES Modules Web Touch Engine)

An immersive 2D Fighting Game Engine built with **Jetpack Compose**, **Kotlin Coroutines**, **Canvas Rendering**, and **HTML5 Touch Web Engine**.

---

## 🌟 Key Features

1. **Touch-Only Viewport Architecture (`dvh`/`dvw` Dynamic Viewport)**
   - Designed exclusively for touch devices with dynamic touch joystick and glassmorphic HUD touch controls.
   - Zero dependency on hardware mouse or physical keyboard.

2. **Folder Animation & Naming Convention Resolver**
   - Flexible sprite loading supporting multiple standard frame naming formats:
     - **Prefix Index Zero-Padded**: `idle_000.png`, `idle_001.png`, `punch_002.png` *(Default Preferred Format)*
     - **Sequential Simple Numbering**: `1.png`, `2.png`, `3.png`
     - **Frame Index Prefix**: `frame0001.png`, `frame0002.png`
     - **Hyphenated Index**: `idle-0.png`, `idle-1.png`

3. **Fighter Health Bars & Super Energy Gauges**
   - Dynamic depletion and replenishment animations with glow shadows.
   - Dual-tier HUD with real-time Super Meter tracking special moves and energy costs.

4. **Character & Stage Selection**
   - Interactive character picker showcasing special move specs, color avatars, and frame format options.
   - Stage selector with custom sky color, ambient ground palettes, and fight arenas.

5. **Frame Data & Training Mode**
   - Real-time frame data inspector detailing Startup, Active, Recovery, and Advantage on Block.
   - Hitbox/Hurtbox visual debug overlay and configurable AI training dummy reactions.

6. **Single-File JS ES Modules Web Engine**
   - Built-in embedded HTML5/JS ES Modules engine running in a WebView.
   - Features dynamic Canvas 2D rendering, touch input handlers, and one-click copy source code.

7. **Match Replay Persistence (Room Database)**
   - Automatically records match outcomes, player statistics, duration, and maximum combo hits.

---

## 📁 Directory Structure & Frame Conventions

```
assets/
└── sprites/
    └── fighter/
        └── east/
            ├── idle/
            │   ├── idle_000.png
            │   ├── idle_001.png
            │   └── idle_002.png
            ├── walk/
            ├── punch/
            ├── kick/
            └── special/
```

### Preferred Animation Frame Naming Convention
- **Format**: `<action>_<index_000>.png`
- **Example**:
  - `idle_000.png`, `idle_001.png`, `idle_002.png`
  - `punch_000.png`, `punch_001.png`
  - `kick_000.png`, `kick_001.png`
  - `special_000.png`, `special_001.png`

---

## 🚀 Tech Stack

- **UI Framework**: Jetpack Compose (Material Design 3 Immersive Dark Theme)
- **Engine Loop**: 60 FPS Coroutine State Engine with Compose Canvas 2D
- **Database**: Room Persistence Library (`FightDatabase`)
- **Web Engine**: ES Modules / HTML5 Canvas 2D / WebView Android View
