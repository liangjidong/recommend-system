package com.liangjidong.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.liangjidong.similarity.UPSSimiliarity;

public class UserClustering {
	private static final float alph = 4.0f;
	private static final float beta = 2.0f;
	// 数据集
	private static DataModel dataModel = null;
	// 相似度计算方法
	private static UPSSimiliarity similarity = null;
	// optimistic center userID
	private static long co = Long.MIN_VALUE;
	// pessimistic center userID
	private static long cp = Long.MIN_VALUE;
	// neutral center userID
	private static long cn = Long.MIN_VALUE + 1;
	// cn--->user
	private static PreferenceArray cnUser = null;
	// cn--->user---->dataModel1;
	private static DataModel model1;
	// neutral cluster
	public static long[] Cluster_n = null;
	// optimistic cluster
	public static long[] Cluster_o = null;
	// pessimistic cluster
	public static long[] Cluster_p = null;

	/**
	 * if user belongs to neutral cluster.the ids_n is Cluster_n
	 */
	public static LongPrimitiveArrayIterator ids_n;
	/**
	 * if user belongs to pessimistic cluster.the ids_p is Cluster_p+Cluster_n
	 */
	public static LongPrimitiveArrayIterator ids_p;
	/**
	 * if user belongs to optimistic cluster.the ids_o is Cluster_o+Cluster_n
	 */
	public static LongPrimitiveArrayIterator ids_o;

	/**
	 * 在调用该方法后才能调用OPNNearestNeighborhood
	 * 
	 * @param dataModel
	 * @param similarity
	 * @throws TasteException
	 */
	public static void invokeClustering(DataModel dataModel, UPSSimiliarity similarity) throws TasteException {
		UserClustering.dataModel = dataModel;
		UserClustering.similarity = similarity;
		getCenter();// 获取三个中心点
		generateCluster();// 分组
	}

	/**
	 * 获取三个中心点
	 * 
	 * @throws TasteException
	 */
	public static void getCenter() throws TasteException {
		// 获取所有用户
		LongPrimitiveIterator userIDs = dataModel.getUserIDs();
		// 上一次遍历到的用户的打分项数量
		int item_num_o = 0, item_num_p = 0;
		int current_item_num = 0;
		FastIDSet set = null;
		long currentUser = 0;

		while (userIDs.hasNext()) {
			currentUser = userIDs.next();
			// 计算该用户打分的平均值
			set = dataModel.getItemIDsFromUser(currentUser);
			float avgPref = avgPreferences(currentUser, set);
			current_item_num = set.size();
			// 遍历获得co
			if (avgPref >= alph && current_item_num > item_num_o) {
				co = currentUser;
				item_num_o = current_item_num;
			}
			// 遍历获取cp
			if (avgPref < beta && current_item_num > item_num_p) {
				cp = currentUser;
				item_num_p = current_item_num;
			}
			/*
			 * System.out.println(avgPref);
			 * System.out.println(current_item_num);
			 */

		}
		System.out.println("积极用户为：" + co);
		System.out.println("消极用户为：" + cp);

		// 构造一个cn(每个打分项的值都是平均值)
		cnUser = getUserCn();

		// 计算所有用户打分的平均值
		double cnSum = 0;
		Iterator<Preference> iterator = cnUser.iterator();
		while (iterator.hasNext()) {
			Preference next = iterator.next();
			cnSum += next.getValue();
			System.out.println(next.getItemID() + ":" + next.getValue());
		}

		System.out.println("全体用户打分的平均值为：" + cnSum / cnUser.length());

		// 将cnUser构造为一个dataModel，用于后续计算cn相似度使用
		FastByIDMap<PreferenceArray> preferences = new FastByIDMap<PreferenceArray>();
		preferences.put(cn, cnUser);

		model1 = new GenericDataModel(preferences);

	}

