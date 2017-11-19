package com.liangjidong.similarity;

import java.util.Iterator;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class CommonSimFunction {
	/**
	 * 计算用户a和b的共同打分项数量 <br/>
	 * FastIDSet只实现了intersectionSize（求交集）， 现实现求并
	 * 
	 * 
	 * @return
	 * @throws TasteException
	 */
	public static int unionSize(DataModel dataModel1, DataModel dataModel2, long userID1, long userID2)
			throws TasteException {
		FastIDSet a = dataModel1.getItemIDsFromUser(userID1);
		FastIDSet b = dataModel1.getItemIDsFromUser(userID2);
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
	 * 计算两个用户的共同打分项集合
	 * 
	 * @throws TasteException
	 */
	public static FastIDSet intersectionFastIDSet(DataModel dataModel1, DataModel dataModel2, long userID1,
			long userID2) throws TasteException {
		FastIDSet a = dataModel1.getItemIDsFromUser(userID1);
		FastIDSet b = dataModel1.getItemIDsFromUser(userID2);
		FastIDSet out = new FastIDSet();
		int count = a.size();
		Iterator<Long> iterator = b.iterator();
		while (iterator.hasNext()) {
			long type = (long) iterator.next();
			if (a.contains(type)) {
				out.add(type);
			}
		}
		return out;
	}

	/**
	 * 计算用户的打分平均值
	 * 
	 * @throws TasteException
	 */
	public static float avgPreferences(long userID, DataModel dataModel) throws TasteException {
		FastIDSet set = dataModel.getItemIDsFromUser(userID);
		float score = 0;
		Iterator<Long> iterator = set.iterator();
		while (iterator.hasNext()) {
			long type = (long) iterator.next();
			score += dataModel.getPreferenceValue(userID, type);
		}
		return score / set.size();
	}

	/**
	 * 预测评分
	 * 
	 * @param uid
	 * @param iid
	 * @return
	 * @throws TasteException
	 */
	public static float predictRate(DataModel trainingData, FastByIDMap<Float> avg, long[] nei,
			UserSimilarity similarity, long uid, long iid) throws TasteException {
		// 获取邻居节点
		// long[] nei = neighborhood.getUserNeighborhood(uid);
		// log.info(Arrays.toString(nei));
		// 根据邻居节点计算预测评分
		float pti = avg.get(uid);
		double up = 0;
		double down = 0;
		int notRate = 0;
		double sim;
		for (long l : nei) {
			sim = similarity.userSimilarity(uid, l);
			if (trainingData.getPreferenceValue(l, iid) == null) {
				notRate++;
				continue;
			}
			up += sim * (trainingData.getPreferenceValue(l, iid) - avg.get(l));
			down += Math.abs(sim);
		}
		if (notRate == nei.length) {
			// log.info("所有邻居用户都未对该case打分,case 为：" + uid + ":" + iid);
			return pti;
		} else {
			pti += up / down;
		}
		System.out.println(uid + "的预测评分为：" + pti);
		return pti;
	}

	/**
	 * 获取每个用户的平均打分
	 * 
	 * @throws TasteException
	 */
	public static FastByIDMap<Float> avgAllUser(DataModel trainingData) throws TasteException {
		FastByIDMap<Float> avg = new FastByIDMap<Float>();
		LongPrimitiveIterator userIDs = trainingData.getUserIDs();
		long id;
		int index = 0;
		while (userIDs.hasNext()) {
			id = userIDs.nextLong();
			avg.put(id, avgPreferences(id, trainingData));
		}
		return avg;
	}
}
