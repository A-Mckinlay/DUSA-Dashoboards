package uk.ac.dundee.ac41004.team9.api;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/revenue")
public class Revenue {

    @Routes.GET(path = "/top5", transformer = GSONResponseTransformer.class)
    public static Object topFive(Request req, Response res) {
        DateRangeRequest jsonReq = DateRangeRequest.fromBody(req.body());
        if (jsonReq == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        Map<String, Double> map = DBConnManager.runWithConnection(conn -> {
            try {
                Map<String, Double> out = new HashMap<>();
                PreparedStatement ps = conn.prepareStatement("SELECT outletname, SUM(cashspent) AS cashspent " +
                        "FROM disbursals JOIN outlets ON disbursals.outletref = outlets.outletref " +
                        "WHERE datetime > ? AND datetime < ? " +
                        "GROUP BY outletname");
                ps.setTimestamp(1, Timestamp.valueOf(jsonReq.getStartJ8()));
                ps.setTimestamp(2, Timestamp.valueOf(jsonReq.getEndJ8()));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    out.put(rs.getString(1), rs.getDouble(2));
                }
                return out;
            } catch (SQLException ex) {
                log.error("SQL exception fetching top5", ex);
                return null;
            }
        });

        if (map == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        return map;
    }

}
