package uk.ac.dundee.ac41004.team9.xssf;

import lombok.Getter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javafunk.excelparser.SheetParser;
import org.javafunk.excelparser.annotations.ExcelField;
import org.javafunk.excelparser.annotations.ExcelObject;
import org.javafunk.excelparser.annotations.ParseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.dundee.ac41004.team9.data.TransactionType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Parser for Excel (Horrible Spreadsheet Format) Yoyo files.
 */
@ParametersAreNonnullByDefault
public class YoyoXSSFParser {

    private static final Logger log = LoggerFactory.getLogger(YoyoXSSFParser.class);
    private static final String SHEET_NAME = "List of transactions";

    private YoyoXSSFParser() {} // Static

    /**
     * Parses a complete XLSX file stream into YoyoWeekSpreadsheetRow objects.
     *
     * @implNote Does NOT check for size, so ensure any user provided data is checked for max size.
     *
     * @param strm The stream which provides the sheet.
     * @return A list of spreadsheet row objects.
     */
    public static @Nullable List<YoyoWeekSpreadsheetRow> parseSheet(InputStream strm) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(strm);
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) throw new IllegalArgumentException("No sheet '" + SHEET_NAME + "' in Excel file!");
            return new SheetParser().createEntity(sheet, YoyoWeekSpreadsheetRow.class, ex -> {
                log.error("Error in sheet parsing", ex);
                throw new IllegalArgumentException("Error in sheet parsing", ex);
            });
        } catch (IOException ex) {
            log.error("IO exception in sheet parsing", ex);
            return null;
        }
    }

    /**
     * Represents a single row in a Yoyo weekly spreadsheet.
     */
    @ExcelObject(parseType = ParseType.ROW, start = 7)
    public static class YoyoWeekSpreadsheetRow {

        @ExcelField(position = 2)
        @Getter
        private LocalDateTime dateTime;

        @ExcelField(position = 3)
        @Getter
        private Integer retailerRef;

        @ExcelField(position = 4)
        @Getter
        private Integer outletRef;

        @ExcelField(position = 7)
        @Getter
        private String userId;

        @ExcelField(position = 8)
        private String transactionType;

        @ExcelField(position = 9)
        @Getter
        private Double cashSpent;

        @ExcelField(position = 10)
        @Getter
        private Double discountAmount;

        @ExcelField(position = 11)
        @Getter
        private Double totalAmount;

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
}
