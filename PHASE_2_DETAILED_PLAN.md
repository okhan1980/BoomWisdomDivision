# Phase 2: UI Implementation - Main Screen - Detailed Plan

> **Note**: This is a template. Phase 2 plan will be updated based on learnings from Phase 1.

## Overview
Phase 2 focuses on building the main screen UI with the retro CRT monitor design, implementing the visual foundation that will showcase quotes.

## Prerequisites from Phase 1
- [ ] Core architecture established
- [ ] API integration working
- [ ] Room database functional
- [ ] All dependencies configured
- [ ] Quality checks passing

## Phase 2 Goals
Build the visual interface that matches the design mockup with proper animations and responsive behavior.

## Detailed Task Breakdown

### Task Group 1: CRT Monitor Component (Agent 1)
```kotlin
// Components to create:
- CRTMonitorFrame.kt
- PerspectiveText.kt
- ScanLineOverlay.kt
- GoldenPlaque.kt
```

**Tasks:**
1. **Monitor Frame**
   - Rounded rectangle with perspective tilt
   - Dark textured frame material
   - Proper shadows and depth
   - Responsive sizing for different screens

2. **Text Perspective System**
   - Transform quote text to match screen angle
   - Maintain readability
   - Handle line breaks properly
   - Dynamic font sizing

3. **Visual Effects**
   - Scan line animation overlay
   - Screen glow effects
   - CRT curvature simulation
   - Brightness transitions

### Task Group 2: Screen Layout & Navigation (Agent 2)
```kotlin
// Screens to create:
- MainScreen.kt
- QuoteDisplayScreen.kt
- Navigation setup
```

**Tasks:**
1. **Header Section**
   - Logo with star burst icon
   - "BOOM WISDOM DIVISION" text
   - Three category labels (non-interactive)

2. **Main Layout**
   - Portrait orientation lock
   - Edge-to-edge display
   - Proper safe area handling
   - Responsive layout for different screen sizes

3. **Interactive Elements**
   - Golden oval base with glow
   - Star button with press states
   - Bookmark button in corner
   - Power cable visual elements

### Task Group 3: Theme & Styling (Agent 3)
```kotlin
// Theme files to update:
- Color.kt
- Typography.kt
- Theme.kt
- Custom composables
```

**Tasks:**
1. **Color System**
   - Pure black background (#000000)
   - Off-white display (#F5F5F0)
   - Golden yellow accent (#FFD700)
   - Text colors with proper contrast

2. **Typography**
   - Monospace font configuration
   - Responsive text sizing
   - Line height optimization
   - Weight variations

3. **Component Library**
   - Reusable styled buttons
   - Text display components
   - Animation containers
   - Layout helpers

## Technical Requirements

### Performance Targets
- Smooth 60fps rendering
- Layout inflation < 16ms
- Memory usage < 50MB for UI
- No ANRs during transitions

### Responsive Design
- Support 5" to 7" screens
- Handle landscape briefly (rotation lock)
- Tablet compatibility (though portrait-only)
- Different density support

### Accessibility
- Content descriptions for all interactive elements
- Proper focus handling
- High contrast support
- Minimum touch targets (48dp)

## Implementation Order

### Day 1: Foundation
1. Set up theme system with brand colors
2. Create basic screen structure
3. Implement orientation lock
4. Set up navigation framework

### Day 2: CRT Monitor
1. Build monitor frame composable
2. Implement perspective transformation
3. Add visual effects (scan lines)
4. Test on different screen sizes

### Day 3: Integration & Polish
1. Integrate with ViewModel
2. Add placeholder quote display
3. Implement responsive behavior
4. Performance optimization

## Testing Requirements

### Visual Tests
- Screenshot tests for main screen
- Different screen sizes
- Light/dark theme support
- Accessibility scanner

### Interaction Tests
- Button press animations
- Navigation flows
- Orientation handling
- Memory leak tests

## Definition of Done

- [ ] Main screen matches design mockup
- [ ] CRT monitor effect working smoothly
- [ ] Text perspective rendering correct
- [ ] All animations at 60fps
- [ ] Responsive on all target devices
- [ ] Accessibility requirements met
- [ ] Screenshot tests passing
- [ ] No memory leaks detected
- [ ] Integration with Phase 1 architecture complete

## Dependencies on Phase 1

This phase requires:
- ViewModel structure from Phase 1
- Quote domain models
- Theme foundation
- Navigation setup
- Hilt dependency injection working

## Risk Mitigation

### Complex Visual Effects
- Start with simpler effects, enhance gradually
- Have fallback rendering for performance
- Test on low-end devices early

### Text Perspective
- Implement as custom Canvas drawing if needed
- Ensure text remains readable
- Handle different text lengths gracefully

### Performance
- Profile early and often
- Use Compose performance tools
- Optimize recomposition

## Files to Create/Modify

### New Files
```
presentation/ui/screen/MainScreen.kt
presentation/ui/component/CRTMonitor.kt
presentation/ui/component/PerspectiveText.kt
presentation/ui/component/ScanLines.kt
presentation/ui/component/StarButton.kt
presentation/ui/component/BookmarkButton.kt
presentation/ui/theme/BoomWisdomTheme.kt
```

### Modified Files
```
presentation/ui/theme/Color.kt
presentation/ui/theme/Type.kt
presentation/MainActivity.kt
app/src/main/AndroidManifest.xml (orientation lock)
```

---

**Note**: This plan will be refined after Phase 1 completion to incorporate any architectural changes or discoveries.