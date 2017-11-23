package test;

import common.PropertiesUtils;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.math.algorithm.Randoms;
import net.librec.recommender.RecommenderContext;
import net.librec.similarity.AbstractRecommenderSimilarity;
import net.librec.similarity.CosineSimilarity;
import similarity.HybirdSimilarity2;

/**
 * Hello world!
 *
 */
public class App1 {
	public static void main(String[] args) throws Exception {
		String dirName = "Data_EXTRACT";
		Configuration conf = new Configuration();
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
		// AbstractRecommenderSimilarity similarity = new PCCSimilarity();
		AbstractRecommenderSimilarity similarity = new CosineSimilarity();
		// RecommenderSimilarity similarity = new JaccardSimilarity();
		// RecommenderSimilarity similarity = new UPSSimilarity();
		// HybirdSimilarity3 sim = new HybirdSimilarity3(similarity);
		HybirdSimilarity2 sim = new HybirdSimilarity2(similarity);
		sim.buildSimilarityMatrix(dataModel);
		context.setSimilarity(sim);

		// 在这里调用分组，由于我们使用的是AbstractRecommenderSimilarity，所以需要强制转换一下下
		// UserClustering.invokeClustering(dataModel,
		// (AbstractRecommenderSimilarity) similarity);

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
		// recommendation results
		// List recommendedItemList = recommender.getRecommendedList();
		// RecommendedFilter filter = new GenericRecommendedFilter();
		// recommendedItemList = filter.filter(recommendedItemList);
	}
}
