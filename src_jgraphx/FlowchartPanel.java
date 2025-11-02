import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel that contains the JGraphX flowchart component.
 * NEW INTERACTION MODEL:
 * - Starts with Start -> End
 * - Click on EDGES to insert blocks
 * - IF blocks automatically create merge structure
 */
public class FlowchartPanel extends JPanel {

    private mxGraph graph;
    private mxGraphComponent graphComponent;

    // Block type constants
    public static final String PROCESS = "PROCESS";
    public static final String CONDITIONAL = "CONDITIONAL";
    public static final String IO = "IO";
    public static final String LOOP = "LOOP";
    public static final String START = "START";
    public static final String END = "END";
    public static final String MERGE = "MERGE";  // Merge point for conditionals

    // Track merge points for conditionals
    private Map<Object, Object> conditionalMergePoints = new HashMap<>();

    public FlowchartPanel() {
        setLayout(new BorderLayout());

        // Create graph
        graph = new mxGraph() {
            @Override
            public boolean isCellEditable(Object cell) {
                // Only allow editing text of vertices (not edges)
                return cell instanceof mxCell && ((mxCell) cell).isVertex() &&
                       !MERGE.equals(((mxCell) cell).getStyle());
            }
        };

        graph.setAllowDanglingEdges(false);
        graph.setCellsEditable(true);
        graph.setConnectableEdges(false);
        graph.setCellsDisconnectable(false);
        graph.setCellsMovable(false);  // FIXED: Blocks are now non-movable

        // Setup custom styles for flowchart blocks
        setupStyles();

        // Create graph component
        graphComponent = new mxGraphComponent(graph);
        graphComponent.setConnectable(false);
        graphComponent.getViewport().setOpaque(true);
        graphComponent.getViewport().setBackground(Color.WHITE);

        // Enable grid
        graphComponent.setGridVisible(true);
        graphComponent.setGridStyle(mxGraphComponent.GRID_STYLE_DOT);

        // Setup mouse listeners for edge clicking
        setupMouseListeners();

        add(graphComponent, BorderLayout.CENTER);

        // Initialize with Start -> End
        initializeStartEnd();
    }

    private void setupStyles() {
        mxStylesheet stylesheet = graph.getStylesheet();

        // Process block style (rectangle, blue)
        Map<String, Object> processStyle = new HashMap<>();
        processStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        processStyle.put(mxConstants.STYLE_FILLCOLOR, "#C8DCFF");
        processStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        processStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        processStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        processStyle.put(mxConstants.STYLE_FONTSIZE, 12);
        processStyle.put(mxConstants.STYLE_ROUNDED, false);
        stylesheet.putCellStyle(PROCESS, processStyle);

        // Conditional block style (diamond, yellow)
        Map<String, Object> conditionalStyle = new HashMap<>();
        conditionalStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
        conditionalStyle.put(mxConstants.STYLE_FILLCOLOR, "#FFFFC8");
        conditionalStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        conditionalStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        conditionalStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        conditionalStyle.put(mxConstants.STYLE_FONTSIZE, 12);
        stylesheet.putCellStyle(CONDITIONAL, conditionalStyle);

        // I/O block style (cylinder, green)
        Map<String, Object> ioStyle = new HashMap<>();
        ioStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CYLINDER);
        ioStyle.put(mxConstants.STYLE_FILLCOLOR, "#C8FFC8");
        ioStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        ioStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        ioStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        ioStyle.put(mxConstants.STYLE_FONTSIZE, 12);
        stylesheet.putCellStyle(IO, ioStyle);

        // Loop block style (hexagon, orange)
        Map<String, Object> loopStyle = new HashMap<>();
        loopStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
        loopStyle.put(mxConstants.STYLE_FILLCOLOR, "#FFDCC8");
        loopStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        loopStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        loopStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        loopStyle.put(mxConstants.STYLE_FONTSIZE, 12);
        stylesheet.putCellStyle(LOOP, loopStyle);

