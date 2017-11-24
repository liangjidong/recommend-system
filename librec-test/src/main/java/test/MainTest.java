package test;

import common.PropertiesUtils;
import common.RecommendTestUtils;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.math.algorithm.Randoms;
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

    private Configuration conf;
    private AbstractRecommenderSimilarity similarity;
    private RecommenderContext context;

    public MainTest(Configuration conf, AbstractRecommenderSimilarity similarity) {
        this.conf = conf;
        this.similarity = similarity;
    }

    public void test() {
        TextDataModel dataModel = new TextDataModel(conf);
        try {
            dataModel.buildDataModel();
        } catch (Exception e) {
            logger.error("构造数据集异常", e);
            return;
        }
        context = new RecommenderContext(conf, dataModel);
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);

        //测试一些指标,可以自己重写，默认只实现MAE，测试5-50之间的分组
        testMAE();
        testRMSE();
        testRecall();
        testPrecision();
    }

    protected void testPrecision() {

    }

    protected void testRecall() {

    }

    protected void testRMSE() {
    }

    protected void testMAE() {
        try {
            RecommendTestUtils.testMAE(conf, context, "5");
            RecommendTestUtils.testMAE(conf, context, "10");
            RecommendTestUtils.testMAE(conf, context, "15");
            RecommendTestUtils.testMAE(conf, context, "20");
            RecommendTestUtils.testMAE(conf, context, "25");
            RecommendTestUtils.testMAE(conf, context, "30");
            RecommendTestUtils.testMAE(conf, context, "35");
            RecommendTestUtils.testMAE(conf, context, "40");
            RecommendTestUtils.testMAE(conf, context, "45");
            RecommendTestUtils.testMAE(conf, context, "50");
        } catch (Exception e) {
            logger.error("测试MAE指标异常", e);
        }
    }

    public static void main(String[] args) {
//        String dirName = "Data_EXTRACT";
        String dirName = "Data_ML100K";
        Configuration conf = new Configuration();
        //对于classpath，需要先截取，然后才能使用
        conf.set("dfs.data.dir", PropertiesUtils.resourcesDir);
        conf.set("data.input.path", dirName);
        conf.set("data.model.splitter", "testset");
        // 预留的测试数据集应该在训练数据集的路径之下
        conf.set("data.testset.path", dirName + "/testData");
        conf.set("data.model.format", "text");
        conf.set("data.column.format", "UIRT");
        conf.set("data.convert.binarize.threshold", "-1.0");
        conf.set("rec.recommender.similarity.key", "user");
        Randoms.seed(1);
        AbstractRecommenderSimilarity similarity = new PCCSimilarity();
        // RecommenderSimilarity similarity = new CosineSimilarity();
        // AbstractRecommenderSimilarity similarity = new UPSSimilarity();
        // RecommenderSimilarity similarity = new UPSSimilarity();
        HybirdSimilarity3 hybirdSimilarity3 = new HybirdSimilarity3(similarity);
        hybirdSimilarity3.setAssociateRulePath(PropertiesUtils.testOutPath + "userArrayFianlly.txt");
        similarity = hybirdSimilarity3;
        MainTest test = new MainTest(conf, similarity);
        test.test();
    }
}
