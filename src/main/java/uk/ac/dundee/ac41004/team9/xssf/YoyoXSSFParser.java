package uk.ac.dundee.ac41004.team9.xssf;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Parser for Excel (Horrible Spreadsheet Format) Yoyo files.
 */
@SuppressWarnings("WeakerAccess")
@ParametersAreNonnullByDefault
@Slf4j
public class YoyoXSSFParser {

    private static final String SHEET_NAME = "List of transactions";

    private YoyoXSSFParser() {} // Static

    /**
     * Parses a complete XLSX file stream into YoyoWeekSpreadsheetRow objects.
     *
     * @implNote Does NOT check for size, so ensure any user provided data is checked for max size.
     *
     * @param strm The stream which provides the sheet.
     * @return A list of spreadsheet row objects.
     * @throws YoyoParseException if XSSF file is invalid.
     */
    public static List<YoyoWeekSpreadsheetRow> parseSheet(InputStream strm) throws YoyoParseException {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(strm);
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) throw new IllegalArgumentException("No sheet '" + SHEET_NAME + "' in Excel file!");
            YoyoTransactionSheet yoyoSheet = new YoyoTransactionSheet(sheet);
            List<YoyoWeekSpreadsheetRow> rows = yoyoSheet.getAllRows();
            log.debug("Loaded {} rows from XSSF file. Yay.", rows.size());
            return rows;
        } catch (IOException ex) {
            log.error("IO exception in sheet parsing", ex);
            throw new YoyoParseException();
        }
    }
}
