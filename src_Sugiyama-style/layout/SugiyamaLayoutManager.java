package layout;

import model.*;
import java.util.*;

/**
 * Implements Sugiyama-style hierarchical graph layout algorithm.
 *
 * The algorithm consists of four phases:
 * 1. Layer assignment - assign nodes to vertical layers
 * 2. Crossing reduction - minimize edge crossings
 * 3. Horizontal positioning - position nodes within layers
 * 4. Coordinate assignment - convert to actual x,y coordinates
 */
public class SugiyamaLayoutManager {

    private static final int LAYER_VERTICAL_SPACING = 120;
    private static final int HORIZONTAL_SPACING = 150;
    private static final int START_X = 50;
    private static final int START_Y = 50;

    // Maps blocks to their assigned layer (vertical level)
    private Map<FlowBlock, Integer> blockToLayer;

    // Maps layer number to list of blocks in that layer
    private Map<Integer, List<FlowBlock>> layerToBlocks;

    // Maps blocks to their horizontal position within their layer
    private Map<FlowBlock, Double> blockToHorizontalPos;

    private FlowBlock rootBlock;
    private int maxLayer;

    public SugiyamaLayoutManager() {
        blockToLayer = new HashMap<>();
        layerToBlocks = new HashMap<>();
        blockToHorizontalPos = new HashMap<>();
        maxLayer = 0;
    }

    /**
     * Apply Sugiyama layout to the flowchart starting from root block.
     */
    public void layout(FlowBlock root) {
        if (root == null) {
            return;
        }

        this.rootBlock = root;

        // Phase 1: Layer assignment
        assignLayers(root, 0);

        // Phase 2: Horizontal positioning (using barycenter method)
        initializeHorizontalPositions();
        optimizeHorizontalPositions();

        // Phase 3: Convert to actual coordinates
        assignCoordinates();

        // Phase 4: Layout blocks recursively (for their internal layout)
        layoutBlocksRecursively(root);
    }

    /**
     * Phase 1: Assign each block to a layer based on graph structure.
     */
    private void assignLayers(FlowBlock block, int layer) {
        if (block == null || blockToLayer.containsKey(block)) {
            return;
        }

        blockToLayer.put(block, layer);

        if (!layerToBlocks.containsKey(layer)) {
            layerToBlocks.put(layer, new ArrayList<>());
        }
        layerToBlocks.get(layer).add(block);

        maxLayer = Math.max(maxLayer, layer);

        // Process children based on block type
        if (block instanceof ConditionalBlock) {
            ConditionalBlock condBlock = (ConditionalBlock) block;

            // Both branches go to the next layer
            assignLayers(condBlock.getTrueBranch(), layer + 1);
            assignLayers(condBlock.getFalseBranch(), layer + 1);

            // Next block after merge goes to layer+2 (or further down)
            FlowBlock nextBlock = condBlock.getNextBlock();
            if (nextBlock != null) {
                // Find the maximum layer used by branches
                int maxBranchLayer = findMaxLayer(condBlock.getTrueBranch());
                maxBranchLayer = Math.max(maxBranchLayer, findMaxLayer(condBlock.getFalseBranch()));
                assignLayers(nextBlock, Math.max(layer + 2, maxBranchLayer + 1));
            }
        } else if (block instanceof LoopBlock) {
            LoopBlock loopBlock = (LoopBlock) block;
            assignLayers(loopBlock.getLoopBody(), layer + 1);

            FlowBlock nextBlock = loopBlock.getNextBlock();
            if (nextBlock != null) {
                int maxBodyLayer = findMaxLayer(loopBlock.getLoopBody());
                assignLayers(nextBlock, Math.max(layer + 2, maxBodyLayer + 1));
            }
        } else {
            // Simple block with single next connection
            // Use getChildren to find next block
            List<FlowBlock> children = block.getChildren();
            if (!children.isEmpty()) {
                assignLayers(children.get(0), layer + 1);
            }
        }
    }

