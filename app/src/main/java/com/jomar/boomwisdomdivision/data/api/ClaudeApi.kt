package com.jomar.boomwisdomdivision.data.api

import com.jomar.boomwisdomdivision.BuildConfig
import com.jomar.boomwisdomdivision.model.Quote
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@JsonClass(generateAdapter = true)
data class ClaudeRequest(
    val model: String = "claude-3-5-sonnet-20241022",
    val messages: List<Message>,
    val max_tokens: Int = 100,
    val temperature: Double = 0.9
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ClaudeResponse(
    val id: String,
    val content: List<Content>
)

@JsonClass(generateAdapter = true)
data class Content(
    val text: String?,
    val type: String
)

class ClaudeApi {
    companion object {
        private const val BASE_URL = "https://api.anthropic.com/v1/messages"
        private const val API_VERSION = "2023-06-01"
        
        @Volatile
        private var instance: ClaudeApi? = null

        fun getInstance(): ClaudeApi {
            return instance ?: synchronized(this) {
                instance ?: ClaudeApi().also { instance = it }
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { message ->
            Timber.tag("ClaudeApi").d(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(ClaudeRequest::class.java)
    private val responseAdapter = moshi.adapter(ClaudeResponse::class.java)

    suspend fun generateQuote(category: String = "motivation"): Quote? = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(category)
            val requestBody = ClaudeRequest(
                messages = listOf(
                    Message(
                        role = "user",
                        content = prompt
                    )
                )
            )

            val json = requestAdapter.toJson(requestBody)
            val request = Request.Builder()
                .url(BASE_URL)
                .addHeader("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
                .addHeader("anthropic-version", API_VERSION)
                .addHeader("content-type", "application/json")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Timber.e("Claude API error: ${response.code} - ${response.message} - Body: $errorBody")
                when (response.code) {
                    401 -> Timber.e("Authentication failed - check API key")
                    429 -> Timber.e("Rate limit exceeded")
                    500, 502, 503 -> Timber.e("Claude server error")
                }
                return@withContext null
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                Timber.e("Empty response from Claude API")
                return@withContext null
            }

            val claudeResponse = responseAdapter.fromJson(responseBody)
            val generatedText = claudeResponse?.content?.firstOrNull()?.text
            
            if (generatedText.isNullOrBlank()) {
                Timber.e("No text content in Claude response")
                return@withContext null
            }

            // Parse the quote and author from the generated text
            val lines = generatedText.trim().split("\n")
            val quoteLine = lines.firstOrNull { it.isNotBlank() } ?: return@withContext null
            val authorLine = lines.getOrNull(1)?.removePrefix("- ")?.trim() ?: "Unknown"

            Quote(
                text = quoteLine.removeSurrounding("\""),
                author = authorLine,
                id = "claude_${System.currentTimeMillis()}_${Random.nextInt()}"
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate quote from Claude")
            null
        }
    }

    private fun buildPrompt(category: String): String {
        val categoryPrompt = when (category.lowercase()) {
            "mindfulness" -> "Generate a short mindfulness quote about being present, awareness, inner peace, or meditation."
            "creativity" -> "Generate a short creativity quote about innovation, imagination, artistic expression, or creative thinking."
            else -> "Generate a short motivational quote about success, perseverance, growth, or achieving goals."
        }
        
        return """
            $categoryPrompt
            
            Requirements:
            - The quote MUST be 15 words or less (count each word carefully)
            - Make it profound, inspiring, and memorable
            - Write ONLY the quote and author, nothing else
            - Format exactly as shown in the example below
            - Create an original quote, not an existing famous quote
            - The author should be a fictional but realistic-sounding name
            
            Example format (exactly like this):
            Dreams become reality when courage meets determination.
            - Sarah Mitchell
            
            Respond with ONLY the quote and author name, no additional text.
        """.trimIndent()
    }
}