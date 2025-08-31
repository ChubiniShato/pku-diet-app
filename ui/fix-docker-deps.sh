#!/bin/bash

# PKU Diet App UI - Docker Dependencies Fix Script
# This script resolves package.json and package-lock.json conflicts
# and ensures all dependencies are properly installed for Docker builds

echo "🔧 Fixing Docker dependency conflicts for PKU Diet App UI..."
echo ""

# Check if we're in the right directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: package.json not found. Please run this script from the ui/ directory."
    exit 1
fi

echo "📦 Removing old package-lock.json..."
rm -f package-lock.json

echo "📦 Installing all dependencies (including dev dependencies for build)..."
npm install

echo "🔍 Verifying TypeScript installation..."
if ! command -v tsc &> /dev/null; then
    echo "⚠️  TypeScript CLI not found globally, but should be available via npm scripts"
fi

echo "🔍 Checking Vite installation..."
if ! npm list vite &> /dev/null; then
    echo "❌ Vite not found in dependencies"
    exit 1
fi

echo ""
echo "✅ Dependencies fixed and verified!"
echo ""
echo "🚀 You can now run:"
echo "   cd .."
echo "   docker compose --profile fullstack up -d"
echo ""
echo "📋 Next steps:"
echo "   1. Monitor build: docker compose logs -f ui"
echo "   2. Check status: docker compose ps"
echo "   3. Test app: curl http://localhost/health"
echo "   4. Open app: http://localhost"
echo ""
echo "🎉 Ready to build!"
