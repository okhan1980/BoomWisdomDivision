# Project Integrity Rules & Development Guidelines

## Critical Analysis & Improvements

### 1. Build Configuration Integrity

#### Rules for Maintaining Latest Android Support:
1. **Gradle & AGP Version Management**
   - Check for AGP updates weekly during development
   - Never downgrade AGP or Gradle versions
   - Use `./gradlew wrapper --gradle-version=latest` to update
   - Current: AGP 8.10.1, Gradle wrapper should match

2. **SDK Version Policy**
   - **compileSdk**: Always use latest stable (currently 35)
   - **targetSdk**: Match compileSdk for new features
   - **minSdk**: MUST change from 34 to 26 in Phase 1
     - Current minSdk 34 limits to ~5% of devices
     - minSdk 26 covers ~98% of active devices

3. **Dependency Updates**
   ```gradle
   // Add to app/build.gradle.kts
   tasks.register("checkDependencyUpdates") {
       dependsOn("dependencyUpdates")
       doLast {
           println("Check build/dependencyUpdates/report.txt for outdated dependencies")
       }
   }
   ```

### 2. Architecture Integrity Rules

#### Package Structure Enforcement:
```
com.jomar.boomwisdomdivision/
├── data/
│   ├── api/          # Retrofit interfaces, DTOs
│   ├── db/           # Room entities, DAOs
│   ├── repository/   # Repository implementations
│   └── di/           # Data layer DI modules
├── domain/
│   ├── model/        # Domain models
│   ├── usecase/      # Business logic
│   └── repository/   # Repository interfaces
├── presentation/
│   ├── ui/
│   │   ├── screen/   # Screen composables
│   │   ├── component/# Reusable components
│   │   └── theme/    # Theme, colors, typography
│   ├── viewmodel/    # ViewModels
│   └── di/           # Presentation DI modules
└── core/
    ├── util/         # Extensions, helpers
    └── constant/     # App constants
```

#### Dependency Rules:
- **data** layer depends on **domain** only
- **presentation** depends on **domain** only
- **domain** has NO dependencies on other layers
- Use interfaces in **domain** for dependency inversion

### 3. Code Quality Enforcement

#### Mandatory Checks Before Each Phase PR:
```bash
# Add to project root as check-quality.sh
#!/bin/bash
echo "Running quality checks..."

# 1. Build check
./gradlew clean build || exit 1

# 2. Lint check
./gradlew lint || exit 1

# 3. Unit tests
./gradlew test || exit 1

# 4. Detekt (static analysis)
./gradlew detekt || exit 1

# 5. Dependency updates
./gradlew dependencyUpdates

echo "All checks passed!"
```

#### Lint Configuration:
```xml
<!-- app/lint.xml -->
<lint>
    <issue id="NewApi" severity="error" />
    <issue id="ObsoleteSdkInt" severity="warning" />
    <issue id="UnusedResources" severity="warning" />
    <issue id="IconDensities" severity="ignore" />
    <issue id="ComposePreviewPublic" severity="error" />
    <issue id="ComposeMutableParameters" severity="error" />
</lint>
```

### 4. Performance & Memory Rules

#### Baseline Profiles:
```kotlin
// Add in Phase 3
android {
    buildTypes {
        release {
            baselineProfile.automaticGenerationDuringBuild = true
        }
    }
}
```

#### Memory Leak Detection:
```kotlin
// Add LeakCanary for debug builds only
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}
```

#### Strict Mode (Debug Only):
```kotlin
// In Application class
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
}
```

### 5. Compose-Specific Rules

#### State Management:
1. Use `rememberSaveable` for configuration changes
2. Hoist state to appropriate level
3. Use `derivedStateOf` for computed values
4. Avoid recomposition with `remember { mutableStateOf() }`

#### Animation Performance:
```kotlin
// Use infinite transition for continuous animations
val infiniteTransition = rememberInfiniteTransition()
val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.7f,
    animationSpec = infiniteRepeatable(
        animation = tween(1000),
        repeatMode = RepeatMode.Reverse
    )
)
```

