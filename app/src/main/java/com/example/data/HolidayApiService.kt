package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class NagerHoliday(
    val date: String,       // e.g. "2026-01-26"
    val localName: String,  // e.g. "Republic Day"
    val name: String,       // e.g. "Republic Day"
    val countryCode: String,
    val fixed: Boolean,
    val global: Boolean,
    val counties: List<String>?,
    val launchYear: Int?,
    val types: List<String>?
)

interface HolidayApiService {
    @GET("publicholidays/{year}/IN")
    suspend fun getIndianHolidays(@Path("year") year: Int): List<NagerHoliday>

    companion object {
        private const val BASE_URL = "https://date.nager.at/api/v3/"

        fun create(): HolidayApiService {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(HolidayApiService::class.java)
        }
    }
}
