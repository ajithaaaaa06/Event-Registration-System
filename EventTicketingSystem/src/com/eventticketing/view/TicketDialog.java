package com.eventticketing.view;

import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;
import com.eventticketing.util.TicketFormatter;
import com.eventticketing.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Shows the auto-generated ticket confirmation and lets the organizer save it as a text file. */
public class TicketDialog extends JDialog {

    public TicketDialog(Frame owner, Event event, Participant p) {
        super(owner, "Ticket Confirmation", true);
        String ticket = TicketFormatter.format(event, p);

        JLabel banner = new JLabel("  Booking confirmed!  Ticket " + p.getTicketCode(), SwingConstants.LEFT);
        banner.setOpaque(true);
        banner.setBackground(Ui.GREEN);
        banner.setForeground(Color.WHITE);
        banner.setFont(Ui.FONT_BOLD.deriveFont(15f));
        banner.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));

        JTextArea area = new JTextArea(ticket);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JButton save = Ui.button("Save Ticket (.txt)", Ui.BLUE);
        JButton close = Ui.button("Close", Ui.GREY);
        save.addActionListener(e -> saveTicket(p, ticket));
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(save);
        buttons.add(close);

        setLayout(new BorderLayout());
        add(banner, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setSize(Math.max(getWidth(), 480), getHeight());
        setLocationRelativeTo(owner);
    }

    private void saveTicket(Participant p, String ticket) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(p.getTicketCode() + ".txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(fc.getSelectedFile().toPath(), ticket.getBytes(StandardCharsets.UTF_8));
                Ui.info(this, "Ticket saved to:\n" + fc.getSelectedFile().getAbsolutePath());
            } catch (IOException ex) {
                Ui.error(this, "Could not save the ticket: " + ex.getMessage());
            }
        }
    }
}
