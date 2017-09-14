package uk.ac.dundee.ac41004.team9.xssf;

import com.google.common.base.Preconditions;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a "List of transactions" sheet in an XSSF file.
 */
@ParametersAreNonnullByDefault
class YoyoTransactionSheet {

    private static final Logger log = LoggerFactory.getLogger(YoyoTransactionSheet.class);
    private static final int START_ROW = 6;
    private static final DateTimeFormatter DT_FMT = new DateTimeFormatterBuilder()
            .appendPattern("dd/MM/yyyy HH:mm")
            .toFormatter();

    private final Sheet sheet;

    YoyoTransactionSheet(Sheet sheet) {
        Preconditions.checkNotNull(sheet);
        this.sheet = sheet;
    }

    /**
     * Gets a row by its logical index (1-based, like in Excel)
     * @param rowIndex Row to look up
     * @return A row or null if the row is undefined.
     * @throws YoyoParseException if sheet row is malformed.
     */
    YoyoWeekSpreadsheetRow getRow(int rowIndex) throws YoyoParseException {
        Preconditions.checkArgument(rowIndex > 0, "rowIndex must be positive");

        int rowId = START_ROW + rowIndex - 1;
        log.debug("getRow {}", rowId);

        Row row = sheet.getRow(rowId);
        if (row == null) return null;
        log.debug("row exists");

        try {
            LocalDateTime dateTime = LocalDateTime.parse(row.getCell(1).toString(), DT_FMT);
            int retailerRef = (int)row.getCell(2).getNumericCellValue();
            int outletRef = (int)row.getCell(3).getNumericCellValue();
            String outletName = row.getCell(5).toString();
            String userId = row.getCell(6).toString();
            String rawTransactionType = row.getCell(7).toString();
            double cashSpent = Double.parseDouble(row.getCell(8).toString());
            double discountAmount = Double.parseDouble(row.getCell(9).toString());
            double totalAmount = Double.parseDouble(row.getCell(10).toString());

            YoyoWeekSpreadsheetRow outRow = new YoyoWeekSpreadsheetRow(dateTime, retailerRef, outletRef, outletName,
                    userId, rawTransactionType, cashSpent, discountAmount, totalAmount);
            log.debug("parsed: {}", outRow);
            return outRow;
        } catch (DateTimeParseException | NullPointerException | IllegalStateException ex) {
            log.error("Error parsing Excel sheet row.", ex);
            throw new YoyoParseException();
        }
    }

    List<YoyoWeekSpreadsheetRow> getAllRows() throws YoyoParseException {
        return IntStream.rangeClosed(1, sheet.getLastRowNum() + 1 - START_ROW)
                .mapToObj(Unchecked.intFunction(this::getRow))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
