package uk.ac.dundee.ac41004.team9.api;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/meta")
public class Metadata {

    @Routes.GET(path = "/latestdate", transformer = GSONResponseTransformer.class)
    public static Object latestDate(Request request, Response res) {
        LocalDateTime dt = DBConnManager.runWithConnection(conn -> {
            try {
                PreparedStatement ps =
                        conn.prepareStatement("SELECT datetime FROM disbursals ORDER BY datetime DESC LIMIT 1");
                ResultSet results = ps.executeQuery();
                if (!results.next()) return null;
                return results.getTimestamp(1).toLocalDateTime();
            } catch (SQLException ex) {
                log.error("SQL error getting latest date", ex);
                return null;
            }
        });
        if (dt == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }
        return Date.from(dt.toInstant(ZoneOffset.UTC));
    }

}
