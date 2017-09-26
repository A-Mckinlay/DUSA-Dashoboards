package uk.ac.dundee.ac41004.team9.xssf;

import org.apache.poi.ss.usermodel.Cell;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

import static uk.ac.dundee.ac41004.team9.xssf.YoyoTransactionSheet.WEEKLY_DT_FMT;

enum YoyoExcelType {
    Weekly("List of transactions", 7, 2, cell -> { return LocalDateTime.parse(cell.toString(), WEEKLY_DT_FMT); }),
    Disbursals("Disbursals", 2, 1, cell -> { return LocalDateTime.ofInstant(cell.getDateCellValue().toInstant(), ZoneOffset.UTC); });

    public final String sheetName;
    public final int startRow;
    public final int startCol;
    public final Function<Cell, LocalDateTime> dtFunc;

    /**
     * Defines a type of Excel spreadsheet the Yoyo parser can handle.
     *
     * @param sheetName The Excel sheet name which contains the data e.g. "Disbursals".
     * @param startRow The first Excel row number where data can be found.
     * @param startCol The first Excel column number where data can be found.
     * @param dtFunc A function that converts the date/time cell in a row to a Java LocalDateTime.
     */
    YoyoExcelType(String sheetName, int startRow, int startCol, Function<Cell, LocalDateTime> dtFunc) {
        this.sheetName = sheetName;
        this.startRow = startRow - 1;
        this.startCol = startCol - 1;
        this.dtFunc = dtFunc;
    }
}
