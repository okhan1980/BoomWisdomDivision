# Phase 1: Foundation & Core Architecture - Detailed Plan

## Overview
Phase 1 establishes the technical foundation with proper architecture, dependencies, and quality checks to ensure long-term project success.

## Critical Setup Tasks

### 1. Project Configuration Updates
```kotlin
// app/build.gradle.kts modifications needed:
android {
    compileSdk = 35  // Keep latest
    
    defaultConfig {
        minSdk = 26  // CHANGE FROM 34
        targetSdk = 35
        
        testInstrumentationRunner = "com.jomar.boomwisdomdivision.HiltTestRunner"
    }
    
    buildFeatures {
        buildConfig = true  // Enable BuildConfig
    }
    
    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
    }
    
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
```

### 2. Complete Dependency List
```toml
# gradle/libs.versions.toml additions needed:

[versions]
retrofit = "2.11.0"
moshi = "1.15.1"
hilt = "2.51"
room = "2.6.1"
lifecycle = "2.8.7"
coroutines = "1.8.1"
timber = "5.0.1"
detekt = "1.23.7"
leakcanary = "2.14"

[libraries]
# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
moshi = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi" }
moshi-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version = "4.12.0" }

# Dependency Injection
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Database
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Architecture Components
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }

# Coroutines
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# Utilities
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
leakcanary = { group = "com.squareup.leakcanary", name = "leakcanary-android", version.ref = "leakcanary" }

# Testing additions
mockk = { group = "io.mockk", name = "mockk", version = "1.13.12" }
turbine = { group = "app.cash.turbine", name = "turbine", version = "1.1.0" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.21-1.0.28" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

### 3. Package Structure Creation

```
app/src/main/java/com/jomar/boomwisdomdivision/
├── BoomWisdomApplication.kt
├── data/
│   ├── api/
│   │   ├── QuotableApi.kt
│   │   ├── model/
│   │   │   ├── QuoteResponse.kt
│   │   │   └── QuoteDto.kt
│   │   └── interceptor/
│   │       └── LoggingInterceptor.kt
│   ├── db/
│   │   ├── BoomWisdomDatabase.kt
│   │   ├── dao/
│   │   │   └── QuoteDao.kt
│   │   ├── entity/
│   │   │   └── QuoteEntity.kt
│   │   └── converter/
│   │       └── DateConverter.kt
│   ├── repository/
│   │   └── QuoteRepositoryImpl.kt
│   └── di/
│       ├── NetworkModule.kt
│       ├── DatabaseModule.kt
│       └── RepositoryModule.kt
├── domain/
│   ├── model/
│   │   └── Quote.kt
│   ├── repository/
│   │   └── QuoteRepository.kt
│   └── usecase/
│       ├── GetRandomQuoteUseCase.kt
│       ├── SaveQuoteUseCase.kt
│       ├── GetSavedQuotesUseCase.kt
│       └── DeleteQuoteUseCase.kt
├── presentation/
│   ├── MainActivity.kt
│   └── di/
│       └── PresentationModule.kt
└── core/
    ├── util/
    │   ├── Constants.kt
    │   ├── Extensions.kt
    │   └── Result.kt
    └── di/
        └── DispatcherModule.kt
```

### 4. Core Models

```kotlin
// domain/model/Quote.kt
data class Quote(
    val id: String,
    val content: String,
    val author: String,
    val length: Int,
    val tags: List<String>,
    val savedAt: Long? = null
)

// data/api/model/QuoteDto.kt
@JsonClass(generateAdapter = true)
data class QuoteDto(
    @Json(name = "_id") val id: String,
    @Json(name = "content") val content: String,
    @Json(name = "author") val author: String,
    @Json(name = "length") val length: Int,
    @Json(name = "tags") val tags: List<String>
)

// data/db/entity/QuoteEntity.kt
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val id: String,
    val content: String,
    val author: String,
    val length: Int,
    val tags: String, // JSON string
    val savedAt: Long
)
```

### 5. Quality Setup Tasks

1. **Detekt Configuration**
```yaml
# detekt.yml
build:
  maxIssues: 0
  excludeCorrectable: false

complexity:
  LongMethod:
    threshold: 30
  ComplexMethod:
    threshold: 10
  TooManyFunctions:
    active: false

style:
  MaxLineLength:
    maxLineLength: 120
  WildcardImport:
    active: false
```

2. **GitHub Actions**
```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main, develop, feature/* ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run detekt
      run: ./gradlew detekt
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Generate APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### 6. Implementation Order

#### Day 1: Core Setup
1. Update build.gradle.kts with all dependencies
2. Create package structure
3. Set up Hilt modules
4. Configure Detekt and quality checks
5. Create base models and interfaces

#### Day 2: Data Layer
1. Implement Retrofit API interface
2. Create Room database and DAOs
3. Build repository implementation
4. Add data mappers
5. Write repository tests

#### Day 3: Domain & Initial Integration
1. Create use cases
2. Set up coroutine dispatchers
3. Implement error handling
4. Write use case tests
5. Verify API integration

### 7. Testing Requirements

```kotlin
// Minimum tests for Phase 1:
- QuoteRepositoryImplTest (mock API and DB)
- QuoteDaoTest (Room testing)
- QuoteApiTest (MockWebServer)
- GetRandomQuoteUseCaseTest
- MapperTest (DTO to Domain)
```

### 8. Definition of Done

- [ ] MinSdk changed to 26
- [ ] All dependencies added and building
- [ ] Package structure created
- [ ] Hilt configuration working
- [ ] API integration tested manually
- [ ] Database schema created
- [ ] Repository pattern implemented
- [ ] Basic error handling in place
- [ ] Detekt passing with 0 issues
- [ ] Unit tests >80% coverage on data layer
- [ ] GitHub Actions CI passing
- [ ] No memory leaks in LeakCanary
- [ ] PR created with comprehensive summary

### 9. Risk Mitigation

1. **API Issues**: Create mock responses for testing
2. **Database Migrations**: Start with version 1 schema
3. **Dependency Conflicts**: Use BOM where available
4. **Build Times**: Enable build cache and parallel builds

### 10. Architecture Decision Records

1. **Why MVVM**: Best Compose support, lifecycle aware
2. **Why Hilt**: Official Google recommendation, Compose integration
3. **Why Room**: Type-safe, migration support, Coroutines integration
4. **Why Retrofit/Moshi**: Lightweight, Kotlin-first, good performance
5. **Why Repository Pattern**: Abstraction for data sources, testability