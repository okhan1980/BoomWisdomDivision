# Project Integrity Rules & Development Guidelines

## Project Overview
BoomWisdomDivision follows a **simplified development approach** prioritizing working functionality over complex architecture patterns.

## Core Principles
1. **Working first, perfect later**: Get functionality running quickly
2. **Simple is better**: Avoid over-engineering 
3. **Incremental complexity**: Add features one at a time
4. **Focus on UX**: The CRT interface is the main attraction

---

## 1. Build Configuration Integrity

### SDK Version Policy
- **compileSdk**: 35 (latest stable)
- **targetSdk**: 35 (match compileSdk)
- **minSdk**: 26 (covers ~98% of devices, already set)

### Gradle & AGP Management
- Current: AGP 8.10.1 with Gradle 8.10.2
- Check for updates periodically but don't chase bleeding edge
- Use version catalog for all dependencies

### Quality Check Script
```bash
#!/bin/bash
# check-quality.sh
echo "Running quality checks..."

./gradlew clean build || exit 1
./gradlew lint || exit 1
./gradlew test || exit 1
./gradlew detekt || exit 1

echo "All checks passed!"
```

---

## 2. Simplified Architecture Rules

### Package Structure (Simplified)
```
com.jomar.boomwisdomdivision/
├── data/
│   ├── api/          # API interfaces, response models
│   ├── model/        # Data models  
│   └── repository/   # Repository implementations
├── ui/
│   ├── components/   # Reusable UI components
│   ├── theme/        # Colors, typography, theme
│   └── screen/       # Screen composables (if multiple)
└── MainActivity.kt   # Main entry point
```

### Dependency Rules (Simplified)
- **No Hilt/Dagger**: Use simple singletons and manual DI
- **No Room Database**: SharedPreferences for simple storage
- **No Retrofit**: Basic OkHttp + Moshi for networking
- **Simple MVVM**: ViewModels + Composables, minimal abstraction

### Dependencies by Phase
```kotlin
// Phase 2: Just Compose (already have)
// Phase 3: Add networking (already added)
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

// Phase 4: Add if needed
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

---

## 3. Code Quality Rules

### Mandatory Checks Before Each Phase
1. Run `./check-quality.sh`
2. App builds and runs successfully
3. Manual testing on target devices
4. Core functionality works as expected
5. No TODO comments in committed code

### Lint Configuration (Minimal)
```xml
<!-- app/lint.xml -->
<lint>
    <issue id="NewApi" severity="error" />
    <issue id="UnusedResources" severity="warning" />
    <issue id="ComposePreviewPublic" severity="error" />
</lint>
```

---

## 4. Performance Guidelines

### Memory Management
- Monitor memory usage (target: <100MB)
- Add LeakCanary for debug builds only:
```kotlin
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}
```

### Compose Performance
- Use `remember` appropriately to avoid recomposition
- Implement smooth 60fps animations for quote transitions
- Optimize golden glow effects for performance

---

## 5. Testing Requirements (Simplified)

### Per-Phase Testing Minimums
- **Phase 2**: Manual testing of UI components
- **Phase 3**: Unit tests for quote repository logic
- **Phase 4**: Basic UI tests for critical flows
- **Phase 5**: End-to-end testing and release prep

### Test Configuration
```kotlin
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}
```

---

## 6. Security & API Management

### API Configuration
```kotlin
// Simple configuration approach
object ApiConfig {
    const val BASE_URL = "https://api.quotable.io"
    const val TIMEOUT_SECONDS = 30L
}
```

### Network Security
- Enforce HTTPS only
- Basic error handling and retry logic
- Simple exponential backoff for failures

---

## 7. Common Pitfalls to Avoid

1. **Don't over-engineer**: Keep architecture simple
2. **Don't add dependencies** without version catalog entries
3. **Don't skip basic testing** for core functionality
4. **Don't commit code** with TODO comments
5. **Don't add complex patterns** when simple ones work
6. **Don't use advanced SDK features** without API level checks

---

## 8. Version Control Rules

### Commit Messages (Simplified)
- `feat:` new feature
- `fix:` bug fix
- `docs:` documentation updates
- `style:` formatting/UI changes
- `refactor:` code restructuring

### Branch Strategy
- Feature branches: `feature/phase-X-description`
- PR after each phase with summary
- Main branch contains only stable, reviewed code

---

## 9. Phase Checkpoint Rules

Before completing each phase:
1. ✅ Run `./check-quality.sh`
2. ✅ Update `PROGRESS.md` with achievements
3. ✅ Ensure no TODOs in committed code
4. ✅ Verify app runs on API 26 and API 35 devices
5. ✅ Test core functionality manually
6. ✅ Create PR with comprehensive summary

---

## 10. Development Focus Areas

### Phase 2 Priorities (Current)
- CRT monitor visual styling with perspective
- Monospace font implementation
- Golden glow effects
- Smooth quote transitions
- Portrait orientation lock

### Performance Targets
- App launch: < 2 seconds
- Quote transitions: 60fps smooth
- Memory usage: < 100MB
- API response: < 500ms (from cache)

---

## Success Criteria

### Each Phase Must Achieve:
- ✅ Functional feature implementation
- ✅ No critical bugs or crashes
- ✅ Smooth user experience
- ✅ Clean, readable code
- ✅ Updated documentation

### Red Flags to Address Immediately:
- App crashes during normal use
- Animation performance below 30fps
- Memory leaks detected
- API calls failing consistently
- Build failures or lint errors

---

## Quality Gates

**No phase proceeds without:**
- [ ] All builds passing
- [ ] Core functionality working
- [ ] No critical issues
- [ ] PROGRESS.md updated
- [ ] PR approved and merged

**Remember**: Focus on delivering working features over perfect architecture. Complexity can be added incrementally as needed.