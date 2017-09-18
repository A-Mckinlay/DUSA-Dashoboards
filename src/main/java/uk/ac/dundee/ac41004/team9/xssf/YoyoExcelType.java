package uk.ac.dundee.ac41004.team9.xssf;

import org.apache.poi.ss.usermodel.Cell;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

import static uk.ac.dundee.ac41004.team9.xssf.YoyoTransactionSheet.WEEKLY_DT_FMT;

enum YoyoExcelType {
    Weekly("List of transactions", 6, 1, cell -> {return LocalDateTime.parse(cell.toString(), WEEKLY_DT_FMT);}),
    Disbursals("Disbursals", 1, 0, cell -> {return LocalDateTime.ofInstant(cell.getDateCellValue().toInstant(), ZoneOffset.UTC);});

    public final String sheetName;
    public final int startRow;
    public final int startCol;
    public final Function<Cell, LocalDateTime> dtFunc;

    YoyoExcelType(String sheetName, int startRow, int startCol, Function<Cell, LocalDateTime> dtFunc) {
        this.sheetName = sheetName;
        this.startRow = startRow;
        this.startCol = startCol;
        this.dtFunc = dtFunc;
    }
}
