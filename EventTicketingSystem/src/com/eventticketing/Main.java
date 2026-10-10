package com.eventticketing;

import com.eventticketing.controller.EventController;
import com.eventticketing.dao.EventRepository;
import com.eventticketing.dao.RepositoryFactory;
import com.eventticketing.util.AppException;
import com.eventticketing.util.Ui;
import com.eventticketing.view.MainFrame;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/** Application entry point: run this class and the dashboard opens. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ignored) {
                // fall back to the default look and feel
            }
            UIManager.put("defaultFont", Ui.FONT);
            try {
                EventRepository repo = RepositoryFactory.create(loadConfig());
                new MainFrame(new EventController(repo)).setVisible(true);
            } catch (AppException ex) {
                Ui.error(null, ex.getMessage());
                System.exit(1);
            }
        });
    }

    /** Reads config.properties from the project folder (or uses built-in defaults). */
    private static Properties loadConfig() {
        Properties p = new Properties();
        p.setProperty("db.mode", "auto");
        p.setProperty("db.url", "jdbc:mysql://localhost:3306/event_ticketing"
                + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        p.setProperty("db.user", "root");
        p.setProperty("db.password", "Ajitha@06");
        try (InputStream in = Files.newInputStream(Paths.get("config.properties"))) {
            p.load(in);
        } catch (IOException ignored) {
            // no config file: defaults above are used
        }
        return p;
    }
}
