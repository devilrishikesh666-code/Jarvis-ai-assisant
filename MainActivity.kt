
package com.rishikesh.jarvis

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

private data class ChatMessage(
    val text: String,
    val fromUser: Boolean
)

class MainActivity : ComponentActivity(),
    TextToSpeech.OnInitListener {

    private var speech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speech = TextToSpeech(this, this)

        setContent {
            JarvisApp()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            speech?.language = Locale.US
        }
    }

    private fun say(text: String) {
        speech?.speak(
            text.take(3500),
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis-response"
        )
    }

    private fun tryOpenApp(message: String): String? {

        val lower = message.lowercase(Locale.ROOT).trim()

        val apps = listOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "chrome" to "com.android.chrome",
            "gmail" to "com.google.android.gm"
        )

        val match = apps.firstOrNull { (name, _) ->
            lower == "open $name" ||
            lower == "launch $name" ||
            lower == name
        } ?: return null

        val intent =
            packageManager.getLaunchIntentForPackage(match.second)

        if (intent == null) {
            return "I couldn't find ${match.first} on this device."
        }

        startActivity(intent)

        return "Opening ${match.first}."
    }

    @Composable
    private fun JarvisApp() {

        val preferences = remember {
            getSharedPreferences("jarvis", MODE_PRIVATE)
        }

        var apiKey by remember {
            mutableStateOf(
                preferences.getString("gemini_key", "") ?: ""
            )
        }

        var keyDraft by remember {
            mutableStateOf(apiKey)
        }

        var showSettings by remember {
            mutableStateOf(false)
        }

        var messageInput by remember {
            mutableStateOf("")
        }

        var loading by remember {
            mutableStateOf(false)
        }

        var status by remember {
            mutableStateOf("SYSTEM ONLINE • STANDING BY")
        }

        val chat = remember {
            mutableStateListOf(
                ChatMessage(
                    "Good day. I am JARVIS. Configure your Gemini API key in Settings to activate AI chat. Try saying open YouTube.",
                    false
                )
            )
        }

        val scope = rememberCoroutineScope()

        val voiceLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->

                val words = result.data
                    ?.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )
                    ?.firstOrNull()

                if (!words.isNullOrBlank()) {
                    messageInput = words
                }
            }

        fun submit() {

            val prompt = messageInput.trim()

            if (prompt.isEmpty() || loading) return

            messageInput = ""

            chat.add(ChatMessage(prompt, true))

            val appResult = tryOpenApp(prompt)

            if (appResult != null) {
                chat.add(ChatMessage(appResult, false))
                say(appResult)
                return
            }

            if (apiKey.isBlank()) {

                val reply =
                    "Please add your Gemini API key in Settings to enable AI conversations."

                chat.add(ChatMessage(reply, false))
                say(reply)

                return
            }

            loading = true
            status = "PROCESSING REQUEST..."

            scope.launch {

                val reply = withContext(Dispatchers.IO) {
                    GeminiClient.reply(apiKey, prompt)
                }

                chat.add(ChatMessage(reply, false))

                say(reply)

                loading = false
                status = "SYSTEM ONLINE • STANDING BY"
            }
        }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFFE5B86B),
                background = Color(0xFF090D12),
                surface = Color(0xFF141C24),
                onSurface = Color(0xFFEAF0F5)
            )
        ) {

            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090D12))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {

                        Text(
                            "J.A.R.V.I.S",
                            color = Color(0xFFE5B86B),
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "PERSONAL AI SYSTEM",
                            color = Color(0xFF8998A6),
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            keyDraft = apiKey
                            showSettings = true
                        }
                    ) {
                        Text(
                            "SETTINGS",
                            color = Color(0xFFE5B86B)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF141C24),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(Modifier.padding(13.dp)) {

                        Text(
                            "ASSISTANT STATUS",
                            color = Color(0xFF8795A2),
                            fontSize = 10.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            status,
                            color = Color(0xFF9BD5AF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    items(chat) { item ->

                        Row(
                            Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                if (item.fromUser)
                                    Arrangement.End
                                else
                                    Arrangement.Start
                        ) {

                            Surface(
                                color =
                                    if (item.fromUser)
                                        Color(0xFF263849)
                                    else
                                        Color(0xFF151E27),

                                shape =
                                    RoundedCornerShape(14.dp),

                                modifier =
                                    Modifier.fillMaxWidth(0.9f)
                            ) {

                                Text(
                                    item.text,
                                    color = Color(0xFFEAF0F5),
                                    modifier = Modifier.padding(13.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (loading) {
                        item {
                            Text(
                                "JARVIS is thinking...",
                                color = Color(0xFFE5B86B),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = {
                        messageInput = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Message JARVIS...")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            submit()
                        }
                    )
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedButton(
                        onClick = {

                            val intent =
                                Intent(
                                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                                ).apply {

                                    putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                    )

                                    putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE,
                                        Locale.getDefault()
                                    )

                                    putExtra(
                                        RecognizerIntent.EXTRA_PROMPT,
                                        "Speak to JARVIS"
                                    )
                                }

                            try {
                                voiceLauncher.launch(intent)
                            } catch (_: Exception) {
                                status = "VOICE RECOGNITION UNAVAILABLE"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("VOICE")
                    }

                    Button(
                        onClick = {
                            submit()
                        },
                        enabled = !loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "SEND",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "GEMINI AI • VOICE CONTROL • APP SHORTCUTS",
                    color = Color(0xFF657482),
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            if (showSettings) {

                AlertDialog(
                    onDismissRequest = {
                        showSettings = false
                    },

                    title = {
                        Text("JARVIS SETTINGS")
                    },

                    text = {

                        Column {

                            Text("Gemini API key")

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = keyDraft,
                                onValueChange = {
                                    keyDraft = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation =
                                    PasswordVisualTransformation(),
                                placeholder = {
                                    Text("Paste your API key")
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "This starter version stores the key locally. For a public release, use a secure backend."
                            )
                        }
                    },

                    confirmButton = {

                        TextButton(
                            onClick = {

                                apiKey = keyDraft.trim()

                                preferences.edit()
                                    .putString("gemini_key", apiKey)
                                    .apply()

                                showSettings = false
                            }
                        ) {
                            Text("SAVE")
                        }
                    },

                    dismissButton = {

                        TextButton(
                            onClick = {
                                showSettings = false
                            }
                        ) {
                            Text("CANCEL")
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        speech?.stop()
        speech?.shutdown()
        super.onDestroy()
    }
}

private object GeminiClient {

    fun reply(apiKey: String, prompt: String): String {

        var connection: HttpURLConnection? = null

        return try {

            val key = URLEncoder.encode(apiKey, "UTF-8")

            connection = (
                URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key"
                ).openConnection() as HttpURLConnection
            ).apply {

                requestMethod = "POST"
                connectTimeout = 20000
                readTimeout = 30000
                doOutput = true

                setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8"
                )
            }

            val instruction =
                "You are JARVIS, a helpful and respectful personal AI assistant. Answer clearly and concisely. User message: $prompt"

            val body = JSONObject().put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(
                            JSONObject().put(
                                "text",
                                instruction
                            )
                        )
                    )
                )
            )

            connection.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val code = connection.responseCode

            val stream =
                if (code in 200..299)
                    connection.inputStream
                else
                    connection.errorStream

            val raw = stream.bufferedReader().use {
                it.readText()
            }

            val json = JSONObject(raw)

            if (code !in 200..299) {

                "Gemini returned error $code: " +
                    (
                        json.optJSONObject("error")
                            ?.optString("message")
                            ?: "Request failed"
                    )

            } else {

                json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "I received an empty response. Please try again."
            }

        } catch (error: Exception) {

            "I couldn't connect to Gemini: " +
                (error.message ?: "network error")

        } finally {

            connection?.disconnect()
        }
    }
}