        // Start/End block style (rounded rectangle, gray)
        Map<String, Object> startEndStyle = new HashMap<>();
        startEndStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        startEndStyle.put(mxConstants.STYLE_FILLCOLOR, "#E0E0E0");
        startEndStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        startEndStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        startEndStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        startEndStyle.put(mxConstants.STYLE_FONTSIZE, 12);
        startEndStyle.put(mxConstants.STYLE_ROUNDED, true);
        startEndStyle.put(mxConstants.STYLE_ARCSIZE, 50);
        stylesheet.putCellStyle(START, startEndStyle);
        stylesheet.putCellStyle(END, startEndStyle);

        // Merge point style (small circle, black)
        Map<String, Object> mergeStyle = new HashMap<>();
        mergeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        mergeStyle.put(mxConstants.STYLE_FILLCOLOR, "#000000");
        mergeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        mergeStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        mergeStyle.put(mxConstants.STYLE_FONTCOLOR, "#FFFFFF");
        mergeStyle.put(mxConstants.STYLE_FONTSIZE, 1);
        stylesheet.putCellStyle(MERGE, mergeStyle);

        // Edge styles - THICKER for better visibility
        Map<String, Object> edgeStyle = new HashMap<>();
        edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 3);  // Increased from 2 to 3
        edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
        stylesheet.setDefaultEdgeStyle(edgeStyle);

        // True branch edge style (green) - exits from RIGHT side of diamond
        Map<String, Object> trueBranchStyle = new HashMap<>(edgeStyle);
        trueBranchStyle.put(mxConstants.STYLE_STROKECOLOR, "#009600");
        trueBranchStyle.put(mxConstants.STYLE_FONTCOLOR, "#009600");
        trueBranchStyle.put(mxConstants.STYLE_STROKEWIDTH, 3);
        trueBranchStyle.put(mxConstants.STYLE_EXIT_X, 1.0);  // Exit from right
        trueBranchStyle.put(mxConstants.STYLE_EXIT_Y, 0.5);  // Middle of right side
        trueBranchStyle.put(mxConstants.STYLE_EXIT_PERIMETER, 0);
        stylesheet.putCellStyle("TRUE_BRANCH", trueBranchStyle);

        // False branch edge style (red) - exits from LEFT side of diamond
        Map<String, Object> falseBranchStyle = new HashMap<>(edgeStyle);
        falseBranchStyle.put(mxConstants.STYLE_STROKECOLOR, "#960000");
        falseBranchStyle.put(mxConstants.STYLE_FONTCOLOR, "#960000");
        falseBranchStyle.put(mxConstants.STYLE_STROKEWIDTH, 3);
        falseBranchStyle.put(mxConstants.STYLE_EXIT_X, 0.0);  // Exit from left
        falseBranchStyle.put(mxConstants.STYLE_EXIT_Y, 0.5);  // Middle of left side
        falseBranchStyle.put(mxConstants.STYLE_EXIT_PERIMETER, 0);
        stylesheet.putCellStyle("FALSE_BRANCH", falseBranchStyle);
    }

    private void setupMouseListeners() {
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Double-click to edit label
                    editSelectedLabel();
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Single click - check if clicking on an edge
                    handleEdgeClick(e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Initialize flowchart with Start -> End
     */
    private void initializeStartEnd() {
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            // Create Start block
            Object start = graph.insertVertex(parent, "start", "Start", 300, 50, 120, 50, START);

            // Create End block
            Object end = graph.insertVertex(parent, "end", "End", 300, 200, 120, 50, END);

            // Connect them
            graph.insertEdge(parent, null, "", start, end);

            // Apply layout
            applyHierarchicalLayout();

        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Handle click on edges to insert blocks
     */
    private void handleEdgeClick(int x, int y) {
        Object cell = graphComponent.getCellAt(x, y);

        // Check if it's an edge
        if (cell != null && cell instanceof mxCell && ((mxCell) cell).isEdge()) {
            mxCell edge = (mxCell) cell;

            // Ask user what block type to insert
            showBlockTypeDialog(edge);
        }
    }

    /**
     * Show dialog to select block type to insert
     */
    private void showBlockTypeDialog(mxCell edge) {
        String[] options = {"Process", "Conditional (IF)", "I/O", "Loop", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Select block type to insert:",
            "Insert Block",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        String blockType = null;
        switch (choice) {
            case 0: blockType = PROCESS; break;
            case 1: blockType = CONDITIONAL; break;
            case 2: blockType = IO; break;
            case 3: blockType = LOOP; break;
            default: return; // Cancelled
        }

        if (blockType != null) {
            // Ask for block text
            String defaultText = getDefaultTextForBlockType(blockType);
            String text = JOptionPane.showInputDialog(
                this,
                "Enter block text:",
                defaultText
            );

            if (text != null && !text.trim().isEmpty()) {
                insertBlockInEdge(edge, blockType, text.trim());
            }
        }
    }

    /**
     * Insert a block in the middle of an edge
     */
    private void insertBlockInEdge(mxCell edge, String blockType, String text) {
        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try {
            // Get source and target of the edge
            mxCell source = (mxCell) edge.getSource();
            mxCell target = (mxCell) edge.getTarget();

            // Remove the original edge
            graph.removeCells(new Object[]{edge});

            if (blockType.equals(CONDITIONAL)) {
                // Special handling for IF blocks
                insertConditionalBlock(source, target, text);
            } else {
                // Regular block insertion
                insertRegularBlock(source, target, blockType, text);
            }

            // Apply layout
            applyHierarchicalLayout();

        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Insert a regular (non-conditional) block
     */
    private void insertRegularBlock(mxCell source, mxCell target, String blockType, String text) {
        Object parent = graph.getDefaultParent();

        // Determine size based on block type
        int width = 140;
        int height = 60;
        if (blockType.equals(LOOP)) {
            width = 120;
            height = 70;
        }

        // Create new block
        Object newBlock = graph.insertVertex(parent, null, text, 0, 0, width, height, blockType);

        // Connect: source -> newBlock -> target
        graph.insertEdge(parent, null, "", source, newBlock);
        graph.insertEdge(parent, null, "", newBlock, target);
    }

    /**
     * Insert a conditional (IF) block with merge structure
     */
    private void insertConditionalBlock(mxCell source, mxCell target, String text) {
        Object parent = graph.getDefaultParent();

        // Create the conditional block (diamond)
        Object conditional = graph.insertVertex(parent, null, text, 0, 0, 120, 80, CONDITIONAL);

        // Create merge point (small circle where branches meet)
        Object mergePoint = graph.insertVertex(parent, null, "", 0, 0, 15, 15, MERGE);

        // Store the association
        conditionalMergePoints.put(conditional, mergePoint);

        // Connect: source -> conditional
        graph.insertEdge(parent, null, "", source, conditional);

        // Create TRUE and FALSE branches to merge point (SI/NO labels in Italian)
        Object trueBranch = graph.insertEdge(parent, null, "Sì", conditional, mergePoint, "TRUE_BRANCH");
        Object falseBranch = graph.insertEdge(parent, null, "No", conditional, mergePoint, "FALSE_BRANCH");

        // Connect: mergePoint -> target
        graph.insertEdge(parent, null, "", mergePoint, target);
    }

    /**
     * Apply hierarchical layout to organize the flowchart
     */
    public void applyHierarchicalLayout() {
        Object parent = graph.getDefaultParent();
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setInterRankCellSpacing(60);
        layout.setIntraCellSpacing(80);  // More space between branches
        layout.execute(parent);

        // Center the graph in viewport
        centerGraph();
    }

    /**
     * Center the graph in the viewport
     */
    private void centerGraph() {
        // Get the bounds of all cells
        Object[] cells = graph.getChildCells(graph.getDefaultParent());
        if (cells.length == 0) return;

        // Get viewport dimensions
        Dimension viewportSize = graphComponent.getViewport().getSize();

        // Calculate graph bounds
        mxCell firstCell = (mxCell) cells[0];
        double minX = firstCell.getGeometry().getX();
        double maxX = minX + firstCell.getGeometry().getWidth();
        double minY = firstCell.getGeometry().getY();
        double maxY = minY + firstCell.getGeometry().getHeight();

        for (Object cell : cells) {
            if (cell instanceof mxCell && ((mxCell) cell).isVertex()) {
                mxCell mxCell = (mxCell) cell;
                mxGeometry geo = mxCell.getGeometry();
                if (geo != null) {
                    minX = Math.min(minX, geo.getX());
                    maxX = Math.max(maxX, geo.getX() + geo.getWidth());
                    minY = Math.min(minY, geo.getY());
                    maxY = Math.max(maxY, geo.getY() + geo.getHeight());
                }
            }
        }

        double graphWidth = maxX - minX;
        double graphHeight = maxY - minY;

        // Calculate offset to center
        double offsetX = (viewportSize.width - graphWidth) / 2 - minX;
        double offsetY = 50; // Keep some margin from top

        // Move all cells
        graph.getModel().beginUpdate();
        try {
            for (Object cell : cells) {
                if (cell instanceof mxCell && ((mxCell) cell).isVertex()) {
                    mxCell mxCell = (mxCell) cell;
                    mxGeometry geo = mxCell.getGeometry();
                    if (geo != null) {
                        geo = (mxGeometry) geo.clone();
                        geo.setX(geo.getX() + offsetX);
                        geo.setY(geo.getY() + offsetY);
                        graph.getModel().setGeometry(cell, geo);
                    }
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }

        graphComponent.refresh();
    }

    /**
     * Clear the entire flowchart and reinitialize
     */
    public void clearFlowchart() {
        graph.getModel().beginUpdate();
        try {
            graph.removeCells(graph.getChildCells(graph.getDefaultParent()));
            conditionalMergePoints.clear();
            initializeStartEnd();
        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Delete the selected cell(s)
     */
    public void deleteSelected() {
        Object[] cells = graph.getSelectionCells();
        if (cells != null && cells.length > 0) {
            // Don't allow deleting Start or End
            for (Object cell : cells) {
                if (cell instanceof mxCell) {
                    mxCell mxCell = (mxCell) cell;
                    String id = mxCell.getId();
                    if ("start".equals(id) || "end".equals(id)) {
                        JOptionPane.showMessageDialog(this,
                            "Cannot delete Start or End blocks!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            graph.removeCells(cells);
            applyHierarchicalLayout();
        }
    }

    /**
     * Edit the label of the selected cell
     */
    public void editSelectedLabel() {
        Object cell = graph.getSelectionCell();
        if (cell != null && cell instanceof mxCell) {
            mxCell mxCell = (mxCell) cell;
            if (mxCell.isVertex() && !MERGE.equals(mxCell.getStyle())) {
                String currentLabel = (String) mxCell.getValue();
                String newLabel = JOptionPane.showInputDialog(
                    this,
                    "Edit text:",
                    currentLabel
                );

                if (newLabel != null && !newLabel.trim().isEmpty()) {
                    graph.getModel().beginUpdate();
                    try {
                        mxCell.setValue(newLabel.trim());
                        graph.refresh();
                    } finally {
                        graph.getModel().endUpdate();
                    }
                }
            }
        }
    }

    /**
     * Show context menu on right-click
     */
    private void showContextMenu(int x, int y) {
        Object cell = graphComponent.getCellAt(x, y);

        JPopupMenu menu = new JPopupMenu();

        if (cell != null && cell instanceof mxCell) {
            mxCell mxCell = (mxCell) cell;

            if (mxCell.isVertex() && !MERGE.equals(mxCell.getStyle())) {
                JMenuItem editItem = new JMenuItem("Edit");
                editItem.addActionListener(e -> editSelectedLabel());
                menu.add(editItem);

                // Don't allow deleting Start or End
                if (!"start".equals(mxCell.getId()) && !"end".equals(mxCell.getId())) {
                    JMenuItem deleteItem = new JMenuItem("Delete");
                    deleteItem.addActionListener(e -> deleteSelected());
                    menu.add(deleteItem);
                }

                menu.addSeparator();
            } else if (mxCell.isEdge()) {
                JMenuItem insertItem = new JMenuItem("Insert Block Here");
                insertItem.addActionListener(e -> showBlockTypeDialog(mxCell));
                menu.add(insertItem);
                menu.addSeparator();
            }
        }

        JMenuItem layoutItem = new JMenuItem("Re-apply Layout");
        layoutItem.addActionListener(e -> applyHierarchicalLayout());
        menu.add(layoutItem);

        menu.show(graphComponent.getGraphControl(), x, y);
    }

    /**
     * Zoom in
     */
    public void zoomIn() {
        graphComponent.zoomIn();
    }

    /**
     * Zoom out
     */
    public void zoomOut() {
        graphComponent.zoomOut();
    }

    /**
     * Reset zoom to 100%
     */
    public void resetZoom() {
        graphComponent.zoomActual();
    }

    private String getDefaultTextForBlockType(String blockType) {
        switch (blockType) {
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
                return "Block";
        }
    }

    // ===== EXAMPLE FLOWCHARTS =====

    /**
     * Create a simple conditional flowchart example
     */
    public void createSimpleConditionalExample() {
        clearFlowchart();

        Object parent = graph.getDefaultParent();
        Object start = graph.getModel().getCell("start");
        Object end = graph.getModel().getCell("end");

        graph.getModel().beginUpdate();
        try {
            // Remove Start->End edge
            Object[] edges = graph.getEdgesBetween(start, end);
            if (edges.length > 0) {
                graph.removeCells(edges);
            }

            // Build: Start -> Input -> Condition -> Output -> End
            Object input = graph.insertVertex(parent, null, "Input: n", 0, 0, 140, 60, IO);
            graph.insertEdge(parent, null, "", start, input);

            Object condition = graph.insertVertex(parent, null, "n > 0?", 0, 0, 120, 80, CONDITIONAL);
            graph.insertEdge(parent, null, "", input, condition);

            Object mergePoint = graph.insertVertex(parent, null, "", 0, 0, 15, 15, MERGE);
            conditionalMergePoints.put(condition, mergePoint);

            // True branch
            Object processTrue = graph.insertVertex(parent, null, "result = n * 2", 0, 0, 140, 60, PROCESS);
            graph.insertEdge(parent, null, "Sì", condition, processTrue, "TRUE_BRANCH");
            graph.insertEdge(parent, null, "", processTrue, mergePoint);

            // False branch
            Object processFalse = graph.insertVertex(parent, null, "result = 0", 0, 0, 140, 60, PROCESS);
            graph.insertEdge(parent, null, "No", condition, processFalse, "FALSE_BRANCH");
            graph.insertEdge(parent, null, "", processFalse, mergePoint);

            // After merge
            Object output = graph.insertVertex(parent, null, "Output: result", 0, 0, 140, 60, IO);
            graph.insertEdge(parent, null, "", mergePoint, output);
            graph.insertEdge(parent, null, "", output, end);

            applyHierarchicalLayout();

        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Create a loop flowchart example
     */
    public void createLoopExample() {
        clearFlowchart();

        Object parent = graph.getDefaultParent();
        Object start = graph.getModel().getCell("start");
        Object end = graph.getModel().getCell("end");

        graph.getModel().beginUpdate();
        try {
            // Remove Start->End edge
            Object[] edges = graph.getEdgesBetween(start, end);
            if (edges.length > 0) {
                graph.removeCells(edges);
            }

            // Build flowchart
            Object input = graph.insertVertex(parent, null, "Input: n\ni = 0", 0, 0, 140, 60, IO);
            graph.insertEdge(parent, null, "", start, input);

            Object loop = graph.insertVertex(parent, null, "i < n?", 0, 0, 120, 70, LOOP);
            graph.insertEdge(parent, null, "", input, loop);

            Object mergePoint = graph.insertVertex(parent, null, "", 0, 0, 15, 15, MERGE);

            Object loopBody = graph.insertVertex(parent, null, "Print i\ni = i + 1", 0, 0, 140, 60, PROCESS);
            graph.insertEdge(parent, null, "Yes", loop, loopBody, "TRUE_BRANCH");

            // Loop back
            graph.insertEdge(parent, null, "", loopBody, loop);

            graph.insertEdge(parent, null, "No", loop, mergePoint, "FALSE_BRANCH");

            Object output = graph.insertVertex(parent, null, "Done", 0, 0, 140, 60, IO);
            graph.insertEdge(parent, null, "", mergePoint, output);
            graph.insertEdge(parent, null, "", output, end);

            applyHierarchicalLayout();

        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Create a nested conditional flowchart example
     */
    public void createNestedConditionalExample() {
        clearFlowchart();

        Object parent = graph.getDefaultParent();
        Object start = graph.getModel().getCell("start");
        Object end = graph.getModel().getCell("end");

        graph.getModel().beginUpdate();
        try {
            // Remove Start->End edge
            Object[] edges = graph.getEdgesBetween(start, end);
            if (edges.length > 0) {
                graph.removeCells(edges);
            }

            // Build flowchart
            Object input = graph.insertVertex(parent, null, "Input: x, y", 0, 0, 140, 60, IO);
            graph.insertEdge(parent, null, "", start, input);

            // Outer condition
            Object outerCond = graph.insertVertex(parent, null, "x > 0?", 0, 0, 120, 80, CONDITIONAL);
            graph.insertEdge(parent, null, "", input, outerCond);

            Object outerMerge = graph.insertVertex(parent, null, "", 0, 0, 15, 15, MERGE);

            // True branch - nested condition
            Object innerCond = graph.insertVertex(parent, null, "y > 0?", 0, 0, 120, 80, CONDITIONAL);
            graph.insertEdge(parent, null, "Sì", outerCond, innerCond, "TRUE_BRANCH");

            Object innerMerge = graph.insertVertex(parent, null, "", 0, 0, 15, 15, MERGE);

            Object innerTrue = graph.insertVertex(parent, null, "result = x + y", 0, 0, 140, 60, PROCESS);
            graph.insertEdge(parent, null, "Sì", innerCond, innerTrue, "TRUE_BRANCH");
            graph.insertEdge(parent, null, "", innerTrue, innerMerge);

            Object innerFalse = graph.insertVertex(parent, null, "result = x - y", 0, 0, 140, 60, PROCESS);
            graph.insertEdge(parent, null, "No", innerCond, innerFalse, "FALSE_BRANCH");
            graph.insertEdge(parent, null, "", innerFalse, innerMerge);

            graph.insertEdge(parent, null, "", innerMerge, outerMerge);

            // False branch
            Object outerFalse = graph.insertVertex(parent, null, "result = 0", 0, 0, 140, 60, PROCESS);
            graph.insertEdge(parent, null, "No", outerCond, outerFalse, "FALSE_BRANCH");
            graph.insertEdge(parent, null, "", outerFalse, outerMerge);

            // After merge
            Object output = graph.insertVertex(parent, null, "Output: result", 0, 0, 140, 60, IO);
            graph.insertEdge(parent, null, "", outerMerge, output);
            graph.insertEdge(parent, null, "", output, end);

            applyHierarchicalLayout();

        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Method called from toolbar - no longer used with new interaction model
     */
    public void addBlock(String blockType) {
        JOptionPane.showMessageDialog(this,
            "To add blocks:\n" +
            "1. Click on an EDGE (arrow) in the flowchart\n" +
            "2. Select the block type to insert\n\n" +
            "The new block will be inserted in the middle of the edge.",
            "How to Add Blocks",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
