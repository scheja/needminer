package de.janscheurenbrand.needminer.util;

import de.janscheurenbrand.needminer.twitter.Tweet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by janscheurenbrand on 02.12.14.
 */
public class TweetExcelExport {

    private Iterator<Tweet> tweets;
    private File output;
    private Workbook wb;
    private Sheet sheet;
    private CreationHelper createHelper;
    private String documentGroup;
    private String[] headers;

    public TweetExcelExport(String[] headers, String documentGroup, Iterator<Tweet> articles, File output) {
        this.tweets = articles;
        this.output = output;
        this.documentGroup = documentGroup;
        this.headers = headers;
    }

    public void export() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("Export");
        createHelper = wb.getCreationHelper();

        Row row = sheet.createRow(0);

        Cell cell;
        int i = 0;
        for (String cellText : headers) {
            cell = row.createCell(i++);
            cell.setCellValue(cellText);
        }

        // Iterate over tweets with index, starting at 1 because of header
        int j = 1;
        while (tweets.hasNext()) {
            addArticleToSheet(j++, tweets.next());
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(output);
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addArticleToSheet(int index, Tweet tweet) {
        Row row = sheet.createRow(index);

        Cell cell = row.createCell(0);
        cell.setCellValue(documentGroup);

        cell = row.createCell(1);
        cell.setCellValue(tweet.getId());

        cell = row.createCell(2);
        cell.setCellValue(createHelper.createRichTextString(tweet.getText() + " $$" + tweet.getUser().getScreenName()));
    }

}
