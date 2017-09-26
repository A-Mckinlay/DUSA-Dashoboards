package uk.ac.dundee.ac41004.team9.xssf;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.ac.dundee.ac41004.team9.Config;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.db.DBIngest;

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
@UtilityClass
public class YoyoXSSFParser {

    /**
     * Parses a complete XLSX file stream into YoyoWeekSpreadsheetRow objects.
     *
     * Does NOT check for size, so ensure any user provided data is checked for max size.
     *
     * @param strm The stream which provides the sheet.
     * @return A list of spreadsheet row objects.
     * @throws YoyoParseException if XSSF file is invalid.
     */
    public static List<YoyoWeekSpreadsheetRow> parseSheet(InputStream strm) throws YoyoParseException {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(strm);
            return parse(workbook, YoyoExcelType.Weekly);
        } catch (Exception ex) {
            log.error("Exception in sheet parsing", ex);
            throw new YoyoParseException();
        }
    }

    /**
     * Parses a complete XLSX file stream into YoyoWeekSpreadsheetRow objects.
     *
     * Does NOT check for size, so ensure any user provided data is checked for max size.
     *
     * @param path Path of the file which contains the sheet.
     * @return A list of spreadsheet row objects.
     * @throws YoyoParseException if XSSF file is invalid.
     */
    public static List<YoyoWeekSpreadsheetRow> parseSheet(String path) throws YoyoParseException {
        try(OPCPackage pkg = OPCPackage.open(path)) {
            XSSFWorkbook workbook = new XSSFWorkbook(pkg);
            return parse(workbook, YoyoExcelType.Disbursals);
        } catch (IOException | InvalidFormatException ex) {
            log.error("Exception in sheet parsing", ex);
            throw new YoyoParseException();
        }
    }

    /**
     * Actually runs the parse job for either of the parseSheet methods.
     *
     * @param workbook The workbook to extract data from.
     * @param type The sheet type.
     * @return A list of spreadsheet row objects.
     * @throws YoyoParseException if a parser error occurs.
     */
    private static List<YoyoWeekSpreadsheetRow> parse(Workbook workbook, YoyoExcelType type) throws YoyoParseException {
        Sheet sheet = workbook.getSheet(type.sheetName);
        if (sheet == null) throw new IllegalArgumentException("No sheet '" + type.sheetName + "' in Excel file!");
        YoyoTransactionSheet yoyoSheet = new YoyoTransactionSheet(sheet, type);
        List<YoyoWeekSpreadsheetRow> rows = yoyoSheet.getAllRows();
        log.debug("Loaded {} rows from XSSF file. Yay.", rows.size());
        return rows;
    }

    /**
     * Main method that can be run to load a larger Disbursals file, rather than a weekly file, from a file on disk.
     * Takes a single parameter, which is a path to the file to load.
     */
    public static void main(String[] argv) throws Exception {
        if (argv.length < 1) {
            System.out.println("Must provide a path/file name.");
            return;
        }

        // Init needed bits
        log.info("Init.");
        Config.init();
        DBConnManager.init();

        StringBuilder pathBuilder = new StringBuilder();
        boolean first = true;
        for (String s : argv) {
            if (!first) pathBuilder.append(' ');
            first = false;
            pathBuilder.append(s);
        }
        String path = pathBuilder.toString();

        log.info("PARSE START");
        List<YoyoWeekSpreadsheetRow> rows = parseSheet(path);
        log.info("PARSE END");

        DBIngest.uploadRowsToDB(rows);
        log.info("Done.");
    }
}
