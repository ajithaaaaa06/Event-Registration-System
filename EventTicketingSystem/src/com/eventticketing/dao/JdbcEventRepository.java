package com.eventticketing.dao;

import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;
import com.eventticketing.util.AppException;
import com.eventticketing.util.SampleData;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** MySQL implementation. Every statement is a PreparedStatement (no SQL injection). */
public class JdbcEventRepository implements EventRepository {

    private static final String SELECT_EVENTS =
            "SELECT e.*, COALESCE(SUM(p.seats), 0) AS booked FROM events e "
          + "LEFT JOIN participants p ON p.event_id = e.id ";

    private final String url;
    private final String user;
    private final String password;
    private String dbName = "MySQL";

    public JdbcEventRepository(String url, String user, String password) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        boolean fresh;
        try (Connection c = connect()) {
            dbName = c.getCatalog();
            fresh = createSchema(c);
        }
        if (fresh) {
            SampleData.seed(this); // demo data only the very first time the tables are created
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private boolean createSchema(Connection c) throws SQLException {
        boolean fresh;
        try (ResultSet rs = c.getMetaData().getTables(c.getCatalog(), null, "events", new String[]{"TABLE"})) {
            fresh = !rs.next();
        }
        try (Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS events ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "event_name VARCHAR(255) NOT NULL, "
                    + "event_date DATE NOT NULL, "
                    + "venue VARCHAR(255) NOT NULL, "
                    + "organizer_name VARCHAR(255) NOT NULL, "
                    + "ticket_price DECIMAL(10,2) NOT NULL DEFAULT 0, "
                    + "maximum_seats INT NOT NULL, "
                    + "event_category VARCHAR(100) NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS participants ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "event_id INT NOT NULL, "
                    + "attendee_name VARCHAR(255) NOT NULL, "
                    + "email VARCHAR(255) NOT NULL, "
                    + "phone VARCHAR(30) NOT NULL, "
                    + "seats INT NOT NULL, "
                    + "amount_paid DECIMAL(12,2) NOT NULL, "
                    + "booked_at DATETIME NOT NULL, "
                    + "CONSTRAINT fk_participant_event FOREIGN KEY (event_id) "
                    + "REFERENCES events(id) ON DELETE CASCADE)");
        }
        return fresh;
    }

    private AppException wrap(SQLException e) {
        return new AppException("Database error: " + e.getMessage(), e);
    }

    // ------------------------------------------------------------------ events

