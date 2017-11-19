package common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.librec.data.DataModel;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;
import net.librec.math.structure.SymmMatrix;
import net.librec.similarity.AbstractRecommenderSimilarity;
import net.librec.similarity.RecommenderSimilarity;

public class UserClustering {
	private static final float alph = 4.0f;
	private static final float beta = 2.0f;

	private static DataModel dataModel;
	// 基于某个相似度计算方法
	private static AbstractRecommenderSimilarity similarity;
	private static int co, cp, cn;
	// cn--->user
	private static SparseVector cnUser;
	// cn--->user--->dataModel1;
	private static DataModel dataModel1;
	// 现有的用户与虚拟中心用户cn的相似度
	public static Double[] sim_u_cn;
	// 三个分组
	public static List<Integer> cluster_n, cluster_o, cluster_p;
	// 三个分组对应的hash表，通过这个表可以用常数时间得到用户是否属于某个组
	private static boolean[] Con, Cpn, Cn;

	public static void invokeClustering(DataModel dataModel, AbstractRecommenderSimilarity similarity) {
		UserClustering.dataModel = dataModel;
		UserClustering.similarity = similarity;
		getCenter();// 获取三个中心点
		generateCluster();// 用户分组
	}

	/**
	 * 获取三个中心点
	 * 
	 */
	private static void getCenter() {
		// TODO Auto-generated method stub
		// 获取所有用户
		SparseMatrix trainMatrix = dataModel.getDataSplitter().getTrainData();
		int count = trainMatrix.numRows();
		// 上一次遍历到的用户的打分项数量
		int item_num_o = 0, item_num_p = 0;
		int current_item_num = 0;
		for (int i = 0; i < count; i++) {
			SparseVector vector = trainMatrix.row(i);// 获取用户i的打分
			current_item_num = vector.getCount();
			double avgPref = vector.mean();
			// 遍历获得co
			if (avgPref >= alph && current_item_num > item_num_o) {
				co = i;
				item_num_o = current_item_num;
			}
			// 遍历获取cp
			if (avgPref < beta && current_item_num > item_num_p) {
				cp = i;
				item_num_p = current_item_num;
			}
		}
		System.out.println("积极用户为：" + co);
		System.out.println("消极用户为：" + cp);
		// 构造一个cn(每个打分项的值都是平均值)
		cnUser = getCuUser();
		// cnUser;
		// for (int i = 0; i < trainMatrix.numColumns(); i++) {
		// System.out.println(i + ":" + cnUser.get(i));
		// }
	}

	/**
	 * 构造cnUser
	 * 
	 * @return
	 */
	private static SparseVector getCuUser() {
		// TODO Auto-generated method stub
		SparseVector newUser = null;
		SparseMatrix trainMatrix = dataModel.getDataSplitter().getTrainData();
		int itemCount = trainMatrix.numColumns();
		double[] cnPrefs = new double[itemCount];
		int[] indexs = new int[itemCount];
		int index = 0;
		for (int i = 0; i < itemCount; i++) {
			if (trainMatrix.columnSize(i) != 0) {
				cnPrefs[index] = trainMatrix.column(i).mean();
				// if (Double.isNaN(cnPrefs[i]))
				// System.out.println("NAN is" + i + ",column size =" +
				// trainMatrix.columnSize(i));
				// else
				// System.out.println(i + ":" + cnPrefs[i]);
				indexs[index++] = i;
			}

		}
		newUser = new SparseVector(itemCount, indexs, cnPrefs);
		return newUser;
	}

	/**
	 * 产生分组结果 </br>
	 * 用户u分在Co中，当且仅当sim(u,co)>sim(u,cp) 且 sim(u,co)>sim(u,cn) </br>
	 * 用户u分在Cp中，当且仅当sim(u,cp)>sim(u,co) 且 sim(u,cp)>sim(u,cn) </br>
	 * 用户u分在Cn中，当且仅当sim(u,cn)>sim(u,cp) 且 sim(u,cn)>sim(u,co) </br>
	 * 
	 */
	private static void generateCluster() {
		// TODO Auto-generated method stub
		// 实现用户的分组
		cluster_n = new ArrayList<>();
		cluster_p = new ArrayList<>();
		cluster_o = new ArrayList<>();
		SparseMatrix trainMatrix = dataModel.getDataSplitter().getTrainData();
		SymmMatrix similarityMatrix = similarity.getSimilarityMatrix();
		int count = trainMatrix.numRows();
		sim_u_cn = new Double[count];
		// 上一次遍历到的用户的打分项数量
		for (int i = 0; i < count; i++) {
			if (sim_u_cn[i] == null) {
				sim_u_cn[i] = similarity.getCorrelation(trainMatrix.row(i), cnUser);
			}
			if (i == co || i == cp)
				continue;
			double sim_u_co = similarityMatrix.get(i, co);
			double sim_u_cp = similarityMatrix.get(i, cp);
			if (sim_u_co > sim_u_cp && sim_u_co > sim_u_cn[i]) {
				cluster_o.add(i);
			} else if (sim_u_cp >= sim_u_co && sim_u_cp > sim_u_cn[i]) {
				cluster_p.add(i);
			} else if (sim_u_cn[i] >= sim_u_co && sim_u_cn[i] >= sim_u_cp) {
				cluster_n.add(i);
			}
			cluster_o.add(co);
			cluster_p.add(cp);
		}
		// 封装三个整形的hash表
		Con = new boolean[count];
		Cpn = new boolean[count];
		Cn = new boolean[count];
		for (int i = 0; i < cluster_n.size(); i++) {
			Con[cluster_n.get(i)] = true;
			Cpn[cluster_n.get(i)] = true;
			Cn[cluster_n.get(i)] = true;
		}
		for (int i = 0; i < cluster_o.size(); i++) {
			Con[cluster_o.get(i)] = true;
		}
		for (int i = 0; i < cluster_p.size(); i++) {
			Cpn[cluster_p.get(i)] = true;
		}
	}

	/**
	 * 计算userID属于哪个组
	 * 
	 * @param userID
	 * @return
	 */
	public static boolean[] getCluster(int userID) {
		SparseMatrix trainMatrix = dataModel.getDataSplitter().getTrainData();
		int count = trainMatrix.numRows();
		boolean[] hash = new boolean[count];
		SymmMatrix similarityMatrix = similarity.getSimilarityMatrix();
		double sim_u_co = similarityMatrix.get(userID, co);
		double sim_u_cp = similarityMatrix.get(userID, cp);
		if (userID == co || sim_u_co > sim_u_cp && sim_u_co > sim_u_cn[userID]) {
			return Con;
		} else if (userID == cp || sim_u_cp >= sim_u_co && sim_u_cp > sim_u_cn[userID]) {
			return Cpn;
		} else if (sim_u_cn[userID] >= sim_u_co && sim_u_cn[userID] >= sim_u_cp) {
			return Cn;
		}

		return hash;
	}

}
