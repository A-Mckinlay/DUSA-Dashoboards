package uk.ac.dundee.ac41004.team9.db;

import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import uk.ac.dundee.ac41004.team9.util.Pair;
import uk.ac.dundee.ac41004.team9.xssf.YoyoWeekSpreadsheetRow;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;

import static uk.ac.dundee.ac41004.team9.db.DBConnManager.runWithConnection;

@Slf4j
public class DBIngest {


    private DBIngest() {} // Static

    public static boolean uploadRowsToDB(List<YoyoWeekSpreadsheetRow> data) {
        Boolean bool = runWithConnection(conn -> {
            try {
                conn.setAutoCommit(false);

                // Outlet names
                final PreparedStatement ps1 = conn.prepareStatement("INSERT INTO outlets(outletref, outletname)" +
                        " VALUES (?, ?) ON CONFLICT DO NOTHING;");
                data.stream()
                        .map(row -> new Pair<>(row.getOutletRef(), row.getOutletName()))
                        .forEach(Unchecked.consumer(pair -> {
                            ps1.setInt(1, pair.first);
                            ps1.setString(2, pair.second);
                            ps1.executeUpdate();
                        }));

                // Disbursals
                final PreparedStatement ps = conn.prepareStatement("INSERT INTO disbursals(datetime, outletref," +
                        " userid, transactiontype, cashspent, discountamount, totalamount)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)");
                for (YoyoWeekSpreadsheetRow row : data) {
                    ps.setTimestamp(1, new Timestamp(row.getDateTime().toEpochSecond(ZoneOffset.UTC)));
                    ps.setInt(2, row.getOutletRef());
                    ps.setString(3, row.getUserId());
                    //noinspection ConstantConditions
                    ps.setInt(4, row.getTransactionType().ordinal());
                    ps.setDouble(5, row.getCashSpent());
                    ps.setDouble(6, row.getDiscountAmount());
                    ps.setDouble(7, row.getTotalAmount());
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                log.warn("SQL exception occured in uploadRowsToDB", ex);
                try {
                    conn.rollback();
                } catch (SQLException ex2) {
                    log.error("SQL error during rollback!", ex2);
                    throw new RuntimeException(ex2);
                }
                return false;
            }
        });

        if (bool == null) return false;
        return bool;
    }

}
