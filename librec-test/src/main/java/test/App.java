package test;

import common.PropertiesUtils;
import cluster.UserClustering;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.PrecisionEvaluator;
import net.librec.eval.ranking.RecallEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.math.algorithm.Randoms;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.UserKNNRecommender;
import net.librec.similarity.AbstractRecommenderSimilarity;
import net.librec.similarity.PCCSimilarity;
import recommender.OPNUserKNNRecommender;
import similarity.HybirdSimilarity2;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        String dirName = "Data_EXTRACT";
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", PropertiesUtils.resourcesDir);
        conf.set("data.input.path", dirName);
        conf.set("data.model.splitter", "testset");
        // 预留的测试数据集应该在训练数据集的路径之下(好像不是)
        conf.set("data.testset.path", dirName + "/testData");
        conf.set("data.model.format", "text");
        conf.set("data.column.format", "UIRT");
        conf.set("data.convert.binarize.threshold", "-1.0");
        Randoms.seed(1);
        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();
        // 打印出dataModel
        // 获取训练集合
        // SparseMatrix trainMatrix =
        // dataModel.getDataSplitter().getTrainData();
        // int numUsers = trainMatrix.numRows();
        // for (int i = 0; i < numUsers; i++) {
        // SparseVector thisVector = trainMatrix.row(i);
        // System.out.println(thisVector);
        // }

        RecommenderContext context = new RecommenderContext(conf, dataModel);

        conf.set("rec.recommender.similarity.key", "user");
        AbstractRecommenderSimilarity similarity = new PCCSimilarity();
        // AbstractRecommenderSimilarity similarity = new CosineSimilarity();
        // AbstractRecommenderSimilarity similarity = new JaccardSimilarity();
        // AbstractRecommenderSimilarity similarity = new UPSSimilarity();
        // HybirdSimilarity3 sim = new HybirdSimilarity3(similarity);
        HybirdSimilarity2 sim = new HybirdSimilarity2(similarity);
        sim.buildSimilarityMatrix(dataModel);
        context.setSimilarity(sim);

        // 在这里调用分组，由于我们使用的是AbstractRecommenderSimilarity，所以需要强制转换一下下
        // UserClustering.invokeClustering(dataModel,
        // (AbstractRecommenderSimilarity) similarity);
        // conf.set("rec.neighbors.knn.number", "10");
        // Recommender recommender = new UserKNNRecommender();
        // 调用OPNUserKNNRecommonder
        // Recommender recommender = new OPNUserKNNRecommender();
        // recommender.setContext(context);

        // recommender.recommend(context);

        // RecommenderEvaluator evaluator = new MAEEvaluator();
        // RecommenderEvaluator evaluator = new RMSEEvaluator();
        // System.out.println("MAE:" + recommender.evaluate(evaluator));

        test(conf, context, "5");
        test(conf, context, "10");
        test(conf, context, "15");
        test(conf, context, "20");
        test(conf, context, "25");
        test(conf, context, "30");
        test(conf, context, "35");
        test(conf, context, "40");
        test(conf, context, "45");
        test(conf, context, "50");

        // App.testPrecision(conf, context, "5");
        // App.testPrecision(conf, context, "10");
        // App.testPrecision(conf, context, "15");
        // App.testPrecision(conf, context, "20");
        // App.testPrecision(conf, context, "25");
        // App.testPrecision(conf, context, "30");
        // App.testPrecision(conf, context, "35");
        // App.testPrecision(conf, context, "40");
        // App.testPrecision(conf, context, "45");
        // App.testPrecision(conf, context, "50");
        //
        // App.testRecall(conf, context, "5");
        // App.testRecall(conf, context, "10");
        // App.testRecall(conf, context, "15");
        // App.testRecall(conf, context, "20");
        // App.testRecall(conf, context, "25");
        // App.testRecall(conf, context, "30");
        // App.testRecall(conf, context, "35");
        // App.testRecall(conf, context, "40");
        // App.testRecall(conf, context, "45");
        // App.testRecall(conf, context, "50");

        // recommendation results
        // List recommendedItemList = recommender.getRecommendedList();
        // RecommendedFilter filter = new GenericRecommendedFilter();
        // recommendedItemList = filter.filter(recommendedItemList);
    }

    public static void test(Configuration conf, RecommenderContext context, String knn) throws Exception {
        conf.set("rec.neighbors.knn.number", knn);
        //Recommender recommender = new UserKNNRecommender();
        // 调用OPNUserKNNRecommonder
        UserClustering.invokeClustering(context.getDataModel(),
                (AbstractRecommenderSimilarity) context.getSimilarity());
        Recommender recommender = new OPNUserKNNRecommender();
        recommender.setContext(context);

        recommender.recommend(context);

        RecommenderEvaluator evaluator = new MAEEvaluator();
        // RecommenderEvaluator evaluator = new RMSEEvaluator();
        System.out.println(knn + "_MAE:" + recommender.evaluate(evaluator));
    }

    public static void testPrecision(Configuration conf, RecommenderContext context, String knn) throws Exception {
        conf.set("rec.neighbors.knn.number", knn);
        Recommender recommender = new UserKNNRecommender();
        // 调用OPNUserKNNRecommonder
        // Recommender recommender = new OPNUserKNNRecommender();
        recommender.setContext(context);

        recommender.recommend(context);

        RecommenderEvaluator evaluator = new PrecisionEvaluator();
        evaluator.setTopN(Integer.parseInt(knn));
        System.out.println(knn + "_precision:" + recommender.evaluate(evaluator));
    }

    public static void testRecall(Configuration conf, RecommenderContext context, String knn) throws Exception {
        conf.set("rec.neighbors.knn.number", knn);
        Recommender recommender = new UserKNNRecommender();
        // 调用OPNUserKNNRecommonder
        // Recommender recommender = new OPNUserKNNRecommender();
        recommender.setContext(context);

        recommender.recommend(context);

        RecommenderEvaluator evaluator = new RecallEvaluator();
        evaluator.setTopN(Integer.parseInt(knn));
        System.out.println(knn + "_recall:" + recommender.evaluate(evaluator));
    }
}
