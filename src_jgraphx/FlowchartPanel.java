import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
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
 * Handles flowchart creation, editing, and layout.
 */
public class FlowchartPanel extends JPanel {

    private mxGraph graph;
    private mxGraphComponent graphComponent;
    private Object lastAddedCell;  // Track last added cell for auto-connection

    // Block type constants
    public static final String PROCESS = "PROCESS";
    public static final String CONDITIONAL = "CONDITIONAL";
    public static final String IO = "IO";
    public static final String LOOP = "LOOP";
    public static final String START = "START";
    public static final String END = "END";

    public FlowchartPanel() {
        setLayout(new BorderLayout());

        // Create graph
        graph = new mxGraph();
        graph.setAllowDanglingEdges(false);
        graph.setCellsEditable(true);
        graph.setConnectableEdges(false);

        // Setup custom styles for flowchart blocks
        setupStyles();

        // Create graph component
        graphComponent = new mxGraphComponent(graph);
        graphComponent.setConnectable(true);
        graphComponent.getViewport().setOpaque(true);
        graphComponent.getViewport().setBackground(Color.WHITE);

        // Enable grid
        graphComponent.setGridVisible(true);
        graphComponent.setGridStyle(mxGraphComponent.GRID_STYLE_DOT);

        // Setup mouse listeners for context menu and editing
        setupMouseListeners();

        add(graphComponent, BorderLayout.CENTER);

        // Show welcome message
        showWelcomeMessage();
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

        // I/O block style (parallelogram, green)
        Map<String, Object> ioStyle = new HashMap<>();
        ioStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_PARALLELOGRAM);
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

