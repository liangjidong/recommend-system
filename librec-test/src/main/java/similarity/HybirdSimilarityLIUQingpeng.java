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

public class HybirdSimilarityLIUQingpeng extends AbstractRecommenderSimilarity {
	private DataModel dataModel;
	private Double[] userMeanRate;// 用户打分均值
	private Double[] itemMeanRate;// 项目打分均值
	private AbstractRecommenderSimilarity baseSimilarity;

	/**
	 * 将传统的相似度算法和模式挖掘的用户分组结果传入
	 * 
	 * @param baseSimilarity
	 * @param users
	 */
	public HybirdSimilarityLIUQingpeng(AbstractRecommenderSimilarity baseSimilarity) {
		super();
		this.baseSimilarity = baseSimilarity;
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
		// 2，根据map重新构造评分矩阵（包含预测评分）----核心部分
		SparseMatrix oldMatrix = dataModel.getDataSplitter().getTrainData();
		// Table {row-id, col-id, rate}
		Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
		// Map {col-id, multiple row-id}: used to fast build a rating matrix
		Multimap<Integer, Integer> colMap = HashMultimap.create();
		// 初始化用户打分均值，项目打分均值
		userMeanRate = new Double[oldMatrix.numRows];
		itemMeanRate = new Double[oldMatrix.numColumns];
		for (int j = 0; j < oldMatrix.numColumns; j++) {
			SparseVector column = oldMatrix.column(j);
			itemMeanRate[j] = column.mean();
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
					dataTable.put(i, j, rate);
					colMap.put(j, i);
				} else {
					// 用户i没有给项目j打分，此时需要预测
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
				similarityMatrix.set(i, j, sim);

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
		return rBase;

	}

}
