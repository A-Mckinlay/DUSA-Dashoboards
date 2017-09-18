package uk.ac.dundee.ac41004.team9.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DisbursalsRow {
    private final LocalDateTime datetime;
    private final String outlet;
    private final String userId;
    private final TransactionType transactionType;
    private final BigDecimal cashSpent;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;
}
