# Pull Request Workflow - MANDATORY

## ⚠️ IMPORTANT: Direct commits to main are PROHIBITED

All changes must go through pull request review before merging to main branch.

## Workflow Steps

### 1. Create Feature Branch
```bash
# For new features/phases
git checkout -b feature/phase-X-description

# For bug fixes
git checkout -b fix/issue-description

# For documentation updates
git checkout -b docs/update-description

# For chore/maintenance
git checkout -b chore/task-description
```

### 2. Make Changes
- Implement changes on feature branch
- Commit with conventional commit messages
- Push feature branch to origin

### 3. Create Pull Request
```bash
# Push branch
git push -u origin feature/branch-name

# Create PR using GitHub CLI
gh pr create --title "Type: Brief description" \
  --body "Detailed description of changes" \
  --base main
```

### 4. PR Requirements
Before creating PR, ensure:
- [ ] All tests pass
- [ ] Code follows project standards
- [ ] Documentation is updated
- [ ] No direct commits to main
- [ ] Conventional commit messages used
- [ ] Quality checks pass

### 5. Review Process
1. Create PR and request review
2. Wait for approval before merging
3. Address any review comments
4. Only merge after explicit approval

## Branch Naming Convention

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/description` | `feature/phase-2-ui` |
| Fix | `fix/description` | `fix/action-deprecation` |
| Docs | `docs/description` | `docs/update-readme` |
| Chore | `chore/description` | `chore/update-deps` |

## Commit Message Format

```
type(scope): subject

body (optional)

footer (optional)
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test additions/changes
- `chore`: Maintenance tasks

## PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Refactoring
- [ ] Other (specify)

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes
```

## Example Workflow

```bash
# 1. Create feature branch
git checkout -b feature/phase-2-ui

# 2. Make changes
# ... implement features ...

# 3. Commit changes
git add .
git commit -m "feat: implement CRT monitor component"

# 4. Push branch
git push -u origin feature/phase-2-ui

# 5. Create PR
gh pr create --title "feat: Phase 2 UI Implementation" \
  --body "$(cat <<EOF
## Description
Implements Phase 2 UI components including CRT monitor display

## Type of Change
- [x] New feature

## Testing
- [x] Unit tests pass
- [x] Manual testing completed

## Checklist
- [x] Code follows project style guidelines
- [x] Self-review completed
- [x] Documentation updated
EOF
)"

# 6. Wait for review and approval
# 7. Merge only after approval
```

## Phase Development Workflow

For each phase:
1. Create branch: `git checkout -b feature/phase-X-implementation`
2. Implement all phase requirements
3. Run quality checks: `./check-quality.sh`
4. Push branch and create PR
5. Wait for review and approval
6. After merge, run phase rotation: `./phase-rotate.sh X X+1`

## Important Notes

- **NEVER** push directly to main
- **ALWAYS** create a PR for review
- **WAIT** for explicit approval before merging
- **USE** conventional commits for clear history
- **TEST** thoroughly before creating PR

This workflow ensures code quality, enables review feedback, and maintains a clean git history.