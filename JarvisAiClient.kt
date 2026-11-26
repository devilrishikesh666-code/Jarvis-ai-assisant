
package com.rishikesh.jarvis

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.util.concurrent.Executors

class JarvisAiClient(
    private val endpoint: String
) {
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onSuccess(reply: String)
        fun onError(error: String)
    }

    fun ask(
        message: String,
        conversationId: String?,
        callback: Callback
    ) {
        executor.execute {
            var connection: HttpURLConnection? = null

            try {
                connection = URL(endpoint)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                val body = JSONObject().apply {
                    put("message", message)
                    put("conversationId", conversationId ?: "")
                    put("assistant", "JARVIS")
                }

                OutputStreamWriter(
                    connection.outputStream,
                    Charsets.UTF_8
                ).use {
                    it.write(body.toString())
                }

                val status = connection.responseCode

                if (status in 200..299) {
                    val result = connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                    val json = JSONObject(result)
                    val reply = json.optString("reply")

                    if (reply.isBlank()) {
                        callback.onError("Empty AI response")
                    } else {
                        callback.onSuccess(reply)
                    }
                } else {
                    callback.onError(
                        "AI server returned HTTP $status"
                    )
                }

            } catch (e: Exception) {
                callback.onError(
                    e.message ?: "AI connection failed"
                )
            } finally {
                connection?.disconnect()
            }
        }
    }

    fun shutdown() {
        executor.shutdown()
    }
}
