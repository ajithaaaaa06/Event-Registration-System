package com.eventticketing.util;

import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;

import java.math.BigDecimal;

/** Builds the plain-text e-ticket shown (and saved) after a successful registration. */
public final class TicketFormatter {
    private TicketFormatter() { }

    public static String format(Event e, Participant p) {
        String line = "=============================================";
        String thin = "---------------------------------------------";
        StringBuilder sb = new StringBuilder();
        sb.append(line).append('\n');
        sb.append("              EVENT E-TICKET\n");
        sb.append(line).append('\n');
        row(sb, "Ticket ID", p.getTicketCode());
        row(sb, "Event", e.getName());
        row(sb, "Date", Ui.DATE_LONG.format(e.getDate()));
        row(sb, "Venue", e.getVenue());
        row(sb, "Organizer", e.getOrganizer());
        row(sb, "Category", e.getCategory());
        sb.append(thin).append('\n');
        row(sb, "Attendee", p.getName());
        row(sb, "Email", p.getEmail());
        row(sb, "Phone", p.getPhone());
        row(sb, "Seats booked", String.valueOf(p.getSeats()));
        row(sb, "Price / seat", Ui.money(e.getTicketPrice()));
        BigDecimal total = p.getAmountPaid();
        row(sb, "Total amount", Ui.money(total));
        row(sb, "Booked on", Ui.DATE_TIME.format(p.getBookedAt()));
        sb.append(line).append('\n');
        sb.append("Please show this ticket at the venue entrance.\n");
        return sb.toString();
    }

    private static void row(StringBuilder sb, String label, String value) {
        sb.append(String.format("%-14s: %s%n", label, value));
    }
}
