package similarity;

import net.librec.math.structure.SparseVector;
import net.librec.similarity.AbstractRecommenderSimilarity;
import net.librec.similarity.RecommenderSimilarity;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用传统相似度计算直接相似度，然后通过模式挖掘的结果计算间接相似度
 * 
 * @author ljd
 *
 */
public class UPSSimilarity extends AbstractRecommenderSimilarity {
	private RecommenderSimilarity baseSimilarity;
	private List<List<Integer>> users;// 模式挖掘结果

	/**
	 * 基于传统的相似度方法
	 * 
	 * @param baseSimilarity
	 */
	public UPSSimilarity(RecommenderSimilarity baseSimilarity) {
		super();
		this.baseSimilarity = baseSimilarity;
	}

	public UPSSimilarity() {
		super();
		// TODO Auto-generated constructor stub
	}

	// 计算的不是相似度而是 a,b的打分差值绝对值的和 {icIab|rai-rbi|
	@Override
	protected double getSimilarity(List<? extends Number> thisList, List<? extends Number> thatList) {
		double sum = 0;
		for (int i = 0; i < thisList.size(); i++) {
			sum += Math.abs(thisList.get(i).doubleValue() - thatList.get(i).doubleValue());
		}
		return sum;
		// // TODO Auto-generated method stub
		// if (userID1 == userID2)
		// return 1;
		// // TODO Auto-generated method stub
		// DataModel dataModel = getDataModel();
		// // 获取用户打分项的id集合
		// FastIDSet prefs1 = dataModel.getItemIDsFromUser(userID1);
		// FastIDSet prefs2 = dataModel.getItemIDsFromUser(userID2);
		//
		// long prefs1Size = prefs1.size();
		// long prefs2Size = prefs2.size();
		//
		// /*
		// * long intersectionSize = prefs1Size < prefs2Size ?
		// * prefs2.intersectionSize(prefs1) : prefs1.intersectionSize(prefs2);
		// */
		// // 计算交集的大小和产生新的FastIDSet作为交集
		// FastIDSet pre_a, pre_b;// a为大的集合
		// FastIDSet pre_com = new FastIDSet();
		// if (prefs1Size < prefs2Size) {
		// pre_a = prefs2;
		// pre_b = prefs1;
		// } else {
		// pre_a = prefs1;
		// pre_b = prefs2;
		// }
		// int intersectionSize = 0;
		// Iterator<Long> iterator = pre_b.iterator();
		// while (iterator.hasNext()) {
		// long type = (long) iterator.next();
		// if (pre_a.contains(type)) {
		//
		// pre_com.add(type);
		// }
		// }
		// intersectionSize = pre_com.size();
		// // 如果交集为0，则相似度为0
		// if (intersectionSize == 0) {
		// return 0;
		// }
		// // 计算并集的大小
		// long unionSize = unionSize(pre_a, pre_b);
		//
		// // 计算userID1的平均打分
		// float avg_1 = avgPreferences(userID1, prefs1);
		// // 计算userID2的平均打分
		// float avg_2 = avgPreferences(userID2, prefs2);
		//
		// // 计算共同打分项的打分差的和
		// double sum = 0.0;
		// iterator = pre_com.iterator();
		// while (iterator.hasNext()) {
		// long itemID = iterator.next();
		// sum += Math
		// .abs(dataModel.getPreferenceValue(userID1, itemID) -
		// dataModel.getPreferenceValue(userID2, itemID));
		// }
		// return Math.exp(-((sum * 1.0) / intersectionSize) * Math.abs(avg_1 -
		// avg_2))
		// * ((intersectionSize * 1.0) / unionSize);
	}

	@Override
	public double getCorrelation(SparseVector thisVector, SparseVector thatVector) {
		List<Double> thisList = new ArrayList<Double>();
		List<Double> thatList = new ArrayList<Double>();
		int unionSize = thisVector.size();

		for (Integer idx : thatVector.getIndex()) {
			if (thisVector.contains(idx)) {
				thisList.add(thisVector.get(idx));
				thatList.add(thatVector.get(idx));
			} else {
				unionSize++;
			}
		}
		double sim = getSimilarity(thisList, thatList);

		double thisMean = thisVector.mean();
		double thatMean = thatVector.mean();
		// int sizeOfCommon = thisList.size();
		return Math.exp((-sim / thisList.size()) * Math.abs(thisMean - thatMean)) * (thisList.size() * 1.0 / unionSize);
	}

	public static void main(String[] args) {
		SparseVector thatVector;
		SparseVector thisVector;
		int[] idx1 = { 1, 2, 4, 5, 7 };
		int[] idx5 = { 1, 5, 9 };
		double[] pre1 = { 1, 2, 3, 2, 2 };
		double[] pre5 = { 1, 2, 2 };
		thisVector = new SparseVector(9, idx1, pre1);
		thatVector = new SparseVector(9, idx5, pre5);
		System.out.println(new UPSSimilarity().getCorrelation(thisVector, thatVector));
	}

}
