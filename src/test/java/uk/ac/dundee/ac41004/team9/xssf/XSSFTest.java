package uk.ac.dundee.ac41004.team9.xssf;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.ac.dundee.ac41004.team9.data.TransactionType;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@DisplayName("XSSF (Excel) Parser")
public class XSSFTest {

    private static List<YoyoWeekSpreadsheetRow> rows = null;

    @BeforeAll
    static void setup() {
        InputStream is = XSSFTest.class.getClassLoader().getResourceAsStream("sample.xlsx");
        if (is == null) throw new IllegalArgumentException("unable to load stream");
        try {
            rows = YoyoXSSFParser.parseSheet(is);
        } catch (YoyoParseException ex) {
            fail(ex);
        }
    }

    @Test
    @DisplayName("test rows parsed")
    void testRowsParsed() {
        assertNotNull(rows);
        assertEquals(3, rows.size(), "correct row count");
        YoyoWeekSpreadsheetRow first = rows.get(0);
        assertEquals(LocalDateTime.of(2017, 8, 28, 7, 8), first.getDateTime());
        assertEquals(1, first.getRetailerRef().intValue());
        assertEquals(2, first.getOutletRef().intValue());
        assertEquals("fake-0001", first.getUserId());
        assertEquals("Payment", first.getRawTransactionType());
        assertEquals(1.0, first.getCashSpent().doubleValue());
        assertEquals(0.0, first.getDiscountAmount().doubleValue());
        assertEquals(1.0, first.getTotalAmount().doubleValue());
    }

    @Test
    @DisplayName("test transaction type parsing")
    void testTransactionType() {
        assertNotNull(rows);
        assertEquals(3, rows.size());
        YoyoWeekSpreadsheetRow first = rows.get(0), second = rows.get(1), third = rows.get(2);
        assertEquals(TransactionType.Payment, first.getTransactionType());
        assertEquals(TransactionType.Redemption, second.getTransactionType());
        assertEquals(TransactionType.Reversal, third.getTransactionType());
    }

}
