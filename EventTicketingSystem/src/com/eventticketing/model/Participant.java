package com.eventticketing.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Model: one registration (booking) of an attendee for an event. */
public class Participant implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int eventId;
    private String name;
    private String email;
    private String phone;
    private int seats = 1;
    private BigDecimal amountPaid = BigDecimal.ZERO;
    private LocalDateTime bookedAt;

    public Participant() { }

    public Participant(Participant o) {
        this.id = o.id;
        this.eventId = o.eventId;
        this.name = o.name;
        this.email = o.email;
        this.phone = o.phone;
        this.seats = o.seats;
        this.amountPaid = o.amountPaid;
        this.bookedAt = o.bookedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }

    /** Human-friendly ticket number, e.g. TKT-0003-00012. */
    public String getTicketCode() {
        return String.format("TKT-%04d-%05d", eventId, id);
    }
}
