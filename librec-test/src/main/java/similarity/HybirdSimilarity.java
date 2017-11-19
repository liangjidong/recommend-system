package similarity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import net.librec.data.DataModel;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;
import net.librec.math.structure.SymmMatrix;
import net.librec.similarity.AbstractRecommenderSimilarity;
import patternMining.ItemAssociateRuleMining;

public class HybirdSimilarity extends AbstractRecommenderSimilarity {
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
	private double simRatio = 0.35;
	private Double[] userMeanRate;// 用户打分均值
	private Double[] itemMeanRate;// 项目打分均值
	private AbstractRecommenderSimilarity baseSimilarity;
	private Map<Integer, HashMap<Integer, Integer>> map;// 模式挖掘结果

	private List<int[]> ItemChanges;

	/**
	 * 将传统的相似度算法和模式挖掘的用户分组结果传入
	 * 
	 * @param baseSimilarity
	 * @param users
	 */
	public HybirdSimilarity(AbstractRecommenderSimilarity baseSimilarity) {
		super();
		this.baseSimilarity = baseSimilarity;
	}

	public HybirdSimilarity(double maxRate, double minRate, AbstractRecommenderSimilarity baseSimilarity) {
		super();
		this.maxRate = maxRate;
		this.minRate = minRate;
		this.baseSimilarity = baseSimilarity;
	}

	@Override
	protected double getSimilarity(List<? extends Number> thisList, List<? extends Number> thatList) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 计算项目的 DU(degree of uncertain)，然后计算项目之间的影响因子gx
	 */
	private void calculateDU(SparseMatrix matrix) {
		DU = new double[matrix.numColumns];
		int length = ((int) (MAX_RATE - MIN_RATE)) + 1;
		double[] p = new double[length];
		int[] count = new int[length];
		for (int i = 0; i < matrix.numColumns; i++) {
			SparseVector column = matrix.column(i);
			Arrays.fill(count, 0);
			Arrays.fill(p, 0);
			for (int j = 0; j < matrix.numRows; j++) {
				if (column.get(j) == 0)
					continue;
				count[(int) (column.get(j) - MIN_RATE)]++;
			}
			for (int j = 0; j < count.length; j++) {
				p[j] = count[j] * 1.0 / column.getCount();
				// System.out.println(p[j]);
			}
			double sum = 0.0;
			double log2 = Math.log(2.0);
			for (int j = 0; j < p.length; j++) {
				if (p[j] == 0.0)
					continue;
				sum += p[j] * (Math.log(p[j]) / log2);
			}
			DU[i] = -sum / column.getCount();
			// System.out.println("sum=" + sum + " " + "DU[" + i + "]=" +
			// DU[i]);
		}
		double max = 0;
		gx = new double[matrix.numColumns][matrix.numColumns];
		for (int i = 0; i < matrix.numColumns; i++) {
			for (int j = i + 1; j < matrix.numColumns; j++) {
				// gx[i][j] = 1.0 / (1.0 - Math.exp(-Math.abs(DU[i] - DU[j])));
				gx[i][j] = Math.exp(-Math.abs(DU[i] - DU[j]));
				// i和j之间的差异性
				// gx[i][j] = Math.abs(DU[i] - DU[j]);
				// if (gx[i][j] > max)
				// max = gx[i][j];
			}
		}
		// 规范化到0-1
		// for (int i = 0; i < matrix.numColumns; i++) {
		// for (int j = i + 1; j < matrix.numColumns; j++) {
		// gx[i][j] = gx[i][j] / max;
		// }
		// }
	}

