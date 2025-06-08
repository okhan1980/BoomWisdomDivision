#!/bin/bash
# Phase rotation script for BoomWisdomDivision

CURRENT_PHASE=$1
NEXT_PHASE=$2

if [ -z "$CURRENT_PHASE" ] || [ -z "$NEXT_PHASE" ]; then
    echo "Usage: ./phase-rotate.sh <current_phase_number> <next_phase_number>"
    echo "Example: ./phase-rotate.sh 1 2"
    exit 1
fi

echo "🔄 Rotating from Phase $CURRENT_PHASE to Phase $NEXT_PHASE..."

# Archive current phase if not pre-development
if [ "$CURRENT_PHASE" != "0" ]; then
    mkdir -p "archive/phase-${CURRENT_PHASE}-completed"
    
    if [ -f "PHASE_${CURRENT_PHASE}_DETAILED_PLAN.md" ]; then
        mv "PHASE_${CURRENT_PHASE}_DETAILED_PLAN.md" "archive/phase-${CURRENT_PHASE}-completed/"
        echo "📁 Archived Phase $CURRENT_PHASE detailed plan"
    fi
    
    if [ -f "CURRENT_PHASE_PLAN.md" ]; then
        cp "CURRENT_PHASE_PLAN.md" "archive/phase-${CURRENT_PHASE}-completed/PHASE_${CURRENT_PHASE}_DETAILED_PLAN.md"
    fi

    # Create summary template
    cat > "archive/phase-${CURRENT_PHASE}-completed/PHASE_${CURRENT_PHASE}_SUMMARY.md" << EOF
# Phase $CURRENT_PHASE Completion Summary

## Completed Date: $(date +%Y-%m-%d)

## Delivered Features:
- 

## Technical Achievements:
- 

## Challenges Resolved:
- 

## Test Coverage:
- Unit Tests: 
- Integration Tests: 
- UI Tests: 

## Performance Metrics:
- Build Time: 
- App Launch Time: 
- Memory Usage: 

## Key Learnings:
- 

## Files Changed:
- 

## Next Phase Recommendations:
- 
EOF
    echo "📝 Created summary template for Phase $CURRENT_PHASE"
fi

# Update current phase plan
if [ -f "PHASE_${NEXT_PHASE}_DETAILED_PLAN.md" ]; then
    cp "PHASE_${NEXT_PHASE}_DETAILED_PLAN.md" "CURRENT_PHASE_PLAN.md"
    echo "✅ Updated CURRENT_PHASE_PLAN.md to Phase $NEXT_PHASE"
    
    # Remove the source file to avoid confusion
    rm "PHASE_${NEXT_PHASE}_DETAILED_PLAN.md"
    echo "🗑️  Removed PHASE_${NEXT_PHASE}_DETAILED_PLAN.md (now CURRENT_PHASE_PLAN.md)"
else
    echo "⚠️  Warning: PHASE_${NEXT_PHASE}_DETAILED_PLAN.md not found"
    echo "Please create the next phase plan before rotation"
fi

# Update CLAUDE.md to reference CURRENT_PHASE_PLAN.md
if [ -f "CLAUDE.md" ]; then
    # Replace any phase-specific plan reference with CURRENT_PHASE_PLAN.md
    sed -i.bak 's/PHASE_[0-9]_DETAILED_PLAN\.md/CURRENT_PHASE_PLAN.md/g' CLAUDE.md
    rm CLAUDE.md.bak
    echo "📄 Updated CLAUDE.md references"
fi

# Update PROGRESS.md
if [ "$CURRENT_PHASE" != "0" ]; then
    echo "" >> PROGRESS.md
    echo "---" >> PROGRESS.md
    echo "" >> PROGRESS.md
    echo "### Phase $CURRENT_PHASE Completed - $(date +%Y-%m-%d)" >> PROGRESS.md
    echo "**Status**: ✅ Complete" >> PROGRESS.md
    echo "**Archived to**: \`archive/phase-${CURRENT_PHASE}-completed/\`" >> PROGRESS.md
    echo "" >> PROGRESS.md
    echo "### Phase $NEXT_PHASE Started - $(date +%Y-%m-%d)" >> PROGRESS.md
    echo "**Status**: 🔄 In Progress" >> PROGRESS.md
    echo "**Plan**: See \`CURRENT_PHASE_PLAN.md\`" >> PROGRESS.md
    echo "📊 Updated PROGRESS.md"
fi

echo ""
echo "✅ Phase rotation complete!"
echo ""
echo "Next steps:"
echo "1. Review CURRENT_PHASE_PLAN.md for Phase $NEXT_PHASE"
echo "2. If rotating from a completed phase, fill in the summary at:"
echo "   archive/phase-${CURRENT_PHASE}-completed/PHASE_${CURRENT_PHASE}_SUMMARY.md"
echo "3. Commit all changes with:"
echo "   git add -A"
echo "   git commit -m \"chore: Rotate to Phase $NEXT_PHASE, archive Phase $CURRENT_PHASE\""