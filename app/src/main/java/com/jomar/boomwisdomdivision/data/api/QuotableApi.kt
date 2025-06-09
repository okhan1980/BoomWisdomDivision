package com.jomar.boomwisdomdivision.data.api

import com.jomar.boomwisdomdivision.data.model.QuoteResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

/**
 * Simple HTTP client for ZenQuotes API without Retrofit
 * Following Phase 3 requirement for basic OkHttp implementation
 */
class QuotableApi {
    
    companion object {
        private const val BASE_URL = "https://zenquotes.io"
        private const val RANDOM_QUOTE_ENDPOINT = "/api/random"
        private const val TODAY_QUOTE_ENDPOINT = "/api/today"
    }
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // ZenQuotes returns an array, so we need to parse it as a list
    private val quoteListAdapter = moshi.adapter<List<QuoteResponse>>(
        Types.newParameterizedType(List::class.java, QuoteResponse::class.java)
    )
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30.seconds.inWholeMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS)
        .readTimeout(30.seconds.inWholeMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()
    
    /**
     * Fetch a random motivational quote from ZenQuotes API
     * @return QuoteResponse on success, null on failure
     */
    suspend fun getRandomQuote(): Result<QuoteResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL$RANDOM_QUOTE_ENDPOINT"
            println("API Request: $url") // Debug logging
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    println("API Response: $responseBody") // Debug logging
                    
                    // ZenQuotes returns an array, so parse as list and take first item
                    val quoteList = quoteListAdapter.fromJson(responseBody)
                    if (quoteList != null && quoteList.isNotEmpty()) {
                        val quote = quoteList.first()
                        Result.success(quote.copy(id = java.util.UUID.randomUUID().toString()))
                    } else {
                        val error = "Failed to parse quote response. Raw response: $responseBody"
                        println("Parse Error: $error")
                        Result.failure(Exception(error))
                    }
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val error = "HTTP ${response.code}: ${response.message}"
                println("HTTP Error: $error")
                Result.failure(Exception(error))
            }
        } catch (e: IOException) {
            println("Network Error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Unexpected Error: ${e.message}")
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
            
            // Fetch quotes one by one to ensure variety (ZenQuotes doesn't support batch)
            repeat(count) {
                val result = getRandomQuote()
                if (result.isSuccess) {
                    result.getOrNull()?.let { quotes.add(it) }
                }
                
                // Small delay to avoid hitting rate limits (100 requests/day limit)
                kotlinx.coroutines.delay(200)
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
    
    /**
     * Get today's featured quote
     * @return QuoteResponse for today's quote
     */
    suspend fun getTodaysQuote(): Result<QuoteResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL$TODAY_QUOTE_ENDPOINT"
            println("API Request (Today): $url") // Debug logging
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    println("API Response (Today): $responseBody") // Debug logging
                    
                    val quoteList = quoteListAdapter.fromJson(responseBody)
                    if (quoteList != null && quoteList.isNotEmpty()) {
                        val quote = quoteList.first()
                        Result.success(quote.copy(id = "today-${java.time.LocalDate.now()}"))
                    } else {
                        Result.failure(Exception("Failed to parse today's quote"))
                    }
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}