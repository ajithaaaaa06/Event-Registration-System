package com.eventticketing.view;

import com.eventticketing.util.Ui;

import javax.swing.*;
import java.awt.*;

/** A slim progress bar showing how full an event is (green / amber / red). */
public class SeatBar extends JComponent {
    private double fraction;
    private Color color = Ui.GREY;

    public SeatBar() {
        setPreferredSize(new Dimension(100, 18));
    }

    public void setFill(double fraction, Color color) {
        this.fraction = Math.max(0, Math.min(1, fraction));
        this.color = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(0xE2E8F0));
        g2.fillRoundRect(0, 0, w, h, h, h);
        int fill = (int) Math.round(w * fraction);
        if (fill > 0) {
            g2.setColor(color);
            g2.fillRoundRect(0, 0, Math.max(fill, h), h, h, h);
        }
        g2.dispose();
    }
}
