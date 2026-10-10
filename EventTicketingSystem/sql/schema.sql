-- Optional: the application creates all of this automatically on first run.
-- Run it manually only if you prefer to set the database up yourself.

CREATE DATABASE IF NOT EXISTS event_ticketing;
USE event_ticketing;

CREATE TABLE IF NOT EXISTS events (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    event_name     VARCHAR(255)  NOT NULL,
    event_date     DATE          NOT NULL,
    venue          VARCHAR(255)  NOT NULL,
    organizer_name VARCHAR(255)  NOT NULL,
    ticket_price   DECIMAL(10,2) NOT NULL DEFAULT 0,
    maximum_seats  INT           NOT NULL,
    event_category VARCHAR(100)  NOT NULL
);

-- Registrations: seats booked per attendee. Booked seats of an event = SUM(seats).
CREATE TABLE IF NOT EXISTS participants (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    event_id      INT           NOT NULL,
    attendee_name VARCHAR(255)  NOT NULL,
    email         VARCHAR(255)  NOT NULL,
    phone         VARCHAR(30)   NOT NULL,
    seats         INT           NOT NULL,
    amount_paid   DECIMAL(12,2) NOT NULL,
    booked_at     DATETIME      NOT NULL,
    CONSTRAINT fk_participant_event FOREIGN KEY (event_id)
        REFERENCES events(id) ON DELETE CASCADE
);

-- Queries used by the application (all run as PreparedStatements):
-- INSERT INTO events (event_name, event_date, venue, organizer_name, ticket_price, maximum_seats, event_category) VALUES (?,?,?,?,?,?,?);
-- SELECT e.*, COALESCE(SUM(p.seats),0) AS booked FROM events e LEFT JOIN participants p ON p.event_id = e.id WHERE ... GROUP BY e.id;
-- UPDATE events SET event_name=?, event_date=?, venue=?, organizer_name=?, ticket_price=?, maximum_seats=?, event_category=? WHERE id=?;
-- DELETE FROM events WHERE id=?;
