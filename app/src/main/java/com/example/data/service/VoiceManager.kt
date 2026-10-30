package com.example.data.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AiVoiceProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

private const val TAG = "VoiceManager"

class VoiceManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningInternal = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechInputText = MutableStateFlow("")
    val speechInputText: StateFlow<String> = _speechInputText.asStateFlow()

    // 16 waveform amplitude levels (0.0f to 1.0f)
    private val _waveformLevels = MutableStateFlow(List(16) { 0.15f })
    val waveformLevels: StateFlow<List<Float>> = _waveformLevels.asStateFlow()

    private var waveformJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        startWaveformSimulation()
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        stopWaveformSimulation()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        stopWaveformSimulation()
                    }
                })
                Log.d(TAG, "TextToSpeech initialized successfully.")
            } else {
                Log.e(TAG, "TextToSpeech initialization failed: $status")
            }
        }
    }

    fun speak(
        text: String,
        voiceProfile: AiVoiceProfile,
        speechRateMultiplier: Float = 1.0f,
        languageCode: String = "en",
        onDone: () -> Unit = {}
    ) {
        if (!isTtsReady || text.isBlank()) {
            onDone()
            return
        }

        stopSpeaking()

        val locale = when (languageCode.lowercase()) {
            "ur" -> Locale("ur", "PK")
            "ar" -> Locale("ar", "SA")
            "hi" -> Locale("hi", "IN")
            "es" -> Locale("es", "ES")
            "fr" -> Locale("fr", "FR")
            "de" -> Locale("de", "DE")
            else -> Locale.US
        }

        try {
            tts?.language = locale
        } catch (e: Exception) {
            tts?.language = Locale.US
        }

        // Apply voice parameters
        tts?.setPitch(voiceProfile.pitch)
        tts?.setSpeechRate(voiceProfile.speechRate * speechRateMultiplier)

        val utteranceId = "msg_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
            stopWaveformSimulation()
        }
    }

    fun startListening(
        languageCode: String = "en-US",
        onPartialResult: (String) -> Unit = {},
        onFinalResult: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition is not available on this device.")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                isListeningInternal = true
                startWaveformSimulation()
            }

            override fun onBeginningOfSpeech() {
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Modulate waveform using microphone volume
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
                _waveformLevels.value = List(16) { idx ->
                    val factor = 0.5f + 0.5f * kotlin.math.sin((idx.toDouble() / 16.0) * Math.PI).toFloat()
                    (normalized * factor).coerceIn(0.1f, 1.0f)
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                isListeningInternal = false
                stopWaveformSimulation()
            }

            override fun onError(error: Int) {
                _isListening.value = false
                isListeningInternal = false
                stopWaveformSimulation()
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Tap mic to try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out. Tap mic to speak."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue. Please check microphone."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue during speech recognition."
                    else -> "Speech recognition paused."
                }
                Log.w(TAG, "SpeechRecognizer notice: $message ($error)")
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                isListeningInternal = false
                stopWaveformSimulation()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull().orEmpty()
                _speechInputText.value = recognizedText
                onFinalResult(recognizedText)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull().orEmpty()
                if (partial.isNotBlank()) {
                    _speechInputText.value = partial
                    onPartialResult(partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        if (isListeningInternal) {
            speechRecognizer?.stopListening()
            _isListening.value = false
            isListeningInternal = false
            stopWaveformSimulation()
        }
    }

    private fun startWaveformSimulation() {
        waveformJob?.cancel()
        waveformJob = scope.launch {
            while (isActive) {
                _waveformLevels.value = List(16) {
                    Random.nextFloat() * 0.75f + 0.2f
                }
                delay(80)
            }
        }
    }

    private fun stopWaveformSimulation() {
        waveformJob?.cancel()
        waveformJob = null
        _waveformLevels.value = List(16) { 0.12f }
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        waveformJob?.cancel()
    }
}
