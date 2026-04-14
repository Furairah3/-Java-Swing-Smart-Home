#!/bin/bash
# ═══════════════════════════════════════════════════════
#  Smart Home Controller — Build & Run Script
#  No external dependencies required (uses JDK Nimbus L&F)
# ═══════════════════════════════════════════════════════

set -e

echo "╔═══════════════════════════════════════╗"
echo "║  Smart Home Controller — Build        ║"
echo "╚═══════════════════════════════════════╝"

# Clean previous build
rm -rf out/
mkdir -p out/

# Compile all Java sources
echo "▸ Compiling..."
javac -d out/ src/smarthome/*.java

echo "▸ Build successful!"
echo ""

# Run
echo "▸ Launching Smart Home Controller..."
java -cp out/ smarthome.Main
