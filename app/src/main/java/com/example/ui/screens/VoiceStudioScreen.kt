package com.example.ui.screens

import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AiCharacter
import com.example.data.model.AiVoiceProfile
import com.example.data.model.CharacterPresets
import com.example.data.model.VoicePresets
import com.example.data.service.AiAssistantService
import com.example.data.service.VoiceManager
import com.example.ui.components.AnimatedWaveform
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueDeep
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.MetallicGoldRich
import com.example.ui.theme.MidnightNavyDark
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.MidnightNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

data class VoiceExchange(
    val userSpeech: String,
    val aiResponse: String,
    val voiceName: String
)

@Composable
fun VoiceStudioScreen(
    aiService: AiAssistantService,
    voiceManager: VoiceManager,
    initialLanguage: String = "en"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedVoice by remember { mutableStateOf(VoicePresets.allVoices[0]) }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    var speechSpeedMultiplier by remember { mutableStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }

    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()
    val waveformLevels by voiceManager.waveformLevels.collectAsState()

    var statusMessage by remember { mutableStateOf("Tap the microphone orb to speak") }
    var isAiGenerating by remember { mutableStateOf(false) }

    val conversationTranscripts = remember { mutableStateListOf<VoiceExchange>() }
    var lastAiSpokenText by remember { mutableStateOf("") }

    // Pulsing animation for microphone orb
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening || isSpeaking) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    fun processVoiceInput(userQuery: String) {
        if (userQuery.isBlank()) return
        statusMessage = "AI thinking & synthesizing response..."
        isAiGenerating = true

        coroutineScope.launch {
            try {
                // Find matching character personality
                val char = CharacterPresets.allCharacters.firstOrNull { it.defaultVoiceId == selectedVoice.id }
                    ?: CharacterPresets.allCharacters.first()

                val history = conversationTranscripts.takeLast(4).flatMap {
                    listOf("user" to it.userSpeech, "ai" to it.aiResponse)
                }

                val accumulated = StringBuilder()
                aiService.streamChatResponse(
                    character = char,
                    conversationHistory = history,
                    userMessage = userQuery,
                    targetLanguage = selectedLanguage
                ).collect { chunk ->
                    accumulated.append(chunk)
                }

                val aiReply = accumulated.toString().trim()

                if (aiReply.isNotBlank()) {
                    lastAiSpokenText = aiReply
                    conversationTranscripts.add(
                        VoiceExchange(
                            userSpeech = userQuery,
                            aiResponse = aiReply,
                            voiceName = selectedVoice.name
                        )
                    )
                    statusMessage = "${selectedVoice.name} is speaking..."

                    if (!isMuted) {
                        voiceManager.speak(
                            text = aiReply,
                            voiceProfile = selectedVoice,
                            speechRateMultiplier = speechSpeedMultiplier,
                            languageCode = selectedLanguage,
                            onDone = {
                                statusMessage = "Ready for next conversation"
                            }
                        )
                    } else {
                        statusMessage = "Ready for next conversation (Audio muted)"
                    }
                } else {
                    statusMessage = "Ready for next conversation"
                }
            } catch (e: Exception) {
                Log.e("VoiceStudio", "Error in voice processing", e)
                statusMessage = "Ready for next conversation"
            } finally {
                isAiGenerating = false
            }
        }
    }

    fun startListeningFlow() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Microphone permission is required.", Toast.LENGTH_SHORT).show()
            return
        }

        voiceManager.stopSpeaking()
        statusMessage = "Listening to your voice..."

        voiceManager.startListening(
            languageCode = if (selectedLanguage == "ur") "ur-PK" else "en-US",
            onPartialResult = { partial ->
                statusMessage = "\"$partial...\""
            },
            onFinalResult = { userQuery ->
                if (userQuery.isNotBlank()) {
                    processVoiceInput(userQuery)
                } else {
                    statusMessage = "Tap mic to speak, or select a prompt below"
                }
            },
            onError = { err ->
                statusMessage = err
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListeningFlow()
        } else {
            Toast.makeText(context, "Please grant microphone permission to use Voice Studio.", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavyDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Header & Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VOICE STUDIO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Infinite Voices • Real Speech Interaction",
                        fontSize = 12.sp,
                        color = MetallicGold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MidnightNavyElevated)
                            .clickable { showLanguageDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🌐 ${selectedLanguage.uppercase()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Mute / Unmute
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            if (isMuted) voiceManager.stopSpeaking()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MidnightNavyElevated)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Mute",
                            tint = if (isMuted) Color(0xFFEF4444) else ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 10 Selectable Voice Personalities Carousel
        item {
            Column {
                Text(
                    text = "Select AI Voice Persona (${VoicePresets.allVoices.size} Voices)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSilver
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(VoicePresets.allVoices) { voice ->
                        val isSelected = voice.id == selectedVoice.id
                        GlassCard(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable {
                                    voiceManager.stopSpeaking()
                                    selectedVoice = voice
                                    // Sample preview greeting
                                    voiceManager.speak(
                                        "Hi, I am ${voice.name}. Let's create together.",
                                        voice,
                                        speechSpeedMultiplier,
                                        selectedLanguage
                                    )
                                },
                            borderGlowColor = if (isSelected) ElectricBlue else Color(0x33FFFFFF),
                            goldAccent = isSelected && voice.gender == "Female"
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = voice.avatarEmoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = voice.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MetallicGold else TextWhite
                                )
                                Text(
                                    text = "${voice.gender} • ${voice.style.split("&").firstOrNull()?.trim() ?: voice.style}",
                                    fontSize = 10.sp,
                                    color = TextSilver,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main Live Vocal Stage & Glowing Orb
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isElevated = true,
                goldAccent = isSpeaking
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Active Persona Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MidnightNavyElevated)
                            .border(1.dp, MetallicGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${selectedVoice.avatarEmoji} ${selectedVoice.name} (${selectedVoice.style})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MetallicGold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Big Glowing Microphone Interactive Orb
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(pulseScale)
                            .shadow(
                                elevation = if (isListening || isSpeaking) 30.dp else 12.dp,
                                shape = CircleShape,
                                ambientColor = if (isSpeaking) MetallicGold else ElectricBlue,
                                spotColor = if (isSpeaking) MetallicGold else ElectricBlue
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isListening) {
                                        listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                                    } else if (isSpeaking) {
                                        listOf(MetallicGoldRich, Color(0xFFB45309))
                                    } else {
                                        listOf(ElectricBlue, ElectricBlueDeep)
                                    }
                                )
                            )
                            .border(
                                2.5.dp,
                                Brush.linearGradient(listOf(Color.White, MetallicGold)),
                                CircleShape
                            )
                            .clickable {
                                if (isListening) {
                                    voiceManager.stopListening()
                                } else if (isSpeaking) {
                                    voiceManager.stopSpeaking()
                                } else {
                                    val permissionCheck = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        startListeningFlow()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("voice_studio_orb"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAiGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(50.dp),
                                color = TextWhite,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = when {
                                    isListening -> Icons.Default.Stop
                                    isSpeaking -> Icons.Default.Pause
                                    else -> Icons.Default.Mic
                                },
                                contentDescription = "Voice Action",
                                tint = TextWhite,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = statusMessage,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isListening) ElectricBlue else if (isSpeaking) MetallicGold else TextSilver,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Real Animated Audio Waveform
                    AnimatedWaveform(
                        amplitudes = waveformLevels,
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        height = 54.dp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Speed & Replay Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Replay last spoken response
                        GlowingButton(
                            text = "Replay",
                            onClick = {
                                if (lastAiSpokenText.isNotBlank()) {
                                    voiceManager.speak(
                                        text = lastAiSpokenText,
                                        voiceProfile = selectedVoice,
                                        speechRateMultiplier = speechSpeedMultiplier,
                                        languageCode = selectedLanguage
                                    )
                                } else {
                                    Toast.makeText(context, "No previous response to replay", Toast.LENGTH_SHORT).show()
                                }
                            },
                            icon = Icons.Default.Replay,
                            modifier = Modifier.weight(1f),
                            testTag = "voice_replay_button"
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Stop/Interrupt Voice
                        GlowingButton(
                            text = "Stop Audio",
                            onClick = {
                                voiceManager.stopSpeaking()
                                voiceManager.stopListening()
                                statusMessage = "Voice stopped"
                            },
                            icon = Icons.Default.Stop,
                            isGold = true,
                            modifier = Modifier.weight(1f),
                            testTag = "voice_stop_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speech Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = TextSilver,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Speed: ${String.format("%.2f", speechSpeedMultiplier)}x",
                            fontSize = 12.sp,
                            color = TextSilver
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = speechSpeedMultiplier,
                            onValueChange = { speechSpeedMultiplier = it },
                            valueRange = 0.75f..1.5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricBlue,
                                activeTrackColor = ElectricBlue,
                                inactiveTrackColor = MidnightNavyElevated
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Voice Test Prompts
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Quick Voice Prompts (Tap to Speak):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MetallicGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val samplePrompts = listOf(
                                "Tell me an epic fantasy story",
                                "Give me natural photography prompts",
                                "Tell me a hilarious joke",
                                "Inspire me with a creative design concept",
                                "What makes AI creative and unique?"
                            )
                            items(samplePrompts) { prompt ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MidnightNavyElevated)
                                        .border(1.dp, ElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            processVoiceInput(prompt)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = prompt,
                                        fontSize = 11.sp,
                                        color = TextWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Voice Transcripts List
        item {
            Text(
                text = "Live Conversation Transcripts",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        if (conversationTranscripts.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No voice exchanges yet. Tap the microphone orb above and say anything—ask creative design questions, comedy topics, or speak in Urdu, Pashto, Arabic, or English!",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(conversationTranscripts.reversed()) { exchange ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isElevated = false
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🗣️ You:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = exchange.userSpeech, fontSize = 13.sp, color = TextWhite)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "🤖 ${exchange.voiceName}:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = exchange.aiResponse, fontSize = 13.sp, color = TextSilver, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentCode = selectedLanguage,
            onSelectLanguage = { selectedLanguage = it },
            onDismiss = { showLanguageDialog = false }
        )
    }
}
