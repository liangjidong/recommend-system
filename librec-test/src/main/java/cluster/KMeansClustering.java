package cluster;

import net.librec.data.DataModel;
import net.librec.math.algorithm.Randoms;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SymmMatrix;
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
    private static int[] userCluster;

    /**
     * 执行聚类
     *
     * @param dataModel  数据集
     * @param similarity 用户之间的相似度
     * @param k          分组数量
     * @param m          迭代次数
     */
    public static void invokeClustering(DataModel dataModel, AbstractRecommenderSimilarity similarity, int k, int m) {
        KMeansClustering.dataModel = dataModel;
        KMeansClustering.similarity = similarity;
        KMeansClustering.k = k;
        KMeansClustering.m = m;
        //初始化
        centers = new int[k];
        clusters = new boolean[k][((SparseMatrix) dataModel.getTrainDataSet()).numRows];
        userCluster = new int[((SparseMatrix) dataModel.getTrainDataSet()).numRows];
        startKMeans();
    }

    /**
     * 执行kmeans算法
     */
    private static void startKMeans() {
        //1，随机选择k个用户作为聚类的初始中心(自定义)
        initCentroids();

        //2，循环{（1）聚类，（2）重新计算中心，（3）判断中心是否改变，若为改变则退出循环}
        for (int i = 0; i < m; i++) {
            //先把原来的中心点保存下来
            int[] oldCenters = centers;
            //（1）聚类
            SparseMatrix matrix = (SparseMatrix) dataModel.getTrainDataSet();
            SymmMatrix similarityMatrix = similarity.getSimilarityMatrix();
            for (int j = 0; j < matrix.numRows; j++) {
                //先判断当前的用户是否是中心点
                boolean isCenter = false;
                int x = 0;
                for (x = 0; x < k; x++) {
                    if (centers[x] == j) {
                        isCenter = true;
                        break;
                    }
                }
                if (isCenter) {
                    clusters[x][j] = true;
                    continue;
                }
                int tempCenter = -1;
                double simMin = 0;
                for (x = 0; x < k; x++) {
                    double sim = similarityMatrix.get(j, centers[x]);
                    if (tempCenter == -1 || sim < simMin) {
                        tempCenter = x;
                        simMin = sim;
                    }
                }
                clusters[tempCenter][j] = true;
                userCluster[j] = tempCenter;
            }
            //（2）重新计算中心，（3）判断中心是否改变，若为改变则退出循环
            boolean ischanged = false;
            for (int j = 0; j < k; j++) {
                int newJ = recalculateCenter(j);
                if (newJ != centers[j]) {
                    ischanged = true;
                    centers[j] = newJ;
                }
            }
            if (!ischanged)
                break;
        }
    }

    /**
     * @param index 第index个cluster
     * @return
     */
    private static int recalculateCenter(int index) {
        //找相似度之和最大的那个点
        int userId = -1;
        double totalSim = 0;
        SparseMatrix matrix = (SparseMatrix) dataModel.getTrainDataSet();
        SymmMatrix similarityMatrix = similarity.getSimilarityMatrix();
        for (int i = 0; i < matrix.numRows; i++) {
            double tempSim = 0;
            if (clusters[index][i] == true) {
                //该用户在在当前cluster中
                for (int j = 0; j < matrix.numRows; j++) {
                    if (clusters[index][j] == true) {
                        tempSim += similarityMatrix.get(i, j);
                    }
                }
                if (tempSim > totalSim) {
                    userId = i;
                    totalSim = tempSim;
                }
            }
        }
        return userId;
    }

    /**
     * 初始化中心点
     */
    private static void initCentroids() {
        //自定义实现
        //----random-----
        int numRows = ((SparseMatrix) dataModel.getTrainDataSet()).numRows;
        for (int i = 0; i < k; i++) {
            centers[i] = Randoms.nextInt(0, numRows, null);
        }
    }


    /**
     * 计算userID属于哪个组
     *
     * @param userID
     * @return
     */
    public static boolean[] getCluster(int userID) {
        int x = userCluster[userID];
        return clusters[x];
    }

}
