#!/bin/bash

# BoomWisdomDivision Quality Check Script
# Run this script before any commit or PR to ensure code quality

set -e  # Exit on any error

echo "🚀 Running quality checks for BoomWisdomDivision..."
echo "=================================================="

# Function to print section headers
print_section() {
    echo ""
    echo "🔄 $1"
    echo "----------------------------------------"
}

# 1. Clean and build check
print_section "Clean and Build Check"
./gradlew clean build || {
    echo "❌ Build failed!"
    exit 1
}
echo "✅ Build passed!"

# 2. Lint check
print_section "Android Lint Check"
./gradlew lint || {
    echo "❌ Lint check failed!"
    echo "📄 Check app/build/reports/lint-results.html for details"
    exit 1
}
echo "✅ Lint check passed!"

# 3. Unit tests
print_section "Unit Tests"
./gradlew test || {
    echo "❌ Unit tests failed!"
    echo "📄 Check app/build/reports/tests/testDebugUnitTest/index.html for details"
    exit 1
}
echo "✅ Unit tests passed!"

# 4. Detekt (static analysis)
print_section "Detekt Static Analysis"
./gradlew detekt || {
    echo "❌ Detekt check failed!"
    echo "📄 Check app/build/reports/detekt/detekt.html for details"
    exit 1
}
echo "✅ Detekt check passed!"

# 5. Dependency updates check
print_section "Dependency Updates Check"
if ./gradlew dependencyUpdates &>/dev/null; then
    echo "✅ Dependency updates check completed"
    echo "📄 Check build/dependencyUpdates/report.txt for outdated dependencies"
else
    echo "⚠️  Dependency updates plugin not available"
fi

# 6. APK size check (debug build)
print_section "APK Size Check"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(stat -c%s "$APK_PATH")
    APK_SIZE_MB=$((APK_SIZE / 1024 / 1024))
    echo "📦 Debug APK size: ${APK_SIZE_MB}MB"
    
    if [ $APK_SIZE_MB -gt 50 ]; then
        echo "⚠️  APK size is over 50MB - consider optimization"
    else
        echo "✅ APK size is reasonable"
    fi
else
    echo "⚠️  APK not found, skipping size check"
fi

echo ""
echo "🎉 All quality checks passed!"
echo "=================================================="
echo "✅ Build: PASSED"
echo "✅ Lint: PASSED"
echo "✅ Tests: PASSED"
echo "✅ Detekt: PASSED"
echo ""
echo "Ready for commit/PR! 🚀"