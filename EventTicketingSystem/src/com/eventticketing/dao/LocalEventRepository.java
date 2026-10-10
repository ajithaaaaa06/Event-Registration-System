package com.eventticketing.dao;

import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;
import com.eventticketing.util.AppException;
import com.eventticketing.util.SampleData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fallback storage used when MySQL is not reachable, so the dashboard always opens.
 * Data is kept in a single file (data/events.dat) and survives restarts.
 */
public class LocalEventRepository implements EventRepository {

    private static class Store implements Serializable {
        private static final long serialVersionUID = 1L;
        List<Event> events = new ArrayList<>();
        List<Participant> participants = new ArrayList<>();
        int nextEventId = 1;
        int nextParticipantId = 1;
    }

    private final Path file;
    private final String note;
    private Store store;

    public LocalEventRepository(Path file, String note) {
        this.file = file;
        this.note = note;
        boolean fresh = !Files.exists(file);
        this.store = fresh ? new Store() : load();
        if (fresh) {
            SampleData.seed(this);
        }
    }

    private Store load() {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            return (Store) in.readObject();
        } catch (Exception e) {
            return new Store();
        }
    }

    private void save() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
                out.writeObject(store);
            }
        } catch (IOException e) {
            throw new AppException("Could not save data file: " + e.getMessage(), e);
        }
    }

    private int booked(int eventId) {
        return store.participants.stream()
                .filter(p -> p.getEventId() == eventId)
                .mapToInt(Participant::getSeats).sum();
    }

    private Event view(Event e) {
        Event copy = new Event(e);
        copy.setBookedSeats(booked(e.getId()));
        return copy;
    }

    private Event raw(int id) {
        for (Event e : store.events) if (e.getId() == id) return e;
        return null;
    }

    @Override
    public synchronized List<Event> search(String keyword, String category) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        String cat = category == null ? "" : category.trim();
        return store.events.stream()
                .filter(e -> k.isEmpty()
                        || e.getName().toLowerCase().contains(k)
                        || e.getVenue().toLowerCase().contains(k)
                        || e.getOrganizer().toLowerCase().contains(k))
                .filter(e -> cat.isEmpty() || e.getCategory().equalsIgnoreCase(cat))
                .sorted(Comparator.comparing(Event::getDate).thenComparing(Event::getName))
                .map(this::view)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized Event findById(int id) {
        Event e = raw(id);
        return e == null ? null : view(e);
    }

    @Override
    public synchronized int addEvent(Event e) {
        Event copy = new Event(e);
        copy.setId(store.nextEventId++);
        copy.setBookedSeats(0);
        store.events.add(copy);
        save();
        return copy.getId();
    }

    @Override
    public synchronized void updateEvent(Event e) {
        Event target = raw(e.getId());
        if (target == null) throw new AppException("Event no longer exists.");
        target.setName(e.getName());
        target.setDate(e.getDate());
        target.setVenue(e.getVenue());
        target.setOrganizer(e.getOrganizer());
        target.setTicketPrice(e.getTicketPrice());
        target.setMaxSeats(e.getMaxSeats());
        target.setCategory(e.getCategory());
        save();
    }

    @Override
    public synchronized void deleteEvent(int id) {
        store.events.removeIf(e -> e.getId() == id);
        store.participants.removeIf(p -> p.getEventId() == id);
        save();
    }

    @Override
    public synchronized Participant register(Participant p) {
        Event e = raw(p.getEventId());
        if (e == null) throw new AppException("Event not found.");
        int left = e.getMaxSeats() - booked(e.getId());
        if (p.getSeats() > left) {
            throw new AppException(left <= 0
                    ? "Sorry, this event is sold out."
                    : "Only " + left + " seat(s) left for this event.");
        }
        p.setId(store.nextParticipantId++);
        p.setAmountPaid(e.getTicketPrice().multiply(BigDecimal.valueOf(p.getSeats())));
        p.setBookedAt(LocalDateTime.now().withNano(0));
        store.participants.add(new Participant(p));
        save();
        return p;
    }

    @Override
    public synchronized List<Participant> getParticipants(int eventId) {
        return store.participants.stream()
                .filter(p -> p.getEventId() == eventId)
                .sorted(Comparator.comparing(Participant::getBookedAt).thenComparing(Participant::getId).reversed())
                .map(Participant::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getStorageName() {
        return "Local file storage";
    }

    @Override
    public boolean isDatabase() {
        return false;
    }

    @Override
    public String getNote() {
        return note;
    }
}
