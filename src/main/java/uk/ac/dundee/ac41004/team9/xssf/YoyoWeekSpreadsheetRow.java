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
        // Deal with inconsistencies in Disbursals.xlsx
        if (transactionType.equalsIgnoreCase("Refund")) return TransactionType.Reversal;
        if (transactionType.equalsIgnoreCase("Discounted payment")) return TransactionType.Redemption;

        Optional<TransactionType> ttype = Arrays.stream(TransactionType.values())
                .filter(t -> t.name().equalsIgnoreCase(transactionType))
                .findFirst();
        return ttype.orElse(null);
    }

    /**
     * Gets the raw transaction type ID number.
     *
     * @return Raw TX type ID.
     */
    @Nullable String getRawTransactionType() {
        return transactionType;
    }

}
