package com.liangjidong.similarity;

import java.util.Collection;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.google.common.base.Preconditions;

public class UPSSimiliarity implements UserSimilarity {

	private DataModel dataModel;
	private RefreshHelper refreshHelper;

	public UPSSimiliarity(DataModel dataModel) {
		// TODO Auto-generated constructor stub
		Preconditions.checkArgument(dataModel != null, "dataModel is null");
		Preconditions.checkArgument(dataModel.hasPreferenceValues(), "DataModel doesn't have preference values");
		this.dataModel = dataModel;
		this.refreshHelper = new RefreshHelper(null);
		refreshHelper.addDependency(this.dataModel);
	}

	protected DataModel getDataModel() {
		return dataModel;
	}

	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		// TODO Auto-generated method stub
		alreadyRefreshed = RefreshHelper.buildRefreshed(alreadyRefreshed);
		RefreshHelper.maybeRefresh(alreadyRefreshed, getDataModel());
	}

	/**
	 * 实现该方法即实现了相似度计算方法
	 */
	public double userSimilarity(long userID1, long userID2) throws TasteException {
		if (userID1 == userID2)
			return 1;
		// TODO Auto-generated method stub
		DataModel dataModel = getDataModel();
		// 获取用户打分项的id集合
		FastIDSet prefs1 = dataModel.getItemIDsFromUser(userID1);
		FastIDSet prefs2 = dataModel.getItemIDsFromUser(userID2);

		long prefs1Size = prefs1.size();
		long prefs2Size = prefs2.size();

		/*
		 * long intersectionSize = prefs1Size < prefs2Size ?
		 * prefs2.intersectionSize(prefs1) : prefs1.intersectionSize(prefs2);
		 */
		// 计算交集的大小和产生新的FastIDSet作为交集
		FastIDSet pre_a, pre_b;// a为大的集合
		FastIDSet pre_com = new FastIDSet();
		if (prefs1Size < prefs2Size) {
			pre_a = prefs2;
			pre_b = prefs1;
		} else {
			pre_a = prefs1;
			pre_b = prefs2;
		}
		int intersectionSize = 0;
		Iterator<Long> iterator = pre_b.iterator();
		while (iterator.hasNext()) {
			long type = (long) iterator.next();
			if (pre_a.contains(type)) {

				pre_com.add(type);
			}
		}
		intersectionSize = pre_com.size();
		// 如果交集为0，则相似度为0
		if (intersectionSize == 0) {
			return 0;
		}
		// 计算并集的大小
		long unionSize = unionSize(pre_a, pre_b);

		// 计算userID1的平均打分
		float avg_1 = avgPreferences(userID1, prefs1);
		// 计算userID2的平均打分
		float avg_2 = avgPreferences(userID2, prefs2);

		// 计算共同打分项的打分差的和
		double sum = 0.0;
		iterator = pre_com.iterator();
		while (iterator.hasNext()) {
			long itemID = iterator.next();
			sum += Math
					.abs(dataModel.getPreferenceValue(userID1, itemID) - dataModel.getPreferenceValue(userID2, itemID));
		}
		return Math.exp(-((sum * 1.0) / intersectionSize) * Math.abs(avg_1 - avg_2))
				* ((intersectionSize * 1.0) / unionSize);
	}

	/**
	 * 计算不同dataModel（包含相同item）的用户之间的相似度
	 */
	public double userSimilarityWithDifferentDataModel(DataModel dataModel1, long userID1, DataModel dataModel2,
			long userID2) throws TasteException {
		// TODO Auto-generated method stub
		// 获取用户打分项的id集合
		FastIDSet prefs1 = dataModel1.getItemIDsFromUser(userID1);
		FastIDSet prefs2 = dataModel2.getItemIDsFromUser(userID2);

		long prefs1Size = prefs1.size();
		long prefs2Size = prefs2.size();

		// 计算交集的大小和产生新的FastIDSet作为交集
		FastIDSet pre_a, pre_b;// a为大的集合
		FastIDSet pre_com = new FastIDSet();
		if (prefs1Size < prefs2Size) {
			pre_a = prefs2;
			pre_b = prefs1;
		} else {
			pre_a = prefs1;
			pre_b = prefs2;
		}
		int intersectionSize = 0;
		Iterator<Long> iterator = pre_b.iterator();
		while (iterator.hasNext()) {
			long type = (long) iterator.next();
			if (pre_a.contains(type)) {

				pre_com.add(type);
			}
		}
		intersectionSize = pre_com.size();
		// 如果交集为0，则相似度为0
		if (intersectionSize == 0) {
			return 0;
		}
		// 计算并集的大小
		long unionSize = unionSize(pre_a, pre_b);

		// 计算userID1的平均打分
		float avg_1 = avgPreferences(dataModel1, userID1, prefs1);
		// 计算userID2的平均打分
		float avg_2 = avgPreferences(dataModel2, userID2, prefs2);

		// 计算共同打分项的打分差的和
		double sum = 0.0;
		iterator = pre_com.iterator();
		while (iterator.hasNext()) {
			long itemID = iterator.next();
			sum += Math.abs(
					dataModel1.getPreferenceValue(userID1, itemID) - dataModel2.getPreferenceValue(userID2, itemID));
		}
		return Math.exp(-((sum * 1.0) / intersectionSize) * Math.abs(avg_1 - avg_2))
				* ((intersectionSize * 1.0) / unionSize);
	}

	private float avgPreferences(DataModel dataModel1, long userID1, FastIDSet prefs1) throws TasteException {
		// TODO Auto-generated method stub
		float score = (float) 0.0;
		Iterator<Long> iterator = prefs1.iterator();
		while (iterator.hasNext()) {
			long type = (long) iterator.next();
			score += dataModel1.getPreferenceValue(userID1, type);
		}
		return score / prefs1.size();
	}

	public void setPreferenceInferrer(PreferenceInferrer inferrer) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * FastIDSet只实现了intersectionSize（求交集）， 现实现求并
	 */
	private int unionSize(FastIDSet a, FastIDSet b) {
		int count = a.size();
		Iterator<Long> iterator = b.iterator();
		while (iterator.hasNext()) {
			long type = (long) iterator.next();
			if (!a.contains(type)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 计算用户的打分平均值
	 * 
	 * @throws TasteException
	 */
	private float avgPreferences(long userID, FastIDSet set) throws TasteException {
		float score = (float) 0.0;
		Iterator<Long> iterator = set.iterator();
		while (iterator.hasNext()) {
			long type = (long) iterator.next();
			score += dataModel.getPreferenceValue(userID, type);
		}
		return score / set.size();
	}

}
