package view;

import layout.FlowchartManager;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Interactive canvas for drawing and editing flowcharts.
 * Handles mouse events for adding blocks and editing.
 */
public class FlowchartCanvas extends JPanel {
    private FlowchartManager flowchartManager;
    private ConnectionPoint hoveredConnectionPoint;
    private FlowBlock selectedBlock;
    private BlockType selectedBlockType = BlockType.PROCESS;

    // Panning support
    private int offsetX = 0;
    private int offsetY = 0;
    private Point lastMousePos;
    private boolean isPanning = false;

    public enum BlockType {
        PROCESS("Process"),
        CONDITIONAL("Conditional"),
        IO("Input/Output"),
        LOOP("Loop"),
        START("Start"),
        END("End");

        private final String displayName;

        BlockType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public FlowchartCanvas() {
        this.flowchartManager = new FlowchartManager();

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1200, 800));

        setupMouseListeners();
    }

    public FlowchartCanvas(FlowchartManager manager) {
        this();
        this.flowchartManager = manager;
    }

    private void setupMouseListeners() {
        // Mouse motion for hover effects
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPanning && lastMousePos != null) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;
                    offsetX += dx;
                    offsetY += dy;
                    lastMousePos = e.getPoint();
                    repaint();
                }
            }
        });

        // Mouse click for adding blocks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e) ||
                    (SwingUtilities.isLeftMouseButton(e) && e.isControlDown())) {
                    isPanning = true;
                    lastMousePos = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isPanning) {
                    isPanning = false;
                    lastMousePos = null;
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        // Keyboard shortcuts
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }

    private void handleMouseMove(MouseEvent e) {
        int x = e.getX() - offsetX;
        int y = e.getY() - offsetY;

        ConnectionPoint oldHovered = hoveredConnectionPoint;
        hoveredConnectionPoint = flowchartManager.findConnectionPointAtLocation(x, y);

        if (hoveredConnectionPoint != oldHovered) {
            repaint();

            if (hoveredConnectionPoint != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void handleMouseClick(MouseEvent e) {
        if (isPanning) return;

        int x = e.getX() - offsetX;
        int y = e.getY() - offsetY;

        // Check if clicking on a connection point
        ConnectionPoint clickedPoint = flowchartManager.findConnectionPointAtLocation(x, y);

        if (clickedPoint != null) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                // Add new block
                showBlockTypeDialog(clickedPoint);
            }
        } else {
            // Check if clicking on a block
            FlowBlock clickedBlock = flowchartManager.findBlockAtPoint(x, y);

            if (clickedBlock != null) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    // Double-click to edit
                    editBlockText(clickedBlock);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Right-click for context menu
                    showBlockContextMenu(clickedBlock, e.getX(), e.getY());
                }
            }
        }

        repaint();
    }

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                if (selectedBlock != null) {
                    flowchartManager.removeBlock(selectedBlock);
                    selectedBlock = null;
                    repaint();
                }
                break;
            case KeyEvent.VK_1:
                selectedBlockType = BlockType.PROCESS;
                break;
            case KeyEvent.VK_2:
                selectedBlockType = BlockType.CONDITIONAL;
                break;
            case KeyEvent.VK_3:
                selectedBlockType = BlockType.IO;
                break;
            case KeyEvent.VK_4:
                selectedBlockType = BlockType.LOOP;
                break;
        }
    }

    private void showBlockTypeDialog(ConnectionPoint connectionPoint) {
        BlockType[] options = BlockType.values();
        BlockType selected = (BlockType) JOptionPane.showInputDialog(
            this,
            "Select block type:",
            "Add Block",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            selectedBlockType
        );

        if (selected != null) {
            String text = JOptionPane.showInputDialog(
                this,
                "Enter " + selected.getDisplayName() + " text:",
                getDefaultTextForBlockType(selected)
            );

            if (text != null && !text.trim().isEmpty()) {
                FlowBlock newBlock = createBlock(selected, text.trim());
                flowchartManager.addBlockAtConnection(connectionPoint, newBlock);
                repaint();
            }
        }
    }

    private String getDefaultTextForBlockType(BlockType type) {
        switch (type) {
            case PROCESS:
                return "Process";
            case CONDITIONAL:
                return "Condition?";
            case IO:
                return "Input/Output";
            case LOOP:
                return "Loop condition?";
            case START:
                return "Start";
            case END:
                return "End";
            default:
                return "";
        }
    }

    private FlowBlock createBlock(BlockType type, String text) {
        switch (type) {
            case PROCESS:
                return new ProcessBlock(text);
            case CONDITIONAL:
                return new ConditionalBlock(text);
            case IO:
                return new IOBlock(text);
            case LOOP:
                return new LoopBlock(text);
            case START:
                return new StartEndBlock(text, true);
            case END:
                return new StartEndBlock(text, false);
            default:
                return new ProcessBlock(text);
        }
    }

    private void editBlockText(FlowBlock block) {
        String newText = JOptionPane.showInputDialog(
            this,
            "Edit text:",
            block.getText()
        );

        if (newText != null && !newText.trim().isEmpty()) {
            block.setText(newText.trim());
            repaint();
        }
    }

    private void showBlockContextMenu(FlowBlock block, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(e -> editBlockText(block));
        menu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            flowchartManager.removeBlock(block);
            repaint();
        });
        menu.add(deleteItem);

        menu.show(this, x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Apply offset for panning
        g2d.translate(offsetX, offsetY);

        // Draw flowchart
        if (flowchartManager.getRoot() != null) {
            flowchartManager.getRoot().draw(g2d);
        }

        // Draw connection points
        List<ConnectionPoint> connectionPoints = flowchartManager.getAllConnectionPoints();
        for (ConnectionPoint point : connectionPoints) {
            boolean isHovered = point == hoveredConnectionPoint;
            point.draw(g2d, isHovered);
        }

        // Draw instructions if empty
        if (flowchartManager.getRoot() == null) {
            g2d.translate(-offsetX, -offsetY);
            drawInstructions(g2d);
        }
    }

    private void drawInstructions(Graphics2D g2d) {
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));

        String[] instructions = {
            "Welcome to Flowchart Editor!",
            "",
            "Getting Started:",
            "• Use File > New to create a new flowchart",
            "• Or File > Open to load an existing one",
            "",
            "Controls:",
            "• Click on '+' symbols to add blocks",
            "• Double-click blocks to edit text",
            "• Right-click for context menu",
            "• Ctrl+Click or Middle-click to pan",
            "",
            "Keyboard Shortcuts:",
            "• 1 = Process block",
            "• 2 = Conditional block",
            "• 3 = Input/Output block",
            "• 4 = Loop block",
            "• Delete = Remove selected block"
        };

        int y = 150;
        for (String line : instructions) {
            int x = (getWidth() - g2d.getFontMetrics().stringWidth(line)) / 2;
            g2d.drawString(line, x, y);
            y += 25;
        }
    }

    // Public methods
    public void setFlowchartManager(FlowchartManager manager) {
        this.flowchartManager = manager;
        repaint();
    }

    public FlowchartManager getFlowchartManager() {
        return flowchartManager;
    }

    public void resetView() {
        offsetX = 0;
        offsetY = 0;
        repaint();
    }

    public void zoomIn() {
        // TODO: Implement zoom functionality
    }

    public void zoomOut() {
        // TODO: Implement zoom functionality
    }
}
