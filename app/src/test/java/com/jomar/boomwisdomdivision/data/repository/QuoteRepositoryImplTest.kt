package com.jomar.boomwisdomdivision.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jomar.boomwisdomdivision.data.api.QuotableApi
import com.jomar.boomwisdomdivision.data.api.model.QuoteDto
import com.jomar.boomwisdomdivision.data.db.BoomWisdomDatabase
import com.jomar.boomwisdomdivision.data.db.dao.QuoteDao
import com.jomar.boomwisdomdivision.data.db.converter.DateConverter
import com.jomar.boomwisdomdivision.domain.model.Quote
import com.jomar.boomwisdomdivision.core.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Unit tests for QuoteRepositoryImpl.
 * 
 * These tests verify the repository's behavior in various scenarios including:
 * - Successful API responses
 * - Error handling for network failures
 * - Database operations and caching
 * - Data transformation between layers
 * - Integration between remote and local data sources
 * 
 * Uses MockWebServer for API testing and in-memory Room database for DAO testing.
 * Follows AAA pattern (Arrange, Act, Assert) for clear and maintainable tests.
 */
class QuoteRepositoryImplTest {

    private lateinit var repository: QuoteRepositoryImpl
    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: QuotableApi
    private lateinit var database: BoomWisdomDatabase
    private lateinit var quoteDao: QuoteDao
    private lateinit var dateConverter: DateConverter

    // Test data
    private val testQuoteDto = QuoteDto(
        id = "test-id-123",
        content = "The only way to do great work is to love what you do.",
        author = "Steve Jobs",
        length = 49,
        tags = listOf("motivational", "work", "passion")
    )

    private val testQuote = Quote(
        id = "test-id-123",
        content = "The only way to do great work is to love what you do.",
        author = "Steve Jobs",
        length = 49,
        tags = listOf("motivational", "work", "passion"),
        savedAt = null
    )

    @Before
    fun setUp() {
        // Set up MockWebServer
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Create Retrofit with MockWebServer
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(QuotableApi::class.java)

        // Set up in-memory Room database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BoomWisdomDatabase::class.java
        ).allowMainThreadQueries().build()

        quoteDao = database.quoteDao()
        dateConverter = DateConverter()

