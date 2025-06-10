# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BoomWisdomDivision is an Android app that presents motivational quotes through a retro CRT monitor interface. The app combines nostalgic design with modern Android development practices using Jetpack Compose.

## Build and Development Commands

```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Install debug APK on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Generate APK
./gradlew assembleDebug
./gradlew assembleRelease

# Check for dependency updates
./gradlew dependencyUpdates
```

## Architecture and Structure

### Technology Stack (Simplified Approach)
- **UI Framework**: Jetpack Compose with Material3
- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL and Version Catalog
- **Min SDK**: 26 (Android 8.0) - For broader device support
- **Target SDK**: 35 (Android 15)
- **Architecture**: Simple MVVM pattern (no Hilt/Dagger DI)
- **Networking**: Basic OkHttp + Moshi (no Retrofit)
- **Local Storage**: SharedPreferences (no Room database)
- **Quote API**: Quotable API (free, no auth required)
- **Dependencies**: Added gradually per phase to avoid complexity

### Simplified Architecture Principles
1. **No Hilt/Dagger**: Use simple singletons and manual DI
2. **No Room Database**: SharedPreferences for simple storage
3. **No Complex Networking**: Basic OkHttp calls
4. **Simple MVVM**: ViewModels + Composables, minimal abstraction
5. **Working first, perfect later**: Get functionality running quickly

### Dependencies Added by Phase
```kotlin
// Phase 2: Just Compose basics (already have)
// Phase 3: Add networking
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

// Phase 4: Add if needed for preferences
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

### Key Implementation Requirements

1. **Quote Display System**
   - Implement perspective transformation for text to match CRT monitor angle
   - Monospace font (Courier New or IBM Plex Mono)
   - Golden glow effects on the monitor base

2. **API Integration (Phase 3)**
   - Start with hardcoded quotes in Phase 2, then add Quotable API
   - Simple HTTP calls with basic error handling
   - Memory caching for offline fallback (no database)

3. **Local Storage (Phase 4)**
   - SharedPreferences for favorites and settings
   - Store: favorite quotes, last viewed quote, user preferences
   - Simple key-value storage approach

4. **UI Components Needed**
   - Custom CRT monitor composable with perspective tilt
   - Star button with press animations and glow effects
   - Bookmark toggle with filled/outline states
   - Favorites list screen

5. **Animation Specifications**
   - Quote transition: 1.5s total (glow → fade out → fade in → unglow)
   - Star button: scale to 0.95x on press
   - Bookmark: scale bounce effect (1.0x → 1.2x → 1.0x)

### Color Scheme
- Background: Pure black (#000000)
- Monitor display: Off-white (#F5F5F0)
- Text: Dark gray (#1A1A1A)
- Accent/Glow: Golden yellow (#FFD700)

### Configuration Notes
- App must be locked to portrait orientation
- Status and navigation bars should be hidden or translucent black
- Currently using default Material theme - needs update to match design requirements

## Development Workflow

### Phase-Based Development (Simplified)
The project follows a simplified phased approach with PR reviews after each phase:
1. **Phase 1**: Foundation & Core Architecture ✅ Complete
2. **Phase 2**: Core UI & CRT Display (hardcoded quotes, CRT interface)
3. **Phase 3**: Quote Management & API (Quotable API integration)
4. **Phase 4**: Persistence & Polish (favorites, SharedPreferences)
5. **Phase 5**: Testing & Release Prep

### Progress Tracking
- Check `PROGRESS.md` for current development status
- Each phase creates a feature branch and PR
- Use Task agents for parallel development within phases

### Git Workflow
- Feature branches: `feature/phase-X-description`
- Commit format: `type(scope): description`
- PR after each phase with comprehensive summary
- Main branch contains only stable, reviewed code

### API Implementation Notes

**Quotable API Usage**:
- Base URL: `https://api.quotable.io`
- Random quote endpoint: `/quotes/random`
- Recommended tags filter: `?tags=motivational|inspirational|wisdom`
- Rate limit: 180 requests/minute (very generous)
- No authentication required
- Response includes: content, author, length, tags

**Caching Strategy (Simplified)**:
- Cache 10-20 quotes in memory for offline use
- Simple refresh mechanism when cache is low
- Basic error handling and retry logic
- No database - memory-only caching initially

### Testing Requirements (Phase 5)
- Unit tests for core quote logic
- UI tests for critical user flows
- Manual testing on multiple devices
- Basic performance testing
- Network error scenario testing

### Performance Targets
- App launch: < 2 seconds
- Quote generation: < 500ms (from cache)
- Animation FPS: 60fps consistent
- Memory usage: < 100MB
- APK size: < 15MB

## Critical Project Rules

**IMPORTANT**: Before any development, review:
1. `SIMPLIFIED_DEVELOPMENT_PLAN.md` - Current simplified development approach
2. `CURRENT_PHASE_PLAN.md` - Specific implementation details for the active phase
3. `PHASE_ROTATION_PROTOCOL.md` - Process for transitioning between phases
4. `PROJECT_INTEGRITY_RULES.md` - Code quality guidelines

### Pre-Development Checklist
- [ ] Verify Android Studio has SDK 35 installed
- [ ] Confirm JDK 17 is being used (REQUIRED - AGP 8.10.1 needs Java 17)
- [ ] Review current phase plan in `CURRENT_PHASE_PLAN.md`
- [ ] Understand simplified architecture (no complex DI, simple MVVM)
- [ ] Check that minSdk is 26 for broader device support

### Quality Gates
Each phase must pass these checks:
1. Run `./check-quality.sh` script
2. Detekt analysis shows 0 issues
3. App builds and runs successfully
4. Manual testing on target devices
5. Core functionality works as expected

### Common Pitfalls to Avoid
1. Don't over-engineer - keep architecture simple
2. Don't add dependencies without version catalog entries
3. Don't skip basic testing for core functionality
4. Don't commit code with TODO comments
5. Don't add complex patterns when simple ones work
6. Don't use advanced SDK features without API level checks

## Phase Management

### Current Phase Tracking
- Active phase plan is always in `CURRENT_PHASE_PLAN.md`
- Completed phases are archived in `archive/phase-X-completed/`
- Use `./phase-rotate.sh <current> <next>` to transition phases

### Phase Rotation Process
1. Complete all tasks in current phase
2. Create PR and get it merged
3. Run phase rotation script
4. Fill in completion summary
5. Begin next phase with fresh plan

This ensures focus on current work while preserving project history.

## 🚨 MANDATORY: Pull Request Workflow

**CRITICAL**: Never push directly to main branch!

### All changes must follow PR workflow:
1. Create feature branch: `git checkout -b feature/description`
2. Make changes and commit
3. Push branch: `git push -u origin feature/branch-name`
4. Create PR: `gh pr create`
5. Wait for review and approval
6. Only merge after explicit approval

See `PR_WORKFLOW.md` for complete guidelines.