package layout;

import model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the flowchart structure and provides operations for adding/removing blocks.
 * Handles automatic layout recalculation and branch balancing.
 */
public class FlowchartManager {
    private FlowBlock root;
    private List<FlowBlock> allBlocks;

    public FlowchartManager() {
        this.allBlocks = new ArrayList<>();
    }

    public FlowchartManager(FlowBlock root) {
        this.root = root;
        this.allBlocks = new ArrayList<>();
        collectAllBlocks(root);
    }

    /**
     * Set the root block and rebuild the block list.
     */
    public void setRoot(FlowBlock root) {
        this.root = root;
        this.allBlocks.clear();
        if (root != null) {
            collectAllBlocks(root);
        }
    }

    /**
     * Add a block at a specific connection point.
     */
    public void addBlockAtConnection(ConnectionPoint connectionPoint, FlowBlock newBlock) {
        FlowBlock owner = connectionPoint.getOwner();

        switch (connectionPoint.getType()) {
            case NEXT:
                addNextBlock(owner, newBlock);
                break;
            case TRUE_BRANCH:
                if (owner instanceof ConditionalBlock) {
                    ((ConditionalBlock) owner).setTrueBranch(newBlock);
                }
                break;
            case FALSE_BRANCH:
                if (owner instanceof ConditionalBlock) {
                    ((ConditionalBlock) owner).setFalseBranch(newBlock);
                }
                break;
            case LOOP_BODY:
                if (owner instanceof LoopBlock) {
                    ((LoopBlock) owner).setLoopBody(newBlock);
                }
                break;
        }

        newBlock.setParent(owner);
        allBlocks.add(newBlock);

        // Recalculate layout
        recalculateLayout();
    }

    /**
     * Add a block as the next block of the given owner.
     */
    private void addNextBlock(FlowBlock owner, FlowBlock newBlock) {
        if (owner instanceof ProcessBlock) {
            ((ProcessBlock) owner).setNextBlock(newBlock);
        } else if (owner instanceof IOBlock) {
            ((IOBlock) owner).setNextBlock(newBlock);
        } else if (owner instanceof StartEndBlock) {
            ((StartEndBlock) owner).setNextBlock(newBlock);
        } else if (owner instanceof ConditionalBlock) {
            ((ConditionalBlock) owner).setNextBlock(newBlock);
        } else if (owner instanceof LoopBlock) {
            ((LoopBlock) owner).setNextBlock(newBlock);
        }
    }

    /**
     * Remove a block from the flowchart.
     */
    public void removeBlock(FlowBlock block) {
        if (block == root) {
            root = null;
            allBlocks.clear();
            return;
        }

        FlowBlock parent = block.getParent();
        if (parent == null) return;

        // Remove from parent
        if (parent instanceof ProcessBlock) {
            ((ProcessBlock) parent).setNextBlock(null);
        } else if (parent instanceof IOBlock) {
            ((IOBlock) parent).setNextBlock(null);
        } else if (parent instanceof StartEndBlock) {
            ((StartEndBlock) parent).setNextBlock(null);
        } else if (parent instanceof ConditionalBlock) {
            ConditionalBlock cond = (ConditionalBlock) parent;
            if (cond.getTrueBranch() == block) {
                cond.setTrueBranch(null);
            } else if (cond.getFalseBranch() == block) {
                cond.setFalseBranch(null);
            } else if (cond.getNextBlock() == block) {
                cond.setNextBlock(null);
            }
        } else if (parent instanceof LoopBlock) {
            LoopBlock loop = (LoopBlock) parent;
            if (loop.getLoopBody() == block) {
                loop.setLoopBody(null);
            } else if (loop.getNextBlock() == block) {
                loop.setNextBlock(null);
            }
        }

        allBlocks.remove(block);
        recalculateLayout();
    }

    /**
     * Recalculate the entire layout starting from root.
     * This is called after any structural change.
     * Uses Sugiyama-style hierarchical layout algorithm.
     */
    public void recalculateLayout() {
        if (root != null) {
            SugiyamaLayoutManager sugiyamaLayout = new SugiyamaLayoutManager();
            sugiyamaLayout.layout(root);
        }
    }

    /**
     * Collect all blocks in the flowchart (for rendering and collision detection).
     */
    private void collectAllBlocks(FlowBlock block) {
        if (block == null) return;

        allBlocks.add(block);

        for (FlowBlock child : block.getChildren()) {
            collectAllBlocks(child);
        }
    }

    /**
     * Find a block by ID.
     */
    public FlowBlock findBlockById(String id) {
        for (FlowBlock block : allBlocks) {
            if (block.getId().equals(id)) {
                return block;
            }
        }
        return null;
    }

