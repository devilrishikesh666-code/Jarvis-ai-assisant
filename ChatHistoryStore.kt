
package com.rishikesh.jarvis

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ChatHistoryStore {

    private const val PREFS = "jarvis_history"
    private const val HISTORY_KEY = "messages"

    fun save(
        context: Context,
        messages: List<ChatMessage>
    ) {
        val array = JSONArray()

        messages.takeLast(100).forEach { message ->

            val item = JSONObject().apply {
                put("text", message.text)
                put("fromUser", message.fromUser)
            }

            array.put(item)
        }

        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).edit()
            .putString(HISTORY_KEY, array.toString())
            .apply()
    }

    fun load(context: Context): List<ChatMessage> {

        val saved = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).getString(HISTORY_KEY, null) ?: return emptyList()

        return try {

            val array = JSONArray(saved)
            val messages = mutableListOf<ChatMessage>()

            for (i in 0 until array.length()) {

                val item = array.getJSONObject(i)

                messages.add(
                    ChatMessage(
                        text = item.optString("text"),
                        fromUser = item.optBoolean("fromUser")
                    )
                )
            }

            messages

        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {

        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).edit()
            .remove(HISTORY_KEY)
            .apply()
    }
}
