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

### Technology Stack
- **UI Framework**: Jetpack Compose with Material3
- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL and Version Catalog
- **Min SDK**: 34 (Android 14) - Should be lowered to 26 for broader device support
- **Target SDK**: 35 (Android 15)
- **Architecture**: MVVM with Repository Pattern
- **DI Framework**: Hilt
- **Networking**: Retrofit with Moshi
- **Database**: Room
- **Quote API**: Quotable API (free, no auth required)

### Key Implementation Requirements

1. **Quote Display System**
   - Implement perspective transformation for text to match CRT monitor angle
   - Monospace font (Courier New or IBM Plex Mono)
   - Golden glow effects on the monitor base

2. **API Integration Required**
   - Recommended APIs: Quotable, ZenQuotes, or Quotes.rest
   - Must handle quote text and author attribution
   - Implement offline fallback for saved quotes

3. **Local Storage**
   - Implement favorites system using SharedPreferences or Room
   - Store: quote text, author, save date, last viewed quote

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

### Phase-Based Development
The project follows a phased approach with PR reviews after each phase:
1. **Phase 1**: Foundation & Core Architecture
2. **Phase 2**: UI Implementation - Main Screen
3. **Phase 3**: Core Functionality & Animations
4. **Phase 4**: Favorites Screen & Polish
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

**Caching Strategy**:
- Pre-fetch 10-20 quotes in background
- Store in Room database with timestamp
- Serve from cache for instant response
- Refresh cache when below 5 quotes
- Implement exponential backoff for failures

### Testing Requirements
- Unit tests for ViewModels and Repositories
- UI tests for critical user flows
- Animation performance testing
- Network error scenario testing
- Database migration testing

### Performance Targets
- App launch: < 2 seconds
- Quote generation: < 500ms (from cache)
- Animation FPS: 60fps consistent
- Memory usage: < 100MB
- APK size: < 15MB