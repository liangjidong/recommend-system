package similarity;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import common.PropertiesUtils;
import net.librec.data.DataModel;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;
import net.librec.math.structure.SymmMatrix;
import net.librec.similarity.AbstractRecommenderSimilarity;
import patternMining.ItemAssociateRuleMining;
import patternMining.ItemScorePreprocess;

import java.lang.reflect.Field;
import java.util.*;

public class HybirdSimilarity3 extends AbstractRecommenderSimilarity {
    //应该由外部提供
    private String associateRulePath = PropertiesUtils.testOutPath + "userArrayFianlly.txt";
    private static final double MIN_RATE = 1.0;
    private static final double MAX_RATE = 5.0;
    private DataModel dataModel;
    // 贡献因子计算---项目打分的差异性因子
    private double[] DU;
    private double[][] gx;// 贡献因子

    private double maxRate = 4.0;
    private double minRate = 1.5;
    private double midRate = 3.0;
    private double minRatio = 0.25;
    private double maxRatio = 0.5;
    private double simRatio = 0.4;
    private Double[] userMeanRate;// 用户打分均值
    private Double[] itemMeanRate;// 项目打分均值
    private AbstractRecommenderSimilarity baseSimilarity;
    private Map<Integer, HashMap<Integer, Integer>> map;// 模式挖掘结果

    private List<int[]> ItemChanges;

    /**
     * 将传统的相似度算法和模式挖掘的用户分组结果传入
     *
     * @param baseSimilarity
     */
    public HybirdSimilarity3(AbstractRecommenderSimilarity baseSimilarity) {
        super();
        this.baseSimilarity = baseSimilarity;
    }

    public HybirdSimilarity3(double maxRate, double minRate, AbstractRecommenderSimilarity baseSimilarity) {
        super();
        this.maxRate = maxRate;
        this.minRate = minRate;
        this.baseSimilarity = baseSimilarity;
    }

    /**
     * 设置模式挖掘文件路径:默认 {@code PropertiesUtils.testOutPath + "userArrayFianlly.txt"}
     *
     * @param associateRulePath
     */
    public void setAssociateRulePath(String associateRulePath) {
        this.associateRulePath = associateRulePath;
    }