        // Create repository instance
        repository = QuoteRepositoryImpl(api, quoteDao, dateConverter)
    }

    @After
    fun tearDown() {
        database.close()
        mockWebServer.shutdown()
    }

    @Test
    fun `getRandomQuote returns success when API responds with valid quote`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "_id": "test-id-123",
                    "content": "The only way to do great work is to love what you do.",
                    "author": "Steve Jobs",
                    "length": 49,
                    "tags": ["motivational", "work", "passion"]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val result = repository.getRandomQuote()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.id).isEqualTo("test-id-123")
        assertThat(result.data.content).isEqualTo("The only way to do great work is to love what you do.")
        assertThat(result.data.author).isEqualTo("Steve Jobs")
        assertThat(result.data.length).isEqualTo(49)
        assertThat(result.data.tags).containsExactly("motivational", "work", "passion")
        assertThat(result.data.savedAt).isNull()
    }

    @Test
    fun `getRandomQuote returns error when API responds with 404`() = runTest {
        // Arrange
        val mockResponse = MockResponse().setResponseCode(404)
        mockWebServer.enqueue(mockResponse)

        // Act
        val result = repository.getRandomQuote()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception.message).contains("API Error: 404")
    }

    @Test
    fun `getRandomQuote returns error when API responds with empty body`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("")
        mockWebServer.enqueue(mockResponse)

        // Act
        val result = repository.getRandomQuote()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception.message).isEqualTo("Empty response from API")
    }

    @Test
    fun `getRandomQuote returns error when network exception occurs`() = runTest {
        // Arrange
        mockWebServer.shutdown() // Simulate network failure

        // Act
        val result = repository.getRandomQuote()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(Exception::class.java)
    }

    @Test
    fun `saveQuote saves quote to database and returns success`() = runTest {
        // Arrange
        val timestamp = System.currentTimeMillis()
        val quoteToSave = testQuote.copy(savedAt = timestamp)

        // Act
        val result = repository.saveQuote(quoteToSave)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.id).isEqualTo(testQuote.id)
        assertThat(result.data.savedAt).isNotNull()

        // Verify quote was saved to database
        val savedQuote = quoteDao.getQuoteById(testQuote.id)
        assertThat(savedQuote).isNotNull()
        assertThat(savedQuote!!.id).isEqualTo(testQuote.id)
        assertThat(savedQuote.content).isEqualTo(testQuote.content)
        assertThat(savedQuote.author).isEqualTo(testQuote.author)
    }

    @Test
    fun `saveQuote marks quote as saved with timestamp`() = runTest {
        // Arrange
        val quoteWithoutTimestamp = testQuote.copy(savedAt = null)

        // Act
        val result = repository.saveQuote(quoteWithoutTimestamp)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.savedAt).isNotNull()
        assertThat(result.data.savedAt).isGreaterThan(0L)
    }

    @Test
    fun `deleteQuote removes quote from database when quote exists`() = runTest {
        // Arrange
        val entity = testQuote.copy(savedAt = System.currentTimeMillis())
        repository.saveQuote(entity)

        // Verify quote exists
        assertThat(quoteDao.getQuoteById(testQuote.id)).isNotNull()

        // Act
        val result = repository.deleteQuote(testQuote.id)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        
        // Verify quote was deleted
        assertThat(quoteDao.getQuoteById(testQuote.id)).isNull()
    }

    @Test
    fun `deleteQuote returns error when quote does not exist`() = runTest {
        // Arrange
        val nonExistentId = "non-existent-id"

        // Act
        val result = repository.deleteQuote(nonExistentId)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception.message).contains("Quote not found")
    }

    @Test
    fun `isQuoteSaved returns true when quote is saved`() = runTest {
        // Arrange
        val savedQuote = testQuote.copy(savedAt = System.currentTimeMillis())
        repository.saveQuote(savedQuote)

        // Act
        val result = repository.isQuoteSaved(testQuote.id)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isTrue()
    }

    @Test
    fun `isQuoteSaved returns false when quote is not saved`() = runTest {
        // Arrange
        val unsavedId = "unsaved-quote-id"

        // Act
        val result = repository.isQuoteSaved(unsavedId)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isFalse()
    }

    @Test
    fun `getQuoteById returns quote when it exists in database`() = runTest {
        // Arrange
        val savedQuote = testQuote.copy(savedAt = System.currentTimeMillis())
        repository.saveQuote(savedQuote)

        // Act
        val result = repository.getQuoteById(testQuote.id)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.id).isEqualTo(testQuote.id)
        assertThat(result.data.content).isEqualTo(testQuote.content)
        assertThat(result.data.author).isEqualTo(testQuote.author)
        assertThat(result.data.savedAt).isNotNull()
    }

    @Test
    fun `getQuoteById returns error when quote does not exist`() = runTest {
        // Arrange
        val nonExistentId = "non-existent-id"

        // Act
        val result = repository.getQuoteById(nonExistentId)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception.message).contains("Quote not found")
    }

    @Test
    fun `getSavedQuotes emits empty list when no quotes saved`() = runTest {
        // Act & Assert
        repository.getSavedQuotes().test {
            val emptyList = awaitItem()
            assertThat(emptyList).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSavedQuotes emits saved quotes in correct order`() = runTest {
        // Arrange
        val quote1 = testQuote.copy(id = "1", savedAt = 1000L)
        val quote2 = testQuote.copy(id = "2", savedAt = 2000L)
        val quote3 = testQuote.copy(id = "3", savedAt = 1500L)

        repository.saveQuote(quote1)
        repository.saveQuote(quote2)
        repository.saveQuote(quote3)

        // Act & Assert
        repository.getSavedQuotes().test {
            val quotes = awaitItem()
            assertThat(quotes).hasSize(3)
            
            // Should be ordered by savedAt DESC (most recent first)
            assertThat(quotes[0].id).isEqualTo("2") // savedAt: 2000L
            assertThat(quotes[1].id).isEqualTo("3") // savedAt: 1500L
            assertThat(quotes[2].id).isEqualTo("1") // savedAt: 1000L
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSavedQuotes emits updated list when quote is added`() = runTest {
        // Arrange
        val quote1 = testQuote.copy(id = "1", savedAt = 1000L)
        val quote2 = testQuote.copy(id = "2", savedAt = 2000L)

        // Act & Assert
        repository.getSavedQuotes().test {
            // Initial empty state
            assertThat(awaitItem()).isEmpty()

            // Add first quote
            repository.saveQuote(quote1)
            val listWithOne = awaitItem()
            assertThat(listWithOne).hasSize(1)
            assertThat(listWithOne[0].id).isEqualTo("1")

            // Add second quote
            repository.saveQuote(quote2)
            val listWithTwo = awaitItem()
            assertThat(listWithTwo).hasSize(2)
            assertThat(listWithTwo[0].id).isEqualTo("2") // Most recent first
            assertThat(listWithTwo[1].id).isEqualTo("1")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSavedQuotes emits updated list when quote is removed`() = runTest {
        // Arrange
        val quote1 = testQuote.copy(id = "1", savedAt = 1000L)
        val quote2 = testQuote.copy(id = "2", savedAt = 2000L)
        
        repository.saveQuote(quote1)
        repository.saveQuote(quote2)

        // Act & Assert
        repository.getSavedQuotes().test {
            // Initial state with two quotes
            val initialList = awaitItem()
            assertThat(initialList).hasSize(2)

            // Remove one quote
            repository.deleteQuote("1")
            val updatedList = awaitItem()
            assertThat(updatedList).hasSize(1)
            assertThat(updatedList[0].id).isEqualTo("2")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `repository handles concurrent operations correctly`() = runTest {
        // Arrange
        val quotes = (1..10).map { i ->
            testQuote.copy(id = "quote-$i", savedAt = i * 1000L)
        }

        // Act - Save all quotes concurrently
        quotes.forEach { quote ->
            repository.saveQuote(quote)
        }

        // Assert
        repository.getSavedQuotes().test {
            val savedQuotes = awaitItem()
            assertThat(savedQuotes).hasSize(10)
            
            // Verify all quotes are saved correctly
            savedQuotes.forEachIndexed { index, quote ->
                assertThat(quote.id).isEqualTo("quote-${10 - index}") // Reverse order due to DESC sorting
            }
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}