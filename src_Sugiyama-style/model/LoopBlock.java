package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Loop block (hexagon) - represents a loop structure (while, for, etc.).
 * The loop body connects back to the loop condition.
 */
public class LoopBlock extends FlowBlock {
    private static final long serialVersionUID = 1L;

    private FlowBlock loopBody;  // Executed while condition is true
    private FlowBlock nextBlock;  // Executed after loop exits
    private int loopBackY;  // Y coordinate where loop body connects back

    public LoopBlock(String condition) {
        super(condition);
        this.width = 140;
        this.height = 70;
    }

    @Override
    public int calculateHeight() {
        int bodyHeight = loopBody != null ? loopBody.calculateHeight() : 0;
        int myHeight = height + bodyHeight + VERTICAL_SPACING * 2;

        if (nextBlock != null) {
            myHeight += nextBlock.calculateHeight();
        }

        return myHeight;
    }

    @Override
    public int calculateWidth() {
        int bodyWidth = loopBody != null ? loopBody.calculateWidth() : 0;
        int myWidth = Math.max(width, bodyWidth + HORIZONTAL_SPACING);

        if (nextBlock != null) {
            myWidth = Math.max(myWidth, nextBlock.calculateWidth());
        }

        return myWidth;
    }

    @Override
    public void layout(int startX, int startY) {
        this.x = startX;
        this.y = startY;

        if (loopBody != null) {
            loopBody.layout(startX, startY + height + VERTICAL_SPACING);
            loopBody.setParent(this);

            int bodyHeight = loopBody.calculateHeight();
            loopBackY = startY + height + VERTICAL_SPACING + bodyHeight;
        } else {
            loopBackY = startY + height + VERTICAL_SPACING;
        }

        if (nextBlock != null) {
            nextBlock.layout(startX, loopBackY + VERTICAL_SPACING);
            nextBlock.setParent(this);
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();

        // Create hexagon
        int[] xPoints = {
            x + 20,              // Top left
            x + width - 20,      // Top right
            x + width,           // Middle right
            x + width - 20,      // Bottom right
            x + 20,              // Bottom left
            x                    // Middle left
        };
        int[] yPoints = {
            y,
            y,
            y + height / 2,
            y + height,
            y + height,
            y + height / 2
        };

        // Fill hexagon
        g2d.setColor(new Color(220, 255, 220));
        g2d.fillPolygon(xPoints, yPoints, 6);

        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 6);

        // Draw text
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, text, x, y, width, height);

        int centerX = x + width / 2;

        // Draw loop body connection
        if (loopBody != null) {
            g2d.setColor(new Color(0, 150, 0));
            int targetX = loopBody.getX() + loopBody.getWidth() / 2;
            int targetY = loopBody.getY();

            drawOrthogonalConnection(g2d, centerX, y + height, targetX, targetY);

            loopBody.draw(g2d);

            // Draw loop back arrow
            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));

            // Find last block in body
            FlowBlock lastBlock = findLastBlock(loopBody);
            int lastBlockCenterX = lastBlock.getX() + lastBlock.getWidth() / 2;
            int lastBlockBottomY = lastBlock.getY() + lastBlock.getHeight();

            // Draw loop back arrow to the left side
            int loopBackX = x - 30;
            g2d.drawLine(lastBlockCenterX, lastBlockBottomY, lastBlockCenterX, loopBackY);
            g2d.drawLine(lastBlockCenterX, loopBackY, loopBackX, loopBackY);
            g2d.drawLine(loopBackX, loopBackY, loopBackX, y + height / 2);
            g2d.drawLine(loopBackX, y + height / 2, x, y + height / 2);
            drawArrow(g2d, loopBackX, y + height / 2, x, y + height / 2);

            g2d.setStroke(new BasicStroke(2));
            g2d.drawString("Loop", lastBlockCenterX + 5, loopBackY - 5);
        }

        // Draw exit connection
        if (nextBlock != null) {
            g2d.setColor(new Color(150, 0, 0));
            int exitX = x + width;
            g2d.drawLine(exitX, y + height / 2,
                        exitX + 30, y + height / 2);
            g2d.drawLine(exitX + 30, y + height / 2,
                        exitX + 30, nextBlock.getY());
            g2d.drawLine(exitX + 30, nextBlock.getY(),
                        nextBlock.getX() + nextBlock.getWidth() / 2, nextBlock.getY());
            drawArrow(g2d, exitX + 30, nextBlock.getY(),
                     nextBlock.getX() + nextBlock.getWidth() / 2, nextBlock.getY());

            g2d.drawString("Exit", exitX + 5, y + height / 2 - 5);

            nextBlock.draw(g2d);
        }

        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    private FlowBlock findLastBlock(FlowBlock block) {
        List<FlowBlock> children = block.getChildren();
        if (children.isEmpty()) {
            return block;
        }
        return findLastBlock(children.get(children.size() - 1));
    }

    @Override
    public List<FlowBlock> getChildren() {
        List<FlowBlock> children = new ArrayList<>();
        if (loopBody != null) children.add(loopBody);
        if (nextBlock != null) children.add(nextBlock);
        return children;
    }

    @Override
    public List<ConnectionPoint> getConnectionPoints() {
        List<ConnectionPoint> points = new ArrayList<>();

        int centerX = x + width / 2;

        if (loopBody == null) {
            points.add(new ConnectionPoint(
                centerX - 10, y + height - 10, 20, 20,
                ConnectionPoint.Type.LOOP_BODY, this, "+"
            ));
        }

        if (nextBlock == null) {
            points.add(new ConnectionPoint(
                x + width - 10, y + height / 2 - 10, 20, 20,
                ConnectionPoint.Type.NEXT, this, "+"
            ));
        }

        return points;
    }

    @Override
    public FlowBlock clone() {
        LoopBlock cloned = new LoopBlock(this.text);
        if (loopBody != null) cloned.loopBody = loopBody.clone();
        if (nextBlock != null) cloned.nextBlock = nextBlock.clone();
        return cloned;
    }

    // Getters and setters
    public FlowBlock getLoopBody() {
        return loopBody;
    }

    public void setLoopBody(FlowBlock loopBody) {
        this.loopBody = loopBody;
        if (loopBody != null) {
            loopBody.setParent(this);
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
}
