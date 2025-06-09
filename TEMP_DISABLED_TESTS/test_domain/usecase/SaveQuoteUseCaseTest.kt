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
 * Unit tests for SaveQuoteUseCase.
 * 
 * These tests verify the use case's business logic including:
 * - Successful quote saving with timestamp assignment
 * - Quote validation before saving
 * - Handling of already saved quotes (idempotent operations)
 * - Error handling for repository failures
 * - Edge cases and boundary conditions
 * 
 * Uses MockK for repository mocking and follows AAA testing pattern.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SaveQuoteUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockRepository = mockk<QuoteRepository>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var useCase: SaveQuoteUseCase

    // Test data
    private val validQuote = Quote(
        id = "valid-quote-id",
        content = "Innovation distinguishes between a leader and a follower.",
        author = "Steve Jobs",
        length = 56,
        tags = listOf("innovation", "leadership"),
        savedAt = null
    )

    private val alreadySavedQuote = Quote(
        id = "saved-quote-id",
        content = "Already saved quote content.",
        author = "Saved Author",
        length = 30,
        tags = listOf("saved"),
        savedAt = 1640995200000L
    )

    private val invalidQuoteEmptyContent = Quote(
        id = "invalid-id",
        content = "",
        author = "Author",
        length = 0,
        tags = listOf("test"),
        savedAt = null
    )

    private val invalidQuoteBlankContent = Quote(
        id = "invalid-id",
        content = "   ",
        author = "Author",
        length = 3,
        tags = listOf("test"),
        savedAt = null
    )

    private val invalidQuoteEmptyAuthor = Quote(
        id = "invalid-id",
        content = "Valid content here",
        author = "",
        length = 18,
        tags = listOf("test"),
        savedAt = null
    )

    private val invalidQuoteEmptyId = Quote(
        id = "",
        content = "Valid content here",
        author = "Valid Author",
        length = 18,
        tags = listOf("test"),
        savedAt = null
    )

    private val invalidQuoteTooLong = Quote(
        id = "too-long-id",
        content = "A".repeat(1001), // Exceeds maximum length
        author = "Author",
        length = 1001,
        tags = listOf("test"),
        savedAt = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = SaveQuoteUseCase(mockRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke saves valid quote successfully`() = runTest {
        // Arrange
        val expectedSavedQuote = validQuote.copy(savedAt = 1640995200000L)
        
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } returns Result.Success(expectedSavedQuote)

        // Act
        val result = useCase(validQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.id).isEqualTo(validQuote.id)
        assertThat(result.data.content).isEqualTo(validQuote.content)
        assertThat(result.data.savedAt).isNotNull()
        
        coVerify(exactly = 1) { mockRepository.isQuoteSaved(validQuote.id) }
        coVerify(exactly = 1) { mockRepository.saveQuote(any()) }
    }

    @Test
    fun `invoke adds timestamp when quote has no savedAt`() = runTest {
        // Arrange
        val beforeTime = System.currentTimeMillis()
        
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } answers {
            val quoteToSave = firstArg<Quote>()
            Result.Success(quoteToSave)
        }

        // Act
        val result = useCase(validQuote)
        val afterTime = System.currentTimeMillis()

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.savedAt).isNotNull()
        assertThat(result.data.savedAt!!).isAtLeast(beforeTime)
        assertThat(result.data.savedAt!!).isAtMost(afterTime)
    }

    @Test
    fun `invoke preserves existing timestamp when quote already has savedAt`() = runTest {
        // Arrange
        val existingTimestamp = 1234567890L
        val quoteWithTimestamp = validQuote.copy(savedAt = existingTimestamp)
        
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } answers {
            val quoteToSave = firstArg<Quote>()
            Result.Success(quoteToSave)
        }

        // Act
        val result = useCase(quoteWithTimestamp)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.savedAt).isEqualTo(existingTimestamp)
    }

    @Test
    fun `invoke returns existing quote when already saved`() = runTest {
        // Arrange
        coEvery { mockRepository.isQuoteSaved(alreadySavedQuote.id) } returns Result.Success(true)
        coEvery { mockRepository.getQuoteById(alreadySavedQuote.id) } returns Result.Success(alreadySavedQuote)

        // Act
        val result = useCase(alreadySavedQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(alreadySavedQuote)
        
        coVerify(exactly = 1) { mockRepository.isQuoteSaved(alreadySavedQuote.id) }
        coVerify(exactly = 1) { mockRepository.getQuoteById(alreadySavedQuote.id) }
        coVerify(exactly = 0) { mockRepository.saveQuote(any()) }
    }

    @Test
    fun `invoke continues with save when isQuoteSaved check fails`() = runTest {
        // Arrange
        val expectedSavedQuote = validQuote.copy(savedAt = 1640995200000L)
        
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Error(Exception("Check failed"))
        coEvery { mockRepository.saveQuote(any()) } returns Result.Success(expectedSavedQuote)

        // Act
        val result = useCase(validQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(expectedSavedQuote)
        
        coVerify(exactly = 1) { mockRepository.saveQuote(any()) }
    }

    @Test
    fun `invoke continues with save when getQuoteById fails for already saved quote`() = runTest {
        // Arrange
        val expectedSavedQuote = validQuote.copy(savedAt = 1640995200000L)
        
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Success(true)
        coEvery { mockRepository.getQuoteById(validQuote.id) } returns Result.Error(Exception("Get failed"))
        coEvery { mockRepository.saveQuote(any()) } returns Result.Success(expectedSavedQuote)

        // Act
        val result = useCase(validQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(expectedSavedQuote)
    }

    @Test
    fun `invoke returns error for invalid quote with empty content`() = runTest {
        // Act
        val result = useCase(invalidQuoteEmptyContent)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exception.message).contains("Cannot save invalid quote")
        
        coVerify(exactly = 0) { mockRepository.saveQuote(any()) }
    }

    @Test
    fun `invoke returns error for invalid quote with blank content`() = runTest {
        // Act
        val result = useCase(invalidQuoteBlankContent)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exception.message).contains("Cannot save invalid quote")
    }

    @Test
    fun `invoke returns error for invalid quote with empty author`() = runTest {
        // Act
        val result = useCase(invalidQuoteEmptyAuthor)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exception.message).contains("Cannot save invalid quote")
    }

    @Test
    fun `invoke returns error for invalid quote with empty ID`() = runTest {
        // Act
        val result = useCase(invalidQuoteEmptyId)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exception.message).contains("Cannot save invalid quote")
    }

    @Test
    fun `invoke returns error for quote that is too long`() = runTest {
        // Act
        val result = useCase(invalidQuoteTooLong)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exception.message).contains("Cannot save invalid quote")
    }

    @Test
    fun `invoke accepts quote with minimum valid length`() = runTest {
        // Arrange
        val minLengthQuote = Quote(
            id = "min-id",
            content = "A", // Minimum length of 1
            author = "Author",
            length = 1,
            tags = listOf("test"),
            savedAt = null
        )
        val expectedSavedQuote = minLengthQuote.copy(savedAt = 1640995200000L)
        
        coEvery { mockRepository.isQuoteSaved(minLengthQuote.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } returns Result.Success(expectedSavedQuote)

        // Act
        val result = useCase(minLengthQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(expectedSavedQuote)
    }

    @Test
    fun `invoke accepts quote with maximum valid length`() = runTest {
        // Arrange
        val maxLengthContent = "A".repeat(1000) // Maximum length of 1000
        val maxLengthQuote = Quote(
            id = "max-id",
            content = maxLengthContent,
            author = "Author",
            length = 1000,
            tags = listOf("test"),
            savedAt = null
        )
        val expectedSavedQuote = maxLengthQuote.copy(savedAt = 1640995200000L)
        
        coEvery { mockRepository.isQuoteSaved(maxLengthQuote.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } returns Result.Success(expectedSavedQuote)

        // Act
        val result = useCase(maxLengthQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.content).isEqualTo(maxLengthContent)
    }

    @Test
    fun `invoke returns error when repository save fails`() = runTest {
        // Arrange
        val repositoryException = Exception("Save failed")
        
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } returns Result.Error(repositoryException)

        // Act
        val result = useCase(validQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isEqualTo(repositoryException)
    }

    @Test
    fun `invoke returns error when repository returns loading state for isQuoteSaved`() = runTest {
        // Arrange
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Loading

        // Act
        val result = useCase(validQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Unexpected loading state")
    }

    @Test
    fun `invoke returns error when repository returns loading state for getQuoteById`() = runTest {
        // Arrange
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Success(true)
        coEvery { mockRepository.getQuoteById(validQuote.id) } returns Result.Loading

        // Act
        val result = useCase(validQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exception.message).contains("Unexpected loading state")
    }

    @Test
    fun `invoke returns error when repository returns loading state for saveQuote`() = runTest {
        // Arrange
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } returns Result.Loading

        // Act
        val result = useCase(validQuote)

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
        coEvery { mockRepository.isQuoteSaved(validQuote.id) } throws unexpectedException

        // Act
        val result = useCase(validQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.exception).isEqualTo(unexpectedException)
    }

    @Test
    fun `invoke accepts quote with special characters`() = runTest {
        // Arrange
        val specialCharQuote = Quote(
            id = "special-id",
            content = "Quote with special chars: àáâãäåæçèéêë & \"quotes\" 🚀",
            author = "Spëcîål Àüthør",
            length = 59,
            tags = listOf("special", "unicode"),
            savedAt = null
        )
        val expectedSavedQuote = specialCharQuote.copy(savedAt = 1640995200000L)
        
        coEvery { mockRepository.isQuoteSaved(specialCharQuote.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } returns Result.Success(expectedSavedQuote)

        // Act
        val result = useCase(specialCharQuote)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.content).contains("àáâãäåæçèéêë")
        assertThat(result.data.author).contains("Spëcîål")
    }

    @Test
    fun `invoke accepts quote with empty tags`() = runTest {
        // Arrange
        val quoteWithEmptyTags = validQuote.copy(tags = emptyList())
        val expectedSavedQuote = quoteWithEmptyTags.copy(savedAt = 1640995200000L)
        
        coEvery { mockRepository.isQuoteSaved(quoteWithEmptyTags.id) } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(any()) } returns Result.Success(expectedSavedQuote)

        // Act
        val result = useCase(quoteWithEmptyTags)

        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data.tags).isEmpty()
    }

    @Test
    fun `invoke can be called multiple times with different quotes`() = runTest {
        // Arrange
        val quote1 = validQuote.copy(id = "quote-1")
        val quote2 = validQuote.copy(id = "quote-2")
        val savedQuote1 = quote1.copy(savedAt = 1000L)
        val savedQuote2 = quote2.copy(savedAt = 2000L)
        
        coEvery { mockRepository.isQuoteSaved("quote-1") } returns Result.Success(false)
        coEvery { mockRepository.isQuoteSaved("quote-2") } returns Result.Success(false)
        coEvery { mockRepository.saveQuote(quote1.markAsSaved(any())) } returns Result.Success(savedQuote1)
        coEvery { mockRepository.saveQuote(quote2.markAsSaved(any())) } returns Result.Success(savedQuote2)

        // Act
        val result1 = useCase(quote1)
        val result2 = useCase(quote2)

        // Assert
        assertThat(result1).isInstanceOf(Result.Success::class.java)
        assertThat(result2).isInstanceOf(Result.Success::class.java)
        
        result1 as Result.Success
        result2 as Result.Success
        
        assertThat(result1.data.id).isEqualTo("quote-1")
        assertThat(result2.data.id).isEqualTo("quote-2")
        
        coVerify(exactly = 2) { mockRepository.saveQuote(any()) }
    }
}
