# Prompt to generate this project from scratch

Copy everything below into an AI coding assistant.

---

You are a senior Java engineer. Build a complete, ready-to-run desktop application called **Event Registration and Ticketing System** using **Java Swing, MySQL and JDBC**. Deliver it as a single project folder that I can open in VS Code and run by clicking Run on `Main.java` - the dashboard must open immediately.

## Features
1. **Add event**: name, date, venue, organizer, ticket price, maximum seats, category -> stored in the database.
2. **View all events** in a JTable with sorting (click headers), a search box (name/venue/organizer) and a category filter (JComboBox).
3. **Edit event**: select a row -> dialog pre-filled with its details -> update venue, price, seats, etc.
4. **Delete event** with a confirmation prompt (registrations are removed with it).
5. **Register participants**: name, email, phone, number of tickets; auto-generate a ticket confirmation (ticket ID, event details, seats, total) that can be saved as a file.
6. **Seat tracking**: show total / booked / available seats per event (columns + a progress bar) and prevent overbooking, using a DB transaction with a row lock. Block registration for sold-out and past events.

## Database (MySQL)
- Table `events`: id INT PK AI, event_name VARCHAR(255), event_date DATE, venue VARCHAR(255), organizer_name VARCHAR(255), ticket_price DECIMAL(10,2), maximum_seats INT, event_category VARCHAR(100).
- Table `participants` (id, event_id FK ON DELETE CASCADE, attendee_name, email, phone, seats, amount_paid, booked_at). Booked seats = SUM(seats).
- Auto-create the database and tables on first run; insert a few demo events the first time.
- Use **PreparedStatement for every query** (INSERT / SELECT ... WHERE / UPDATE ... WHERE id=? / DELETE ... WHERE id=?).

## GUI (Swing, modern flat look)
- Main window: header bar, stat cards (total events, upcoming, total seats, seats booked, revenue), search field + category combo + Search/Refresh buttons, buttons Add Event / Edit Event / Delete Event / Register Participant / View Attendees, the events JTable, and a seat-availability panel for the selected event.
- Add/Edit dialog: JTextFields, JSpinner for ticket price and seat capacity, a date picker (pop-up calendar), Save/Cancel.
- Buttons that need a selection are disabled until a row is selected. Double-click a row to edit.

## Validation
Required fields; price >= 0; seats >= 1; date not in the past; one event per venue per day; seats cannot be reduced below the number already booked; valid email and phone; max 10 tickets per booking. Show friendly error dialogs.

## Architecture
- MVC packages: `model`, `view`, `controller`, `dao`, `util`.
- `EventRepository` interface with a JDBC/MySQL implementation **and a local-file fallback**, so the dashboard still opens when MySQL or the driver is missing (show an "Offline mode" badge in the header).
- Database settings in `config.properties` (mode auto/mysql/local, url, user, password).

## Deliverables and constraints
- Plain Java 17, **no Maven/Gradle**; the only external file is MySQL Connector/J, which the user drops into `lib/`.
- Include `.vscode/settings.json` (source path `src`, referenced libraries `lib/**/*.jar`), `.vscode/launch.json`, `run.bat`, `run.sh`, `sql/schema.sql`, and a README with setup steps.
- Compile it and test the registration/overbooking logic before delivering. Print the final folder tree.

## Optional enhancements (only after the core works)
PDF e-tickets, QR-code entry, email/SMS confirmation, payment gateway, attendee reports and analytics.
