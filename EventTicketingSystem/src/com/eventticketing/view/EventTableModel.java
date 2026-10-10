package com.eventticketing.view;

import com.eventticketing.model.Event;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Read-only table model that feeds the events JTable. */
public class EventTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"ID", "Event Name", "Date", "Venue", "Organizer",
            "Category", "Ticket Price", "Seats", "Booked", "Available", "Status"};

    private List<Event> events = new ArrayList<>();

    public void setEvents(List<Event> events) {
        this.events = new ArrayList<>(events);
        fireTableDataChanged();
    }

    public Event getEventAt(int modelRow) {
        return events.get(modelRow);
    }

    @Override public int getRowCount() { return events.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int c) { return COLUMNS[c]; }
    @Override public boolean isCellEditable(int r, int c) { return false; }

    @Override
    public Class<?> getColumnClass(int c) {
        switch (c) {
            case 0: case 7: case 8: case 9: return Integer.class;
            case 2: return LocalDate.class;
            case 6: return BigDecimal.class;
            default: return String.class;
        }
    }

    @Override
    public Object getValueAt(int r, int c) {
        Event e = events.get(r);
        switch (c) {
            case 0: return e.getId();
            case 1: return e.getName();
            case 2: return e.getDate();
            case 3: return e.getVenue();
            case 4: return e.getOrganizer();
            case 5: return e.getCategory();
            case 6: return e.getTicketPrice();
            case 7: return e.getMaxSeats();
            case 8: return e.getBookedSeats();
            case 9: return e.getAvailableSeats();
            default: return e.getStatus();
        }
    }
}
