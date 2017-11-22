package cluster;

import net.librec.data.DataModel;
import net.librec.similarity.AbstractRecommenderSimilarity;

/**
 * 基于K-means聚类算法对用户进行聚类
 * Created by author on 17-11-22.
 */
public class KMeansClustering {
    private static int k;//分组数量
    private static int m;//迭代次数
    //数据集
    private static DataModel dataModel;
    // 基于某个相似度计算方法，kmeans中相似度的差值用于聚类的标准
    private static AbstractRecommenderSimilarity similarity;
    //=======K-means算法步骤============
    //1，随机选择k个用户作为聚类的中心
    //2，对余下的每个用户，计算与这k个用户的相似度差值，与最小的分到同一组中
    //3，重新计算中心用户（重要：如何计算）
    //4，迭代2,3两步，直到收敛
    private static int[] centers;//每个分组的中心用户
    private static boolean[][] clusters;//记录哪些用户在同一个分组

    public static void invokeClustering(DataModel dataModel, AbstractRecommenderSimilarity similarity) {
        KMeansClustering.dataModel = dataModel;
        KMeansClustering.similarity = similarity;
        startKMeans();
    }

    private static void startKMeans() {

    }


    /**
     * 计算userID属于哪个组
     *
     * @param userID
     * @return
     */
    public static boolean[] getCluster(int userID) {

        return null;
    }

}
