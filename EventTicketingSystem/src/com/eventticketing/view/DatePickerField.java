package com.eventticketing.view;

import com.eventticketing.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/** Date picker: a read-only text field plus a pop-up month calendar (no external library needed). */
public class DatePickerField extends JPanel {

    private LocalDate date;
    private final JTextField display = new JTextField(12);

    public DatePickerField(LocalDate initial) {
        super(new BorderLayout(6, 0));
        setOpaque(false);
        display.setEditable(false);
        display.setBackground(Color.WHITE);
        display.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton pick = Ui.button("Pick Date", Ui.BLUE);
        add(display, BorderLayout.CENTER);
        add(pick, BorderLayout.EAST);
        setDate(initial);
        pick.addActionListener(e -> openPopup());
        display.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openPopup(); }
        });
    }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate d) {
        this.date = d;
        display.setText(Ui.DATE.format(d));
    }

    private void openPopup() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Select Date", Dialog.ModalityType.APPLICATION_MODAL);
        YearMonth[] shown = {YearMonth.from(date)};

        JLabel title = new JLabel("", SwingConstants.CENTER);
        title.setFont(Ui.FONT_BOLD.deriveFont(15f));
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        JPanel header = new JPanel(new BorderLayout());
        header.add(prev, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(7, 7, 3, 3));
        Runnable[] rebuild = new Runnable[1];
        rebuild[0] = () -> {
            grid.removeAll();
            YearMonth ym = shown[0];
            title.setText(ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + ym.getYear());
            for (DayOfWeek d : DayOfWeek.values()) {
                JLabel l = new JLabel(d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), SwingConstants.CENTER);
                l.setForeground(Ui.GREY);
                grid.add(l);
            }
            int offset = ym.atDay(1).getDayOfWeek().getValue() - 1; // Monday = 0
            int cells = 0;
            for (int i = 0; i < offset; i++, cells++) grid.add(new JLabel());
            for (int day = 1; day <= ym.lengthOfMonth(); day++, cells++) {
                LocalDate d = ym.atDay(day);
                boolean selected = d.equals(date);
                boolean today = d.equals(LocalDate.now());
                JButton b = Ui.button(String.valueOf(day),
                        selected ? Ui.BLUE : today ? new Color(0xFDE68A) : new Color(0xE2E8F0));
                b.setForeground(selected ? Color.WHITE : Ui.NAVY);
                b.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
                b.addActionListener(e -> {
                    setDate(d);
                    dialog.dispose();
                });
                grid.add(b);
            }
            for (; cells < 42; cells++) grid.add(new JLabel());
            grid.revalidate();
            grid.repaint();
        };
        prev.addActionListener(e -> { shown[0] = shown[0].minusMonths(1); rebuild[0].run(); });
        next.addActionListener(e -> { shown[0] = shown[0].plusMonths(1); rebuild[0].run(); });
        rebuild[0].run();

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(header, BorderLayout.NORTH);
        root.add(grid, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
