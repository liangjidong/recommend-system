package test;

import common.PropertiesUtils;
import common.RecommendTestUtils;
import net.librec.data.DataModel;
import net.librec.recommender.RecommenderContext;
import net.librec.similarity.AbstractRecommenderSimilarity;
import net.librec.similarity.PCCSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public void test() {
        DataModel dataModel = PropertiesUtils.dataModel;
        context = new RecommenderContext(dataModel.getContext().getConf(), dataModel);
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);

        //测试一些指标,可以自己重写，默认只实现MAE，测试5-50之间的分组
        //testMAE(dataModel);
        testRMSE(dataModel);
        testRecall(dataModel);
        testPrecision(dataModel);
    }

    protected void testPrecision(DataModel dataModel) {
        try {
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "5");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "10");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "15");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "20");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "25");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "30");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "35");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "40");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "45");
            RecommendTestUtils.testPrecision(dataModel.getContext().getConf(), context, "50");
        } catch (Exception e) {
            logger.error("测试Precision指标异常", e);
        }
    }

    protected void testRecall(DataModel dataModel) {

    }

    protected void testRMSE(DataModel dataModel) {
    }

    protected void testMAE(DataModel dataModel) {
        try {
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "5");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "10");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "15");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "20");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "25");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "30");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "35");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "40");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "45");
            RecommendTestUtils.testMAE(dataModel.getContext().getConf(), context, "50");
        } catch (Exception e) {
            logger.error("测试MAE指标异常", e);
        }
    }

    public static void main(String[] args) {
        AbstractRecommenderSimilarity similarity = new PCCSimilarity();
//        AbstractRecommenderSimilarity similarity = new CosineSimilarity();
//         AbstractRecommenderSimilarity similarity = new UPSSimilarity();
        // RecommenderSimilarity similarity = new UPSSimilarity();
        HybirdSimilarity3 hybirdSimilarity = new HybirdSimilarity3(similarity);
        //HybirdSimilarity2 hybirdSimilarity = new HybirdSimilarity2(similarity);
        //hybirdSimilarity.setAssociateRulePath(PropertiesUtils.testOutPath + "userArrayFianlly.txt");
        similarity = hybirdSimilarity;
        MainTest test = new MainTest(similarity);
        test.test();
    }
}
