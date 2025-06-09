package com.jomar.boomwisdomdivision.data.api.interceptor

import com.jomar.boomwisdomdivision.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * Custom logging interceptor for HTTP requests and responses.
 *
 * This interceptor logs detailed information about HTTP requests and responses
 * for debugging purposes. It logs request method, URL, headers, and response
 * status codes using Timber.
 */
class LoggingInterceptor : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Log request details
        Timber.tag("BoomWisdom-HTTP").d(
            "Request: ${request.method} ${request.url}"
        )
        
        // Log request headers if in debug mode
        if (BuildConfig.DEBUG) {
            request.headers.forEach { (name, value) ->
                Timber.tag("BoomWisdom-HTTP").d("Request Header: $name: $value")
            }
        }
        
        val startTime = System.currentTimeMillis()
        
        return try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime
            
            // Log response details
            Timber.tag("BoomWisdom-HTTP").d(
                "Response: ${response.code} ${response.message} (${duration}ms)"
            )
            
            response
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Timber.tag("BoomWisdom-HTTP").e(
                e,
                "Request failed after ${duration}ms: ${request.url}"
            )
            throw e
        }
    }
}