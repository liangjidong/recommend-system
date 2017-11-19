package com.liangjidong;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.liangjidong.neighborhood.OPNNearestNUserNeighborhood;
import com.liangjidong.similarity.UPSSimiliarity;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * 简单的使用皮尔逊相关系数进行推荐
 * 
 * @author
 *
 */
public class RecommenderIntro {
	public static void main(String[] args) throws IOException, TasteException {
		JDBCRecommender();
	}

	private static void fileRecommenderUserUPUSCF() throws IOException, TasteException {
		String projectDir = System.getProperty("user.dir");
		DataModel model = new FileDataModel(new File(projectDir + "/src/main/ups.csv"));
		UserSimilarity similarity = new UPSSimiliarity(model);
		// 在这之后，先进行分组
		UserNeighborhood neighborhood = new OPNNearestNUserNeighborhood(2, similarity, model);
		Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
		List<RecommendedItem> recommendedItems = recommender.recommend(1, 1);
		for (RecommendedItem recommendedItem : recommendedItems) {
			System.out.println(recommendedItem);
		}
	}

	private static void fileRecommender() throws IOException, TasteException {
		String projectDir = System.getProperty("user.dir");
		DataModel model = new FileDataModel(new File(projectDir + "/src/main/intro.csv"));
		UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
		// 在这之后，先进行分组
		UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, model);
		Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
		List<RecommendedItem> recommendedItems = recommender.recommend(1, 1);
		for (RecommendedItem recommendedItem : recommendedItems) {
			System.out.println(recommendedItem);
		}
	}

	/**
	 * 使用mysql jdbc方式读取偏好值,并推荐
	 * 
	 * @throws TasteException
	 */

	private static void JDBCRecommender() throws TasteException {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setServerName("localhost");
		dataSource.setUser("root");
		dataSource.setPassword("123456");
		dataSource.setDatabaseName("st_test");
		JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "taste_preferences", "user_id", "item_id", "preference", null);
		UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
		UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);

		Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
		List<RecommendedItem> recommendedItems = recommender.recommend(1, 1);
		for (RecommendedItem recommendedItem : recommendedItems) {
			System.out.println(recommendedItem);
		}
	}
}
