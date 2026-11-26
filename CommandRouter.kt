
package com.rishikesh.jarvis

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import java.util.Locale

class JarvisCommandRouter(
    private val context: Context
) {

    sealed class Result {
        data class Handled(val message: String) : Result()
        data class AskAI(val message: String) : Result()
    }

    fun process(input: String): Result {

        val command = input.trim().lowercase(Locale.ROOT)

        if (command.isBlank()) {
            return Result.Handled("Please give me a command.")
        }

        return when {

            command in listOf(
                "hello",
                "hello jarvis",
                "hey jarvis"
            ) -> Result.Handled(
                "Hello! JARVIS systems are ready."
            )

            command.contains("open youtube") ->
                launchApp(
                    "com.google.android.youtube",
                    "YouTube"
                )

            command.contains("open chrome") ->
                launchApp(
                    "com.android.chrome",
                    "Chrome"
                )

            command.contains("open settings") ->
                openSettings()

            command.startsWith("search ") ->
                searchWeb(command.removePrefix("search "))

            command.contains("who are you") ->
                Result.Handled(
                    "I am JARVIS, an AI assistant developed by Rishikesh Singh."
                )

            command.contains("what can you do") ->
                Result.Handled(
                    "I can recognize voice commands, answer questions, search the web and open supported apps."
                )

            else -> Result.AskAI(input)
        }
    }

    private fun launchApp(
        packageName: String,
        appName: String
    ): Result {

        return try {
            val intent = context.packageManager
                .getLaunchIntentForPackage(packageName)

            if (intent == null) {
                Result.Handled("$appName is not installed.")
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)

                Result.Handled("Opening $appName.")
            }

        } catch (e: Exception) {
            Result.Handled("Unable to open $appName.")
        }
    }

    private fun openSettings(): Result {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            Result.Handled("Opening Android settings.")

        } catch (e: Exception) {
            Result.Handled("Could not open settings.")
        }
    }

    private fun searchWeb(query: String): Result {

        if (query.isBlank()) {
            return Result.Handled(
                "Tell me what you want to search."
            )
        }

        return try {
            val uri = Uri.parse(
                "https://www.google.com/search?q=${Uri.encode(query)}"
            )

            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)

            Result.Handled("Searching for $query.")

        } catch (e: Exception) {
            Result.Handled("Unable to perform search.")
        }
    }
}
