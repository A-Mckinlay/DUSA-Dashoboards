package uk.ac.dundee.ac41004.team9.xssf;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jooq.lambda.Unchecked;

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
@Slf4j
class YoyoTransactionSheet {

    static final DateTimeFormatter WEEKLY_DT_FMT = new DateTimeFormatterBuilder()
            .appendPattern("dd/MM/yyyy HH:mm")
            .toFormatter();

    private final Sheet sheet;
    private final YoyoExcelType type;

    YoyoTransactionSheet(Sheet sheet, YoyoExcelType type) {
        Preconditions.checkNotNull(sheet);
        Preconditions.checkNotNull(type);
        this.sheet = sheet;
        this.type = type;
    }

    /**
     * Gets a row by its logical index (1-based, like in Excel)
     * @param rowIndex Row to look up
     * @return A row or null if the row is undefined.
     * @throws YoyoParseException if sheet row is malformed.
     */
    YoyoWeekSpreadsheetRow getRow(int rowIndex) throws YoyoParseException {
        Preconditions.checkArgument(rowIndex > 0, "rowIndex must be positive");

        int rowId = type.startRow + rowIndex - 1;
        log.trace("getRow {}", rowId);

        Row row = sheet.getRow(rowId);
        if (row == null || row.getCell(type.startCol) == null) return null;
        log.trace("row exists");

        int startCol = type.startCol;

        try {
            LocalDateTime dateTime = type.dtFunc.apply(row.getCell(startCol));
            int retailerRef = (int)row.getCell(startCol + 1).getNumericCellValue();
            int outletRef = (int)row.getCell(startCol + 2).getNumericCellValue();
            String outletName = row.getCell(startCol + 4).toString();
            String userId = row.getCell(startCol + 5).toString();
            String rawTransactionType = row.getCell(startCol + 6).toString();
            double cashSpent = Double.parseDouble(row.getCell(startCol + 7).toString());
            double discountAmount = Double.parseDouble(row.getCell(startCol + 8).toString());
            double totalAmount = Double.parseDouble(row.getCell(startCol + 9).toString());

            YoyoWeekSpreadsheetRow outRow = new YoyoWeekSpreadsheetRow(dateTime, retailerRef, outletRef, outletName,
                    userId, rawTransactionType, cashSpent, discountAmount, totalAmount);
            log.trace("parsed: {}", outRow);
            return outRow;
        } catch (DateTimeParseException | NullPointerException | IllegalStateException ex) {
            log.error("Error parsing Excel sheet row {}.", rowIndex);
            log.error("Exception from ^", ex);
            throw new YoyoParseException();
        }
    }

    List<YoyoWeekSpreadsheetRow> getAllRows() throws YoyoParseException {
        return IntStream.rangeClosed(1, sheet.getLastRowNum() + 1 - type.startRow)
                .mapToObj(Unchecked.intFunction(this::getRow))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
