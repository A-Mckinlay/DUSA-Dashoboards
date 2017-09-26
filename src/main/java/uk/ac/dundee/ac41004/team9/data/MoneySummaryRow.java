package uk.ac.dundee.ac41004.team9.data;

import lombok.Data;

/**
 * A set of values that represent the three values in a database row or TX summary when it comes to money.
 */
@Data
public class MoneySummaryRow {
    private final double cashspent;
    private final double discountamount;
    private final double totalamount;
}
