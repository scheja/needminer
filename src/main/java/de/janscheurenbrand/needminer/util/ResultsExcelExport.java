package de.janscheurenbrand.needminer.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by janscheurenbrand on 02.12.14.
 */
public class ResultsExcelExport {

    private List<HashMap<String, Object>> results;
    private File output;
    private Workbook wb;
    private Sheet sheet;

    public ResultsExcelExport(List<HashMap<String, Object>> results, File output) {
        this.results = results;
        this.output = output;
    }

    public void export() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("Export");

        Row row = sheet.createRow(0);

        Cell cell;
        int i = 0;
        for (String cellText : results.get(0).keySet()) {
            cell = row.createCell(i++);
            cell.setCellValue(cellText);
        }

        // Iterate over tweets with index, starting at 1 because of header
        int j = 1;
        for (HashMap<String, Object> result : results) {
            addArticleToSheet(j++, result);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(output);
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addArticleToSheet(int index, HashMap<String,Object> result) {
        Row row = sheet.createRow(index);

        Cell cell;

        int j = 0;
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            cell = row.createCell(j);
            Object o = entry.getValue();
            if (o instanceof String) {
                cell.setCellValue((String) o);
            }

            if (o instanceof Double) {
                cell.setCellValue((Double) o);
            }
            j++;
        }

    }

}
