package com.jomar.boomwisdomdivision

import com.jomar.boomwisdomdivision.data.model.QuoteResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify JSON parsing matches DummyJSON API format
 */
class JsonParsingTest {
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    @Test
    fun `test parsing DummyJSON single quote JSON format`() {
        println("========================================")
        println("Testing DummyJSON Single Quote JSON Parsing")
        println("========================================")
        
        // Sample response from DummyJSON API
        val sampleJson = """{"id":123,"quote":"The only way to do great work is to love what you do.","author":"Steve Jobs"}"""
        
        println("Sample JSON: $sampleJson")
        
        // Create adapter for direct quote object
        val quoteAdapter = moshi.adapter<com.jomar.boomwisdomdivision.data.model.QuoteResponse>(
            com.jomar.boomwisdomdivision.data.model.QuoteResponse::class.java
        )
        
        try {
            val quote = quoteAdapter.fromJson(sampleJson)
            
            assertNotNull("Quote should not be null", quote)
            
            println("\n✅ Successfully parsed quote:")
            println("   ID: ${quote!!.id}")
            println("   Content: \"${quote.content}\"")
            println("   Author: ${quote.author}")
            
            // Verify fields are mapped correctly
            assertEquals(123, quote.id)
            assertEquals("The only way to do great work is to love what you do.", quote.content)
            assertEquals("Steve Jobs", quote.author)
            
        } catch (e: Exception) {
            println("❌ Parsing failed: ${e.message}")
            e.printStackTrace()
            fail("JSON parsing should succeed")
        }
    }
    
    @Test
    fun `test parsing multiple quotes`() {
        println("\n========================================")
        println("Testing Multiple Quotes Parsing")
        println("========================================")
        
        val multipleQuotesJson = """[
            {"id":1,"quote":"Quote 1","author":"Author 1"},
            {"id":2,"quote":"Quote 2","author":"Author 2"}
        ]"""
        
        val listAdapter = moshi.adapter<List<com.jomar.boomwisdomdivision.data.model.QuoteResponse>>(
            Types.newParameterizedType(List::class.java, com.jomar.boomwisdomdivision.data.model.QuoteResponse::class.java)
        )
        
        val quotes = listAdapter.fromJson(multipleQuotesJson)
        
        assertNotNull(quotes)
        assertEquals(2, quotes!!.size)
        
        println("✅ Successfully parsed ${quotes.size} quotes")
        quotes.forEachIndexed { index, quote ->
            println("   ${index + 1}. \"${quote.content}\" - ${quote.author} (ID: ${quote.id})")
        }
    }
    
    @Test
    fun `test handling special characters in quotes`() {
        println("\n========================================")
        println("Testing Special Characters Handling")
        println("========================================")
        
        val specialCharsJson = """{"id":123,"quote":"Life's what happens when you're busy making other plans.","author":"John Lennon"}"""
        
        val singleAdapter = moshi.adapter<com.jomar.boomwisdomdivision.data.model.QuoteResponse>(
            com.jomar.boomwisdomdivision.data.model.QuoteResponse::class.java
        )
        
        try {
            val quote = singleAdapter.fromJson(specialCharsJson)
            assertNotNull(quote)
            
            println("✅ Successfully parsed quote with special characters:")
            println("   \"${quote!!.content}\"")
            assertEquals("Life's what happens when you're busy making other plans.", quote.content)
            
        } catch (e: Exception) {
            println("❌ Failed to parse special characters: ${e.message}")
            fail("Should handle special characters")
        }
    }
}