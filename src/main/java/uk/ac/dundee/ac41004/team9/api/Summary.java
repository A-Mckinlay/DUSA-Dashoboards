package uk.ac.dundee.ac41004.team9.api;

import io.drakon.spark.autorouter.Routes;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.data.MoneySummaryRow;
import uk.ac.dundee.ac41004.team9.data.TransactionType;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.util.Pair;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/summary")
public class Summary {

    @Routes.GET(path = "/money", transformer = GSONResponseTransformer.class)
    public static Object money(Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
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
                ps.setTimestamp(1, Timestamp.valueOf(p.first));
                ps.setTimestamp(2, Timestamp.valueOf(p.second));
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
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
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
                ps.setTimestamp(1, Timestamp.valueOf(p.first));
                ps.setTimestamp(2, Timestamp.valueOf(p.second));
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

    @Routes.GET(path = "/tx/daily", transformer = GSONResponseTransformer.class)
    public static Object dailyTx(Request req, Response res) {
        return periodTx(Period.ofDays(1), req, res);
    }

    @Routes.GET(path = "/tx/weekly", transformer = GSONResponseTransformer.class)
    public static Object weeklyTx(Request req, Response res) {
        return periodTx(Period.ofWeeks(1), req, res);
    }

    @Routes.GET(path = "/tx/monthly", transformer = GSONResponseTransformer.class)
    public static Object monthlyTx(Request req, Response res) {
        return periodTx(Period.ofMonths(1), req, res);
    }

    @Routes.GET(path = "/tx/yearly", transformer = GSONResponseTransformer.class)
    public static Object yearlyTx(Request req, Response res) {
        return periodTx(Period.ofYears(1), req, res);
    }

    @Routes.GET(path = "/busybot", transformer = GSONResponseTransformer.class)
    public static Object busybot(Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        List<Pair<LocalDateTime, String>> timesAndPlaces = busybotData(p);

        if (timesAndPlaces == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        Map<Integer, AtomicInteger> hoursTx = new HashMap<>();
        IntStream.range(0, 24).forEach(i -> hoursTx.putIfAbsent(i, new AtomicInteger(0)));

        timesAndPlaces
                .parallelStream()
                .forEach(pair -> hoursTx.get(pair.first.getHour()).incrementAndGet());

        double days = p.first.until(p.second, ChronoUnit.DAYS);
        return hoursTx.entrySet().stream()
                .map(ent -> new Pair<>(ent.getKey(), ent.getValue().intValue() / days))
                .collect(Collectors.toMap(pair -> pair.first, pair -> pair.second));
    }

    @Routes.GET(path = "/busybot/venues", transformer = GSONResponseTransformer.class)
    public static Object busybotVenues(Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        List<Pair<LocalDateTime, String>> timesAndPlaces = busybotData(p);

        if (timesAndPlaces == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        Set<String> outlets = timesAndPlaces.parallelStream()
                .map(ent -> ent.second)
                .collect(Collectors.toSet());
        Map<String, Map<Integer, AtomicInteger>> hoursTxByVenue = new HashMap<>(outlets.size());
        outlets.forEach(outlet -> {
            Map<Integer, AtomicInteger> hoursTx = new HashMap<>();
            IntStream.range(0, 24).forEach(i -> hoursTx.put(i, new AtomicInteger(0)));
            hoursTxByVenue.put(outlet, hoursTx);
        });

        timesAndPlaces.parallelStream().forEach(pair ->
                hoursTxByVenue.get(pair.second).get(pair.first.getHour()).incrementAndGet());

        return hoursTxByVenue;
    }

    private static List<Pair<LocalDateTime, String>> busybotData(Pair<LocalDateTime, LocalDateTime> p) {
        return DBConnManager.runWithConnection(conn -> {
            try {
                List<Pair<LocalDateTime, String>> outLs = new ArrayList<>();
                PreparedStatement ps = conn.prepareStatement("SELECT datetime, outletname " +
                        "FROM disbursals JOIN outlets ON disbursals.outletref = outlets.outletref " +
                        "WHERE datetime > ? AND datetime < ?");
                ps.setTimestamp(1, Timestamp.valueOf(p.first));
                ps.setTimestamp(2, Timestamp.valueOf(p.second));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    outLs.add(new Pair<>(
                            rs.getTimestamp(1).toLocalDateTime(),
                            rs.getString(2)
                    ));
                }
                return outLs;
            } catch (SQLException ex) {
                log.error("SQL error in busybot", ex);
                return null;
            }
        });
    }

    private static Object periodTx(Period period, Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        List<Pair<LocalDateTime, LocalDateTime>> intervals = Common.getIntervalsBetween(p.first, period, p.second);

        Map<LocalDateTime, TxSummaryRow> out = getSummaryOverTime(intervals);
        if (out == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        return out;
    }

    private static Map<LocalDateTime, TxSummaryRow> getSummaryOverTime(
            List<Pair<LocalDateTime, LocalDateTime>> intervals) {
        return DBConnManager.runWithConnection(conn -> {
            Map<LocalDateTime, TxSummaryRow> outMap = new HashMap<>();
            for (Pair<LocalDateTime, LocalDateTime> interval : intervals) {
                TxSummaryRow row = getSummaryBetween(conn, interval.first, interval.second);
                if (row == null) return null; // Error
                outMap.put(interval.first, row);
            }
            return outMap;
        });
    }

    private static TxSummaryRow getSummaryBetween(Connection conn, LocalDateTime start, LocalDateTime end) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT transactiontype, COUNT(transactiontype) AS count " +
                    "FROM disbursals " +
                    "WHERE datetime > ? AND datetime < ? GROUP BY transactiontype");
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet resultSet = ps.executeQuery();
            TxSummaryRow.TxSummaryRowBuilder builder = TxSummaryRow.builder();
            while (resultSet.next()) {
                TransactionType type = TransactionType.fromId(resultSet.getInt(1));
                int count = resultSet.getInt(2);
                if (type == null) return null;
                switch (type) {
                    case Payment:
                        builder.payments(count);
                        break;
                    case Redemption:
                        builder.redemptions(count);
                        break;
                    case Reversal:
                        builder.reversals(count);
                        break;
                    default:
                        log.error("Unknown tx type: {}", type);
                        return null; // Invalid
                }
            }
            return builder.build();
        } catch (SQLException ex) {
            log.error("SQL error fetching summary row", ex);
            return null;
        }
    }

    @Data
    @Builder
    private static class TxSummaryRow {
        public final int payments;
        public final int redemptions;
        public final int reversals;
    }

}
