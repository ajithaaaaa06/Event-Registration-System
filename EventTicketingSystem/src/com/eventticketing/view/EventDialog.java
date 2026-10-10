package com.eventticketing.view;

import com.eventticketing.controller.EventController;
import com.eventticketing.model.Event;
import com.eventticketing.util.AppException;
import com.eventticketing.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.LocalDate;

/** Add / Edit dialog for an event. getResult() is null when the user cancelled. */
public class EventDialog extends JDialog {

    private final EventController controller;
    private final Event editing;
    private Event result;

    private final JTextField nameField = new JTextField(26);
    private final JTextField venueField = new JTextField(26);
    private final JTextField organizerField = new JTextField(26);
    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(500.0, 0.0, 99999999.0, 50.0));
    private final JSpinner seatsSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 1000000, 10));
    private final DatePickerField datePicker;

    public EventDialog(Frame owner, EventController controller, Event editing) {
        super(owner, editing == null ? "Add New Event" : "Edit Event", true);
        this.controller = controller;
        this.editing = editing;
        this.datePicker = new DatePickerField(editing == null ? LocalDate.now().plusDays(7) : editing.getDate());

        for (String c : EventController.CATEGORIES) categoryBox.addItem(c);
        categoryBox.setEditable(true);
        priceSpinner.setEditor(new JSpinner.NumberEditor(priceSpinner, "#,##0.00"));
        seatsSpinner.setEditor(new JSpinner.NumberEditor(seatsSpinner, "#,##0"));

        if (editing != null) {
            nameField.setText(editing.getName());
            venueField.setText(editing.getVenue());
            organizerField.setText(editing.getOrganizer());
            categoryBox.setSelectedItem(editing.getCategory());
            priceSpinner.setValue(editing.getTicketPrice().doubleValue());
            seatsSpinner.setValue(editing.getMaxSeats());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 8, 18));
        int r = 0;
        addRow(form, r++, "Event Name *", nameField);
        addRow(form, r++, "Event Date *", datePicker);
        addRow(form, r++, "Venue *", venueField);
        addRow(form, r++, "Organizer Name *", organizerField);
        addRow(form, r++, "Ticket Price (\u20B9) *", priceSpinner);
        addRow(form, r++, "Maximum Seats *", seatsSpinner);
        addRow(form, r++, "Event Category *", categoryBox);
        if (editing != null) {
            JLabel note = new JLabel("Already booked: " + editing.getBookedSeats()
                    + " seat(s). Maximum seats cannot go below this.");
            note.setForeground(Ui.GREY);
            addRow(form, r, "", note);
        }

        JButton save = Ui.button("Save", Ui.GREEN);
        JButton cancel = Ui.button("Cancel", Ui.GREY);
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        buttons.add(cancel);
        buttons.add(save);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
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

    private void onSave() {
        try {
            priceSpinner.commitEdit();
            seatsSpinner.commitEdit();
        } catch (ParseException ex) {
            Ui.error(this, "Please enter a valid number for price and seats.");
            return;
        }
        Event e = new Event();
        if (editing != null) {
            e.setId(editing.getId());
            e.setBookedSeats(editing.getBookedSeats());
        }
        e.setName(nameField.getText().trim());
        e.setDate(datePicker.getDate());
        e.setVenue(venueField.getText().trim());
        e.setOrganizer(organizerField.getText().trim());
        e.setTicketPrice(BigDecimal.valueOf(((Number) priceSpinner.getValue()).doubleValue())
                .setScale(2, RoundingMode.HALF_UP));
        e.setMaxSeats(((Number) seatsSpinner.getValue()).intValue());
        Object cat = categoryBox.getEditor().getItem();
        e.setCategory(cat == null ? "" : cat.toString().trim());
        try {
            controller.validateEvent(e, editing);
            result = e;
            dispose();
        } catch (AppException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    public Event getResult() {
        return result;
    }
}
