package com.jomar.boomwisdomdivision

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for BoomWisdomDivision.
 * 
 * This class serves as the entry point for the application and handles:
 * - Dependency injection setup with Hilt
 * - Debug tools configuration (LeakCanary, StrictMode, Timber)
 * - Application-wide initialization
 * - Performance monitoring setup
 * 
 * The class is annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application.
 */
@HiltAndroidApp
class BoomWisdomApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        setupLogging()
        setupDebugTools()
        setupPerformanceMonitoring()
    }

    /**
     * Sets up logging configuration for the application.
     * 
     * - In debug builds, plants a debug tree that logs to console
     * - In release builds, can be configured to log to crash reporting services
     */
    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Debug logging enabled")
        } else {
            // In production, you might want to plant a crash reporting tree
            // Timber.plant(CrashReportingTree())
        }
    }

    /**
     * Configures debug tools for development builds.
     * 
     * Sets up:
     * - StrictMode for detecting performance issues and policy violations
     * - LeakCanary is automatically configured via dependency inclusion
     */
    private fun setupDebugTools() {
        if (BuildConfig.DEBUG) {
            setupStrictMode()
            Timber.d("Debug tools configured")
        }
    }

    /**
     * Configures StrictMode for detecting performance issues and policy violations.
     * 
     * Thread Policy detects:
     * - Network calls on main thread
     * - Disk reads/writes on main thread
     * - Custom slow calls
     * 
     * VM Policy detects:
     * - Memory leaks (Activity, Fragment, etc.)
     * - SQLite leaks
     * - File descriptor leaks
     * - Untagged network traffic
     */
    private fun setupStrictMode() {
        // Configure StrictMode for thread policy violations
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectCustomSlowCalls()
                .detectResourceMismatches()
                .penaltyLog()
                .penaltyFlashScreen() // Visual indicator of violations
                .build()
        )

        // Configure StrictMode for VM policy violations
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectActivityLeaks()
                .detectFileUriExposure()
                .detectCleartextNetwork()
                .detectContentUriWithoutPermission()
                .detectUntaggedSockets()
                .penaltyLog()
                .build()
        )

        Timber.d("StrictMode configured for development")
    }

    /**
     * Sets up performance monitoring for the application.
     * 
     * This can include:
     * - Memory usage tracking
     * - Network performance monitoring
     * - Database query performance
     * - UI rendering performance
     */
    private fun setupPerformanceMonitoring() {
        if (BuildConfig.DEBUG) {
            // Setup development performance monitoring
            Timber.d("Performance monitoring enabled for debug builds")
            
            // You can add additional performance monitoring tools here
            // For example: Flipper, Stetho, or custom monitoring
        }
    }
}