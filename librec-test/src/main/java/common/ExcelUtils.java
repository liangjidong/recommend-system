package common;

import org.apache.poi.hssf.usermodel.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by author on 17-12-6.
 */
public class ExcelUtils {
    public static void main(String[] args) {
        String fileName = "/home/ljd/testout/test.xls";
        String sheetName = "test";
        String[] title = {"", "PCC", "Hybird"};
        String[][] values = {{"5", "1111", "0.111111"}, {"10", "1111", "0.111111"}};
        HSSFWorkbook wb = getHSSFWorkbook(sheetName, title, values, null);
        writeExcel(wb, fileName);
    }

    public static void writeExcel(HSSFWorkbook wb, String fileName) {
        if (wb == null || fileName == null)
            return;
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            wb.write(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HSSFWorkbook getHSSFWorkbook(String sheetName, String[] title, String[][] values, HSSFWorkbook wb) {
        if (wb == null) {
            wb = new HSSFWorkbook();
        }

        HSSFSheet sheet = wb.createSheet(sheetName);

        HSSFRow row = sheet.createRow(0);

        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        HSSFCell cell = null;

        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }

        //对于values,都使用数字
        for (int i = 0; i < values.length; i++) {
            row = sheet.createRow(i + 1);
            for (int j = 0; j < values[i].length; j++) {
                cell = row.createCell(j);
                cell.setCellStyle(style);
                cell.setCellValue(Double.parseDouble(values[i][j]));
            }
        }

        return wb;
    }
}
