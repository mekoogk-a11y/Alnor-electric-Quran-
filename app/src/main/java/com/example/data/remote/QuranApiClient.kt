package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object QuranApiClient {

    private const val BASE_URL = "https://api.quran.com/api/v4/"

    private class RetryInterceptor(private val maxRetries: Int = 2) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .build()

            var attempt = 0
            var lastException: IOException? = null
            while (attempt <= maxRetries) {
                try {
                    val response = chain.proceed(request)
                    if (response.isSuccessful || (response.code !in listOf(429, 500, 502, 503, 504))) {
                        return response
                    }
                    response.close()
                } catch (e: IOException) {
                    lastException = e
                    if (attempt == maxRetries) throw e
                }
                attempt++
                try {
                    Thread.sleep(350L * attempt)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
            throw lastException ?: IOException("Quran API request failed after $maxRetries retries")
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    val apiService: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuranApiService::class.java)
    }

    fun normalizeAudioUrl(rawUrl: String): String {
        return when {
            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
            rawUrl.startsWith("//") -> "https:$rawUrl"
            else -> "https://verses.quran.foundation/${rawUrl.trimStart('/')}"
        }
    }
}
