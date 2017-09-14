package uk.ac.dundee.ac41004.team9.xssf;


import lombok.Getter;
import lombok.ToString;
import uk.ac.dundee.ac41004.team9.data.TransactionType;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a single row in a Yoyo weekly spreadsheet.
 */
@SuppressWarnings("unused")
@ToString
public class YoyoWeekSpreadsheetRow {

    @Getter
    private LocalDateTime dateTime;

    @Getter
    private Integer retailerRef;

    @Getter
    private Integer outletRef;

    @Getter
    private String outletName;

    @Getter
    private String userId;

    private String transactionType;

    @Getter
    private Double cashSpent;

    @Getter
    private Double discountAmount;

    @Getter
    private Double totalAmount;

    public YoyoWeekSpreadsheetRow(LocalDateTime dateTime, Integer retailerRef, Integer outletRef, String outletName,
                                  String userId, String transactionType, Double cashSpent, Double discountAmount,
                                  Double totalAmount) {
        this.dateTime = dateTime;
        this.retailerRef = retailerRef;
        this.outletRef = outletRef;
        this.outletName = outletName;
        this.userId = userId;
        this.transactionType = transactionType;
        this.cashSpent = cashSpent;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
    }

    /**
     * Gets the transaction type in this row.
     *
     * @return The transaction type, or null if the type entry is invalid.
     */
    public @Nullable
    TransactionType getTransactionType() {
        Optional<TransactionType> ttype = Arrays.stream(TransactionType.values())
                .filter(t -> t.name().equalsIgnoreCase(transactionType))
                .findFirst();
        return ttype.orElse(null);
    }

    @Nullable String getRawTransactionType() {
        return transactionType;
    }

}
