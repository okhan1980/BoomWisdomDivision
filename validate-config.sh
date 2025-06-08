#!/bin/bash

# Configuration Validation Script
# Validates that all build configuration files are properly formatted

echo "🔍 Validating BoomWisdomDivision configuration files..."
echo "======================================================="

ERRORS=0

# Check if all required files exist
check_file_exists() {
    if [ ! -f "$1" ]; then
        echo "❌ Missing file: $1"
        ERRORS=$((ERRORS + 1))
    else
        echo "✅ Found: $1"
    fi
}

echo "📁 Checking required files..."
check_file_exists "app/build.gradle.kts"
check_file_exists "gradle/libs.versions.toml"
check_file_exists "app/proguard-rules.pro"
check_file_exists "detekt.yml"
check_file_exists "check-quality.sh"

# Validate version catalog TOML syntax
echo ""
echo "📋 Validating libs.versions.toml syntax..."
if grep -q "\[versions\]" gradle/libs.versions.toml && \
   grep -q "\[libraries\]" gradle/libs.versions.toml && \
   grep -q "\[plugins\]" gradle/libs.versions.toml; then
    echo "✅ TOML structure is valid"
else
    echo "❌ TOML structure is invalid"
    ERRORS=$((ERRORS + 1))
fi

# Check critical dependencies are present
echo ""
echo "📦 Checking critical dependencies..."
CRITICAL_DEPS=("retrofit" "hilt" "room" "moshi" "coroutines" "timber")
for dep in "${CRITICAL_DEPS[@]}"; do
    if grep -q "^$dep = " gradle/libs.versions.toml; then
        echo "✅ Found version: $dep"
    else
        echo "❌ Missing version: $dep"
        ERRORS=$((ERRORS + 1))
    fi
done

# Check build.gradle.kts has required plugins
echo ""
echo "🔌 Checking plugins in build.gradle.kts..."
REQUIRED_PLUGINS=("hilt" "ksp" "detekt")
for plugin in "${REQUIRED_PLUGINS[@]}"; do
    if grep -q "alias(libs.plugins.$plugin)" app/build.gradle.kts; then
        echo "✅ Found plugin: $plugin"
    else
        echo "❌ Missing plugin: $plugin"
        ERRORS=$((ERRORS + 1))
    fi
done

# Check minSdk change
echo ""
echo "📱 Checking minSdk configuration..."
if grep -q "minSdk = 26" app/build.gradle.kts; then
    echo "✅ minSdk correctly set to 26"
else
    echo "❌ minSdk not set to 26"
    ERRORS=$((ERRORS + 1))
fi

# Check testInstrumentationRunner change
echo ""
echo "🧪 Checking test runner configuration..."
if grep -q "com.jomar.boomwisdomdivision.HiltTestRunner" app/build.gradle.kts; then
    echo "✅ Hilt test runner configured"
else
    echo "❌ Hilt test runner not configured"
    ERRORS=$((ERRORS + 1))
fi

# Summary
echo ""
echo "📊 Validation Summary"
echo "===================="
if [ $ERRORS -eq 0 ]; then
    echo "🎉 All configuration files are valid!"
    echo "✅ Ready for Java/Android Studio build"
    exit 0
else
    echo "❌ Found $ERRORS configuration errors"
    echo "🔧 Fix these issues before building"
    exit 1
fi