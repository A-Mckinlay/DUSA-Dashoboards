package uk.ac.dundee.ac41004.team9.models;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import uk.ac.dundee.ac41004.team9.data.DisbursalsRow;
import uk.ac.dundee.ac41004.team9.data.TransactionType;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

@UtilityClass
@Slf4j
public class Disbursals {

    /**
     * Fetches all disbursals rows between a start and end date.
     *
     * @param start Start of time range.
     * @param end End of time range.
     * @return The returned rows, or null if something went wrong.
     */
    public static List<DisbursalsRow> getRowsBetween(LocalDateTime start, LocalDateTime end) {
        return DBConnManager.runWithConnection(conn -> {
            try {
                List<DisbursalsRow> ls = new ArrayList<>();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT datetime, outletname, userid, " +
                        "transactiontype, cashspent, discountamount, totalamount " +
                        "FROM disbursals JOIN outlets ON disbursals.outletref = outlets.outletref " +
                        "WHERE datetime > ? AND datetime < ?");
                ps.setTimestamp(1, Timestamp.valueOf(start));
                ps.setTimestamp(2, Timestamp.valueOf(end));
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    LocalDateTime dateTime = resultSet.getTimestamp(1).toLocalDateTime();
                    String outlet = resultSet.getString(2);
                    String userId = resultSet.getString(3);
                    TransactionType transactionType = TransactionType.fromId(resultSet.getInt(4));
                    BigDecimal cashSpent = resultSet.getBigDecimal(5).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal discountAmount = resultSet.getBigDecimal(6).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal totalAmount = resultSet.getBigDecimal(7).setScale(2, BigDecimal.ROUND_HALF_UP);
                    ls.add(new DisbursalsRow(dateTime, outlet, userId, transactionType, cashSpent, discountAmount,
                            totalAmount));
                }
                return ls;
            } catch (SQLException ex) {
                log.error("SQL error fetching within datetime range", ex);
                return null;
            }
        });
    }

}
