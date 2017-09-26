package uk.ac.dundee.ac41004.team9.data;

/**
 * Yoyo transaction types (payment, redemption, reversal)
 */
public enum TransactionType {
    Payment,
    Redemption,
    Reversal;

    public static TransactionType fromId(int id) {
        switch (id) {
            case 0:
                return Payment;
            case 1:
                return Redemption;
            case 2:
                return Reversal;
            default:
                return null;
        }
    }
}
