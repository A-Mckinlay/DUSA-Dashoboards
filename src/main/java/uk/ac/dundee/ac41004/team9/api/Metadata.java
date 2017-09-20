package uk.ac.dundee.ac41004.team9.api;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/meta")
public class Metadata {

    @Routes.GET(path = "/latestdate", transformer = DateResponseTransformer.class)
    public static Object latestDate(Request req, Response res) {
        return getLatestOrOldestDate(req, res, true);
    }

    @Routes.GET(path = "/oldestdate", transformer = DateResponseTransformer.class)
    public static Object oldestDate(Request req, Response res) {
        return getLatestOrOldestDate(req, res, false);
    }

    private static Object getLatestOrOldestDate(Request req, Response res, boolean latest) {
        LocalDateTime dt = DBConnManager.runWithConnection(conn -> {
            try {
                String ordering = latest ? "DESC" : "ASC";
                PreparedStatement ps = conn.prepareStatement("SELECT datetime FROM disbursals " +
                        "ORDER BY datetime " + ordering + " LIMIT 1");
                ResultSet results = ps.executeQuery();
                if (!results.next()) return null;
                return results.getTimestamp(1).toLocalDateTime();
            } catch (SQLException ex) {
                log.error("SQL error getting latest/oldest date", ex);
                return null;
            }
        });
        if (dt == null) {
            res.status(500);
            return null;
        }
        return dt;
    }

}
