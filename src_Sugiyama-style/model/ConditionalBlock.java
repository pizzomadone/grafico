package model;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Conditional block (diamond shape) with true/false branches.
 * This is the most complex block as it manages branch balancing.
 */
public class ConditionalBlock extends FlowBlock {
    private static final long serialVersionUID = 1L;

    private FlowBlock trueBranch;
    private FlowBlock falseBranch;
    private FlowBlock nextBlock;  // Block after the merge point
    private int mergePointY;  // Y coordinate where branches merge
    private int trueBranchEndY;  // Where true branch ends
    private int falseBranchEndY;  // Where false branch ends

    // Offsets for branch positioning
    private static final int BRANCH_HORIZONTAL_OFFSET = 150;

    public ConditionalBlock(String condition) {
        super(condition);
        this.width = 120;
        this.height = 80;
    }

    @Override
    public int calculateHeight() {
        int trueHeight = trueBranch != null ? trueBranch.calculateHeight() : 0;
        int falseHeight = falseBranch != null ? falseBranch.calculateHeight() : 0;
        int maxBranchHeight = Math.max(trueHeight, falseHeight);

        // Height includes: block itself + max branch + merge spacing + next block
        int totalHeight = height + maxBranchHeight + VERTICAL_SPACING;

        if (nextBlock != null) {
            totalHeight += nextBlock.calculateHeight();
        }

        return totalHeight;
    }

    @Override
    public int calculateWidth() {
        int trueWidth = trueBranch != null ? trueBranch.calculateWidth() : 0;
        int falseWidth = falseBranch != null ? falseBranch.calculateWidth() : 0;

        // Width is max of: own width, sum of both branches + spacing
        int branchesWidth = trueWidth + falseWidth + BRANCH_HORIZONTAL_OFFSET * 2 + HORIZONTAL_SPACING;
        int nextWidth = nextBlock != null ? nextBlock.calculateWidth() : 0;

        return Math.max(width, Math.max(branchesWidth, nextWidth));
    }

    @Override
    public void layout(int startX, int startY) {
        // In Sugiyama-style layout, positions are already set by SugiyamaLayoutManager
        // This method just calculates merge point and branch end positions for drawing

        // Calculate branch end positions
        if (trueBranch != null) {
            trueBranchEndY = findLastBlockY(trueBranch);
        } else {
            trueBranchEndY = y + height;
        }

        if (falseBranch != null) {
            falseBranchEndY = findLastBlockY(falseBranch);
        } else {
            falseBranchEndY = y + height;
        }

        // Merge point is between the branches and the next block
        if (nextBlock != null) {
            mergePointY = nextBlock.getY() - VERTICAL_SPACING / 2;
        } else {
            int maxBranchEndY = Math.max(trueBranchEndY, falseBranchEndY);
            mergePointY = maxBranchEndY + VERTICAL_SPACING;
        }
    }

