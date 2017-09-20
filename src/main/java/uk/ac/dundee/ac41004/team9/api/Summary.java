package uk.ac.dundee.ac41004.team9.api;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.data.MoneySummaryRow;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/summary")
public class Summary {

    @Routes.GET(path = "/money", transformer = GSONResponseTransformer.class)
    public static Object money(Request req, Response res) {
        DateRangeRequest jsonReq = DateRangeRequest.fromBody(req.body());
        if (jsonReq == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        Map<String, MoneySummaryRow> map = DBConnManager.runWithConnection(conn -> {
            try {
                Map<String, MoneySummaryRow> out = new HashMap<>();
                PreparedStatement ps = conn.prepareStatement("SELECT transactiontypes.transactiontype, " +
                        "SUM(cashspent) as cashspent, " +
                        "SUM(discountamount) AS discountamount, " +
                        "SUM(totalamount) AS totalamount " +
                        "FROM disbursals JOIN transactiontypes " +
                        "ON disbursals.transactiontype = transactiontypes.transactionid " +
                        "WHERE datetime > ? AND datetime < ? " +
                        "GROUP BY transactiontypes.transactiontype");
                ps.setTimestamp(1, Timestamp.valueOf(jsonReq.getStartJ8()));
                ps.setTimestamp(2, Timestamp.valueOf(jsonReq.getEndJ8()));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    out.put(rs.getString(1), new MoneySummaryRow(rs.getDouble(2), rs.getDouble(3), rs.getDouble(4)));
                }
                return out;
            } catch (SQLException ex) {
                log.error("SQL error getting money summary", ex);
                return null;
            }
        });

        if (map == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        return map;
    }

    @Routes.GET(path = "/tx", transformer = GSONResponseTransformer.class)
    public static Object tx(Request req, Response res) {
        DateRangeRequest jsonReq = DateRangeRequest.fromBody(req.body());
        if (jsonReq == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        Map<String, Integer> map = DBConnManager.runWithConnection(conn -> {
            try {
                Map<String, Integer> out = new HashMap<>();
                PreparedStatement ps = conn.prepareStatement("SELECT transactiontypes.transactiontype, " +
                        "COUNT(transactiontypes.transactiontype) " +
                        "FROM disbursals JOIN transactiontypes " +
                        "ON disbursals.transactiontype = transactiontypes.transactionid " +
                        "WHERE datetime > ? AND datetime < ? " +
                        "GROUP BY transactiontypes.transactiontype");
                ps.setTimestamp(1, Timestamp.valueOf(jsonReq.getStartJ8()));
                ps.setTimestamp(2, Timestamp.valueOf(jsonReq.getEndJ8()));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    out.put(rs.getString(1), rs.getInt(2));
                }
                return out;
            } catch (SQLException ex) {
                log.error("SQL error getting tx summary", ex);
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
