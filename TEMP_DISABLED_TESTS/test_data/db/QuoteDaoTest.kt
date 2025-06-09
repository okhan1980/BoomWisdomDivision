package com.jomar.boomwisdomdivision.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jomar.boomwisdomdivision.data.db.dao.QuoteDao
import com.jomar.boomwisdomdivision.data.db.entity.QuoteEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for QuoteDao.
 * 
 * These tests verify the DAO's database operations including:
 * - Insert operations (single and batch)
 * - Query operations (all, by ID, by author, search)
 * - Delete operations (single, by ID, all)
 * - Flow-based reactive queries
 * - Database constraints and conflict resolution
 * 
 * Uses in-memory Room database for fast, isolated testing.
 * Tests follow AAA pattern for clarity and maintainability.
 */
class QuoteDaoTest {

    private lateinit var database: BoomWisdomDatabase
    private lateinit var quoteDao: QuoteDao

    // Test data
    private val testQuote1 = QuoteEntity(
        id = "quote-1",
        content = "The only way to do great work is to love what you do.",
        author = "Steve Jobs",
        length = 49,
        tags = """["motivational", "work", "passion"]""",
        savedAt = 1000L
    )

    private val testQuote2 = QuoteEntity(
        id = "quote-2",
        content = "Innovation distinguishes between a leader and a follower.",
        author = "Steve Jobs",
        length = 56,
        tags = """["innovation", "leadership", "business"]""",
        savedAt = 2000L
    )

    private val testQuote3 = QuoteEntity(
        id = "quote-3",
        content = "Imagination is more important than knowledge.",
        author = "Albert Einstein",
        length = 43,
        tags = """["wisdom", "knowledge", "imagination"]""",
        savedAt = 1500L
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BoomWisdomDatabase::class.java
        ).allowMainThreadQueries().build()