    @Override
    public List<Event> search(String keyword, String category) {
        StringBuilder sql = new StringBuilder(SELECT_EVENTS).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (e.event_name LIKE ? OR e.venue LIKE ? OR e.organizer_name LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append("AND e.event_category = ? ");
            params.add(category.trim());
        }
        sql.append("GROUP BY e.id ORDER BY e.event_date, e.event_name");

        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Event> list = new ArrayList<>();
                while (rs.next()) list.add(mapEvent(rs));
                return list;
            }
        } catch (SQLException ex) {
            throw wrap(ex);
        }
    }

    @Override
    public Event findById(int id) {
        String sql = SELECT_EVENTS + "WHERE e.id = ? GROUP BY e.id";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapEvent(rs) : null;
            }
        } catch (SQLException ex) {
            throw wrap(ex);
        }
    }

    @Override
    public int addEvent(Event e) {
        String sql = "INSERT INTO events (event_name, event_date, venue, organizer_name, "
                   + "ticket_price, maximum_seats, event_category) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getName());
            ps.setDate(2, java.sql.Date.valueOf(e.getDate()));
            ps.setString(3, e.getVenue());
            ps.setString(4, e.getOrganizer());
            ps.setBigDecimal(5, e.getTicketPrice());
            ps.setInt(6, e.getMaxSeats());
            ps.setString(7, e.getCategory());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw wrap(ex);
        }
    }

    @Override
    public void updateEvent(Event e) {
        String sql = "UPDATE events SET event_name=?, event_date=?, venue=?, organizer_name=?, "
                   + "ticket_price=?, maximum_seats=?, event_category=? WHERE id=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setDate(2, java.sql.Date.valueOf(e.getDate()));
            ps.setString(3, e.getVenue());
            ps.setString(4, e.getOrganizer());
            ps.setBigDecimal(5, e.getTicketPrice());
            ps.setInt(6, e.getMaxSeats());
            ps.setString(7, e.getCategory());
            ps.setInt(8, e.getId());
            if (ps.executeUpdate() == 0) throw new AppException("Event no longer exists.");
        } catch (SQLException ex) {
            throw wrap(ex);
        }
    }

    @Override
    public void deleteEvent(int id) {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM events WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate(); // participants are removed by ON DELETE CASCADE
        } catch (SQLException ex) {
            throw wrap(ex);
        }
    }

    // ------------------------------------------------------------ registrations

    @Override
    public Participant register(Participant p) {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                BigDecimal price;
                int max;
                // Lock the event row so two people can't grab the last seats at the same time.
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT ticket_price, maximum_seats FROM events WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, p.getEventId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new AppException("Event not found.");
                        price = rs.getBigDecimal(1);
                        max = rs.getInt(2);
                    }
                }
                int booked;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(SUM(seats), 0) FROM participants WHERE event_id = ?")) {
                    ps.setInt(1, p.getEventId());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        booked = rs.getInt(1);
                    }
                }
                int left = max - booked;
                if (p.getSeats() > left) {
                    throw new AppException(left <= 0
                            ? "Sorry, this event is sold out."
                            : "Only " + left + " seat(s) left for this event.");
                }
                LocalDateTime now = LocalDateTime.now().withNano(0);
                BigDecimal amount = price.multiply(BigDecimal.valueOf(p.getSeats()));
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO participants (event_id, attendee_name, email, phone, seats, amount_paid, booked_at) "
                      + "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, p.getEventId());
                    ps.setString(2, p.getName());
                    ps.setString(3, p.getEmail());
                    ps.setString(4, p.getPhone());
                    ps.setInt(5, p.getSeats());
                    ps.setBigDecimal(6, amount);
                    ps.setTimestamp(7, Timestamp.valueOf(now));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) p.setId(keys.getInt(1));
                    }
                }
                c.commit();
                p.setAmountPaid(amount);
                p.setBookedAt(now);
                return p;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw wrap(ex);
        }
    }

    @Override
    public List<Participant> getParticipants(int eventId) {
        String sql = "SELECT * FROM participants WHERE event_id = ? ORDER BY booked_at DESC, id DESC";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Participant> list = new ArrayList<>();
                while (rs.next()) {
                    Participant p = new Participant();
                    p.setId(rs.getInt("id"));
                    p.setEventId(rs.getInt("event_id"));
                    p.setName(rs.getString("attendee_name"));
                    p.setEmail(rs.getString("email"));
                    p.setPhone(rs.getString("phone"));
                    p.setSeats(rs.getInt("seats"));
                    p.setAmountPaid(rs.getBigDecimal("amount_paid"));
                    p.setBookedAt(rs.getTimestamp("booked_at").toLocalDateTime());
                    list.add(p);
                }
                return list;
            }
        } catch (SQLException ex) {
            throw wrap(ex);
        }
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("event_name"));
        e.setDate(rs.getDate("event_date").toLocalDate());
        e.setVenue(rs.getString("venue"));
        e.setOrganizer(rs.getString("organizer_name"));
        e.setTicketPrice(rs.getBigDecimal("ticket_price"));
        e.setMaxSeats(rs.getInt("maximum_seats"));
        e.setCategory(rs.getString("event_category"));
        e.setBookedSeats(rs.getInt("booked"));
        return e;
    }

    @Override
    public String getStorageName() {
        return "MySQL \u00B7 " + dbName;
    }

    @Override
    public boolean isDatabase() {
        return true;
    }
}
