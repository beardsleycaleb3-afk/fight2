package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleFileEngineScreen(
    onBackToNative: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Live WebView Preview, 1 = Source Code Engine Inspector

    val engineHtmlSource = remember { generateSingleFileHtmlEngine() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Single-File 2D HTML5 Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = "Touch-Only • dvh/dvw Dynamic Viewport • CSS3 HUD",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToNative) {
                        Text("◀", color = Color.White, fontSize = 18.sp)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Fighting Engine index.html", engineHtmlSource)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Engine index.html copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_html_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Source Code", tint = Color(0xFF00E5FF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF0A0A0C)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Tab Selector: Live Preview vs HTML Source
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E1E24),
                contentColor = Color(0xFFFFD54F)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Live Touch Engine", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("index.html Source", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Code, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Render HTML5 Single-File Fighting Engine in WebView
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                webViewClient = WebViewClient()
                                loadDataWithBaseURL("https://game.local/", engineHtmlSource, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("webview_engine_preview")
                    )
                }
                1 -> {
                    // Render HTML Code Inspector
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .background(Color(0xFF121218), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF333344), RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = engineHtmlSource,
                            color = Color(0xFF80CBC4),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

fun generateSingleFileHtmlEngine(): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<title>2D Fighting Engine - Touch Only</title>
<style>
* { box-sizing: border-box; margin: 0; padding: 0; touch-action: none; user-select: none; -webkit-user-select: none; }
body, html { width: 100dvw; height: 100dvh; overflow: hidden; background: #000; font-family: 'Courier New', monospace; color: #fff; }
#game-container { position: relative; width: 100dvw; height: 100dvh; display: flex; flex-direction: column; justify-content: space-between; background: linear-gradient(180deg, #0f0a1e 0%, #291242 70%, #15002b 100%); }
canvas { position: absolute; top: 0; left: 0; width: 100dvw; height: 100dvh; z-index: 1; }

/* CSS3 ARCADE HUD */
#hud { position: absolute; top: 0; left: 0; width: 100dvw; padding: 12px; z-index: 10; display: flex; justify-content: space-between; align-items: flex-start; }
.fighter-hud { width: 42dvw; max-width: 280px; }
.name-tag { font-size: 12px; font-weight: 900; text-transform: uppercase; margin-bottom: 4px; color: #ffd54f; }
.bar-bg { width: 100%; height: 16px; background: #222; border: 2px solid #555; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; width: 100%; transition: width 0.1s ease; }
.p1-fill { background: linear-gradient(90deg, #00e676, #b9f6ca); }
.p2-fill { background: linear-gradient(90deg, #ff1744, #ff8a80); }
.super-bg { width: 100%; height: 8px; background: #111; border: 1px solid #00e5ff; margin-top: 4px; border-radius: 2px; }
.super-fill { height: 100%; width: 40%; background: #00e5ff; }

#timer-box { width: 44px; height: 44px; background: #1a1a24; border: 2px solid #ffd54f; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-size: 18px; font-weight: 900; color: #ffd54f; z-index: 10; }

/* TOUCH CONTROLS (NO MOUSE / KEYBOARD) */
#controls { position: absolute; bottom: 16px; left: 0; width: 100dvw; padding: 0 16px; z-index: 10; display: flex; justify-content: space-between; align-items: flex-end; }
#joystick-area { width: 120px; height: 120px; background: rgba(0, 229, 255, 0.15); border: 2px solid rgba(0, 229, 255, 0.6); border-radius: 50%; position: relative; display: flex; justify-content: center; align-items: center; }
#joystick-knob { width: 48px; height: 48px; background: radial-gradient(circle, #00e5ff, #00838f); border: 2px solid #fff; border-radius: 50%; position: absolute; }

#btn-matrix { display: grid; grid-template-columns: repeat(3, 52px); gap: 10px; }
.touch-btn { width: 52px; height: 52px; border-radius: 50%; border: none; font-family: monospace; font-weight: 900; font-size: 11px; color: #fff; display: flex; justify-content: center; align-items: center; box-shadow: 0 4px 10px rgba(0,0,0,0.5); }
.btn-punch { background: #ffb300; }
.btn-kick { background: #e53935; }
.btn-special { background: #7c4dff; }
.btn-block { background: #00e676; }
.btn-jump { background: #29b6f6; }

#combo-banner { position: absolute; top: 35%; left: 50%; transform: translate(-50%, -50%); font-size: 28px; font-weight: 900; color: #ffeb3b; text-shadow: 0 0 10px #ff5722; z-index: 20; pointer-events: none; opacity: 0; transition: opacity 0.2s ease; }
</style>
</head>
<body>
<div id="game-container">
    <canvas id="gameCanvas"></canvas>

    <div id="hud">
        <div class="fighter-hud">
            <div class="name-tag">EAST GUARDIAN</div>
            <div class="bar-bg"><div id="p1-hp" class="bar-fill p1-fill"></div></div>
            <div class="super-bg"><div id="p1-sp" class="super-fill"></div></div>
        </div>
        <div id="timer-box">99</div>
        <div class="fighter-hud" style="text-align: right;">
            <div class="name-tag">SHADOW KATANA</div>
            <div class="bar-bg"><div id="p2-hp" class="bar-fill p2-fill"></div></div>
            <div class="super-bg"><div id="p2-sp" class="super-fill" style="float: right;"></div></div>
        </div>
    </div>

    <div id="combo-banner">3 HITS!</div>

    <div id="controls">
        <div id="joystick-area">
            <div id="joystick-knob"></div>
        </div>
        <div id="btn-matrix">
            <div id="btn-jump" class="touch-btn btn-jump">JMP</div>
            <div id="btn-punch" class="touch-btn btn-punch">PNC</div>
            <div id="btn-special" class="touch-btn btn-special">SPC</div>
            <div id="btn-kick" class="touch-btn btn-kick">KCK</div>
            <div id="btn-block" class="touch-btn btn-block">BLK</div>
        </div>
    </div>
</div>

<script>
// SINGLE-FILE FIGHT ENGINE IMPLEMENTATION
const Fighters = {
    east: { root: "assets/sprites/fighter/east/" },
    flaming: { root: "assets/sprites/fighter/flaming/east/" }
};

const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

function resize() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}
window.addEventListener('resize', resize);
resize();

// Engine State
const p1 = { x: 100, y: canvas.height - 120, vx: 0, vy: 0, hp: 100, sp: 40, state: 'idle', facing: 1 };
const p2 = { x: canvas.width - 150, y: canvas.height - 120, vx: 0, vy: 0, hp: 100, sp: 20, state: 'idle', facing: -1 };

// Touch Input Handler
let joyTouchId = null;
const joyArea = document.getElementById('joystick-area');
const joyKnob = document.getElementById('joystick-knob');

joyArea.addEventListener('touchstart', (e) => {
    const touch = e.changedTouches[0];
    joyTouchId = touch.identifier;
    updateJoystick(touch);
});

joyArea.addEventListener('touchmove', (e) => {
    for (let t of e.changedTouches) {
        if (t.identifier === joyTouchId) updateJoystick(t);
    }
});

joyArea.addEventListener('touchend', (e) => {
    for (let t of e.changedTouches) {
        if (t.identifier === joyTouchId) {
            joyKnob.style.transform = `translate(0px, 0px)`;
            p1.vx = 0;
        }
    }
});

function updateJoystick(touch) {
    const rect = joyArea.getBoundingClientRect();
    const cx = rect.left + rect.width / 2;
    const cy = rect.top + rect.height / 2;
    const dx = touch.clientX - cx;
    const dy = touch.clientY - cy;
    const dist = Math.min(40, Math.hypot(dx, dy));
    const angle = Math.atan2(dy, dx);
    const kx = Math.cos(angle) * dist;
    const ky = Math.sin(angle) * dist;
    joyKnob.style.transform = "translate(" + kx + "px, " + ky + "px)";

    if (kx > 10) p1.vx = 5;
    else if (kx < -10) p1.vx = -5;
    else p1.vx = 0;

    if (ky < -20 && p1.y >= canvas.height - 120) p1.vy = -14;
}

// Touch Action Buttons
function bindTouch(id, action) {
    const el = document.getElementById(id);
    el.addEventListener('touchstart', (e) => { e.preventDefault(); action(); });
}

bindTouch('btn-punch', () => { p1.state = 'punch'; setTimeout(() => p1.state = 'idle', 300); triggerHit(); });
bindTouch('btn-kick', () => { p1.state = 'kick'; setTimeout(() => p1.state = 'idle', 400); triggerHit(); });
bindTouch('btn-special', () => { p1.state = 'special'; setTimeout(() => p1.state = 'idle', 500); triggerHit(); });
bindTouch('btn-jump', () => { if(p1.y >= canvas.height - 120) p1.vy = -14; });

function triggerHit() {
    if (Math.abs(p1.x - p2.x) < 90) {
        p2.hp = Math.max(0, p2.hp - 12);
        document.getElementById('p2-hp').style.width = p2.hp + '%';
        const banner = document.getElementById('combo-banner');
        banner.style.opacity = '1';
        setTimeout(() => banner.style.opacity = '0', 800);
    }
}

// Game Loop
function loop() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Gravity & Ground
    p1.x += p1.vx;
    p1.y += p1.vy;
    if (p1.y < canvas.height - 120) p1.vy += 0.8;
    else { p1.y = canvas.height - 120; p1.vy = 0; }

    p1.x = Math.max(40, Math.min(canvas.width - 40, p1.x));

    // Render Stage Ground
    ctx.fillStyle = '#ff007f';
    ctx.fillRect(0, canvas.height - 40, canvas.width, 4);

    // Render P1 Fighter
    ctx.fillStyle = p1.state === 'punch' ? '#ffb300' : '#00e676';
    ctx.beginPath();
    ctx.arc(p1.x, p1.y - 40, 20, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillRect(p1.x - 12, p1.y - 20, 24, 40);

    // Render P2 Fighter
    ctx.fillStyle = '#e53935';
    ctx.beginPath();
    ctx.arc(p2.x, p2.y - 40, 20, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillRect(p2.x - 12, p2.y - 20, 24, 40);

    requestAnimationFrame(loop);
}
loop();
</script>
</body>
</html>
    """.trimIndent()
}
