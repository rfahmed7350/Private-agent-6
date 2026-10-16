package com.yesmin.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiClient(private val apiKey: String, private val model: String = "gemini-2.5-flash") {
    suspend fun reply(history: List<Pair<String, String>>, prompt: String): String = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 30000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-goog-api-key", apiKey)
            }
            val contents = JSONArray()
            history.forEach { (speaker, text) ->
                contents.put(JSONObject()
                    .put("role", if (speaker == "You") "user" else "model")
                    .put("parts", JSONArray().put(JSONObject().put("text", text))))
            }
            contents.put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", prompt))))
            val body = JSONObject()
                .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text",
                    "You are Yesmin AI, a warm, helpful and playful AI companion. Be transparent that you are an AI. Respond naturally in the language the user uses, including Hindi, Hinglish, or English. Do not claim to perform phone actions unless confirmed."))))
                .put("contents", contents)
                .put("generationConfig", JSONObject().put("temperature", 0.8))
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) {
                val message = runCatching { JSONObject(response).getJSONObject("error").getString("message") }.getOrDefault("HTTP $code")
                "Gemini request failed: $message"
            } else {
                val json = JSONObject(response)
                json.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            }
        } catch (e: Exception) {
            "I couldn't reach Gemini. Check your internet connection and API key, then try again."
        } finally {
            connection?.disconnect()
        }
    }
}
