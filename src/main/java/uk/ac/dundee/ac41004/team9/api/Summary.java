package uk.ac.dundee.ac41004.team9.api;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.data.MoneySummaryRow;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

}
