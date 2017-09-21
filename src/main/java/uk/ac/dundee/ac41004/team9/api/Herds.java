package uk.ac.dundee.ac41004.team9.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import io.drakon.spark.autorouter.Routes;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.data.DisbursalsRow;
import uk.ac.dundee.ac41004.team9.data.Outlet;
import uk.ac.dundee.ac41004.team9.data.TransactionType;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/herds")
public class Herds {

    @Routes.GET(path = "/chords", transformer = GSONResponseTransformer.class)
    public static Object chords(Request req, Response res) {
        Pair<LocalDateTime, LocalDateTime> p = Common.getStartEndFromRequest(req);
        if (p == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        Set<Outlet> outletSet = new HashSet<>();
        List<Pair<DisbursalsRow, Outlet>> rows = DBConnManager.runWithConnection(conn -> {
            try {
                List<Pair<DisbursalsRow, Outlet>> allRows = new ArrayList<>();
                PreparedStatement ps = conn.prepareStatement("SELECT datetime, disbursals.outletref, outletname, " +
                        "userid, transactiontype, cashspent, discountamount, totalamount " +
                        "FROM disbursals JOIN outlets ON disbursals.outletref = outlets.outletref " +
                        "WHERE datetime > ? AND datetime < ?");
                ps.setTimestamp(1, Timestamp.valueOf(p.first));
                ps.setTimestamp(2, Timestamp.valueOf(p.second));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Outlet ol = new Outlet(rs.getInt(2), rs.getString(3));
                    outletSet.add(ol);
                    allRows.add(new Pair<>(
                        new DisbursalsRow(
                            rs.getTimestamp(1).toLocalDateTime(),
                            rs.getString(3),
                            rs.getString(4),
                            TransactionType.fromId(rs.getInt(5)),
                            rs.getBigDecimal(6),
                            rs.getBigDecimal(7),
                            rs.getBigDecimal(8)
                        ), ol
                    ));
                }
                return allRows;
            } catch (SQLException ex) {
                log.error("SQL error in chords", ex);
                return null;
            }
        });

        if (rows == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        Multimap<String, Outlet> userLocs = Multimaps.synchronizedSetMultimap(
                MultimapBuilder.hashKeys().hashSetValues().build());

        rows.parallelStream().forEach(pair -> userLocs.put(pair.first.getUserId(), pair.second));

        int[][] matrix = new int[outletSet.size()][outletSet.size()];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                matrix[i][j] = 0;
            }
        }

        Outlet[] EMPTY_OUTLET_ARR = new Outlet[]{};
        List<Outlet> outlets = new ArrayList<>(outletSet);
        synchronized (userLocs) {
            userLocs.asMap().values().parallelStream().forEach(usrOutlets -> {
                // Based on https://stackoverflow.com/q/13805759
                Outlet[] ols = usrOutlets.toArray(EMPTY_OUTLET_ARR);
                for (int i = 0; i < ols.length; i++) {
                    for (int j = i + 1; j < ols.length; j++) {
                        int a = outlets.indexOf(ols[i]);
                        int b = outlets.indexOf(ols[j]);
                        // a -> b
                        synchronized (matrix[a]) { matrix[a][b] += 1; }
                        // b -> a
                        synchronized (matrix[b]) { matrix[b][a] += 1; }
                    }
                }
            });
        }

        String[] EMPTY_STRING_ARR = new String[]{};
        return new ChordsResponse(matrix,
                outlets.parallelStream().map(Outlet::getName).collect(Collectors.toList()).toArray(EMPTY_STRING_ARR));
    }

    @RequiredArgsConstructor
    private static class ChordsResponse {
        public final int[][] matrix;
        public final String[] outlets;
    }

}
