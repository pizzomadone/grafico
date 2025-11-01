package com.flowchart;

import com.flowchart.layout.FlowchartManager;
import com.flowchart.serialization.FlowchartSerializer;
import com.flowchart.view.FlowchartCanvas;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Main application window for the Flowchart Editor.
 * Provides menu bar, toolbar, and canvas for creating flowcharts.
 */
public class FlowchartEditorApp extends JFrame {

    private FlowchartCanvas canvas;
    private File currentFile;
    private boolean modified = false;

    public FlowchartEditorApp() {
        setTitle("Flowchart Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        initializeComponents();
        setupMenuBar();
        setupToolbar();

        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        // Create canvas
        canvas = new FlowchartCanvas();

        // Add canvas in scroll pane
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        newItem.addActionListener(e -> newFlowchart());
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        openItem.addActionListener(e -> openFlowchart());
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveItem.addActionListener(e -> saveFlowchart());
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
        saveAsItem.addActionListener(e -> saveFlowchartAs());
        fileMenu.add(saveAsItem);

        fileMenu.addSeparator();

        JMenuItem exportItem = new JMenuItem("Export to PNG...");
        exportItem.addActionListener(e -> exportToPNG());
        fileMenu.add(exportItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // Examples menu
        JMenu examplesMenu = new JMenu("Examples");
        examplesMenu.setMnemonic('E');

        JMenuItem simpleExample = new JMenuItem("Simple Conditional");
        simpleExample.addActionListener(e -> loadExample(1));
        examplesMenu.add(simpleExample);

        JMenuItem loopExample = new JMenuItem("Loop Example");
        loopExample.addActionListener(e -> loadExample(2));
        examplesMenu.add(loopExample);

        JMenuItem nestedExample = new JMenuItem("Nested Conditional");
        nestedExample.addActionListener(e -> loadExample(3));
        examplesMenu.add(nestedExample);

        menuBar.add(examplesMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        JMenuItem resetViewItem = new JMenuItem("Reset View");
        resetViewItem.setAccelerator(KeyStroke.getKeyStroke("control 0"));
        resetViewItem.addActionListener(e -> canvas.resetView());
        viewMenu.add(resetViewItem);

        JMenuItem zoomInItem = new JMenuItem("Zoom In");
        zoomInItem.setAccelerator(KeyStroke.getKeyStroke("control PLUS"));
        zoomInItem.addActionListener(e -> canvas.zoomIn());
        viewMenu.add(zoomInItem);

        JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
        zoomOutItem.setAccelerator(KeyStroke.getKeyStroke("control MINUS"));
        zoomOutItem.addActionListener(e -> canvas.zoomOut());
        viewMenu.add(zoomOutItem);

        menuBar.add(viewMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        JMenuItem helpItem = new JMenuItem("How to Use");
        helpItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        helpItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(helpItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void setupToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // New button
        JButton newBtn = new JButton(new ImageIcon());
        newBtn.setText("New");
        newBtn.setToolTipText("Create new flowchart");
        newBtn.addActionListener(e -> newFlowchart());
        toolBar.add(newBtn);

        // Open button
        JButton openBtn = new JButton("Open");
        openBtn.setToolTipText("Open flowchart");
        openBtn.addActionListener(e -> openFlowchart());
        toolBar.add(openBtn);

        // Save button
        JButton saveBtn = new JButton("Save");
        saveBtn.setToolTipText("Save flowchart");
        saveBtn.addActionListener(e -> saveFlowchart());
        toolBar.add(saveBtn);

        toolBar.addSeparator();

        // Block type selector
        toolBar.add(new JLabel(" Add: "));
        String[] blockTypes = {"Process", "Conditional", "I/O", "Loop", "Start", "End"};
        JComboBox<String> blockTypeCombo = new JComboBox<>(blockTypes);
        blockTypeCombo.setMaximumSize(new Dimension(150, 30));
        blockTypeCombo.setToolTipText("Select block type to add");
        toolBar.add(blockTypeCombo);

        toolBar.addSeparator();

        // Info label
        JLabel infoLabel = new JLabel(" Click on '+' symbols to add blocks | Double-click to edit | Right-click for menu ");
        toolBar.add(infoLabel);

        add(toolBar, BorderLayout.NORTH);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel, BorderLayout.WEST);

        return statusBar;
    }

    private void newFlowchart() {
        if (confirmDiscardChanges()) {
            int choice = JOptionPane.showOptionDialog(
                this,
                "Create new flowchart:",
                "New Flowchart",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Empty", "With Start Block", "Cancel"},
                "With Start Block"
            );

            if (choice == 0) {
                // Empty flowchart
                canvas.setFlowchartManager(new FlowchartManager());
            } else if (choice == 1) {
                // With start block
                FlowchartManager manager = new FlowchartManager();
                com.flowchart.model.StartEndBlock start = new com.flowchart.model.StartEndBlock("Start", true);
                manager.setRoot(start);
                manager.recalculateLayout();
                canvas.setFlowchartManager(manager);
            }

            currentFile = null;
            modified = false;
            updateTitle();
        }
    }

    private void openFlowchart() {
        if (confirmDiscardChanges()) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Flowchart Files (*.flowchart)", "flowchart"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    FlowchartManager manager = FlowchartSerializer.loadFromFile(file);
                    canvas.setFlowchartManager(manager);
                    currentFile = file;
                    modified = false;
                    updateTitle();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Error loading file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    private void saveFlowchart() {
        if (currentFile == null) {
            saveFlowchartAs();
        } else {
            try {
                FlowchartSerializer.saveToFile(canvas.getFlowchartManager(), currentFile);
                modified = false;
                updateTitle();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error saving file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void saveFlowchartAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Flowchart Files (*.flowchart)", "flowchart"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Add extension if not present
            if (!file.getName().endsWith(".flowchart")) {
                file = new File(file.getAbsolutePath() + ".flowchart");
            }

            try {
                FlowchartSerializer.saveToFile(canvas.getFlowchartManager(), file);
                currentFile = file;
                modified = false;
                updateTitle();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error saving file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void exportToPNG() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images (*.png)", "png"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }

            try {
                // TODO: Implement PNG export
                JOptionPane.showMessageDialog(
                    this,
                    "PNG export is not yet implemented",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error exporting: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void loadExample(int exampleNumber) {
        if (confirmDiscardChanges()) {
            FlowchartManager manager;

            switch (exampleNumber) {
                case 1:
                    manager = FlowchartManager.createDefaultFlowchart();
                    break;
                case 2:
                    manager = FlowchartManager.createLoopFlowchart();
                    break;
                case 3:
                    manager = FlowchartManager.createNestedFlowchart();
                    break;
                default:
                    return;
            }

            canvas.setFlowchartManager(manager);
            currentFile = null;
            modified = false;
            updateTitle();
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
            this,
            "Flowchart Editor v1.0\n\n" +
            "A dynamic flowchart editor with automatic branch balancing.\n\n" +
            "Features:\n" +
            "• Multiple block types (Process, Conditional, I/O, Loop)\n" +
            "• Automatic layout and branch balancing\n" +
            "• Save/Load flowcharts\n" +
            "• Interactive editing\n\n" +
            "Built with Java Swing",
            "About Flowchart Editor",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showHelpDialog() {
        String help = "How to Use Flowchart Editor\n\n" +
                "Creating Flowcharts:\n" +
                "1. Click on '+' symbols to add new blocks\n" +
                "2. Select the block type from the dialog\n" +
                "3. Enter the block text\n\n" +
                "Editing:\n" +
                "• Double-click a block to edit its text\n" +
                "• Right-click for context menu (Edit/Delete)\n" +
                "• Press Delete key to remove selected block\n\n" +
                "Navigation:\n" +
                "• Ctrl+Click or Middle-click to pan the canvas\n" +
                "• Use scroll bars to navigate\n\n" +
                "Keyboard Shortcuts:\n" +
                "• Ctrl+N: New flowchart\n" +
                "• Ctrl+O: Open\n" +
                "• Ctrl+S: Save\n" +
                "• Ctrl+0: Reset view\n" +
                "• F1: Show this help\n\n" +
                "Block Types:\n" +
                "• Process: Rectangular (actions/processes)\n" +
                "• Conditional: Diamond (if/else decisions)\n" +
                "• I/O: Parallelogram (input/output)\n" +
                "• Loop: Hexagon (while/for loops)\n" +
                "• Start/End: Rounded rectangle\n\n" +
                "Branch Balancing:\n" +
                "The editor automatically balances conditional branches.\n" +
                "When you add blocks to one branch, the other branch\n" +
                "extends automatically to maintain symmetry.";

        JTextArea textArea = new JTextArea(help);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Help",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private boolean confirmDiscardChanges() {
        if (modified) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Do you want to continue?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            return result == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private void updateTitle() {
        String title = "Flowchart Editor";
        if (currentFile != null) {
            title += " - " + currentFile.getName();
        }
        if (modified) {
            title += " *";
        }
        setTitle(title);
    }

    private void exitApplication() {
        if (confirmDiscardChanges()) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlowchartEditorApp app = new FlowchartEditorApp();
            app.setVisible(true);
        });
    }
}
