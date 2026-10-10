package com.eventticketing.view;

import com.eventticketing.controller.EventController;
import com.eventticketing.model.Event;
import com.eventticketing.model.Participant;
import com.eventticketing.util.AppException;
import com.eventticketing.util.Ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

/** The dashboard: stats, search/filter bar, events table, action buttons and seat availability. */
public class MainFrame extends JFrame {

    private static final String ALL = "All Categories";

    private final EventController controller;
    private final EventTableModel model = new EventTableModel();
    private final JTable table = new JTable(model);

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> categoryBox = new JComboBox<>();
    private boolean adjustingCategories;

    private final Ui.StatCard cardEvents = new Ui.StatCard("Total Events", Ui.BLUE);
    private final Ui.StatCard cardUpcoming = new Ui.StatCard("Upcoming Events", Ui.GREEN);
    private final Ui.StatCard cardSeats = new Ui.StatCard("Total Seats", Ui.PURPLE);
    private final Ui.StatCard cardBooked = new Ui.StatCard("Seats Booked", Ui.AMBER);
    private final Ui.StatCard cardRevenue = new Ui.StatCard("Revenue", Ui.NAVY);

    private final JLabel seatInfo = new JLabel("Select an event to see its seat availability.");
    private final SeatBar seatBar = new SeatBar();

    private final JButton btnAdd = Ui.button("Add Event", Ui.GREEN);
    private final JButton btnEdit = Ui.button("Edit Event", Ui.BLUE);
    private final JButton btnDelete = Ui.button("Delete Event", Ui.RED);
    private final JButton btnRegister = Ui.button("Register Participant", Ui.PURPLE);
    private final JButton btnAttendees = Ui.button("View Attendees", Ui.NAVY);
    private final JButton btnSearch = Ui.button("Search", Ui.BLUE);
    private final JButton btnRefresh = Ui.button("Refresh", Ui.GREY);

    public MainFrame(EventController controller) {
        super("Event Registration & Ticketing System");
        this.controller = controller;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.BG);
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        body.add(buildTop(), BorderLayout.NORTH);
        body.add(buildTable(), BorderLayout.CENTER);
        body.add(buildSeatPanel(), BorderLayout.SOUTH);
        root.add(body, BorderLayout.CENTER);

        wireActions();
        reload();
        updateButtons();

