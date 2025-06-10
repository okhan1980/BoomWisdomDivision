package com.jomar.boomwisdomdivision

import com.jomar.boomwisdomdivision.data.api.QuotableApi
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Simple test to verify DummyJSON API connectivity
 * Run this test to ensure the API is accessible from your environment
 */
class ApiConnectivityTest {
    
    @Test
    fun `test DummyJSON API connectivity`() {
        println("========================================")
        println("Testing DummyJSON API Connectivity")
        println("========================================")
        
        val api = QuotableApi()
        
        runBlocking {
            println("\n1. Testing single random quote endpoint...")
            val result = api.getRandomQuote()
            
            if (result.isSuccess) {
                val quote = result.getOrNull()
                println("✅ SUCCESS: API is accessible!")
                println("   Quote: \"${quote?.content}\"")
                println("   Author: ${quote?.author}")
                println("   ID: ${quote?.id}")
            } else {
                println("❌ FAILED: ${result.exceptionOrNull()?.message}")
                result.exceptionOrNull()?.printStackTrace()
            }
            
            println("\n2. Testing multiple quotes fetch...")
            val multiResult = api.getMultipleRandomQuotes(3)
            
            if (multiResult.isSuccess) {
                val quotes = multiResult.getOrNull()
                println("✅ SUCCESS: Fetched ${quotes?.size} quotes")
                quotes?.forEachIndexed { index, quote ->
                    println("   ${index + 1}. \"${quote.content.take(50)}...\" - ${quote.author}")
                }
            } else {
                println("❌ FAILED: ${multiResult.exceptionOrNull()?.message}")
            }
        }
        
        println("\n========================================")
        println("API Connectivity Test Complete")
        println("========================================")
    }
    
    @Test
    fun `test direct HTTP call to DummyJSON`() {
        println("\n========================================")
        println("Testing Direct HTTP Call to DummyJSON")
        println("========================================")
        
        runBlocking {
            try {
                val url = java.net.URL("https://dummyjson.com/quotes/random")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                val responseCode = connection.responseCode
                println("Response Code: $responseCode")
                
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    println("✅ Raw Response: $response")
                } else {
                    println("❌ HTTP Error: $responseCode")
                }
                
                connection.disconnect()
            } catch (e: Exception) {
                println("❌ Connection Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}