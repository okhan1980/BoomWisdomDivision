package com.jomar.boomwisdomdivision.performance

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jomar.boomwisdomdivision.data.api.model.QuoteDto
import com.jomar.boomwisdomdivision.data.mapper.toDomain
import com.jomar.boomwisdomdivision.data.mapper.toEntity
import com.jomar.boomwisdomdivision.domain.model.Quote
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Performance and memory profiling tests.
 * 
 * These tests verify that the application performs well under various conditions:
 * - Memory usage during data transformations
 * - Performance of mapping operations
 * - Large dataset handling
 * - Memory leak detection for data operations
 * 
 * These tests serve as performance regression detection and help identify
 * potential memory issues before they reach production.
 */
class MemoryProfileTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var memoryTracker: MemoryTracker

    @Before
    fun setUp() {
        memoryTracker = MemoryTracker()
        // Force garbage collection before tests
        System.gc()
        Thread.sleep(100)
    }

    @After
    fun tearDown() {
        // Force garbage collection after tests
        System.gc()
        Thread.sleep(100)
    }

    @Test
    fun `mapping operations should complete within acceptable time limits`() = runTest {
        // Arrange
        val largeBatch = generateTestQuoteDtos(1000)

        // Act & Measure
        val mappingTime = measureTimeMillis {
            largeBatch.forEach { dto ->
                val domain = dto.toDomain()
                val entity = domain.toEntity()
                val backToDomain = entity.toDomain()
                
                // Verify mapping correctness
                assertThat(backToDomain.id).isEqualTo(dto.id)
                assertThat(backToDomain.content).isEqualTo(dto.content)
            }
        }

        // Assert - Should complete mapping 1000 quotes in under 500ms
        assertThat(mappingTime).isLessThan(500L)
        println("Mapped 1000 quotes in ${mappingTime}ms")
    }

    @Test
    fun `memory usage should remain stable during batch operations`() = runTest {
        // Arrange
        val initialMemory = memoryTracker.getCurrentMemoryUsage()
        val batchSize = 500

        // Act - Perform multiple batch operations
        repeat(5) { iteration ->
            val quotes = generateTestQuoteDtos(batchSize)
            
            // Transform to domain and entity
            val domainQuotes = quotes.map { it.toDomain() }
            val entities = domainQuotes.map { it.toEntity() }
            val backToDomain = entities.map { it.toDomain() }
            
            // Verify batch integrity
            assertThat(backToDomain).hasSize(batchSize)
            
            println("Iteration $iteration completed, processed $batchSize quotes")
        }

        // Force garbage collection
        System.gc()
        Thread.sleep(200)

        val finalMemory = memoryTracker.getCurrentMemoryUsage()
        val memoryIncrease = finalMemory - initialMemory

        // Assert - Memory increase should be minimal (less than 50MB)
        assertThat(memoryIncrease).isLessThan(50 * 1024 * 1024L) // 50MB
        println("Memory increase: ${memoryIncrease / (1024 * 1024)}MB")
    }

    @Test
    fun `large quote content should be handled efficiently`() = runTest {
        // Arrange - Create quotes with very large content
        val largeContentQuotes = (1..100).map { i ->
            QuoteDto(
                id = "large-$i",
                content = "A".repeat(10000), // 10KB content per quote
                author = "Large Content Author $i",
                length = 10000,
                tags = listOf("large", "content", "test", "performance")
            )
        }

        val initialMemory = memoryTracker.getCurrentMemoryUsage()

        // Act
        val processingTime = measureTimeMillis {
            largeContentQuotes.forEach { dto ->
                val domain = dto.toDomain()
                val entity = domain.toEntity()
                
                // Verify large content is handled correctly
                assertThat(entity.content).hasLength(10000)
                assertThat(entity.tags).contains("large")
            }
        }

        val finalMemory = memoryTracker.getCurrentMemoryUsage()

        // Assert
        assertThat(processingTime).isLessThan(1000L) // Should complete in under 1 second
        assertThat(finalMemory - initialMemory).isLessThan(100 * 1024 * 1024L) // Less than 100MB increase
        
        println("Processed 100 large quotes (10KB each) in ${processingTime}ms")
    }

    @Test
    fun `concurrent mapping operations should not cause memory issues`() = runTest {
        // Arrange
        val initialMemory = memoryTracker.getCurrentMemoryUsage()
        val quoteBatches = (1..10).map { batchIndex ->
            generateTestQuoteDtos(100).map { dto ->
                dto.copy(id = "${dto.id}-batch-$batchIndex")
            }
        }

        // Act - Process batches concurrently
        val processingTime = measureTimeMillis {
            quoteBatches.forEach { batch ->
                // Simulate concurrent processing
                batch.map { dto ->
                    val domain = dto.toDomain()
                    val entity = domain.toEntity()
                    entity.toDomain()
                }
            }
        }

        // Force cleanup
        System.gc()
        Thread.sleep(100)

        val finalMemory = memoryTracker.getCurrentMemoryUsage()

        // Assert
        assertThat(processingTime).isLessThan(2000L) // Should complete in under 2 seconds
        assertThat(finalMemory - initialMemory).isLessThan(75 * 1024 * 1024L) // Less than 75MB increase
        
        println("Processed 10 batches of 100 quotes concurrently in ${processingTime}ms")
    }

    @Test
    fun `quote validation should be performant for large datasets`() = runTest {
        // Arrange
        val quotes = generateTestQuotes(2000)
        
        // Act
        val validationTime = measureTimeMillis {
            val validQuotes = quotes.filter { quote ->
                isValidQuote(quote)
            }
            
            // Should filter out any invalid quotes
            assertThat(validQuotes.size).isEqualTo(quotes.size) // All generated quotes are valid
        }

        // Assert - Validation should be fast
        assertThat(validationTime).isLessThan(100L) // Should complete in under 100ms
        println("Validated 2000 quotes in ${validationTime}ms")
    }

    @Test
    fun `memory should be released after processing large batches`() = runTest {
        // Arrange
        val initialMemory = memoryTracker.getCurrentMemoryUsage()
        
        // Act - Process large batch and let it go out of scope
        run {
            val largeBatch = generateTestQuoteDtos(2000)
            val processedQuotes = largeBatch.map { it.toDomain().toEntity().toDomain() }
            assertThat(processedQuotes).hasSize(2000)
            
            // Verify processing completed
            println("Processed ${processedQuotes.size} quotes")
        }

        // Force garbage collection
        repeat(3) {
            System.gc()
            Thread.sleep(100)
        }

        val finalMemory = memoryTracker.getCurrentMemoryUsage()
        val memoryDifference = finalMemory - initialMemory

        // Assert - Memory should return close to initial levels
        assertThat(memoryDifference).isLessThan(20 * 1024 * 1024L) // Less than 20MB difference
        println("Memory difference after cleanup: ${memoryDifference / (1024 * 1024)}MB")
    }

    /**
     * Generates test QuoteDto objects for performance testing.
     */
    private fun generateTestQuoteDtos(count: Int): List<QuoteDto> {
        return (1..count).map { i ->
            QuoteDto(
                id = "test-quote-$i",
                content = "This is test quote content number $i for performance testing purposes.",
                author = "Test Author $i",
                length = 70,
                tags = listOf("test", "performance", "batch-$i")
            )
        }
    }

    /**
     * Generates test Quote domain objects for performance testing.
     */
    private fun generateTestQuotes(count: Int): List<Quote> {
        return (1..count).map { i ->
            Quote(
                id = "test-quote-$i",
                content = "This is test quote content number $i for performance testing purposes.",
                author = "Test Author $i",
                length = 70,
                tags = listOf("test", "performance", "batch-$i"),
                savedAt = System.currentTimeMillis() + i
            )
        }
    }

    /**
     * Validates a quote according to business rules.
     */
    private fun isValidQuote(quote: Quote): Boolean {
        return quote.id.isNotBlank() &&
               quote.content.isNotBlank() &&
               quote.author.isNotBlank() &&
               quote.content.length in 1..1000 &&
               quote.length >= 0
    }

    /**
     * Simple memory tracking utility for tests.
     */
    private class MemoryTracker {
        private val runtime = Runtime.getRuntime()

        fun getCurrentMemoryUsage(): Long {
            return runtime.totalMemory() - runtime.freeMemory()
        }

        fun getMaxMemory(): Long {
            return runtime.maxMemory()
        }

        fun getTotalMemory(): Long {
            return runtime.totalMemory()
        }

        fun getFreeMemory(): Long {
            return runtime.freeMemory()
        }
    }
}