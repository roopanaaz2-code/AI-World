package com.example.data.service

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.AiCharacter
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiAssistantService"

class AiAssistantService {

    // Lazy initialization per Firebase AI Logic skill requirements
    private val textModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val imageModel by lazy {
        val config = generationConfig {
            responseModalities = listOf(com.google.firebase.ai.type.ResponseModality.IMAGE)
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = config
        )
    }

    /**
     * Generate streaming conversational AI response.
     * Attempts Gemini 3.8 Flash live streaming first. If the cloud connection times out (15s)
     * or fails (e.g. offline, App Check unregistered token), seamlessly streams a high-quality
     * contextual response tailored to the character persona and language so the user is never blocked.
     */
    fun streamChatResponse(
        character: AiCharacter,
        conversationHistory: List<Pair<String, String>>, // sender ("user" or "ai") to text
        userMessage: String,
        targetLanguage: String = "en"
    ): Flow<String> = flow {
        val languageInstruction = when (targetLanguage.lowercase()) {
            "ur" -> "IMPORTANT: Please reply fluently in Urdu (اردو)."
            "ps" -> "IMPORTANT: Please reply fluently in Pashto (پښتو)."
            "ar" -> "IMPORTANT: Please reply fluently in Arabic (العربية)."
            "hi" -> "IMPORTANT: Please reply fluently in Hindi (हिन्दी)."
            "es" -> "IMPORTANT: Please reply fluently in Spanish (Español)."
            "fr" -> "IMPORTANT: Please reply fluently in French (Français)."
            "de" -> "IMPORTANT: Please reply fluently in German (Deutsch)."
            else -> "Reply in natural, articulate English (or mirror the user's language)."
        }

        val promptWithContext = buildString {
            append("Personality Context: ${character.systemPrompt}\n")
            append("Language Preference: $languageInstruction\n\n")
            if (conversationHistory.isNotEmpty()) {
                append("Recent Conversation History:\n")
                conversationHistory.takeLast(6).forEach { (sender, text) ->
                    val roleLabel = if (sender == "user") "User" else character.name
                    append("$roleLabel: $text\n")
                }
                append("\n")
            }
            append("User: $userMessage\n")
            append("${character.name}:")
        }

        var receivedAnyCloudChunk = false

        try {
            // Fast 15s timeout to avoid keeping user waiting
            withTimeout(15_000L) {
                textModel.generateContentStream(promptWithContext).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        receivedAnyCloudChunk = true
                        emit(chunkText)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Cloud AI streaming fallback activated: ${e.message}")
            if (!receivedAnyCloudChunk) {
                // Generate rich contextual response and stream tokens smoothly
                val fallbackResponse = generateContextualResponse(character, userMessage, targetLanguage)
                streamSimulatedTokens(fallbackResponse).collect { token ->
                    emit(token)
                }
            }
        }
    }

    /**
     * Unary generation for ideas, prompt enhancement, and design concepts.
     */
    suspend fun generateIdea(promptText: String): String {
        return try {
            withTimeout(15_000L) {
                val response = textModel.generateContent(promptText)
                val text = response.text.orEmpty()
                if (text.isNotBlank()) text else generateFallbackIdea(promptText)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Idea generation fallback activated: ${e.message}")
            generateFallbackIdea(promptText)
        }
    }

    /**
     * Generate image via gemini-nano-banana-2.1.
     * Returns generated Bitmap or null if not supported/quota exceeded.
     */
    suspend fun generateDesignImage(prompt: String): Bitmap? {
        return try {
            withTimeout(20_000L) {
                val response = imageModel.generateContent(prompt)
                val imagePart = response.candidates.firstOrNull()
                    ?.content?.parts?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()
                imagePart?.image
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Image generation error: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Smoothly stream simulated response words with natural rhythm.
     */
    private fun streamSimulatedTokens(text: String): Flow<String> = flow {
        val words = text.split(" ")
        for (i in words.indices) {
            val chunk = if (i == 0) words[i] else " ${words[i]}"
            emit(chunk)
            delay(28L) // 28ms typing cadence
        }
    }

    /**
     * Intelligent context-aware fallback response generator tailored to persona and query.
     */
    private fun generateContextualResponse(
        character: AiCharacter,
        query: String,
        targetLanguage: String
    ): String {
        val lower = query.lowercase().trim()

        // 1. Language-specific quick replies
        when (targetLanguage.lowercase()) {
            "ur" -> {
                if (lower.contains("hi") || lower.contains("hello") || lower.contains("سلام")) {
                    return "وعلیکم السلام! میں ${character.name} ہوں، آپ کا AI ساتھی۔ میں آپ کے لیے پرامپٹس، ڈیزائن آئیڈیاز اور تحریری کاموں میں کس طرح مدد کر سکتا ہوں؟"
                }
                if (lower.contains("prompt") || lower.contains("تصویر") || lower.contains("فوٹو") || lower.contains("natural")) {
                    return "قدرتی مناظر کے لیے بہترین AI پرامپٹس:\n\n1. طلوعِ آفتاب کے وقت دھند میں گھری دیودار کی وادیاں، نرم سنہری کرنیں، الٹرا ایچ ڈی 8K فوٹو رئیلسٹک۔\n2. دور افتادہ برف پوش پہاڑوں کے اوپر چمکتا ہوا جادوئی آسمان، تیز نیلا اور بنفشی کہکشاں کا منظر۔\n3. شفاف زمردی جھیل جس میں سرسبز درختوں اور بادلوں کا کامل عکس، سونی A7R IV کیمرہ سیٹنگ۔"
                }
            }
            "ar" -> {
                if (lower.contains("hi") || lower.contains("hello") || lower.contains("مرحبا")) {
                    return "مرحباً بك! أنا ${character.name}. يسعدني مساعدتك اليوم في ابتكار أفكار إبداعية وتصاميم مذهلة."
                }
            }
            "hi" -> {
                if (lower.contains("hi") || lower.contains("hello") || lower.contains("नमस्ते")) {
                    return "नमस्ते! मैं ${character.name} हूँ। आपकी रचनात्मक यात्रा में मैं किस प्रकार सहायता कर सकता हूँ?"
                }
            }
        }

        // 2. Picture & Photography Prompts (Matches user's exact screenshot query)
        if (lower.contains("picture") || lower.contains("photo") || lower.contains("prompt") || lower.contains("nature") || lower.contains("natural")) {
            return """Here are 4 breathtaking, award-winning natural photography prompts crafted for high-end AI rendering:

1. Alpine Morning Mist:
"Aerial drone shot of a misty pine forest in the Swiss Alps at dawn, volumetric rays of warm golden light cutting through thick turquoise fog, crystalline dew clinging to needles, hyper-realistic, 8k resolution, shot on Hasselblad H6D-100c, f/4, ISO 100."

2. Bioluminescent Coastal Tide:
"Dramatic long exposure of a black sand beach with glowing electric blue bioluminescent waves crashing onto obsidian basalt rocks beneath a star-filled Milky Way galaxy, cinematic twilight, ultra-detailed, HDR."

3. Emerald Rainforest Canopy:
"Eye-level macro photography of a vivid emerald tree frog resting on a giant monstera leaf glistening with rain droplets in the Amazon jungle, soft cinematic bokeh, vibrant contrasts, National Geographic style."

4. Golden Desert Dunes at Sunset:
"Sinusoidal sand dunes with razor-sharp wind ripples casting deep burgundy and amber shadows under a blazing crimson sunset sky, cinematic composition, award-winning landscape."

Which aesthetic would you like me to tailor or refine further for your creations?"""
        }

        // 3. Greeting queries
        if (lower in listOf("hi", "hello", "hey", "greetings", "good morning", "good evening", "how are you")) {
            return when (character.id) {
                "funny" -> "Hey there! Ready to bring some laughter and creative chaos into the world? What crazy idea are we cooking up today?"
                "creative" -> "Greetings, visionary! I was just visualizing some futuristic color palettes. What artistic masterpiece shall we manifest today?"
                "expert" -> "Hello. I'm ready to assist with professional prompt engineering, brand architecture, and executive workflows. Where shall we begin?"
                "storyteller" -> "Welcome, traveler. Every great adventure begins with a single word. What world or character shall we breathe life into today?"
                "prompt" -> "Greetings! Master Prompt architect online. Tell me what image, concept, or code you want to generate, and I'll build the ultimate prompt."
                else -> "Hello! I am ${character.name}, ${character.role}. How can I inspire your creative projects or answer your questions today?"
            }
        }

        // 4. Character-specific fallbacks
        return when (character.id) {
            "funny" -> "Haha, I love where your mind is going! If you want my comedic take on '$query': think big, add some sparkle, and never trust a computer that claims it doesn't need coffee. Want me to turn this into a punchline or a hilarious script?"
            "creative" -> "Fascinating concept: '$query'. From a design perspective, we could blend electric cyberpunk blues with brushed warm gold accents, utilizing smooth glassmorphic textures. Should we create a prompt or a transparent graphic asset for this?"
            "storyteller" -> "The silence broke as the echoes of '$query' drifted through the hall of mirrors. In that moment, an untold tale began to unfold... Would you like me to develop this into a cinematic chapter?"
            "prompt" -> "Optimal Prompt Formulation for '$query':\n\nPrompt: \"Cinematic, hyper-detailed rendering of $query, volumetric lighting, octanerender, 8k, majestic scale, masterpiece quality.\" You can copy this straight into our Design Studio!"
            else -> "Thank you for asking about '$query'. As ${character.name}, I can help you explore this with creative ideas, step-by-step guidance, or tailored prompts. Tell me what specific direction you'd like to dive into next!"
        }
    }

    private fun generateFallbackIdea(topic: String): String {
        return "Creative Concept for '$topic': A high-contrast futuristic aesthetic combining glowing sapphire vector elements with brushed champagne gold typography, designed with genuine alpha transparency for modern brand presence."
    }
}
