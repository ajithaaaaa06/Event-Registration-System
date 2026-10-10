package com.eventticketing.view;

import com.eventticketing.controller.EventController;
import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;
import com.eventticketing.util.AppException;
import com.eventticketing.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;

/** Registers an attendee for the selected event. getResult() holds the booking (null if cancelled). */
public class RegisterDialog extends JDialog {

    private final EventController controller;
    private final Event event;
    private Participant result;

    private final JTextField nameField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JSpinner ticketsSpinner;
    private final JLabel totalLabel = new JLabel();

    public RegisterDialog(Frame owner, EventController controller, Event event) {
        super(owner, "Register Participant", true);
        this.controller = controller;
        this.event = event;

        int maxTickets = Math.max(1, Math.min(event.getAvailableSeats(), EventController.MAX_TICKETS_PER_BOOKING));
        ticketsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, maxTickets, 1));

        JLabel info = new JLabel("<html><div style='width:330px'><b style='font-size:13px'>" + Ui.html(event.getName())
                + "</b><br>" + Ui.DATE_LONG.format(event.getDate()) + "  \u2022  " + Ui.html(event.getVenue())
                + "<br>" + Ui.money(event.getTicketPrice()) + " per ticket  \u2022  "
                + event.getAvailableSeats() + " seat(s) available</div></html>");
        JPanel infoBox = new JPanel(new BorderLayout());
        infoBox.setBackground(new Color(0xEFF6FF));
        infoBox.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        infoBox.add(info);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 18, 6, 18));
        addRow(form, 0, "Attendee Name *", nameField);
        addRow(form, 1, "Email *", emailField);
        addRow(form, 2, "Phone *", phoneField);
        addRow(form, 3, "Number of Tickets *", ticketsSpinner);
        totalLabel.setFont(Ui.FONT_BOLD.deriveFont(15f));
        totalLabel.setForeground(Ui.GREEN);
        addRow(form, 4, "Total Amount", totalLabel);
        ticketsSpinner.addChangeListener(e -> updateTotal());
        updateTotal();

        JButton confirm = Ui.button("Confirm Booking", Ui.GREEN);
        JButton cancel = Ui.button("Cancel", Ui.GREY);
        confirm.addActionListener(e -> onConfirm());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        buttons.add(cancel);
        buttons.add(confirm);

        setLayout(new BorderLayout());
        add(infoBox, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(confirm);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel p, int row, String label, JComponent field) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(6, 0, 6, 12);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        p.add(Ui.formLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);
        p.add(field, gc);
    }

    private void updateTotal() {
        int n = ((Number) ticketsSpinner.getValue()).intValue();
        totalLabel.setText(Ui.money(event.getTicketPrice().multiply(BigDecimal.valueOf(n))));
    }

    private void onConfirm() {
        try {
            ticketsSpinner.commitEdit();
        } catch (ParseException ex) {
            Ui.error(this, "Please enter a valid number of tickets.");
            return;
        }
        try {
            result = controller.register(event.getId(), nameField.getText(), emailField.getText(),
                    phoneField.getText(), ((Number) ticketsSpinner.getValue()).intValue());
            dispose();
        } catch (AppException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    public Participant getResult() {
        return result;
    }
}