	/**
	 * 产生分组结果 </br>
	 * 用户u分在Co中，当且仅当sim(u,co)>sim(u,cp) 且 sim(u,co)>sim(u,cn) </br>
	 * 用户u分在Cp中，当且仅当sim(u,cp)>sim(u,co) 且 sim(u,cp)>sim(u,cn) </br>
	 * 用户u分在Cn中，当且仅当sim(u,cn)>sim(u,cp) 且 sim(u,cn)>sim(u,co) </br>
	 * 
	 * @throws TasteException
	 */
	public static void generateCluster() throws TasteException {
		// 实现分组
		/* Cluster_n = new long[dataModel.getNumUsers()]; */
		//
		FastIDSet cluster_n = new FastIDSet();
		FastIDSet cluster_p = new FastIDSet();
		FastIDSet cluster_o = new FastIDSet();
		// 所有用户
		LongPrimitiveIterator userIDs = dataModel.getUserIDs();
		long currentUser = 0;
		while (userIDs.hasNext()) {
			currentUser = userIDs.next();
			if (currentUser == co || currentUser == cp)
				continue;
			double sim_u_co = similarity.userSimilarity(currentUser, co);
			double sim_u_cp = similarity.userSimilarity(currentUser, cp);
			double sim_u_cn = cnWithOthersSimilarity(currentUser);
			if (sim_u_co > sim_u_cp && sim_u_co > sim_u_cn) {
				cluster_o.add(currentUser);
			} else if (sim_u_cp >= sim_u_co && sim_u_cp > sim_u_cn) {
				cluster_p.add(currentUser);
			} else if (sim_u_cn >= sim_u_co && sim_u_cn >= sim_u_cp) {
				cluster_n.add(currentUser);
			}
		}
		cluster_o.add(co);
		cluster_p.add(cp);
		Cluster_n = cluster_n.toArray();
		Cluster_o = cluster_o.toArray();
		Cluster_p = cluster_p.toArray();

		System.out.println("中立组的人数:" + Cluster_n.length);
		System.out.println("积极组的人数:" + Cluster_o.length);
		System.out.println("消极组的人数:" + Cluster_p.length);

		// 根据论文的Definition5
		ids_n = new LongPrimitiveArrayIterator(Cluster_n);
		ids_o = new LongPrimitiveArrayIterator(concat(Cluster_o, Cluster_n));
		ids_p = new LongPrimitiveArrayIterator(concat(Cluster_p, Cluster_n));
	}

	/**
	 * 计算userID属于哪个组
	 * 
	 * @param userID
	 * @return
	 */
	public static LongPrimitiveArrayIterator getCluster(long userID) {
		if (ArrayUtils.contains(UserClustering.Cluster_n, userID)) {
			return new LongPrimitiveArrayIterator(Cluster_n);
			// return UserClustering.ids_n;
		} else if (ArrayUtils.contains(UserClustering.Cluster_p, userID)) {
			// return UserClustering.ids_p;
			return new LongPrimitiveArrayIterator(concat(Cluster_p, Cluster_n));
		} else if (ArrayUtils.contains(UserClustering.Cluster_o, userID)) {
			// return UserClustering.ids_o;
			return new LongPrimitiveArrayIterator(concat(Cluster_o, Cluster_n));
		}
		return null;
	}

	/**
	 * 计算用户打分平均值
	 * 
	 * @param ID:用户
	 * @param set:用户打分的项目
	 * @throws TasteException
	 */
	private static float avgPreferences(long ID, FastIDSet set) throws TasteException {
		float score = (float) 0.0;
		Iterator<Long> iterator = set.iterator();
		while (iterator.hasNext()) {
			long type = (long) iterator.next();
			score += dataModel.getPreferenceValue(ID, type);
		}
		return score / set.size();
	}

	/*
	 * private static float avgPreferences_item(long itemID, FastID set) {
	 * 
	 * }
	 */
	/**
	 * 构造cnUser
	 * 
	 * @return
	 * @throws TasteException
	 */
	private static PreferenceArray getUserCn() throws TasteException {
		LongPrimitiveIterator itemIDs = dataModel.getItemIDs();
		// float[] avg_items = new float[dataModel.getNumItems()];
		PreferenceArray cn = new GenericUserPreferenceArray(dataModel.getNumItems());
		int index = 0;
		while (itemIDs.hasNext()) {
			long itemID = itemIDs.next();
			PreferenceArray pa = dataModel.getPreferencesForItem(itemID);
			double sum = 0;
			for (int i = 0; i < pa.length(); i++) {
				sum += pa.getValue(i);
			}
			// avg_items[index++] = (float) (sum / pa.length());
			cn.setItemID(index, itemID);
			cn.setUserID(index, UserClustering.cn);
			cn.setValue(index, (float) (sum / pa.length()));
			index++;
		}

		return cn;
	}

	/**
	 * 由于cn是主观构造的用户，dataModel没有该用户的ID，计算它与某个用户的相似度的时候，需要单独计算
	 * 
	 * @throws TasteException
	 */
	private static double cnWithOthersSimilarity(long userID) throws TasteException {
		// DataModel dataModel1 = new GenericDataModel(null);

		return similarity.userSimilarityWithDifferentDataModel(model1, cn, dataModel, userID);
	}

	/**
	 * 数组合并
	 */
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * long 型数组合并
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	public static long[] concat(long[] first, long[] second) {
		long[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

}
