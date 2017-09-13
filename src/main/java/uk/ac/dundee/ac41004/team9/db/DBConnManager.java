package uk.ac.dundee.ac41004.team9.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.dundee.ac41004.team9.Config;

/** Manager for database connections */
public class DBConnManager {

    private static final Logger log = LoggerFactory.getLogger(DBConnManager.class);

    private DBConnManager() {} // Static

    /** Initialises the database manager. Call once during app startup. */
    public static void init() {
        // Check we have Postgres driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Unable to load Postgres driver!", ex);
        }
    }

    /**
     * Runs the given lambda/function with a database connection passed to it. Destroys the connection after execution.
     *
     * @return Lambda return, or null in case of error.
     */
    public static <R> R runWithConnection(Function<Connection, R> fn) {
        try (Connection conn = getConnection()) {
            return fn.apply(conn);
        } catch (SQLException ex) {
            log.error("Error getting connection.", ex);
            return null;
        }
    }

    /** Gets a new connection from the DriverManager. Connections <b>must</b> be destroyed by the caller! */
    private static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://" + Config.getDbHost() + ":" + Config.getDbPort() + "/" + Config.getDbName();
        return DriverManager.getConnection(url, Config.getDbUser(), Config.getDbPass());
    }

}
