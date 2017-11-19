package com.liangjidong.neighborhood;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.google.common.base.Preconditions;

/**
 * <p>
 * Contains methods and resources useful to all classes in this package.
 * </p>
 */
abstract class AbstractUserNeighborhood implements UserNeighborhood {

	private final UserSimilarity userSimilarity;
	private final DataModel dataModel;
	private final double samplingRate;
	private final RefreshHelper refreshHelper;

	AbstractUserNeighborhood(UserSimilarity userSimilarity, DataModel dataModel, double samplingRate) {
		Preconditions.checkArgument(userSimilarity != null, "userSimilarity is null");
		Preconditions.checkArgument(dataModel != null, "dataModel is null");
		Preconditions.checkArgument(samplingRate > 0.0 && samplingRate <= 1.0, "samplingRate must be in (0,1]");
		this.userSimilarity = userSimilarity;
		this.dataModel = dataModel;
		this.samplingRate = samplingRate;
		this.refreshHelper = new RefreshHelper(null);
		this.refreshHelper.addDependency(this.dataModel);
		this.refreshHelper.addDependency(this.userSimilarity);
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

	public final void refresh(Collection<Refreshable> alreadyRefreshed) {
		refreshHelper.refresh(alreadyRefreshed);
	}

}
