package uk.ac.dundee.ac41004.team9.db;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@UtilityClass
@Slf4j
@ParametersAreNonnullByDefault
public class UserManager {

    public @Nullable User getUser(String username) {
        return DBConnManager.runWithConnection(conn -> {
            try {
                final PreparedStatement ps =
                        conn.prepareStatement("SELECT username, password, email, isadmin, firstname, lastname" +
                                " FROM users WHERE username = ?");
                ps.setString(1, username);
                ResultSet res = ps.executeQuery();
                if (!res.next()) return null;
                String user = res.getString(1);
                String pass = res.getString(2);
                String email = res.getString(3);
                boolean isAdmin = res.getBoolean(4);
                String firstName = res.getString(5);
                String lastName = res.getString(6);
                return new User(user, pass, email, isAdmin, firstName, lastName);
            } catch (SQLException ex) {
                log.error("SQL error in User Manager fetch.", ex);
                return null;
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public boolean writeUser(User user) {
        return DBConnManager.runWithConnection(conn -> {
            try {
                final PreparedStatement ps = conn.prepareStatement("INSERT INTO" +
                        " users(username, password, email, isadmin, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?) ON" +
                        " CONFLICT (username) DO UPDATE SET" +
                        "(username, password, email, isadmin, firstname, lastname) = (?, ? ,?, ?, ?, ?)");
                ps.setString(1, user.username);
                ps.setString(2, user.password);
                ps.setString(3, user.email);
                ps.setBoolean(4, user.isAdmin);
                ps.setString(5, user.firstName);
                ps.setString(6, user.lastName);

                ps.setString(7, user.username);
                ps.setString(8, user.password);
                ps.setString(9, user.email);
                ps.setBoolean(10, user.isAdmin);
                ps.setString(11, user.firstName);
                ps.setString(12, user.lastName);
                return ps.executeUpdate() != 0;
            } catch (SQLException ex) {
                log.error("SQL error in User Manager write.", ex);
                return false;
            }
        });
    }

    @Data
    @RequiredArgsConstructor
    public static class User {
        private final String username;
        @Wither
        private final String password;
        private final String email;
        private final boolean isAdmin;
        private final String firstName;
        private final String lastName;
    }
}
