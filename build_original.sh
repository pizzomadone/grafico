#!/bin/bash

echo "Compiling original version..."
mkdir -p bin
javac -d bin src/model/*.java src/layout/*.java src/view/*.java src/FlowchartEditorApp.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo "To run: java -cp bin FlowchartEditorApp"
else
    echo "✗ Compilation failed!"
    exit 1
fi
