package com.liangjidong.mae;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.liangjidong.similarity.CommonSimFunction;

public abstract class AbstractMAETest {
	private static final Logger log = LoggerFactory.getLogger(AbstractMAETest.class);
	private DataModel trainingData;
	private DataModel testData;
	private UserSimilarity similarity;
	private UserNeighborhood neighborhood;
	FastByIDMap<Float> avg;// 存储所有用户的打分均值
	private double maeTotal = 0;
	private int size = 0;

	public UserSimilarity getSimilarity() {
		return similarity;
	}

	public void setSimilarity(UserSimilarity similarity) {
		this.similarity = similarity;
	}

	public UserNeighborhood getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(UserNeighborhood neighborhood) {
		this.neighborhood = neighborhood;
	}

	public DataModel getTrainingData() {
		return trainingData;
	}

	public void setTrainingData(DataModel trainingData) {
		this.trainingData = trainingData;
	}

	public DataModel getTestData() {
		return testData;
	}

	public void setTestData(DataModel testData) {
		this.testData = testData;
	}

	public AbstractMAETest(DataModel trainingData, DataModel testData) {
		super();
		this.trainingData = trainingData;
		this.testData = testData;
		Preconditions.checkArgument(trainingData != null, "trainingData is null");
		Preconditions.checkArgument(testData != null, "testData is null");

	}

	protected abstract UserSimilarity setSimilarityByYourself() throws TasteException;

	protected abstract UserNeighborhood setNeighborhoodByYourself(int neiSize) throws TasteException;

	public double getMAE(int neiSize) {
		try {
			// 计算所有用户的平均打分
			avg = CommonSimFunction.avgAllUser(trainingData);
			// 定义相似度计算方法
			// similarity = new PCCSimiliarity(trainingData);
			setSimilarityByYourself();
			// 用户分组
			// UserClustering.invokeClustering(trainingData, (UPSSimiliarity)
			// similarity);
			// System.out.println(Arrays.toString(UserClustering.Cluster_n));
			// System.out.println(Arrays.toString(UserClustering.Cluster_o));
			// System.out.println(Arrays.toString(UserClustering.Cluster_p));
			// neiSize 表示邻居用户的个数
			// neighborhood = new OPNNearestNUserNeighborhood(neiSize,
			// similarity, trainingData);
			// neighborhood = new NearestNUserNeighborhood(neiSize, similarity,
			// trainingData);
			setNeighborhoodByYourself(neiSize);
			// 测试一下
			// predictRate(3, 318);
			return useThreadPoolCalcaulateMAE();

		} catch (TasteException e) {
			// TODO Auto-generated catch block
			log.error("TasteException 导致无法计算MAE");
			e.printStackTrace();
		}
		return 0;
	}

	// 使用线程池计算MAE
	private double useThreadPoolCalcaulateMAE() throws TasteException {
		// 创建一个线程池
		ExecutorService pool = Executors.newFixedThreadPool(8);
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
			long[] nei = neighborhood.getUserNeighborhood(uid);
			// System.out.println(Arrays.toString(nei));
			float predictRate = CommonSimFunction.predictRate(trainingData, avg, nei, similarity, uid, iid);
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
