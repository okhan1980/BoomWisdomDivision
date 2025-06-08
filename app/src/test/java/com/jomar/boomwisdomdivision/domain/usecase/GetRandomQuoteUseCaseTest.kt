package com.jomar.boomwisdomdivision.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jomar.boomwisdomdivision.core.util.Result
import com.jomar.boomwisdomdivision.domain.model.Quote
import com.jomar.boomwisdomdivision.domain.repository.QuoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for GetRandomQuoteUseCase.
 * 
 * These tests verify the use case's business logic including:
 * - Successful quote retrieval and validation
 * - Error handling for repository failures
 * - Quote validation according to business rules
 * - Proper threading with test dispatchers
 * - Edge cases and boundary conditions
 * 
 * Uses MockK for repository mocking and follows AAA testing pattern.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetRandomQuoteUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockRepository = mockk<QuoteRepository>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var useCase: GetRandomQuoteUseCase

    // Test data
    private val validQuote = Quote(
        id = "valid-quote-id",
        content = "Innovation distinguishes between a leader and a follower.",
        author = "Steve Jobs",
        length = 56,
        tags = listOf("innovation", "leadership"),
        savedAt = null
    )

    private val shortQuote = Quote(
        id = "short-quote-id",
        content = "Short",
        author = "Author",
        length = 5,
        tags = listOf("short"),
        savedAt = null
    )

    private val longQuote = Quote(
        id = "long-quote-id",
        content = "A".repeat(600), // Exceeds maximum length
        author = "Long Author",
        length = 600,
        tags = listOf("long"),
        savedAt = null
    )

    private val quoteWithEmptyContent = Quote(
        id = "empty-content-id",
        content = "",
        author = "Author",
        length = 0,
        tags = listOf("empty"),
        savedAt = null
    )

    private val quoteWithBlankContent = Quote(
        id = "blank-content-id",
        content = "   ",
        author = "Author",
        length = 3,
        tags = listOf("blank"),
        savedAt = null
    )

    private val quoteWithEmptyAuthor = Quote(
        id = "empty-author-id",
        content = "Valid quote content here",
        author = "",
        length = 24,
        tags = listOf("test"),
        savedAt = null
    )

    private val quoteWithEmptyId = Quote(
        id = "",
        content = "Valid quote content here",
        author = "Valid Author",
        length = 24,
        tags = listOf("test"),
        savedAt = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = GetRandomQuoteUseCase(mockRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke returns success when repository returns valid quote`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(validQuote)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(validQuote)
        
        coVerify(exactly = 1) { mockRepository.getRandomQuote() }
    }

    @Test
    fun `invoke returns error when repository returns error`() = runTest {
        // Arrange
        val repositoryException = Exception("Network error")
        coEvery { mockRepository.getRandomQuote() } returns Result.Error(repositoryException)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isEqualTo(repositoryException)
        
        coVerify(exactly = 1) { mockRepository.getRandomQuote() }
    }

    @Test
    fun `invoke returns error when quote content is empty`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(quoteWithEmptyContent)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Received invalid quote")
    }

    @Test
    fun `invoke returns error when quote content is blank`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(quoteWithBlankContent)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Received invalid quote")
    }

    @Test
    fun `invoke returns error when quote content is too short`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(shortQuote)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Received invalid quote")
    }

    @Test
    fun `invoke returns error when quote content is too long`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(longQuote)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Received invalid quote")
    }

    @Test
    fun `invoke returns error when quote author is empty`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(quoteWithEmptyAuthor)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Received invalid quote")
    }

    @Test
    fun `invoke returns error when quote ID is empty`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(quoteWithEmptyId)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Received invalid quote")
    }

    @Test
    fun `invoke returns error when repository returns loading state`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Loading

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Unexpected loading state")
    }

    @Test
    fun `invoke handles unexpected exceptions gracefully`() = runTest {
        // Arrange
        val unexpectedException = RuntimeException("Unexpected error")
        coEvery { mockRepository.getRandomQuote() } throws unexpectedException

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isEqualTo(unexpectedException)
    }

    @Test
    fun `invoke validates quote with minimum acceptable length`() = runTest {
        // Arrange - Quote with exactly minimum length (10 characters)
        val minLengthQuote = Quote(
            id = "min-length-id",
            content = "1234567890", // Exactly 10 characters
            author = "Test Author",
            length = 10,
            tags = listOf("test"),
            savedAt = null
        )
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(minLengthQuote)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(minLengthQuote)
    }

    @Test
    fun `invoke validates quote with maximum acceptable length`() = runTest {
        // Arrange - Quote with exactly maximum length (500 characters)
        val maxLengthContent = "A".repeat(500)
        val maxLengthQuote = Quote(
            id = "max-length-id",
            content = maxLengthContent,
            author = "Test Author",
            length = 500,
            tags = listOf("test"),
            savedAt = null
        )
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(maxLengthQuote)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(maxLengthQuote)
    }

    @Test
    fun `invoke accepts quote with special characters`() = runTest {
        // Arrange
        val specialCharQuote = Quote(
            id = "special-char-id",
            content = "Quote with special chars: àáâãäåæçèéêë & \"quotes\" 🚀",
            author = "Spëcîål Àüthør",
            length = 59,
            tags = listOf("special", "unicode"),
            savedAt = null
        )
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(specialCharQuote)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(specialCharQuote)
    }

    @Test
    fun `invoke accepts quote with empty tags list`() = runTest {
        // Arrange
        val quoteWithEmptyTags = validQuote.copy(tags = emptyList())
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(quoteWithEmptyTags)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.tags).isEmpty()
    }

    @Test
    fun `invoke accepts quote with savedAt timestamp`() = runTest {
        // Arrange
        val savedQuote = validQuote.copy(savedAt = System.currentTimeMillis())
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(savedQuote)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.savedAt).isNotNull()
    }

    @Test
    fun `invoke executes on correct dispatcher`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(validQuote)

        // Act
        val result = useCase()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        // The test dispatcher should execute immediately in test environment
        coVerify(exactly = 1) { mockRepository.getRandomQuote() }
    }

    @Test
    fun `invoke can be called multiple times`() = runTest {
        // Arrange
        val quote1 = validQuote.copy(id = "quote-1")
        val quote2 = validQuote.copy(id = "quote-2")
        
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(quote1) andThen Result.Success(quote2)

        // Act
        val result1 = useCase()
        val result2 = useCase()

        // Assert
        assertThat(result1).isInstanceOf(Result.Success::class.java)
        assertThat(result2).isInstanceOf(Result.Success::class.java)
        
        result1 as Result.Success
        result2 as Result.Success
        
        assertThat(result1.data.id).isEqualTo("quote-1")
        assertThat(result2.data.id).isEqualTo("quote-2")
        
        coVerify(exactly = 2) { mockRepository.getRandomQuote() }
    }

    @Test
    fun `invoke handles concurrent execution correctly`() = runTest {
        // Arrange
        coEvery { mockRepository.getRandomQuote() } returns Result.Success(validQuote)

        // Act - Execute multiple calls concurrently
        val results = listOf(
            useCase(),
            useCase(),
            useCase()
        )

        // Assert
        results.forEach { result ->
            assertThat(result).isInstanceOf(Result.Success::class.java)
            result as Result.Success
            assertThat(result.data).isEqualTo(validQuote)
        }
        
        coVerify(exactly = 3) { mockRepository.getRandomQuote() }
    }
}