    /**
     * Find the maximum layer used by a block and its descendants.
     */
    private int findMaxLayer(FlowBlock block) {
        if (block == null || !blockToLayer.containsKey(block)) {
            return -1;
        }

        int maxLay = blockToLayer.get(block);

        if (block instanceof ConditionalBlock) {
            ConditionalBlock condBlock = (ConditionalBlock) block;
            maxLay = Math.max(maxLay, findMaxLayer(condBlock.getTrueBranch()));
            maxLay = Math.max(maxLay, findMaxLayer(condBlock.getFalseBranch()));
            maxLay = Math.max(maxLay, findMaxLayer(condBlock.getNextBlock()));
        } else if (block instanceof LoopBlock) {
            LoopBlock loopBlock = (LoopBlock) block;
            maxLay = Math.max(maxLay, findMaxLayer(loopBlock.getLoopBody()));
            maxLay = Math.max(maxLay, findMaxLayer(loopBlock.getNextBlock()));
        } else {
            // Use getChildren for simple blocks
            List<FlowBlock> children = block.getChildren();
            for (FlowBlock child : children) {
                maxLay = Math.max(maxLay, findMaxLayer(child));
            }
        }

        return maxLay;
    }

    /**
     * Phase 2a: Initialize horizontal positions evenly across each layer.
     */
    private void initializeHorizontalPositions() {
        for (Map.Entry<Integer, List<FlowBlock>> entry : layerToBlocks.entrySet()) {
            List<FlowBlock> blocks = entry.getValue();
            for (int i = 0; i < blocks.size(); i++) {
                blockToHorizontalPos.put(blocks.get(i), (double) i);
            }
        }
    }

    /**
     * Phase 2b: Optimize horizontal positions using barycenter method.
     * Iterate multiple times to converge to good positions.
     */
    private void optimizeHorizontalPositions() {
        int iterations = 10;

        for (int iter = 0; iter < iterations; iter++) {
            // Downward pass - position based on parents
            for (int layer = 1; layer <= maxLayer; layer++) {
                if (!layerToBlocks.containsKey(layer)) continue;

                for (FlowBlock block : layerToBlocks.get(layer)) {
                    List<FlowBlock> parents = findParents(block);
                    if (!parents.isEmpty()) {
                        double sum = 0;
                        for (FlowBlock parent : parents) {
                            sum += blockToHorizontalPos.get(parent);
                        }
                        blockToHorizontalPos.put(block, sum / parents.size());
                    }
                }

                // Sort blocks in layer by horizontal position
                sortLayer(layer);
            }

            // Upward pass - position based on children
            for (int layer = maxLayer - 1; layer >= 0; layer--) {
                if (!layerToBlocks.containsKey(layer)) continue;

                for (FlowBlock block : layerToBlocks.get(layer)) {
                    List<FlowBlock> children = findChildren(block);
                    if (!children.isEmpty()) {
                        double sum = 0;
                        for (FlowBlock child : children) {
                            sum += blockToHorizontalPos.get(child);
                        }
                        blockToHorizontalPos.put(block, sum / children.size());
                    }
                }

                // Sort blocks in layer by horizontal position
                sortLayer(layer);
            }
        }
    }

    /**
     * Find parent blocks (blocks that point to this block).
     */
    private List<FlowBlock> findParents(FlowBlock target) {
        List<FlowBlock> parents = new ArrayList<>();

        for (FlowBlock block : blockToLayer.keySet()) {
            if (block instanceof ConditionalBlock) {
                ConditionalBlock condBlock = (ConditionalBlock) block;
                if (condBlock.getTrueBranch() == target || condBlock.getFalseBranch() == target) {
                    parents.add(block);
                }
                if (condBlock.getNextBlock() == target) {
                    parents.add(block);
                }
            } else if (block instanceof LoopBlock) {
                LoopBlock loopBlock = (LoopBlock) block;
                if (loopBlock.getLoopBody() == target || loopBlock.getNextBlock() == target) {
                    parents.add(block);
                }
            } else {
                // Check if any child of this block is the target
                for (FlowBlock child : block.getChildren()) {
                    if (child == target) {
                        parents.add(block);
                        break;
                    }
                }
            }
        }

        return parents;
    }

