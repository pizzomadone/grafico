package model;

import java.awt.*;
import java.io.Serializable;

/**
 * Represents a clickable connection point where new blocks can be added.
 */
public class ConnectionPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        NEXT,           // Normal sequential flow
        TRUE_BRANCH,    // True branch of conditional
        FALSE_BRANCH,   // False branch of conditional
        LOOP_BODY       // Body of a loop
    }

    private Rectangle bounds;
    private Type type;
    private FlowBlock owner;
    private String label;  // Optional label (e.g., "True", "False")

    public ConnectionPoint(Rectangle bounds, Type type, FlowBlock owner, String label) {
        this.bounds = bounds;
        this.type = type;
        this.owner = owner;
        this.label = label;
    }

    public ConnectionPoint(int x, int y, int width, int height, Type type, FlowBlock owner, String label) {
        this(new Rectangle(x, y, width, height), type, owner, label);
    }

    public boolean contains(int x, int y) {
        return bounds.contains(x, y);
    }

    public void draw(Graphics2D g2d, boolean highlighted) {
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();

        if (highlighted) {
            g2d.setColor(new Color(0, 255, 0, 100));
            g2d.fill(bounds);
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(2));
        } else {
            g2d.setColor(new Color(100, 100, 255, 50));
            g2d.fill(bounds);
            g2d.setColor(new Color(100, 100, 255, 150));
        }

        g2d.draw(bounds);

        // Draw label if present
        if (label != null && highlighted) {
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textX = bounds.x + (bounds.width - textWidth) / 2;
            int textY = bounds.y + (bounds.height + fm.getAscent()) / 2;
            g2d.drawString(label, textX, textY);
        }

        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    // Getters and setters
    public Rectangle getBounds() {
        return bounds;
    }

    public Type getType() {
        return type;
    }

    public FlowBlock getOwner() {
        return owner;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
