package pojo;

/**
 * Created by author on 17-12-6.
 */
public class ExcelInputData {
    private String fileName;
    private String sheetName;
    private String[] title;
    private String[][] values;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String[] getTitle() {
        return title;
    }

    public void setTitle(String[] title) {
        this.title = title;
    }

    public String[][] getValues() {
        return values;
    }

    public void setValues(String[][] values) {
        this.values = values;
    }
}
