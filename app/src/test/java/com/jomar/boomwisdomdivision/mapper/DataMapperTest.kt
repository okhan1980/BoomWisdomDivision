package com.jomar.boomwisdomdivision.mapper

import com.google.common.truth.Truth.assertThat
import com.jomar.boomwisdomdivision.data.api.model.QuoteDto
import com.jomar.boomwisdomdivision.data.db.entity.QuoteEntity
import com.jomar.boomwisdomdivision.data.mapper.toDomain
import com.jomar.boomwisdomdivision.data.mapper.toDto
import com.jomar.boomwisdomdivision.data.mapper.toEntity
import com.jomar.boomwisdomdivision.domain.model.Quote
import org.junit.Test

/**
 * Unit tests for data mapping functions.
 * 
 * These tests verify the correct transformation between different data representations:
 * - API DTOs to Domain models
 * - Database entities to Domain models
 * - Domain models to Database entities
 * - Domain models to DTOs
 * 
 * Tests ensure data integrity, proper null handling, and correct field mapping.
 * Critical for maintaining data consistency across architectural layers.
 */
class DataMapperTest {

    // Test data
    private val testQuoteDto = QuoteDto(
        id = "test-dto-id",
        content = "Innovation distinguishes between a leader and a follower.",
        author = "Steve Jobs",
        length = 56,
        tags = listOf("innovation", "leadership", "business")
    )

    private val testQuoteEntity = QuoteEntity(
        id = "test-entity-id",
        content = "The only way to do great work is to love what you do.",
        author = "Steve Jobs",
        length = 49,
        tags = """["motivational", "work", "passion"]""",
        savedAt = 1640995200000L // 2022-01-01T00:00:00Z
    )

    private val testQuote = Quote(
        id = "test-domain-id",
        content = "Imagination is more important than knowledge.",
        author = "Albert Einstein",
        length = 43,
        tags = listOf("wisdom", "knowledge", "imagination"),
        savedAt = 1640995200000L
    )

    private val testQuoteWithoutSavedAt = Quote(
        id = "test-unsaved-id",
        content = "Stay hungry, stay foolish.",
        author = "Steve Jobs",
        length = 25,
        tags = listOf("motivation", "life"),
        savedAt = null
    )

    @Test
    fun `QuoteDto toDomain maps all fields correctly`() {
        // Act
        val domainQuote = testQuoteDto.toDomain()

        // Assert
        assertThat(domainQuote.id).isEqualTo(testQuoteDto.id)
        assertThat(domainQuote.content).isEqualTo(testQuoteDto.content)
        assertThat(domainQuote.author).isEqualTo(testQuoteDto.author)
        assertThat(domainQuote.length).isEqualTo(testQuoteDto.length)
        assertThat(domainQuote.tags).containsExactlyElementsIn(testQuoteDto.tags)
        assertThat(domainQuote.savedAt).isNull() // DTO quotes are not saved by default
    }

    @Test
    fun `QuoteDto toDomain handles empty tags`() {
        // Arrange
        val dtoWithEmptyTags = testQuoteDto.copy(tags = emptyList())

        // Act
        val domainQuote = dtoWithEmptyTags.toDomain()

        // Assert
        assertThat(domainQuote.tags).isEmpty()
    }

    @Test
    fun `QuoteDto toDomain handles single tag`() {
        // Arrange
        val dtoWithSingleTag = testQuoteDto.copy(tags = listOf("wisdom"))

        // Act
        val domainQuote = dtoWithSingleTag.toDomain()

        // Assert
        assertThat(domainQuote.tags).containsExactly("wisdom")
    }

    @Test
    fun `QuoteDto toDomain handles special characters`() {
        // Arrange
        val dtoWithSpecialChars = testQuoteDto.copy(
            content = "Quote with special chars: àáâãäåæçèéêë & \"quotes\" 🚀",
            author = "Spëcîål Àüthør"
        )

        // Act
        val domainQuote = dtoWithSpecialChars.toDomain()

        // Assert
        assertThat(domainQuote.content).isEqualTo("Quote with special chars: àáâãäåæçèéêë & \"quotes\" 🚀")
        assertThat(domainQuote.author).isEqualTo("Spëcîål Àüthør")
    }

    @Test
    fun `QuoteEntity toDomain maps all fields correctly`() {
        // Act
        val domainQuote = testQuoteEntity.toDomain()

        // Assert
        assertThat(domainQuote.id).isEqualTo(testQuoteEntity.id)
        assertThat(domainQuote.content).isEqualTo(testQuoteEntity.content)
        assertThat(domainQuote.author).isEqualTo(testQuoteEntity.author)
        assertThat(domainQuote.length).isEqualTo(testQuoteEntity.length)
        assertThat(domainQuote.tags).containsExactly("motivational", "work", "passion")
        assertThat(domainQuote.savedAt).isEqualTo(testQuoteEntity.savedAt)
    }

