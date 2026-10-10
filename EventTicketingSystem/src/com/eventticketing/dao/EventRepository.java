package com.eventticketing.dao;

import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;

import java.util.List;

/** Data-access contract. Implemented by JDBC/MySQL and by a local-file fallback. */
public interface EventRepository {

    /** Events (with booked seat counts) filtered by keyword (name/venue/organizer) and category; null = no filter. */
    List<Event> search(String keyword, String category);

    default List<Event> findAll() {
        return search(null, null);
    }

    Event findById(int id);

    int addEvent(Event event);

    void updateEvent(Event event);

    void deleteEvent(int id);

    /** Books seats atomically; throws AppException if that would overbook the event. */
    Participant register(Participant participant);

    List<Participant> getParticipants(int eventId);

    String getStorageName();

    boolean isDatabase();

    default String getNote() {
        return "";
    }
}
