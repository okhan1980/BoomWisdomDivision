# Phase Rotation Protocol

## Overview
This document defines the process for rotating phase documentation to maintain focus on the current phase while preserving historical records.

## File Structure

### Active Files (Always Present)
1. **CURRENT_PHASE_PLAN.md** - Detailed plan for the phase in progress
2. **CLAUDE.md** - Always references CURRENT_PHASE_PLAN.md
3. **PROGRESS.md** - Cumulative progress tracking
4. **PROJECT_INTEGRITY_RULES.md** - Permanent quality guidelines
5. **DEVELOPMENT_PLAN.md** - Overall roadmap (permanent)

### Archive Structure
```
archive/
├── phase-1-completed/
│   ├── PHASE_1_DETAILED_PLAN.md
│   ├── PHASE_1_SUMMARY.md
│   └── PHASE_1_METRICS.md
├── phase-2-completed/
│   ├── PHASE_2_DETAILED_PLAN.md
│   ├── PHASE_2_SUMMARY.md
│   └── PHASE_2_METRICS.md
└── ...
```

## Phase Transition Process

### 1. Phase Completion Steps
```bash
# Create archive directory for completed phase
mkdir -p archive/phase-X-completed

# Move phase-specific plan to archive
mv PHASE_X_DETAILED_PLAN.md archive/phase-X-completed/

# Create phase summary
echo "# Phase X Completion Summary" > archive/phase-X-completed/PHASE_X_SUMMARY.md

# Document metrics
echo "# Phase X Metrics" > archive/phase-X-completed/PHASE_X_METRICS.md
```

### 2. Next Phase Preparation
```bash
# Remove old current phase plan
rm -f CURRENT_PHASE_PLAN.md

# Create new current phase plan
cp PHASE_$(X+1)_DETAILED_PLAN.md CURRENT_PHASE_PLAN.md

# Update CLAUDE.md reference (automated by script)
```

### 3. CLAUDE.md Update Pattern
The CLAUDE.md file should always reference the current phase plan as:
```markdown
## Critical Project Rules

**IMPORTANT**: Before any development, review:
1. `PROJECT_INTEGRITY_RULES.md` - Comprehensive rules for maintaining code quality
2. `CURRENT_PHASE_PLAN.md` - Specific implementation details for the active phase
3. `DEVELOPMENT_PLAN.md` - Overall project phases and timeline
```

## Automation Script

### phase-rotate.sh
```bash
#!/bin/bash
# Phase rotation script

CURRENT_PHASE=$1
NEXT_PHASE=$2

if [ -z "$CURRENT_PHASE" ] || [ -z "$NEXT_PHASE" ]; then
    echo "Usage: ./phase-rotate.sh <current_phase_number> <next_phase_number>"
    exit 1
fi

echo "Rotating from Phase $CURRENT_PHASE to Phase $NEXT_PHASE..."

# Archive current phase
mkdir -p "archive/phase-${CURRENT_PHASE}-completed"
mv "PHASE_${CURRENT_PHASE}_DETAILED_PLAN.md" "archive/phase-${CURRENT_PHASE}-completed/" 2>/dev/null

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
- 

## Performance Metrics:
- 
EOF

# Update current phase plan
if [ -f "PHASE_${NEXT_PHASE}_DETAILED_PLAN.md" ]; then
    cp "PHASE_${NEXT_PHASE}_DETAILED_PLAN.md" "CURRENT_PHASE_PLAN.md"
    echo "✅ Updated CURRENT_PHASE_PLAN.md to Phase $NEXT_PHASE"
else
    echo "⚠️  Warning: PHASE_${NEXT_PHASE}_DETAILED_PLAN.md not found"
fi

# Update CLAUDE.md reference
sed -i 's/PHASE_[0-9]_DETAILED_PLAN\.md/CURRENT_PHASE_PLAN.md/g' CLAUDE.md

# Update PROGRESS.md
echo "" >> PROGRESS.md
echo "### Phase $CURRENT_PHASE Completed - $(date +%Y-%m-%d)" >> PROGRESS.md
echo "Archived to: archive/phase-${CURRENT_PHASE}-completed/" >> PROGRESS.md

echo "✅ Phase rotation complete!"
```

## Implementation Timeline

### Before Starting Phase 1:
1. Rename `PHASE_1_DETAILED_PLAN.md` to `CURRENT_PHASE_PLAN.md`
2. Update CLAUDE.md to reference `CURRENT_PHASE_PLAN.md`
3. Create `phase-rotate.sh` script

### After Completing Each Phase:
1. Run: `./phase-rotate.sh <completed_phase> <next_phase>`
2. Fill in the completion summary
3. Commit the archive
4. Create PR with archived phase

### At Project End:
1. Archive final phase
2. Create `PROJECT_RETROSPECTIVE.md`
3. Archive all phase plans

## Benefits

1. **Focus**: Only current phase details are active
2. **History**: Complete record in archive
3. **Clarity**: CLAUDE.md always points to relevant plan
4. **Consistency**: Same filename for current work
5. **Automation**: Script handles rotation

## Example Usage

After completing Phase 1:
```bash
# Run rotation script
./phase-rotate.sh 1 2

# Check results
ls CURRENT_PHASE_PLAN.md  # Should show Phase 2 content
ls archive/phase-1-completed/  # Should contain Phase 1 files

# Commit changes
git add -A
git commit -m "chore: Rotate to Phase 2, archive Phase 1 completion"
```

## Phase Plan Creation Schedule

- **Phase 2 Plan**: Create at end of Phase 1
- **Phase 3 Plan**: Create at end of Phase 2
- **Phase 4 Plan**: Create at end of Phase 3
- **Phase 5 Plan**: Create at end of Phase 4

This ensures plans incorporate learnings from previous phases.