        setSize(1240, 780);
        setMinimumSize(new Dimension(1050, 620));
        setLocationRelativeTo(null);
    }

    // ----------------------------------------------------------------- layout

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Ui.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Event Registration & Ticketing System");
        title.setForeground(Color.WHITE);
        title.setFont(Ui.FONT.deriveFont(Font.BOLD, 21f));
        JLabel sub = new JLabel("Manage events, register participants and track seat availability");
        sub.setForeground(new Color(0x94A3B8));
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        boolean db = controller.isDatabase();
        JLabel badge = new JLabel(db ? "  \u25CF  " + controller.getStorageName() + "  "
                                     : "  \u25CF  Offline mode: local storage  ");
        badge.setOpaque(true);
        badge.setBackground(db ? Ui.GREEN : Ui.AMBER);
        badge.setForeground(Color.WHITE);
        badge.setFont(Ui.FONT_BOLD);
        badge.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        if (!controller.getStorageNote().isEmpty()) {
            badge.setToolTipText("<html><div style='width:360px'>" + Ui.html(controller.getStorageNote()) + "</div></html>");
        }
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.setOpaque(false);
        right.add(badge);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildTop() {
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0));
        cards.setOpaque(false);
        cards.add(cardEvents);
        cards.add(cardUpcoming);
        cards.add(cardSeats);
        cards.add(cardBooked);
        cards.add(cardRevenue);

        searchField.setPreferredSize(new Dimension(250, 32));
        searchField.setToolTipText("Search by event name, venue or organizer");
        categoryBox.setPreferredSize(new Dimension(170, 32));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.setOpaque(false);
        row1.add(Ui.formLabel("Search:"));
        row1.add(searchField);
        row1.add(Ui.formLabel("Category:"));
        row1.add(categoryBox);
        row1.add(btnSearch);
        row1.add(btnRefresh);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);
        row2.add(btnAdd);
        row2.add(btnEdit);
        row2.add(btnDelete);
        row2.add(btnRegister);
        row2.add(btnAttendees);

        JPanel tools = new JPanel(new GridLayout(2, 1, 0, 8));
        tools.setBackground(Color.WHITE);
        tools.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.BORDER),
                BorderFactory.createEmptyBorder(10, 6, 10, 6)));
        tools.add(row1);
        tools.add(row2);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(cards, BorderLayout.NORTH);
        top.add(tools, BorderLayout.CENTER);
        return top;
    }

    private JComponent buildTable() {
        table.setRowHeight(30);
        table.setFont(Ui.FONT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);          // click a column header to sort
        table.setShowVerticalLines(false);
        table.setGridColor(Ui.BORDER);
        table.setSelectionBackground(new Color(0xDBEAFE));
        table.setSelectionForeground(Ui.NAVY);
        table.getTableHeader().setFont(Ui.FONT_BOLD);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.setDefaultRenderer(Integer.class, new CellRenderer());
        table.setDefaultRenderer(String.class, new CellRenderer());
        table.setDefaultRenderer(LocalDate.class, new CellRenderer());
        table.setDefaultRenderer(BigDecimal.class, new CellRenderer());

        int[] widths = {45, 220, 100, 200, 150, 95, 100, 60, 80, 90, 105};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JComponent buildSeatPanel() {
        seatInfo.setFont(Ui.FONT_BOLD);
        seatInfo.setForeground(Ui.NAVY);
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(Ui.cardBorder(12));
        JLabel caption = new JLabel("SEAT AVAILABILITY");
        caption.setFont(Ui.FONT.deriveFont(Font.BOLD, 11f));
        caption.setForeground(Ui.GREY);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(caption, BorderLayout.NORTH);
        top.add(seatInfo, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        p.add(seatBar, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------------- actions

    private void wireActions() {
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRegister.addActionListener(e -> onRegister());
        btnAttendees.addActionListener(e -> onAttendees());
        btnSearch.addActionListener(e -> reload());
        btnRefresh.addActionListener(e -> onRefresh());
        searchField.addActionListener(e -> reload());
        categoryBox.addActionListener(e -> { if (!adjustingCategories) reload(); });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtons();
                updateSeatPanel();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) onEdit();
            }
        });
    }

    private void onAdd() {
        EventDialog d = new EventDialog(this, controller, null);
        d.setVisible(true);
        if (d.getResult() != null) {
            guarded(() -> {
                controller.addEvent(d.getResult());
                reload();
            });
        }
    }

    private void onEdit() {
        Event selected = selectedEvent();
        if (selected == null) return;
        EventDialog d = new EventDialog(this, controller, selected);
        d.setVisible(true);
        if (d.getResult() != null) {
            guarded(() -> {
                controller.updateEvent(d.getResult());
                reload();
                selectById(d.getResult().getId());
            });
        }
    }

    private void onDelete() {
        Event selected = selectedEvent();
        if (selected == null) return;
        String msg = "Permanently delete \"" + selected.getName() + "\"?";
        if (selected.getBookedSeats() > 0) {
            msg += "\n\nThis will also remove its " + selected.getBookedSeats() + " booked seat(s) / registrations.";
        }
        if (Ui.confirm(this, msg)) {
            guarded(() -> {
                controller.deleteEvent(selected.getId());
                reload();
            });
        }
    }

    private void onRegister() {
        Event selected = selectedEvent();
        if (selected == null) return;
        if (selected.isPast()) {
            Ui.info(this, "This event has already taken place, so registration is closed.");
            return;
        }
        if (selected.isSoldOut()) {
            Ui.info(this, "\"" + selected.getName() + "\" is sold out.");
            return;
        }
        RegisterDialog d = new RegisterDialog(this, controller, selected);
        d.setVisible(true);
        Participant booking = d.getResult();
        if (booking != null) {
            guarded(() -> {
                reload();
                selectById(selected.getId());
                new TicketDialog(this, controller.getEvent(selected.getId()), booking).setVisible(true);
            });
        }
    }

    private void onAttendees() {
        Event selected = selectedEvent();
        if (selected == null) return;
        guarded(() -> new AttendeesDialog(this, controller, selected).setVisible(true));
    }

    private void onRefresh() {
        searchField.setText("");
        adjustingCategories = true;
        categoryBox.setSelectedItem(ALL);
        adjustingCategories = false;
        reload();
    }

    // ------------------------------------------------------------------- data

    /** Re-reads the events using the current search text and category filter. */
    private void reload() {
        int keep = selectedId();
        guarded(() -> {
            String chosen = (String) categoryBox.getSelectedItem();
            rebuildCategories(controller.getEvents(), chosen);
            String cat = (String) categoryBox.getSelectedItem();
            List<Event> events = controller.search(searchField.getText(), cat == null || ALL.equals(cat) ? null : cat);
            model.setEvents(events);
            updateStats(events);
        });
        selectById(keep);
        updateButtons();
        updateSeatPanel();
    }

    private void rebuildCategories(List<Event> all, String keep) {
        adjustingCategories = true;
        try {
            TreeSet<String> cats = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            cats.addAll(EventController.CATEGORIES);
            for (Event e : all) cats.add(e.getCategory());
            categoryBox.removeAllItems();
            categoryBox.addItem(ALL);
            for (String c : cats) categoryBox.addItem(c);
            categoryBox.setSelectedItem(keep != null && (ALL.equals(keep) || cats.contains(keep)) ? keep : ALL);
        } finally {
            adjustingCategories = false;
        }
    }

    private void updateStats(List<Event> events) {
        LocalDate today = LocalDate.now();
        int upcoming = 0, seats = 0, booked = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        for (Event e : events) {
            if (!e.getDate().isBefore(today)) upcoming++;
            seats += e.getMaxSeats();
            booked += e.getBookedSeats();
            revenue = revenue.add(e.getTicketPrice().multiply(BigDecimal.valueOf(e.getBookedSeats())));
        }
        cardEvents.setValue(String.valueOf(events.size()));
        cardUpcoming.setValue(String.valueOf(upcoming));
        cardSeats.setValue(String.format("%,d", seats));
        cardBooked.setValue(String.format("%,d", booked));
        cardRevenue.setValue(Ui.money(revenue));
    }

    private void updateButtons() {
        boolean has = table.getSelectedRow() >= 0;
        btnEdit.setEnabled(has);
        btnDelete.setEnabled(has);
        btnRegister.setEnabled(has);
        btnAttendees.setEnabled(has);
    }

    private void updateSeatPanel() {
        int row = table.getSelectedRow();
        if (row < 0) {
            seatInfo.setText("Select an event to see its seat availability.");
            seatBar.setFill(0, Ui.GREY);
            return;
        }
        Event e = model.getEventAt(table.convertRowIndexToModel(row));
        double fraction = e.getMaxSeats() == 0 ? 0 : (double) e.getBookedSeats() / e.getMaxSeats();
        Color color = fraction >= 1 ? Ui.RED : fraction >= 0.8 ? Ui.AMBER : Ui.GREEN;
        seatInfo.setText(e.getName() + "   |   Total: " + e.getMaxSeats() + "   Booked: " + e.getBookedSeats()
                + "   Available: " + e.getAvailableSeats() + "   (" + Math.round(fraction * 100) + "% full)");
        seatBar.setFill(fraction, color);
    }

    private int selectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : model.getEventAt(table.convertRowIndexToModel(row)).getId();
    }

    private void selectById(int id) {
        if (id < 0) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getEventAt(i).getId() == id) {
                int view = table.convertRowIndexToView(i);
                table.setRowSelectionInterval(view, view);
                table.scrollRectToVisible(table.getCellRect(view, 0, true));
                return;
            }
        }
    }

    /** Returns the freshly loaded selected event, or null (after telling the user) if none is selected. */
    private Event selectedEvent() {
        int id = selectedId();
        if (id < 0) {
            Ui.info(this, "Please select an event from the table first.");
            return null;
        }
        try {
            return controller.getEvent(id);
        } catch (AppException ex) {
            Ui.error(this, ex.getMessage());
            reload();
            return null;
        }
    }

    private void guarded(Runnable r) {
        try {
            r.run();
        } catch (AppException ex) {
            Ui.error(this, ex.getMessage());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Ui.error(this, "Unexpected error: " + ex.getMessage());
        }
    }

    // --------------------------------------------------------------- renderer

    private class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, false, row, col);
            int mc = t.convertColumnIndexToModel(col);
            Event e = model.getEventAt(t.convertRowIndexToModel(row));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(LEFT);
            if (!selected) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                setForeground(Ui.NAVY);
            }
            switch (mc) {
                case 2:
                    setText(Ui.DATE.format(e.getDate()));
                    break;
                case 6:
                    setText(Ui.money(e.getTicketPrice()));
                    setHorizontalAlignment(RIGHT);
                    break;
                case 7: case 8:
                    setHorizontalAlignment(CENTER);
                    break;
                case 9:
                    setHorizontalAlignment(CENTER);
                    if (!selected && e.isSoldOut()) setForeground(Ui.RED);
                    break;
                case 10:
                    setHorizontalAlignment(CENTER);
                    setFont(Ui.FONT_BOLD);
                    if (!selected) {
                        String s = e.getStatus();
                        setForeground("Open".equals(s) ? Ui.GREEN : "Sold Out".equals(s) ? Ui.RED : Ui.GREY);
                    }
                    break;
                default:
                    break;
            }
            return this;
        }
    }
}
