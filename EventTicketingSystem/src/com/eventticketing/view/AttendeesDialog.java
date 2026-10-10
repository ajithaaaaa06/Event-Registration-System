package com.eventticketing.view;

import com.eventticketing.controller.EventController;
import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;
import com.eventticketing.util.Ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/** Attendee report for one event, with CSV export. */
public class AttendeesDialog extends JDialog {

    private final List<Participant> attendees;
    private final Event event;

    public AttendeesDialog(Frame owner, EventController controller, Event event) {
        super(owner, "Attendees - " + event.getName(), true);
        this.event = event;
        this.attendees = controller.getParticipants(event.getId());

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Ticket ID", "Name", "Email", "Phone", "Seats", "Amount", "Booked On"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        int seats = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        for (Participant p : attendees) {
            model.addRow(new Object[]{p.getTicketCode(), p.getName(), p.getEmail(), p.getPhone(),
                    p.getSeats(), Ui.money(p.getAmountPaid()), Ui.DATE_TIME.format(p.getBookedAt())});
            seats += p.getSeats();
            revenue = revenue.add(p.getAmountPaid());
        }
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(Ui.FONT_BOLD);

        JLabel summary = new JLabel(attendees.size() + " booking(s)   \u2022   " + seats + " seat(s)   \u2022   Revenue "
                + Ui.money(revenue));
        summary.setFont(Ui.FONT_BOLD);
        summary.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JButton export = Ui.button("Export CSV", Ui.BLUE);
        JButton close = Ui.button("Close", Ui.GREY);
        export.setEnabled(!attendees.isEmpty());
        export.addActionListener(e -> exportCsv());
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(export);
        buttons.add(close);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(summary, BorderLayout.WEST);
        bottom.add(buttons, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setSize(820, 420);
        setLocationRelativeTo(owner);
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("attendees-event-" + event.getId() + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        StringBuilder sb = new StringBuilder("Ticket ID,Name,Email,Phone,Seats,Amount Paid,Booked On\n");
        for (Participant p : attendees) {
            sb.append(csv(p.getTicketCode())).append(',').append(csv(p.getName())).append(',')
              .append(csv(p.getEmail())).append(',').append(csv(p.getPhone())).append(',')
              .append(p.getSeats()).append(',').append(p.getAmountPaid().toPlainString()).append(',')
              .append(csv(Ui.DATE_TIME.format(p.getBookedAt()))).append('\n');
        }
        try {
            Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            Ui.info(this, "Report saved to:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (IOException ex) {
            Ui.error(this, "Could not save the report: " + ex.getMessage());
        }
    }

    /** Quotes a CSV field and neutralises spreadsheet formulas (=, +, -, @). */
    private static String csv(String s) {
        if (s == null) return "";
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
