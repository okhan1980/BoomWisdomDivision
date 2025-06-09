package com.jomar.boomwisdomdivision.model

/**
 * Simple data class for representing a quote
 */
data class Quote(
    val text: String,
    val author: String,
    val id: Int = text.hashCode()
)

/**
 * Hardcoded inspirational quotes for Phase 2
 */
object QuoteRepository {
    private val quotes = listOf(
        Quote(
            text = "The only way to do great work is to love what you do.",
            author = "Steve Jobs"
        ),
        Quote(
            text = "Innovation distinguishes between a leader and a follower.",
            author = "Steve Jobs"
        ),
        Quote(
            text = "Life is what happens to you while you're busy making other plans.",
            author = "John Lennon"
        ),
        Quote(
            text = "The future belongs to those who believe in the beauty of their dreams.",
            author = "Eleanor Roosevelt"
        ),
        Quote(
            text = "It is during our darkest moments that we must focus to see the light.",
            author = "Aristotle"
        ),
        Quote(
            text = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            author = "Winston Churchill"
        ),
        Quote(
            text = "The way to get started is to quit talking and begin doing.",
            author = "Walt Disney"
        ),
        Quote(
            text = "Don't let yesterday take up too much of today.",
            author = "Will Rogers"
        ),
        Quote(
            text = "You learn more from failure than from success. Don't let it stop you.",
            author = "Unknown"
        ),
        Quote(
            text = "If you are working on something that you really care about, you don't have to be pushed.",
            author = "Steve Jobs"
        ),
        Quote(
            text = "Whether you think you can or you think you can't, you're right.",
            author = "Henry Ford"
        ),
        Quote(
            text = "The only impossible journey is the one you never begin.",
            author = "Tony Robbins"
        )
    )
    
    fun getRandomQuote(): Quote {
        return quotes.random()
    }
    
    fun getAllQuotes(): List<Quote> {
        return quotes
    }
    
    fun getQuoteByIndex(index: Int): Quote {
        return quotes[index % quotes.size]
    }
    
    fun getQuoteCount(): Int {
        return quotes.size
    }
}