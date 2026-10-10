package com.eventticketing.util;

import com.eventticketing.dao.EventRepository;
import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Demo records inserted the first time a brand-new store is created, so the dashboard is not empty. */
public final class SampleData {
    private SampleData() { }

    public static void seed(EventRepository repo) {
        LocalDate today = LocalDate.now();
        int hack = repo.addEvent(new Event("TechNova Hackathon 2026", today.plusDays(12),
                "Main Auditorium, Acharya Institute", "CSE Department", new BigDecimal("199.00"), 120, "Technical"));
        int music = repo.addEvent(new Event("Sufi Night Live", today.plusDays(25),
                "Palace Grounds, Bengaluru", "Cultural Club", new BigDecimal("750.00"), 40, "Music"));
        repo.addEvent(new Event("Java & Spring Boot Workshop", today.plusDays(6),
                "Seminar Hall 2", "Java Developers Guild", new BigDecimal("0.00"), 60, "Workshop"));
        repo.addEvent(new Event("Inter-College Cricket Cup", today.plusDays(40),
                "College Ground", "Sports Committee", new BigDecimal("50.00"), 300, "Sports"));
        repo.addEvent(new Event("Freshers Meet 2026", today.minusDays(20),
                "Open Air Theatre", "Student Council", new BigDecimal("100.00"), 200, "Cultural"));

        book(repo, hack, "Ananya Rao", "ananya.rao@example.com", "9876543210", 4);
        book(repo, hack, "Rahul Menon", "rahul.menon@example.com", "9123456780", 2);
        book(repo, music, "Priya Nair", "priya.nair@example.com", "9988776655", 38);
    }

    private static void book(EventRepository repo, int eventId, String name, String email, String phone, int seats) {
        try {
            Participant p = new Participant();
            p.setEventId(eventId);
            p.setName(name);
            p.setEmail(email);
            p.setPhone(phone);
            p.setSeats(seats);
            repo.register(p);
        } catch (RuntimeException ignored) {
            // demo data only
        }
    }
}
