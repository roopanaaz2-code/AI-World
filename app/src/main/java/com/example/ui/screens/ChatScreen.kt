package com.example.ui.screens

import android.util.Log
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AiCharacter
import com.example.data.model.CharacterPresets
import com.example.data.model.ChatMessage
import com.example.data.model.VoicePresets
import com.example.data.repository.AppRepository
import com.example.data.service.AiAssistantService
import com.example.data.service.VoiceManager
import com.example.ui.components.GlassCard
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    initialCharacterId: String?,
    repository: AppRepository,
    aiService: AiAssistantService,
    voiceManager: VoiceManager,
    preferredLanguage: String = "en"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var activeCharacter by remember {
        mutableStateOf(CharacterPresets.getById(initialCharacterId ?: "companion"))
    }

    var inputMessage by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var currentStreamingAiText by remember { mutableStateOf("") }

    // Active session messages
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var activeChatId by remember { mutableStateOf<String?>(null) }

    // Speech-to-text state
    var isMicRecording by remember { mutableStateOf(false) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isMicRecording = true
            voiceManager.startListening(
                languageCode = if (preferredLanguage == "ur") "ur-PK" else "en-US",
                onPartialResult = { inputMessage = it },
                onFinalResult = { text ->
                    isMicRecording = false
                    if (text.isNotBlank()) {
                        inputMessage = text
                    }
                },
                onError = {
                    isMicRecording = false
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Microphone permission is required for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

    // Initialize or load a chat session
    LaunchedEffect(activeCharacter) {
        coroutineScope.launch {
            try {
                val chatId = repository.createChatSession(
                    title = "Chat with ${activeCharacter.name}",
                    characterId = activeCharacter.id,
                    voiceId = activeCharacter.defaultVoiceId
                )
                activeChatId = chatId
                repository.observeMessages(chatId).collectLatest { dbMessages ->
                    messages.clear()
                    messages.addAll(dbMessages)
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            } catch (e: Exception) {
                // If offline or starting new session, initialize default welcome
                if (messages.isEmpty()) {
                    messages.add(
                        ChatMessage(
                            id = "welcome",
                            sender = "ai",
                            text = "Hello! I am ${activeCharacter.name}. ${activeCharacter.tagline} How can I inspire your creativity today?",
                            characterId = activeCharacter.id
                        )
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavyDark)
            .padding(bottom = 80.dp)
    ) {
        // Character Selector Bar
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            isElevated = true
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeCharacter.avatarEmoji,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = activeCharacter.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = activeCharacter.role,
                                fontSize = 11.sp,
                                color = MetallicGold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MidnightNavyElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Switch Character",
                            fontSize = 11.sp,
                            color = ElectricBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CharacterPresets.allCharacters) { char ->
                        val isSelected = char.id == activeCharacter.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) MidnightNavyElevated else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricBlue else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    activeCharacter = char
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = char.avatarEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = char.name,
                                    fontSize = 12.sp,
                                    color = if (isSelected) ElectricBlue else TextSilver
                                )
                            }
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    character = activeCharacter,
                    onSpeak = {
                        val voice = VoicePresets.getById(activeCharacter.defaultVoiceId)
                        voiceManager.speak(msg.text, voice, languageCode = preferredLanguage)
                    },
                    onCopy = {
                        copyToClipboard(context, msg.text)
                    },
                    onShare = {
                        shareText(context, msg.text)
                    },
                    onDelete = {
                        coroutineScope.launch {
                            activeChatId?.let { cid -> repository.deleteMessage(cid, msg.id) }
                            messages.remove(msg)
                        }
                    }
                )
            }

            // Real-time AI Streaming Bubble
            if (isGenerating && currentStreamingAiText.isNotBlank()) {
                item {
                    StreamingAiBubble(
                        text = currentStreamingAiText,
                        character = activeCharacter
                    )
                }
            }
        }

        // Bottom Message Input Row
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Microphone Input Button
                IconButton(
                    onClick = {
                        if (isMicRecording) {
                            voiceManager.stopListening()
                            isMicRecording = false
                        } else {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                isMicRecording = true
                                voiceManager.startListening(
                                    languageCode = if (preferredLanguage == "ur") "ur-PK" else "en-US",
                                    onPartialResult = { inputMessage = it },
                                    onFinalResult = { text ->
                                        isMicRecording = false
                                        if (text.isNotBlank()) {
                                            inputMessage = text
                                        }
                                    },
                                    onError = {
                                        isMicRecording = false
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isMicRecording) Color(0xFFEF4444) else MidnightNavyElevated)
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (isMicRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isMicRecording) TextWhite else ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text(
                            text = if (isMicRecording) "Listening..." else "Message ${activeCharacter.name}...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        val userText = inputMessage.trim()
                        if (userText.isNotBlank() && !isGenerating) {
                            inputMessage = ""
                            isGenerating = true
                            currentStreamingAiText = ""

                            coroutineScope.launch {
                                try {
                                    val userMsg = ChatMessage(
                                        id = "usr_${System.currentTimeMillis()}",
                                        sender = "user",
                                        text = userText,
                                        characterId = activeCharacter.id
                                    )
                                    messages.add(userMsg)
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                    activeChatId?.let { cid ->
                                        try {
                                            repository.addMessage(cid, "user", userText, activeCharacter.id)
                                        } catch (e: Exception) {
                                            Log.w("ChatScreen", "Failed to persist user message: ${e.message}")
                                        }
                                    }

                                    val history = messages.map { it.sender to it.text }
                                    val accumulated = StringBuilder()

                                    aiService.streamChatResponse(
                                        character = activeCharacter,
                                        conversationHistory = history,
                                        userMessage = userText,
                                        targetLanguage = preferredLanguage
                                    ).collect { chunk ->
                                        accumulated.append(chunk)
                                        currentStreamingAiText = accumulated.toString()
                                    }

                                    val finalAiResponse = accumulated.toString().trim()
                                    if (finalAiResponse.isNotBlank()) {
                                        val aiMsg = ChatMessage(
                                            id = "ai_${System.currentTimeMillis()}",
                                            sender = "ai",
                                            text = finalAiResponse,
                                            characterId = activeCharacter.id
                                        )
                                        messages.add(aiMsg)
                                        if (messages.isNotEmpty()) {
                                            listState.animateScrollToItem(messages.size - 1)
                                        }
                                        activeChatId?.let { cid ->
                                            try {
                                                repository.addMessage(cid, "ai", finalAiResponse, activeCharacter.id)
                                            } catch (e: Exception) {
                                                Log.w("ChatScreen", "Failed to persist AI message: ${e.message}")
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("ChatScreen", "Error during chat generation", e)
                                } finally {
                                    isGenerating = false
                                    currentStreamingAiText = ""
                                }
                            }
                        }
                    },
                    enabled = inputMessage.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputMessage.isNotBlank() && !isGenerating) ElectricBlue else MidnightNavyElevated
                        )
                        .testTag("chat_send_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = ElectricBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputMessage.isNotBlank()) MidnightNavyDark else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    character: AiCharacter,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MidnightNavyElevated)
                        .border(1.dp, MetallicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = character.avatarEmoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isUser) {
                            Brush.horizontalGradient(listOf(ElectricBlueDeep, ElectricBlue))
                        } else {
                            Brush.verticalGradient(listOf(MidnightNavySurface, MidnightNavyElevated))
                        }
                    )
                    .border(
                        1.dp,
                        if (isUser) Color.White.copy(alpha = 0.3f) else MetallicGold.copy(alpha = 0.35f),
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = if (isUser) TextWhite else TextSilver,
                    lineHeight = 20.sp
                )
            }
        }

        // Message Quick Action Bar
        Row(
            modifier = Modifier.padding(
                start = if (!isUser) 40.dp else 0.dp,
                end = if (isUser) 6.dp else 0.dp,
                top = 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!isUser) {
                IconButton(onClick = onSpeak, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Speak message",
                        tint = MetallicGold,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(26.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy message",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
            IconButton(onClick = onShare, modifier = Modifier.size(26.dp)) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share message",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete message",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun StreamingAiBubble(
    text: String,
    character: AiCharacter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MidnightNavyElevated)
                .border(1.dp, MetallicGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = character.avatarEmoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MidnightNavySurface)
                .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = TextSilver,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        color = ElectricBlue,
                        strokeWidth = 1.5.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Thinking & Typing...",
                        fontSize = 10.sp,
                        color = ElectricBlue
                    )
                }
            }
        }
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("AI Creative", text))
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}
