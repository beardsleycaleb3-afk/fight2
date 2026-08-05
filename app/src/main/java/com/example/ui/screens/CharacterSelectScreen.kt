package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FighterCharacter
import com.example.engine.FolderAnimationLoader
import com.example.engine.FrameNamingPattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSelectScreen(
    selectedP1: FighterCharacter,
    selectedP2: FighterCharacter,
    onSelectP1: (FighterCharacter) -> Unit,
    onSelectP2: (FighterCharacter) -> Unit,
    onConfirmSelection: () -> Unit,
    onOpenHtmlEngine: () -> Unit,
    onOpenReplays: () -> Unit
) {
    var p1Pattern by remember { mutableStateOf(selectedP1.framePattern) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CHARACTER SELECT",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFFD54F)
                    )
                },
                actions = {
                    Button(
                        onClick = onOpenReplays,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E28)),
                        modifier = Modifier.padding(end = 8.dp).testTag("replays_button")
                    ) {
                        Text("REPLAYS", fontSize = 11.sp, color = Color(0xFF00E5FF))
                    }
                    Button(
                        onClick = onOpenHtmlEngine,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332042)),
                        modifier = Modifier.padding(end = 8.dp).testTag("html_engine_button")
                    ) {
                        Text("HTML ENGINE", fontSize = 11.sp, color = Color(0xFFFFD54F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D12))
            )
        },
        containerColor = Color(0xFF0D0D12)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Player 1 Card Preview
            CharacterPreviewCard(
                playerLabel = "P1 SELECT",
                character = selectedP1,
                accentColor = Color(0xFF00E676)
            )

            // Characters Selection Row
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CHOOSE YOUR FIGHTER",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(FolderAnimationLoader.DEFAULT_CHARACTERS) { char ->
                        val isSelected = selectedP1.id == char.id
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) char.primaryColor else Color(0xFF1E1E28))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF333344),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectP1(char) }
                                .padding(8.dp)
                                .testTag("select_char_${char.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(char.secondaryColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = char.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Folder & Frame Format Resolver Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161622), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "FOLDER FRAME PATTERN",
                    color = Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Path: ${selectedP1.folderRoot}idle/${p1Pattern.formatFrame("idle", 0)}",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrameNamingPattern.values().forEach { pattern ->
                        FilterChip(
                            selected = p1Pattern == pattern,
                            onClick = { p1Pattern = pattern },
                            label = { Text(pattern.displayName, fontSize = 10.sp) }
                        )
                    }
                }
            }

            // Start Fight Button
            Button(
                onClick = onConfirmSelection,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_fight_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENTER ARENA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CharacterPreviewCard(
    playerLabel: String,
    character: FighterCharacter,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, accentColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161620))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playerLabel,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = character.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = character.title,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SPECIAL: ${character.specialMoveName}",
                    color = Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(character.primaryColor, CircleShape)
                    .border(2.dp, character.secondaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = character.name.first().toString(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
