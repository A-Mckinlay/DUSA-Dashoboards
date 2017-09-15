package uk.ac.dundee.ac41004.team9.xssf;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.ac.dundee.ac41004.team9.data.TransactionType;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a single row in a Yoyo weekly spreadsheet.
 */
@SuppressWarnings("unused")
@Data
@RequiredArgsConstructor
public class YoyoWeekSpreadsheetRow {

    private final LocalDateTime dateTime;
    private final Integer retailerRef;
    private final Integer outletRef;
    private final String outletName;
    private final String userId;
    private final String transactionType;
    private final Double cashSpent;
    private final Double discountAmount;
    private final Double totalAmount;

    /**
     * Gets the transaction type in this row.
     *
     * @return The transaction type, or null if the type entry is invalid.
     */
    public @Nullable TransactionType getTransactionType() {
        Optional<TransactionType> ttype = Arrays.stream(TransactionType.values())
                .filter(t -> t.name().equalsIgnoreCase(transactionType))
                .findFirst();
        return ttype.orElse(null);
    }

    @Nullable String getRawTransactionType() {
        return transactionType;
    }

}
