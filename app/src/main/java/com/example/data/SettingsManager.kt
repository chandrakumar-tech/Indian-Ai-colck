package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SettingsManager {
    private const val PREFS_NAME = "samay_settings"
    private const val KEY_USE_CUSTOM_TIME = "use_custom_time"
    private const val KEY_TIME_OFFSET = "time_offset_millis"
    private const val KEY_CUSTOM_FESTIVALS = "custom_festivals_json"

    private lateinit var prefs: SharedPreferences

    // Compose states for reactivity in UI
    var useCustomTime by mutableStateOf(false)
        private set

    var timeOffsetMillis by mutableStateOf(0L)
        private set

    var customFestivals by mutableStateOf<List<Festival>>(emptyList())
        private set

    var onlineFestivals by mutableStateOf<List<Festival>>(emptyList())
        private set

    var isFetchingOnline by mutableStateOf(false)
        private set

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        useCustomTime = prefs.getBoolean(KEY_USE_CUSTOM_TIME, false)
        timeOffsetMillis = prefs.getLong(KEY_TIME_OFFSET, 0L)
        loadCustomFestivals()
    }

    /**
     * Calculates and saves a new simulated date/time.
     * Takes the desired target calendar and computes the offset relative to the current actual system time.
     */
    fun setCustomDateTime(targetCal: Calendar) {
        val actualSystemTime = System.currentTimeMillis()
        val targetTime = targetCal.timeInMillis
        timeOffsetMillis = targetTime - actualSystemTime
        useCustomTime = true

        prefs.edit()
            .putBoolean(KEY_USE_CUSTOM_TIME, true)
            .putLong(KEY_TIME_OFFSET, timeOffsetMillis)
            .apply()
    }

    /**
     * Resets the application time back to the device's actual system time.
     */
    fun resetToSystemTime() {
        useCustomTime = false
        timeOffsetMillis = 0L

        prefs.edit()
            .putBoolean(KEY_USE_CUSTOM_TIME, false)
            .putLong(KEY_TIME_OFFSET, 0L)
            .apply()
    }

    /**
     * Gets the active current time in milliseconds, applying custom offsets if enabled.
     */
    fun getCurrentTimeMillis(): Long {
        val actualTime = System.currentTimeMillis()
        return if (useCustomTime) actualTime + timeOffsetMillis else actualTime
    }

    /**
     * Gets a Calendar instance set to the current simulated app time.
     */
    fun getAppCalendar(): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = getCurrentTimeMillis()
        }
    }

    // --- FESTIVAL MANAGEMENT ---

    fun addCustomFestival(festival: Festival) {
        val updatedList = customFestivals.toMutableList()
        // Replace if exists with same name, or append
        val index = updatedList.indexOfFirst { it.name.equals(festival.name, ignoreCase = true) }
        if (index >= 0) {
            updatedList[index] = festival
        } else {
            updatedList.add(festival)
        }
        customFestivals = updatedList
        saveCustomFestivals()
    }

    fun deleteCustomFestival(festivalName: String) {
        customFestivals = customFestivals.filterNot { it.name.equals(festivalName, ignoreCase = true) }
        saveCustomFestivals()
    }

    fun getAllFestivals(): List<Festival> {
        // Return combination of base list (online fetched if available, otherwise local) and custom ones
        val baseList = if (onlineFestivals.isNotEmpty()) onlineFestivals else FestivalData.list2026
        val customMap = customFestivals.associateBy { it.name.lowercase() }

        val combined = mutableListOf<Festival>()
        // If a custom festival overrides a base one (matching name), use the custom one
        baseList.forEach { baseFest ->
            val customOverride = customMap[baseFest.name.lowercase()]
            if (customOverride != null) {
                combined.add(customOverride)
            } else {
                combined.add(baseFest)
            }
        }

        // Add any other custom festivals that are brand new
        val baseNames = baseList.map { it.name.lowercase() }.toSet()
        customFestivals.forEach { customFest ->
            if (customFest.name.lowercase() !in baseNames) {
                combined.add(customFest)
            }
        }

        // Sort by month and day
        return combined.sortedWith(compareBy({ it.month }, { it.day }))
    }

    fun fetchOnlineHolidays(year: Int) {
        if (isFetchingOnline) return
        isFetchingOnline = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Fetch official holidays from public API
                val apiService = HolidayApiService.create()
                val nagerHolidays = try {
                    apiService.getIndianHolidays(year)
                } catch (e: Exception) {
                    emptyList()
                }
                val nagerMapped = nagerHolidays.map { mapNagerHolidayToFestival(it) }

                // 2. Fetch rich cultural and regional festivals online from Gemini AI
                val geminiFestivals = try {
                    com.example.api.GeminiApiService.fetchUpcomingFestivalsOnline(year, 1, 1)
                } catch (e: Exception) {
                    emptyList()
                }

                // 3. Merge them carefully (prioritize Gemini for descriptions & rituals, but keep unique Nager entries)
                val merged = mutableListOf<Festival>()
                val existingNames = mutableSetOf<String>()

                geminiFestivals.forEach { fest ->
                    merged.add(fest)
                    existingNames.add(fest.name.lowercase().trim())
                }

                nagerMapped.forEach { fest ->
                    val cleanName = fest.name.lowercase().trim()
                    if (!existingNames.any { it.contains(cleanName) || cleanName.contains(it) }) {
                        merged.add(fest)
                        existingNames.add(cleanName)
                    }
                }

                val finalFestivals = if (merged.isNotEmpty()) {
                    merged.sortedWith(compareBy({ it.month }, { it.day }))
                } else {
                    emptyList()
                }

                CoroutineScope(Dispatchers.Main).launch {
                    if (finalFestivals.isNotEmpty()) {
                        onlineFestivals = finalFestivals
                    }
                    isFetchingOnline = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    isFetchingOnline = false
                }
            }
        }
    }

    private fun mapNagerHolidayToFestival(nager: NagerHoliday): Festival {
        val parts = nager.date.split("-")
        val yearVal = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val monthVal = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val dayVal = parts.getOrNull(2)?.toIntOrNull() ?: 1

        val nameLower = nager.name.lowercase()
        // Try to match against existing rich list to preserve nice description & ritual
        val match = FestivalData.list2026.firstOrNull {
            it.name.lowercase().contains(nameLower) || nameLower.contains(it.name.lowercase())
        }

        return Festival(
            name = nager.name,
            dateStr = nager.date,
            month = monthVal,
            day = dayVal,
            type = match?.type ?: FestivalType.NATIONAL,
            description = match?.description ?: "Public holiday in India celebrating ${nager.name}.",
            ritualOrTip = match?.ritualOrTip ?: "Spend peaceful time with your family, follow national celebrations, or enjoy a restful holiday."
        )
    }

    fun getFestivalsForMonth(monthVal: Int): List<Festival> {
        return getAllFestivals().filter { it.month == monthVal }
    }

    private fun loadCustomFestivals() {
        val jsonStr = prefs.getString(KEY_CUSTOM_FESTIVALS, null)
        if (jsonStr.isNullOrEmpty()) {
            customFestivals = emptyList()
            return
        }

        try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<Festival>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.optString("type", FestivalType.REGIONAL.name)
                val type = try {
                    FestivalType.valueOf(typeStr)
                } catch (e: Exception) {
                    FestivalType.REGIONAL
                }

                list.add(
                    Festival(
                        name = obj.getString("name"),
                        dateStr = obj.optString("dateStr", ""),
                        month = obj.getInt("month"),
                        day = obj.getInt("day"),
                        type = type,
                        description = obj.optString("description", ""),
                        ritualOrTip = obj.optString("ritualOrTip", "")
                    )
                )
            }
            customFestivals = list
        } catch (e: Exception) {
            e.printStackTrace()
            customFestivals = emptyList()
        }
    }

    private fun saveCustomFestivals() {
        try {
            val arr = JSONArray()
            customFestivals.forEach { fest ->
                val obj = JSONObject().apply {
                    put("name", fest.name)
                    put("dateStr", fest.dateStr)
                    put("month", fest.month)
                    put("day", fest.day)
                    put("type", fest.type.name)
                    put("description", fest.description)
                    put("ritualOrTip", fest.ritualOrTip)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_CUSTOM_FESTIVALS, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
