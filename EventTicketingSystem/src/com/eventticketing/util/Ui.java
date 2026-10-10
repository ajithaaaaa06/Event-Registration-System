package com.eventticketing.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Shared look & feel helpers: colours, fonts, flat rounded buttons, stat cards, message dialogs. */
public final class Ui {
    private Ui() { }

    public static final Color NAVY = new Color(0x1E293B);
    public static final Color BLUE = new Color(0x2563EB);
    public static final Color GREEN = new Color(0x16A34A);
    public static final Color RED = new Color(0xDC2626);
    public static final Color AMBER = new Color(0xD97706);
    public static final Color PURPLE = new Color(0x7C3AED);
    public static final Color GREY = new Color(0x64748B);
    public static final Color BG = new Color(0xF1F5F9);
    public static final Color BORDER = new Color(0xE2E8F0);

    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = FONT.deriveFont(Font.BOLD);

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    public static final DateTimeFormatter DATE_LONG = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    public static String money(BigDecimal v) {
        return "\u20B9" + String.format(Locale.ENGLISH, "%,.2f", v == null ? BigDecimal.ZERO : v);
    }

    public static String html(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ------------------------------------------------------------- components

    public static JButton button(String text, Color color) {
        return new StyledButton(text, color);
    }

    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD);
        l.setForeground(NAVY);
        return l;
    }

    public static Border cardBorder(int pad) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(pad, pad, pad, pad));
    }

    private static class StyledButton extends JButton {
        private final Color base;

        StyledButton(String text, Color base) {
            super(text);
            this.base = base;
            setUI(new BasicButtonUI());
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(FONT_BOLD);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c;
            if (!isEnabled()) c = new Color(0xCBD5E1);
            else if (getModel().isPressed()) c = base.darker().darker();
            else if (getModel().isRollover()) c = base.darker();
            else c = base;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public void updateUI() {
            // keep our flat BasicButtonUI whatever look & feel is active
        }
    }

    /** A small dashboard tile: caption on top, big number below, coloured stripe on the left. */
    public static class StatCard extends JPanel {
        private final JLabel value = new JLabel("0");

        public StatCard(String title, Color accent) {
            super(new BorderLayout(0, 4));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                            BorderFactory.createEmptyBorder(10, 14, 10, 14))));
            JLabel t = new JLabel(title.toUpperCase());
            t.setFont(FONT.deriveFont(Font.BOLD, 11f));
            t.setForeground(GREY);
            value.setFont(FONT.deriveFont(Font.BOLD, 22f));
            value.setForeground(NAVY);
            add(t, BorderLayout.NORTH);
            add(value, BorderLayout.CENTER);
        }

        public void setValue(String text) {
            value.setText(text);
        }
    }

    // ---------------------------------------------------------------- dialogs

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
