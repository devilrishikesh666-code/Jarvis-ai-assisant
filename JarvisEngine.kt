
package com.rishikesh.jarvis

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.speech.tts.TextToSpeech
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisEngine(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                tts?.language = Locale("en", "IN")
            }
        }
    }

    fun speak(message: String) {
        if (ready) {
            tts?.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS_RESPONSE"
            )
        }
    }

    fun processCommand(input: String): String {
        val command = input.lowercase(Locale.ROOT).trim()

        val response = when {
            command.contains("hello") ||
            command.contains("hey jarvis") ||
            command.contains("hello jarvis") ->
                "Hello! JARVIS system is online."

            command.contains("time") ->
                "The current time is ${
                    SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(Date())
                }"

            command.contains("date") ->
                "Today's date is ${
                    SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                        .format(Date())
                }"

            command.contains("open youtube") ->
                openApp("com.google.android.youtube", "YouTube")

            command.contains("open chrome") ->
                openApp("com.android.chrome", "Chrome")

            command.contains("open settings") ->
                openSettings()

            command.contains("search") ->
                searchGoogle(command.removePrefix("search").trim())

            command.contains("who are you") ->
                "I am JARVIS, your personal Android AI assistant."

            command.contains("your name") ->
                "My name is JARVIS."

            command.contains("thank you") ->
                "You're welcome. Always ready to assist."

            command.contains("good morning") ->
                "Good morning! All systems are ready."

            command.contains("help") ->
                "You can ask me for the time, date, open applications, or search Google."

            else ->
                "I don't know that command yet. Please connect an AI service for advanced conversations."
        }

        speak(response)
        return response
    }

    private fun openApp(packageName: String, appName: String): String {
        return try {
            val intent = context.packageManager
                .getLaunchIntentForPackage(packageName)

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "$appName is opening."
            } else {
                "I couldn't find $appName on this device."
            }
        } catch (e: Exception) {
            "Unable to open $appName."
        }
    }

    private fun openSettings(): String {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "Opening Android settings."
        } catch (e: Exception) {
            "Unable to open settings."
        }
    }

    private fun searchGoogle(query: String): String {
        if (query.isBlank()) return "Please tell me what to search."

        val url = "https://www.google.com/search?q=" +
                Uri.encode(query)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return try {
            context.startActivity(intent)
            "Searching Google for $query."
        } catch (e: Exception) {
            "Search could not be opened."
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