        quoteDao = database.quoteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertQuote saves quote to database`() = runTest {
        // Act
        quoteDao.insertQuote(testQuote1)

        // Assert
        val savedQuote = quoteDao.getQuoteById(testQuote1.id)
        assertThat(savedQuote).isNotNull()
        assertThat(savedQuote!!.id).isEqualTo(testQuote1.id)
        assertThat(savedQuote.content).isEqualTo(testQuote1.content)
        assertThat(savedQuote.author).isEqualTo(testQuote1.author)
        assertThat(savedQuote.length).isEqualTo(testQuote1.length)
        assertThat(savedQuote.tags).isEqualTo(testQuote1.tags)
        assertThat(savedQuote.savedAt).isEqualTo(testQuote1.savedAt)
    }

    @Test
    fun `insertQuote replaces existing quote with same ID`() = runTest {
        // Arrange
        quoteDao.insertQuote(testQuote1)
        val updatedQuote = testQuote1.copy(
            content = "Updated quote content",
            savedAt = 3000L
        )

        // Act
        quoteDao.insertQuote(updatedQuote)

        // Assert
        val retrievedQuote = quoteDao.getQuoteById(testQuote1.id)
        assertThat(retrievedQuote).isNotNull()
        assertThat(retrievedQuote!!.content).isEqualTo("Updated quote content")
        assertThat(retrievedQuote.savedAt).isEqualTo(3000L)
        
        // Verify only one quote exists
        val allQuotes = quoteDao.getAllQuotes()
        allQuotes.test {
            val quotes = awaitItem()
            assertThat(quotes).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertQuotes saves multiple quotes`() = runTest {
        // Arrange
        val quotes = listOf(testQuote1, testQuote2, testQuote3)

        // Act
        quoteDao.insertQuotes(quotes)

        // Assert
        quoteDao.getAllQuotes().test {
            val savedQuotes = awaitItem()
            assertThat(savedQuotes).hasSize(3)
            
            // Verify quotes are ordered by savedAt DESC
            assertThat(savedQuotes[0].id).isEqualTo("quote-2") // savedAt: 2000L
            assertThat(savedQuotes[1].id).isEqualTo("quote-3") // savedAt: 1500L
            assertThat(savedQuotes[2].id).isEqualTo("quote-1") // savedAt: 1000L
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getQuoteById returns null when quote does not exist`() = runTest {
        // Act
        val result = quoteDao.getQuoteById("non-existent-id")

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `getAllQuotes returns empty list when no quotes saved`() = runTest {
        // Act & Assert
        quoteDao.getAllQuotes().test {
            val quotes = awaitItem()
            assertThat(quotes).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllQuotes returns quotes ordered by savedAt DESC`() = runTest {
        // Arrange
        quoteDao.insertQuotes(listOf(testQuote1, testQuote2, testQuote3))

        // Act & Assert
        quoteDao.getAllQuotes().test {
            val quotes = awaitItem()
            assertThat(quotes).hasSize(3)
            
            // Verify ordering by savedAt DESC (most recent first)
            assertThat(quotes[0].savedAt).isEqualTo(2000L) // testQuote2
            assertThat(quotes[1].savedAt).isEqualTo(1500L) // testQuote3
            assertThat(quotes[2].savedAt).isEqualTo(1000L) // testQuote1
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isQuoteSaved returns true when quote exists`() = runTest {
        // Arrange
        quoteDao.insertQuote(testQuote1)

        // Act
        val isSaved = quoteDao.isQuoteSaved(testQuote1.id)

        // Assert
        assertThat(isSaved).isTrue()
    }

    @Test
    fun `isQuoteSaved returns false when quote does not exist`() = runTest {
        // Act
        val isSaved = quoteDao.isQuoteSaved("non-existent-id")

        // Assert
        assertThat(isSaved).isFalse()
    }

    @Test
    fun `deleteQuote removes specific quote`() = runTest {
        // Arrange
        quoteDao.insertQuotes(listOf(testQuote1, testQuote2))

        // Act
        quoteDao.deleteQuote(testQuote1)

        // Assert
        assertThat(quoteDao.getQuoteById(testQuote1.id)).isNull()
        assertThat(quoteDao.getQuoteById(testQuote2.id)).isNotNull()
        
        quoteDao.getAllQuotes().test {
            val quotes = awaitItem()
            assertThat(quotes).hasSize(1)
            assertThat(quotes[0].id).isEqualTo(testQuote2.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteQuoteById removes quote and returns count`() = runTest {
        // Arrange
        quoteDao.insertQuote(testQuote1)

        // Act
        val deletedCount = quoteDao.deleteQuoteById(testQuote1.id)

        // Assert
        assertThat(deletedCount).isEqualTo(1)
        assertThat(quoteDao.getQuoteById(testQuote1.id)).isNull()
    }

    @Test
    fun `deleteQuoteById returns 0 when quote does not exist`() = runTest {
        // Act
        val deletedCount = quoteDao.deleteQuoteById("non-existent-id")

        // Assert
        assertThat(deletedCount).isEqualTo(0)
    }

    @Test
    fun `getQuotesByAuthor returns quotes by specific author`() = runTest {
        // Arrange
        quoteDao.insertQuotes(listOf(testQuote1, testQuote2, testQuote3))

        // Act & Assert
        quoteDao.getQuotesByAuthor("Steve Jobs").test {
            val steveJobsQuotes = awaitItem()
            assertThat(steveJobsQuotes).hasSize(2)
            assertThat(steveJobsQuotes.all { it.author == "Steve Jobs" }).isTrue()
            
            // Should be ordered by savedAt DESC
            assertThat(steveJobsQuotes[0].id).isEqualTo("quote-2") // savedAt: 2000L
            assertThat(steveJobsQuotes[1].id).isEqualTo("quote-1") // savedAt: 1000L
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getQuotesByAuthor handles case insensitive search`() = runTest {
        // Arrange
        quoteDao.insertQuote(testQuote1)

        // Act & Assert
        quoteDao.getQuotesByAuthor("steve jobs").test {
            val quotes = awaitItem()
            assertThat(quotes).hasSize(1)
            assertThat(quotes[0].author).isEqualTo("Steve Jobs")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getQuotesByAuthor returns empty list when no matches`() = runTest {
        // Arrange
        quoteDao.insertQuote(testQuote1)

        // Act & Assert
        quoteDao.getQuotesByAuthor("Unknown Author").test {
            val quotes = awaitItem()
            assertThat(quotes).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuotesByContent finds quotes containing search text`() = runTest {
        // Arrange
        quoteDao.insertQuotes(listOf(testQuote1, testQuote2, testQuote3))

        // Act & Assert
        quoteDao.searchQuotesByContent("work").test {
            val matchingQuotes = awaitItem()
            assertThat(matchingQuotes).hasSize(1)
            assertThat(matchingQuotes[0].content).contains("work")
            assertThat(matchingQuotes[0].id).isEqualTo("quote-1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuotesByContent handles case insensitive search`() = runTest {
        // Arrange
        quoteDao.insertQuote(testQuote1)

        // Act & Assert
        quoteDao.searchQuotesByContent("GREAT WORK").test {
            val quotes = awaitItem()
            assertThat(quotes).hasSize(1)
            assertThat(quotes[0].content).contains("great work")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuotesByContent returns empty list when no matches`() = runTest {
        // Arrange
        quoteDao.insertQuote(testQuote1)

        // Act & Assert
        quoteDao.searchQuotesByContent("nonexistent").test {
            val quotes = awaitItem()
            assertThat(quotes).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getQuoteCount returns correct count`() = runTest {
        // Arrange
        assertThat(quoteDao.getQuoteCount()).isEqualTo(0)
        
        quoteDao.insertQuote(testQuote1)
        assertThat(quoteDao.getQuoteCount()).isEqualTo(1)
        
        quoteDao.insertQuotes(listOf(testQuote2, testQuote3))
        assertThat(quoteDao.getQuoteCount()).isEqualTo(3)
    }

    @Test
    fun `deleteAllQuotes removes all quotes`() = runTest {
        // Arrange
        quoteDao.insertQuotes(listOf(testQuote1, testQuote2, testQuote3))
        assertThat(quoteDao.getQuoteCount()).isEqualTo(3)

        // Act
        quoteDao.deleteAllQuotes()

        // Assert
        assertThat(quoteDao.getQuoteCount()).isEqualTo(0)
        
        quoteDao.getAllQuotes().test {
            val quotes = awaitItem()
            assertThat(quotes).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllQuotes emits updates when quotes are added`() = runTest {
        // Act & Assert
        quoteDao.getAllQuotes().test {
            // Initial empty state
            assertThat(awaitItem()).isEmpty()

            // Add first quote
            quoteDao.insertQuote(testQuote1)
            val quotesAfterFirst = awaitItem()
            assertThat(quotesAfterFirst).hasSize(1)
            assertThat(quotesAfterFirst[0].id).isEqualTo(testQuote1.id)

            // Add second quote
            quoteDao.insertQuote(testQuote2)
            val quotesAfterSecond = awaitItem()
            assertThat(quotesAfterSecond).hasSize(2)
            // Most recent should be first
            assertThat(quotesAfterSecond[0].id).isEqualTo(testQuote2.id)
            assertThat(quotesAfterSecond[1].id).isEqualTo(testQuote1.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllQuotes emits updates when quotes are deleted`() = runTest {
        // Arrange
        quoteDao.insertQuotes(listOf(testQuote1, testQuote2))

        // Act & Assert
        quoteDao.getAllQuotes().test {
            // Initial state with two quotes
            val initialQuotes = awaitItem()
            assertThat(initialQuotes).hasSize(2)

            // Delete one quote
            quoteDao.deleteQuoteById(testQuote1.id)
            val quotesAfterDelete = awaitItem()
            assertThat(quotesAfterDelete).hasSize(1)
            assertThat(quotesAfterDelete[0].id).isEqualTo(testQuote2.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `database enforces primary key constraint`() = runTest {
        // Arrange
        quoteDao.insertQuote(testQuote1)
        val duplicateId = testQuote1.copy(content = "Different content")

        // Act - Insert quote with same ID should replace existing
        quoteDao.insertQuote(duplicateId)

        // Assert
        val retrievedQuote = quoteDao.getQuoteById(testQuote1.id)
        assertThat(retrievedQuote).isNotNull()
        assertThat(retrievedQuote!!.content).isEqualTo("Different content")
        
        // Verify only one quote exists
        assertThat(quoteDao.getQuoteCount()).isEqualTo(1)
    }

    @Test
    fun `queries handle special characters correctly`() = runTest {
        // Arrange
        val specialQuote = testQuote1.copy(
            id = "special-chars",
            content = "Quote with special chars: àáâãäåæçèéêë & \"quotes\" 🚀",
            author = "Spëcîål Àüthør"
        )
        quoteDao.insertQuote(specialQuote)

        // Act & Assert
        val retrievedQuote = quoteDao.getQuoteById("special-chars")
        assertThat(retrievedQuote).isNotNull()
        assertThat(retrievedQuote!!.content).contains("àáâãäåæçèéêë")
        assertThat(retrievedQuote.content).contains("\"quotes\"")
        assertThat(retrievedQuote.content).contains("🚀")
        assertThat(retrievedQuote.author).isEqualTo("Spëcîål Àüthør")

        // Test search with special characters
        quoteDao.searchQuotesByContent("àáâãäåæçèéêë").test {
            val searchResults = awaitItem()
            assertThat(searchResults).hasSize(1)
            assertThat(searchResults[0].id).isEqualTo("special-chars")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
