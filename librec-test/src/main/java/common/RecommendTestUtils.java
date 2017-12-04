package common;

import cluster.KMeansClustering;
import eval.MyPrecisionEvaluator;
import eval.MyRecallEvaluator;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.PrecisionEvaluator;
import net.librec.eval.ranking.RecallEvaluator;
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

    public static void test(Configuration conf, RecommenderContext context, String knn) throws LibrecException {
        conf.set("rec.neighbors.knn.number", knn);
        Recommender recommender = new UserKNNRecommender();
        recommender.setContext(context);
        //进行推荐,得出推荐结果
        //=====rating eval================
        conf.set("rec.recommender.isranking", "false");
        recommender.recommend(context);
        //MAE
        RecommenderEvaluator evaluator = new MAEEvaluator();
        System.out.println(knn + "_MAE:" + recommender.evaluate(evaluator));

        //=====ranking eval===============
//        conf.getBoolean("rec.recommender.isranking");
//        conf.getInt("rec.recommender.ranking.topn", 10);
        conf.set("rec.recommender.isranking", "true");
        conf.set("rec.recommender.ranking.topn", "20");
        recommender.recommend(context);
        //Precision
        evaluator = new MyPrecisionEvaluator();
        evaluator.setTopN(20);
        System.out.println(knn + "_Precision:" + recommender.evaluate(evaluator));
        //Recall
        evaluator = new MyRecallEvaluator();
        evaluator.setTopN(20);
        System.out.println(knn + "_recall:" + recommender.evaluate(evaluator));
        //Coverage
    }

    public static void testMAE(Configuration conf, RecommenderContext context, String knn) throws Exception {
        conf.set("rec.neighbors.knn.number", knn);
        Recommender recommender = new UserKNNRecommender();
        recommender.setContext(context);

        recommender.recommend(context);

        RecommenderEvaluator evaluator = new MAEEvaluator();
        System.out.println(knn + "_MAE:" + recommender.evaluate(evaluator));
    }

    public static void testPrecision(Configuration conf, RecommenderContext context, String knn) throws Exception {
        conf.set("rec.neighbors.knn.number", knn);
        Recommender recommender = new UserKNNRecommender();
        recommender.setContext(context);

        recommender.recommend(context);

        RecommenderEvaluator evaluator = new PrecisionEvaluator();
        evaluator.setTopN(Integer.parseInt(knn));
        System.out.println(knn + "_Precision:" + recommender.evaluate(evaluator));
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
