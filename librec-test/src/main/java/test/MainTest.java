package test;

import common.ExcelUtils;
import common.PropertiesUtils;
import common.RecommendTestUtils;
import net.librec.data.DataModel;
import net.librec.recommender.RecommenderContext;
import net.librec.similarity.AbstractRecommenderSimilarity;
import net.librec.similarity.PCCSimilarity;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.ExperimentResult;
import similarity.HybirdSimilarity3;

/**
 * Created by author on 17-11-23.
 */
public class MainTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AbstractRecommenderSimilarity similarity;
    private RecommenderContext context;

    public MainTest(AbstractRecommenderSimilarity similarity) {
        this.similarity = similarity;
    }


    //每次执行test,都要将结果写入excle中
    public void test() {
        DataModel dataModel = PropertiesUtils.dataModel;
        context = new RecommenderContext(dataModel.getContext().getConf(), dataModel);
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);
        String excelFileName = null;
        if (similarity instanceof HybirdSimilarity3) {
            excelFileName = "/home/ljd/testout/excel_" + ((HybirdSimilarity3) similarity).getLen() + ".xls";
        } else {
            excelFileName = "/home/ljd/testout/excel.xls";
        }
        String sheetName = "out";
        String[] title = {"", "MAE", "Precision", "Recall"};
        //5-50,共10行
        String[][] values = new String[10][title.length];

        try {
            int index = 0;
            for (int i = 5; i <= 50; i += 5) {
                ExperimentResult result = RecommendTestUtils.test(dataModel.getContext().getConf(), context, i + "");
                values[index][0] = i + "";
                values[index][1] = result.getMae() + "";
                values[index][2] = result.getPrecision() + "";
                values[index][3] = result.getRecall() + "";
                index++;
            }
            HSSFWorkbook wb = ExcelUtils.getHSSFWorkbook(sheetName, title, values, null);
            ExcelUtils.writeExcel(wb, excelFileName);
        } catch (Exception e) {
            logger.error("测试指标异常", e);
        }
    }

    public static void main(String[] args) {
        AbstractRecommenderSimilarity similarity = new PCCSimilarity();
//        AbstractRecommenderSimilarity similarity = new CosineSimilarity();
//         AbstractRecommenderSimilarity similarity = new UPSSimilarity();
        // RecommenderSimilarity similarity = new UPSSimilarity();
        HybirdSimilarity3 hybirdSimilarity = new HybirdSimilarity3(similarity);
        hybirdSimilarity.setLen(2);
        similarity = hybirdSimilarity;
        MainTest test = new MainTest(similarity);
        test.test();
//        hybirdSimilarity.setLen(8);
//        HybirdSimilarity2 hybirdSimilarity = new HybirdSimilarity2(similarity);
//        hybirdSimilarity.setAssociateRulePath(PropertiesUtils.testOutPath + "userArrayFianlly.txt");
//        similarity = hybirdSimilarity;
//        MainTest test = new MainTest(similarity);
//        test.test();
    }
}
