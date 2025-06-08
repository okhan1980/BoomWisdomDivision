package com.jomar.boomwisdomdivision.data.di

import com.jomar.boomwisdomdivision.data.api.QuotableApi
import com.jomar.boomwisdomdivision.data.api.interceptor.LoggingInterceptor
import com.jomar.boomwisdomdivision.core.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for providing network-related dependencies.
 * 
 * This module configures the networking layer for the Boom Wisdom Division app,
 * including HTTP client setup, JSON serialization, and API interface provisioning.
 * All network dependencies are scoped as singletons to ensure efficient resource usage.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * Provides a configured Moshi instance for JSON serialization/deserialization.
     * 
     * Moshi is configured with the Kotlin adapter factory to support Kotlin-specific
     * features like data classes, default parameters, and nullable types.
     * 
     * @return Configured [Moshi] instance
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    
    /**
     * Provides an HTTP logging interceptor for debugging network requests.
     * 
     * The logging level is set based on the build variant:
     * - DEBUG builds: Full body logging for detailed debugging
     * - RELEASE builds: Basic logging for minimal overhead
     * 
     * @return Configured [HttpLoggingInterceptor]
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (com.jomar.boomwisdomdivision.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }
    
    /**
     * Provides the custom logging interceptor for application-specific logging.
     * 
     * This interceptor handles app-specific logging requirements and can be
     * customized for different logging strategies or analytics.
     * 
     * @return Configured [LoggingInterceptor]
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): LoggingInterceptor {
        return LoggingInterceptor()
    }
    
    /**
     * Provides a configured OkHttpClient for network requests.
     * 
     * The client is configured with:
     * - Connection timeout settings
     * - Read/write timeout settings
     * - Logging interceptors for debugging
     * - Error handling and retry logic
     * 
     * @param httpLoggingInterceptor HTTP logging interceptor for network debugging
     * @param loggingInterceptor Custom logging interceptor for app-specific logging
     * @return Configured [OkHttpClient]
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        loggingInterceptor: LoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    /**
     * Provides a configured Retrofit instance for API communication.
     * 
     * Retrofit is configured with:
     * - Base URL for the Quotable API
     * - Moshi converter for JSON handling
     * - OkHttpClient for network requests
     * 
     * @param okHttpClient Configured HTTP client
     * @param moshi Configured JSON serializer
     * @return Configured [Retrofit] instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    /**
     * Provides the QuotableApi interface implementation.
     * 
     * This creates a dynamic implementation of the API interface using Retrofit's
     * annotation processing. The implementation handles HTTP requests, response
     * parsing, and error handling automatically.
     * 
     * @param retrofit Configured Retrofit instance
     * @return [QuotableApi] implementation for making API calls
     */
    @Provides
    @Singleton
    fun provideQuotableApi(retrofit: Retrofit): QuotableApi {
        return retrofit.create(QuotableApi::class.java)
    }
}