	/**
	 * 已有实现通过调用{@code getCorrelation()获得两个用户间的相似度} <br/>
	 * 现在需要通过挖掘的来计算间接相似度
	 */
	@Override
	public void buildSimilarityMatrix(DataModel dataModel) {
		this.dataModel = dataModel;
		// 1,获取map,itemChanges
		map = new ItemAssociateRuleMining()
				.getItemSet("C:\\Users\\ljd\\Desktop\\论文相关--开题报告--梁继东\\testout\\userArrayFianlly.txt");
		ItemChanges = getListOfInterestingItems(dataModel);
		// 2，根据map重新构造评分矩阵（包含预测评分）----核心部分
		SparseMatrix oldMatrix = dataModel.getDataSplitter().getTrainData();
		// 计算DU
		calculateDU(oldMatrix);
		// Table {row-id, col-id, rate}
		Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
		// Map {col-id, multiple row-id}: used to fast build a rating matrix
		Multimap<Integer, Integer> colMap = HashMultimap.create();
		Double[] meanArray = new Double[oldMatrix.numColumns];
		Double[] maxArray = new Double[oldMatrix.numColumns];
		Double[] minArray = new Double[oldMatrix.numColumns];
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
					maxRate = maxArray[j];
					minRate = minArray[j];
					double predictRate = predictRate(oldMatrix, i, j);
					if (!Double.isNaN(predictRate) && predictRate != 0) {
						// trainMatrix.set(i, j, predictRate);
						dataTable.put(i, j, predictRate);
						colMap.put(j, i);
						System.out.println("用户" + i + "对物品" + j + "的预测打分为：" + predictRate);
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
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				double sim = baseSimilarity.getCorrelation(thisVector, thatVector);
				double sim1 = baseSimilarity.getCorrelation(thisVectorOld, thatVectorOld);
				System.out.println("用户" + i + "对用户" + j + "的真实相似度为：" + sim1 + "," + "预测相似度为：" + sim);
				if (!Double.isNaN(sim) && !Double.isNaN(sim1)) {
					similarityMatrix.set(i, j, simRatio * sim + (1 - simRatio) * sim1);
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
		// 计算用户userId对项目itemId的基准评分
		double rBase = (ra + rt) / 2;
		// -----------------------------------------------------------------------------------
		// TODO Auto-generated method stub
		HashMap<Integer, Integer> zheng = map.get(itemId + 1);// +i的频繁项集
		HashMap<Integer, Integer> fu = map.get(-(itemId + 1));// -i的频繁项集
		// 求交集
		int[] userArray = ItemChanges.get(userId);
		int x = 0;
		int y = 0;
		double a = 0;
		double b = 0;
		int zhengSize = 0, fuSize = 0;
		if (zheng != null) {
			zhengSize = zheng.size();
			Integer itemIdTimes = zheng.get(itemId);
			if (itemIdTimes != null) {
				for (int i = 0; i < userArray.length; i++) {
					Integer times = zheng.get(userArray[i]);
					if (times != null) {
						x++;
						a += times * 1.0 / itemIdTimes
						/**
						 * gx[Math.abs(userArray[i])][itemId]
						 **/
						;
					}
				}
				a = a * x / zhengSize;
			}
		}
		if (fu != null) {
			fuSize = fu.size();
			Integer itemIdTimes = fu.get(itemId);
			if (itemIdTimes != null) {
				for (int i = 0; i < userArray.length; i++) {
					Integer times = fu.get(userArray[i]);
					if (times != null) {
						y++;
						b += times * 1.0 / itemIdTimes /**
														 * gx[Math.abs(userArray[i])][itemId]
														 **/
						;
					}
				}
				b = b * y / fuSize;
			}
		}
		// for (int i = 0; i < userArray.length; i++) {
		// if (zheng != null && zheng.contains(userArray[i])) {
		// x++;
		// if (gx[itemId][Math.abs(userArray[i])] != 0.0)
		// a += gx[itemId][Math.abs(userArray[i])];
		// else
		// a += gx[Math.abs(userArray[i])][itemId];
		// }
		// if (fu != null && fu.contains(userArray[i])) {
		// y++;
		// if (gx[itemId][Math.abs(userArray[i])] != 0.0)
		// b += gx[itemId][Math.abs(userArray[i])];
		// else
		// b += gx[Math.abs(userArray[i])][itemId];
		// }
		// }
		if (a == 0 && b == 0)
			return 0;
		// System.out.println(a + ":" + b);
		double tempHigh = 0, tempLow = 0;
		if (x != 0) {
			tempHigh = a / (zhengSize + fuSize);
			// tempA = a / zheng.size();
			// a = a / (x * 1.0 * zheng.size());// +1的贡献因子
		}
		if (y != 0) {
			tempLow = b / (zhengSize + fuSize);
			// tempB = b / fu.size();
			// b = b / (y * 1.0 * fu.size());// -1的贡献因子
		}
		// System.out.println(a + ":" + b);
		// if (tempA != 0 || tempB != 0) {
		// a = tempA / (tempA + tempB);
		// b = tempB / (tempA + tempB);
		// }
		// System.out.println(tempA + ":" + tempB);
		// if (a < minRatio && b < minRatio) {
		// return midRate;
		// }
		// if (a < minRatio && b >= maxRatio) {
		// return midRate - b * (midRate - minRate);
		// }
		// if (b < minRatio && a >= maxRatio) {
		// return midRate + a * (maxRate - midRate);
		// }
		// return midRate - b * (midRate - minRate) + a * (maxRate -midRate);
		// --------------
		// if (tempA < minRatio && tempB < minRatio) {
		// return 0;
		// }
		// if (tempA < minRatio && tempB >= maxRatio) {
		// return midRate - tempB * (midRate - minRate);
		// }
		// if (tempB < minRatio && tempA >= maxRatio) {
		// return midRate + tempA * (maxRate - midRate);
		// }
		return rBase - tempLow * (rBase - minRate) + tempHigh * (maxRate - rBase);

	}

	// ，评分预处理
	public List<int[]> getListOfInterestingItems(DataModel dataModel) {
		List<int[]> ans = new ArrayList<>();
		SparseMatrix trainMatrix = dataModel.getDataSplitter().getTrainData();
		// 获取所有用户
		// trainData.
		int count = trainMatrix.numRows();
		int[] temp;
		for (int i = 0; i < count; i++) {
			SparseVector interest = trainMatrix.row(i);
			if (interest.getCount() == 0)
				continue;
			temp = new int[interest.getCount()];
			int index = 0;
			for (Integer idx : interest.getIndex()) {
				if (interest.get(idx) >= 3) {
					temp[index++] = (idx + 1);
				} else {
					temp[index++] = -(idx + 1);
				}
			}
			Arrays.sort(temp);
			ans.add(temp);
		}
		return ans;
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
