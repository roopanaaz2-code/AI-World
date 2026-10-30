package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository
import com.example.data.service.AiAssistantService
import com.example.data.service.VoiceManager
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreationsScreen
import com.example.ui.screens.DesignStudioScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.VoiceStudioScreen
import com.example.ui.theme.AiCreativeTheme
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.MidnightNavyDark
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.MidnightNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    HOME("Home"),
    CHAT("AI Chat"),
    VOICE("Voice Studio"),
    DISCOVER("Discover"),
    CREATIONS("Creations")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch Intent before UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            AiCreativeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MidnightNavyDark
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        MainStudioApp(user = currentUser!!)
    }
}

@Composable
fun MainStudioApp(user: FirebaseUser) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val repository = remember { AppRepository(context) }
    val aiService = remember { AiAssistantService() }
    val voiceManager = remember { VoiceManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    // Load or initialize user profile
    val userProfile by repository.observeUserProfile().collectAsState(initial = null)
    var currentLanguage by remember { mutableStateOf("en") }

    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            currentLanguage = profile.preferredLanguage
        } ?: run {
            // Save initial profile
            coroutineScope.launch {
                repository.saveUserProfile(
                    UserProfile(
                        userId = user.uid,
                        displayName = user.displayName ?: "Creator",
                        email = user.email ?: "",
                        avatarUrl = user.photoUrl?.toString(),
                        preferredLanguage = "en",
                        preferredVoiceId = "aria"
                    )
                )
            }
        }
    }

    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // Inter-screen navigation parameters
    var targetChatCharacterId by remember { mutableStateOf<String?>(null) }
    var targetDiscoverQuery by remember { mutableStateOf<String?>(null) }
    var targetDesignPrompt by remember { mutableStateOf<String?>(null) }

    // Dialogs
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    // BackHandler: Return to Home tab if on any sub-screen
    BackHandler(enabled = currentTab != AppTab.HOME) {
        currentTab = AppTab.HOME
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MidnightNavyDark,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, Color(0x3300D2FF)),
                containerColor = MidnightNavySurface,
                contentColor = TextWhite
            ) {
                // 1. Home
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { currentTab = AppTab.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = AppTab.HOME.title,
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightNavyDark,
                        selectedTextColor = ElectricBlue,
                        indicatorColor = ElectricBlue,
                        unselectedIconColor = TextSilver,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )

                // 2. AI Chat
                NavigationBarItem(
                    selected = currentTab == AppTab.CHAT,
                    onClick = { currentTab = AppTab.CHAT },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "AI Chat",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = AppTab.CHAT.title,
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.CHAT) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightNavyDark,
                        selectedTextColor = ElectricBlue,
                        indicatorColor = ElectricBlue,
                        unselectedIconColor = TextSilver,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_chat_tab")
                )

                // 3. Voice Studio
                NavigationBarItem(
                    selected = currentTab == AppTab.VOICE,
                    onClick = { currentTab = AppTab.VOICE },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.VOICE) Icons.Filled.Mic else Icons.Outlined.MicNone,
                            contentDescription = "Voice Studio",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = AppTab.VOICE.title,
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.VOICE) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightNavyDark,
                        selectedTextColor = MetallicGold,
                        indicatorColor = MetallicGold,
                        unselectedIconColor = TextSilver,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_voice_tab")
                )

                // 4. Discover
                NavigationBarItem(
                    selected = currentTab == AppTab.DISCOVER,
                    onClick = { currentTab = AppTab.DISCOVER },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.DISCOVER) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = "Discover",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = AppTab.DISCOVER.title,
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.DISCOVER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightNavyDark,
                        selectedTextColor = ElectricBlue,
                        indicatorColor = ElectricBlue,
                        unselectedIconColor = TextSilver,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_discover_tab")
                )

                // 5. My Creations
                NavigationBarItem(
                    selected = currentTab == AppTab.CREATIONS,
                    onClick = { currentTab = AppTab.CREATIONS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CREATIONS) Icons.Filled.AutoAwesomeMosaic else Icons.Outlined.Palette,
                            contentDescription = "Creations",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = AppTab.CREATIONS.title,
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.CREATIONS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightNavyDark,
                        selectedTextColor = MetallicGold,
                        indicatorColor = MetallicGold,
                        unselectedIconColor = TextSilver,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_creations_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        userProfile = userProfile,
                        onNavigateToChat = { charId ->
                            targetChatCharacterId = charId
                            currentTab = AppTab.CHAT
                        },
                        onNavigateToVoice = {
                            currentTab = AppTab.VOICE
                        },
                        onNavigateToDesign = {
                            targetDesignPrompt = null
                            currentTab = AppTab.CREATIONS
                        },
                        onNavigateToDiscover = { query ->
                            targetDiscoverQuery = query
                            currentTab = AppTab.DISCOVER
                        },
                        onOpenSettings = {
                            showSettingsDialog = true
                        }
                    )
                }
                AppTab.CHAT -> {
                    ChatScreen(
                        initialCharacterId = targetChatCharacterId,
                        repository = repository,
                        aiService = aiService,
                        voiceManager = voiceManager,
                        preferredLanguage = currentLanguage
                    )
                }
                AppTab.VOICE -> {
                    VoiceStudioScreen(
                        aiService = aiService,
                        voiceManager = voiceManager,
                        initialLanguage = currentLanguage
                    )
                }
                AppTab.DISCOVER -> {
                    DiscoverScreen(
                        repository = repository,
                        initialSearchQuery = targetDiscoverQuery,
                        onUsePromptInChat = { prompt ->
                            targetChatCharacterId = "prompt_master"
                            currentTab = AppTab.CHAT
                        },
                        onUsePromptInDesign = { prompt ->
                            targetDesignPrompt = prompt
                            currentTab = AppTab.CREATIONS
                        }
                    )
                }
                AppTab.CREATIONS -> {
                    DesignStudioScreen(
                        initialPrompt = targetDesignPrompt,
                        repository = repository,
                        aiService = aiService
                    )
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            userProfile = userProfile,
            currentLanguageCode = currentLanguage,
            onOpenLanguagePicker = {
                showLanguagePicker = true
            },
            onDismiss = { showSettingsDialog = false },
            onSignOut = {
                showSettingsDialog = false
            }
        )
    }

    // Language Picker Dialog
    if (showLanguagePicker) {
        LanguageSelectionDialog(
            currentCode = currentLanguage,
            onSelectLanguage = { newLang ->
                currentLanguage = newLang
                coroutineScope.launch {
                    userProfile?.let {
                        repository.saveUserProfile(it.copy(preferredLanguage = newLang))
                    }
                }
            },
            onDismiss = { showLanguagePicker = false }
        )
    }
}
