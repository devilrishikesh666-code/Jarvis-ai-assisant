
package com.rishikesh.jarvis

import android.content.Context

class JarvisMemory(context: Context) {

    private val preferences = context.getSharedPreferences(
        "jarvis_memory",
        Context.MODE_PRIVATE
    )

    fun save(key: String, value: String) {
        if (key.isBlank()) return

        preferences.edit()
            .putString(key, value)
            .apply()
    }

    fun read(key: String): String? {
        return preferences.getString(key, null)
    }

    fun savePreference(name: String, value: String) {
        save("preference_$name", value)
    }

    fun getPreference(name: String): String? {
        return read("preference_$name")
    }

    fun saveConversationSummary(summary: String) {
        save("conversation_summary", summary)
    }

    fun getConversationSummary(): String {
        return read("conversation_summary") ?: ""
    }

    fun clearMemory() {
        preferences.edit().clear().apply()
    }
}
