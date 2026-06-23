package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiService {
    private const val TAG = "GeminiApiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getGreetingOrResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing or not configured. Please add your GEMINI_API_KEY to secrets or .env to activate the AI Helper."
        }

        val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        val systemInstruction = "You are Samay AI, an expert cultural AI companion built to help users learn about Indian festivals, national holidays, and time-tracking features. Speak warmly, respectfully, and include customary greetings (like 'Namaste!', 'Happy Diwali!', 'Eid Mubarak!') when talking about festivals. Provide clean, readable, concise answers, and guide the user through celebrating their auspicious days. Keep responses format-friendly (use bullet points or bold text where appropriate)."

        try {
            // Build direct JSON representation using org.json (100% stable, built-into Android)
            val requestBodyJson = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", systemInstruction)
                    }))
                })
                // Contents (prompt)
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", prompt)
                    }))
                }))
                // Generation configuration
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val requestBody = requestBodyJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed: Status = ${response.code}, Body = $errBody")
                    return@withContext "Error: Request failed with code ${response.code}. Please ensure your API key is correct."
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext "I received an empty response. Let's try again!"
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No readable answer returned.")
                    }
                }
                return@withContext "I'm having trouble retrieving details. Please phrase your question differently!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            return@withContext "Sorry, I can't connect right now. Please check your internet connection. Detail: ${e.localizedMessage}"
        }
    }
}
