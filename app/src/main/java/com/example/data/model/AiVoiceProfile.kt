package com.example.data.model

data class AiVoiceProfile(
    val id: String,
    val name: String,
    val gender: String, // "Female" or "Male"
    val description: String,
    val style: String,
    val pitch: Float, // 0.5 to 2.0
    val speechRate: Float, // 0.5 to 2.0
    val avatarEmoji: String,
    val accentTag: String
)

object VoicePresets {
    val allVoices = listOf(
        AiVoiceProfile(
            id = "aria",
            name = "Aria",
            gender = "Female",
            description = "Warm, cheerful, approachable companion voice",
            style = "Friendly & Warm",
            pitch = 1.15f,
            speechRate = 1.0f,
            avatarEmoji = "✨",
            accentTag = "Bright & Expressive"
        ),
        AiVoiceProfile(
            id = "lyra",
            name = "Lyra",
            gender = "Female",
            description = "Natural female storyteller with cinematic pacing",
            style = "Narrative & Poetic",
            pitch = 1.0f,
            speechRate = 0.95f,
            avatarEmoji = "📖",
            accentTag = "Captivating & Melodic"
        ),
        AiVoiceProfile(
            id = "elena",
            name = "Elena",
            gender = "Female",
            description = "Crisp, polite, articulate executive assistant",
            style = "Professional & Direct",
            pitch = 1.05f,
            speechRate = 1.05f,
            avatarEmoji = "💼",
            accentTag = "Executive & Precise"
        ),
        AiVoiceProfile(
            id = "chloe",
            name = "Chloe",
            gender = "Female",
            description = "Witty, humorous and lively comedic personality",
            style = "Funny & Upbeat",
            pitch = 1.25f,
            speechRate = 1.1f,
            avatarEmoji = "🎉",
            accentTag = "Playful & Vibrant"
        ),
        AiVoiceProfile(
            id = "leo",
            name = "Leo",
            gender = "Male",
            description = "Friendly, encouraging and approachable assistant",
            style = "Supportive & Clear",
            pitch = 0.95f,
            speechRate = 1.0f,
            avatarEmoji = "🚀",
            accentTag = "Warm & Natural"
        ),
        AiVoiceProfile(
            id = "marcus",
            name = "Marcus",
            gender = "Male",
            description = "Deep, resonant, authoritative broadcast voice",
            style = "Authoritative & Deep",
            pitch = 0.75f,
            speechRate = 0.92f,
            avatarEmoji = "🎙️",
            accentTag = "Rich & Baritone"
        ),
        AiVoiceProfile(
            id = "david",
            name = "David",
            gender = "Male",
            description = "Executive strategist, analytical and concise",
            style = "Corporate & Analytical",
            pitch = 0.9f,
            speechRate = 1.05f,
            avatarEmoji = "👔",
            accentTag = "Polished & Formal"
        ),
        AiVoiceProfile(
            id = "felix",
            name = "Felix",
            gender = "Male",
            description = "Hilarious comedian, boundless energy and humor",
            style = "Comedic & Energetic",
            pitch = 1.1f,
            speechRate = 1.15f,
            avatarEmoji = "😄",
            accentTag = "Animated & Fun"
        ),
        AiVoiceProfile(
            id = "zen",
            name = "Zen",
            gender = "Neutral",
            description = "Calm, soothing meditation & mindfulness guide",
            style = "Serene & Relaxing",
            pitch = 0.85f,
            speechRate = 0.85f,
            avatarEmoji = "🌿",
            accentTag = "Tranquil & Mindful"
        ),
        AiVoiceProfile(
            id = "orion",
            name = "Orion",
            gender = "Male",
            description = "Creative visionary, mythic lore and epic narratives",
            style = "Imaginative & Epic",
            pitch = 0.88f,
            speechRate = 0.95f,
            avatarEmoji = "🌌",
            accentTag = "Visionary & Epic"
        )
    )

    fun getById(id: String): AiVoiceProfile = allVoices.firstOrNull { it.id == id } ?: allVoices.first()
}
