package uk.ac.dundee.ac41004.team9.db;

import java.sql.Connection;
import java.util.function.Function;

/** Manager for database connections */
public class DBConnManager {

    private DBConnManager() {} // Static

    public static void init() {
        // TODO
    }

    public static <R> R run(Function<Connection, R> fn) {
        return null; // TODO
    }

}
