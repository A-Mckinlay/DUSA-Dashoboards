package uk.ac.dundee.ac41004.team9.api;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.data.MoneySummaryRow;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/users")
public class Users {

    @Routes.GET(path = "/avgspend", transformer = GSONResponseTransformer.class)
    public static Object avgSpend(Request req, Response res) {
        DateRangeRequest jsonReq = DateRangeRequest.fromBody(req.body());
        if (jsonReq == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        MoneySummaryRow moneySummaryRow = DBConnManager.runWithConnection(conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT " +
                        "AVG(tmptable.cashspent) AS cashspent, " +
                        "AVG(tmptable.discountamount) AS discountamount, " +
                        "AVG(tmptable.totalamount) AS totalamount " +
                        "FROM (SELECT userid, " +
                        "SUM(cashspent) AS cashspent, " +
                        "SUM(discountamount) AS discountamount, " +
                        "SUM(totalamount) AS totalamount " +
                        "FROM disbursals " +
                        "WHERE datetime > ? AND datetime < ? " +
                        "GROUP BY userid) " +
                        "AS tmptable");
                ps.setTimestamp(1, Timestamp.valueOf(jsonReq.getStartJ8()));
                ps.setTimestamp(2, Timestamp.valueOf(jsonReq.getEndJ8()));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    BigDecimal cs = rs.getBigDecimal(1).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal da = rs.getBigDecimal(2).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal ta = rs.getBigDecimal(3).setScale(2, BigDecimal.ROUND_HALF_UP);
                    return new MoneySummaryRow(cs.doubleValue(), da.doubleValue(), ta.doubleValue());
                }
                log.warn("No rows in user avg spend summary!");
                return null;
            } catch (SQLException ex) {
                log.error("SQL error getting user avg spend summary", ex);
                return null;
            }
        });

        if (moneySummaryRow == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        return moneySummaryRow;
    }

}
