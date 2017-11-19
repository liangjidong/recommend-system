package com.liangjidong.neighborhood;

import java.util.Collection;
import java.util.HashMap;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.google.common.base.Preconditions;
import com.liangjidong.common.UserClustering;

public class OPNNearestNUserNeighborhood implements UserNeighborhood {

	// 用户相似度计算方法
	private final UserSimilarity userSimilarity;
	// 数据集
	private final DataModel dataModel;
	// 测试数据的比例
	private final double samplingRate;
	// 刷新组件助手
	private final RefreshHelper refreshHelper;

	// 邻居用户的个数
	private final int n;
	// 能成为邻居的最小相似度
	private final double minSimilarity;

	private static final long[] EMPTY_NEI = new long[0];

	private static HashMap<Long, long[]> cacheNei = new HashMap<Long, long[]>();

	/**
	 * @param n
	 *            neighborhood size; capped at the number of users in the data
	 *            model
	 * @throws IllegalArgumentException
	 *             if {@code n < 1}, or userSimilarity or dataModel are
	 *             {@code null}
	 */
	public OPNNearestNUserNeighborhood(int n, UserSimilarity userSimilarity, DataModel dataModel)
			throws TasteException {
		this(n, Double.NEGATIVE_INFINITY, userSimilarity, dataModel, 1.0);
	}

	/**
	 * @param n
	 *            neighborhood size; capped at the number of users in the data
	 *            model
	 * @param minSimilarity
	 *            minimal similarity required for neighbors
	 * @throws IllegalArgumentException
	 *             if {@code n < 1}, or userSimilarity or dataModel are
	 *             {@code null}
	 */
	public OPNNearestNUserNeighborhood(int n, double minSimilarity, UserSimilarity userSimilarity, DataModel dataModel)
			throws TasteException {
		this(n, minSimilarity, userSimilarity, dataModel, 1.0);
	}

	/**
	 * @param n
	 *            neighborhood size; capped at the number of users in the data
	 *            model
	 * @param minSimilarity
	 *            minimal similarity required for neighbors
	 * @param samplingRate
	 *            percentage of users to consider when building neighborhood --
	 *            decrease to trade quality for performance
	 * @throws IllegalArgumentException
	 *             if {@code n < 1} or samplingRate is NaN or not in (0,1], or
	 *             userSimilarity or dataModel are {@code null}
	 */
	public OPNNearestNUserNeighborhood(int n, double minSimilarity, UserSimilarity userSimilarity, DataModel dataModel,
			double samplingRate) throws TasteException {
		Preconditions.checkArgument(userSimilarity != null, "userSimilarity is null");
		Preconditions.checkArgument(dataModel != null, "dataModel is null");
		Preconditions.checkArgument(samplingRate > 0.0 && samplingRate <= 1.0, "samplingRate must be in (0,1]");
		this.userSimilarity = userSimilarity;
		this.dataModel = dataModel;
		this.samplingRate = samplingRate;
		this.refreshHelper = new RefreshHelper(null);
		this.refreshHelper.addDependency(this.dataModel);
		this.refreshHelper.addDependency(this.userSimilarity);
		Preconditions.checkArgument(n >= 1, "n must be at least 1");
		int numUsers = dataModel.getNumUsers();
		this.n = n > numUsers ? numUsers : n;
		this.minSimilarity = minSimilarity;
		// 调用一下UserClustering，对用户先分组

	}

	public long[] getUserNeighborhood(long userID) throws TasteException {

		UserSimilarity userSimilarityImpl = getUserSimilarity();

		TopItems.Estimator<Long> estimator = new Estimator(userSimilarityImpl, userID, minSimilarity);

		// 获取三个中心点 cp,co,cn

		// 根据相似度计算方法将用户分为三个组Cp,Co,Cn

		// 对userID，分到{Cp，Cn}，{Co,Cn},{Cn}中的一个,然后封装该集合成候选集，使用LongPrimitiveArrayIterator(long[]
		// userIDs)来封装,上述步骤都属于离线作业,真正线上作业只要通过userID来定位到三个组中的一个，然后取出该组就行了

		// 只需要实现的时候修改这个候选集就可以了-------加入分组思想
		// 默认没有分组思想
		LongPrimitiveIterator userIDs = UserClustering.getCluster(userID);

		if (userIDs == null) {
			return EMPTY_NEI;
		}
		// 进行缓存
		long[] cache = cacheNei.get(userID);
		if (cache == null) {
			cache = TopItems.getTopUsers(n, userIDs, null, estimator);
			cacheNei.put(userID, cache);
		}
		return cache;
	}

	@Override
	public String toString() {
		return "NearestNUserNeighborhood";
	}

	private static final class Estimator implements TopItems.Estimator<Long> {
		private final UserSimilarity userSimilarityImpl;
		private final long theUserID;
		private final double minSim;

		private Estimator(UserSimilarity userSimilarityImpl, long theUserID, double minSim) {
			this.userSimilarityImpl = userSimilarityImpl;
			this.theUserID = theUserID;
			this.minSim = minSim;
		}

		public double estimate(Long userID) throws TasteException {
			if (userID == theUserID) {
				return Double.NaN;
			}
			double sim = userSimilarityImpl.userSimilarity(theUserID, userID);
			return sim >= minSim ? sim : Double.NaN;
		}
	}

	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		// TODO Auto-generated method stub
		refreshHelper.refresh(alreadyRefreshed);

	}

	final UserSimilarity getUserSimilarity() {
		return userSimilarity;
	}

	final DataModel getDataModel() {
		return dataModel;
	}

	final double getSamplingRate() {
		return samplingRate;
	}

}
