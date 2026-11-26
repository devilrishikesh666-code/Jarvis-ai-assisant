
package com.rishikesh.jarvis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class JarvisVoiceController(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onStatus: (String) -> Unit
) {

    private var recognizer: SpeechRecognizer? = null
    private var listening = false

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(language: String = "en-IN") {
        if (!isAvailable()) {
            onStatus("Speech recognition unavailable")
            return
        }

        stopListening()

        recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    listening = true
                    onStatus("Listening...")
                }

                override fun onBeginningOfSpeech() {
                    onStatus("Voice detected")
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    listening = false
                    onStatus("Processing voice...")
                }

                override fun onError(error: Int) {
                    listening = false
                    onStatus(
                        when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "No speech detected"

                            SpeechRecognizer.ERROR_NETWORK ->
                                "Network error"

                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "Listening timed out"

                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Microphone permission required"

                            else -> "Voice recognition error: $error"
                        }
                    )
                }

                override fun onResults(results: Bundle?) {
                    listening = false

                    val matches = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    val text = matches?.firstOrNull()

                    if (!text.isNullOrBlank()) {
                        onResult(text)
                    } else {
                        onStatus("No voice command found")
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                language
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )

            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
            )
        }

        try {
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            onStatus("Unable to start microphone")
        }
    }

    fun isListening(): Boolean = listening

    fun stopListening() {
        listening = false
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }

    fun destroy() {
        stopListening()
    }
}
