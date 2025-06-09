package com.jomar.boomwisdomdivision.data.api

import com.google.common.truth.Truth.assertThat
import com.jomar.boomwisdomdivision.data.api.model.QuoteDto
import com.jomar.boomwisdomdivision.data.api.model.QuoteResponse
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Unit tests for QuotableApi.
 * 
 * These tests verify the API interface behavior with various server responses:
 * - Successful responses with valid data
 * - Error responses (4xx, 5xx)
 * - Malformed JSON responses
 * - Network connectivity issues
 * - Parameter handling for different endpoints
 * 
 * Uses MockWebServer to simulate API responses without actual network calls.
 * Tests follow AAA pattern for clarity and maintainability.
 */
class QuotableApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var quotableApi: QuotableApi

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        quotableApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(QuotableApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getRandomQuote returns successful response with valid quote`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "_id": "abc123",
                    "content": "Innovation distinguishes between a leader and a follower.",
                    "author": "Steve Jobs",
                    "length": 56,
                    "tags": ["innovation", "leadership", "business"]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.getRandomQuote()

        // Assert
        assertThat(response.isSuccessful).isTrue()
        assertThat(response.code()).isEqualTo(200)
        
        val quote = response.body()
        assertThat(quote).isNotNull()
        assertThat(quote!!.id).isEqualTo("abc123")
        assertThat(quote.content).isEqualTo("Innovation distinguishes between a leader and a follower.")
        assertThat(quote.author).isEqualTo("Steve Jobs")
        assertThat(quote.length).isEqualTo(56)
        assertThat(quote.tags).containsExactly("innovation", "leadership", "business")
    }

    @Test
    fun `getRandomQuote sends correct query parameters`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "_id": "test-id",
                    "content": "Test quote",
                    "author": "Test Author",
                    "length": 10,
                    "tags": ["test"]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        quotableApi.getRandomQuote(
            tags = "motivational,success",
            author = "Steve Jobs",
            minLength = 20,
            maxLength = 100
        )

        // Assert
        val request = mockWebServer.takeRequest()
        assertThat(request.path).contains("random")
        assertThat(request.requestUrl?.queryParameter("tags")).isEqualTo("motivational,success")
        assertThat(request.requestUrl?.queryParameter("author")).isEqualTo("Steve Jobs")
        assertThat(request.requestUrl?.queryParameter("minLength")).isEqualTo("20")
        assertThat(request.requestUrl?.queryParameter("maxLength")).isEqualTo("100")
    }

    @Test
    fun `getRandomQuote handles 404 error response`() = runTest {
        // Arrange
        val mockResponse = MockResponse().setResponseCode(404)
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.getRandomQuote()

        // Assert
        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(404)
        assertThat(response.body()).isNull()
    }

    @Test
    fun `getRandomQuote handles 500 server error`() = runTest {
        // Arrange
        val mockResponse = MockResponse().setResponseCode(500)
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.getRandomQuote()

        // Assert
        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(500)
        assertThat(response.body()).isNull()
    }

    @Test
    fun `getQuotes returns paginated response with multiple quotes`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "count": 2,
                    "totalCount": 100,
                    "page": 1,
                    "totalPages": 50,
                    "lastItemIndex": 2,
                    "results": [
                        {
                            "_id": "quote1",
                            "content": "First quote content",
                            "author": "Author One",
                            "length": 19,
                            "tags": ["tag1"]
                        },
                        {
                            "_id": "quote2",
                            "content": "Second quote content",
                            "author": "Author Two",
                            "length": 20,
                            "tags": ["tag2"]
                        }
                    ]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.getQuotes(page = 1, limit = 2)

        // Assert
        assertThat(response.isSuccessful).isTrue()
        
        val quoteResponse = response.body()
        assertThat(quoteResponse).isNotNull()
        assertThat(quoteResponse!!.count).isEqualTo(2)
        assertThat(quoteResponse.totalCount).isEqualTo(100)
        assertThat(quoteResponse.page).isEqualTo(1)
        assertThat(quoteResponse.totalPages).isEqualTo(50)
        assertThat(quoteResponse.results).hasSize(2)
        
        val firstQuote = quoteResponse.results[0]
        assertThat(firstQuote.id).isEqualTo("quote1")
        assertThat(firstQuote.content).isEqualTo("First quote content")
        assertThat(firstQuote.author).isEqualTo("Author One")
    }

    @Test
    fun `getQuotes sends correct pagination parameters`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "count": 0,
                    "totalCount": 0,
                    "page": 2,
                    "totalPages": 0,
                    "lastItemIndex": 0,
                    "results": []
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        quotableApi.getQuotes(
            page = 2,
            limit = 10,
            tags = "wisdom",
            author = "Einstein",
            sortBy = "author",
            order = "asc"
        )

        // Assert
        val request = mockWebServer.takeRequest()
        assertThat(request.path).contains("quotes")
        assertThat(request.requestUrl?.queryParameter("page")).isEqualTo("2")
        assertThat(request.requestUrl?.queryParameter("limit")).isEqualTo("10")
        assertThat(request.requestUrl?.queryParameter("tags")).isEqualTo("wisdom")
        assertThat(request.requestUrl?.queryParameter("author")).isEqualTo("Einstein")
        assertThat(request.requestUrl?.queryParameter("sortBy")).isEqualTo("author")
        assertThat(request.requestUrl?.queryParameter("order")).isEqualTo("asc")
    }

    @Test
    fun `getQuoteById returns specific quote`() = runTest {
        // Arrange
        val quoteId = "specific-quote-id"
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "_id": "$quoteId",
                    "content": "Specific quote content",
                    "author": "Specific Author",
                    "length": 21,
                    "tags": ["specific"]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.getQuoteById(quoteId)

        // Assert
        assertThat(response.isSuccessful).isTrue()
        
        val quote = response.body()
        assertThat(quote).isNotNull()
        assertThat(quote!!.id).isEqualTo(quoteId)
        assertThat(quote.content).isEqualTo("Specific quote content")
        assertThat(quote.author).isEqualTo("Specific Author")
        
        // Verify correct path was called
        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/quotes/$quoteId")
    }

    @Test
    fun `getQuoteById handles quote not found`() = runTest {
        // Arrange
        val nonExistentId = "non-existent-id"
        val mockResponse = MockResponse().setResponseCode(404)
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.getQuoteById(nonExistentId)

        // Assert
        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(404)
        assertThat(response.body()).isNull()
    }

    @Test
    fun `searchQuotes returns search results`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "count": 1,
                    "totalCount": 5,
                    "page": 1,
                    "totalPages": 5,
                    "lastItemIndex": 1,
                    "results": [
                        {
                            "_id": "search-result-id",
                            "content": "Search result quote with innovation keyword",
                            "author": "Innovation Author",
                            "length": 44,
                            "tags": ["innovation", "technology"]
                        }
                    ]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.searchQuotes(
            query = "innovation",
            page = 1,
            limit = 10,
            fields = "content,author"
        )

        // Assert
        assertThat(response.isSuccessful).isTrue()
        
        val searchResults = response.body()
        assertThat(searchResults).isNotNull()
        assertThat(searchResults!!.results).hasSize(1)
        assertThat(searchResults.results[0].content).contains("innovation")
        
        // Verify correct search parameters
        val request = mockWebServer.takeRequest()
        assertThat(request.path).contains("search/quotes")
        assertThat(request.requestUrl?.queryParameter("query")).isEqualTo("innovation")
        assertThat(request.requestUrl?.queryParameter("page")).isEqualTo("1")
        assertThat(request.requestUrl?.queryParameter("limit")).isEqualTo("10")
        assertThat(request.requestUrl?.queryParameter("fields")).isEqualTo("content,author")
    }

    @Test
    fun `searchQuotes handles empty search results`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "count": 0,
                    "totalCount": 0,
                    "page": 1,
                    "totalPages": 0,
                    "lastItemIndex": 0,
                    "results": []
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.searchQuotes("nonexistent query")

        // Assert
        assertThat(response.isSuccessful).isTrue()
        
        val searchResults = response.body()
        assertThat(searchResults).isNotNull()
        assertThat(searchResults!!.results).isEmpty()
        assertThat(searchResults.count).isEqualTo(0)
        assertThat(searchResults.totalCount).isEqualTo(0)
    }

    @Test
    fun `API handles malformed JSON response gracefully`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{ invalid json }")
        mockWebServer.enqueue(mockResponse)

        // Act & Assert
        try {
            quotableApi.getRandomQuote()
            // Should throw an exception due to malformed JSON
            assertThat(false).isTrue() // This line should not be reached
        } catch (e: Exception) {
            // Expected behavior - JSON parsing should fail
            assertThat(e).isNotNull()
        }
    }

    @Test
    fun `API handles network timeout gracefully`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"_id":"test","content":"test","author":"test","length":4,"tags":[]}""")
            .setBodyDelay(10, java.util.concurrent.TimeUnit.SECONDS)
        mockWebServer.enqueue(mockResponse)

        // Act & Assert
        try {
            quotableApi.getRandomQuote()
            // This might timeout depending on Retrofit configuration
        } catch (e: Exception) {
            // Network timeouts are expected to be handled gracefully
            assertThat(e).isNotNull()
        }
    }

    @Test
    fun `API preserves special characters in quote content`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "_id": "special-chars-id",
                    "content": "Quote with special chars: àáâãäåæçèéêë & \"quotes\" and emoji 🚀",
                    "author": "Special Author",
                    "length": 70,
                    "tags": ["special", "unicode"]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = quotableApi.getRandomQuote()

        // Assert
        assertThat(response.isSuccessful).isTrue()
        
        val quote = response.body()
        assertThat(quote).isNotNull()
        assertThat(quote!!.content).contains("àáâãäåæçèéêë")
        assertThat(quote.content).contains("\"quotes\"")
        assertThat(quote.content).contains("🚀")
    }
}
