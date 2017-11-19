package com.liangjidong;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;

import com.liangjidong.common.UserClustering;
import com.liangjidong.neighborhood.OPNNearestNUserNeighborhood;
import com.liangjidong.similarity.UPSSimiliarity;

/**
 * 对推荐程序进行评价:
 * 
 * @author ljd
 *
 */
public class RecommenderEvaluatorTest {
	public static void main(String[] args) throws IOException, TasteException {
		// recommenderIRStatsEvaluatorTest();
		// 未加入分组思想 MAE 0.779703248131861
		// 加入分组思想MAE 1.2205519676208496
		// 未加入分组思想，100个邻居MAE= 0.7614161425381712
		// 加入分组思想，100个邻居MAE=0.7419142251619912(最好的效果)
		recommenderEvaluatorUPUCCFTest1(20);
		// recommenderEvaluatorUPUCCFTest();
	}

	/**
	 * 使用查准率和查全率
	 * 
	 * @throws IOException
	 * @throws TasteException
	 */
	private static void recommenderIRStatsEvaluatorTest() throws IOException, TasteException {
		String projectDir = System.getProperty("user.dir");
		RandomUtils.useTestSeed();// 生成可重复的结果
		DataModel model = new FileDataModel(new File(projectDir + "/src/main/intro.csv"));

		RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
		RecommenderBuilder builder = new RecommenderBuilder() {

			public Recommender buildRecommender(DataModel model) throws TasteException {
				// TODO Auto-generated method stub
				UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
				UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, model);

				Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

				return recommender;
			}
		};

		// 其中CHOOSE_THRESHOLD表示让系统计算评分阈值。如果我将3分作为评分阈值也是可以的
		/**
		 * 
		 */
		/*
		 * IRStatistics stats = evaluator.evaluate(builder, null, model, null,
		 * 2, GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0);
		 */
		IRStatistics stats = evaluator.evaluate(builder, null, model, null, 2, 3, 1.0);
		System.out.println(stats.getPrecision());
		System.out.println(stats.getRecall());
	}

	/**
	 * 使用平均绝对误差MAE
	 * 
	 * @throws IOException
	 * @throws TasteException
	 */
	private static void recommenderEvaluatorTest() throws IOException, TasteException {
		String projectDir = System.getProperty("user.dir");
		RandomUtils.useTestSeed();// 生成可重复的结果
		// DataModel model = new FileDataModel(new File(projectDir +
		// "/src/main/intro.csv"));
		DataModel model = new FileDataModel(new File(projectDir + "/src/main/ua.base"));

		// 使用平均绝对误差MAE 评测
		RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
		RecommenderBuilder builder = new RecommenderBuilder() {

			public Recommender buildRecommender(DataModel model) throws TasteException {
				// TODO Auto-generated method stub
				UserSimilarity similarity = new UPSSimiliarity(model);
				UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, model);

				Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

				return recommender;
			}
		};
		double score = evaluator.evaluate(builder, null, model, 0.8, 1.0);
		System.out.println(score);
	}

	/**
	 * 由于slopeOne算法在mahout0.9版本已经被去除，所以就不实验了
	 * 
	 * @deprecated
	 * @throws IOException
	 * @throws TasteException
	 */
	private static void slopeOneRecommenderEvaluatorTest() throws IOException, TasteException {
		String projectDir = System.getProperty("user.dir");
		RandomUtils.useTestSeed();// 生成可重复的结果
		// DataModel model = new FileDataModel(new File(projectDir +
		// "/src/main/intro.csv"));
		DataModel model = new FileDataModel(new File(projectDir + "/src/main/ua.base"));

		// 使用平均绝对误差MAE 评测
		RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
		RecommenderBuilder builder = new RecommenderBuilder() {

			public Recommender buildRecommender(DataModel model) throws TasteException {
				// TODO Auto-generated method stub
				return null;
			}
		};
		double score = evaluator.evaluate(builder, null, model, 0.8, 1.0);
		System.out.println(score);
	}

	/**
	 * 对UPUC——CF算法进行MAE测评
	 * 
	 * @param times
	 * @throws IOException
	 * @throws TasteException
	 */
	private static void recommenderEvaluatorUPUCCFTest1(int times) throws IOException, TasteException {
		// 首先加载实验数据
		String projectDir = System.getProperty("user.dir");
		RandomUtils.useTestSeed();// 生成可重复的结果
		// DataModel model = new FileDataModel(new File(projectDir +
		// "/src/main/intro.csv"));
		DataModel model = new FileDataModel(new File(projectDir + "/src/main/u.data"));
		// 使用平均绝对误差MAE 评测
		// 定义相似度计算方法
		UserSimilarity similarity = new UPSSimiliarity(model);
		// 用户分组
		UserClustering.invokeClustering(model, (UPSSimiliarity) similarity);
		double minScore = Double.MAX_VALUE;
		for (int i = 0; i < 1; i++) {
			double score = recommenderEvaluatorUPUCCFTest(model, similarity);
			if (score < minScore) {
				minScore = score;
			}
		}
		System.out.println("minScore=" + minScore);
	}

	/**
	 * 使用平均绝对误差MAE计算UPUC-CF的精度
	 * 
	 * @throws IOException
	 * @throws TasteException
	 */
	private static double recommenderEvaluatorUPUCCFTest(DataModel model, final UserSimilarity similarity)
			throws IOException, TasteException {
		// final String projectDir = System.getProperty("user.dir");
		// RandomUtils.useTestSeed();// 生成可重复的结果
		// // DataModel model = new FileDataModel(new File(projectDir +
		// // "/src/main/intro.csv"));
		// DataModel model = new FileDataModel(new File(projectDir +
		// "/src/main/u.data"));

		// 使用平均绝对误差MAE 评测
		RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
		RecommenderBuilder builder = new RecommenderBuilder() {

			public Recommender buildRecommender(DataModel model) throws TasteException {
				// TODO Auto-generated method stub

				UserNeighborhood neighborhood = new OPNNearestNUserNeighborhood(100, similarity, model);
				// UserNeighborhood neighborhood = new
				// NearestNUserNeighborhood(100, similarity, model);

				Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

				return recommender;
			}
		};
		// 未使用到
		// DataModelBuilder dataModelBuilder = new DataModelBuilder() {
		//
		// public DataModel buildDataModel(FastByIDMap<PreferenceArray>
		// trainingData) {
		// // TODO Auto-generated method stub
		// try {
		// return new FileDataModel(new File(projectDir + "/src/main/ua.test"));
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return null;
		// }
		// };
		double score = evaluator.evaluate(builder, null, model, 0.8, 1.0);
		System.out.println(score);
		return score;
	}

}
