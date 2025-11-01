package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Process block (rectangle) - represents a process/action.
 */
public class ProcessBlock extends FlowBlock {
    private static final long serialVersionUID = 1L;

    private FlowBlock nextBlock;

    public ProcessBlock(String process) {
        super(process);
        this.width = 140;
        this.height = 60;
    }

    @Override
    public int calculateHeight() {
        int myHeight = height;
        if (nextBlock != null) {
            myHeight += VERTICAL_SPACING + nextBlock.calculateHeight();
        }
        return myHeight;
    }

    @Override
    public int calculateWidth() {
        int myWidth = width;
        if (nextBlock != null) {
            myWidth = Math.max(myWidth, nextBlock.calculateWidth());
        }
        return myWidth;
    }

    @Override
    public void layout(int startX, int startY) {
        this.x = startX;
        this.y = startY;

        if (nextBlock != null) {
            nextBlock.layout(startX, startY + height + VERTICAL_SPACING);
            nextBlock.setParent(this);
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();

        // Fill rectangle
        g2d.setColor(new Color(200, 220, 255));
        g2d.fillRect(x, y, width, height);

        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, width, height);

        // Draw text
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, text, x, y, width, height);

        // Draw connection to next block
        if (nextBlock != null) {
            int centerX = x + width / 2;
            g2d.drawLine(centerX, y + height,
                        nextBlock.getX() + nextBlock.getWidth() / 2, nextBlock.getY());
            drawArrow(g2d, centerX, y + height,
                     nextBlock.getX() + nextBlock.getWidth() / 2, nextBlock.getY());

            nextBlock.draw(g2d);
        }

        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    @Override
    public List<FlowBlock> getChildren() {
        List<FlowBlock> children = new ArrayList<>();
        if (nextBlock != null) {
            children.add(nextBlock);
        }
        return children;
    }

    @Override
    public List<ConnectionPoint> getConnectionPoints() {
        List<ConnectionPoint> points = new ArrayList<>();

        if (nextBlock == null) {
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
        ProcessBlock cloned = new ProcessBlock(this.text);
        if (nextBlock != null) {
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
}
