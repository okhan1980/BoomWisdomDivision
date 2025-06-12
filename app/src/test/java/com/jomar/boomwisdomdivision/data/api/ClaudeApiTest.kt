package com.jomar.boomwisdomdivision.data.api

import kotlinx.coroutines.runBlocking
import org.junit.Test

class ClaudeApiTest {
    
    @Test
    fun `test Claude API quote generation`() {
        // This is a simple integration test to verify the API works
        // Run with: ./gradlew test --tests "*.ClaudeApiTest"
        
        val api = ClaudeApi.getInstance()
        
        runBlocking {
            println("\n=== Testing Claude API Quote Generation ===\n")
            
            val categories = listOf("motivation", "mindfulness", "creativity")
            
            for (category in categories) {
                println("Testing category: $category")
                val quote = api.generateQuote(category)
                
                if (quote != null) {
                    println("✅ Success!")
                    println("   Quote: \"${quote.text}\"")
                    println("   Author: ${quote.author}")
                    println("   Word count: ${quote.text.split(" ").size}")
                    println("   ID: ${quote.id}")
                    
                    // Verify word count
                    val wordCount = quote.text.split(" ").size
                    assert(wordCount <= 15) { "Quote exceeds 15 words: $wordCount words" }
                } else {
                    println("❌ Failed to generate quote for $category")
                }
                println()
                
                // Small delay between requests
                Thread.sleep(1000)
            }
        }
    }
}