    /**
     * Find the Y coordinate of the last block in a branch chain.
     */
    private int findLastBlockY(FlowBlock block) {
        if (block == null) return y + height;

        FlowBlock last = findLastBlock(block);
        return last.getY() + last.getHeight();
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Draw branches first (so they appear behind the diamond)
        drawBranches(g2d);

        // Draw the diamond shape
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();

        // Create diamond path
        Path2D.Double diamond = new Path2D.Double();
        int centerX = x + width / 2;
        int centerY = y + height / 2;

        diamond.moveTo(centerX, y);                    // Top
        diamond.lineTo(x + width, centerY);            // Right
        diamond.lineTo(centerX, y + height);           // Bottom
        diamond.lineTo(x, centerY);                    // Left
        diamond.closePath();

        // Fill diamond
        g2d.setColor(new Color(255, 255, 200));
        g2d.fill(diamond);

        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(diamond);

        // Draw text
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, text, x, y, width, height);

        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);

        // Draw child blocks
        if (trueBranch != null) {
            trueBranch.draw(g2d);
        }
        if (falseBranch != null) {
            falseBranch.draw(g2d);
        }
        if (nextBlock != null) {
            nextBlock.draw(g2d);
        }
    }

    private void drawBranches(Graphics2D g2d) {
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2));

        int centerX = x + width / 2;

        // Draw true branch (right) with orthogonal lines
        g2d.setColor(new Color(0, 150, 0));
        if (trueBranch != null) {
            // Orthogonal connection from diamond to first block
            int startX = x + width;
            int startY = y + height / 2;
            int targetX = trueBranch.getX() + trueBranch.getWidth() / 2;
            int targetY = trueBranch.getY();

            // Right, then down
            g2d.drawLine(startX, startY, startX + 20, startY);
            g2d.drawLine(startX + 20, startY, startX + 20, (startY + targetY) / 2);
            g2d.drawLine(startX + 20, (startY + targetY) / 2, targetX, (startY + targetY) / 2);
            g2d.drawLine(targetX, (startY + targetY) / 2, targetX, targetY);
            drawArrowVertical(g2d, targetX, targetY, true);

            // Label "True"
            g2d.setColor(new Color(0, 100, 0));
            g2d.drawString("True", startX + 5, startY - 5);

            // Line from last block to merge point (orthogonal)
            g2d.setColor(new Color(0, 150, 0));
            drawBranchToMerge(g2d, trueBranch, trueBranchEndY);
        } else {
            // Direct orthogonal line to merge point (empty TRUE branch)
            int startX = x + width;
            int startY = y + height / 2;
            int targetX = centerX + BRANCH_HORIZONTAL_OFFSET;

            // 1. Orizzontale verso destra
            g2d.drawLine(startX, startY, targetX, startY);
            // 2. Verticale giù fino al MERGE POINT (bilanciamento!)
            g2d.drawLine(targetX, startY, targetX, mergePointY);
            // 3. Orizzontale verso centro (al livello del merge point)
            g2d.drawLine(targetX, mergePointY, centerX, mergePointY);

            g2d.setColor(new Color(0, 100, 0));
            g2d.drawString("True", startX + 5, startY - 5);
        }

        // Draw false branch (left) with orthogonal lines
        g2d.setColor(new Color(150, 0, 0));
        if (falseBranch != null) {
            // Orthogonal connection from diamond to first block
            int startX = x;
            int startY = y + height / 2;
            int targetX = falseBranch.getX() + falseBranch.getWidth() / 2;
            int targetY = falseBranch.getY();

            // Left, then down
            g2d.drawLine(startX, startY, startX - 20, startY);
            g2d.drawLine(startX - 20, startY, startX - 20, (startY + targetY) / 2);
            g2d.drawLine(startX - 20, (startY + targetY) / 2, targetX, (startY + targetY) / 2);
            g2d.drawLine(targetX, (startY + targetY) / 2, targetX, targetY);
            drawArrowVertical(g2d, targetX, targetY, true);

            // Label "False"
            g2d.setColor(new Color(100, 0, 0));
            g2d.drawString("False", startX - 45, startY - 5);

            // Line from last block to merge point (orthogonal)
            g2d.setColor(new Color(150, 0, 0));
            drawBranchToMerge(g2d, falseBranch, falseBranchEndY);
        } else {
            // Direct orthogonal line to merge point (empty FALSE branch)
            int startX = x;
            int startY = y + height / 2;
            int targetX = centerX - BRANCH_HORIZONTAL_OFFSET;

            // 1. Orizzontale verso sinistra
            g2d.drawLine(startX, startY, targetX, startY);
            // 2. Verticale giù fino al MERGE POINT (bilanciamento!)
            g2d.drawLine(targetX, startY, targetX, mergePointY);
            // 3. Orizzontale verso centro (al livello del merge point)
            g2d.drawLine(targetX, mergePointY, centerX, mergePointY);

            g2d.setColor(new Color(100, 0, 0));
            g2d.drawString("False", startX - 45, startY - 5);
        }

        // Draw merge point circle
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 6, mergePointY - 6, 12, 12);

        // SEMPRE disegna un trattino verticale dopo il merge point
        int lineStartY = mergePointY + 6;  // Parte dal fondo del cerchio
        int lineEndY = lineStartY + 30;     // Trattino di 30px (più visibile)

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(centerX, lineStartY, centerX, lineEndY);

        // Se c'è un next block, connettilo partendo dal trattino
        if (nextBlock != null) {
            int targetX = nextBlock.getX() + nextBlock.getWidth() / 2;
            int targetY = nextBlock.getY();

            // Connessione ortogonale dal trattino al next block
            if (centerX == targetX) {
                // Se allineati verticalmente, solo linea verticale
                g2d.drawLine(centerX, lineEndY, centerX, targetY);
                drawArrowVertical(g2d, centerX, targetY, true);
            } else {
                // Se non allineati, usa percorso ortogonale
                drawOrthogonalConnection(g2d, centerX, lineEndY, targetX, targetY);
            }
        }

        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    private void drawBranchToMerge(Graphics2D g2d, FlowBlock branch, int branchEndY) {
        // Find the last block in the branch
        FlowBlock lastBlock = findLastBlock(branch);
        int lastBlockCenterX = lastBlock.getX() + lastBlock.getWidth() / 2;
        int lastBlockBottomY = lastBlock.getY() + lastBlock.getHeight();

        int centerX = x + width / 2;

        // Calculate intermediate Y position (scende un po' prima di virare)
        int intermediateY;
        if (lastBlockBottomY < branchEndY) {
            intermediateY = branchEndY + 20; // Anche per i rami corti, aggiungi 20px
        } else {
            intermediateY = lastBlockBottomY + 20; // Scende 20px dal blocco
        }

        // 1. Scende verticalmente dal blocco (20px)
        g2d.drawLine(lastBlockCenterX, lastBlockBottomY, lastBlockCenterX, intermediateY);

        // 2. Va orizzontalmente verso il centro AL LIVELLO DEL MERGE POINT
        // Il merge point è già a questa altezza (intermediateY), quindi linea orizzontale diretta
        g2d.drawLine(lastBlockCenterX, intermediateY, centerX, intermediateY);

        // Note: NO vertical line needed - merge point is AT intermediateY!
    }

    private FlowBlock findLastBlock(FlowBlock block) {
        List<FlowBlock> children = block.getChildren();
        if (children.isEmpty()) {
            return block;
        }
        // For conditional blocks, we need to find the longest branch
        if (block instanceof ConditionalBlock) {
            ConditionalBlock cond = (ConditionalBlock) block;
            if (cond.getNextBlock() != null) {
                return findLastBlock(cond.getNextBlock());
            }
        }
        // For other blocks, return the last child
        return findLastBlock(children.get(children.size() - 1));
    }

    @Override
    public List<FlowBlock> getChildren() {
        List<FlowBlock> children = new ArrayList<>();
        if (trueBranch != null) children.add(trueBranch);
        if (falseBranch != null) children.add(falseBranch);
        if (nextBlock != null) children.add(nextBlock);
        return children;
    }

    @Override
    public List<ConnectionPoint> getConnectionPoints() {
        List<ConnectionPoint> points = new ArrayList<>();

        int centerX = x + width / 2;

        // True branch connection (right side)
        if (trueBranch == null) {
            points.add(new ConnectionPoint(
                x + width - 10, y + height / 2 - 10, 20, 20,
                ConnectionPoint.Type.TRUE_BRANCH, this, "+"
            ));
        }

        // False branch connection (left side)
        if (falseBranch == null) {
            points.add(new ConnectionPoint(
                x - 10, y + height / 2 - 10, 20, 20,
                ConnectionPoint.Type.FALSE_BRANCH, this, "+"
            ));
        }

        // Next block connection (after merge)
        if (nextBlock == null) {
            points.add(new ConnectionPoint(
                centerX - 10, mergePointY + VERTICAL_SPACING / 2 - 10, 20, 20,
                ConnectionPoint.Type.NEXT, this, "+"
            ));
        }

        return points;
    }

    @Override
    public FlowBlock clone() {
        ConditionalBlock cloned = new ConditionalBlock(this.text);
        if (trueBranch != null) cloned.trueBranch = trueBranch.clone();
        if (falseBranch != null) cloned.falseBranch = falseBranch.clone();
        if (nextBlock != null) cloned.nextBlock = nextBlock.clone();
        return cloned;
    }

    // Getters and setters
    public FlowBlock getTrueBranch() {
        return trueBranch;
    }

    public void setTrueBranch(FlowBlock trueBranch) {
        this.trueBranch = trueBranch;
        if (trueBranch != null) {
            trueBranch.setParent(this);
        }
    }

    public FlowBlock getFalseBranch() {
        return falseBranch;
    }

    public void setFalseBranch(FlowBlock falseBranch) {
        this.falseBranch = falseBranch;
        if (falseBranch != null) {
            falseBranch.setParent(this);
        }
    }

    public FlowBlock getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(FlowBlock nextBlock) {
        this.nextBlock = nextBlock;
        if (nextBlock != null) {
            nextBlock.setParent(this);
        }
    }

    public int getMergePointY() {
        return mergePointY;
    }
}
