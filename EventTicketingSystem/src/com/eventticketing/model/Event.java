package com.eventticketing.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Model: one row of the "events" table (plus the computed number of booked seats). */
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private LocalDate date;
    private String venue;
    private String organizer;
    private BigDecimal ticketPrice = BigDecimal.ZERO;
    private int maxSeats;
    private String category;
    private int bookedSeats; // computed from participants, not stored in the events table

    public Event() { }

    public Event(String name, LocalDate date, String venue, String organizer,
                 BigDecimal ticketPrice, int maxSeats, String category) {
        this.name = name;
        this.date = date;
        this.venue = venue;
        this.organizer = organizer;
        this.ticketPrice = ticketPrice;
        this.maxSeats = maxSeats;
        this.category = category;
    }

    public Event(Event o) {
        this.id = o.id;
        this.name = o.name;
        this.date = o.date;
        this.venue = o.venue;
        this.organizer = o.organizer;
        this.ticketPrice = o.ticketPrice;
        this.maxSeats = o.maxSeats;
        this.category = o.category;
        this.bookedSeats = o.bookedSeats;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
    public int getMaxSeats() { return maxSeats; }
    public void setMaxSeats(int maxSeats) { this.maxSeats = maxSeats; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(int bookedSeats) { this.bookedSeats = bookedSeats; }

    public int getAvailableSeats() { return Math.max(0, maxSeats - bookedSeats); }
    public boolean isSoldOut() { return getAvailableSeats() == 0; }
    public boolean isPast() { return date != null && date.isBefore(LocalDate.now()); }

    public String getStatus() {
        if (isPast()) return "Completed";
        if (isSoldOut()) return "Sold Out";
        return "Open";
    }
}
