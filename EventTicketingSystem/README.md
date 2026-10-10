# Event Registration and Ticketing System

Java Swing desktop app (MVC) with MySQL / JDBC. Manage events, register participants,
generate ticket confirmations and track seat availability.

## Run it in VS Code (2 minutes)

1. Install a **JDK 17 or newer** and the VS Code extension **"Extension Pack for Java"**.
2. Unzip this folder, then **File > Open Folder...** and pick `EventTicketingSystem`.
3. Open `src/com/eventticketing/Main.java` and click **Run** (above `main`) - or press **F5**.

The dashboard opens straight away with a few sample events.
The first run works even without MySQL: the header badge shows **"Offline mode: local storage"**
and data is saved in `data/events.dat`.

No VS Code? Run `run.bat` (Windows) or `./run.sh` (Mac/Linux).

## Switch to MySQL

1. Start your MySQL server.
2. Download MySQL Connector/J (Platform Independent ZIP) and copy the `.jar` into `lib/`
   (see `lib/README.txt`).
3. Put your password in `config.properties` (`db.password=`).
4. Run again. The badge turns green: **MySQL - event_ticketing**.
   The database and tables are created automatically (`sql/schema.sql` is there for reference).

## Features

| Requirement | Where |
|---|---|
| Add event (name, date, venue, organizer, price, seats, category) | **Add Event** - dialog with JSpinners + date picker |
| View all events in a JTable, sort / filter | Table (click headers to sort), search box + category filter |
| Update event | Select a row > **Edit Event** (or double-click) |
| Delete event | **Delete Event** (confirmation; registrations removed too) |
| Register participants + ticket confirmation | **Register Participant** > ticket dialog, save as `.txt` |
| Seat availability, no overbooking | Seats / Booked / Available columns, seat bar, DB transaction with row lock |
| Extra | Dashboard stat cards, attendee list per event, CSV export |

## Validation rules

- Name, venue, organizer and category are required; price >= 0; seats >= 1.
- Event date cannot be in the past (when adding or changing the date).
- Only one event per venue per day.
- Seats cannot be reduced below the number already booked.
- Registration needs a valid name, email and phone; max 10 tickets per booking;
  blocked for sold-out and completed events.

## Project structure

```
src/com/eventticketing/
  Main.java                    entry point
  model/        Event, Participant
  view/         MainFrame (dashboard), EventDialog, RegisterDialog, TicketDialog,
                AttendeesDialog, DatePickerField, EventTableModel, SeatBar
  controller/   EventController (validation + business rules)
  dao/          EventRepository (interface), JdbcEventRepository (MySQL, PreparedStatement),
                LocalEventRepository (offline fallback), RepositoryFactory
  util/         Ui, TicketFormatter, SampleData, AppException
config.properties   database settings
sql/schema.sql      table definitions
```

## Ideas for the optional enhancements

PDF e-tickets (Apache PDFBox / iText), QR codes (ZXing), email confirmations (Jakarta Mail),
payment gateway (Razorpay), charts for analytics (JFreeChart). `TicketFormatter` and
`EventController.register(...)` are the places to hook them in.
