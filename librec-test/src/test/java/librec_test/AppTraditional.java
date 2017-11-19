package librec_test;

import common.PropertiesUtils;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.math.algorithm.Randoms;
import net.librec.recommender.RecommenderContext;
import net.librec.similarity.CosineSimilarity;
import net.librec.similarity.RecommenderSimilarity;

public class AppTraditional {
	public static void main(String[] args) throws Exception {
		String dirName = "Data_EXTRACT";
		Configuration conf = new Configuration();
		conf.set("dfs.data.dir", PropertiesUtils.mainDir);
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
		// RecommenderSimilarity similarity = new PCCSimilarity();
		RecommenderSimilarity similarity = new CosineSimilarity();
		// RecommenderSimilarity similarity = new JaccardSimilarity();
		// RecommenderSimilarity similarity = new UPSSimilarity();
		similarity.buildSimilarityMatrix(dataModel);
		context.setSimilarity(similarity);

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
		//
		App.test(conf, context, "5");
		App.test(conf, context, "10");
		App.test(conf, context, "15");
		App.test(conf, context, "20");
		App.test(conf, context, "25");
		App.test(conf, context, "30");
		App.test(conf, context, "35");
		App.test(conf, context, "40");
		App.test(conf, context, "45");
		App.test(conf, context, "50");

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
}
