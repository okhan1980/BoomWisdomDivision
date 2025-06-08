# Boom Wisdom Division - Product Definition Document

## Product Overview

**App Name:** Boom Wisdom Division  
**Platform:** Android (Portrait mode only)  
**Version:** 1.0  
**Description:** A minimalist motivational quote app that presents ephemeral wisdom through a retro-inspired CRT monitor interface, combining nostalgic design with modern functionality.

## Core Concept

The app functions like a digital "magic eight ball" for wisdom and inspiration. Users receive random quotes that appear on a vintage-style monitor display, creating a sense of discovery and serendipity. While quotes are meant to be ephemeral, users can bookmark their favorites for later reflection.

## Design Requirements

### Visual Design
- **Primary Background:** Pure black (#000000)
- **Monitor Frame:** Dark textured frame with rounded corners mimicking vintage CRT monitors
- **Display Area:** Off-white/cream background (#F5F5F0) with subtle scan line texture
- **Text Color:** Dark gray/black (#1A1A1A) for quotes
- **Accent Color:** Golden yellow (#FFD700) for the base glow and star button
- **Font:** Monospace font that matches retro computer terminals (e.g., "Courier New" or "IBM Plex Mono")

### Layout Structure
1. **Header Section**
   - Company logo (white outline of head with star burst)
   - "BOOM WISDOM DIVISION" text in white
   - Three decorative labels: "MOTIVATION", "MINDFULNESS", "CREATIVITY" (non-interactive)

2. **Monitor Display**
   - Centered retro CRT monitor with perspective tilt
   - Quote text displayed with perspective transformation to match screen angle
   - Golden manufacturer plaque at bottom (decorative element with fictional text)

3. **Interactive Base**
   - Glowing golden oval base beneath monitor
   - Black star button (primary interaction point)
   - Power cable visual elements

4. **Bottom Navigation**
   - Bookmark icon in bottom right corner

## Functional Requirements

### Quote Display System
- **Initial Load:** Display random quote of the day
- **Quote Format:** 
  - Primary quote text (multi-line, centered)
  - Attribution below quote when available (e.g., "- Eleanor Roosevelt")
- **Text Rendering:** Must appear to follow the curved perspective of the monitor screen

### User Interactions

1. **Star Button (Quote Generator)**
   - **Action:** Tap to generate new random quote
   - **Animation Sequence:**
     - Screen glows bright (white overlay fade in)
     - Current quote fades out
     - New quote fades in
     - Glow reduces back to normal
   - **Duration:** ~1.5 seconds total

2. **Bookmark Button**
   - **Action:** Save current quote to favorites
   - **Visual Feedback:** 
     - Filled bookmark icon when quote is saved
     - Outline icon when quote is not saved
   - **Persistence:** Saved quotes stored locally

3. **Favorites View**
   - **Access:** Tap on bookmark icon when it has saved quotes
   - **Display:** Scrollable list of saved quotes
   - **Features:**
     - Display quote and attribution
     - Option to remove from favorites
     - Return to main view

## Technical Specifications

### API Integration
- **Quote Source:** Public quotes API (to be researched and selected)
- **Requirements:**
  - Must provide quote text
  - Should include author/attribution
  - Should support random quote retrieval
  - Preference for APIs with categories matching our theme

**Recommended APIs to evaluate:**
- Quotable API (https://github.com/lukePeavey/quotable)
- ZenQuotes API (https://zenquotes.io/)
- Quotes.rest API

### Data Storage
- **Local Storage:** SharedPreferences or Room Database
- **Stored Data:**
  - Favorited quotes (text, author, date saved)
  - Last viewed quote (to maintain state)
  - User preferences (if any added later)

### Animation Details

1. **Quote Transition Animation**
   ```
   - Phase 1 (0-500ms): Brightness overlay fades in (0% to 60% white)
   - Phase 2 (400-700ms): Current quote fades out (100% to 0% opacity)
   - Phase 3 (700-1000ms): New quote fades in (0% to 100% opacity)
   - Phase 4 (1000-1500ms): Brightness overlay fades out (60% to 0% white)
   ```

2. **Star Button Press**
   - Scale animation: 0.95x on press
   - Glow intensifies on the golden base

3. **Bookmark Animation**
   - Smooth transition between outline and filled states
   - Small scale bounce effect (1.0x → 1.2x → 1.0x)

## Screen Specifications

### Main Screen (Quote Display)
- **Orientation:** Portrait only (locked)
- **Status Bar:** Hidden or translucent black
- **Navigation Bar:** Hidden or translucent black

### Favorites Screen
- **Header:** "Saved Wisdom" with back arrow
- **List Items:** 
  - Quote text (truncated if necessary)
  - Author attribution
  - Delete button
- **Empty State:** "No saved quotes yet. Bookmark your favorite wisdom!"

## Typography

### Quote Text
- **Font Family:** Monospace (Courier New or similar)
- **Size:** Responsive to screen size, approximately 24-32sp
- **Line Height:** 1.4x font size
- **Alignment:** Center
- **Transformation:** Perspective skew to match monitor angle

### Attribution Text
- **Font Family:** Same as quote
- **Size:** 75% of quote text size
- **Style:** Italic
- **Position:** Below quote with margin

### UI Text
- **Header Labels:** Sans-serif, all caps, letter-spacing: 2dp
- **Navigation:** Material Icons standard

## Golden Plaque Text
Example manufacturer text (decorative only):
```
WISDOMATIC 3000 © SHANGHAI
THE FUTURE-RETRO ENLIGHTENMENT TERMINAL
MODEL BWD-1984 | SERIES φ
```

## Performance Requirements
- App launch to first quote: < 2 seconds
- Quote generation: < 500ms (excluding animation)
- Smooth 60fps animations
- Offline capability for saved quotes

## Future Considerations (Post-MVP)
- Widget support for daily quote
- Share functionality
- Additional themes/color schemes
- Quote categories/filtering
- Daily notification option
- Statistics (quotes viewed, favorites, etc.)

## Development Priorities
1. Core quote display with perspective rendering
2. Star button functionality with animations
3. API integration
4. Bookmark/favorites system
5. Polish animations and transitions
6. Performance optimization

## Success Metrics
- User engagement: Average quotes viewed per session
- Retention: Daily active users
- Feature adoption: Percentage of users with saved quotes
- Performance: Crash-free rate > 99%