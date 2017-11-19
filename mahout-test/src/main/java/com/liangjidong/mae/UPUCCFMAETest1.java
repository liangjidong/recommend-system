package com.liangjidong.mae;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.eval.AbstractDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.liangjidong.common.UserClustering;
import com.liangjidong.neighborhood.OPNNearestNUserNeighborhood;
import com.liangjidong.similarity.UPSSimiliarity;

public class UPUCCFMAETest1 {
	private static final Logger log = LoggerFactory.getLogger(UPUCCFMAETest1.class);
	private DataModel trainingData = null;
	private DataModel testData = null;
	private UserSimilarity similarity = null;
	private UserNeighborhood neighborhood;
	FastByIDMap<Float> avg = new FastByIDMap<Float>();// 存储所有用户的打分均值
	private double maeTotal = 0;
	private int size = 0;

	public static void main(String[] args) throws IOException, TasteException {
		String projectDir = System.getProperty("user.dir");
		DataModel trainingData = new FileDataModel(new File(projectDir + "/src/main/ua.base"));
		DataModel testData = new FileDataModel(new File(projectDir + "/src/main/ua.test"));
		UPUCCFMAETest1 test = new UPUCCFMAETest1(trainingData, testData);

		System.out.println(test.getMAE(10));
		/*
		 * System.out.println(test.getMAE(30));
		 * System.out.println(test.getMAE(40));
		 * System.out.println(test.getMAE(50));
		 * System.out.println(test.getMAE(60));
		 * System.out.println(test.getMAE(70));
		 * System.out.println(test.getMAE(80));
		 * System.out.println(test.getMAE(90));
		 */

		// 分组为10时 MAE=0.7849556792604714
		// 分组为20时 MAE=0.7627988197167012
		// 分组为30时 MAE=0.752540139177435
		// 分组为40时 MAE=0.7482394121322119
		// 分组为50时 MAE=0.7455329340629478
		// 分组为60时 MAE=0.7445626653469167
		// 分组为70时 MAE=0.7432770060236628
		// 分组为80时 MAE=0.7430517431820285
		// 分组为90时 MAE=0.7422810925891803
		// 分组为100时 MAE=0.7419142251619912
	}

	public UPUCCFMAETest1(DataModel trainingData, DataModel testData) {
		super();
		this.trainingData = trainingData;
		this.testData = testData;
		Preconditions.checkArgument(trainingData != null, "trainingData is null");
		Preconditions.checkArgument(testData != null, "testData is null");

	}

	public double getMAE(int neiSize) {
		try {
			// 计算所有用户的平均打分
			avgPreferences();
			// 定义相似度计算方法
			similarity = new UPSSimiliarity(trainingData);
			// 用户分组
			UserClustering.invokeClustering(trainingData, (UPSSimiliarity) similarity);
			System.out.println(Arrays.toString(UserClustering.Cluster_n));
			System.out.println(Arrays.toString(UserClustering.Cluster_o));
			System.out.println(Arrays.toString(UserClustering.Cluster_p));
			// neiSize 表示邻居用户的个数
			neighborhood = new OPNNearestNUserNeighborhood(neiSize, similarity, trainingData);
			// 测试一下
			// predictRate(3, 318);
			return userThreadPoolCalcaulateMAE();

		} catch (TasteException e) {
			// TODO Auto-generated catch block
			log.error("TasteException 导致无法计算MAE");
			e.printStackTrace();
		}
		return 0;
	}

	// 使用线程池计算MAE
	private double userThreadPoolCalcaulateMAE() throws TasteException {
		// 创建一个线程池
		ExecutorService pool = Executors.newFixedThreadPool(20);
		// 从testData中取出测试用例，预测评分，获得MAE
		// int size = 0;
		// double maeTotal = 0;
		LongPrimitiveIterator users = testData.getUserIDs();
		// 对每个用户，获取所打分的item
		long userId;
		long itemId;
		while (users.hasNext()) {
			userId = users.nextLong();
			FastIDSet itemIDsFromUser = testData.getItemIDsFromUser(userId);
			// 对每个item进行预测评分
			LongPrimitiveIterator iterator = itemIDsFromUser.iterator();
			while (iterator.hasNext()) {
				itemId = iterator.nextLong();

				// float pti = predictRate(userId, itemId);
				// // log.info(userId + ":" + itemId + "的预测打分为" + pti);
				// if (pti == 0) {
				// continue;
				// }
				// // log.info(userId + ":" + itemId + "的预测打分为" + pti); size++;
				// maeTotal += Math.abs(pti -
				// testData.getPreferenceValue(userId, itemId));
				PredictCallable pc = new PredictCallable(userId, itemId);
				pool.submit(pc);
			}
		}
		pool.shutdown();
		try {// 等待直到所有任务完成
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(size);
		if (size != 0)
			return maeTotal / size;
		return 0;
	}

	/**
	 * 计算所有用户的平均打分
	 * 
	 * @throws TasteException
	 */
	private void avgPreferences() throws TasteException {
		LongPrimitiveIterator userIDs = trainingData.getUserIDs();
		long id;
		int index = 0;
		while (userIDs.hasNext()) {
			id = userIDs.nextLong();
			avg.put(id, avgPreferences(id, trainingData.getItemIDsFromUser(id)));
		}
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
			score += trainingData.getPreferenceValue(userID, type);
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
	private float predictRate(long uid, long iid) throws TasteException {
		// 获取邻居节点
		long[] nei = neighborhood.getUserNeighborhood(uid);
		// log.info(Arrays.toString(nei));
		// 根据邻居节点计算预测评分
		float pti = avg.get(uid);
		double up = 0;
		double down = 0;
		int notRate = 0;
		for (long l : nei) {
			double sim = similarity.userSimilarity(uid, l);
			if (trainingData.getPreferenceValue(l, iid) == null) {
				notRate++;
				continue;
			}
			up += sim * (trainingData.getPreferenceValue(l, iid) - avg.get(l));
			down += Math.abs(sim);
		}
		if (notRate == nei.length) {
			// log.info("所有邻居用户都未对该case打分,case 为：" + uid + ":" + iid);
			return 0;
		} else {
			pti += up / down;
		}
		return pti;
	}

	/**
	 * 执行预测并返回结果
	 * 
	 * @author ljd
	 *
	 */
	class PredictCallable implements Callable<Float> {
		private long uid, iid;

		public PredictCallable(long uid, long iid) {
			super();
			this.uid = uid;
			this.iid = iid;
		}

		public Float call() throws Exception {
			// TODO Auto-generated method stub
			float predictRate = predictRate(uid, iid);
			// 将结果合计
			addPredict(predictRate, uid, iid);
			return predictRate;
		}

	}

	/**
	 * 同步计算结果
	 * 
	 * @param pre
	 * @throws TasteException
	 */
	private synchronized void addPredict(float pti, long userId, long itemId) throws TasteException {
		if (pti != 0) {
			this.maeTotal += Math.abs(pti - testData.getPreferenceValue(userId, itemId));
			size++;
		}
	}
}
