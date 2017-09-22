package uk.ac.dundee.ac41004.team9.api;

import io.drakon.spark.autorouter.Routes;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.util.Pair;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@Routes.PathGroup(prefix = "/api/sales")
@UtilityClass
@Slf4j
public class SalesActivity {

    @Data
    private static class OutletSummaryRow {
        private final String outletname;
        private final double totalamount;
        private final double cashspent;
        private final double discountamount;
    }

    @Routes.GET(path = "/txsummary", transformer = GSONResponseTransformer.class)
    public static Object txSummary(Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        List<OutletSummaryRow> rows = DBConnManager.runWithConnection(conn ->
                getSummaryBetween(conn, p.first, p.second));

        if (rows == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        return rows;
    }

    @Routes.GET(path = "/dailytx", transformer = GSONResponseTransformer.class)
    public static Object dailyTx(Request req, Response res) {
        return periodTx(Period.ofDays(1), req, res);
    }

    @Routes.GET(path = "/weeklytx", transformer = GSONResponseTransformer.class)
    public static Object weeklyTx(Request req, Response res) {
        return periodTx(Period.ofWeeks(1), req, res);
    }

    @Routes.GET(path = "/monthlytx", transformer = GSONResponseTransformer.class)
    public static Object monthlyTx(Request req, Response res) {
        return periodTx(Period.ofMonths(1), req, res);
    }

    @Routes.GET(path = "/yearlytx", transformer = GSONResponseTransformer.class)
    public static Object yearlyTx(Request req, Response res) {
        return periodTx(Period.ofYears(1), req, res);
    }

    private static Object periodTx(Period period, Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        List<Pair<LocalDateTime, LocalDateTime>> intervals = Common.getIntervalsBetween(p.first, period, p.second);

        Map<LocalDateTime, List<OutletSummaryRow>> out = getSummaryOverTime(intervals);
        if (out == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        return out;
    }

    private static Map<LocalDateTime, List<OutletSummaryRow>> getSummaryOverTime(
            List<Pair<LocalDateTime, LocalDateTime>> intervals) {
        Map<LocalDateTime, List<OutletSummaryRow>> map = DBConnManager.runWithConnection(conn -> {
            Map<LocalDateTime, List<OutletSummaryRow>> outMap = new HashMap<>();
            for (Pair<LocalDateTime, LocalDateTime> interval : intervals) {
                List<OutletSummaryRow> rows = getSummaryBetween(conn, interval.first, interval.second);
                if (rows == null) return null; // Error
                outMap.put(interval.first, rows);
            }
            return outMap;
        });

        return map;
    }

    private static List<OutletSummaryRow> getSummaryBetween(Connection conn, LocalDateTime start, LocalDateTime end) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT outletname, SUM(totalamount) AS totalamount, " +
                    "SUM(cashspent) AS cashspent, SUM(discountamount) AS discountamount " +
                    "FROM disbursals JOIN outlets ON disbursals.outletref = outlets.outletref " +
                    "WHERE datetime > ? AND datetime < ? GROUP BY outletname");
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet resultSet = ps.executeQuery();
            List<OutletSummaryRow> fetchRows = new ArrayList<>();
            while (resultSet.next()) {
                String outletname = resultSet.getString(1);
                BigDecimal totalamount = resultSet.getBigDecimal(2).setScale(2, BigDecimal.ROUND_UNNECESSARY);
                BigDecimal cashspent = resultSet.getBigDecimal(3).setScale(2, BigDecimal.ROUND_UNNECESSARY);
                BigDecimal discountamount = resultSet.getBigDecimal(4).setScale(2, BigDecimal.ROUND_UNNECESSARY);
                fetchRows.add(new OutletSummaryRow(outletname,
                        totalamount.doubleValue(),
                        cashspent.doubleValue(),
                        discountamount.doubleValue()));
            }
            return fetchRows;
        } catch (SQLException ex) {
            log.error("SQL error fetching summary rows", ex);
            return null;
        }
    }

}