    @Test
    fun `QuoteEntity toDomain handles malformed tags JSON`() {
        // Arrange
        val entityWithMalformedTags = testQuoteEntity.copy(tags = "invalid json")

        // Act
        val domainQuote = entityWithMalformedTags.toDomain()

        // Assert
        assertThat(domainQuote.tags).isEmpty() // Should handle gracefully
    }

    @Test
    fun `QuoteEntity toDomain handles empty tags JSON`() {
        // Arrange
        val entityWithEmptyTags = testQuoteEntity.copy(tags = "[]")

        // Act
        val domainQuote = entityWithEmptyTags.toDomain()

        // Assert
        assertThat(domainQuote.tags).isEmpty()
    }

    @Test
    fun `QuoteEntity toDomain handles single tag JSON`() {
        // Arrange
        val entityWithSingleTag = testQuoteEntity.copy(tags = """["wisdom"]""")

        // Act
        val domainQuote = entityWithSingleTag.toDomain()

        // Assert
        assertThat(domainQuote.tags).containsExactly("wisdom")
    }

    @Test
    fun `QuoteEntity toDomain handles tags with spaces`() {
        // Arrange
        val entityWithSpacedTags = testQuoteEntity.copy(
            tags = """["tag with spaces", "another tag", "simple"]"""
        )

        // Act
        val domainQuote = entityWithSpacedTags.toDomain()

        // Assert
        assertThat(domainQuote.tags).containsExactly("tag with spaces", "another tag", "simple")
    }

    @Test
    fun `Quote toEntity maps all fields correctly`() {
        // Act
        val entity = testQuote.toEntity()

        // Assert
        assertThat(entity.id).isEqualTo(testQuote.id)
        assertThat(entity.content).isEqualTo(testQuote.content)
        assertThat(entity.author).isEqualTo(testQuote.author)
        assertThat(entity.length).isEqualTo(testQuote.length)
        assertThat(entity.tags).isEqualTo("""["wisdom","knowledge","imagination"]""")
        assertThat(entity.savedAt).isEqualTo(testQuote.savedAt)
    }

    @Test
    fun `Quote toEntity sets current timestamp when savedAt is null`() {
        // Arrange
        val beforeTime = System.currentTimeMillis()

        // Act
        val entity = testQuoteWithoutSavedAt.toEntity()
        val afterTime = System.currentTimeMillis()

        // Assert
        assertThat(entity.savedAt).isAtLeast(beforeTime)
        assertThat(entity.savedAt).isAtMost(afterTime)
    }

    @Test
    fun `Quote toEntity handles empty tags`() {
        // Arrange
        val quoteWithEmptyTags = testQuote.copy(tags = emptyList())

        // Act
        val entity = quoteWithEmptyTags.toEntity()

        // Assert
        assertThat(entity.tags).isEqualTo("[]")
    }

    @Test
    fun `Quote toEntity handles single tag`() {
        // Arrange
        val quoteWithSingleTag = testQuote.copy(tags = listOf("wisdom"))

        // Act
        val entity = quoteWithSingleTag.toEntity()

        // Assert
        assertThat(entity.tags).isEqualTo("""["wisdom"]""")
    }

    @Test
    fun `Quote toEntity handles tags with special characters`() {
        // Arrange
        val quoteWithSpecialTags = testQuote.copy(
            tags = listOf("café", "naïve", "emoji🚀", "quotes\"test")
        )

        // Act
        val entity = quoteWithSpecialTags.toEntity()

        // Assert
        assertThat(entity.tags).isEqualTo("""["café","naïve","emoji🚀","quotes\"test"]""")
    }

    @Test
    fun `Quote toDto maps all fields correctly`() {
        // Act
        val dto = testQuote.toDto()

        // Assert
        assertThat(dto.id).isEqualTo(testQuote.id)
        assertThat(dto.content).isEqualTo(testQuote.content)
        assertThat(dto.author).isEqualTo(testQuote.author)
        assertThat(dto.length).isEqualTo(testQuote.length)
        assertThat(dto.tags).containsExactlyElementsIn(testQuote.tags)
    }

    @Test
    fun `Quote toDto handles empty tags`() {
        // Arrange
        val quoteWithEmptyTags = testQuote.copy(tags = emptyList())

        // Act
        val dto = quoteWithEmptyTags.toDto()

        // Assert
        assertThat(dto.tags).isEmpty()
    }

