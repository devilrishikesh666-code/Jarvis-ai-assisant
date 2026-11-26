
package com.rishikesh.jarvis

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.UUID

class JarvisAssistant(
    private val context: Context,
    private val backendUrl: String,
    private val onResponse: (String) -> Unit,
    private val onStatus: (String) -> Unit
) {

    private val router = JarvisCommandRouter(context)
    private val memory = JarvisMemory(context)

    private val ai = JarvisAiClient(backendUrl)

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val conversationId = UUID.randomUUID().toString()

    init {
        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS

            if (ttsReady) {
                tts?.language = Locale("en", "IN")
            }
        }
    }

    fun speak(message: String) {
        if (ttsReady) {
            tts?.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                UUID.randomUUID().toString()
            )
        }
    }

    fun handleMessage(input: String) {

        if (input.isBlank()) return

        onStatus("JARVIS is thinking...")

        when (val result = router.process(input)) {

            is JarvisCommandRouter.Result.Handled -> {
                deliver(result.message)
            }

            is JarvisCommandRouter.Result.AskAI -> {
                askAI(result.message)
            }
        }
    }

    private fun askAI(message: String) {

        ai.ask(
            message,
            conversationId,
            object : JarvisAiClient.Callback {

                override fun onSuccess(reply: String) {
                    onResponse(reply)
                    speak(reply)
                    onStatus("JARVIS is ready.")
                }

                override fun onError(error: String) {
                    onResponse(
                        "I cannot connect to my AI brain right now. $error"
                    )
                    onStatus("AI connection unavailable.")
                }
            }
        )
    }

    private fun deliver(message: String) {
        onResponse(message)
        speak(message)
        onStatus("JARVIS is ready.")
    }

    fun remember(key: String, value: String) {
        memory.save(key, value)
    }

    fun recall(key: String): String? {
        return memory.read(key)
    }

    fun saveUserPreference(name: String, value: String) {
        memory.savePreference(name, value)
    }

    fun getUserPreference(name: String): String? {
        return memory.getPreference(name)
    }

    fun getMemorySummary(): String {
        return memory.getConversationSummary()
    }

    fun shutdown() {
        ai.shutdown()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