    /**
     * Find the block at a specific point (for mouse clicks).
     */
    public FlowBlock findBlockAtPoint(int x, int y) {
        // Search in reverse order to find topmost block
        for (int i = allBlocks.size() - 1; i >= 0; i--) {
            FlowBlock block = allBlocks.get(i);
            if (block.contains(x, y)) {
                return block;
            }
        }
        return null;
    }

    /**
     * Find all connection points in the flowchart.
     */
    public List<ConnectionPoint> getAllConnectionPoints() {
        List<ConnectionPoint> points = new ArrayList<>();
        for (FlowBlock block : allBlocks) {
            points.addAll(block.getConnectionPoints());
        }
        return points;
    }

    /**
     * Find connection point at a specific location.
     */
    public ConnectionPoint findConnectionPointAtLocation(int x, int y) {
        for (ConnectionPoint point : getAllConnectionPoints()) {
            if (point.contains(x, y)) {
                return point;
            }
        }
        return null;
    }

    /**
     * Create a default flowchart (for testing/demo).
     */
    public static FlowchartManager createDefaultFlowchart() {
        // Start block
        StartEndBlock start = new StartEndBlock("Start", true);

        // Input block
        IOBlock input = new IOBlock("Input: n");
        start.setNextBlock(input);

        // Conditional block
        ConditionalBlock condition = new ConditionalBlock("n > 0?");
        input.setNextBlock(condition);

        // True branch
        ProcessBlock processTrue = new ProcessBlock("result = n * 2");
        condition.setTrueBranch(processTrue);

        // False branch
        ProcessBlock processFalse = new ProcessBlock("result = 0");
        condition.setFalseBranch(processFalse);

        // Output after conditional
        IOBlock output = new IOBlock("Output: result");
        condition.setNextBlock(output);

        // End block
        StartEndBlock end = new StartEndBlock("End", false);
        output.setNextBlock(end);

        FlowchartManager manager = new FlowchartManager(start);
        manager.recalculateLayout();

        return manager;
    }

    /**
     * Create a flowchart with a loop (for testing/demo).
     */
    public static FlowchartManager createLoopFlowchart() {
        // Start block
        StartEndBlock start = new StartEndBlock("Start", true);

        // Input block
        IOBlock input = new IOBlock("Input: n\ni = 0");
        start.setNextBlock(input);

        // Loop block
        LoopBlock loop = new LoopBlock("i < n?");
        input.setNextBlock(loop);

        // Loop body
        ProcessBlock loopBody = new ProcessBlock("Print i\ni = i + 1");
        loop.setLoopBody(loopBody);

        // After loop
        IOBlock output = new IOBlock("Output: Done");
        loop.setNextBlock(output);

        // End block
        StartEndBlock end = new StartEndBlock("End", false);
        output.setNextBlock(end);

        FlowchartManager manager = new FlowchartManager(start);
        manager.recalculateLayout();

        return manager;
    }

    /**
     * Create a nested conditional flowchart (for testing/demo).
     */
    public static FlowchartManager createNestedFlowchart() {
        StartEndBlock start = new StartEndBlock("Start", true);

        IOBlock input = new IOBlock("Input: x, y");
        start.setNextBlock(input);

        // Outer conditional
        ConditionalBlock outer = new ConditionalBlock("x > 0?");
        input.setNextBlock(outer);

        // Inner conditional (true branch of outer)
        ConditionalBlock inner = new ConditionalBlock("y > 0?");
        outer.setTrueBranch(inner);

        ProcessBlock innerTrue = new ProcessBlock("result = x + y");
        inner.setTrueBranch(innerTrue);

        ProcessBlock innerFalse = new ProcessBlock("result = x - y");
        inner.setFalseBranch(innerFalse);

        // False branch of outer
        ProcessBlock outerFalse = new ProcessBlock("result = 0");
        outer.setFalseBranch(outerFalse);

        // Output
        IOBlock output = new IOBlock("Output: result");
        outer.setNextBlock(output);

        StartEndBlock end = new StartEndBlock("End", false);
        output.setNextBlock(end);

        FlowchartManager manager = new FlowchartManager(start);
        manager.recalculateLayout();

        return manager;
    }

    // Getters
    public FlowBlock getRoot() {
        return root;
    }

    public List<FlowBlock> getAllBlocks() {
        return new ArrayList<>(allBlocks);
    }

    /**
     * Clear the flowchart.
     */
    public void clear() {
        root = null;
        allBlocks.clear();
    }
}
