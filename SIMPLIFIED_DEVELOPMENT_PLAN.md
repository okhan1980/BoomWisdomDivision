# Simplified BoomWisdomDivision Development Plan

## Overview
Building a retro CRT monitor quote display app with a focus on working functionality over complex architecture. We'll start simple and add features incrementally.

## Phase Breakdown

### Phase 2: Core UI & CRT Display ✅ Ready to Start
**Goal**: Create the iconic CRT monitor interface with quote display

#### Tasks:
1. **Update app configuration**
   - Change minSdk from 26 to support more devices
   - Update app theme colors to match CRT aesthetic (black background, golden glow)
   - Set portrait-only orientation

2. **Create CRT Monitor Composable**
   - Design retro CRT monitor frame with perspective tilt
   - Implement monospace font display (Courier New/IBM Plex Mono)
   - Add golden glow effects around monitor base
   - Create perspective text transformation for screen angle

3. **Basic Quote Display**
   - Start with hardcoded inspirational quotes
   - Display quote text and author
   - Implement quote rotation (next/previous buttons)
   - Add smooth transitions between quotes

4. **Basic Interactions**
   - Star/favorite button with scale animation
   - Simple navigation (if multiple screens needed)
   - Basic touch interactions

**Deliverable**: Working CRT interface showing rotating quotes with retro styling

---

### Phase 3: Quote Management & API
**Goal**: Add dynamic quote fetching and basic data management

#### Tasks:
1. **Simple Data Models**
   - Create basic Quote data class (text, author, id)
   - Simple quote repository pattern (no DI framework)

2. **API Integration**
   - Add basic OkHttp + Moshi dependencies
   - Create simple HTTP client for Quotable API
   - Implement basic quote fetching with error handling
   - Add loading states for quote fetching

3. **Quote Caching**
   - Cache 10-20 quotes in memory for offline use
   - Simple refresh mechanism
   - Basic error handling and retry logic

**Deliverable**: App fetches real quotes from API with offline fallback

---

### Phase 4: Persistence & Polish
**Goal**: Add favorites and final polish

#### Tasks:
1. **Local Storage**
   - SharedPreferences for favorite quotes
   - Save/restore last viewed quote
   - User preferences (theme, font size, etc.)

2. **Favorites System**
   - Star button saves quotes to favorites
   - Simple favorites screen/list
   - Remove from favorites functionality

3. **Polish & Performance**
   - Optimize animations and transitions
   - Add haptic feedback
   - Performance testing and optimization
   - Final UI polish and bug fixes

**Deliverable**: Complete app with favorites and polished UX

---

### Phase 5: Testing & Release Prep
**Goal**: Add essential testing and prepare for release

#### Tasks:
1. **Basic Testing**
   - Unit tests for core quote logic
   - UI tests for critical user flows
   - Manual testing on multiple devices

2. **Release Preparation**
   - App icon and branding
   - Play Store metadata
   - Build release APK
   - Basic analytics (optional)

**Deliverable**: Release-ready app

---

## Technical Decisions

### Simplified Architecture
- **No Hilt/Dagger**: Use simple singletons and manual DI
- **No Room Database**: SharedPreferences for simple storage
- **No Complex Networking**: Basic OkHttp calls
- **Simple MVVM**: ViewModels + Composables, minimal abstraction

### Dependencies to Add Gradually
```kotlin
// Phase 2: Just Compose basics (already have)
// Phase 3: Add these
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

// Phase 4: Add if needed
implementation("androidx.datastore:datastore-preferences:1.0.0") // Instead of SharedPreferences
```

### Key Principles
1. **Working first, perfect later**: Get something running quickly
2. **Simple is better**: Avoid over-engineering
3. **Incremental complexity**: Add features one at a time
4. **Focus on UX**: The CRT interface is the main attraction

## Current Status
- ✅ Phase 1: Project setup and basic app structure complete
- 🚀 **Ready to start Phase 2**: Core UI & CRT Display

## Next Immediate Steps
1. Update minSdk to 26 in build.gradle.kts
2. Update app theme to CRT aesthetic (black + golden)
3. Create basic CRT monitor composable
4. Add hardcoded quotes with rotation functionality