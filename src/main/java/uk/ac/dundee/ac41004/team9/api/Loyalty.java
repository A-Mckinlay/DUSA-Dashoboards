package uk.ac.dundee.ac41004.team9.api;

import io.drakon.spark.autorouter.Routes;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/loyalty")
public class Loyalty {

    @Routes.GET(path = "/txtypes/venues", transformer = GSONResponseTransformer.class)
    public static Object txTypesByVenue(Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        return DBConnManager.<Object>runWithConnection(conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT outletname, " +
                        "transactiontypes.transactiontype," +
                        "COUNT(transactiontypes.transactiontype) " +
                        "FROM disbursals JOIN outlets ON disbursals.outletref = outlets.outletref " +
                        "JOIN transactiontypes ON disbursals.transactiontype = transactiontypes.transactionid " +
                        "WHERE datetime > ? AND datetime < ? " +
                        "GROUP BY transactiontypes.transactiontype, outletname");
                ps.setTimestamp(1, Timestamp.valueOf(p.first));
                ps.setTimestamp(2, Timestamp.valueOf(p.second));
                ResultSet rs = ps.executeQuery();
                Map<String, Map<String, Integer>> map = new HashMap<>();
                while (rs.next()) {
                    String ol = rs.getString(1);
                    String ttype = rs.getString(2);
                    int count = rs.getInt(3);
                    Map<String, Integer> inner = map.getOrDefault(ol, new HashMap<>());
                    inner.put(ttype, count);
                    map.put(ol, inner);
                }
                return map;
            } catch (SQLException ex) {
                log.error("SQL exception in txtypes venues", ex);
                res.status(500);
                return immutableMapOf("error", "internal server error");
            }
        });
    }

}
