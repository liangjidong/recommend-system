package com.liangjidong.similarity;

import java.util.Collection;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.google.common.base.Preconditions;

public class PCCSimiliarity implements UserSimilarity {

	private DataModel dataModel;// 主数据集
	private RefreshHelper refreshHelper;

	public PCCSimiliarity(DataModel dataModel) {
		super();
		Preconditions.checkArgument(dataModel != null, "dataModel is null");
		this.dataModel = dataModel;
		this.refreshHelper = new RefreshHelper(null);
		refreshHelper.addDependency(this.dataModel);
	}

	public DataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		// TODO Auto-generated method stub
		refreshHelper.refresh(alreadyRefreshed);
	}

	/**
	 * 相同的dataModel下计算用户相似度
	 */
	public double userSimilarity(long userID1, long userID2) throws TasteException {
		return userSimilarityWithDiffModel(dataModel, dataModel, userID1, userID2);
	}

	/**
	 * 不同dataModel下计算用户相似度
	 * 
	 * @param userID1
	 * @param userID2
	 * @return
	 * @throws TasteException
	 */
	public double userSimilarityWithDiffModel(DataModel dataModel1, DataModel dataModel2, long userID1, long userID2)
			throws TasteException {
		// TODO Auto-generated method stub
		PreferenceArray xPrefs = dataModel1.getPreferencesFromUser(userID1);
		PreferenceArray yPrefs = dataModel2.getPreferencesFromUser(userID2);
		// 计算item交集
		FastIDSet intersection = CommonSimFunction.intersectionFastIDSet(dataModel1, dataModel2, userID1, userID2);
		float avg1 = CommonSimFunction.avgPreferences(userID1, dataModel1);
		float avg2 = CommonSimFunction.avgPreferences(userID2, dataModel2);
		// 分别计算三个和
		double x = 0.0, y = 0.0, z = 0.0;
		LongPrimitiveIterator iterator = intersection.iterator();
		long itemID;
		while (iterator.hasNext()) {
			itemID = iterator.nextLong();
			float x1 = dataModel1.getPreferenceValue(userID1, itemID) - avg1;
			float x2 = dataModel2.getPreferenceValue(userID2, itemID) - avg2;
			x += x1 * x2;
			y += x1 * x1;
			z += x2 * x2;
		}

		return computeResult(intersection.size(), x, y, z, 0);
	}

	/**
	 * <p>
	 * Note that the sum of all X and Y values must then be 0. This value isn't
	 * passed down into the standard similarity computations as a result.
	 * </p>
	 * 
	 * @param n
	 *            total number of users or items
	 * 
	 * @param sumXY
	 *            sum of product of user/item preference values, over all
	 *            items/users preferred by both users/items
	 * 
	 * @param sumX2
	 *            sum of the square of user/item preference values, over the
	 *            first item/user
	 * 
	 * @param sumY2
	 *            sum of the square of the user/item preference values, over the
	 *            second item/user
	 * 
	 * @param sumXYdiff2
	 *            sum of squares of differences in X and Y values
	 * 
	 * @return similarity value between -1.0 and 1.0, inclusive, or
	 *         {@link Double#NaN} if no similarity can be computed (e.g. when no
	 *         items have been rated by both users
	 */
	double computeResult(int n, double sumXY, double sumX2, double sumY2, double sumXYdiff2) {
		if (n == 0) {
			return Double.NaN;
		}
		// Note that sum of X and sum of Y don't appear here since they are
		// assumed to be 0;
		// the data is assumed to be centered.
		double denominator = Math.sqrt(sumX2) * Math.sqrt(sumY2);
		if (denominator == 0.0) {
			// One or both parties has -all- the same ratings;
			// can't really say much similarity under this measure
			return Double.NaN;
		}
		return sumXY / denominator;
	}

	public void setPreferenceInferrer(PreferenceInferrer inferrer) {
		// TODO Auto-generated method stub

	}

}
