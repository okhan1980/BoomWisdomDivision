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
- **Min SDK**: 34 (Android 14) - Consider lowering to 24-26 for broader device support
- **Target SDK**: 35 (Android 15)

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