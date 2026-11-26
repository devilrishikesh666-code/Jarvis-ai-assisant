
package com.rishikesh.jarvis

data class JarvisFeature(
    val title: String,
    val description: String,
    val enabled: Boolean
)

object JarvisFeatures {

    val features = listOf(

        JarvisFeature(
            "AI Chat",
            "Chat with the Gemini AI model.",
            true
        ),

        JarvisFeature(
            "Voice Recognition",
            "Speak commands to JARVIS.",
            true
        ),

        JarvisFeature(
            "Voice Response",
            "Listen to spoken AI responses.",
            true
        ),

        JarvisFeature(
            "App Launcher",
            "Open supported applications.",
            true
        ),

        JarvisFeature(
            "Web Search",
            "Search the web using a browser.",
            true
        ),

        JarvisFeature(
            "YouTube Search",
            "Search videos on YouTube.",
            true
        ),

        JarvisFeature(
            "Conversation Memory",
            "Save chat history locally.",
            true
        ),

        JarvisFeature(
            "Smart Home",
            "Future integration with compatible devices.",
            false
        ),

        JarvisFeature(
            "Offline AI",
            "Future offline assistant support.",
            false
        )
    )
}