        // Edge style (arrows)
        Map<String, Object> edgeStyle = new HashMap<>();
        edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
        stylesheet.setDefaultEdgeStyle(edgeStyle);
    }

    private void setupMouseListeners() {
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Double-click to edit label
                    editSelectedLabel();
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

    private void showWelcomeMessage() {
        // This will be visible until the first block is added
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            mxCell welcomeCell = (mxCell) graph.insertVertex(parent, null,
                "Welcome to Flowchart Editor!\n\n" +
                "Use toolbar buttons or Examples menu\n" +
                "to start creating flowcharts.",
                200, 200, 300, 100, "fillColor=#F0F0F0;strokeColor=#808080;fontColor=#404040");
            welcomeCell.setConnectable(false);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Add a new block to the flowchart.
     */
    public void addBlock(String blockType) {
        // Ask for block text
        String defaultText = getDefaultTextForBlockType(blockType);
        String text = JOptionPane.showInputDialog(
            this,
            "Enter " + blockType.toLowerCase() + " text:",
            defaultText
        );

        if (text != null && !text.trim().isEmpty()) {
            Object parent = graph.getDefaultParent();

            graph.getModel().beginUpdate();
            try {
                // Determine size based on block type
                int width = 140;
                int height = 60;
                if (blockType.equals(CONDITIONAL)) {
                    width = 120;
                    height = 80;
                } else if (blockType.equals(LOOP)) {
                    width = 120;
                    height = 70;
                }

                // Calculate position (below last added cell or at top)
                int x = 300;
                int y = 50;
                if (lastAddedCell != null && lastAddedCell instanceof mxCell) {
                    mxCell lastCell = (mxCell) lastAddedCell;
                    x = (int) lastCell.getGeometry().getX();
                    y = (int) (lastCell.getGeometry().getY() + lastCell.getGeometry().getHeight() + 60);
                }

                // Create vertex
                Object newCell = graph.insertVertex(parent, null, text, x, y, width, height, blockType);

                // Connect to previous cell if exists
                if (lastAddedCell != null) {
                    graph.insertEdge(parent, null, "", lastAddedCell, newCell);
                }

                lastAddedCell = newCell;

                // Auto layout
                applyHierarchicalLayout();

            } finally {
                graph.getModel().endUpdate();
            }
        }
    }

    /**
     * Apply hierarchical layout to organize the flowchart.
     */
    public void applyHierarchicalLayout() {
        Object parent = graph.getDefaultParent();
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setInterRankCellSpacing(60);
        layout.setIntraCellSpacing(50);
        layout.execute(parent);
    }

    /**
     * Clear the entire flowchart.
     */
    public void clearFlowchart() {
        graph.getModel().beginUpdate();
        try {
            graph.removeCells(graph.getChildCells(graph.getDefaultParent()));
            lastAddedCell = null;
            showWelcomeMessage();
        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Delete the selected cell(s).
     */
    public void deleteSelected() {
        Object[] cells = graph.getSelectionCells();
        if (cells != null && cells.length > 0) {
            graph.removeCells(cells);
            applyHierarchicalLayout();
        }
    }

    /**
     * Edit the label of the selected cell.
     */
    public void editSelectedLabel() {
        Object cell = graph.getSelectionCell();
        if (cell != null && cell instanceof mxCell) {
            mxCell mxCell = (mxCell) cell;
            if (mxCell.isVertex()) {
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
     * Show context menu on right-click.
     */
    private void showContextMenu(int x, int y) {
        Object cell = graphComponent.getCellAt(x, y);

        JPopupMenu menu = new JPopupMenu();

        if (cell != null && cell instanceof mxCell && ((mxCell) cell).isVertex()) {
            JMenuItem editItem = new JMenuItem("Edit");
            editItem.addActionListener(e -> editSelectedLabel());
            menu.add(editItem);

            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener(e -> deleteSelected());
            menu.add(deleteItem);
        }

        JMenuItem addProcessItem = new JMenuItem("Add Process Block");
        addProcessItem.addActionListener(e -> addBlock(PROCESS));
        menu.add(addProcessItem);

        JMenuItem addConditionalItem = new JMenuItem("Add Conditional Block");
        addConditionalItem.addActionListener(e -> addBlock(CONDITIONAL));
        menu.add(addConditionalItem);

        menu.addSeparator();

        JMenuItem layoutItem = new JMenuItem("Re-apply Layout");
        layoutItem.addActionListener(e -> applyHierarchicalLayout());
        menu.add(layoutItem);

        menu.show(graphComponent.getGraphControl(), x, y);
    }

    /**
     * Zoom in.
     */
    public void zoomIn() {
        graphComponent.zoomIn();
    }

    /**
     * Zoom out.
     */
    public void zoomOut() {
        graphComponent.zoomOut();
    }

    /**
     * Reset zoom to 100%.
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
     * Create a simple conditional flowchart example.
     */
    public void createSimpleConditionalExample() {
        clearFlowchart();

        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            // Create blocks
            Object start = graph.insertVertex(parent, null, "Start", 300, 20, 120, 50, START);
            Object input = graph.insertVertex(parent, null, "Input: n", 300, 100, 140, 60, IO);
            Object condition = graph.insertVertex(parent, null, "n > 0?", 300, 200, 120, 80, CONDITIONAL);
            Object processTrue = graph.insertVertex(parent, null, "result = n * 2", 450, 320, 140, 60, PROCESS);
            Object processFalse = graph.insertVertex(parent, null, "result = 0", 150, 320, 140, 60, PROCESS);
            Object output = graph.insertVertex(parent, null, "Output: result", 300, 420, 140, 60, IO);
            Object end = graph.insertVertex(parent, null, "End", 300, 520, 120, 50, END);

            // Create edges
            graph.insertEdge(parent, null, "", start, input);
            graph.insertEdge(parent, null, "", input, condition);

            // True branch
            Object edgeTrue = graph.insertEdge(parent, null, "True", condition, processTrue);
            ((mxCell) edgeTrue).setStyle("strokeColor=#009600;fontColor=#009600");

            // False branch
            Object edgeFalse = graph.insertEdge(parent, null, "False", condition, processFalse);
            ((mxCell) edgeFalse).setStyle("strokeColor=#960000;fontColor=#960000");

            graph.insertEdge(parent, null, "", processTrue, output);
            graph.insertEdge(parent, null, "", processFalse, output);
            graph.insertEdge(parent, null, "", output, end);

            // Apply layout
            applyHierarchicalLayout();

        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Create a loop flowchart example.
     */
    public void createLoopExample() {
        clearFlowchart();

        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            // Create blocks
            Object start = graph.insertVertex(parent, null, "Start", 300, 20, 120, 50, START);
            Object input = graph.insertVertex(parent, null, "Input: n\ni = 0", 300, 100, 140, 60, IO);
            Object loop = graph.insertVertex(parent, null, "i < n?", 300, 200, 120, 70, LOOP);
            Object loopBody = graph.insertVertex(parent, null, "Print i\ni = i + 1", 450, 310, 140, 60, PROCESS);
            Object output = graph.insertVertex(parent, null, "Output: Done", 300, 400, 140, 60, IO);
            Object end = graph.insertVertex(parent, null, "End", 300, 500, 120, 50, END);

            // Create edges
            graph.insertEdge(parent, null, "", start, input);
            graph.insertEdge(parent, null, "", input, loop);

            // Loop body edge
            Object edgeLoop = graph.insertEdge(parent, null, "Yes", loop, loopBody);
            ((mxCell) edgeLoop).setStyle("strokeColor=#009600;fontColor=#009600");

            // Loop back
            Object edgeBack = graph.insertEdge(parent, null, "", loopBody, loop);
            ((mxCell) edgeBack).setStyle("strokeColor=#0000FF;dashed=1");

            // Exit loop
            Object edgeExit = graph.insertEdge(parent, null, "No", loop, output);
            ((mxCell) edgeExit).setStyle("strokeColor=#960000;fontColor=#960000");

            graph.insertEdge(parent, null, "", output, end);

            // Apply layout
            applyHierarchicalLayout();

        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Create a nested conditional flowchart example.
     */
    public void createNestedConditionalExample() {
        clearFlowchart();

        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            // Create blocks
            Object start = graph.insertVertex(parent, null, "Start", 300, 20, 120, 50, START);
            Object input = graph.insertVertex(parent, null, "Input: x, y", 300, 100, 140, 60, IO);
            Object outerCond = graph.insertVertex(parent, null, "x > 0?", 300, 200, 120, 80, CONDITIONAL);

            // True branch - nested conditional
            Object innerCond = graph.insertVertex(parent, null, "y > 0?", 450, 320, 120, 80, CONDITIONAL);
            Object innerTrue = graph.insertVertex(parent, null, "result = x + y", 550, 440, 140, 60, PROCESS);
            Object innerFalse = graph.insertVertex(parent, null, "result = x - y", 350, 440, 140, 60, PROCESS);

            // False branch
            Object outerFalse = graph.insertVertex(parent, null, "result = 0", 150, 320, 140, 60, PROCESS);

            Object output = graph.insertVertex(parent, null, "Output: result", 300, 560, 140, 60, IO);
            Object end = graph.insertVertex(parent, null, "End", 300, 660, 120, 50, END);

            // Create edges
            graph.insertEdge(parent, null, "", start, input);
            graph.insertEdge(parent, null, "", input, outerCond);

            // Outer true branch
            Object edgeOuterTrue = graph.insertEdge(parent, null, "True", outerCond, innerCond);
            ((mxCell) edgeOuterTrue).setStyle("strokeColor=#009600;fontColor=#009600");

            // Outer false branch
            Object edgeOuterFalse = graph.insertEdge(parent, null, "False", outerCond, outerFalse);
            ((mxCell) edgeOuterFalse).setStyle("strokeColor=#960000;fontColor=#960000");

            // Inner conditional branches
            Object edgeInnerTrue = graph.insertEdge(parent, null, "True", innerCond, innerTrue);
            ((mxCell) edgeInnerTrue).setStyle("strokeColor=#009600;fontColor=#009600");

            Object edgeInnerFalse = graph.insertEdge(parent, null, "False", innerCond, innerFalse);
            ((mxCell) edgeInnerFalse).setStyle("strokeColor=#960000;fontColor=#960000");

            // Merge to output
            graph.insertEdge(parent, null, "", innerTrue, output);
            graph.insertEdge(parent, null, "", innerFalse, output);
            graph.insertEdge(parent, null, "", outerFalse, output);
            graph.insertEdge(parent, null, "", output, end);

            // Apply layout
            applyHierarchicalLayout();

        } finally {
            graph.getModel().endUpdate();
        }
    }
}
