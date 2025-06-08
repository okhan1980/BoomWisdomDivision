package com.jomar.boomwisdomdivision.data.api.interceptor

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

/**
 * Custom logging interceptor for HTTP requests and responses
 *
 * Provides detailed logging of HTTP requests and responses for debugging purposes.
 * Uses Timber for logging with appropriate log levels.
 *
 * @return HttpLoggingInterceptor configured with custom logger
 */
fun createLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor { message ->
        Timber.tag("BoomWisdom-HTTP").d(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}