    @Test
    fun `Quote toDto does not include savedAt field`() {
        // Act
        val dto = testQuote.toDto()

        // Assert - DTO should not have savedAt field, it's domain-specific
        // This is verified by the fact that QuoteDto doesn't have a savedAt property
        assertThat(dto.id).isEqualTo(testQuote.id)
        assertThat(dto.content).isEqualTo(testQuote.content)
        assertThat(dto.author).isEqualTo(testQuote.author)
        assertThat(dto.length).isEqualTo(testQuote.length)
        assertThat(dto.tags).containsExactlyElementsIn(testQuote.tags)
    }

    @Test
    fun `roundtrip DTO to Domain to DTO preserves data`() {
        // Act
        val domainQuote = testQuoteDto.toDomain()
        val backToDto = domainQuote.toDto()

        // Assert
        assertThat(backToDto.id).isEqualTo(testQuoteDto.id)
        assertThat(backToDto.content).isEqualTo(testQuoteDto.content)
        assertThat(backToDto.author).isEqualTo(testQuoteDto.author)
        assertThat(backToDto.length).isEqualTo(testQuoteDto.length)
        assertThat(backToDto.tags).containsExactlyElementsIn(testQuoteDto.tags)
    }

    @Test
    fun `roundtrip Entity to Domain to Entity preserves data`() {
        // Act
        val domainQuote = testQuoteEntity.toDomain()
        val backToEntity = domainQuote.toEntity()

        // Assert
        assertThat(backToEntity.id).isEqualTo(testQuoteEntity.id)
        assertThat(backToEntity.content).isEqualTo(testQuoteEntity.content)
        assertThat(backToEntity.author).isEqualTo(testQuoteEntity.author)
        assertThat(backToEntity.length).isEqualTo(testQuoteEntity.length)
        assertThat(backToEntity.savedAt).isEqualTo(testQuoteEntity.savedAt)
        
        // Tags should be equivalent (order might vary in JSON)
        val originalTags = testQuoteEntity.toDomain().tags
        val roundtripTags = backToEntity.toDomain().tags
        assertThat(roundtripTags).containsExactlyElementsIn(originalTags)
    }

    @Test
    fun `mapping handles very long content`() {
        // Arrange
        val longContent = "A".repeat(10000) // Very long quote
        val longQuoteDto = testQuoteDto.copy(
            content = longContent,
            length = longContent.length
        )

        // Act
        val domainQuote = longQuoteDto.toDomain()
        val entity = domainQuote.toEntity()
        val backToDto = domainQuote.toDto()

        // Assert
        assertThat(domainQuote.content).isEqualTo(longContent)
        assertThat(domainQuote.length).isEqualTo(10000)
        assertThat(entity.content).isEqualTo(longContent)
        assertThat(backToDto.content).isEqualTo(longContent)
    }

    @Test
    fun `mapping handles maximum number of tags`() {
        // Arrange
        val manyTags = (1..100).map { "tag$it" }
        val quoteWithManyTags = testQuote.copy(tags = manyTags)

        // Act
        val entity = quoteWithManyTags.toEntity()
        val backToDomain = entity.toDomain()
        val dto = quoteWithManyTags.toDto()

        // Assert
        assertThat(backToDomain.tags).containsExactlyElementsIn(manyTags)
        assertThat(dto.tags).containsExactlyElementsIn(manyTags)
    }

    @Test
    fun `mapping preserves data types correctly`() {
        // Arrange
        val testTimestamp = 1640995200000L
        val quote = Quote(
            id = "type-test",
            content = "Test content",
            author = "Test Author",
            length = 12,
            tags = listOf("test"),
            savedAt = testTimestamp
        )

        // Act
        val entity = quote.toEntity()
        val dto = quote.toDto()

        // Assert - Verify data types are preserved
        assertThat(entity.id).isInstanceOf(String::class.java)
        assertThat(entity.content).isInstanceOf(String::class.java)
        assertThat(entity.author).isInstanceOf(String::class.java)
        assertThat(entity.length).isInstanceOf(Int::class.java)
        assertThat(entity.tags).isInstanceOf(String::class.java)
        assertThat(entity.savedAt).isInstanceOf(Long::class.java)
        assertThat(entity.savedAt).isEqualTo(testTimestamp)

        assertThat(dto.id).isInstanceOf(String::class.java)
        assertThat(dto.content).isInstanceOf(String::class.java)
        assertThat(dto.author).isInstanceOf(String::class.java)
        assertThat(dto.length).isInstanceOf(Int::class.java)
        assertThat(dto.tags).isInstanceOf(List::class.java)
    }
}