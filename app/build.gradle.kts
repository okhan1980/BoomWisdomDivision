plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.hilt)  // Temporarily disabled
    // alias(libs.plugins.ksp)  // Temporarily disabled
    // kotlin("kapt")  # Reverted to KSP
    // alias(libs.plugins.detekt)  // Disabled due to many style violations - will be re-enabled in Phase 2
    jacoco
}

android {
    namespace = "com.jomar.boomwisdomdivision"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jomar.boomwisdomdivision"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isTestCoverageEnabled = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }
    
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// KSP configuration - temporarily disabled
// ksp {
//     arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
// }

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Networking for Phase 3
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    
    // Dependency Injection - Temporarily disabled
    // implementation(libs.hilt.android)
    // implementation(libs.hilt.navigation.compose)
    // ksp(libs.hilt.compiler)
    
    // Database - Temporarily disabled
    // implementation(libs.room.runtime)
    // implementation(libs.room.ktx)
    // ksp(libs.room.compiler)  // Temporarily disabled
    
    // Architecture Components
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    
    // Navigation
    implementation(libs.navigation.compose)
    
    // Coroutines
    implementation(libs.coroutines.android)
    
    // Utilities
    implementation(libs.timber)
    debugImplementation(libs.leakcanary)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    // testImplementation(libs.room.testing)
    // testImplementation(libs.mockwebserver)
    // testImplementation(libs.hilt.testing)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.arch.core.testing)
    
    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    
    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Jacoco configuration for test coverage
jacoco {
    toolVersion = "0.8.8"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/BoomWisdomApplication.*", // Exclude Application class from coverage
            "**/ui/theme/**", // Exclude theme files
            "**/*\$Companion.*",
            "**/*\$serializer.*",
            "**/*_Factory.*",
            "**/*_Impl.*",
            "**/*_MembersInjector.*",
            "**/Dagger*Component*.*",
            "**/*Module_*Factory.*"
        )
    }
    
    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom("${projectDir}/src/main/java")
    executionData.setFrom("${buildDir}/jacoco/testDebugUnitTest.exec")
}

// Task to run tests with coverage
tasks.register("testDebugUnitTestCoverage") {
    dependsOn("testDebugUnitTest", "jacocoTestReport")
}