### 6. Security & Privacy Rules

#### API Key Management:
```gradle
// In local.properties (never commit)
QUOTABLE_BASE_URL=https://api.quotable.io

// In build.gradle.kts
android {
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_BASE_URL", "\"${properties["QUOTABLE_BASE_URL"]}\"")
        }
    }
}
```

#### Network Security:
```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.quotable.io</domain>
    </domain-config>
</network-security-config>
```

### 7. Testing Requirements

#### Per-Phase Testing Minimums:
- **Phase 1**: 80% coverage for repositories
- **Phase 2**: UI screenshot tests for main screen
- **Phase 3**: Animation performance tests
- **Phase 4**: Full E2E user flow tests
- **Phase 5**: Monkey testing, memory profiling

#### Test Configuration:
```kotlin
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }
}
```

### 8. Build Optimization

#### R8/ProGuard Rules:
```pro
# Quotable API models
-keep class com.jomar.boomwisdomdivision.data.api.model.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions

# Room
-keep class * extends androidx.room.RoomDatabase
```

#### Build Performance:
```gradle
// gradle.properties additions
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
kotlin.incremental.useClasspathSnapshot=true
```

### 9. Accessibility Requirements

1. All interactive elements must have content descriptions
2. Minimum touch target: 48dp x 48dp
3. Contrast ratio: 4.5:1 for normal text
4. Test with TalkBack enabled

### 10. Phase Checkpoint Rules

Before completing each phase:
1. Run `./check-quality.sh`
2. Update `PROGRESS.md` with detailed notes
3. Ensure no TODOs in committed code
4. Verify app runs on oldest (API 26) and newest (API 35) devices
5. Profile memory usage and fix leaks
6. Update documentation if APIs changed

### 11. Continuous Monitoring

#### GitHub Actions Workflow:
```yaml
name: Android CI
on:
  push:
    branches: [ main, feature/* ]
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
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run lint
      run: ./gradlew lint
    
    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### 12. Version Control Rules

1. **Commit Messages**: Follow conventional commits
   - `feat:` new feature
   - `fix:` bug fix
   - `docs:` documentation
   - `style:` formatting
   - `refactor:` code restructuring
   - `test:` test additions
   - `chore:` maintenance

2. **Branch Protection**:
   - Require PR reviews
   - Require status checks to pass
   - Dismiss stale reviews
   - Include administrators

### 13. Critical Path Items

**Must Complete in Phase 1:**
1. Change minSdk to 26
2. Set up Detekt for code analysis
3. Configure R8 optimization
4. Add version catalogs for all dependencies

**Blockers to Address:**
1. Quotable API's 125-char limit may be too short
2. CRT perspective effect complexity
3. Golden glow performance on low-end devices

### 14. Rollback Strategy

If issues arise:
1. Each phase in separate branch
2. Tag each phase completion
3. Maintain compatibility between phases
4. Document breaking changes

### 15. Success Metrics Tracking

Track from Phase 1:
```kotlin
// Analytics events to implement
sealed class AnalyticsEvent {
    object AppLaunched : AnalyticsEvent()
    object QuoteGenerated : AnalyticsEvent()
    object QuoteBookmarked : AnalyticsEvent()
    data class ErrorOccurred(val error: String) : AnalyticsEvent()
}
```

## Red Flags to Watch

1. **Performance**: Quote transition drops below 60fps
2. **Memory**: App uses >100MB for basic operation  
3. **Network**: API calls take >2 seconds
4. **Crashes**: Any crash in core flow
5. **Architecture**: Layers start depending incorrectly

## Phase Gate Criteria

No phase proceeds without:
- [ ] All tests passing
- [ ] No critical lint errors
- [ ] Memory profiling shows no leaks
- [ ] Runs on API 26 and API 35
- [ ] PR approved with no unresolved comments
- [ ] PROGRESS.md updated completely