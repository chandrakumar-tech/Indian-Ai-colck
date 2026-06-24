package com.example.api

import android.util.Log
import com.example.data.Festival
import com.example.data.FestivalType
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

        val systemInstruction = "You are Samay AI, an expert cultural companion. Provide extremely brief, direct, and concise answers (maximum 1-2 sentences, under 30 words) to ensure near-instantaneous response. Get straight to the point immediately."

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
                // Generation configuration with constrained tokens for maximum speed
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 100)
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

    suspend fun fetchUpcomingFestivalsOnline(year: Int, month: Int, day: Int): List<Festival> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is empty/placeholder. Cannot fetch online festivals.")
            return@withContext emptyList()
        }

        val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        val prompt = """
            Provide a list of the next 5 major Indian festivals or public holidays occurring on or after ${"$"}year-${"$"}month-${"$"}day.
            Return a JSON array of objects. Each object MUST have this exact schema:
            {
                "name": "Festival Name",
                "dateStr": "Month DD, YYYY (DayOfWeek)",
                "month": Int,
                "day": Int,
                "type": "NATIONAL" | "GAZETTED" | "RESTRICTED" | "REGIONAL",
                "description": "Short description.",
                "ritualOrTip": "Celebration ritual or tip."
            }
            Ensure the month (1-12) and day (1-31) match the actual date of the festival in the year ${"$"}year.
            Return ONLY the raw JSON array string. No markdown formatting, no ```json tags, no explanation.
        """.trimIndent()

        val systemInstruction = "You are Samay AI, a cultural data server. Return only raw JSON arrays without markdown or prose."

        try {
            val requestBodyJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", systemInstruction)
                    }))
                })
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", prompt)
                    }))
                }))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("maxOutputTokens", 1000)
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
                    Log.e(TAG, "API call failed: Status = ${"$"}{response.code}, Body = ${"$"}errBody")
                    return@withContext emptyList()
                }

                val responseBody = response.body?.string() ?: return@withContext emptyList()
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates") ?: return@withContext emptyList()
                if (candidates.length() == 0) return@withContext emptyList()

                val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext emptyList()
                val parts = contentObj.optJSONArray("parts") ?: return@withContext emptyList()
                if (parts.length() == 0) return@withContext emptyList()

                var rawText = parts.getJSONObject(0).optString("text", "").trim()
                
                // Clean markdown wrappers if any
                if (rawText.startsWith("```")) {
                    rawText = rawText.substringAfter("```json").substringBeforeLast("```").trim()
                } else if (rawText.startsWith("`")) {
                    rawText = rawText.trim('`').trim()
                }

                val jsonArray = JSONArray(rawText)
                val festivals = mutableListOf<Festival>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val dateStr = obj.optString("dateStr", "")
                    val m = obj.getInt("month")
                    val d = obj.getInt("day")
                    val typeStr = obj.optString("type", "REGIONAL")
                    val type = try {
                        FestivalType.valueOf(typeStr)
                    } catch (e: Exception) {
                        FestivalType.REGIONAL
                    }
                    val description = obj.optString("description", "")
                    val ritualOrTip = obj.optString("ritualOrTip", "")

                    festivals.add(
                        Festival(
                            name = name,
                            dateStr = dateStr,
                            month = m,
                            day = d,
                            type = type,
                            description = description,
                            ritualOrTip = ritualOrTip
                        )
                    )
                }
                return@withContext festivals
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching upcoming festivals online", e)
            return@withContext emptyList()
        }
    }
}
