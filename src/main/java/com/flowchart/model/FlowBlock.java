package com.flowchart.model;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for all flowchart blocks.
 * Implements recursive height calculation and layout positioning.
 */
public abstract class FlowBlock implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected int x, y;  // Absolute position
    protected int width, height;  // Block dimensions
    protected transient FlowBlock parent;
    protected String text;  // Text content of the block

    // Constants for layout
    protected static final int DEFAULT_WIDTH = 120;
    protected static final int DEFAULT_HEIGHT = 60;
    protected static final int VERTICAL_SPACING = 40;
    protected static final int HORIZONTAL_SPACING = 80;

    public FlowBlock(String text) {
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    /**
     * Calculate the total height of this block and all its children.
     * This is critical for auto-balancing branches.
     */
    public abstract int calculateHeight();

    /**
     * Calculate the total width of this block and all its children.
     */
    public abstract int calculateWidth();

    /**
     * Position this block and all its children starting from (startX, startY).
     * This method implements the recursive layout algorithm.
     */
    public abstract void layout(int startX, int startY);

    /**
     * Get all child blocks (used for traversal and serialization).
     */
    public abstract List<FlowBlock> getChildren();

    /**
     * Draw this block and its connections.
     */
    public abstract void draw(Graphics2D g2d);

    /**
     * Check if a point is inside this block (for mouse clicks).
     */
    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    /**
     * Get clickable areas for adding new blocks.
     * Returns list of rectangles representing connection points.
     */
    public abstract List<ConnectionPoint> getConnectionPoints();

    /**
     * Clone this block (for copy operations).
     */
    public abstract FlowBlock clone();

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public FlowBlock getParent() {
        return parent;
    }

    public void setParent(FlowBlock parent) {
        this.parent = parent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Draw text centered in a rectangle.
     */
    protected void drawCenteredText(Graphics2D g2d, String text, int x, int y, int width, int height) {
        FontMetrics fm = g2d.getFontMetrics();

        // Handle multi-line text
        String[] lines = text.split("\n");
        int totalHeight = lines.length * fm.getHeight();
        int startY = y + (height - totalHeight) / 2 + fm.getAscent();

        for (String line : lines) {
            int textWidth = fm.stringWidth(line);
            int textX = x + (width - textWidth) / 2;
            g2d.drawString(line, textX, startY);
            startY += fm.getHeight();
        }
    }

    /**
     * Draw an arrow at the end of a line.
     */
    protected void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        int arrowSize = 8;

        // Calculate arrow angle
        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Arrow head points
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = x2;
        yPoints[0] = y2;

        xPoints[1] = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        yPoints[1] = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));

        xPoints[2] = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        yPoints[2] = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + text + "]";
    }
}
