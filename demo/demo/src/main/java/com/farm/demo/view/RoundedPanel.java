package com.farm.demo.view;

import javax.swing.*;
import java.awt.*;

/**
 * A reusable JPanel with rounded corners.
 * Uses Graphics2D fillRoundRect for smooth rendering.
 */
public class RoundedPanel extends JPanel {
    private Color backgroundColor;
    private int cornerRadius;

    public RoundedPanel(Color backgroundColor, int cornerRadius) {
        this.backgroundColor = backgroundColor;
        this.cornerRadius = cornerRadius;
        setOpaque(false);
    }

    public RoundedPanel(Color backgroundColor) {
        this(backgroundColor, 18);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        g2d.dispose();
        super.paintComponent(g);
    }
}