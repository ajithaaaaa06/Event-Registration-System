package com.eventticketing.dao;

import com.eventticketing.util.AppException;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

/** Chooses MySQL when it is reachable; otherwise falls back to local file storage (mode "auto"). */
public final class RepositoryFactory {
    private RepositoryFactory() { }

    public static EventRepository create(Properties cfg) {
        String mode = cfg.getProperty("db.mode", "auto").trim().toLowerCase();
        String reason = "db.mode=local in config.properties";

        if (!mode.equals("local")) {
            try {
                return new JdbcEventRepository(
                        cfg.getProperty("db.url"),
                        cfg.getProperty("db.user", "root"),
                        cfg.getProperty("db.password", ""));
            } catch (SQLException ex) {
                reason = "MySQL not available: " + ex.getMessage()
                       + " (is the server running, and is mysql-connector-j.jar inside the lib/ folder?)";
                if (mode.equals("mysql")) {
                    throw new AppException(reason, ex);
                }
                System.err.println("[EventTicketing] " + reason);
            }
        }
        return new LocalEventRepository(Paths.get("data", "events.dat"), reason);
    }
}
