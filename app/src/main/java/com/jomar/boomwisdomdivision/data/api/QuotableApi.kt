package com.jomar.boomwisdomdivision.data.api

import com.jomar.boomwisdomdivision.data.model.QuoteResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

/**
 * Simple HTTP client for Quotable API without Retrofit
 * Following Phase 3 requirement for basic OkHttp implementation
 */
class QuotableApi {
    
    companion object {
        private const val BASE_URL = "https://api.quotable.io"
        private const val RANDOM_QUOTE_ENDPOINT = "/quotes/random"
        private const val MOTIVATIONAL_TAGS = "?tags=motivational|inspirational|wisdom"
    }
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val quoteResponseAdapter = moshi.adapter(QuoteResponse::class.java)
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30.seconds.inWholeMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS)
        .readTimeout(30.seconds.inWholeMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()
    
    /**
     * Fetch a random motivational quote from Quotable API
     * @return QuoteResponse on success, null on failure
     */
    suspend fun getRandomQuote(): Result<QuoteResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL$RANDOM_QUOTE_ENDPOINT$MOTIVATIONAL_TAGS")
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val quoteResponse = quoteResponseAdapter.fromJson(responseBody)
                    if (quoteResponse != null) {
                        Result.success(quoteResponse)
                    } else {
                        Result.failure(Exception("Failed to parse quote response"))
                    }
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetch multiple random quotes for caching
     * @param count Number of quotes to fetch (default: 10)
     * @return List of QuoteResponse objects
     */
    suspend fun getMultipleRandomQuotes(count: Int = 10): Result<List<QuoteResponse>> = withContext(Dispatchers.IO) {
        try {
            val quotes = mutableListOf<QuoteResponse>()
            
            // Fetch quotes one by one to ensure variety
            repeat(count) {
                val result = getRandomQuote()
                if (result.isSuccess) {
                    result.getOrNull()?.let { quotes.add(it) }
                }
                
                // Small delay to avoid hitting rate limits
                kotlinx.coroutines.delay(100)
            }
            
            if (quotes.isNotEmpty()) {
                Result.success(quotes)
            } else {
                Result.failure(Exception("Failed to fetch any quotes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}