    /**
     * Find child blocks (blocks this block points to).
     */
    private List<FlowBlock> findChildren(FlowBlock block) {
        List<FlowBlock> children = new ArrayList<>();

        if (block instanceof ConditionalBlock) {
            ConditionalBlock condBlock = (ConditionalBlock) block;
            if (condBlock.getTrueBranch() != null) children.add(condBlock.getTrueBranch());
            if (condBlock.getFalseBranch() != null) children.add(condBlock.getFalseBranch());
            if (condBlock.getNextBlock() != null) children.add(condBlock.getNextBlock());
        } else if (block instanceof LoopBlock) {
            LoopBlock loopBlock = (LoopBlock) block;
            if (loopBlock.getLoopBody() != null) children.add(loopBlock.getLoopBody());
            if (loopBlock.getNextBlock() != null) children.add(loopBlock.getNextBlock());
        } else {
            // Use getChildren for simple blocks
            children.addAll(block.getChildren());
        }

        return children;
    }

    /**
     * Sort blocks within a layer by horizontal position and reassign positions.
     */
    private void sortLayer(int layer) {
        if (!layerToBlocks.containsKey(layer)) return;

        List<FlowBlock> blocks = layerToBlocks.get(layer);
        blocks.sort(Comparator.comparingDouble(b -> blockToHorizontalPos.get(b)));

        // Reassign positions to maintain order and spacing
        for (int i = 0; i < blocks.size(); i++) {
            blockToHorizontalPos.put(blocks.get(i), (double) i);
        }
    }

    /**
     * Phase 3: Convert layer and horizontal positions to actual x,y coordinates.
     */
    private void assignCoordinates() {
        for (Map.Entry<FlowBlock, Integer> entry : blockToLayer.entrySet()) {
            FlowBlock block = entry.getKey();
            int layer = entry.getValue();
            double hPos = blockToHorizontalPos.get(block);

            int x = START_X + (int)(hPos * HORIZONTAL_SPACING);
            int y = START_Y + layer * LAYER_VERTICAL_SPACING;

            block.setPosition(x, y);
        }
    }

    /**
     * Phase 4: Call layout on each block to handle internal positioning.
     */
    private void layoutBlocksRecursively(FlowBlock block) {
        if (block == null) {
            return;
        }

        Set<FlowBlock> visited = new HashSet<>();
        layoutBlocksRecursivelyHelper(block, visited);
    }

    private void layoutBlocksRecursivelyHelper(FlowBlock block, Set<FlowBlock> visited) {
        if (block == null || visited.contains(block)) {
            return;
        }

        visited.add(block);

        // For conditional blocks, we need special handling
        if (block instanceof ConditionalBlock) {
            ConditionalBlock condBlock = (ConditionalBlock) block;
            // The Sugiyama layout already positioned everything,
            // but we still need to call layout for internal calculations
            condBlock.layout(block.getX(), block.getY());

            layoutBlocksRecursivelyHelper(condBlock.getTrueBranch(), visited);
            layoutBlocksRecursivelyHelper(condBlock.getFalseBranch(), visited);
            layoutBlocksRecursivelyHelper(condBlock.getNextBlock(), visited);
        } else if (block instanceof LoopBlock) {
            LoopBlock loopBlock = (LoopBlock) block;
            loopBlock.layout(block.getX(), block.getY());

            layoutBlocksRecursivelyHelper(loopBlock.getLoopBody(), visited);
            layoutBlocksRecursivelyHelper(loopBlock.getNextBlock(), visited);
        } else {
            block.layout(block.getX(), block.getY());
            // Use getChildren for simple blocks
            for (FlowBlock child : block.getChildren()) {
                layoutBlocksRecursivelyHelper(child, visited);
            }
        }
    }
}
