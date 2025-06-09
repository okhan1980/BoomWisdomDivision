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
 * Simple HTTP client for DummyJSON Quotes API without Retrofit
 * Following Phase 3 requirement for basic OkHttp implementation
 */
class QuotableApi {
    
    companion object {
        private const val BASE_URL = "https://dummyjson.com"
        private const val RANDOM_QUOTE_ENDPOINT = "/quotes/random"
    }
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // DummyJSON returns direct quote objects
    private val quoteAdapter = moshi.adapter<QuoteResponse>(QuoteResponse::class.java)
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            
            val request = originalRequest.newBuilder()
                .header("Cache-Control", "no-cache")
                .build()
            
            println("🌐 HTTP Request: ${request.url}")
            val response = chain.proceed(request)
            println("🌐 HTTP Response: ${response.code} ${response.message}")
            response
        }
        .connectTimeout(15.seconds.inWholeMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS)
        .readTimeout(15.seconds.inWholeMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()
    
    /**
     * Fetch a random quote from DummyJSON API
     * @return QuoteResponse on success
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
                    println("📥 Raw API Response: $responseBody")
                    
                    try {
                        // DummyJSON returns a direct quote object
                        val quote = quoteAdapter.fromJson(responseBody)
                        
                        if (quote != null) {
                            println("✅ Quote: \"${quote.content}\" - ${quote.author} (ID: ${quote.id})")
                            Result.success(quote)
                        } else {
                            val error = "Failed to parse quote response. Raw response: $responseBody"
                            println("❌ Parse Error: $error")
                            Result.failure(Exception(error))
                        }
                    } catch (e: Exception) {
                        println("❌ JSON Parse Exception: ${e.message}")
                        e.printStackTrace()
                        Result.failure(Exception("JSON parsing failed: ${e.message}"))
                    }
                } else {
                    println("❌ Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val error = "HTTP ${response.code}: ${response.message}"
                println("❌ HTTP Error: $error")
                Result.failure(Exception(error))
            }
        } catch (e: IOException) {
            println("❌ Network Error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("❌ Unexpected Error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Fetch multiple random quotes for caching (DummyJSON doesn't support bulk, so we call individually)
     * @param count Number of quotes to fetch (default: 10)
     * @return List of QuoteResponse objects
     */
    suspend fun getMultipleRandomQuotes(count: Int = 10): Result<List<QuoteResponse>> = withContext(Dispatchers.IO) {
        try {
            val quotes = mutableListOf<QuoteResponse>()
            val maxAttempts = count * 2 // Try more attempts to get unique quotes
            var attempts = 0
            val seenIds = mutableSetOf<Int>()
            
            println("Fetching $count quotes from DummyJSON...")
            
            while (quotes.size < count && attempts < maxAttempts) {
                attempts++
                
                val result = getRandomQuote()
                if (result.isSuccess) {
                    val quote = result.getOrNull()
                    if (quote != null && !seenIds.contains(quote.id)) {
                        quotes.add(quote)
                        seenIds.add(quote.id)
                        println("✅ Got unique quote ${quotes.size}/$count: \"${quote.content.take(50)}...\"")
                    }
                } else {
                    println("❌ Failed to fetch quote on attempt $attempts")
                    // Add small delay between requests
                    kotlinx.coroutines.delay(100)
                }
                
                // Small delay to be respectful to the API
                kotlinx.coroutines.delay(200)
            }
            
            if (quotes.isNotEmpty()) {
                println("✅ Successfully fetched ${quotes.size} unique quotes in $attempts attempts")
                Result.success(quotes)
            } else {
                Result.failure(Exception("Failed to fetch any quotes after $attempts attempts"))
            }
        } catch (e: Exception) {
            println("❌ Exception in getMultipleRandomQuotes: ${e.message}")
            Result.failure(e)
        }
    }
    
}