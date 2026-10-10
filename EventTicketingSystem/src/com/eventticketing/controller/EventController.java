package com.eventticketing.controller;

import com.eventticketing.dao.EventRepository;
import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;
import com.eventticketing.util.AppException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Controller: validation and business rules between the Swing views and the repository. */
public class EventController {

    public static final List<String> CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            "Technical", "Cultural", "Workshop", "Music", "Sports", "Seminar", "Conference", "Other"));

    public static final int MAX_TICKETS_PER_BOOKING = 10;

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9 \\-]{5,18}[0-9]$");

    private final EventRepository repo;

    public EventController(EventRepository repo) {
        this.repo = repo;
    }

    // ------------------------------------------------------------------ queries

    public List<Event> getEvents() {
        return repo.findAll();
    }

    public List<Event> search(String keyword, String category) {
        return repo.search(keyword, category);
    }

    public Event getEvent(int id) {
        Event e = repo.findById(id);
        if (e == null) throw new AppException("This event no longer exists. Please refresh the list.");
        return e;
    }

    public List<Participant> getParticipants(int eventId) {
        return repo.getParticipants(eventId);
    }

    public boolean isDatabase() {
        return repo.isDatabase();
    }

    public String getStorageName() {
        return repo.getStorageName();
    }

    public String getStorageNote() {
        return repo.getNote();
    }

    // ----------------------------------------------------------------- commands

    public void addEvent(Event e) {
        validateEvent(e, null);
        repo.addEvent(e);
    }

    public void updateEvent(Event e) {
        Event existing = getEvent(e.getId());
        validateEvent(e, existing);
        repo.updateEvent(e);
    }

    public void deleteEvent(int id) {
        repo.deleteEvent(id);
    }

    public Participant register(int eventId, String name, String email, String phone, int seats) {
        Event ev = getEvent(eventId);
        if (ev.isPast()) throw new AppException("Registration is closed - this event has already taken place.");
        if (ev.isSoldOut()) throw new AppException("Sorry, this event is sold out.");

        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim();
        phone = phone == null ? "" : phone.trim();
        if (name.length() < 2) throw new AppException("Please enter the attendee's full name.");
        if (!EMAIL.matcher(email).matches()) throw new AppException("Please enter a valid email address.");
        if (!PHONE.matcher(phone).matches()) throw new AppException("Please enter a valid phone number (7-15 digits).");
        if (seats < 1) throw new AppException("Book at least one ticket.");
        if (seats > MAX_TICKETS_PER_BOOKING) {
            throw new AppException("You can book at most " + MAX_TICKETS_PER_BOOKING + " tickets at a time.");
        }
        if (seats > ev.getAvailableSeats()) {
            throw new AppException("Only " + ev.getAvailableSeats() + " seat(s) are still available.");
        }

        Participant p = new Participant();
        p.setEventId(eventId);
        p.setName(name);
        p.setEmail(email);
        p.setPhone(phone);
        p.setSeats(seats);
        return repo.register(p); // the repository re-checks availability inside a transaction
    }

    // --------------------------------------------------------------- validation

    /** @param existing the stored version when editing, or null when adding a new event */
    public void validateEvent(Event e, Event existing) {
        require(e.getName(), "Event name");
        require(e.getVenue(), "Venue");
        require(e.getOrganizer(), "Organizer name");
        require(e.getCategory(), "Event category");
        if (e.getName().length() > 255 || e.getVenue().length() > 255 || e.getOrganizer().length() > 255) {
            throw new AppException("Name, venue and organizer must be 255 characters or fewer.");
        }
        if (e.getCategory().length() > 100) throw new AppException("Category must be 100 characters or fewer.");
        if (e.getDate() == null) throw new AppException("Please choose the event date.");

        boolean dateChanged = existing == null || !e.getDate().equals(existing.getDate());
        if (dateChanged && e.getDate().isBefore(LocalDate.now())) {
            throw new AppException("Event date cannot be in the past.");
        }
        if (e.getTicketPrice() == null || e.getTicketPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException("Ticket price cannot be negative.");
        }
        if (e.getTicketPrice().compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new AppException("Ticket price is too large.");
        }
        if (e.getMaxSeats() < 1) throw new AppException("Maximum seats must be at least 1.");
        if (existing != null && e.getMaxSeats() < existing.getBookedSeats()) {
            throw new AppException("Seats cannot be reduced below the " + existing.getBookedSeats()
                    + " seat(s) already booked.");
        }
        // Scheduling rule: one event per venue per day.
        for (Event other : repo.findAll()) {
            if (other.getId() != e.getId()
                    && other.getDate().equals(e.getDate())
                    && other.getVenue().trim().equalsIgnoreCase(e.getVenue().trim())) {
                throw new AppException("\"" + other.getName() + "\" is already scheduled at this venue on "
                        + e.getDate() + ". Choose another date or venue.");
            }
        }
    }

    private static void require(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new AppException(field + " is required.");
        }
    }
}
