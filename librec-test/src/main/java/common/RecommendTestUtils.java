package common;

import cluster.KMeansClustering;
import net.librec.conf.Configuration;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.UserKNNRecommender;
import net.librec.similarity.AbstractRecommenderSimilarity;
import recommender.KMeansKNNRecommender;

/**
 * Created by author on 17-11-23.
 */
public class RecommendTestUtils {
    public static void testMAE(Configuration conf, RecommenderContext context, String knn) throws Exception {
        conf.set("rec.neighbors.knn.number", knn);
        Recommender recommender = new UserKNNRecommender();
        recommender.setContext(context);

        recommender.recommend(context);

        RecommenderEvaluator evaluator = new MAEEvaluator();
        System.out.println(knn + "_MAE:" + recommender.evaluate(evaluator));
    }

    public static void testMAEWithKMeans(Configuration conf, RecommenderContext context, String knn) throws Exception {
        conf.set("rec.neighbors.knn.number", knn);

        KMeansClustering.invokeClustering(context.getDataModel(),
                (AbstractRecommenderSimilarity) context.getSimilarity(), 10, 10);

        Recommender recommender = new KMeansKNNRecommender();

        recommender.setContext(context);
        recommender.recommend(context);

        RecommenderEvaluator evaluator = new MAEEvaluator();
        System.out.println(knn + "_MAE:" + recommender.evaluate(evaluator));
    }
}
