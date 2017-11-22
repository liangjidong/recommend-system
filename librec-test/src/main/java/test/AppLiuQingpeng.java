package test;

import common.PropertiesUtils;
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
import similarity.HybirdSimilarityLIUQingpeng;

/**
 * Hello world!
 *
 */
public class AppLiuQingpeng {
	public static void main(String[] args) throws Exception {
		String dirName = "Data_EXTRACT";
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
		Randoms.seed(1);
		TextDataModel dataModel = new TextDataModel(conf);
		dataModel.buildDataModel();
		RecommenderContext context = new RecommenderContext(conf, dataModel);

		conf.set("rec.recommender.similarity.key", "user");
		 AbstractRecommenderSimilarity similarity = new PCCSimilarity();
//		AbstractRecommenderSimilarity similarity = new CosineSimilarity();
		HybirdSimilarityLIUQingpeng sim = new HybirdSimilarityLIUQingpeng(similarity);
		sim.buildSimilarityMatrix(dataModel);
		context.setSimilarity(sim);

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
		Recommender recommender = new UserKNNRecommender();
		// 调用OPNUserKNNRecommonder
		// Recommender recommender = new OPNUserKNNRecommender();
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
