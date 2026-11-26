
package com.rishikesh.jarvis

import android.content.Context
import android.content.Intent
import android.net.Uri

object JarvisCommandRouter {

    fun execute(
        context: Context,
        command: String
    ): String {

        val text = command.trim().lowercase()

        return when {

            text == "open youtube" ->
                launchApp(context, "com.google.android.youtube")

            text == "open whatsapp" ->
                launchApp(context, "com.whatsapp")

            text == "open chrome" ->
                launchApp(context, "com.android.chrome")

            text == "open gmail" ->
                launchApp(context, "com.google.android.gm")

            text.startsWith("search youtube for ") -> {
                val query = command.substringAfter(
                    "search youtube for ",
                    ""
                ).trim()

                if (query.isBlank()) {
                    "Please tell me what to search for."
                } else {
                    openLink(
                        context,
                        "https://www.youtube.com/results?search_query=" +
                            Uri.encode(query)
                    )
                    "Opening YouTube search for $query."
                }
            }

            text.startsWith("search web for ") -> {
                val query = command.substringAfter(
                    "search web for ",
                    ""
                ).trim()

                if (query.isBlank()) {
                    "Please provide a search query."
                } else {
                    openLink(
                        context,
                        "https://www.google.com/search?q=" +
                            Uri.encode(query)
                    )
                    "Searching the web for $query."
                }
            }

            text == "open settings" -> {
                val intent = Intent(
                    android.provider.Settings.ACTION_SETTINGS
                )
                context.startActivity(intent)
                "Opening device settings."
            }

            else -> "UNKNOWN_COMMAND"
        }
    }

    private fun launchApp(
        context: Context,
        packageName: String
    ): String {

        val intent =
            context.packageManager.getLaunchIntentForPackage(
                packageName
            )

        return if (intent != null) {
            context.startActivity(intent)
            "Application opened successfully."
        } else {
            "This application is not installed."
        }
    }

    private fun openLink(
        context: Context,
        url: String
    ) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )

        context.startActivity(intent)
    }
}
