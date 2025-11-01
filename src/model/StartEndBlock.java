package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Start/End block (rounded rectangle/oval) - represents start or end of flowchart.
 */
public class StartEndBlock extends FlowBlock {
    private static final long serialVersionUID = 1L;

    private FlowBlock nextBlock;
    private boolean isStart;  // true for Start, false for End

    public StartEndBlock(String text, boolean isStart) {
        super(text);
        this.isStart = isStart;
        this.width = 120;
        this.height = 50;
    }

    @Override
    public int calculateHeight() {
        int myHeight = height;
        if (nextBlock != null && isStart) {
            myHeight += VERTICAL_SPACING + nextBlock.calculateHeight();
        }
        return myHeight;
    }

    @Override
    public int calculateWidth() {
        int myWidth = width;
        if (nextBlock != null && isStart) {
            myWidth = Math.max(myWidth, nextBlock.calculateWidth());
        }
        return myWidth;
    }

    @Override
    public void layout(int startX, int startY) {
        this.x = startX;
        this.y = startY;

        if (nextBlock != null && isStart) {
            nextBlock.layout(startX, startY + height + VERTICAL_SPACING);
            nextBlock.setParent(this);
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();

        // Fill rounded rectangle
        if (isStart) {
            g2d.setColor(new Color(200, 255, 200));
        } else {
            g2d.setColor(new Color(255, 200, 200));
        }
        g2d.fillRoundRect(x, y, width, height, 40, 40);

        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 40, 40);

        // Draw text
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, text, x, y, width, height);

        // Draw connection to next block (only for Start)
        if (nextBlock != null && isStart) {
            int centerX = x + width / 2;
            int targetX = nextBlock.getX() + nextBlock.getWidth() / 2;
            int targetY = nextBlock.getY();

            drawOrthogonalConnection(g2d, centerX, y + height, targetX, targetY);

            nextBlock.draw(g2d);
        }

        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    @Override
    public List<FlowBlock> getChildren() {
        List<FlowBlock> children = new ArrayList<>();
        if (nextBlock != null && isStart) {
            children.add(nextBlock);
        }
        return children;
    }

    @Override
    public List<ConnectionPoint> getConnectionPoints() {
        List<ConnectionPoint> points = new ArrayList<>();

        if (nextBlock == null && isStart) {
            int centerX = x + width / 2;
            points.add(new ConnectionPoint(
                centerX - 10, y + height - 10, 20, 20,
                ConnectionPoint.Type.NEXT, this, "+"
            ));
        }

        return points;
    }

    @Override
    public FlowBlock clone() {
        StartEndBlock cloned = new StartEndBlock(this.text, this.isStart);
        if (nextBlock != null && isStart) {
            cloned.nextBlock = nextBlock.clone();
        }
        return cloned;
    }

    // Getters and setters
    public FlowBlock getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(FlowBlock nextBlock) {
        this.nextBlock = nextBlock;
        if (nextBlock != null) {
            nextBlock.setParent(this);
        }
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }
}
