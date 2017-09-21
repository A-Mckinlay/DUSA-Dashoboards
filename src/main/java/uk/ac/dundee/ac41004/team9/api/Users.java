package uk.ac.dundee.ac41004.team9.api;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.data.MoneySummaryRow;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.util.Pair;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/users")
public class Users {

    @Routes.GET(path = "/avgspend", transformer = GSONResponseTransformer.class)
    public static Object avgSpend(Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
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
                ps.setTimestamp(1, Timestamp.valueOf(p.first));
                ps.setTimestamp(2, Timestamp.valueOf(p.second));
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
