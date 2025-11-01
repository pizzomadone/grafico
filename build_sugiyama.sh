#!/bin/bash

echo "Compiling Sugiyama-style version..."
mkdir -p bin_sugiyama
javac -d bin_sugiyama src_Sugiyama-style/model/*.java src_Sugiyama-style/layout/*.java src_Sugiyama-style/view/*.java src_Sugiyama-style/FlowchartEditorApp.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo "To run: java -cp bin_sugiyama FlowchartEditorApp"
else
    echo "✗ Compilation failed!"
    exit 1
fi
