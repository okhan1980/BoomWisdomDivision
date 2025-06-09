package com.jomar.boomwisdomdivision

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom AndroidJUnitRunner for Hilt testing.
 * 
 * This runner creates the Hilt test application instead of the real application,
 * allowing for dependency injection testing with test doubles and mocks.
 * 
 * The test runner is configured in the build.gradle.kts file as the default test runner.
 * It's essential for instrumented tests that need to inject dependencies or use Hilt modules.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    /**
     * Creates and returns the Hilt test application.
     * 
     * @param cl The class loader to use for creating the application
     * @param className The application class name (ignored for Hilt tests)
     * @param context The application context
     * @return HiltTestApplication instance for dependency injection testing
     */
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
