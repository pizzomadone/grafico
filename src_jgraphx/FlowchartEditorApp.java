import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window for the Flowchart Editor using JGraphX.
 * Uses only Swing and JGraphX library (no JavaFX).
 */
public class FlowchartEditorApp extends JFrame {

    private FlowchartPanel flowchartPanel;

    public FlowchartEditorApp() {
        setTitle("Flowchart Editor - JGraphX Version");
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
        // Create flowchart panel
        flowchartPanel = new FlowchartPanel();

        // Add panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(flowchartPanel);
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

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');

        JMenuItem deleteItem = new JMenuItem("Delete Selected");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        deleteItem.addActionListener(e -> flowchartPanel.deleteSelected());
        editMenu.add(deleteItem);

        JMenuItem editLabelItem = new JMenuItem("Edit Label");
        editLabelItem.setAccelerator(KeyStroke.getKeyStroke("F2"));
        editLabelItem.addActionListener(e -> flowchartPanel.editSelectedLabel());
        editMenu.add(editLabelItem);

        menuBar.add(editMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        JMenuItem zoomInItem = new JMenuItem("Zoom In");
        zoomInItem.setAccelerator(KeyStroke.getKeyStroke("control PLUS"));
        zoomInItem.addActionListener(e -> flowchartPanel.zoomIn());
        viewMenu.add(zoomInItem);

        JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
        zoomOutItem.setAccelerator(KeyStroke.getKeyStroke("control MINUS"));
        zoomOutItem.addActionListener(e -> flowchartPanel.zoomOut());
        viewMenu.add(zoomOutItem);

        JMenuItem resetZoomItem = new JMenuItem("Reset Zoom");
        resetZoomItem.setAccelerator(KeyStroke.getKeyStroke("control 0"));
        resetZoomItem.addActionListener(e -> flowchartPanel.resetZoom());
        viewMenu.add(resetZoomItem);

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
        JButton newBtn = new JButton("New");
        newBtn.setToolTipText("Create new flowchart");
        newBtn.addActionListener(e -> newFlowchart());
        toolBar.add(newBtn);

        toolBar.addSeparator();

        // Add block buttons
        JButton addProcessBtn = new JButton("+ Process");
        addProcessBtn.setToolTipText("Add process block");
        addProcessBtn.addActionListener(e -> flowchartPanel.addBlock("PROCESS"));
        toolBar.add(addProcessBtn);

        JButton addConditionalBtn = new JButton("+ Conditional");
        addConditionalBtn.setToolTipText("Add conditional block");
        addConditionalBtn.addActionListener(e -> flowchartPanel.addBlock("CONDITIONAL"));
        toolBar.add(addConditionalBtn);

        JButton addIOBtn = new JButton("+ I/O");
        addIOBtn.setToolTipText("Add input/output block");
        addIOBtn.addActionListener(e -> flowchartPanel.addBlock("IO"));
        toolBar.add(addIOBtn);

        JButton addLoopBtn = new JButton("+ Loop");
        addLoopBtn.setToolTipText("Add loop block");
        addLoopBtn.addActionListener(e -> flowchartPanel.addBlock("LOOP"));
        toolBar.add(addLoopBtn);

        toolBar.addSeparator();

        // Info label
        JLabel infoLabel = new JLabel(" Click blocks to select | Double-click to edit | Right-click for menu ");
        toolBar.add(infoLabel);

        add(toolBar, BorderLayout.NORTH);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JLabel statusLabel = new JLabel("Ready - JGraphX Version (Swing only)");
        statusBar.add(statusLabel, BorderLayout.WEST);

        return statusBar;
    }

    private void newFlowchart() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Create new flowchart? This will clear the current diagram.",
            "New Flowchart",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.OK_OPTION) {
            flowchartPanel.clearFlowchart();
        }
    }

    private void loadExample(int exampleNumber) {
        switch (exampleNumber) {
            case 1:
                flowchartPanel.createSimpleConditionalExample();
                break;
            case 2:
                flowchartPanel.createLoopExample();
                break;
            case 3:
                flowchartPanel.createNestedConditionalExample();
                break;
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
            this,
            "Flowchart Editor v1.0 - JGraphX Version\n\n" +
            "A flowchart editor built with JGraphX and Swing.\n\n" +
            "Features:\n" +
            "• Multiple block types (Process, Conditional, I/O, Loop)\n" +
            "• Hierarchical automatic layout\n" +
            "• Interactive editing\n" +
            "• Zoom and pan support\n\n" +
            "Built with Java Swing and JGraphX library!",
            "About Flowchart Editor",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showHelpDialog() {
        String help = "How to Use Flowchart Editor\n\n" +
                "Creating Flowcharts:\n" +
                "1. Use toolbar buttons to add blocks\n" +
                "2. Select a block type from the dialog\n" +
                "3. Enter the block text\n\n" +
                "Editing:\n" +
                "• Double-click a block to edit its text (or press F2)\n" +
                "• Click to select a block\n" +
                "• Press Delete to remove selected block\n" +
                "• Right-click for context menu\n\n" +
                "Navigation:\n" +
                "• Mouse wheel to zoom\n" +
                "• Ctrl+Click and drag to pan\n" +
                "• Ctrl+Plus/Minus to zoom in/out\n\n" +
                "Keyboard Shortcuts:\n" +
                "• Ctrl+N: New flowchart\n" +
                "• Delete: Delete selected block\n" +
                "• F2: Edit label\n" +
                "• Ctrl+0: Reset zoom\n" +
                "• F1: Show this help\n\n" +
                "Block Types:\n" +
                "• Process: Rectangular (actions/processes)\n" +
                "• Conditional: Diamond (if/else decisions)\n" +
                "• I/O: Parallelogram (input/output)\n" +
                "• Loop: Hexagon (while/for loops)\n" +
                "• Start/End: Rounded rectangle\n\n" +
                "Tips:\n" +
                "• Use Examples menu to see sample flowcharts\n" +
                "• JGraphX automatically arranges blocks hierarchically\n" +
                "• You can manually move blocks by dragging them";

        JTextArea textArea = new JTextArea(help);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(550, 500));

        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Help",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void exitApplication() {
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlowchartEditorApp app = new FlowchartEditorApp();
            app.setVisible(true);
        });
    }
}
