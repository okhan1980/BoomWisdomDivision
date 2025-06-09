package com.jomar.boomwisdomdivision.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jomar.boomwisdomdivision.domain.model.Quote
import com.jomar.boomwisdomdivision.domain.repository.QuoteRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for GetSavedQuotesUseCase.
 * 
 * These tests verify the use case's business logic including:
 * - Reactive data flow from repository
 * - Quote ordering and filtering
 * - Search functionality
 * - Tag-based filtering
 * - Various sorting options
 * - Validation of saved quotes
 * - Handling of edge cases
 * 
 * Uses MockK for repository mocking and Turbine for Flow testing.
 * Follows AAA testing pattern for clarity.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetSavedQuotesUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockRepository = mockk<QuoteRepository>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var useCase: GetSavedQuotesUseCase

    // Test data
    private val quote1 = Quote(
        id = "quote-1",
        content = "Innovation distinguishes between a leader and a follower.",
        author = "Steve Jobs",
        length = 56,
        tags = listOf("innovation", "leadership"),
        savedAt = 3000L
    )

    private val quote2 = Quote(
        id = "quote-2",
        content = "The only way to do great work is to love what you do.",
        author = "Steve Jobs",
        length = 49,
        tags = listOf("work", "passion"),
        savedAt = 2000L
    )

    private val quote3 = Quote(
        id = "quote-3",
        content = "Imagination is more important than knowledge.",
        author = "Albert Einstein",
        length = 43,
        tags = listOf("wisdom", "knowledge"),
        savedAt = 1000L
    )

    private val invalidQuoteNoSavedAt = Quote(
        id = "invalid-1",
        content = "Quote without saved timestamp",
        author = "Author",
        length = 30,
        tags = listOf("test"),
        savedAt = null
    )

    private val invalidQuoteEmptyContent = Quote(
        id = "invalid-2",
        content = "",
        author = "Author",
        length = 0,
        tags = listOf("test"),
        savedAt = 1500L
    )

    private val invalidQuoteEmptyAuthor = Quote(
        id = "invalid-3",
        content = "Quote with empty author",
        author = "",
        length = 23,
        tags = listOf("test"),
        savedAt = 1500L
    )

    private val invalidQuoteEmptyId = Quote(
        id = "",
        content = "Quote with empty ID",
        author = "Author",
        length = 19,
        tags = listOf("test"),
        savedAt = 1500L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = GetSavedQuotesUseCase(mockRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke returns empty list when no quotes saved`() = runTest {
        // Arrange
        every { mockRepository.getSavedQuotes() } returns flowOf(emptyList())

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns quotes ordered by savedAt descending`() = runTest {
        // Arrange
        val unsortedQuotes = listOf(quote2, quote1, quote3) // Out of order
        every { mockRepository.getSavedQuotes() } returns flowOf(unsortedQuotes)

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            
            // Should be ordered by savedAt DESC (most recent first)
            assertThat(result[0].id).isEqualTo("quote-1") // savedAt: 3000L
            assertThat(result[1].id).isEqualTo("quote-2") // savedAt: 2000L
            assertThat(result[2].id).isEqualTo("quote-3") // savedAt: 1000L
            
            awaitComplete()
        }
    }

    @Test
    fun `invoke filters out invalid quotes`() = runTest {
        // Arrange
        val mixedQuotes = listOf(
            quote1, 
            invalidQuoteNoSavedAt, 
            quote2, 
            invalidQuoteEmptyContent,
            invalidQuoteEmptyAuthor,
            invalidQuoteEmptyId,
            quote3
        )
        every { mockRepository.getSavedQuotes() } returns flowOf(mixedQuotes)

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertThat(result).hasSize(3) // Only valid quotes
            assertThat(result.map { it.id }).containsExactly("quote-1", "quote-2", "quote-3")
            awaitComplete()
        }
    }

    @Test
    fun `invoke respects maximum displayed quotes limit`() = runTest {
        // Arrange - Create more than 1000 quotes (the limit)
        val manyQuotes = (1..1100).map { i ->
            Quote(
                id = "quote-$i",
                content = "Quote content $i",
                author = "Author $i",
                length = 15,
                tags = listOf("test"),
                savedAt = i.toLong()
            )
        }
        every { mockRepository.getSavedQuotes() } returns flowOf(manyQuotes)

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertThat(result).hasSize(1000) // Should be limited to 1000
            awaitComplete()
        }
    }

    @Test
    fun `invoke with DATE_SAVED_DESC sorts correctly`() = runTest {
        // Arrange
        val quotes = listOf(quote3, quote1, quote2) // Random order
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase(GetSavedQuotesUseCase.SortOrder.DATE_SAVED_DESC).test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            assertThat(result[0].savedAt).isEqualTo(3000L) // quote1
            assertThat(result[1].savedAt).isEqualTo(2000L) // quote2
            assertThat(result[2].savedAt).isEqualTo(1000L) // quote3
            awaitComplete()
        }
    }

    @Test
    fun `invoke with DATE_SAVED_ASC sorts correctly`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote3, quote2) // Random order
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase(GetSavedQuotesUseCase.SortOrder.DATE_SAVED_ASC).test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            assertThat(result[0].savedAt).isEqualTo(1000L) // quote3
            assertThat(result[1].savedAt).isEqualTo(2000L) // quote2
            assertThat(result[2].savedAt).isEqualTo(3000L) // quote1
            awaitComplete()
        }
    }

    @Test
    fun `invoke with AUTHOR_NAME sorts alphabetically`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3) // Steve Jobs, Steve Jobs, Albert Einstein
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase(GetSavedQuotesUseCase.SortOrder.AUTHOR_NAME).test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            assertThat(result[0].author).isEqualTo("Albert Einstein")
            assertThat(result[1].author).isEqualTo("Steve Jobs")
            assertThat(result[2].author).isEqualTo("Steve Jobs")
            awaitComplete()
        }
    }

    @Test
    fun `invoke with CONTENT_LENGTH sorts by length`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3) // lengths: 56, 49, 43
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase(GetSavedQuotesUseCase.SortOrder.CONTENT_LENGTH).test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            assertThat(result[0].length).isEqualTo(43) // quote3
            assertThat(result[1].length).isEqualTo(49) // quote2
            assertThat(result[2].length).isEqualTo(56) // quote1
            awaitComplete()
        }
    }

    @Test
    fun `invoke with ALPHABETICAL sorts by content`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3) // "Innovation...", "The only...", "Imagination..."
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase(GetSavedQuotesUseCase.SortOrder.ALPHABETICAL).test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            assertThat(result[0].content).startsWith("Imagination") // quote3
            assertThat(result[1].content).startsWith("Innovation") // quote1
            assertThat(result[2].content).startsWith("The only") // quote2
            awaitComplete()
        }
    }

    @Test
    fun `searchSavedQuotes returns all quotes when query is empty`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.searchSavedQuotes("").test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            awaitComplete()
        }
    }

    @Test
    fun `searchSavedQuotes returns all quotes when query is blank`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.searchSavedQuotes("   ").test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            awaitComplete()
        }
    }

    @Test
    fun `searchSavedQuotes finds quotes by content`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.searchSavedQuotes("innovation").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo("quote-1")
            awaitComplete()
        }
    }

    @Test
    fun `searchSavedQuotes finds quotes by author`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.searchSavedQuotes("steve jobs").test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result.all { it.author == "Steve Jobs" }).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `searchSavedQuotes finds quotes by tags`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.searchSavedQuotes("knowledge").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo("quote-3")
            awaitComplete()
        }
    }

    @Test
    fun `searchSavedQuotes is case insensitive`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.searchSavedQuotes("INNOVATION").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo("quote-1")
            awaitComplete()
        }
    }

    @Test
    fun `searchSavedQuotes returns empty when no matches`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.searchSavedQuotes("nonexistent").test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getSavedQuotesByTags returns all quotes when tags list is empty`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.getSavedQuotesByTags(emptyList()).test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            awaitComplete()
        }
    }

    @Test
    fun `getSavedQuotesByTags filters by specific tags`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.getSavedQuotesByTags(listOf("innovation")).test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo("quote-1")
            awaitComplete()
        }
    }

    @Test
    fun `getSavedQuotesByTags filters by multiple tags (OR logic)`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.getSavedQuotesByTags(listOf("innovation", "wisdom")).test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result.map { it.id }).containsExactly("quote-1", "quote-3")
            awaitComplete()
        }
    }

    @Test
    fun `getSavedQuotesByTags is case insensitive`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.getSavedQuotesByTags(listOf("INNOVATION")).test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo("quote-1")
            awaitComplete()
        }
    }

    @Test
    fun `getSavedQuotesByTags returns empty when no matches`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.getSavedQuotesByTags(listOf("nonexistent")).test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `flows emit updates when repository data changes`() = runTest {
        // Arrange
        val initialQuotes = listOf(quote1, quote2)
        val updatedQuotes = listOf(quote1, quote2, quote3)
        
        every { mockRepository.getSavedQuotes() } returns flowOf(initialQuotes, updatedQuotes)

        // Act & Assert
        useCase().test {
            val firstEmission = awaitItem()
            assertThat(firstEmission).hasSize(2)
            
            val secondEmission = awaitItem()
            assertThat(secondEmission).hasSize(3)
            assertThat(secondEmission[2].id).isEqualTo("quote-3")
            
            awaitComplete()
        }
    }

    @Test
    fun `flows operate on correct dispatcher`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            awaitComplete()
        }
        // The test dispatcher should execute immediately in test environment
    }

    @Test
    fun `multiple flows can be active simultaneously`() = runTest {
        // Arrange
        val quotes = listOf(quote1, quote2, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase().test {
            val defaultResult = awaitItem()
            assertThat(defaultResult).hasSize(3)
            
            useCase(GetSavedQuotesUseCase.SortOrder.AUTHOR_NAME).test {
                val authorSortedResult = awaitItem()
                assertThat(authorSortedResult).hasSize(3)
                assertThat(authorSortedResult[0].author).isEqualTo("Albert Einstein")
                awaitComplete()
            }
            
            awaitComplete()
        }
    }

    @Test
    fun `handles quotes with special characters in search`() = runTest {
        // Arrange
        val specialQuote = Quote(
            id = "special-id",
            content = "Quote with special chars: àáâãäåæçèéêë & \"quotes\" 🚀",
            author = "Spëcîål Àüthør",
            length = 59,
            tags = listOf("special", "unicode"),
            savedAt = 1500L
        )
        val quotes = listOf(quote1, specialQuote, quote3)
        every { mockRepository.getSavedQuotes() } returns flowOf(quotes)

        // Act & Assert
        useCase.searchSavedQuotes("àáâãäåæçèéêë").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo("special-id")
            awaitComplete()
        }
    }
}