    @Override
    protected double getSimilarity(List<? extends Number> thisList, List<? extends Number> thatList) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 已有实现通过调用{@code getCorrelation()获得两个用户间的相似度} <br/>
     * 现在需要通过挖掘的来计算间接相似度
     */
    @Override
    public void buildSimilarityMatrix(DataModel dataModel) {
        this.dataModel = dataModel;
        // 1,获取map,itemChanges,（应该错了，内部编号和外部编号不同，需要将外部编号和内部编号统一，已经实现）
        map = new ItemAssociateRuleMining(2).getItemSet(associateRulePath);

        System.out.println(map.get(1));
        System.out.println(map.get(-1));

        ItemChanges = ItemScorePreprocess.getListOfInterestingItems(dataModel);
        // 2，根据map重新构造评分矩阵（包含预测评分）----核心部分
        SparseMatrix oldMatrix = dataModel.getDataSplitter().getTrainData();
        // 计算DU
        // calculateDU(oldMatrix);
        // Table {row-id, col-id, rate}
        Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
        // Map {col-id, multiple row-id}: used to fast build a rating matrix
        Multimap<Integer, Integer> colMap = HashMultimap.create();
        Double[] maxArray = new Double[oldMatrix.numColumns];
        Double[] minArray = new Double[oldMatrix.numColumns];
        Double[] maxArrayUser = new Double[oldMatrix.numColumns];
        Double[] minArrayUser = new Double[oldMatrix.numColumns];
        // 初始化用户打分均值，项目打分均值
        userMeanRate = new Double[oldMatrix.numRows];
        itemMeanRate = new Double[oldMatrix.numColumns];
        for (int j = 0; j < oldMatrix.numColumns; j++) {
            SparseVector column = oldMatrix.column(j);
            itemMeanRate[j] = column.mean();
            maxArray[j] = getArrayMax(column.getData());
            minArray[j] = getArrayMin(column.getData());
        }
        for (int i = 0; i < oldMatrix.numRows; i++) {
            SparseVector row = oldMatrix.row(i);
            userMeanRate[i] = row.mean();
            maxArrayUser[i] = getArrayMax(row.getData());
            minArrayUser[i] = getArrayMin(row.getData());
        }
        for (int i = 0; i < oldMatrix.numRows; i++) {
            // 找哪些没有打分的，预测打分
            for (int j = 0; j < oldMatrix.numColumns; j++) {
                double rate;
                if ((rate = oldMatrix.get(i, j)) != 0) {
                    // 仅保存预测评分，暂时丢弃真实评分
                    // dataTable.put(i, j, rate);
                    // colMap.put(j, i);
                } else {
                    // 用户i没有给项目j打分，此时需要预测
                    maxRate = Math.sqrt(maxArray[j] * maxArrayUser[i]);
                    minRate = Math.sqrt(minArray[j] * minArrayUser[i]);
                    midRate = Math.sqrt(itemMeanRate[j] * userMeanRate[i]);
                    double predictRate = predictRate(oldMatrix, i, j);
                    if (!Double.isNaN(predictRate) && predictRate != 0) {
                        // trainMatrix.set(i, j, predictRate);
                        dataTable.put(i, j, predictRate);
                        colMap.put(j, i);
                    }
                    // tempVector.add(j, predictRate);
                }
            }

        }
        SparseMatrix trainMatrix = new SparseMatrix(oldMatrix.numRows, oldMatrix.numColumns, dataTable, colMap);
        // 3,调用传入的相似度算法
        int numUsers = trainMatrix.numRows();
        // int numItems = trainMatrix.numColumns();

        similarityMatrix = new SymmMatrix(numUsers);

        for (int i = 0; i < numUsers; i++) {
            SparseVector thisVector = trainMatrix.row(i);
            SparseVector thisVectorOld = oldMatrix.row(i);
            if (thisVector.getCount() == 0) {
                continue;
            }
            // user itself exclusive
            for (int j = i + 1; j < numUsers; j++) {
                SparseVector thatVector = trainMatrix.row(j);
                SparseVector thatVectorOld = oldMatrix.row(j);
                if (thatVector.getCount() == 0) {
                    continue;
                }
                // 调用传统相似度计算方法
                // 先通过反射设置conf
                Class<?> clazz = baseSimilarity.getClass().getSuperclass();
                try {
                    Field f = clazz.getDeclaredField("conf");
                    f.setAccessible(true);
                    f.set(baseSimilarity, dataModel.getContext().getConf());
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                double sim = baseSimilarity.getCorrelation(thisVector, thatVector);// 预测相似度
                double sim1 = baseSimilarity.getCorrelation(thisVectorOld, thatVectorOld);// 真实相似度
                // System.out.println("用户" + i + "对用户" + j + "的真实相似度为：" + sim1 +
                // "," + "预测相似度为：" + sim);
                if (!Double.isNaN(sim) && !Double.isNaN(sim1)) {
                    double simRatio = 1.0 / (1 + Math.exp(-Math.abs(sim1 - sim)));
                    similarityMatrix.set(i, j, simRatio * sim1 + (1 - simRatio) * sim);
                } else if (!Double.isNaN(sim1)) {
                    similarityMatrix.set(i, j, sim1);
                }
            }
        }

    }


    private double predictRate(SparseMatrix oldMatrix, int userId, int itemId) {
        // 计算目标项目itemId的用户优化评分ra
        double ra = 0;
        SparseVector column = oldMatrix.column(itemId);
        for (int i = 0; i < column.getCapacity(); i++) {
            if (column.get(i) != 0.0) {
                ra += (column.get(i) - itemMeanRate[itemId]);
            }
        }
        ra = ra / column.size() + userMeanRate[userId];
        // 计算rt
        double rt = 0;
        SparseVector row = oldMatrix.row(userId);
        for (int i = 0; i < row.getCapacity(); i++) {
            if (row.get(i) != 0.0) {
                rt += (row.get(i) - userMeanRate[userId]);
            }
        }
        rt = rt / row.size() + itemMeanRate[itemId];
        // -----------------------------------------------------------------------------------
        // TODO Auto-generated method stub
        HashMap<Integer, Integer> zheng = map.get(itemId + 1);// +i的频繁项集
        HashMap<Integer, Integer> fu = map.get(-(itemId + 1));// -i的频繁项集
        // 求交集
        if (ItemChanges.size() <= userId)// 由于数据集被随机切为1：4，导致有些用户不在train集中。
            return 0;
        int[] userArray = ItemChanges.get(userId);
        int x = 0;
        int y = 0;
        double a = 0;
        double b = 0;
        int zhengSize = 0, fuSize = 0;
        rt = 0;// 在这里重新计算rt

        if (zheng != null) {
            zhengSize = zheng.size();
            Integer itemIdTimes = zheng.get(itemId + 1);
            if (itemIdTimes != null) {
                for (int i = 0; i < userArray.length; i++) {
                    Integer times = zheng.get(userArray[i]);
                    if (times != null) {
                        rt += (row.get(Math.abs(userArray[i]) - 1) - userMeanRate[userId]);
                        x++;
                        a += times * 1.0 / itemIdTimes;
                    }
                }
            }
        }
        if (fu != null) {
            fuSize = fu.size();
            Integer itemIdTimes = fu.get(-(itemId + 1));
            if (itemIdTimes != null) {
                for (int i = 0; i < userArray.length; i++) {
                    Integer times = fu.get(userArray[i]);
                    if (times != null) {
                        rt += (row.get(Math.abs(userArray[i]) - 1) - userMeanRate[userId]);
                        y++;
                        b += times * 1.0 / itemIdTimes;
                    }
                }
            }
        }
        if (a == 0 && b == 0)
            return 0;
        // 通过上述计算得出rt，进而得出rBase
        rt = rt / (x + y) + itemMeanRate[itemId];
        double rBase = Math.sqrt(ra * rt);
        double tempHigh = a, tempLow = b;
        // ====================使用sigmod函数============================
        double sigmoid = 1.0 / (1 + Math.exp(-(a - b)));
        tempHigh = sigmoid;
        tempLow = 1 - sigmoid;
        return rBase - tempLow * (rBase - minRate) + tempHigh * (maxRate - rBase);
    }


    private double getArrayMax(double[] array) {
        if (array == null || array.length == 0)
            return -1;
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            max = Math.max(max, array[i]);
        }
        return max;
    }

    private double getArrayMin(double[] array) {
        if (array == null || array.length == 0)
            return -1;
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            min = Math.min(min, array[i]);
        }
        return min;
    }

}
